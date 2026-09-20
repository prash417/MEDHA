package studio.paras.medha.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import studio.paras.medha.core.analytics.AnalyticsEvents
import studio.paras.medha.core.audio.MusicTrack
import studio.paras.medha.core.audio.Sfx
import studio.paras.medha.core.audio.VoiceCue
import studio.paras.medha.data.repository.toPlayable
import studio.paras.medha.di.AppContainer
import studio.paras.medha.domain.engine.LifelineLogic
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.ExpertAdvice
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.GameOutcome
import studio.paras.medha.domain.model.GamePhase
import studio.paras.medha.domain.model.GameResult
import studio.paras.medha.domain.model.LifelineKind
import studio.paras.medha.domain.model.PlayableQuestion
import studio.paras.medha.domain.model.PrizeLadder
import studio.paras.medha.domain.model.PrizeRung
import studio.paras.medha.domain.model.QuestionRecord

data class GameUiState(
    val mode: GameMode = GameMode.CLASSIC,
    val phase: GamePhase = GamePhase.IDLE,
    val playerName: String = "",
    val question: PlayableQuestion? = null,
    val selected: AnswerLetter? = null,
    val correct: AnswerLetter? = null,
    val explanation: String? = null,
    val hidden: Set<AnswerLetter> = emptySet(),
    val audience: Map<AnswerLetter, Float>? = null,
    val expert: ExpertAdvice? = null,
    val usedLifelines: Set<LifelineKind> = emptySet(),
    val index: Int = 0,
    val total: Int = 15,
    val correctCount: Int = 0,
    val error: String? = null,
    val ladderOpen: Boolean = false,
    val reducedMotion: Boolean = false,
    val secondsLeft: Int? = null,
    val timerTotal: Int? = null,
    val optionsVisible: Int = 4,
) {
    val canAnswer: Boolean get() = phase == GamePhase.QUESTION_DISPLAY
    val canLifeline: Boolean get() = phase == GamePhase.QUESTION_DISPLAY
    val playingFor: PrizeRung? get() = question?.prize
    val lockedIn: PrizeRung? get() = PrizeLadder.guaranteed(correctCount, mode)
    val walkAwayPrize: PrizeRung? get() = PrizeLadder.lastCleared(correctCount, mode)
    val nextPrize: PrizeRung? get() = PrizeLadder.forMode(mode).getOrNull(index)
    val dropTo: PrizeRung? get() = PrizeLadder.guaranteed(correctCount, mode)
    val isHighStakes: Boolean get() = index >= 10
    val timed: Boolean get() = timerTotal != null
}

