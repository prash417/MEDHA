package studio.paras.medha.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.AvatarGlyph
import studio.paras.medha.core.ui.GoldButton
import studio.paras.medha.core.ui.GoldPanel
import studio.paras.medha.core.ui.MedhaGold
import studio.paras.medha.data.cloud.FirebaseCloud
import studio.paras.medha.data.cloud.LeaderRow
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.domain.model.PrizeLadder
import studio.paras.medha.ui.home.GoldScreen

private enum class BoardTab(val label: String, val field: String) {
    XP("Top XP", "xp"),
    PRIZE("Best prize", "bestPrize"),
}

private sealed interface BoardState {
    data object Loading : BoardState
    data class Ready(val rows: List<LeaderRow>) : BoardState
    data class Failed(val message: String) : BoardState
}

@Composable
fun LeaderboardScreen(
    profile: PlayerProfile,
    cloud: FirebaseCloud,
    onBack: () -> Unit,
) {
    var tab by remember { mutableStateOf(BoardTab.XP) }
    var state by remember { mutableStateOf<BoardState>(BoardState.Loading) }
    var reload by remember { mutableIntStateOf(0) }
    LaunchedEffect(tab, reload) {
        state = BoardState.Loading
        state = runCatching { cloud.topPlayers(tab.field) }
            .fold(
                onSuccess = { BoardState.Ready(it) },
                onFailure = { BoardState.Failed("Could not load the leaderboard. Check your internet.") },
            )
    }
    val me = cloud.uid()
    GoldScreen(title = "Leaderboard", onBack = onBack) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GoldPanel(title = "You", modifier = Modifier.width(220.dp)) {
                AvatarGlyph(id = profile.avatarId, selected = true, size = 64.dp)
                Text(
                    profile.displayName.ifBlank { "Guest" },
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                )
                MiniStat("Level", profile.level.toString())
                MiniStat("XP", profile.xp.toString())
                MiniStat("Best prize", if (profile.bestPrize == 0L) "—" else PrizeLadder.formatRupee(profile.bestPrize))
                if (profile.gamesPlayed == 0) {
                    Text(
                        "Finish a game to join the board.",
                        fontFamily = Outfit,
                        fontSize = 11.sp,
                        color = Color(0xFFD5E4F5),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BoardTab.entries.forEach { t ->
                        GoldButton(
                            label = t.label.uppercase(),
                            onClick = { tab = t },
                            enabled = t != tab,
                            height = 32.dp,
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                ) {
                    when (val s = state) {
                        BoardState.Loading -> CenterNote("Loading…")
                        is BoardState.Failed -> Column(
                            Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CenterNote(s.message)
                            GoldButton("RETRY", onClick = { reload++ }, modifier = Modifier.padding(top = 10.dp))
                        }
                        is BoardState.Ready -> if (s.rows.isEmpty()) {
                            CenterNote("No one on the board yet. Be the first!")
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                items(s.rows, key = { it.uid }) { row ->
                                    BoardRow(row, isMe = row.uid == me, tab = tab)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardRow(row: LeaderRow, isMe: Boolean, tab: BoardTab) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isMe) Color(0xCC5A3F06) else Color(0xB3071531))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "#${row.rank}",
            fontFamily = Outfit,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = when (row.rank) {
                1 -> Color(0xFFFFD54A)
                2 -> Color(0xFFD9E2EC)
                3 -> Color(0xFFE0A26B)
                else -> MedhaGold.Hi
            },
            modifier = Modifier.width(44.dp),
        )
        AvatarGlyph(id = row.avatarId, selected = isMe, size = 30.dp)
        Column(
            Modifier
                .weight(1f)
                .padding(start = 10.dp),
        ) {
            Text(
                if (isMe) "${row.name} (you)" else row.name,
                fontFamily = Outfit,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Level ${row.level}  ·  ${row.wins} wins",
                fontFamily = Outfit,
                fontSize = 11.sp,
                color = Color(0xFFB9C9DD),
            )
        }
        Text(
            when (tab) {
                BoardTab.XP -> "${row.xp} XP"
                BoardTab.PRIZE -> PrizeLadder.formatRupee(row.bestPrize)
            },
            fontFamily = Outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MedhaGold.Gold,
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
    ) {
        Text(label, fontFamily = Outfit, fontSize = 12.sp, color = Color(0xFFB9C9DD), modifier = Modifier.weight(1f))
        Text(value, fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedhaGold.Hi)
    }
}

@Composable
private fun CenterNote(text: String) {
    Text(
        text,
        fontFamily = Outfit,
        fontSize = 14.sp,
        color = Color(0xFFD5E4F5),
        modifier = Modifier.padding(16.dp),
    )
}
