package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.R
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.medhaClickable
import studio.paras.medha.core.ui.rememberAssetImage
import studio.paras.medha.domain.engine.LoginStreak
import studio.paras.medha.domain.model.Achievement
import studio.paras.medha.domain.model.PlayerProfile

@Composable
fun GameSettingsPopup(
    soundOn: Boolean,
    voiceOn: Boolean,
    hapticsOn: Boolean,
    disturbOn: Boolean,
    pingOn: Boolean,
    onToggleSound: () -> Unit,
    onToggleVoice: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleDisturb: () -> Unit,
    onTogglePing: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xB3050810))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        val art = rememberAssetImage("Settings_Box.png", trim = true)
        val aspect = art.width.toFloat() / art.height.toFloat().coerceAtLeast(1f)
        BoxWithConstraints(
            Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.94f)
                .aspectRatio(aspect)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Image(
                bitmap = art,
                contentDescription = "Settings",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            val rowGap = 6.dp
            val topPad = maxHeight * 0.30f
            val listHeight = maxHeight * 0.44f
            val rowHeight = (listHeight - rowGap * 2) / 3
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        start = maxWidth * 0.12f,
                        end = maxWidth * 0.12f,
                        top = topPad,
                    )
                    .height(listHeight)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(rowGap),
            ) {
                SettingRow("Sound.png", "Sound", "All game audio, one switch", soundOn, onToggleSound, rowHeight)
                SettingRow("Host voice.png", "Host voice", "The host reads the question", voiceOn, onToggleVoice, rowHeight)
                SettingRow("Vibration.png", "Vibration", "Pulse on lock, correct, and wrong", hapticsOn, onToggleHaptics, rowHeight)
                SettingRow("Do not disturb.png", "Do not disturb", "No toasts or messages while you play", disturbOn, onToggleDisturb, rowHeight)
                SettingRow("Ping.png", "Ping", "Live connection time on the top bar", pingOn, onTogglePing, rowHeight)
            }
        }
    }
}

data class ShopPack(val title: String, val detail: String, val grant: Int)

