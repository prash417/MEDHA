package studio.paras.medha.ui.splash

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.os.SystemClock
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
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

@Composable
fun SplashScreen(
    vm: SplashViewModel = viewModel(),
    onFinished: () -> Unit,
) {
    val view = LocalView.current
    val ui by vm.state.collectAsStateWithLifecycle()
    val time = rememberWallSeconds()
    val started = remember { SystemClock.uptimeMillis() }
    var leaveFrom by remember { mutableFloatStateOf(-1f) }

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

    val creditGone = appear(time, 2.20f, 0.95f)
    val loadingIn = appear(time, 3.15f, 0.7f)
    LaunchedEffect(ui.ready) {
        if (!ui.ready || leaveFrom >= 0f) return@LaunchedEffect
        val elapsed = SystemClock.uptimeMillis() - started
        delay((4_600L - elapsed).coerceAtLeast(400L))
        leaveFrom = (SystemClock.uptimeMillis() - started) / 1000f
    }
    val loadingGone = if (leaveFrom < 0f) 0f else appear(time, leaveFrom, 0.7f)
    LaunchedEffect(loadingGone, leaveFrom) {
        if (leaveFrom >= 0f && loadingGone >= 1f) onFinished()
    }

    val loadingAlpha = loadingIn * (1f - loadingGone)

    Box(
        Modifier
            .fillMaxSize()
            .background(SplashPalette.Ink),
    ) {
        SplashBackground(
            time = time,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = loadingAlpha },
        )
        LoadingProgress(
            state = ui,
            time = time,
            showStudioCredit = false,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { alpha = loadingAlpha },
        )
        StudioCard(
            time = time,
            alpha = 1f - creditGone,
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
    val kicker = appear(time, 0.08f, 0.55f)
    val line = appear(time, 0.95f, 0.55f)
    Column(
        modifier.graphicsLayer {
            this.alpha = alpha
            val s = lerp(1f, 0.96f, 1f - alpha)
            scaleX = s
            scaleY = s
        },
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
                val t = easeOutCubic(appear(time, 0.38f + i * 0.055f, 0.42f))
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
