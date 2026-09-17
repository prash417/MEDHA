package studio.paras.medha.ui.result

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.ui.ArenaScaffold
import studio.paras.medha.core.ui.EnergyButton
import studio.paras.medha.core.ui.MedhaMark
import studio.paras.medha.domain.model.GameOutcome
import studio.paras.medha.domain.model.GameResult

@Composable
fun ResultScreen(
    result: GameResult?,
    onHome: () -> Unit,
    onReplay: () -> Unit,
) {
    val outcome = result?.outcome ?: GameOutcome.DROPPED
    val title = when (outcome) {
        GameOutcome.WON -> "THE ASCENT HOLDS"
        GameOutcome.WALKED_AWAY -> "LEFT ON YOUR TERMS"
        GameOutcome.DROPPED -> "THE CHAMBER CLOSES"
    }
    val accent = when (outcome) {
        GameOutcome.WON -> MedhaColors.Teal
        GameOutcome.WALKED_AWAY -> MedhaColors.Copper
        GameOutcome.DROPPED -> MedhaColors.Wrong
    }
    val subtitle = when (outcome) {
        GameOutcome.WON -> "Every step. The full ladder."
        GameOutcome.WALKED_AWAY -> "You sealed the prize you had already earned."
        GameOutcome.DROPPED -> "A miss drops you to the last lock — not the last correct."
    }
    ArenaScaffold(accent = accent, onBack = onHome) {
        Row(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, top = 72.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(Modifier.weight(1.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                MedhaMark(glow = if (outcome == GameOutcome.WON) 1.3f else 0.8f)
                Spacer(Modifier.height(10.dp))
                Text(title, style = MedhaTypography.labelLarge, color = accent, textAlign = TextAlign.Center)
                Text(
                    result?.prizeLabel ?: "₹0",
                    style = MedhaTypography.displayMedium.copy(fontSize = 52.sp, letterSpacing = 2.sp, color = accent),
                    textAlign = TextAlign.Center,
                )
                Text(subtitle, style = MedhaTypography.bodyMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    "${result?.correctCount ?: 0} CLEAR   ·   ${result?.mode?.title ?: ""}",
                    style = MedhaTypography.labelLarge,
                    color = MedhaColors.IvoryMute,
                )
            }
            Column(Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!result?.lastCorrectText.isNullOrBlank() && outcome == GameOutcome.DROPPED) {
                    Text("CORRECT ANSWER", style = MedhaTypography.labelSmall, color = MedhaColors.Mist)
                    Text(result?.lastCorrectText.orEmpty(), style = MedhaTypography.headlineLarge.copy(fontSize = 22.sp))
                    if (!result?.lastExplanation.isNullOrBlank()) {
                        Text("DID YOU KNOW?", style = MedhaTypography.labelSmall, color = MedhaColors.Copper)
                        Text(result?.lastExplanation.orEmpty(), style = MedhaTypography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                EnergyButton("PLAY AGAIN", onClick = onReplay, modifier = Modifier.fillMaxWidth())
                EnergyButton("HOME", onClick = onHome, filled = false, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
