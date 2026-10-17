package studio.paras.medha

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.paras.medha.core.design.MedhaTheme
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.ui.SwipeBackController
import studio.paras.medha.ui.navigation.MedhaNav

class MainActivity : ComponentActivity() {
    private val swipeBack = SwipeBackController()
    private val backGuard = SwipeBackGuard(this, swipeBack)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setOnExitAnimationListener { it.remove() }
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        swipeBack.callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = backGuard.onBack()
        }
        onBackPressedDispatcher.addCallback(this, swipeBack.callback)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        applyWindowChrome()
        val container = (application as MedhaApplication).container
        setContent {
            val profile by container.playerStore.profile.collectAsStateWithLifecycle(PlayerProfile())
            MedhaTheme(reducedMotion = profile.reducedMotion) {
                MedhaNav(
                    container = container,
                    profile = profile,
                    swipeBack = swipeBack,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        backGuard.install()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        applyWindowChrome()
    }

    override fun onPause() {
        (application as MedhaApplication).container.ads.attach(null)
        backGuard.dismiss()
        super.onPause()
        (application as MedhaApplication).container.audio.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        swipeBack.callback.remove()
        onBackPressedDispatcher.addCallback(this, swipeBack.callback)
        swipeBack.callback.isEnabled = swipeBack.isHome()
        applyWindowChrome()
        (application as MedhaApplication).container.ads.attach(this)
        (application as MedhaApplication).container.audio.resumeMusic()
    }

    override fun finish() {
        if (backGuard.interceptFinish()) return
        super.finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (backGuard.offerTouch(ev)) return true
        return super.dispatchTouchEvent(ev)
    }

    internal fun applyWindowChrome() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        val bars = WindowCompat.getInsetsController(window, window.decorView)
        bars.isAppearanceLightStatusBars = false
        bars.isAppearanceLightNavigationBars = false
        if (swipeBack.isHome()) {
            bars.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            bars.hide(WindowInsetsCompat.Type.statusBars())
            bars.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