@Composable
fun StreakPopup(
    profile: PlayerProfile,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
    onDayChanged: (Long) -> Unit = {},
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val zone = ZoneId.systemDefault()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    LaunchedEffect(today) { onDayChanged(today.toEpochDay()) }
    val week = LoginStreak.week(profile, today.toEpochDay())
    val canClaim = LoginStreak.canClaim(week, nowMillis)
    val nextBonus = LoginStreak.bonusFor(week)
    val reducedRewards = LoginStreak.hasReducedRewards(week)
    val nextRewardDay = week.todayIndex + if (LoginStreak.claimed(week, week.todayIndex)) 1 else 0
    val nextRewardIsGem = reducedRewards && nextRewardDay in 0..6 && LoginStreak.dayGems[nextRewardDay] > 0
    val nextRewardLabel = when {
        nextRewardIsGem -> "1 Gem"
        reducedRewards -> "100 Coins"
        else -> "$nextBonus Coins"
    }
    val nextClaimAt = maxOf(
        LoginStreak.nextClaimAt(week),
        if (LoginStreak.claimed(week, week.todayIndex)) {
            today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        } else 0L,
    )
    val comeBackLabel = if (canClaim) "Ready" else streakComeBackLabel(nextClaimAt - nowMillis)
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .zIndex(8f)
            .background(Color(0xB3050810))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        val art = rememberAssetImage("7 days.png", trim = true)
        val aspect = art.width.toFloat() / art.height.toFloat().coerceAtLeast(1f)
        val panelWidth = minOf(maxWidth * 0.90f, maxHeight * 0.94f * aspect)
        val claimedDays = (week.joinIndex..6).count { day ->
            studio.paras.medha.domain.engine.LoginStreak.claimed(week, day)
        }
        val mark = when {
            week.broken -> "Skiped.png"
            claimedDays in 1..7 -> "$claimedDays day.png"
            else -> null
        }
        val resetAt = LocalDate.ofEpochDay(week.anchor)
            .plusDays(7)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val resetLabel = streakResetLabel((resetAt - nowMillis).coerceAtLeast(0L))
        BoxWithConstraints(
            Modifier
                .align(Alignment.Center)
                .width(panelWidth)
                .aspectRatio(aspect)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Image(
                bitmap = art,
                contentDescription = "7 day streak",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = maxWidth * 0.822f, top = maxHeight * 0.211f)
                    .size(width = maxWidth * 0.108f, height = maxHeight * 0.038f),
                contentAlignment = Alignment.Center,
            ) {
                if (mark != null) {
                    Image(
                        bitmap = rememberAssetImage(mark, trim = true),
                        contentDescription = mark,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            // Size visible artwork, not the unequal transparent margins in each PNG.
            val rowWidth = maxWidth * 0.88f
            val tileMargin = maxWidth * 0.007f
            val tileWidth = (rowWidth - tileMargin * 6) / 7
            val rowHeight = tileWidth * 1.5f
            val rowTop = maxHeight * 0.33f + (maxHeight * 0.40f - rowHeight) / 2
            Row(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = rowTop)
                    .width(rowWidth)
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(tileMargin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(7) { day ->
                    val claimed = studio.paras.medha.domain.engine.LoginStreak.claimed(week, day)
                    val dayName = studio.paras.medha.domain.engine.LoginStreak.dayNames[day]
                    val claimable = day == week.todayIndex && canClaim
                    val state = when {
                        claimed -> "claimed"
                        claimable -> "claim"
                        else -> "locked"
                    }
                    val reducedCard = reducedRewards && day >= week.joinIndex
                    val amount = if (LoginStreak.dayGems[day] > 0) "1 Gem" else "100 Coins"
                    val actionLabel = when {
                        claimed -> "$dayName reward claimed"
                        claimable -> "$dayName claim reward"
                        else -> "$dayName reward locked"
                    }
                    val buttonLabel = if (reducedCard) "$actionLabel, $amount" else actionLabel
                    BoxWithConstraints(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .todayRewardShine(enabled = claimed || day == week.todayIndex)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = claimable,
                                onClick = onClaim,
                            ),
                    ) {
                        Image(
                            bitmap = rememberAssetImage(streakStateAsset(week, day, state), trim = true),
                            contentDescription = buttonLabel,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            StreakCountdown(
                label = resetLabel,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = maxWidth * 0.871f, top = maxHeight * 0.268f)
                    .size(width = maxWidth * 0.076f, height = maxHeight * 0.040f),
            )
            if (nextRewardIsGem) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = maxWidth * 0.642f, top = maxHeight * 0.792f)
                        .size(width = maxWidth * 0.043f, height = maxHeight * 0.066f)
                        .background(Color(0xFF00193E)),
                ) {
                    Image(
                        painter = painterResource(R.drawable.hud_gem),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                    )
                }
            }
            StreakCountdown(
                label = nextRewardLabel,
                alignment = Alignment.Center,
                color = Color(0xFFFFE8A3),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = maxWidth * 0.684f, top = maxHeight * 0.808f)
                    .size(width = maxWidth * 0.088f, height = maxHeight * 0.041f)
                    .background(Color(0xFF00193E)),
            )
            StreakCountdown(
                label = comeBackLabel,
                alignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = maxWidth * 0.829f, top = maxHeight * 0.808f)
                    .size(width = maxWidth * 0.088f, height = maxHeight * 0.041f),
            )
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = maxWidth * 0.008f, top = maxHeight * 0.105f)
                    .size(width = maxWidth * 0.065f, height = maxHeight * 0.11f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
    }
}

