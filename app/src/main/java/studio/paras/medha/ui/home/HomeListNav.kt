package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.R
import studio.paras.medha.core.design.Outfit

private val ListWidth = 152.dp
private val ListPadH = 12.dp
private val SectionPadTop = 4.dp
private val SectionPadBottom = 4.dp
private val RowStart = 10.dp
private val IconTextGap = 6.dp
private val IconSize = 16.dp
private val LabelSize = 13.sp

private data class HomeNavItem(
    val dest: LobbyDest,
    val label: String,
    val icon: ImageVector,
    val padTop: Dp = SectionPadTop,
    val padBottom: Dp = SectionPadBottom,
)

private val HomeNavItems = listOf(
    HomeNavItem(LobbyDest.HOME, "Home", Icons.Filled.Home, padTop = 8.dp, padBottom = 4.dp),
    HomeNavItem(LobbyDest.LEADERBOARD, "Lead Board", Icons.Filled.Leaderboard, padTop = 4.dp, padBottom = 4.dp),
    HomeNavItem(LobbyDest.MISSIONS, "Missions", Icons.Filled.EmojiEvents, padTop = 4.dp, padBottom = 5.dp),
    HomeNavItem(LobbyDest.PROFILE, "Profile", Icons.Filled.Person, padTop = 4.dp, padBottom = 10.dp),
    HomeNavItem(LobbyDest.SETTINGS, "Settings", Icons.Filled.Settings, padTop = 4.dp, padBottom = 15.dp),
)

private const val ListAspect = 817f / 1414f
private val LabelGold = Color(0xFFE8C15A)

@Composable
fun HomeListNav(
    onSelect: (LobbyDest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .width(ListWidth)
            .aspectRatio(ListAspect),
    ) {
        Image(
            painter = painterResource(R.drawable.home_page_list),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = ListPadH),
        ) {
            HomeNavItems.forEach { item ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = item.padTop, bottom = item.padBottom)
                        .clickable { onSelect(item.dest) },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = RowStart),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = LabelGold,
                            modifier = Modifier.size(IconSize),
                        )
                        Text(
                            item.label,
                            color = LabelGold,
                            fontFamily = Outfit,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = LabelSize,
                            lineHeight = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = IconTextGap),
                        )
                    }
                }
            }
        }
    }
}
