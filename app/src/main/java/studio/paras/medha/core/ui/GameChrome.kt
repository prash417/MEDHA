package studio.paras.medha.core.ui

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class HudControls(
    val musicOn: Boolean,
    val sfxOn: Boolean,
    val voiceOn: Boolean,
    val onToggleMusic: () -> Unit,
    val onToggleSfx: () -> Unit,
    val onToggleVoice: () -> Unit,
)

val LocalHudControls = compositionLocalOf<HudControls?> { null }

@Composable
fun rememberArenaPulse(): Float {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val t0 = SystemClock.uptimeMillis()
        while (isActive) {
            t = ((SystemClock.uptimeMillis() - t0) % 4800L) / 4800f
            delay(16)
        }
    }
    return t
}

@Composable
fun GameStage(
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal,
) {
    ArenaBackdrop(modifier = modifier, accent = accent, pulse = rememberArenaPulse())
}

@Composable
fun ArenaBackdrop(
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal,
    pulse: Float = 0.4f,
) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val glow = 0.55f + 0.45f * sin(pulse * Math.PI * 2).toFloat()
        drawRect(Color(0xFF03050A))
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.22f * glow), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.28f),
                radius = min(w, h) * 0.85f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(MedhaColors.Copper.copy(alpha = 0.10f * glow), Color.Transparent),
                center = Offset(w * 0.18f, h * 0.12f),
                radius = min(w, h) * 0.45f,
            ),
        )
        val beam = sin(pulse * Math.PI * 2).toFloat()
        drawLine(
            accent.copy(alpha = 0.08f + 0.06f * glow),
            Offset(w * (0.35f + 0.08f * beam), 0f),
            Offset(w * 0.48f, h * 0.55f),
            48f,
        )
        drawLine(
            accent.copy(alpha = 0.08f + 0.06f * glow),
            Offset(w * (0.65f - 0.08f * beam), 0f),
            Offset(w * 0.52f, h * 0.55f),
            48f,
        )
        val horizon = h * 0.70f
        for (i in 0..14) {
            val y = horizon + i * 18f + pulse * 18f
            if (y < h) drawLine(accent.copy(alpha = 0.08f), Offset(0f, y), Offset(w, y), 1.2f)
        }
        for (i in -14..14) {
            drawLine(
                accent.copy(alpha = 0.07f),
                Offset(w / 2f, horizon - 12f),
                Offset(w / 2f + i * 100f, h),
                1.2f,
            )
        }
        repeat(36) { i ->
            val x = (w * ((i * 47 + pulse * 80) % 100) / 100f)
            val y = (h * 0.62f * ((i * 29) % 100) / 100f)
            drawCircle(Color.White.copy(alpha = 0.07f + 0.08f * glow), 1.6f + (i % 3), Offset(x, y))
        }
        val scanY = (h * pulse)
        drawLine(accent.copy(alpha = 0.10f), Offset(0f, scanY), Offset(w, scanY), 2f)
        drawLine(MedhaColors.Copper.copy(alpha = 0.55f), Offset(16f, 16f), Offset(86f, 16f), 3f, StrokeCap.Square)
        drawLine(MedhaColors.Copper.copy(alpha = 0.55f), Offset(16f, 16f), Offset(16f, 64f), 3f, StrokeCap.Square)
        drawLine(MedhaColors.Copper.copy(alpha = 0.55f), Offset(w - 16f, 16f), Offset(w - 86f, 16f), 3f, StrokeCap.Square)
        drawLine(MedhaColors.Copper.copy(alpha = 0.55f), Offset(w - 16f, 16f), Offset(w - 16f, 64f), 3f, StrokeCap.Square)
        drawLine(MedhaColors.Copper.copy(alpha = 0.35f), Offset(16f, h - 16f), Offset(86f, h - 16f), 3f, StrokeCap.Square)
        drawLine(MedhaColors.Copper.copy(alpha = 0.35f), Offset(w - 16f, h - 16f), Offset(w - 86f, h - 16f), 3f, StrokeCap.Square)
    }
}

enum class ChromeIcon { BACK, SETTINGS, AUDIO_ON, AUDIO_OFF, CLOSE }

@Composable
fun HexIconButton(
    icon: ChromeIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal,
    size: Dp = 52.dp,
    label: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pulse = rememberArenaPulse()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Canvas(
            Modifier
                .size(size)
                .graphicsLayer { scaleX = if (pressed) 0.92f else 1f; scaleY = if (pressed) 0.92f else 1f }
                .medhaClickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        ) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val r = this.size.minDimension / 2f - 3f
            val hex = hexPath(cx, cy, r)
            val glow = 0.35f + 0.25f * sin(pulse * Math.PI * 2).toFloat()
            drawPath(hex, accent.copy(alpha = 0.12f + 0.10f * glow))
            drawPath(hex, accent.copy(alpha = 0.35f), style = Stroke(width = 6f))
            drawPath(hex, accent.copy(alpha = 0.95f), style = Stroke(width = 2.2f))
            drawChromeGlyph(icon, cx, cy, r * 0.42f, Color.White)
        }
        if (label != null) {
            Text(label, style = MedhaTypography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp), color = accent)
        }
    }
}

