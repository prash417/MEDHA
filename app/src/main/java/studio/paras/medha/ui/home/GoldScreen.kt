package studio.paras.medha.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.MedhaGold
import studio.paras.medha.core.ui.medhaClickable
import studio.paras.medha.core.ui.rememberAssetImage

/** Lobby background, back arrow and a gold title; shared by Profile and Leaderboard. */
@Composable
fun GoldScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    BackHandler(onBack = onBack)
    Box(Modifier.fillMaxSize()) {
        GameLobbyBackground(Modifier.fillMaxSize())
        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .medhaClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = rememberAssetImage("Back.png", trim = true),
                    contentDescription = "Back",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text(
                title.uppercase(),
                fontFamily = Outfit,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 3.sp,
                color = MedhaGold.Gold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 62.dp, bottom = 12.dp),
            content = content,
        )
    }
}
