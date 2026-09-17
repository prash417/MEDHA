package studio.paras.medha.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.domain.model.Achievement

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
