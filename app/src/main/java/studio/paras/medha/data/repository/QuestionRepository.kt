package studio.paras.medha.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import studio.paras.medha.data.local.SampleQuestionBank
import studio.paras.medha.domain.engine.XpRules
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.AnswerVerdict
import studio.paras.medha.domain.model.Difficulty
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.GameResult
import studio.paras.medha.domain.model.PlayableQuestion
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.domain.model.PrizeLadder
import studio.paras.medha.domain.model.QuestionRecord
import java.time.LocalDate
import kotlin.random.Random

data class SessionDraw(
    val questions: List<QuestionRecord>,
    /** Ids from this category to forget because the full pool was used and a new pass started. */
    val dropSeenIds: Set<String> = emptySet(),
)

interface QuestionRepository {
    suspend fun loadSession(mode: GameMode, seenIds: List<String>, category: String? = null): Result<SessionDraw>
    suspend fun validate(questionId: String, selected: AnswerLetter): Result<AnswerVerdict>
    fun peek(questionId: String): QuestionRecord?
    fun replacement(
        currentId: String,
        usedIds: Set<String>,
        difficulty: Difficulty,
        category: String? = null,
        seenIds: Set<String> = emptySet(),
    ): QuestionRecord?
}

/**
 * Phase 1 local source. Phase 2 will swap this for a Firestore-backed
 * implementation that never returns `correct` to the UI layer.
 */
class LocalQuestionRepository : QuestionRepository {

    private val byId = SampleQuestionBank.all.associateBy { it.id }

    override suspend fun loadSession(mode: GameMode, seenIds: List<String>, category: String?): Result<SessionDraw> {
        delay(280)
        val ladder = PrizeLadder.forMode(mode)
        val pool = SampleQuestionBank.all.filter {
            it.active && it.language == "en" && (category.isNullOrBlank() || it.category == category)
        }
        if (pool.size < ladder.size) {
            return Result.failure(IllegalStateException("Not enough questions in the chamber."))
        }
        val random = if (mode == GameMode.DAILY) {
            Random(LocalDate.now().toEpochDay())
        } else {
            Random.Default
        }
        return Result.success(drawUnseen(pool, seenIds, ladder.size, random))
    }

    override suspend fun validate(questionId: String, selected: AnswerLetter): Result<AnswerVerdict> {
        delay(120)
        val q = byId[questionId] ?: return Result.failure(IllegalStateException("That question is no longer in the chamber."))
        return Result.success(
            AnswerVerdict(
                correct = q.correct == selected,
                correctLetter = q.correct,
                selected = selected,
                explanation = q.explanation,
            ),
        )
    }

    override fun peek(questionId: String): QuestionRecord? = byId[questionId]

    override fun replacement(
        currentId: String,
        usedIds: Set<String>,
        difficulty: Difficulty,
        category: String?,
        seenIds: Set<String>,
    ): QuestionRecord? {
        val pool = SampleQuestionBank.all.filter {
            it.active && it.language == "en" && (category.isNullOrBlank() || it.category == category)
        }
        return pickFresh(pool, usedIds + currentId, seenIds, difficulty)
    }
}

fun drawUnseen(
    pool: List<QuestionRecord>,
    seenIds: List<String>,
    count: Int,
    random: Random,
): SessionDraw {
    val seen = seenIds.toSet()
    val unseen = pool.filter { it.id !in seen }
    if (unseen.size >= count) {
        return SessionDraw(pickByDifficulty(unseen, count, random))
    }
    val used = mutableSetOf<String>()
    val out = mutableListOf<QuestionRecord>()
    repeat(count) { index ->
        val want = PrizeLadder.difficultyForIndex(index)
        val fresh = pool.filter { it.id !in seen && it.id !in used }
        val pick = chooseQuestion(fresh, want, random)
            ?: chooseQuestion(pool.filter { it.id !in used }, want, random)
        if (pick != null) {
            used += pick.id
            out += pick
        }
    }
    return SessionDraw(out, pool.map { it.id }.toSet())
}

private fun chooseQuestion(
    pool: List<QuestionRecord>,
    want: Difficulty,
    random: Random,
): QuestionRecord? {
    if (pool.isEmpty()) return null
    return pool.filter { it.difficulty == want }.randomOrNull(random)
        ?: pool.minByOrNull { kotlin.math.abs(it.difficulty.ordinal - want.ordinal) }
}

