package studio.paras.medha.ui.splash

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

/** Wall-clock seconds from first composition. Ignores animator_duration_scale. */
@Composable
fun rememberWallSeconds(): Float {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val t0 = SystemClock.uptimeMillis()
        while (isActive) {
            t = (SystemClock.uptimeMillis() - t0) / 1000f
            delay(16)
        }
    }
    return t
}

fun saturate(x: Float): Float = x.coerceIn(0f, 1f)

fun smoothstep(t: Float): Float {
    val x = saturate(t)
    return x * x * (3f - 2f * x)
}

fun appear(time: Float, start: Float, duration: Float): Float {
    if (duration <= 0f) return if (time >= start) 1f else 0f
    return smoothstep((time - start) / duration)
}

fun pulse(time: Float, speed: Float = 1f, min: Float = 0.72f, max: Float = 1f): Float {
    val wave = (sin(time * speed * 2.0 * PI) * 0.5 + 0.5).toFloat()
    return min + (max - min) * wave
}

fun easeOutCubic(t: Float): Float {
    val x = saturate(t)
    val i = 1f - x
    return 1f - i * i * i
}

fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

fun easeOutBack(t: Float): Float {
    val x = saturate(t)
    val c = 1.70158f
    val i = x - 1f
    return 1f + (c + 1f) * i * i * i + c * i * i
}
