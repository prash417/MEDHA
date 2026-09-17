package studio.paras.medha.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.R
import studio.paras.medha.core.design.Cormorant

@Composable
fun MedhaLogo(
    time: Float,
    modifier: Modifier = Modifier,
    live: Boolean = true,
) {
    val enter = if (live) easeOutBack(appear(time, 2.55f, 0.90f)) else 1f
    val tag = if (live) appear(time, 3.22f, 0.65f) else 1f
    val glow = if (live) pulse(time, speed = 0.48f, min = 0.40f, max = 0.92f) else 0.62f

    Box(
        modifier.graphicsLayer {
            alpha = enter.coerceIn(0f, 1f)
            val s = 0.78f + 0.22f * enter
            scaleX = s
            scaleY = s
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(bottom = 28.dp)) {
            val c = Offset(size.width * 0.5f, size.height * 0.40f)
            val r = size.minDimension * 0.50f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xBBF0C85A), Color(0x33F0C85A), Color.Transparent),
                    center = c,
                    radius = r,
                ),
                radius = r,
                center = c,
                blendMode = BlendMode.Plus,
                alpha = glow * enter.coerceIn(0f, 1f),
            )
        }
        Image(
            painter = painterResource(R.drawable.medha_logo_gold),
            contentDescription = "MEDHA",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 26.dp),
        )
        Text(
            "Knowledge changes lives",
            fontFamily = Cormorant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = 1.6.sp,
            color = SplashPalette.GoldHi,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(color = Color.Black, offset = Offset(0f, 2f), blurRadius = 8f),
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { alpha = tag },
        )
    }
}