class GameViewModel(
    private val mode: GameMode,
    private val container: AppContainer,
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState(mode = mode, phase = GamePhase.LOADING, total = mode.questionCount))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var records: List<QuestionRecord> = emptyList()
    private var usedIds = mutableSetOf<String>()
    private var startedAt = System.currentTimeMillis()
    private var sequence: Job? = null
    private var heartbeat: Job? = null
    private var timer: Job? = null
    private var remainingMs: Long = 0L

    init {
        start()
    }

    private fun start() {
        viewModelScope.launch {
            startedAt = System.currentTimeMillis()
            _state.update { it.copy(phase = GamePhase.LOADING, error = null) }
            val profile = container.playerStore.current()
            container.audio.sfxEnabled = profile.sfxEnabled
            container.audio.musicEnabled = profile.musicEnabled
            container.audio.setMusic(MusicTrack.LOBBY)
            container.analytics.track(
                if (mode == GameMode.DAILY) AnalyticsEvents.DAILY_STARTED else AnalyticsEvents.GAME_STARTED,
                mapOf("mode" to mode.id),
            )
            val result = container.questions.loadSession(mode, profile.seenQuestionIds, container.playCategory)
            result.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) {
                        _state.update { it.copy(phase = GamePhase.IDLE, error = "No questions are ready. Try again shortly.") }
                    } else {
                        records = list
                        usedIds = list.map { q -> q.id }.toMutableSet()
                        _state.update {
                            it.copy(
                                phase = GamePhase.SEAT_INTRO,
                                playerName = profile.displayName.ifBlank { "Guest" },
                                total = list.size,
                                reducedMotion = false,
                            )
                        }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(phase = GamePhase.IDLE, error = e.message ?: "Could not load questions.") }
                },
            )
        }
    }

    fun takeTheSeat() {
        if (_state.value.phase != GamePhase.SEAT_INTRO) return
        container.audio.play(Sfx.LAUNCH)
        container.audio.setMusic(MusicTrack.GAMEPLAY)
        stingThenShow(0)
    }

    private fun stingThenShow(index: Int) {
        stopClock()
        heartbeat?.cancel()
        sequence?.cancel()
        val record = records.getOrNull(index) ?: return finish(GameOutcome.WON)
        val playable = record.toPlayable(index, records.size, index, mode)
        val seconds = PrizeLadder.timerSeconds(index, mode)
        _state.update {
            it.copy(
                phase = GamePhase.QUESTION_STING,
                question = playable,
                selected = null,
                correct = null,
                explanation = null,
                hidden = emptySet(),
                audience = null,
                expert = null,
                index = index,
                total = records.size,
                ladderOpen = false,
                secondsLeft = seconds,
                timerTotal = seconds,
                optionsVisible = 0,
            )
        }
        if (index >= 10) container.audio.setMusic(MusicTrack.HIGH_STAKES)
        container.audio.play(Sfx.QUESTION)
        container.voice.speak(VoiceCue.QUESTION_NUMBER, "${index + 1}")
        sequence = viewModelScope.launch {
            val pause = if (_state.value.reducedMotion) 200L else 1400L
            delay(pause)
            _state.update { it.copy(phase = GamePhase.QUESTION_DISPLAY, optionsVisible = 0) }
            if (!_state.value.reducedMotion) {
                repeat(4) { n ->
                    delay(90)
                    _state.update { it.copy(optionsVisible = n + 1) }
                }
            } else {
                _state.update { it.copy(optionsVisible = 4) }
            }
            startClock(reset = true)
        }
    }

    private fun startClock(reset: Boolean = true) {
        val total = _state.value.timerTotal ?: return
        if (reset) remainingMs = total * 1000L
        timer?.cancel()
        timer = viewModelScope.launch {
            while (isActive && remainingMs > 0 && _state.value.phase == GamePhase.QUESTION_DISPLAY) {
                delay(250)
                remainingMs -= 250
                val secs = ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)
                val prev = _state.value.secondsLeft
                if (secs != prev) {
                    _state.update { it.copy(secondsLeft = secs) }
                    if (secs in 1..5) container.audio.play(Sfx.TIMER)
                }
            }
            if (_state.value.phase == GamePhase.QUESTION_DISPLAY && remainingMs <= 0) {
                onTimeout()
            }
        }
    }

    private fun pauseClock() {
        timer?.cancel()
    }

    private fun resumeClock() {
        if (_state.value.timerTotal != null && remainingMs > 0) {
            _state.update { it.copy(phase = GamePhase.QUESTION_DISPLAY) }
            startClock(reset = false)
        } else {
            _state.update { it.copy(phase = GamePhase.QUESTION_DISPLAY) }
        }
    }

    private fun stopClock() {
        timer?.cancel()
        timer = null
    }

    fun select(letter: AnswerLetter) {
        val s = _state.value
        if (!s.canAnswer || letter in s.hidden) return
        pauseClock()
        container.audio.play(Sfx.SELECT)
        container.voice.speak(VoiceCue.OPTION_SELECTED, letter.name)
        container.voice.speak(VoiceCue.FINAL_ANSWER)
        _state.update { it.copy(phase = GamePhase.CONFIRMING, selected = letter) }
    }

    fun changeAnswer() {
        if (_state.value.phase != GamePhase.CONFIRMING) return
        _state.update { it.copy(selected = null) }
        resumeClock()
    }

    fun sealAnswer() {
        val s = _state.value
        if (s.phase != GamePhase.CONFIRMING || s.selected == null) return
        stopClock()
        sequence?.cancel()
        _state.update { it.copy(phase = GamePhase.ANSWER_SELECTED) }
        val wait = if (s.reducedMotion) 180L else if (s.isHighStakes) 1600L else 1100L
        heartbeat?.cancel()
        heartbeat = viewModelScope.launch {
            delay(160)
            container.audio.play(Sfx.HEARTBEAT)
            delay(380)
            container.audio.play(Sfx.SUSPENSE)
        }
        sequence = viewModelScope.launch {
            delay(wait)
            reveal()
        }
    }

    private suspend fun onTimeout() {
        stopClock()
        val s = _state.value
        val record = records.getOrNull(s.index)
        delay(if (s.reducedMotion) 80L else 400L)
        container.audio.play(Sfx.WRONG)
        _state.update {
            it.copy(
                phase = GamePhase.WRONG,
                selected = null,
                correct = record?.correct,
                explanation = record?.explanation ?: "Time ran out before an answer was sealed.",
            )
        }
    }

    private suspend fun reveal() {
        val s = _state.value
        val qid = s.question?.id ?: return
        val selected = s.selected ?: return
        heartbeat?.cancel()
        _state.update { it.copy(phase = GamePhase.ANSWER_REVEAL) }
        val verdict = container.questions.validate(qid, selected).getOrElse { err ->
            _state.update { it.copy(error = err.message ?: "Could not verify that answer.", phase = GamePhase.QUESTION_DISPLAY) }
            return
        }
        delay(if (s.reducedMotion) 80L else 320L)
        container.analytics.track(AnalyticsEvents.QUESTION_ANSWERED, mapOf("correct" to verdict.correct.toString()))
        if (verdict.correct) {
            container.audio.play(Sfx.CORRECT)
            container.audio.play(Sfx.PRIZE)
            container.voice.speak(VoiceCue.CORRECT)
            container.analytics.track(AnalyticsEvents.ANSWER_CORRECT)
            _state.update {
                it.copy(
                    phase = GamePhase.CORRECT,
                    correct = verdict.correctLetter,
                    explanation = verdict.explanation,
                    correctCount = it.correctCount + 1,
                )
            }
            delay(if (s.reducedMotion) 450L else 1700L)
            val next = s.index + 1
            if (next >= records.size) {
                finish(GameOutcome.WON)
            } else {
                _state.update { it.copy(phase = GamePhase.NEXT_QUESTION) }
                container.audio.play(Sfx.PROGRESS)
                delay(if (s.reducedMotion) 120L else 420L)
                stingThenShow(next)
            }
        } else {
            container.audio.play(Sfx.WRONG)
            container.voice.speak(VoiceCue.INCORRECT)
            container.analytics.track(AnalyticsEvents.ANSWER_WRONG)
            _state.update {
                it.copy(
                    phase = GamePhase.WRONG,
                    correct = verdict.correctLetter,
                    explanation = verdict.explanation,
                )
            }
        }
    }

    fun continueAfterWrong() {
        if (_state.value.phase != GamePhase.WRONG) return
        finish(GameOutcome.DROPPED)
    }

    fun requestWalkAway() {
        val s = _state.value
        if (s.phase != GamePhase.QUESTION_DISPLAY && s.phase != GamePhase.CONFIRMING) return
        if (s.walkAwayPrize == null) return
        pauseClock()
        _state.update { it.copy(phase = GamePhase.WALK_AWAY_CONFIRM) }
    }

    fun cancelWalkAway() {
        if (_state.value.phase != GamePhase.WALK_AWAY_CONFIRM) return
        _state.update { it.copy(selected = null) }
        resumeClock()
    }

    fun confirmWalkAway() {
        if (_state.value.phase != GamePhase.WALK_AWAY_CONFIRM) return
        stopClock()
        finish(GameOutcome.WALKED_AWAY)
    }

    fun useLifeline(kind: LifelineKind) {
        val s = _state.value
        if (!s.canLifeline) return
        if (kind in s.usedLifelines) return
        val record = records.getOrNull(s.index) ?: return
        container.audio.play(Sfx.LIFELINE)
        container.analytics.track(AnalyticsEvents.LIFELINE_USED, mapOf("kind" to kind.name))
        when (kind) {
            LifelineKind.FIFTY_FIFTY -> {
                val hidden = LifelineLogic.fiftyFifty(record.correct, s.hidden)
                _state.update { it.copy(hidden = hidden, usedLifelines = it.usedLifelines + kind) }
            }
            LifelineKind.AUDIENCE -> {
                val bars = LifelineLogic.audienceBars(record.correct, record.difficulty, s.hidden)
                _state.update { it.copy(audience = bars, usedLifelines = it.usedLifelines + kind) }
            }
            LifelineKind.EXPERT -> {
                val advice = LifelineLogic.expertAdvice(record.correct, record.difficulty, s.hidden)
                _state.update { it.copy(expert = advice, usedLifelines = it.usedLifelines + kind) }
            }
            LifelineKind.SWITCH -> {
                val replacement = container.questions.replacement(record.id, usedIds, record.difficulty)
                if (replacement == null) {
                    _state.update { it.copy(error = "No alternate question is available.") }
                    return
                }
                usedIds += replacement.id
                records = records.toMutableList().also { it[s.index] = replacement }
                _state.update { it.copy(usedLifelines = it.usedLifelines + kind) }
                stopClock()
                stingThenShow(s.index)
            }
        }
    }

    fun toggleLadder() {
        _state.update { it.copy(ladderOpen = !it.ladderOpen) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun abandon() {
        if (_state.value.phase == GamePhase.GAME_COMPLETE) return
        if (_state.value.walkAwayPrize != null && _state.value.phase in setOf(
                GamePhase.QUESTION_DISPLAY, GamePhase.CONFIRMING, GamePhase.QUESTION_STING,
            )
        ) {
            requestWalkAway()
            return
        }
        container.analytics.track(AnalyticsEvents.GAME_ABANDONED, mapOf("mode" to mode.id))
        finish(if (_state.value.walkAwayPrize != null) GameOutcome.WALKED_AWAY else GameOutcome.DROPPED)
    }

    private fun finish(outcome: GameOutcome) {
        heartbeat?.cancel()
        sequence?.cancel()
        stopClock()
        val s = _state.value
        val prize = when (outcome) {
            GameOutcome.WON -> PrizeLadder.forMode(mode).last()
            GameOutcome.WALKED_AWAY -> PrizeLadder.lastCleared(s.correctCount, mode)
            GameOutcome.DROPPED -> PrizeLadder.guaranteed(s.correctCount, mode)
        }
        val result = GameResult(
            mode = mode,
            reachedIndex = s.index,
            prizeWon = prize?.amount ?: 0L,
            prizeLabel = prize?.label ?: "₹0",
            correctCount = s.correctCount,
            totalAnswered = s.correctCount + if (outcome == GameOutcome.DROPPED) 1 else 0,
            completed = outcome == GameOutcome.WON,
            outcome = outcome,
            durationMs = System.currentTimeMillis() - startedAt,
            lastExplanation = s.explanation,
            lastCorrectText = s.correct?.let { letter ->
                s.question?.options?.find { it.letter == letter }?.text
            },
            riskedLabel = s.playingFor?.label,
        )
        container.lastResult = result
        viewModelScope.launch {
            val before = container.playerStore.current()
            container.playerStore.applyGameResult(result)
            container.playerStore.update { p ->
                p.copy(seenQuestionIds = (p.seenQuestionIds + usedIds).distinct().takeLast(200))
            }
            val after = container.playerStore.current()
            if (after.level > before.level) {
                container.analytics.track(AnalyticsEvents.LEVEL_UP, mapOf("level" to after.level.toString()))
            }
        }
        when (outcome) {
            GameOutcome.WON -> {
                container.audio.setMusic(MusicTrack.VICTORY)
                container.audio.play(Sfx.VICTORY)
                container.analytics.track(
                    if (mode == GameMode.DAILY) AnalyticsEvents.DAILY_COMPLETED else AnalyticsEvents.GAME_COMPLETED,
                )
            }
            GameOutcome.WALKED_AWAY -> {
                container.audio.setMusic(MusicTrack.VICTORY)
                container.audio.play(Sfx.PRIZE)
            }
            GameOutcome.DROPPED -> {
                container.audio.setMusic(MusicTrack.GAME_OVER)
                container.audio.play(Sfx.GAME_OVER)
            }
        }
        _state.update { it.copy(phase = GamePhase.GAME_COMPLETE) }
    }

    override fun onCleared() {
        heartbeat?.cancel()
        sequence?.cancel()
        stopClock()
        super.onCleared()
    }

    class Factory(
        private val mode: GameMode,
        private val container: AppContainer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(mode, container) as T
    }
}