@Composable
private fun StreakCountdown(
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.CenterStart,
    color: Color = Color(0xFFF4FBFF),
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier, contentAlignment = alignment) {
        val style = TextStyle(
            fontFamily = Outfit,
            fontWeight = FontWeight.SemiBold,
            fontSize = with(density) { (maxHeight * 0.72f).toSp() },
            color = color,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        )
        val measured = textMeasurer.measure(label, style, softWrap = false, maxLines = 1)
        val scale = with(density) {
            minOf(
                1f,
                maxWidth.toPx() / measured.size.width.coerceAtLeast(1),
                maxHeight.toPx() / measured.size.height.coerceAtLeast(1),
            )
        }
        Text(
            label,
            style = style.copy(fontSize = (style.fontSize.value * scale).sp),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun CurrencyShopPopup(
    fileName: String,
    watchTitle: String,
    watchDetail: String,
    onWatch: () -> Unit,
    packs: List<ShopPack>,
    onBuy: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(8f)
            .background(Color(0xB3050810))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        val art = rememberAssetImage(fileName, trim = true)
        val aspect = art.width.toFloat() / art.height.toFloat().coerceAtLeast(1f)
        BoxWithConstraints(
            Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.82f)
                .aspectRatio(aspect)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Image(
                bitmap = art,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = maxWidth * 0.11f,
                        end = maxWidth * 0.11f,
                        top = maxHeight * 0.30f,
                        bottom = maxHeight * 0.20f,
                    )
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShopPackRow(
                    title = watchTitle,
                    detail = watchDetail,
                    action = "WATCH",
                    enabled = true,
                    onClick = onWatch,
                )
                packs.forEach { pack ->
                    ShopPackRow(
                        title = pack.title,
                        detail = pack.detail,
                        action = "BUY",
                        enabled = true,
                        onClick = { onBuy(pack.grant) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopPackRow(
    title: String,
    detail: String,
    action: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val plate = rememberAssetImage("Settings option box.png", trim = true)
    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .medhaClickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Image(
            bitmap = plate,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 1)
                Text(detail, fontFamily = Outfit, fontSize = 11.sp, color = Color(0xFFD5E4F5), maxLines = 1, modifier = Modifier.padding(top = 2.dp))
            }
            Text(
                action,
                fontFamily = Outfit,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp,
                color = if (enabled) Color(0xFFFFF3C4) else Color(0xFF9BB0C8),
            )
        }
    }
}

@Composable
fun ArtFramePopup(
    fileName: String,
    onDismiss: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xB3050810))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        val art = rememberAssetImage(fileName, trim = true)
        val aspect = art.width.toFloat() / art.height.toFloat().coerceAtLeast(1f)
        Image(
            bitmap = art,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.82f)
                .aspectRatio(aspect)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        )
    }
}

@Composable
private fun SettingRow(
    iconFile: String,
    title: String,
    detail: String,
    on: Boolean,
    onClick: () -> Unit,
    rowHeight: androidx.compose.ui.unit.Dp,
) {
    val plate = rememberAssetImage("Settings option box.png", trim = true)
    val line = TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .medhaClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Image(
            bitmap = plate,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = rememberAssetImage(iconFile, trim = true),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(rowHeight * 0.56f),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    title,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 17.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = line,
                )
                Text(
                    detail,
                    fontFamily = Outfit,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFFD5E4F5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = line,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            OnOffToggle(on, rowHeight * 0.46f)
        }
    }
}

@Composable
private fun OnOffToggle(on: Boolean, height: androidx.compose.ui.unit.Dp) {
    val art = rememberAssetImage(if (on) "ON.png" else "OFF.png", trim = true)
    val aspect = art.width.toFloat() / art.height.toFloat().coerceAtLeast(1f)
    Image(
        bitmap = art,
        contentDescription = if (on) "ON" else "OFF",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .height(height)
            .width(height * aspect),
    )
}

@Composable
fun BoxScope.LobbyOverlayHost(
    overlay: LobbyOverlay?,
    catalog: LobbyCatalog,
    onDismiss: () -> Unit,
) {
    if (overlay == null) return
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xB3050810))
            .clickable(onClick = onDismiss),
    ) {
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.82f)
                .widthIn(min = 360.dp, max = 520.dp)
                .clip(LobbyRound)
                .background(LobbyPalette.PanelSolid)
                .clickable(onClick = {})
                .padding(20.dp),
        ) {
            when (overlay) {
                LobbyOverlay.MISSIONS -> MissionList(catalog)
                LobbyOverlay.ACHIEVEMENTS -> AchievementList(catalog.achievements)
                LobbyOverlay.INVENTORY -> SimpleNote("INVENTORY", "Seals, boosts and chamber kits land here with the live season.")
                LobbyOverlay.FRIENDS -> SimpleNote("FRIENDS", "The chamber circle opens with the live season.")
                LobbyOverlay.MESSAGES -> SimpleNote("MESSAGES", "No sealed notes waiting.")
                LobbyOverlay.VAULT -> VaultNote()
            }
        }
    }
}

