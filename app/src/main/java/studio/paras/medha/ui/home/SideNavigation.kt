package studio.paras.medha.ui.home

import androidx.compose.foundation.background
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit

private data class NavItem(
    val dest: LobbyDest,
    val label: String,
    val icon: ImageVector,
)

private val Items = listOf(
    NavItem(LobbyDest.HOME, "HOME", Icons.Filled.Home),
    NavItem(LobbyDest.LEADERBOARD, "LEADERBOARD", Icons.Outlined.Leaderboard),
    NavItem(LobbyDest.MISSIONS, "MISSIONS", Icons.Outlined.EmojiEvents),
    NavItem(LobbyDest.PROFILE, "PROFILE", Icons.Outlined.Person),
    NavItem(LobbyDest.SETTINGS, "SETTINGS", Icons.Outlined.Settings),
)

@Composable
fun SideNavigation(
    selected: LobbyDest,
    onSelect: (LobbyDest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val panel = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(panel)
            .background(Color(0xF2080E18))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Items.forEach { item ->
            val on = item.dest == selected
            val cell = RoundedCornerShape(12.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(cell)
                    .then(
                        if (on) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFE08A), Color(0xFFC9A227)),
                                ),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .medhaClickable { onSelect(item.dest) }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = if (on) Color(0xFF1A1204) else LobbyPalette.White,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    item.label,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp,
                    color = if (on) Color(0xFF1A1204) else LobbyPalette.White,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
    }
}
