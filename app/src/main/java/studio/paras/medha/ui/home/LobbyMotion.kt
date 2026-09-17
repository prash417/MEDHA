package studio.paras.medha.ui.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import studio.paras.medha.ui.splash.appear

fun Modifier.lobbyEnter(
    time: Float,
    start: Float,
    duration: Float = 0.55f,
    dy: Float = 16f,
): Modifier {
    val v = appear(time, start, duration)
    return graphicsLayer {
        alpha = v
        translationY = (1f - v) * dy
    }
}
