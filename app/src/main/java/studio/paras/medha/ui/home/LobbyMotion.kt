package studio.paras.medha.ui.home

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import studio.paras.medha.core.design.LocalReducedMotion
import studio.paras.medha.ui.splash.appear
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

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

@Composable
fun Modifier.todayRewardShine(enabled: Boolean): Modifier {
    if (!enabled || LocalReducedMotion.current) return this
    // Keep the highlight moving even when Android's animator duration scale is zero.
    val elapsed = remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val started = SystemClock.uptimeMillis()
        while (isActive) {
            elapsed.longValue = SystemClock.uptimeMillis() - started
            delay(16)
        }
    }
    return graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val seconds = elapsed.longValue / 1000f
            val pulse = (sin(seconds * PI * 2 / 1.8).toFloat() + 1f) / 2f
            drawRect(
                color = Color(0xFFFFDC80).copy(alpha = 0.04f + pulse * 0.12f),
                blendMode = BlendMode.SrcAtop,
            )
            val progress = (elapsed.longValue % 2_600L) / 2_600f
            val band = size.width * 0.85f
            val x = (size.width + band * 2) * progress - band
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent,
                    0.25f to Color(0x33FFE8A3),
                    0.5f to Color(0x88FFF9E4),
                    0.75f to Color(0x33FFE8A3),
                    1f to Color.Transparent,
                    start = Offset(x, 0f),
                    end = Offset(x + band, 0f),
                ),
                blendMode = BlendMode.SrcAtop,
            )
            repeat(4) { index ->
                val twinkle = (sin(seconds * PI * 2 / 1.4 + index * 1.7).toFloat() + 1f) / 2f
                val alpha = twinkle * twinkle * twinkle
                val center = Offset(
                    size.width * if (index % 2 == 0) 0.08f else 0.92f,
                    size.height * (0.18f + index * 0.20f),
                )
                val radius = size.width * (0.025f + alpha * 0.025f)
                val color = Color(0xFFFFF7CC).copy(alpha = alpha * 0.95f)
                drawCircle(color, radius * 0.24f, center, blendMode = BlendMode.SrcAtop)
                drawLine(
                    color, center - Offset(radius, 0f), center + Offset(radius, 0f),
                    strokeWidth = size.width * 0.009f, blendMode = BlendMode.SrcAtop,
                )
                drawLine(
                    color, center - Offset(0f, radius), center + Offset(0f, radius),
                    strokeWidth = size.width * 0.009f, blendMode = BlendMode.SrcAtop,
                )
            }
        }
}
