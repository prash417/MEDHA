package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.medhaClickable
import studio.paras.medha.core.ui.rememberAssetImage
import studio.paras.medha.domain.model.Achievement

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
