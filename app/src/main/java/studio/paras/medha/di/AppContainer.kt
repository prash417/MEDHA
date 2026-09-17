package studio.paras.medha.di

import android.content.Context
import studio.paras.medha.core.ads.AdController
import studio.paras.medha.core.ads.NoOpAds
import studio.paras.medha.core.analytics.LogAnalytics
import studio.paras.medha.core.analytics.MedhaAnalytics
import studio.paras.medha.core.audio.MedhaAudio
import studio.paras.medha.core.audio.SilentVoice
import studio.paras.medha.core.audio.VoiceAnnouncer
import studio.paras.medha.data.prefs.PlayerStore
import studio.paras.medha.data.repository.LocalQuestionRepository
import studio.paras.medha.data.repository.QuestionRepository
import studio.paras.medha.domain.model.GameResult

class AppContainer(context: Context) {
    val audio = MedhaAudio(context).also { it.warm() }
    val playerStore = PlayerStore(context)
    val questions: QuestionRepository = LocalQuestionRepository()
    val localQuestions: LocalQuestionRepository get() = questions as LocalQuestionRepository
    val analytics: MedhaAnalytics = LogAnalytics()
    val ads: AdController = NoOpAds()
    val voice: VoiceAnnouncer = SilentVoice()
    @Volatile var lastResult: GameResult? = null

    fun release() {
        audio.release()
    }
}
