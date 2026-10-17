package studio.paras.medha.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import studio.paras.medha.core.analytics.AnalyticsEvents
import studio.paras.medha.core.audio.HostBeat
import studio.paras.medha.core.audio.MusicTrack
import studio.paras.medha.core.audio.Sfx
import studio.paras.medha.core.audio.VoiceCue
import studio.paras.medha.data.repository.leaksAnswer
import studio.paras.medha.data.repository.toPlayable
import studio.paras.medha.di.AppContainer
import studio.paras.medha.domain.engine.LifelineLogic
import studio.paras.medha.domain.engine.LifelineShop
import studio.paras.medha.domain.engine.RunRewards
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

data class XpAward(val question: Int, val amount: Int, val lifeline: Boolean)

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
    val earnedXp: Int = 0,
    val xpAwards: List<XpAward> = emptyList(),
    val startXp: Int = 0,
    val error: String? = null,
    val ladderOpen: Boolean = false,
    val reducedMotion: Boolean = false,
    val secondsLeft: Int? = null,
    val timerTotal: Int? = null,
    val optionsVisible: Int = 0,
    val questionPaceMs: Long = 0L,
    val optionPaceMs: List<Long> = emptyList(),
    val hintText: String? = null,
    val hintUsed: Boolean = false,
    val promptShown: String = "",
    val resultPrize: String? = null,
    val dareOffer: Boolean = false,
    val dareArmed: Boolean = false,
    val protectArmed: Boolean = false,
    val lifelinesOnQuestion: Int = 0,
    /** Player's coin balance, kept live so lifeline prices can be checked. */
    val coins: Int = 0,
    /** How many times each lifeline was used this run (first use is free). */
    val lifelineUses: Map<LifelineKind, Int> = emptyMap(),
    val hintsUsed: Int = 0,
    /** Set when the run ends, so the game-over card can say how it ended. */
    val outcome: GameOutcome? = null,
    val coinsEarned: Int = 0,
    /** Short message such as "Not enough coins". Cleared after a moment by the screen. */
    val notice: String? = null,
) {
    /** Coins the next use of [kind] costs; 0 while it is still free. */
    fun lifelineCost(kind: LifelineKind): Int = LifelineShop.nextCost(kind, lifelineUses[kind] ?: 0)
    val hintCost: Int get() = LifelineShop.nextHintCost(hintsUsed)
    fun canAfford(cost: Int): Boolean = cost <= 0 || coins >= cost

    val canAnswer: Boolean get() = phase == GamePhase.QUESTION_DISPLAY
    /** Question text and all four options are on screen, and the clock has started when this question is timed. */
    val answersOpen: Boolean
        get() = optionsVisible >= 4 && promptShown.isNotBlank() && (timerTotal == null || secondsLeft != null)
    val canLifeline: Boolean get() = phase == GamePhase.QUESTION_DISPLAY
    val canSpendLifeline: Boolean get() = lifelinesOnQuestion < PrizeLadder.lifelineSlots(index)
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
    private val stepBackOnTimeout: Boolean = false,
    private val category: String? = null,
) : ViewModel() {

    companion object {
        private const val DareSkip = 1
        private const val DareBack = 3
        private const val DareSeconds = 15
        const val DailyCategory = "daily"
    }

    private val _state = MutableStateFlow(GameUiState(mode = mode, phase = GamePhase.LOADING, total = mode.questionCount))
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var records: List<QuestionRecord> = emptyList()
    private var usedIds = mutableSetOf<String>()
    private var seenIds = mutableSetOf<String>()
    private var shownIds = mutableSetOf<String>()
    private val hintsShown = mutableSetOf<String>()
    private var startedAt = System.currentTimeMillis()
    private var sequence: Job? = null
    private var heartbeat: Job? = null
    private var timer: Job? = null
    private var remainingMs: Long = 0L
    @Volatile private var left = false
    private var sessionOpen = false
    private var runId = 0

    @Volatile private var latestProfile: studio.paras.medha.domain.model.PlayerProfile? = null

    /** The Daily Challenge draws from every category. */
    private val pool: String? = category?.takeUnless { it.isBlank() || it == DailyCategory }

    init {
        viewModelScope.launch {
            container.playerStore.profile.collect { profile ->
                latestProfile = profile
                _state.update { it.copy(coins = profile.coins) }
            }
        }
        start()
    }

    private fun start() {
        if (sessionOpen) return
        sessionOpen = true
        left = false
        val run = ++runId
        viewModelScope.launch {
            startedAt = System.currentTimeMillis()
            if (run != runId) return@launch
            _state.value = GameUiState(phase = GamePhase.LOADING)
            val profile = container.playerStore.current()
            if (run != runId) return@launch
            _state.update { it.copy(startXp = profile.xp) }
            container.audio.sfxEnabled = profile.sfxEnabled
            container.audio.musicEnabled = profile.musicEnabled
            container.audio.timerMusicEnabled = profile.timerMusicEnabled
            container.audio.clockTicksEnabled = profile.clockTicksEnabled
            container.audio.hapticsEnabled = profile.hapticsEnabled
            container.audio.reducedMotion = profile.reducedMotion
            container.voice.enabled = profile.voiceEnabled
            container.analytics.track(
                if (mode == GameMode.DAILY) AnalyticsEvents.DAILY_STARTED else AnalyticsEvents.GAME_STARTED,
                mapOf("mode" to mode.id),
            )
            val drawFrom = pool ?: container.playCategory?.takeUnless { it == DailyCategory }
            seenIds = profile.seenQuestionIds.toMutableSet()
            shownIds.clear()
            val result = container.questions.loadSession(mode, profile.seenQuestionIds, drawFrom)
            result.fold(
                onSuccess = { draw ->
                    if (run != runId) return@fold
                    if (draw.questions.isEmpty()) {
                        _state.update { it.copy(phase = GamePhase.IDLE, error = "No questions are ready. Try again shortly.") }
                    } else {
                        if (draw.dropSeenIds.isNotEmpty()) {
                            seenIds.removeAll(draw.dropSeenIds)
                            container.playerStore.update { player ->
                                player.copy(seenQuestionIds = player.seenQuestionIds.filter { it !in draw.dropSeenIds })
                            }
                        }
                        records = draw.questions
                        usedIds = draw.questions.map { q -> q.id }.toMutableSet()
                        prefetchVoice(0)
                        prefetchVoice(1)
                        _state.value = GameUiState(
                            mode = mode,
                            phase = GamePhase.SEAT_INTRO,
                            playerName = profile.displayName.ifBlank { "Guest" },
                            total = draw.questions.size,
                            reducedMotion = profile.reducedMotion,
                            startXp = profile.xp,
                            coins = profile.coins,
                        )
                    }
                },
                onFailure = { e ->
                    if (run != runId) return@fold
                    _state.update { it.copy(phase = GamePhase.IDLE, error = e.message ?: "Could not load questions.") }
                },
            )
        }
    }

    fun restartFresh() {
        heartbeat?.cancel()
        sequence?.cancel()
        stopClock()
        container.voice.stop()
        left = true
        sessionOpen = false
        runId++
        _state.value = GameUiState(mode = mode, phase = GamePhase.LOADING, total = mode.questionCount)
        start()
    }

    fun ensureRunning() {
        if (sessionOpen) return
        start()
    }

    /** A slot that was already asked gets a different question from the ones still unused. */
    private fun questionFor(index: Int): QuestionRecord? {
        val current = records.getOrNull(index) ?: return null
        if (current.id !in shownIds) return current
        val next = container.questions.replacement(
            current.id,
            usedIds,
            PrizeLadder.difficultyForIndex(index),
            pool,
            seenIds,
        ) ?: return current
        if (next.id == current.id) return current
        usedIds += next.id
        records = records.toMutableList().also { it[index] = next }
        return next
    }

    private fun noteSeen(id: String) {
        if (!seenIds.add(id)) return
        viewModelScope.launch {
            container.playerStore.update { player ->
                if (id in player.seenQuestionIds) player
                else player.copy(seenQuestionIds = player.seenQuestionIds + id)
            }
        }
    }

    fun takeTheSeat() {
        if (left || _state.value.phase != GamePhase.SEAT_INTRO) return
        container.audio.setMusic(MusicTrack.TICKING_MYSTERY)
        stingThenShow(0)
    }

    private fun stingThenShow(index: Int) {
        if (left) return
        stopClock()
        heartbeat?.cancel()
        sequence?.cancel()
        container.voice.stop()
        val record = questionFor(index) ?: return finish(GameOutcome.WON)
        if (shownIds.add(record.id)) noteSeen(record.id)
        hintsShown.clear()
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
                secondsLeft = null,
                timerTotal = seconds,
                optionsVisible = 0,
                questionPaceMs = 0L,
                optionPaceMs = emptyList(),
                hintText = null,
                promptShown = "",
                dareOffer = false,
                dareArmed = false,
                protectArmed = it.protectArmed,
                lifelinesOnQuestion = if (index == it.index) it.lifelinesOnQuestion else 0,
            )
        }
        container.audio.setMusic(MusicTrack.TICKING_MYSTERY)
        val fullPrompt = playable.prompt
        val shown = index
        _state.update { it.copy(phase = GamePhase.QUESTION_DISPLAY, optionsVisible = 0, promptShown = "") }
        prefetchVoice(index + 1)
        prefetchVoice(index + 2)
        container.voice.readQuestion(
            number = index + 1,
            prompt = playable.prompt,
            options = playable.options,
            onBeat = { beat ->
                when (beat) {
                    is HostBeat.Question -> _state.update {
                        it.copy(
                            promptShown = fullPrompt,
                            questionPaceMs = beat.durationMs.coerceAtLeast(1L),
                        )
                    }
                    is HostBeat.Option -> _state.update {
                        val paces = it.optionPaceMs.toMutableList()
                        while (paces.size <= beat.index) paces += 0L
                        paces[beat.index] = beat.durationMs.coerceAtLeast(1L)
                        it.copy(
                            promptShown = fullPrompt,
                            optionsVisible = maxOf(it.optionsVisible, beat.index + 1),
                            optionPaceMs = paces,
                        )
                    }
                }
            },
        ) {
            viewModelScope.launch {
                val now = _state.value
                if (!left && now.index == shown && now.phase.clockLive()) {
                    _state.update { it.copy(promptShown = fullPrompt, optionsVisible = 4) }
                    if (now.secondsLeft == null) startClock(reset = true)
                }
            }
        }
    }

    private fun prefetchVoice(index: Int) {
        val record = records.getOrNull(index) ?: return
        val playable = record.toPlayable(index, records.size, index, mode)
        container.voice.prefetchQuestion(index + 1, playable.prompt, playable.options)
    }

    private fun countsXp(): Boolean = container.awardXp && mode != GameMode.PRACTICE

    private fun startClock(reset: Boolean = true) {
        if (left) return
        val total = _state.value.timerTotal ?: return
        if (reset) remainingMs = total * 1000L
        val secsNow = ((remainingMs + 999) / 1000).toInt()
        _state.update { it.copy(secondsLeft = secsNow) }
        cueTimerMusic(secsNow, restart = reset)
        timer?.cancel()
        timer = viewModelScope.launch {
            while (isActive && remainingMs > 0 && _state.value.phase.clockLive()) {
                delay(250)
                remainingMs -= 250
                val secs = ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)
                val prev = _state.value.secondsLeft
                if (secs != prev) {
                    _state.update { it.copy(secondsLeft = secs) }
                    val hitThirty = secs == 30 && (prev ?: 0) > 30
                    cueTimerMusic(secs, restart = hitThirty)
                    if (secs in 1..5 && container.audio.clockTicksEnabled) container.audio.play(Sfx.TIMER)
                }
            }
            if (_state.value.phase.clockLive() && remainingMs <= 0) {
                timer = null
                remainingMs = 0
                onTimeout()
            }
        }
    }

    private fun cueTimerMusic(secondsLeft: Int, restart: Boolean = false) {
        if (left) return
        if (_state.value.index >= 10) return
        if (_state.value.timerTotal == null) return
        if (secondsLeft <= 30 && container.audio.timerMusicEnabled) {
            container.audio.setMusic(MusicTrack.THINK_CAREFULLY, restart = restart)
        } else if (container.audio.musicEnabled) {
            container.audio.setMusic(MusicTrack.TICKING_MYSTERY)
        }
    }

    private fun restorePageMusic() {
        if (left) return
        container.audio.setMusic(MusicTrack.TICKING_MYSTERY)
    }

    private fun pauseClock() {
        timer?.cancel()
        restorePageMusic()
    }

    private fun resumeClock() {
        if (_state.value.timerTotal != null && remainingMs > 0) {
            _state.update { it.copy(phase = GamePhase.QUESTION_DISPLAY) }
            startClock(reset = false)
            container.audio.resumeMusic()
        } else {
            _state.update { it.copy(phase = GamePhase.QUESTION_DISPLAY) }
        }
    }

    private fun stopClock() {
        timer?.cancel()
        timer = null
    }

    private fun GamePhase.clockLive(): Boolean =
        this == GamePhase.QUESTION_DISPLAY || this == GamePhase.CONFIRMING

    fun select(letter: AnswerLetter) {
        val s = _state.value
        if (!s.answersOpen || !s.phase.clockLive() || letter in s.hidden) return
        if (s.phase == GamePhase.CONFIRMING && s.selected == letter) return
        container.voice.stop()
        container.voice.speak(VoiceCue.OPTION_SELECTED, letter.name)
        _state.update {
            it.copy(
                phase = GamePhase.CONFIRMING,
                selected = letter,
                promptShown = it.question?.prompt.orEmpty(),
            )
        }
    }

    fun changeAnswer() {
        if (_state.value.phase != GamePhase.CONFIRMING) return
        _state.update { it.copy(selected = null, phase = GamePhase.QUESTION_DISPLAY) }
    }

    fun sealAnswer() {
        val s = _state.value
        if (s.phase != GamePhase.CONFIRMING || s.selected == null) return
        stopClock()
        sequence?.cancel()
        container.voice.stop()
        restorePageMusic()
        container.voice.speak(VoiceCue.FINAL_ANSWER)
        container.audio.buzz()
        _state.update { it.copy(phase = GamePhase.ANSWER_SELECTED) }
        val wait = if (container.audio.reducedMotion) 180L else if (s.isHighStakes) 1600L else 1100L
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

    private fun onTimeout() {
        if (left || _state.value.phase == GamePhase.GAME_COMPLETE) return
        container.voice.stop()
        restorePageMusic()
        _state.update {
            it.copy(
                secondsLeft = 0,
                selected = null,
                explanation = "Time ran out before an answer was sealed.",
            )
        }
        if (_state.value.protectArmed) {
            absorbProtect()
            return
        }
        if (_state.value.dareArmed) {
            applyDareFail()
        } else {
            retreatOrDrop()
        }
    }

    /** Wrong or time-up after question 1 steps back to the question before the last correct one. */
    private fun retreatOrDrop() {
        if (!stepBackOnTimeout || _state.value.index <= 0) {
            finish(GameOutcome.DROPPED)
            return
        }
        stepBackQuestion()
    }

    private fun stepBackQuestion() {
        val s = _state.value
        val land = (s.index - 2).coerceAtLeast(0)
        val kept = s.xpAwards.filter { it.question <= land }
        val drop = (s.earnedXp - kept.sumOf { it.amount }).coerceAtLeast(0)
        _state.update {
            it.copy(
                correctCount = land,
                earnedXp = (it.earnedXp - drop).coerceAtLeast(0),
                xpAwards = kept,
                dareArmed = false,
                hintUsed = false,
                hintText = null,
            )
        }
        stingThenShow(land)
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
        delay(if (container.audio.reducedMotion) 80L else 320L)
        container.analytics.track(AnalyticsEvents.QUESTION_ANSWERED, mapOf("correct" to verdict.correct.toString()))
        if (verdict.correct) {
            container.analytics.track(AnalyticsEvents.ANSWER_CORRECT)
            container.audio.buzz()
            val gain = if (!countsXp()) 0 else if (s.lifelinesOnQuestion > 0) 50 else 100
            val award = XpAward(question = s.index + 1, amount = gain, lifeline = s.lifelinesOnQuestion > 0)
            _state.update {
                it.copy(
                    phase = GamePhase.CORRECT,
                    correct = verdict.correctLetter,
                    explanation = verdict.explanation,
                    correctCount = it.correctCount + 1,
                    earnedXp = it.earnedXp + gain,
                    xpAwards = if (gain > 0) it.xpAwards + award else it.xpAwards,
                )
            }
            awaitHost(VoiceCue.CORRECT)
            if (left) return
            if (s.dareArmed) {
                applyDareSkip(s.index, s.correctCount + 1)
                return
            }
            val next = s.index + 1
            if (next >= records.size) {
                finish(GameOutcome.WON)
            } else {
                _state.update { it.copy(phase = GamePhase.NEXT_QUESTION) }
                stingThenShow(next)
            }
        } else {
            container.audio.play(Sfx.WRONG)
            container.audio.buzz(long = true)
            container.analytics.track(AnalyticsEvents.ANSWER_WRONG)
            _state.update {
                it.copy(
                    phase = GamePhase.WRONG,
                    correct = verdict.correctLetter,
                    explanation = verdict.explanation,
                )
            }
            awaitHost(VoiceCue.INCORRECT, spokenOption(verdict.correctLetter, s.question))
            if (left) return
            if (absorbProtect()) return
            if (s.dareArmed) {
                applyDareFail()
            } else {
                retreatOrDrop()
            }
        }
    }

    private fun spokenOption(
        letter: AnswerLetter,
        question: PlayableQuestion? = _state.value.question,
    ): String {
        val text = question?.options?.find { it.letter == letter }?.text?.trim()?.trimEnd('.', ' ') ?: ""
        return if (text.isBlank()) "option ${letter.name}" else "option ${letter.name}, $text"
    }

    private suspend fun awaitHost(cue: VoiceCue, detail: String = "") {
        try {
            withTimeout(18_000) {
                suspendCancellableCoroutine { cont ->
                    container.voice.speak(cue, detail) {
                        if (cont.isActive) cont.resume(Unit)
                    }
                    cont.invokeOnCancellation { container.voice.stop() }
                }
            }
        } catch (_: TimeoutCancellationException) {
            container.voice.stop()
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

    fun useHint() {
        val s = _state.value
        if (!s.answersOpen || s.phase != GamePhase.QUESTION_DISPLAY) return
        val record = records.getOrNull(s.index) ?: return
        val answer = when (record.correct) {
            AnswerLetter.A -> record.optionA
            AnswerLetter.B -> record.optionB
            AnswerLetter.C -> record.optionC
            AnswerLetter.D -> record.optionD
        }
        val pool = s.question?.hints.orEmpty()
            .map { it.trim() }
            .filter { it.isNotBlank() && !leaksAnswer(it, answer) }
            .ifEmpty {
                listOf("Read the question again and drop the option that belongs to a different fact.")
            }
        val fresh = pool.filter { it !in hintsShown }
        val source = fresh.ifEmpty {
            hintsShown.clear()
            pool
        }
        val cost = s.hintCost
        if (!s.canAfford(cost)) {
            _state.update { it.copy(notice = "Not enough coins — a hint costs $cost") }
            return
        }
        spendCoins(cost)
        val one = source.random()
        hintsShown += one
        container.audio.play(Sfx.LIFELINE)
        container.analytics.track(AnalyticsEvents.LIFELINE_USED, mapOf("kind" to "HINT"))
        _state.update {
            it.copy(
                hintText = one,
                hintsUsed = it.hintsUsed + 1,
                lifelinesOnQuestion = if (it.lifelinesOnQuestion == 0) 1 else it.lifelinesOnQuestion,
            )
        }
    }

    fun dismissHint() {
        if (_state.value.hintText == null) return
        _state.update { it.copy(hintText = null) }
    }

    fun useLifeline(kind: LifelineKind) {
        val s = _state.value
        if (!s.answersOpen) return
        val thinking = s.phase in setOf(GamePhase.QUESTION_DISPLAY, GamePhase.CONFIRMING)
        val extraReady = kind == LifelineKind.EXTRA_TIME && thinking && s.timerTotal != null
        val lateReady = kind in setOf(
            LifelineKind.FIFTY_FIFTY,
            LifelineKind.SWITCH,
            LifelineKind.PROTECT,
        ) && thinking
        if (!s.canLifeline && !extraReady && !lateReady) return
        if (kind in s.usedLifelines && !LifelineShop.repeatable(kind)) return
        if (kind == LifelineKind.EXTRA_TIME && s.timerTotal == null) return
        // Stacking the same lifeline on one question would give the answer away.
        if (kind == LifelineKind.FIFTY_FIFTY && s.hidden.isNotEmpty()) return
        if (kind == LifelineKind.PROTECT && s.protectArmed) return
        if (!s.canSpendLifeline) return
        val record = records.getOrNull(s.index) ?: return
        val cost = s.lifelineCost(kind)
        if (!s.canAfford(cost)) {
            _state.update { it.copy(notice = "Not enough coins — this costs $cost") }
            return
        }
        spendCoins(cost)
        _state.update { it.copy(lifelineUses = it.lifelineUses + (kind to ((it.lifelineUses[kind] ?: 0) + 1))) }
        container.audio.play(Sfx.LIFELINE)
        container.analytics.track(AnalyticsEvents.LIFELINE_USED, mapOf("kind" to kind.name))
        when (kind) {
            LifelineKind.FIFTY_FIFTY -> {
                val hidden = LifelineLogic.fiftyFifty(record.correct, s.hidden)
                val lostPick = s.selected != null && s.selected in hidden
                _state.update {
                    it.copy(
                        hidden = hidden,
                        usedLifelines = it.usedLifelines + kind,
                        selected = if (lostPick) null else it.selected,
                        phase = if (lostPick) GamePhase.QUESTION_DISPLAY else it.phase,
                    )
                }
                if (lostPick) resumeClock()
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
                val replacement = container.questions.replacement(
                    record.id,
                    usedIds,
                    record.difficulty,
                    pool,
                    seenIds,
                )
                if (replacement == null) {
                    if (cost > 0) spendCoins(-cost)
                    _state.update {
                        it.copy(
                            notice = "No other question is available right now.",
                            lifelineUses = it.lifelineUses + (kind to ((it.lifelineUses[kind] ?: 1) - 1)),
                        )
                    }
                    return
                }
                usedIds += replacement.id
                records = records.toMutableList().also { it[s.index] = replacement }
                _state.update { it.copy(usedLifelines = it.usedLifelines + kind) }
                stopClock()
                stingThenShow(s.index)
            }
            LifelineKind.EXTRA_TIME -> addExtraTime()
            LifelineKind.DARE -> Unit
            LifelineKind.PROTECT -> {
                _state.update {
                    it.copy(
                        protectArmed = true,
                        usedLifelines = it.usedLifelines + kind,
                    )
                }
            }
        }
        if (kind != LifelineKind.DARE) {
            _state.update { it.copy(lifelinesOnQuestion = it.lifelinesOnQuestion + 1) }
        }
    }

    fun offerDare() {
        val s = _state.value
        if (!s.answersOpen) return
        if (s.dareOffer || s.dareArmed) return
        if (LifelineKind.DARE in s.usedLifelines) return
        if (s.phase !in setOf(GamePhase.QUESTION_DISPLAY, GamePhase.CONFIRMING)) return
        if (!s.canSpendLifeline) return
        if (s.phase == GamePhase.QUESTION_DISPLAY) pauseClock()
        _state.update { it.copy(dareOffer = true, hintText = null) }
    }

    fun confirmDare() {
        val s = _state.value
        if (!s.dareOffer || LifelineKind.DARE in s.usedLifelines) return
        if (!s.canSpendLifeline) return
        container.audio.play(Sfx.LIFELINE)
        container.analytics.track(AnalyticsEvents.LIFELINE_USED, mapOf("kind" to "DARE"))
        _state.update {
            it.copy(
                dareOffer = false,
                dareArmed = true,
                usedLifelines = it.usedLifelines + LifelineKind.DARE,
                lifelinesOnQuestion = it.lifelinesOnQuestion + 1,
            )
        }
        tightenDareClock()
        if (s.phase == GamePhase.QUESTION_DISPLAY) resumeClock()
    }

    fun dismissDare() {
        val s = _state.value
        if (!s.dareOffer) return
        _state.update { it.copy(dareOffer = false) }
        if (s.phase == GamePhase.QUESTION_DISPLAY) resumeClock()
    }

    private fun applyDareSkip(fromIndex: Int, cleared: Int) {
        _state.update { it.copy(dareArmed = false) }
        val skipTo = fromIndex + 1 + DareSkip
        if (skipTo >= records.size) {
            _state.update { it.copy(correctCount = records.size) }
            finish(GameOutcome.WON)
            return
        }
        _state.update { it.copy(correctCount = cleared + 1, phase = GamePhase.NEXT_QUESTION) }
        stingThenShow(skipTo)
    }

    private fun applyDareFail() {
        val s = _state.value
        val back = s.index - DareBack
        _state.update { it.copy(dareArmed = false) }
        if (back < 0) {
            finish(GameOutcome.DROPPED)
            return
        }
        _state.update {
            it.copy(
                correctCount = (s.correctCount - DareBack).coerceAtLeast(0),
                phase = GamePhase.NEXT_QUESTION,
            )
        }
        stingThenShow(back)
    }

    private fun tightenDareClock() {
        val s = _state.value
        val total = s.timerTotal ?: return
        val capMs = DareSeconds * 1000L
        if (s.secondsLeft == null) {
            _state.update { it.copy(timerTotal = DareSeconds.coerceAtMost(total)) }
            return
        }
        if (remainingMs > capMs) remainingMs = capMs
        val secs = ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)
        _state.update {
            it.copy(
                timerTotal = DareSeconds.coerceAtMost(total).coerceAtLeast(secs),
                secondsLeft = secs,
            )
        }
    }

    private fun absorbProtect(): Boolean {
        val s = _state.value
        if (!s.protectArmed) return false
        val record = records.getOrNull(s.index)
        val difficulty = record?.difficulty ?: PrizeLadder.difficultyForIndex(s.index)
        val currentId = record?.id.orEmpty()
        val replacement = container.questions.replacement(currentId, usedIds, difficulty, pool, seenIds)
        if (replacement != null && replacement.id != currentId) {
            usedIds += replacement.id
            records = records.toMutableList().also { it[s.index] = replacement }
        }
        _state.update {
            it.copy(
                protectArmed = false,
                selected = null,
                correct = null,
                explanation = null,
                phase = GamePhase.NEXT_QUESTION,
            )
        }
        stingThenShow(s.index)
        return true
    }

    private fun addExtraTime() {
        val s = _state.value
        val total = s.timerTotal ?: return
        val clockLive = s.secondsLeft != null
        if (clockLive) remainingMs += 30_000L
        val secs = if (clockLive) {
            ((remainingMs + 999) / 1000).toInt().coerceAtLeast(0)
        } else {
            s.secondsLeft
        }
        _state.update {
            it.copy(
                timerTotal = total + 30,
                secondsLeft = secs,
                usedLifelines = it.usedLifelines + LifelineKind.EXTRA_TIME,
            )
        }
        if (clockLive && s.phase == GamePhase.QUESTION_DISPLAY) {
            cueTimerMusic(secs ?: 0)
        }
    }

    private fun spendCoins(amount: Int) {
        if (amount == 0) return
        _state.update { it.copy(coins = (it.coins - amount).coerceAtLeast(0)) }
        viewModelScope.launch {
            container.playerStore.update { it.copy(coins = (it.coins - amount).coerceAtLeast(0)) }
        }
    }

    fun clearNotice() {
        _state.update { it.copy(notice = null) }
    }

    fun toggleLadder() {
        _state.update { it.copy(ladderOpen = !it.ladderOpen) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun leaveSession() {
        if (left) return
        left = true
        sessionOpen = false
        heartbeat?.cancel()
        sequence?.cancel()
        stopClock()
        container.voice.stop()
        if (_state.value.phase != GamePhase.GAME_COMPLETE) {
            container.audio.haltPlayback()
        }
    }

    /** Leave now and keep the prize for every question already answered. */
    fun quitKeepingReward() {
        if (left || _state.value.phase == GamePhase.GAME_COMPLETE) return
        if (_state.value.phase == GamePhase.LOADING || _state.value.phase == GamePhase.IDLE) return
        finish(GameOutcome.WALKED_AWAY)
    }

    fun abandon() {
        if (left || _state.value.phase == GamePhase.GAME_COMPLETE) return
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
        if (left || _state.value.phase == GamePhase.GAME_COMPLETE) return
        heartbeat?.cancel()
        sequence?.cancel()
        stopClock()
        container.voice.stop()
        val s = _state.value
        val prize = when (outcome) {
            GameOutcome.WON -> PrizeLadder.forMode(mode).last()
            GameOutcome.WALKED_AWAY -> PrizeLadder.lastCleared(s.correctCount, mode)
            GameOutcome.DROPPED -> PrizeLadder.guaranteed(s.correctCount, mode)
        }
        val today = java.time.LocalDate.now().toEpochDay()
        val coinsEarned = RunRewards.coins(
            mode = mode,
            prize = prize?.amount ?: 0L,
            completed = outcome == GameOutcome.WON,
            profile = latestProfile ?: studio.paras.medha.domain.model.PlayerProfile(),
            today = today,
        )
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
            sessionXp = if (countsXp()) s.earnedXp else 0,
            coinsEarned = coinsEarned,
        )
        container.lastResult = result
        viewModelScope.launch {
            val before = container.playerStore.current()
            container.playerStore.applyGameResult(result)
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
                container.audio.setMusic(MusicTrack.TICKING_MYSTERY)
            }
        }
        _state.update {
            it.copy(
                phase = GamePhase.GAME_COMPLETE,
                resultPrize = prize?.label ?: "₹0",
                outcome = outcome,
                coinsEarned = coinsEarned,
            )
        }
    }

    override fun onCleared() {
        leaveSession()
        super.onCleared()
    }

    class Factory(
        private val mode: GameMode,
        private val container: AppContainer,
        private val stepBackOnTimeout: Boolean = false,
        private val category: String? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameViewModel(mode, container, stepBackOnTimeout, category) as T
    }
}
