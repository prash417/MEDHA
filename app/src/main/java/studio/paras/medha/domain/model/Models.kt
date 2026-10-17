package studio.paras.medha.domain.model

enum class GameMode(
    val id: String,
    val questionCount: Int,
    val ranks: Boolean,
    val title: String,
    val subtitle: String,
) {
    CLASSIC("classic", 15, true, "Classic", "Fifteen steps. One mind."),
    QUICK("quick", 5, true, "Quick Play", "Five questions. Full tension."),
    PRACTICE("practice", 8, false, "Practice", "Learn freely. Rankings untouched."),
    DAILY("daily", 7, true, "Daily Challenge", "The same seven, for everyone, today."),
}

enum class Difficulty { EASY, MEDIUM, HARD, EXPERT }

enum class AnswerLetter { A, B, C, D }

enum class LifelineKind { FIFTY_FIFTY, AUDIENCE, EXPERT, SWITCH, EXTRA_TIME, DARE, PROTECT }

enum class GamePhase {
    IDLE,
    LOADING,
    SEAT_INTRO,
    QUESTION_STING,
    QUESTION_DISPLAY,
    CONFIRMING,
    ANSWER_SELECTED,
    ANSWER_REVEAL,
    CORRECT,
    WRONG,
    WALK_AWAY_CONFIRM,
    NEXT_QUESTION,
    GAME_COMPLETE,
}

enum class GameOutcome {
    WON,
    WALKED_AWAY,
    DROPPED,
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    KANNADA("kn", "ಕನ್ನಡ"),
}

data class PrizeRung(
    val index: Int,
    val amount: Long,
    val label: String,
    val isAnchor: Boolean,
)

data class QuestionRecord(
    val id: String,
    val prompt: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correct: AnswerLetter,
    val category: String,
    val tags: List<String>,
    val difficulty: Difficulty,
    val explanation: String,
    val hints: List<String> = emptyList(),
    val prizeLevel: Int,
    val language: String = "en",
    val active: Boolean = true,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val source: String? = null,
    val publishedAt: Long? = null,
    val expiresAt: Long? = null,
)

data class AnswerOption(
    val letter: AnswerLetter,
    val text: String,
    val visible: Boolean = true,
)

data class PlayableQuestion(
    val id: String,
    val prompt: String,
    val options: List<AnswerOption>,
    val category: String,
    val difficulty: Difficulty,
    val prize: PrizeRung,
    val number: Int,
    val total: Int,
    val hints: List<String> = emptyList(),
)

data class ExpertAdvice(
    val name: String,
    val title: String,
    val letter: AnswerLetter,
    val confidence: Int,
    val note: String,
)

data class AnswerVerdict(
    val correct: Boolean,
    val correctLetter: AnswerLetter,
    val selected: AnswerLetter,
    val explanation: String,
)

data class GameResult(
    val mode: GameMode,
    val reachedIndex: Int,
    val prizeWon: Long,
    val prizeLabel: String,
    val correctCount: Int,
    val totalAnswered: Int,
    val completed: Boolean,
    val outcome: GameOutcome = if (completed) GameOutcome.WON else GameOutcome.DROPPED,
    val durationMs: Long,
    val lastExplanation: String?,
    val lastCorrectText: String?,
    val riskedLabel: String? = null,
    val sessionXp: Int = 0,
)

data class PlayerProfile(
    val displayName: String = "Guest",
    val avatarId: Int = 0,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val onboardingComplete: Boolean = true,
    val musicEnabled: Boolean = true,
    val timerMusicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val clockTicksEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val doNotDisturb: Boolean = false,
    val showPing: Boolean = false,
    val xp: Int = 0,
    val coins: Int = 0,
    val gems: Int = 0,
    val level: Int = 1,
    val gamesPlayed: Int = 0,
    val bestPrize: Long = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val dailyStreak: Int = 0,
    val streakAnchorDay: Long = 0,
    val streakJoinIndex: Int = -1,
    val streakMask: Int = 0,
    val streakBroken: Boolean = false,
    val streakLastClaimAtMillis: Long = 0,
    val lastPlayEpochDay: Long = 0,
    val seenQuestionIds: List<String> = emptyList(),
) {
    val accuracy: Int
        get() {
            val total = correctAnswers + wrongAnswers
            return if (total == 0) 0 else ((correctAnswers * 100f) / total).toInt()
        }

    val winRate: Int
        get() = if (gamesPlayed == 0) 0 else ((correctAnswers.toFloat() / (gamesPlayed * 8).coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
}

data class Achievement(
    val id: String,
    val title: String,
    val detail: String,
    val unlocked: Boolean,
)

object PrizeLadder {
    val classic: List<PrizeRung> = listOf(
        1_000L, 2_000L, 3_000L, 5_000L, 10_000L,
        20_000L, 40_000L, 80_000L, 1_60_000L, 3_20_000L,
        6_40_000L, 12_50_000L, 25_00_000L, 50_00_000L, 1_00_00_000L,
    ).mapIndexed { index, amount ->
        PrizeRung(
            index = index,
            amount = amount,
            label = formatRupee(amount),
            isAnchor = amount == 10_000L || amount == 3_20_000L || amount == 1_00_00_000L,
        )
    }

    fun forMode(mode: GameMode): List<PrizeRung> = when (mode) {
        GameMode.CLASSIC -> classic
        GameMode.QUICK -> classic.take(5)
        GameMode.PRACTICE -> classic.take(8)
        GameMode.DAILY -> classic.take(7)
    }

    fun formatRupee(amount: Long): String = "₹${indianGroup(amount)}"

    fun lastCleared(correctCount: Int, mode: GameMode): PrizeRung? {
        if (correctCount <= 0) return null
        return forMode(mode).getOrNull(correctCount - 1)
    }

    fun guaranteed(correctCount: Int, mode: GameMode): PrizeRung? {
        val ladder = forMode(mode)
        val lastIndex = correctCount - 1
        if (lastIndex < 0) return null
        return ladder.filter { it.isAnchor && it.index <= lastIndex }.maxByOrNull { it.index }
    }

    fun timerSeconds(index: Int, mode: GameMode): Int? {
        if (mode == GameMode.PRACTICE) return null
        return when {
            index < 5 -> 30
            index < 10 -> 45
            else -> null
        }
    }

    fun lifelineSlots(index: Int): Int = when {
        index < 5 -> 1
        index < 10 -> 2
        else -> Int.MAX_VALUE
    }

    fun difficultyForIndex(index: Int): Difficulty = when {
        index < 5 -> Difficulty.EASY
        index < 10 -> Difficulty.MEDIUM
        else -> Difficulty.HARD
    }

    fun indianGroup(amount: Long): String {
        val s = amount.toString()
        if (s.length <= 3) return s
        val last3 = s.takeLast(3)
        val rest = s.dropLast(3)
        val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
        return "$grouped,$last3"
    }
}
