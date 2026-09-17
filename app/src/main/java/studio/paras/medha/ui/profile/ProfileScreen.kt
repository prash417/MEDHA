package studio.paras.medha.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.ui.ArenaScaffold
import studio.paras.medha.core.ui.AvatarGlyph
import studio.paras.medha.domain.model.Achievement
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.domain.model.PrizeLadder

fun achievementsFor(profile: PlayerProfile): List<Achievement> = listOf(
    Achievement("first_win", "First Win", "Complete a round.", profile.gamesPlayed > 0 && profile.correctAnswers > 0),
    Achievement("ten", "10 Correct", "Answer ten questions correctly.", profile.correctAnswers >= 10),
    Achievement("perfect", "Perfect Round", "Clear every step in a mode.", profile.bestPrize >= 1_00_00_000L),
    Achievement("risk", "Risk Taker", "Play five games.", profile.gamesPlayed >= 5),
    Achievement("master", "Knowledge Master", "Reach 100 correct answers.", profile.correctAnswers >= 100),
    Achievement("crore", "₹1 Crore Club", "Finish the Classic ascent.", profile.bestPrize >= 1_00_00_000L),
)

@Composable
fun ProfileScreen(profile: PlayerProfile, onBack: () -> Unit) {
    val badges = achievementsFor(profile)
    ArenaScaffold(onBack = onBack) {
        Row(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarGlyph(id = profile.avatarId, selected = true, size = 96.dp)
                Spacer(Modifier.height(12.dp))
                Text(profile.displayName.ifBlank { "GUEST" }.uppercase(), style = MedhaTypography.headlineLarge)
                Text("LVL ${profile.level}  ·  ${profile.xp} XP  ·  ${profile.coins} COINS", style = MedhaTypography.labelSmall, color = MedhaColors.Teal)
            }
            Column(
                Modifier
                    .weight(1.2f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Stat("GAMES", profile.gamesPlayed.toString())
                Stat("HIGHEST", if (profile.bestPrize == 0L) "—" else PrizeLadder.formatRupee(profile.bestPrize))
                Stat("CORRECT", profile.correctAnswers.toString())
                Stat("ACCURACY", "${profile.accuracy}%")
                Stat("STREAK", profile.dailyStreak.toString())
                Spacer(Modifier.height(16.dp))
                Text("MARKS OF THE MIND", style = MedhaTypography.labelLarge, color = MedhaColors.Copper)
                Spacer(Modifier.height(8.dp))
                badges.forEach { badge ->
                    Text(
                        badge.title.uppercase(),
                        style = MedhaTypography.titleLarge.copy(
                            color = if (badge.unlocked) MedhaColors.Teal else MedhaColors.Mist,
                            fontSize = 16.sp,
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(badge.detail, style = MedhaTypography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MedhaTypography.labelSmall, modifier = Modifier.weight(1f))
        Text(value, style = MedhaTypography.titleLarge.copy(color = MedhaColors.Ivory, fontSize = 18.sp))
    }
}
