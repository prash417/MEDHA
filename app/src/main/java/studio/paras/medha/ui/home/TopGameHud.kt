package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.R
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.rememberAssetImage

private val HudHeight = 46.dp
private val ChipHeight = 34.dp
private val ChipAspect = 1930f / 491f
private val ChipWidth = ChipHeight * ChipAspect
private val ChipCountPadStart = 0.24f
private val ChipCountPadEnd = 0.24f
private val ChipPlusW = 0.22f
private val TileHeight = 36.dp
private val TokenSize = 28.dp
private val PlusSize = 22.dp
private val HudGap = 0.dp
private val RankHeight = 108.dp
private val RankPadTop = 4.dp
private val RankWellStart = 96.dp
private val RankWellEnd = 16.dp
private val RankWellTop = 16.dp
private val RankWellBottom = 14.dp
private val RankLevelHeight = 34.dp
private val RankTitleHeight = 16.dp
private const val RankAspect = 1247f / 563f
private val StreakPadTop = 4.dp
private val StreakHeight = 78.dp
private val StreakLockW = 0.108f
private val StreakCheckW = 0.101f
private val StreakCheckPadTop = 2.dp
private val StreakLockPadStart = 0.038f
private val StreakLockPadEnd = 0.043f
private val StreakLockPadTop = 0.36f
private val StreakLockPadBottom = 0.22f

private val RankTextPad = PlatformTextStyle(includeFontPadding = false)
private val RankLineCenter = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both,
)

private val RankLevelStyle = TextStyle(
    fontFamily = Outfit,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 26.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.sp,
    brush = Brush.verticalGradient(
        listOf(Color(0xFFFFF8E8), Color(0xFFFFE08A), Color(0xFFC9A227)),
    ),
    shadow = Shadow(
        color = Color(0x88F0C85A),
        offset = Offset.Zero,
        blurRadius = 10f,
    ),
    textAlign = TextAlign.Center,
    platformStyle = RankTextPad,
    lineHeightStyle = RankLineCenter,
)

private val RankTitleStyle = TextStyle(
    fontFamily = Outfit,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.3.sp,
    brush = Brush.verticalGradient(
        listOf(Color(0xFFFFF3C4), Color(0xFFE8C86A)),
    ),
    textAlign = TextAlign.Center,
    platformStyle = RankTextPad,
    lineHeightStyle = RankLineCenter,
)

@Composable
fun TopGameHud(
    coins: Int,
    gems: Int,
    level: Int,
    onAddCoins: () -> Unit,
    onAddGems: () -> Unit,
    onMessages: () -> Unit,
    onSettings: () -> Unit,
    pingMs: Int? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            Modifier
                .zIndex(2f)
                .heightIn(min = HudHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HudGap),
        ) {
            CurrencyHudChips(coins = coins, gems = gems, onAddCoins = onAddCoins, onAddGems = onAddGems)
            if (pingMs != null) HudPing(pingMs)
            HudAssetButton("email.png", "Messages", onMessages)
            HudAssetButton("Settings.png", "Settings", onSettings)
        }
        Box(
            Modifier
                .padding(top = RankPadTop)
                .height(RankHeight)
                .width(RankHeight * RankAspect),
        ) {
            Image(
                painter = painterResource(R.drawable.your_rank),
                contentDescription = "Your Rank",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = RankWellStart,
                        end = RankWellEnd,
                        top = RankWellTop,
                        bottom = RankWellBottom,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val levelSize = when {
                    level >= 100 -> 22.sp
                    level >= 10 -> 24.sp
                    else -> 26.sp
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(RankLevelHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "#$level",
                        style = RankLevelStyle.copy(fontSize = levelSize, lineHeight = RankLevelHeight.value.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    Modifier
                        .padding(top = 2.dp, bottom = 4.dp)
                        .width(40.dp)
                        .height(1.5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x00F0C85A), Color(0xFFFFE08A), Color(0x00F0C85A)),
                            ),
                        ),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(RankTitleHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        RankBadges.messageFor(level).uppercase(),
                        style = RankTitleStyle,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        val rankWidth = RankHeight * RankAspect
        BoxWithConstraints(
            Modifier
                .padding(top = StreakPadTop)
                .width(rankWidth)
                .height(StreakHeight),
        ) {
            Image(
                painter = painterResource(R.drawable.seven_day_streak),
                contentDescription = "7 Day Streak",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            val lockSize = maxWidth * StreakLockW
            val checkSize = maxWidth * StreakCheckW
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = maxWidth * StreakLockPadStart,
                        end = maxWidth * StreakLockPadEnd,
                        top = maxHeight * StreakLockPadTop,
                        bottom = maxHeight * StreakLockPadBottom,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(7) { day ->
                    val checked = day < 4
                    Box(
                        Modifier.size(lockSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(
                                if (checked) R.drawable.streak_check else R.drawable.streak_lock,
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(top = if (checked) StreakCheckPadTop else 0.dp)
                                .size(if (checked) checkSize else lockSize),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyHudChips(
    coins: Int,
    gems: Int,
    onAddCoins: () -> Unit,
    onAddGems: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.height(HudHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HudGap),
    ) {
        CurrencyChip(
            art = R.drawable.hud_coin_bar,
            value = formatCount(coins),
            valueColor = Color(0xFFFFF1C2),
            onAdd = onAddCoins,
        )
        CurrencyChip(
            art = R.drawable.hud_diamond_bar,
            value = formatCount(gems),
            valueColor = Color(0xFFB8F6FF),
            onAdd = onAddGems,
        )
    }
}

@Composable
private fun CurrencyChip(
    art: Int,
    value: String,
    valueColor: Color,
    onAdd: () -> Unit,
) {
    Box(
        Modifier
            .requiredWidth(ChipWidth)
            .requiredHeight(ChipHeight),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(art),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(ChipWidth, ChipHeight),
        )
        Text(
            value,
            style = TextStyle(
                fontFamily = Outfit,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = valueColor,
                shadow = Shadow(valueColor.copy(alpha = 0.45f), Offset.Zero, 8f),
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ChipWidth * ChipCountPadStart,
                    end = ChipWidth * ChipCountPadEnd,
                ),
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(ChipWidth * ChipPlusW)
                .medhaClickable(onClick = onAdd),
        )
    }
}

@Composable
internal fun HudPing(ms: Int) {
    Text(
        "$ms ms",
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color(0xFFFFE08A),
        modifier = Modifier.padding(end = 8.dp),
    )
}

@Composable
internal fun HudAssetButton(
    fileName: String,
    label: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = TileHeight,
) {
    Box(
        Modifier
            .padding(start = 2.dp)
            .size(size)
            .medhaClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = rememberAssetImage(fileName, trim = true),
            contentDescription = label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