@Composable
private fun MissionList(catalog: LobbyCatalog) {
    Column(Modifier.fillMaxSize()) {
        Header("MISSIONS")
        catalog.missions.forEach { m ->
            Column(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                Text(m.title.uppercase(), fontFamily = Outfit, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = LobbyPalette.White)
                Text(m.detail, fontFamily = Outfit, fontSize = 11.sp, color = LobbyPalette.Mute)
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(LobbyRound)
                        .background(Color(0x33000000)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(m.progress.coerceIn(0.08f, 1f))
                            .height(5.dp)
                            .clip(LobbyRound)
                            .background(LobbyPalette.Gold),
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementList(items: List<Achievement>) {
    Column(Modifier.fillMaxSize()) {
        Header("ACHIEVEMENTS")
        items.forEach { a ->
            Row(
                Modifier.padding(top = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(a.title.uppercase(), fontFamily = Outfit, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = LobbyPalette.White)
                    Text(a.detail, fontFamily = Outfit, fontSize = 11.sp, color = LobbyPalette.Mute)
                }
                Text(
                    if (a.unlocked) "SEALED" else "LOCKED",
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = if (a.unlocked) LobbyPalette.Gold else LobbyPalette.Dim,
                )
            }
        }
    }
}

@Composable
private fun VaultNote() {
    Column {
        Header("VAULT")
        Text("Coins and gems will bind to the live economy. The vault is dressed. The ledger is not live yet.", fontFamily = Outfit, fontSize = 13.sp, color = LobbyPalette.Mute, modifier = Modifier.padding(top = 12.dp))
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VaultPack("2,500", "COINS")
            VaultPack("150", "GEMS")
            VaultPack("BUNDLE", "GOLD")
        }
    }
}

@Composable
private fun VaultPack(top: String, bottom: String) {
    Column(
        Modifier
            .width(120.dp)
            .clip(LobbyRound)
            .background(Color(0x33000000))
            .padding(12.dp),
    ) {
        Text(top, fontFamily = Cormorant, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = LobbyPalette.GoldHi)
        Text(bottom, fontFamily = Outfit, fontSize = 10.sp, letterSpacing = 1.6.sp, color = LobbyPalette.Mute)
    }
}

@Composable
private fun SimpleNote(title: String, body: String) {
    Column {
        Header(title)
        Spacer(Modifier.height(12.dp))
        Text(body, fontFamily = Outfit, fontSize = 13.sp, color = LobbyPalette.Mute)
    }
}

private fun streakResetLabel(remainingMs: Long): String {
    val total = remainingMs.coerceAtLeast(0L) / 1000L
    val days = total / 86_400L
    val hours = (total % 86_400L) / 3_600L
    val minutes = (total % 3_600L) / 60L
    return "${days}d ${hours}h ${minutes}m"
}

private fun streakComeBackLabel(remainingMs: Long): String {
    val seconds = (remainingMs.coerceAtLeast(0L) + 999L) / 1000L
    val hours = (seconds / 3_600L).toString().padStart(2, '0')
    val minutes = (seconds % 3_600L / 60L).toString().padStart(2, '0')
    val remainder = (seconds % 60L).toString().padStart(2, '0')
    return "$hours:$minutes:$remainder"
}

private fun streakStateAsset(
    week: studio.paras.medha.domain.engine.LoginStreak.Week,
    day: Int,
    state: String,
): String {
    val dayCard = studio.paras.medha.domain.engine.LoginStreak.cardFor(week, day)
    val card = if (dayCard == studio.paras.medha.domain.engine.LoginStreak.GrandSundayCard) {
        "sun_continue_7_days"
    } else {
        dayCard.removeSuffix(".png")
    }
    val rewardVariant = if (LoginStreak.hasReducedRewards(week) && day >= week.joinIndex) "_reduced" else ""
    return "streak_${card}${rewardVariant}_$state.png"
}

@Composable
private fun Header(text: String) {
    Text(
        text,
        fontFamily = Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 3.sp,
        color = LobbyPalette.GoldHi,
    )
}