private fun hexPath(cx: Float, cy: Float, r: Float): Path {
    val path = Path()
    for (i in 0..5) {
        val a = Math.toRadians((60.0 * i - 30)).toFloat()
        val x = cx + cos(a) * r
        val y = cy + sin(a) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.drawChromeGlyph(icon: ChromeIcon, cx: Float, cy: Float, s: Float, color: Color) {
    when (icon) {
        ChromeIcon.BACK -> {
            val p = Path().apply {
                moveTo(cx + s * 0.35f, cy - s)
                lineTo(cx - s * 0.55f, cy)
                lineTo(cx + s * 0.35f, cy + s)
            }
            drawPath(p, color, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
        }
        ChromeIcon.CLOSE -> {
            drawLine(color, Offset(cx - s, cy - s), Offset(cx + s, cy + s), 3.2f, StrokeCap.Round)
            drawLine(color, Offset(cx + s, cy - s), Offset(cx - s, cy + s), 3.2f, StrokeCap.Round)
        }
        ChromeIcon.SETTINGS -> {
            drawCircle(color, s * 0.35f, Offset(cx, cy), style = Stroke(width = 2.6f))
            for (i in 0..5) {
                val a = Math.toRadians((60.0 * i)).toFloat()
                drawLine(
                    color,
                    Offset(cx + cos(a) * s * 0.55f, cy + sin(a) * s * 0.55f),
                    Offset(cx + cos(a) * s * 1.05f, cy + sin(a) * s * 1.05f),
                    3f,
                    StrokeCap.Round,
                )
            }
        }
        ChromeIcon.AUDIO_ON, ChromeIcon.AUDIO_OFF -> {
            val p = Path().apply {
                moveTo(cx - s * 0.55f, cy - s * 0.25f)
                lineTo(cx - s * 0.15f, cy - s * 0.25f)
                lineTo(cx + s * 0.35f, cy - s * 0.85f)
                lineTo(cx + s * 0.35f, cy + s * 0.85f)
                lineTo(cx - s * 0.15f, cy + s * 0.25f)
                lineTo(cx - s * 0.55f, cy + s * 0.25f)
                close()
            }
            drawPath(p, color)
            if (icon == ChromeIcon.AUDIO_ON) {
                    drawArc(color, -40f, 80f, false, Offset(cx + s * 0.15f, cy - s * 0.7f), Size(s * 1.1f, s * 1.4f), style = Stroke(2.4f))
            } else {
                drawLine(MedhaColors.Wrong, Offset(cx - s, cy + s), Offset(cx + s * 1.1f, cy - s), 3.2f, StrokeCap.Round)
            }
        }
    }
}

val CtaShape = GenericShape { size, _ ->
    val cut = size.height * 0.32f
    moveTo(cut, 0f)
    lineTo(size.width - cut, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width - cut, size.height)
    lineTo(cut, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

@Composable
fun EnergyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal,
    filled: Boolean = true,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pulse = rememberArenaPulse()
    Box(
        modifier
            .defaultMinSize(minHeight = 58.dp)
            .graphicsLayer { scaleX = if (pressed) 0.97f else 1f; scaleY = if (pressed) 0.97f else 1f }
            .drawBehind {
                val glow = 0.45f + 0.35f * sin(pulse * Math.PI * 2).toFloat()
                drawPath(
                    Path().apply {
                        val cut = size.height * 0.32f
                        moveTo(cut, 0f)
                        lineTo(size.width - cut, 0f)
                        lineTo(size.width, size.height / 2f)
                        lineTo(size.width - cut, size.height)
                        lineTo(cut, size.height)
                        lineTo(0f, size.height / 2f)
                        close()
                    },
                    accent.copy(alpha = 0.18f * glow),
                    style = Stroke(width = 14f),
                )
            }
            .clip(CtaShape)
            .background(
                if (filled) Brush.horizontalGradient(listOf(accent.copy(alpha = 0.95f), Color(0xFF9FFFF0)))
                else Brush.horizontalGradient(listOf(Color(0x66101820), Color(0x44101820))),
            )
            .medhaClickable(enabled = enabled, interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label.uppercase(),
            style = MedhaTypography.labelLarge.copy(
                color = if (filled) Color(0xFF04110E) else accent,
                letterSpacing = 3.sp,
                fontSize = 14.sp,
            ),
        )
    }
}

@Composable
fun PlayCta(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    EnergyButton(label, onClick, modifier, filled = true)
}

@Composable
fun GhostButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    EnergyButton(label, onClick, modifier, filled = emphasized)
}

@Composable
fun ArenaScaffold(
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal,
    onBack: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val hud = LocalHudControls.current
    var sheet by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize()) {
        ArenaBackdrop(accent = accent, pulse = rememberArenaPulse())
        content()
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                onBack != null -> HexIconButton(ChromeIcon.BACK, onClick = onBack, accent = accent, label = "BACK")
                leading != null -> leading()
                else -> Spacer(Modifier.size(52.dp))
            }
            Spacer(Modifier.weight(1f))
            if (hud != null) {
                HexIconButton(
                    if (hud.musicOn) ChromeIcon.AUDIO_ON else ChromeIcon.AUDIO_OFF,
                    onClick = hud.onToggleMusic,
                    accent = if (hud.musicOn) accent else MedhaColors.Mist,
                    label = if (hud.musicOn) "AUDIO" else "MUTED",
                )
                Spacer(Modifier.width(8.dp))
                HexIconButton(ChromeIcon.SETTINGS, onClick = { sheet = true }, accent = MedhaColors.Copper, label = "GEAR")
            }
        }
        if (sheet && hud != null) {
            SettingsOverlay(
                musicOn = hud.musicOn,
                sfxOn = hud.sfxOn,
                voiceOn = hud.voiceOn,
                onToggleMusic = hud.onToggleMusic,
                onToggleSfx = hud.onToggleSfx,
                onToggleVoice = hud.onToggleVoice,
                onClose = { sheet = false },
            )
        }
    }
}

