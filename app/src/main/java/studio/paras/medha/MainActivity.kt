package studio.paras.medha

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.paras.medha.core.design.MedhaTheme
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.ui.navigation.MedhaNav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { it.remove() }
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        hideSystemChrome()
        enableEdgeToEdge()
        hideSystemChrome()
        val container = (application as MedhaApplication).container
        setContent {
            val profile by container.playerStore.profile.collectAsStateWithLifecycle(PlayerProfile())
            MedhaTheme(reducedMotion = false) {
                MedhaNav(container = container, profile = profile)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemChrome()
    }

    override fun onPause() {
        super.onPause()
        (application as MedhaApplication).container.audio.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        hideSystemChrome()
        val container = (application as MedhaApplication).container
        container.audio.resumeMusic()
    }

    private fun hideSystemChrome() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
