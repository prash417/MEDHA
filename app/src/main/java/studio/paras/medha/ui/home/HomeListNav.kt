package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
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
private val ArrowSize = 22.dp
private val RowEnd = 4.dp
private val LabelSize = 13.sp

private val RimGold = Brush.verticalGradient(
    listOf(
        Color(0xFFFFF6B8),
        Color(0xFFF0C42A),
        Color(0xFFC98408),
    ),
)

private val LabelStyle = TextStyle(
    fontFamily = Outfit,
    fontWeight = FontWeight.SemiBold,
    fontSize = LabelSize,
    lineHeight = 14.sp,
    brush = RimGold,
    shadow = Shadow(
        color = Color(0x66F0C42A),
        offset = Offset.Zero,
        blurRadius = 6f,
    ),
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

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

private fun Modifier.rimGold(): Modifier = graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithCache {
    onDrawWithContent {
        drawContent()
        drawRect(RimGold, blendMode = BlendMode.SrcAtop)
    }
}

@Composable
fun HomeListNav(
    selected: LobbyDest,
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
                val on = item.dest == selected
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = item.padTop, bottom = item.padBottom)
                        .medhaClickable { onSelect(item.dest) },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = RowStart, end = RowEnd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(IconSize)
                                .rimGold(),
                        )
                        Text(
                            item.label,
                            style = LabelStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = IconTextGap),
                        )
                        if (on) {
                            Spacer(Modifier.weight(1f))
                            Box(Modifier.size(width = 28.dp, height = ArrowSize)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(ArrowSize)
                                        .align(Alignment.CenterStart)
                                        .rimGold(),
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(ArrowSize)
                                        .align(Alignment.CenterStart)
                                        .offset(x = 8.dp)
                                        .rimGold(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
