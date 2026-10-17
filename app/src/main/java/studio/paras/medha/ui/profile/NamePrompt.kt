package studio.paras.medha.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.GoldButton
import studio.paras.medha.core.ui.GoldPanel
import studio.paras.medha.core.ui.GoldScrim
import studio.paras.medha.core.ui.MedhaGold
import studio.paras.medha.core.ui.blockTaps

const val MaxNameLength = 16

/** Cleans a typed name: trims, collapses spaces, keeps letters, digits and a few marks. */
fun cleanPlayerName(raw: String): String =
    raw.trim()
        .replace(Regex("\\s+"), " ")
        .filter { it.isLetterOrDigit() || it == ' ' || it == '_' || it == '.' || it == '-' }
        .take(MaxNameLength)

/** Asks for the player's name. Shown once on first launch and from the profile screen. */
@Composable
fun NamePrompt(
    current: String,
    onSave: (String) -> Unit,
    onDismiss: (() -> Unit)? = null,
    title: String = "What should we call you?",
) {
    var text by remember { mutableStateOf(if (current == "Guest") "" else current) }
    val clean = cleanPlayerName(text)
    GoldScrim(onDismiss = onDismiss) {
        GoldPanel(
            title = title,
            modifier = Modifier
                .widthIn(min = 300.dp, max = 440.dp)
                .blockTaps(),
        ) {
            Text(
                "Your name shows on the leaderboard.",
                fontFamily = Outfit,
                fontSize = 13.sp,
                color = Color(0xFFD5E4F5),
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(MaxNameLength + 4) },
                singleLine = true,
                placeholder = { Text("Your name", fontFamily = Outfit) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (clean.length >= 2) onSave(clean) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedhaGold.Gold,
                    unfocusedBorderColor = Color(0x99FFE08A),
                    cursorColor = MedhaGold.Gold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (onDismiss != null) {
                    GoldButton("LATER", onDismiss, Modifier.weight(1f))
                }
                GoldButton(
                    "SAVE",
                    onClick = { onSave(clean) },
                    enabled = clean.length >= 2,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
