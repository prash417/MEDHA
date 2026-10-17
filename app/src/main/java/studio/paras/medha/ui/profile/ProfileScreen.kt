package studio.paras.medha.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.AvatarGlyph
import studio.paras.medha.core.ui.GoldButton
import studio.paras.medha.core.ui.GoldPanel
import studio.paras.medha.core.ui.GoldProgress
import studio.paras.medha.core.ui.MedhaGold
import studio.paras.medha.core.ui.medhaClickable
import studio.paras.medha.domain.engine.Achievements
import studio.paras.medha.domain.engine.XpRules
import studio.paras.medha.domain.model.Achievement
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.domain.model.PrizeLadder
import studio.paras.medha.ui.home.GoldScreen

/** Kept for older callers; the list now lives in [Achievements]. */
fun achievementsFor(profile: PlayerProfile): List<Achievement> = Achievements.all(profile)

private const val AvatarCount = 8

@Composable
fun ProfileScreen(
    profile: PlayerProfile,
    onBack: () -> Unit,
    onRename: (String) -> Unit = {},
    onAvatar: (Int) -> Unit = {},
) {
    var renaming by remember { mutableStateOf(false) }
    val place = XpRules.place(profile.xp)
    Box(Modifier.fillMaxSize()) {
    GoldScreen(title = "Profile", onBack = onBack) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GoldPanel(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight(),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AvatarGlyph(id = profile.avatarId, selected = true, size = 72.dp)
                    Text(
                        profile.displayName.ifBlank { "Guest" },
                        fontFamily = Outfit,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        "Level ${place.level}",
                        fontFamily = Outfit,
                        fontSize = 12.sp,
                        color = MedhaGold.Gold,
                    )
                    GoldProgress(
                        place.into.toFloat() / place.need.coerceAtLeast(1),
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    )
                    Text(
                        "${place.into} / ${place.need} XP",
                        fontFamily = Outfit,
                        fontSize = 10.sp,
                        color = Color(0xFFB9C9DD),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    GoldButton(
                        "CHANGE NAME",
                        onClick = { renaming = true },
                        height = 32.dp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        "Pick an avatar",
                        fontFamily = Outfit,
                        fontSize = 11.sp,
                        color = Color(0xFFB9C9DD),
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                    )
                    (0 until AvatarCount).chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { id ->
                                AvatarGlyph(
                                    id = id,
                                    selected = id == profile.avatarId,
                                    size = 36.dp,
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .medhaClickable { onAvatar(id) },
                                )
                            }
                        }
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GoldPanel(title = "Stats", modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Stat("Games", profile.gamesPlayed.toString())
                            Stat("Wins", profile.wins.toString())
                            Stat("Correct", profile.correctAnswers.toString())
                            Stat("Accuracy", "${profile.accuracy}%")
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                        ) {
                            Stat("Best prize", if (profile.bestPrize == 0L) "—" else PrizeLadder.formatRupee(profile.bestPrize))
                            Stat("Login streak", "${profile.dailyStreak} days")
                            Stat("Daily wins", profile.dailyWins.toString())
                            Stat("Coins", profile.coins.toString())
                        }
                    }
                }
                GoldPanel(title = "Achievements", modifier = Modifier.fillMaxWidth()) {
                    Achievements.all(profile).forEach { badge ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    badge.title,
                                    fontFamily = Outfit,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (badge.unlocked) Color.White else Color(0xFF8C9AB0),
                                )
                                Text(badge.detail, fontFamily = Outfit, fontSize = 11.sp, color = Color(0xFFB9C9DD))
                            }
                            Text(
                                if (badge.unlocked) "UNLOCKED" else "LOCKED",
                                fontFamily = Outfit,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = if (badge.unlocked) MedhaGold.Gold else Color(0xFF6E7C92),
                            )
                        }
                    }
                }
            }
        }
    }
    if (renaming) {
        NamePrompt(
            current = profile.displayName,
            title = "Change name",
            onSave = { name ->
                onRename(name)
                renaming = false
            },
            onDismiss = { renaming = false },
        )
    }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(label, fontFamily = Outfit, fontSize = 12.sp, color = Color(0xFFB9C9DD), modifier = Modifier.weight(1f))
        Text(value, fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedhaGold.Hi)
    }
}
