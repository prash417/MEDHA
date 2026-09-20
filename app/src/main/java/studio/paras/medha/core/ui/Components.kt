package studio.paras.medha.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.design.Outfit
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

val FacetShape = GenericShape { size, _ ->
    val cut = size.height * 0.22f
    moveTo(cut, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(cut, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

@Composable
fun Atmosphere(
    modifier: Modifier = Modifier,
    pulse: Float = 0f,
    intensity: Float = 1f,
) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(MedhaColors.Ink)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    MedhaColors.Teal.copy(alpha = 0.10f + 0.05f * pulse) * intensity,
                    Color.Transparent,
                ),
                center = Offset(w / 2f, h * 0.28f),
                radius = min(w, h) * 0.72f,
            ),
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, MedhaColors.Copper.copy(alpha = 0.06f * intensity)),
                startY = h * 0.55f,
                endY = h,
            ),
        )
        val stars = 42
        for (i in 0 until stars) {
            val x = (i * 97f) % w
            val y = (i * 53f) % (h * 0.7f)
            val a = 0.08f + ((i * 13) % 10) / 80f
            drawCircle(
                color = MedhaColors.Ivory.copy(alpha = a * intensity),
                radius = 1.4f + (i % 3),
                center = Offset(x, y),
            )
        }
    }
}

private operator fun Color.times(m: Float) = copy(alpha = alpha * m)

@Composable
fun MedhaMark(
    modifier: Modifier = Modifier.size(92.dp),
    glow: Float = 1f,
) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.42f
        fun diamond(scale: Float): Path {
            val s = r * scale
            return Path().apply {
                moveTo(cx, cy - s)
                lineTo(cx + s, cy)
                lineTo(cx, cy + s)
                lineTo(cx - s, cy)
                close()
            }
        }
        drawPath(diamond(1f), color = MedhaColors.Teal.copy(alpha = 0.18f * glow))
        drawPath(diamond(1f), color = MedhaColors.Teal.copy(alpha = 0.95f * glow), style = Stroke(width = 2.4f))
        drawPath(diamond(0.58f), color = MedhaColors.Copper.copy(alpha = 0.9f * glow), style = Stroke(width = 1.6f))
        drawCircle(
            color = MedhaColors.Ivory.copy(alpha = 0.95f * glow),
            radius = 4.2f + 1.4f * glow,
            center = Offset(cx, cy),
        )
    }
}

@Composable
fun MedhaWordmark(
    subtitle: String? = "PARAS STUDIOS",
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "MEDHA",
            style = MedhaTypography.displayMedium,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MedhaTypography.labelSmall,
                color = MedhaColors.Copper,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun AnswerFacet(
    letter: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visible: Boolean = true,
    selected: Boolean = false,
    revealedCorrect: Boolean = false,
    revealedWrong: Boolean = false,
) {
    if (!visible) {
        Box(
            modifier
                .fillMaxSize()
                .background(MedhaColors.Chamber.copy(alpha = 0.35f))
                .border(1.dp, MedhaColors.Mist.copy(alpha = 0.2f), FacetShape),
        )
        return
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val stroke = when {
        revealedCorrect -> MedhaColors.Correct
        revealedWrong -> MedhaColors.Wrong
        selected -> MedhaColors.Selected
        else -> MedhaColors.Teal.copy(alpha = 0.85f)
    }
    val bg = when {
        revealedCorrect -> Brush.horizontalGradient(listOf(MedhaColors.Correct.copy(alpha = 0.55f), Color(0xFF10291F)))
        revealedWrong -> Brush.horizontalGradient(listOf(MedhaColors.Wrong.copy(alpha = 0.55f), Color(0xFF3A1C16)))
        selected -> Brush.horizontalGradient(listOf(MedhaColors.Selected.copy(alpha = 0.45f), Color(0xFF281E10)))
        else -> Brush.horizontalGradient(listOf(Color(0xCC0A1C22), Color(0x99060C12)))
    }
    val status = when {
        revealedCorrect -> " Correct"
        revealedWrong -> " Incorrect"
        selected -> " Selected"
        else -> ""
    }
    Box(
        modifier
            .graphicsLayer {
                val dim = if (!enabled && !selected && !revealedCorrect) 0.38f else 1f
                alpha = dim
                scaleX = if (pressed) 0.987f else 1f
                scaleY = if (pressed) 0.987f else 1f
            }
            .clip(FacetShape)
            .background(bg)
            .border(2.dp, stroke, FacetShape)
            .semantics { contentDescription = "Option $letter. $text.$status" }
            .medhaClickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .fillMaxSize()
            .padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
            .drawBehind {
                drawLine(
                    color = stroke.copy(alpha = 0.7f),
                    start = Offset(size.width * 0.14f, 8f),
                    end = Offset(size.width * 0.14f, size.height - 8f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round,
                )
            },
    ) {
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = letter,
                style = TextStyle(
                    fontFamily = Cormorant,
                    fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                    color = stroke,
                ),
                modifier = Modifier.padding(start = 10.dp, end = 14.dp),
            )
            Text(
                text = text,
                style = MedhaTypography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
                modifier = Modifier.weight(1f),
            )
            if (revealedCorrect || revealedWrong) {
                Text(
                    text = if (revealedCorrect) "✓" else "✕",
                    color = stroke,
                    fontSize = 16.sp,
                    fontFamily = Outfit,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
fun AvatarGlyph(
    id: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val palette = listOf(
        MedhaColors.Teal,
        MedhaColors.Copper,
        Color(0xFF8EB4E8),
        Color(0xFFD4A0C8),
        Color(0xFFB7D17A),
        Color(0xFFE2C48A),
        Color(0xFF7FD0C8),
        Color(0xFFC9A58B),
    )
    val accent = palette[id.mod(palette.size)]
    Canvas(
        modifier
            .size(size)
            .let { if (selected) it.border(1.dp, accent, androidx.compose.foundation.shape.CircleShape) else it },
    ) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension / 2f
        drawCircle(MedhaColors.Veil)
        drawCircle(accent.copy(alpha = 0.18f), radius = r * 0.92f)
        val rays = 5 + id
        val path = Path()
        for (i in 0 until rays) {
            val a = Math.toRadians((i * (360.0 / rays) - 90)).toFloat()
            val inner = r * 0.22f
            val outer = r * 0.62f
            val x1 = cx + cos(a) * outer
            val y1 = cy + sin(a) * outer
            if (i == 0) path.moveTo(x1, y1) else path.lineTo(x1, y1)
            val b = a + (Math.PI / rays).toFloat()
            path.lineTo(cx + cos(b) * inner, cy + sin(b) * inner)
        }
        path.close()
        drawPath(path, accent.copy(alpha = 0.9f), style = Stroke(width = 2.2f))
        drawCircle(accent, radius = 3.5f, center = Offset(cx, cy))
    }
}
