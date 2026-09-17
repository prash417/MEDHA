package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.R
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.domain.model.GameMode

@Composable
fun EventPanel(
    events: List<LobbyEvent>,
    nowMs: Long,
    onPlay: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 260.dp,
    dailyHeight: Dp = 152.dp,
    leagueHeight: Dp = 96.dp,
) {
    Column(
        modifier.width(width),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        events.forEach { event ->
            EventBanner(
                event = event,
                nowMs = nowMs,
                modifier = Modifier.height(if (event.id == "daily") dailyHeight else leagueHeight),
                onClick = { event.playMode?.let(onPlay) },
            )
        }
    }
}

@Composable
private fun EventBanner(
    event: LobbyEvent,
    nowMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remain = event.endsAtEpochMs?.let { formatRemain(it, nowMs) }
    val shape = RoundedCornerShape(14.dp)
    val art = if (event.id == "league") R.drawable.event_league else R.drawable.event_trophy
    val daily = event.id == "daily"

    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xF20B1428), Color(0xE6122040), Color(0xF208101C)),
                ),
            )
            .border(1.dp, event.accent.color().copy(alpha = 0.42f), shape)
            .clickable(enabled = event.playMode != null, onClick = onClick)
            .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    event.kicker,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (daily) 12.sp else 11.sp,
                    letterSpacing = 0.6.sp,
                    color = LobbyPalette.White,
                    maxLines = 1,
                )
                if (daily) {
                    Text(
                        "10 QUESTIONS",
                        fontFamily = Outfit,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = LobbyPalette.Mute,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "WIN UP TO",
                        fontFamily = Outfit,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = LobbyPalette.Mute,
                    )
                }
                Text(
                    event.title,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (daily) 20.sp else 15.sp,
                    color = if (daily) LobbyPalette.Gold else LobbyPalette.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = if (daily) 2.dp else 6.dp),
                )
            }
            if (remain != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = LobbyPalette.Gold,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        remain,
                        fontFamily = Outfit,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = LobbyPalette.GoldHi,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
            if (event.cta != null) {
                Row(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(LobbyPalette.Gold, LobbyPalette.GoldHi)))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        event.cta,
                        fontFamily = Outfit,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp,
                        color = Color.Black,
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Image(
            painter = painterResource(art),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxHeight()
                .width(if (daily) 72.dp else 64.dp),
        )
    }
}
