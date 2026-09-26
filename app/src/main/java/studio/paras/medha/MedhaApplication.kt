package studio.paras.medha

import android.app.Application
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import studio.paras.medha.di.AppContainer

class MedhaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        container = AppContainer(this)
        runBlocking {
            val profile = container.playerStore.current()
            container.audio.musicEnabled = profile.musicEnabled
            container.audio.timerMusicEnabled = profile.timerMusicEnabled
            container.audio.sfxEnabled = profile.sfxEnabled
            container.audio.clockTicksEnabled = profile.clockTicksEnabled
            container.audio.hapticsEnabled = profile.hapticsEnabled
            container.audio.reducedMotion = profile.reducedMotion
            container.voice.enabled = profile.voiceEnabled
        }
    }

    override fun onTerminate() {
        if (::container.isInitialized) container.release()
        super.onTerminate()
    }
}