@Composable
fun SettingsOverlay(
    musicOn: Boolean,
    sfxOn: Boolean,
    voiceOn: Boolean,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleVoice: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xE605070C))
            .clickable(onClick = onClose)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(enabled = false, onClick = {}),
        ) {
            Text("COMMAND DECK", style = MedhaTypography.labelLarge, color = MedhaColors.Copper)
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                AudioToggle("MUSIC", musicOn, onToggleMusic)
                AudioToggle("SFX", sfxOn, onToggleSfx)
                AudioToggle("VOICE", voiceOn, onToggleVoice)
            }
            Spacer(Modifier.height(28.dp))
            HexIconButton(ChromeIcon.CLOSE, onClick = onClose, accent = MedhaColors.Wrong, label = "CLOSE")
        }
    }
}

@Composable
private fun AudioToggle(label: String, on: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HexIconButton(
            if (on) ChromeIcon.AUDIO_ON else ChromeIcon.AUDIO_OFF,
            onClick = onClick,
            accent = if (on) MedhaColors.Teal else MedhaColors.Mist,
            size = 84.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(label, style = MedhaTypography.labelLarge, color = if (on) MedhaColors.Teal else MedhaColors.Mist)
        Text(if (on) "ON" else "OFF", style = MedhaTypography.titleLarge.copy(fontSize = 18.sp, color = if (on) MedhaColors.Teal else MedhaColors.IvoryMute))
    }
}

@Composable
fun ModeOrb(
    title: String,
    prize: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberArenaPulse()
    Column(
        modifier
            .medhaClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title.uppercase(), style = MedhaTypography.labelSmall.copy(color = if (selected) accent else MedhaColors.Mist, letterSpacing = 1.4.sp))
        Canvas(Modifier.size(if (selected) 68.dp else 54.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.minDimension / 2f - 4f
            val glow = if (selected) 0.6f + 0.4f * sin(pulse * Math.PI * 2).toFloat() else 0.25f
            drawPath(hexPath(cx, cy, r + 6f), accent.copy(alpha = 0.16f * glow))
            drawPath(hexPath(cx, cy, r), accent.copy(alpha = if (selected) 0.28f else 0.10f))
            drawPath(hexPath(cx, cy, r), accent.copy(alpha = if (selected) 1f else 0.45f), style = Stroke(if (selected) 3.4f else 1.8f))
            if (selected) drawCircle(accent, 5f, Offset(cx, cy))
        }
        Text(prize, style = MedhaTypography.labelSmall.copy(fontSize = 10.sp, color = if (selected) MedhaColors.Ivory else MedhaColors.Mist.copy(alpha = 0.7f)))
    }
}

@Composable
fun GlowWell(
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val pulse = rememberArenaPulse()
    Box(
        modifier
            .drawBehind {
                val glow = 0.4f + 0.3f * sin(pulse * Math.PI * 2).toFloat()
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.16f * glow), Color(0x99080E14), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.4f),
                        radius = size.maxDimension * 0.72f,
                    ),
                )
                drawLine(accent.copy(alpha = 0.55f), Offset(18f, 0f), Offset(size.width - 18f, 0f), 2f)
                drawLine(accent.copy(alpha = 0.25f), Offset(18f, size.height), Offset(size.width - 18f, size.height), 2f)
            }
            .then(if (onClick != null) Modifier.medhaClickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

@Deprecated("Use GlowWell")
@Composable
fun HudPanel(
    modifier: Modifier = Modifier,
    accent: Color = MedhaColors.Teal.copy(alpha = 0.55f),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    GlowWell(modifier = modifier, accent = accent, onClick = onClick) { content() }
}

@Deprecated("Use ModeOrb")
@Composable
fun ModeTile(
    title: String,
    subtitle: String,
    prize: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ModeOrb(title, prize, selected = true, accent = accent, onClick = onClick, modifier = modifier)
}

@Composable
fun ProvideHudControls(
    musicOn: Boolean,
    sfxOn: Boolean,
    voiceOn: Boolean,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleVoice: () -> Unit,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHudControls provides HudControls(musicOn, sfxOn, voiceOn, onToggleMusic, onToggleSfx, onToggleVoice),
        content = content,
    )
}