fun pickFresh(
    pool: List<QuestionRecord>,
    inGame: Set<String>,
    seenIds: Set<String>,
    difficulty: Difficulty,
): QuestionRecord? {
    val unseen = pool.filter { it.id !in seenIds && it.id !in inGame }
    val notThisGame = pool.filter { it.id !in inGame }
    val source = if (unseen.isNotEmpty()) unseen else notThisGame
    if (source.isEmpty()) return null
    return source.filter { it.difficulty == difficulty }.randomOrNull() ?: source.random()
}

fun pickByDifficulty(
    pool: List<QuestionRecord>,
    count: Int,
    random: Random,
): List<QuestionRecord> {
    val used = mutableSetOf<String>()
    val out = mutableListOf<QuestionRecord>()
    repeat(count) { index ->
        val want = PrizeLadder.difficultyForIndex(index)
        val pick = pool.filter { it.id !in used && it.difficulty == want }.randomOrNull(random)
            ?: pool.filter { it.id !in used }.minByOrNull {
                kotlin.math.abs(it.difficulty.ordinal - want.ordinal)
            }
        if (pick != null) {
            used += pick.id
            out += pick
        }
    }
    return out
}

fun QuestionRecord.toPlayable(index: Int, total: Int, prizeIndex: Int, mode: GameMode): PlayableQuestion {
    val prize = PrizeLadder.forMode(mode).getOrElse(prizeIndex) { PrizeLadder.classic.last() }
    return PlayableQuestion(
        id = id,
        prompt = prompt,
        options = listOf(
            studio.paras.medha.domain.model.AnswerOption(AnswerLetter.A, optionA),
            studio.paras.medha.domain.model.AnswerOption(AnswerLetter.B, optionB),
            studio.paras.medha.domain.model.AnswerOption(AnswerLetter.C, optionC),
            studio.paras.medha.domain.model.AnswerOption(AnswerLetter.D, optionD),
        ),
        category = category,
        difficulty = difficulty,
        prize = prize,
        number = index + 1,
        total = total,
        hints = hints.map { it.trim() }
            .filter { it.isNotBlank() && !leaksAnswer(it, correctText()) }
            .ifEmpty {
                listOf("Read the question again and drop the option that belongs to a different fact.")
            },
    )
}

fun QuestionRecord.correctText(): String = when (correct) {
    AnswerLetter.A -> optionA
    AnswerLetter.B -> optionB
    AnswerLetter.C -> optionC
    AnswerLetter.D -> optionD
}

fun leaksAnswer(hint: String, answer: String): Boolean {
    val raw = answer.trim()
    val h = hint.lowercase()
    val a = raw.lowercase()
    when {
        raw.length >= 4 && h.contains(a) -> return true
        raw.length in 1..3 &&
            (
                Regex("""\b${Regex.escape(raw)}\b""").containsMatchIn(hint) ||
                    Regex(""""${Regex.escape(raw)}"""", RegexOption.IGNORE_CASE).containsMatchIn(hint)
                ) -> return true
    }
    if (Regex("""\b(option|lock)\s+[abcd]\b""").containsMatchIn(h)) return true
    if ("accurate answer" in h || "correct answer is" in h || "the lock is" in h) return true
    return false
}

interface PlayerRepository {
    val profile: Flow<PlayerProfile>
    suspend fun current(): PlayerProfile
    suspend fun update(transform: (PlayerProfile) -> PlayerProfile)
    suspend fun applyGameResult(result: GameResult)
}

fun PlayerProfile.withResult(result: GameResult): PlayerProfile {
    val today = java.time.LocalDate.now().toEpochDay()
    val gainedXp = result.sessionXp
    val newXp = xp + gainedXp
    val dailyWin = result.mode == studio.paras.medha.domain.model.GameMode.DAILY && result.completed
    // The login streak (LoginStreak) owns dailyStreak, so a finished game does not touch it.
    return copy(
        gamesPlayed = gamesPlayed + 1,
        bestPrize = maxOf(bestPrize, result.prizeWon),
        correctAnswers = correctAnswers + result.correctCount,
        wrongAnswers = wrongAnswers + (result.totalAnswered - result.correctCount).coerceAtLeast(0),
        xp = newXp,
        level = XpRules.levelFor(newXp),
        coins = coins + result.coinsEarned,
        wins = wins + if (result.completed) 1 else 0,
        dailyWins = dailyWins + if (dailyWin && lastDailyDay != today) 1 else 0,
        lastDailyDay = if (dailyWin) today else lastDailyDay,
        lastPlayEpochDay = today,
    )
}
