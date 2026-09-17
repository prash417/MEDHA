package studio.paras.medha.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.R
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit

private val NameStyle = TextStyle(
    fontFamily = Cormorant,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.3.sp,
    brush = Brush.linearGradient(
        listOf(Color(0xFFFFF8E8), Color(0xFFE8C86A)),
    ),
    shadow = Shadow(
        color = Color(0x66F0C85A),
        offset = Offset.Zero,
        blurRadius = 8f,
    ),
)

private val GoldRing = Brush.linearGradient(
    listOf(Color(0xFFFFF3C4), Color(0xFFC9A227), Color(0xFFFFE08A)),
)

@Composable
fun PlayerProfileHud(
    player: LobbyPlayer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val height = 64.dp
    val width = height * 2039f / 462f
    Box(
        modifier
            .height(height)
            .width(width)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.profile_box),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.m1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
            )
            Column(
                Modifier
                    .padding(start = 14.dp, end = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LevelHex(
                        level = player.level,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Column {
                        Text(
                            player.name,
                            style = NameStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Box(
                            Modifier
                                .padding(top = 3.dp)
                                .width(28.dp)
                                .height(1.5.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFE08A), Color(0x00F0C85A)),
                                    ),
                                ),
                        )
                    }
                }
                Box(
                    Modifier
                        .padding(start = 4.dp, top = 6.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8A8270), Color(0xFFD4C49A), Color(0xFF8A8270)),
                            ),
                            CircleShape,
                        )
                        .clip(CircleShape)
                        .background(Color(0xFF0A0C14)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(player.xpProgress.coerceIn(0.16f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB8862A), Color(0xFFFFD56A), Color(0xFFFFF6D0)),
                                ),
                            ),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.45f)
                                .align(Alignment.TopCenter)
                                .background(Color(0x55FFFFFF)),
                        )
                    }
                }
            }
            Box(
                Modifier
                    .padding(end = 10.dp)
                    .width(1.dp)
                    .height(26.dp)
                    .background(Color(0x55E8C15A)),
            )
            Image(
                painter = painterResource(RankBadges.drawableFor(player.level)),
                contentDescription = RankBadges.labelFor(player.level),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(42.dp),
            )
        }
    }
}

@Composable
private fun LevelHex(level: Int, modifier: Modifier = Modifier) {
    Box(
        modifier.size(28.dp, 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val hex = Path().apply {
                moveTo(w * 0.22f, 1.5f)
                lineTo(w * 0.78f, 1.5f)
                lineTo(w - 1.5f, h * 0.5f)
                lineTo(w * 0.78f, h - 1.5f)
                lineTo(w * 0.22f, h - 1.5f)
                lineTo(1.5f, h * 0.5f)
                close()
            }
            drawPath(hex, Color(0xFF14100A))
            drawPath(hex, GoldRing, style = Stroke(width = 2.2f))
        }
        Text(
            "$level",
            color = Color(0xFFFFF1C4),
            fontFamily = Outfit,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (level >= 100) 9.sp else 11.sp,
            lineHeight = 12.sp,
        )
    }
}
