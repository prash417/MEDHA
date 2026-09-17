package studio.paras.medha.core.analytics

/**
 * Product analytics façade. Phase 2 binds this to Firebase Analytics.
 * Events stay anonymous and gameplay-focused.
 */
interface MedhaAnalytics {
    fun track(event: String, params: Map<String, String> = emptyMap())
}

class LogAnalytics : MedhaAnalytics {
    override fun track(event: String, params: Map<String, String>) {
        android.util.Log.i("MEDHA", "$event $params")
    }
}

object AnalyticsEvents {
    const val GAME_STARTED = "game_started"
    const val QUESTION_ANSWERED = "question_answered"
    const val ANSWER_CORRECT = "answer_correct"
    const val ANSWER_WRONG = "answer_wrong"
    const val LIFELINE_USED = "lifeline_used"
    const val GAME_COMPLETED = "game_completed"
    const val GAME_ABANDONED = "game_abandoned"
    const val DAILY_STARTED = "daily_challenge_started"
    const val DAILY_COMPLETED = "daily_challenge_completed"
    const val LEVEL_UP = "level_up"
    const val ACHIEVEMENT = "achievement_unlocked"
}
