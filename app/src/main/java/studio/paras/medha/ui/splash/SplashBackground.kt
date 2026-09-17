package studio.paras.medha.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import studio.paras.medha.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashBackground(
    time: Float,
    modifier: Modifier = Modifier,
) {
    val fade = appear(time, 1.55f, 0.9f)
    Box(
        modifier
            .fillMaxSize()
            .background(SplashPalette.Ink),
    ) {
        Image(
            painter = painterResource(R.drawable.splash_keyart),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val zoom = lerp(1.08f, 1.0f, easeOutCubic(appear(time, 1.65f, 4.4f)))
                    val live = appear(time, 2.2f, 0.8f)
                    scaleX = zoom
                    scaleY = zoom
                    translationX = sin(time * 0.18f) * 5f * live
                    translationY = cos(time * 0.14f) * 3f * live
                    alpha = fade
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color(0x3302040C),
                            0.22f to Color.Transparent,
                            0.72f to Color.Transparent,
                            1f to Color(0xE002040C),
                        ),
                    )
                },
        )
    }
}
