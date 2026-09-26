package studio.paras.medha.di

import android.content.Context
import studio.paras.medha.core.ads.AdController
import studio.paras.medha.core.ads.NoOpAds
import studio.paras.medha.core.analytics.LogAnalytics
import studio.paras.medha.core.analytics.MedhaAnalytics
import studio.paras.medha.core.audio.MedhaAudio
import studio.paras.medha.core.audio.MedhaVoice
import studio.paras.medha.core.audio.VoiceAnnouncer
import studio.paras.medha.data.cloud.HostVoiceApi
import studio.paras.medha.data.cloud.FirebaseCloud
import studio.paras.medha.data.cloud.CloudQuestionRepository
import studio.paras.medha.data.prefs.PlayerStore
import studio.paras.medha.data.repository.LocalQuestionRepository
import studio.paras.medha.data.repository.QuestionRepository
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.GameResult

class AppContainer(context: Context) {
    val audio = MedhaAudio(context).also { it.warm() }
    val playerStore = PlayerStore(context)
    val cloud = FirebaseCloud(playerStore).also { it.start() }
    val localQuestions = LocalQuestionRepository()
    val questions: QuestionRepository = CloudQuestionRepository(
        localQuestions,
        java.io.File(context.filesDir, "question-bank.json"),
    )
    val analytics: MedhaAnalytics = LogAnalytics()
    val ads: AdController = NoOpAds()
    val voice: VoiceAnnouncer = MedhaVoice(
        context,
        audio,
        HostVoiceApi(context),
    ) { speaking -> audio.duck(speaking) }.also { it.warm() }
    @Volatile var lastResult: GameResult? = null
    @Volatile var playMode: GameMode = GameMode.CLASSIC
    @Volatile var playCategory: String? = null
    @Volatile var awardXp: Boolean = true

    fun release() {
        audio.release()
        (voice as? MedhaVoice)?.release()
    }
}
