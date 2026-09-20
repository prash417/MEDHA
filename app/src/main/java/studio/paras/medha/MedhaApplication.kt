package studio.paras.medha

import android.app.Application
import com.google.firebase.FirebaseApp
import studio.paras.medha.di.AppContainer

class MedhaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        container = AppContainer(this)
    }

    override fun onTerminate() {
        if (::container.isInitialized) container.release()
        super.onTerminate()
    }
}
