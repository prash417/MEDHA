package studio.paras.medha.ui.splash

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit
import kotlin.math.hypot

@Composable
fun SplashScreen(
    vm: SplashViewModel = viewModel(),
    onFinished: () -> Unit,
) {
    val view = LocalView.current
    val ui by vm.state.collectAsStateWithLifecycle()
    val time = rememberWallSeconds()

    DisposableEffect(view) {
        view.keepScreenOn = true
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            view.keepScreenOn = false
        }
    }

    LaunchedEffect(ui.finished) {
        if (ui.finished) onFinished()
    }

    val irisOpen = easeOutCubic(appear(time, 1.72f, 1.35f))
    val studio = (1f - appear(time, 1.28f, 0.48f))
    val hud = appear(time, 3.30f, 0.70f)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(SplashPalette.Ink),
    ) {
        val artAspect = 16f / 9f
        val screenAspect = maxWidth / maxHeight
        val coverH = if (screenAspect > artAspect) maxWidth / artAspect else maxHeight
        val coverW = coverH * artAspect
        val logoW = coverW * 0.20f
        val logoH = coverH * 0.26f
        val logoTop = coverH * 0.13f

        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (irisOpen >= 0.998f) {
                        drawContent()
                    } else if (irisOpen > 0.004f) {
                        val c = Offset(size.width * 0.5f, size.height * 0.34f)
                        val maxR = hypot(size.width.toDouble(), size.height.toDouble()).toFloat()
                        val r = maxR * irisOpen
                        val hole = Path().apply {
                            addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r))
                        }
                        clipPath(hole) {
                            this@drawWithContent.drawContent()
                        }
                    }
                },
        ) {
            SplashBackground(time = time, modifier = Modifier.fillMaxSize())
            CinematicLightEffect(time = time, modifier = Modifier.fillMaxSize())
            MedhaLogo(
                time = time,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = logoTop)
                    .width(logoW)
                    .height(logoH),
            )
            LoadingProgress(
                state = ui,
                time = time,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = hud },
            )
        }

        StudioCard(
            time = time,
            alpha = studio,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private const val StudioTitle = "PARAS STUDIOS"

@Composable
private fun StudioCard(
    time: Float,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val kicker = appear(time, 0.06f, 0.36f)
    val line = appear(time, 0.78f, 0.42f)
    Column(
        modifier.graphicsLayer { this.alpha = alpha },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "A GAME BY",
            fontFamily = Outfit,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = lerp(14f, 5.5f, easeOutCubic(kicker)).sp,
            color = SplashPalette.Ivory,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(Color.Black, Offset(0f, 2f), 8f),
            ),
            modifier = Modifier.graphicsLayer {
                this.alpha = kicker
                translationY = (1f - kicker) * 8f
            },
        )
        Row(
            Modifier.padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StudioTitle.forEachIndexed { i, ch ->
                val t = easeOutCubic(appear(time, 0.30f + i * 0.040f, 0.30f))
                Text(
                    ch.toString(),
                    fontFamily = Cormorant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 38.sp,
                    color = SplashPalette.GoldHi,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(Color.Black, Offset(0f, 4f), 14f),
                    ),
                    modifier = Modifier.graphicsLayer {
                        this.alpha = t
                        translationY = (1f - t) * 18f
                    },
                )
            }
        }
        Canvas(
            Modifier
                .padding(top = 14.dp)
                .width(220.dp)
                .height(3.dp),
        ) {
            val w = size.width * line
            if (w > 2f) {
                val x = (size.width - w) / 2f
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, SplashPalette.GoldHi, Color.Transparent),
                    ),
                    start = Offset(x, size.height / 2f),
                    end = Offset(x + w, size.height / 2f),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
