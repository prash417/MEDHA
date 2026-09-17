package studio.paras.medha.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

private data class Dust(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val gold: Boolean,
    val phase: Float,
)

@Composable
fun CinematicLightEffect(
    time: Float,
    modifier: Modifier = Modifier,
) {
    val dust = remember {
        List(48) { i ->
            Dust(
                x = ((i * 53) % 1000) / 1000f,
                y = ((i * 89) % 1000) / 1000f,
                speed = 0.011f + (i % 6) * 0.004f,
                size = 1.0f + (i % 4),
                gold = i % 3 != 0,
                phase = i * 0.41f,
            )
        }
    }
    val bloom = appear(time, 1.85f, 1.15f)
    val sparks = appear(time, 2.20f, 1.05f)
    val sweepT = ((time - 3.50f) / 1.55f).coerceIn(0f, 1f)
    val sweepA = if (time in 3.50f..5.20f) (1f - kotlin.math.abs(sweepT - 0.5f) * 2f) else 0f

    Canvas(modifier.fillMaxSize()) {
        if (bloom > 0.01f) {
            val pulseA = pulse(time, 0.42f, 0.7f, 1f)
            val c = Offset(size.width * 0.5f, size.height * 0.30f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55F0C85A), Color(0x14F0C85A), Color.Transparent),
                    center = c,
                    radius = size.minDimension * 0.38f,
                ),
                radius = size.minDimension * 0.38f,
                center = c,
                blendMode = BlendMode.Plus,
                alpha = bloom * pulseA,
            )
            val floor = Offset(size.width * 0.5f, size.height * 0.78f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28F0C85A), Color.Transparent),
                    center = floor,
                    radius = size.width * 0.36f,
                ),
                radius = size.width * 0.36f,
                center = floor,
                blendMode = BlendMode.Plus,
                alpha = bloom * pulse(time, 0.38f, 0.4f, 0.8f),
            )
        }

        if (sweepA > 0.02f) {
            val x = size.width * sweepT
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color(0x33FFF3C4), Color.Transparent),
                    startX = x - size.width * 0.12f,
                    endX = x + size.width * 0.12f,
                ),
                blendMode = BlendMode.Plus,
                alpha = sweepA,
            )
        }

        if (sparks > 0.01f) {
            dust.forEach { p ->
                val y = (p.y - time * p.speed + p.phase) % 1f
                val yy = if (y < 0f) y + 1f else y
                val x = p.x + sin((time + p.phase) * 0.65) * 0.012
                val twinkle = 0.25f + 0.75f * ((sin((time * 2.0f + p.phase) * PI) * 0.5 + 0.5).toFloat())
                drawCircle(
                    color = if (p.gold) SplashPalette.Gold.copy(alpha = 0.55f * twinkle * sparks)
                    else Color.White.copy(alpha = 0.35f * twinkle * sparks),
                    radius = p.size,
                    center = Offset(x.toFloat() * size.width, yy * size.height),
                )
            }
        }
    }
}
