package studio.paras.medha.ui.leaderboard

import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.ui.ArenaScaffold
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.domain.model.PrizeLadder

private val tabs = listOf("Daily", "Weekly", "Monthly", "All-time")

@Composable
fun LeaderboardScreen(profile: PlayerProfile, onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(3) }
    ArenaScaffold(onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, top = 72.dp, bottom = 16.dp),
        ) {
            Text("LEADERBOARD", style = MedhaTypography.displayMedium.copy(fontSize = 32.sp))
            Text("Ranks sync in Phase 3. Device scores are never trusted alone.", style = MedhaTypography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { i, label ->
                    Text(
                        label.uppercase(),
                        style = MedhaTypography.labelSmall,
                        color = if (i == tab) MedhaColors.Teal else MedhaColors.Mist,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .medhaClickable { tab = i }
                            .padding(vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            if (profile.gamesPlayed == 0) {
                Text("No climbs yet. Finish Classic to take the first seat.", style = MedhaTypography.bodyLarge)
            } else {
                Row(Modifier.fillMaxWidth()) {
                    Text("01", style = MedhaTypography.labelLarge, color = MedhaColors.Copper, modifier = Modifier.padding(end = 16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.displayName.ifBlank { "YOU" }.uppercase(), style = MedhaTypography.titleLarge)
                        Text("${profile.correctAnswers} CORRECT", style = MedhaTypography.bodyMedium)
                    }
                    Text(
                        if (profile.bestPrize == 0L) "—" else PrizeLadder.formatRupee(profile.bestPrize),
                        style = MedhaTypography.titleLarge.copy(color = MedhaColors.Teal),
                    )
                }
            }
        }
    }
}
