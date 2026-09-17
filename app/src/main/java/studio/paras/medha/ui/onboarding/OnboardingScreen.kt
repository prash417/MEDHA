package studio.paras.medha.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.ui.ArenaScaffold
import studio.paras.medha.core.ui.AvatarGlyph
import studio.paras.medha.core.ui.EnergyButton
import studio.paras.medha.domain.model.AppLanguage

@Composable
fun OnboardingScreen(
    onFinished: (name: String, avatar: Int, language: AppLanguage) -> Unit,
    onGuest: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableIntStateOf(0) }
    var language by remember { mutableStateOf(AppLanguage.ENGLISH) }

    ArenaScaffold {
        Column(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 32.dp, end = 32.dp, top = 72.dp, bottom = 16.dp),
        ) {
            Text("PARAS STUDIOS", style = MedhaTypography.labelSmall, color = MedhaColors.Copper)
            Spacer(Modifier.height(12.dp))
            when (step) {
                0 -> {
                    Text("WHAT SHOULD WE CALL YOU?", style = MedhaTypography.headlineLarge)
                    Spacer(Modifier.height(20.dp))
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= 24) name = it },
                        textStyle = MedhaTypography.displayMedium.copy(fontSize = 36.sp),
                        cursorBrush = SolidColor(MedhaColors.Teal),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box {
                                if (name.isBlank()) {
                                    Text("YOUR NAME", style = MedhaTypography.displayMedium.copy(color = MedhaColors.Mist, fontSize = 36.sp))
                                }
                                inner()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.weight(1f))
                    EnergyButton(
                        label = "CONTINUE",
                        onClick = { if (name.isNotBlank()) step = 1 },
                        modifier = Modifier.width(360.dp).align(Alignment.CenterHorizontally),
                    )
                    Text(
                        "ENTER AS GUEST",
                        style = MedhaTypography.labelLarge,
                        color = MedhaColors.Mist,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable(onClick = onGuest)
                            .padding(12.dp),
                    )
                }
                1 -> {
                    Text("CHOOSE A MARK", style = MedhaTypography.headlineLarge)
                    Text("A silent emblem. Yours alone.", style = MedhaTypography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(2) { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                repeat(4) { col ->
                                    val id = row * 4 + col
                                    Box(
                                        Modifier.clickable { avatar = id },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AvatarGlyph(id = id, selected = avatar == id)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    EnergyButton("CONTINUE", onClick = { step = 2 }, modifier = Modifier.fillMaxWidth())
                }
                else -> {
                    Text("PREFERRED LANGUAGE", style = MedhaTypography.headlineLarge)
                    Text("More languages can join later.", style = MedhaTypography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            EnergyButton(
                                lang.label,
                                onClick = { language = lang },
                                filled = language == lang,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    EnergyButton(
                        "ENTER",
                        onClick = { onFinished(name.trim(), avatar, language) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
