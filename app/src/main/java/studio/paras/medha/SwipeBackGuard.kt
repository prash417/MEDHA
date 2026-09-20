package studio.paras.medha

import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import studio.paras.medha.ui.SwipeBackController
import kotlin.math.abs

internal class SwipeBackGuard(
    private val activity: ComponentActivity,
    private val swipeBack: SwipeBackController,
) {
    private lateinit var overlay: FrameLayout
    private var armed = false
    private var tracking = false
    private var fromLeft = false
    private var fired = false
    private var downX = 0f
    private var downY = 0f
    private var lastBackAt = 0L
    private val hide = Runnable { dismiss() }

    fun install() {
        val widthPx = (280 * activity.resources.displayMetrics.density).toInt()
        val art = activity.assets.open("Swipe back.png").use { BitmapFactory.decodeStream(it) }
        val image = ImageView(activity).apply {
            setImageBitmap(art)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { dismiss() }
        }
        overlay = FrameLayout(activity).apply {
            visibility = View.GONE
            isClickable = true
            elevation = 1000f
            setOnClickListener { dismiss() }
            addView(
                image,
                FrameLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER),
            )
        }
        activity.addContentView(
            overlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        activity.window.decorView.viewTreeObserver.addOnGlobalLayoutListener { excludeSystemBack() }
        swipeBack.onRouteChanged = {
            if (!swipeBack.isHome()) dismiss()
            swipeBack.callback.isEnabled = swipeBack.isHome()
            excludeSystemBack()
            (activity as MainActivity).applyWindowChrome()
        }
    }

    fun offerTouch(ev: MotionEvent): Boolean {
        if (!swipeBack.isHome()) {
            tracking = false
            fired = false
            return false
        }
        val density = activity.resources.displayMetrics.density
        val edge = 40f * density
        val slop = 28f * density
        val width = activity.window.decorView.width.toFloat().coerceAtLeast(1f)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                tracking = ev.x <= edge || ev.x >= width - edge
                fromLeft = ev.x <= edge
                downX = ev.x
                downY = ev.y
                fired = false
                return tracking
            }
            MotionEvent.ACTION_MOVE -> {
                if (!tracking) return false
                if (!fired) {
                    val dx = ev.x - downX
                    val dy = ev.y - downY
                    val inward = if (fromLeft) dx > slop else dx < -slop
                    if (inward && abs(dx) >= abs(dy)) {
                        fired = true
                        onBack()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!tracking) return false
                tracking = false
                return true
            }
        }
        return false
    }

    fun interceptFinish(): Boolean {
        if (!swipeBack.isHome()) return false
        if (armed) return false
        onBack()
        return true
    }

    fun onBack() {
        if (!swipeBack.isHome()) {
            dismiss()
            val nav = swipeBack.nav
            if (nav != null && nav.previousBackStackEntry != null) {
                nav.popBackStack()
            } else {
                activity.moveTaskToBack(true)
            }
            return
        }
        val now = SystemClock.uptimeMillis()
        if (now - lastBackAt < 450) return
        lastBackAt = now
        if (armed) {
            dismiss()
            activity.moveTaskToBack(true)
        } else {
            show()
        }
    }

    fun dismiss() {
        armed = false
        if (::overlay.isInitialized) overlay.visibility = View.GONE
        activity.window.decorView.removeCallbacks(hide)
    }

    private fun show() {
        if (!swipeBack.isHome()) return
        armed = true
        overlay.visibility = View.VISIBLE
        overlay.bringToFront()
        activity.window.decorView.removeCallbacks(hide)
        activity.window.decorView.postDelayed(hide, 2400)
    }

    private fun excludeSystemBack() {
        val view = activity.window.decorView
        if (view.width <= 0 || view.height <= 0) return
        if (!swipeBack.isHome()) {
            view.systemGestureExclusionRects = emptyList()
            return
        }
        val edge = (40 * activity.resources.displayMetrics.density).toInt()
        view.systemGestureExclusionRects = listOf(
            Rect(0, 0, edge, view.height),
            Rect(view.width - edge, 0, view.width, view.height),
        )
    }
}
