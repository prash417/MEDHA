package studio.paras.medha.ui

import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavHostController

class SwipeBackController {
    var nav: NavHostController? = null
    var route: String? = "home"
        set(value) {
            if (field == value) return
            field = value
            onRouteChanged?.invoke()
        }
    var onRouteChanged: (() -> Unit)? = null
    var splash: Boolean = false

    lateinit var callback: OnBackPressedCallback

    fun isHome(): Boolean {
        val nav = nav ?: return true
        if (nav.previousBackStackEntry != null) return false
        val r = nav.currentDestination?.route ?: route
        return r == null || r == "home"
    }
}
