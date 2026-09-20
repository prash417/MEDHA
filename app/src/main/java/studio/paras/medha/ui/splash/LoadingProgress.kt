package studio.paras.medha.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit

private val LineStyle = TextStyle(
    fontFamily = Outfit,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    letterSpacing = 0.4.sp,
    lineHeight = 18.sp,
    shadow = Shadow(color = Color.Black, offset = Offset(0f, 2f), blurRadius = 10f),
)

@Composable
fun LoadingProgress(
    state: SplashUiState,
    time: Float,
    modifier: Modifier = Modifier,
    showStudioCredit: Boolean = true,
) {
    val p = state.progress.coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxWidth()
            .padding(bottom = if (showStudioCredit) 14.dp else 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.fillMaxWidth(0.42f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FloorText(
                text = state.line,
                color = Color.White,
                style = LineStyle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            ) {
                Canvas(Modifier.weight(1f).height(20.dp)) {
                    val h = 9.dp.toPx()
                    val stroke = 1.15.dp.toPx()
                    val top = (size.height - h) / 2f
                    val r = CornerRadius(h / 2f, h / 2f)
                    val gold = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF8B6914),
                            Color(0xFFC9A34A),
                            Color(0xFFE8C96A),
                            Color(0xFFD4AF37),
                            Color(0xFF8B6914),
                        ),
                    )
                    drawRoundRect(
                        color = Color(0xE002040C),
                        topLeft = Offset(0f, top),
                        size = Size(size.width, h),
                        cornerRadius = r,
                    )
                    val inset = stroke
                    val fill = ((size.width - inset * 2f) * p).coerceAtLeast(0f)
                    if (fill > 1f) {
                        drawRoundRect(
                            brush = gold,
                            topLeft = Offset(inset, top + inset),
                            size = Size(fill, (h - inset * 2f).coerceAtLeast(1f)),
                            cornerRadius = CornerRadius((h - inset * 2f) / 2f),
                        )
                        val shimmer = ((time * 0.85f) % 1.4f) / 1.4f
                        val sx = fill * shimmer
                        val sw = (fill * 0.20f).coerceAtLeast(8f)
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0x66FFE9A0), Color.Transparent),
                            ),
                            topLeft = Offset(inset + (sx - sw / 2f).coerceAtLeast(0f), top + inset),
                            size = Size(sw.coerceAtMost(fill), (h - inset * 2f).coerceAtLeast(1f)),
                            cornerRadius = CornerRadius((h - inset * 2f) / 2f),
                        )
                    }
                    drawRoundRect(
                        brush = gold,
                        topLeft = Offset(stroke / 2f, top + stroke / 2f),
                        size = Size(size.width - stroke, h - stroke),
                        cornerRadius = r,
                        style = Stroke(width = stroke),
                    )
                }
                FloorText(
                    text = "${state.percent}%",
                    color = Color(0xFFE8C96A),
                    style = LineStyle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Clip,
                    fillWidth = false,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            FloorText(
                text = state.detail.ifBlank { " " },
                color = Color.White,
                style = LineStyle,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(16.dp),
            )
        }

        if (showStudioCredit) {
            Spacer(Modifier.height(14.dp))
            Text(
                "A GAME BY",
                style = LineStyle,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                letterSpacing = 3.2.sp,
                color = Color(0xFFF4F1E6),
                textAlign = TextAlign.Center,
            )
            Text(
                "PARAS STUDIOS",
                fontFamily = Cormorant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 3.4.sp,
                color = SplashPalette.GoldHi,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FloorText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = LineStyle,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fillWidth: Boolean = true,
) {
    val textMod = if (fillWidth) Modifier.fillMaxWidth() else Modifier
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = style.copy(
                color = Color.Black,
                shadow = Shadow(Color.Black, Offset.Zero, 8f),
                drawStyle = Stroke(width = 8f, join = StrokeJoin.Round),
            ),
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            overflow = overflow,
            modifier = textMod,
        )
        Text(
            text = text,
            style = style.copy(shadow = Shadow(Color.Transparent, Offset.Zero, 0f)),
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            overflow = overflow,
            modifier = textMod,
        )
    }
}
