package studio.paras.medha.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.GoldButton

/** Daily Challenge and Missions shortcuts on the home screen. */
@Composable
fun DailyChallengeButton(
    doneToday: Boolean,
    missionsReady: Int,
    onPlay: () -> Unit,
    onMissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            GoldButton(
                label = "MISSIONS",
                onClick = onMissions,
                height = 34.dp,
                leading = {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFF2A1700),
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
            if (missionsReady > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5483B)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        missionsReady.coerceAtMost(9).toString(),
                        fontFamily = Outfit,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White,
                    )
                }
            }
        }
        GoldButton(
            label = if (doneToday) "DAILY  ✓" else "DAILY CHALLENGE",
            onClick = onPlay,
            height = 34.dp,
            leading = {
                Icon(
                    Icons.Filled.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFF2A1700),
                    modifier = Modifier.size(16.dp),
                )
            },
        )
    }
}
