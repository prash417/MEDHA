package studio.paras.medha.ui.home

import androidx.compose.foundation.background
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit

private val StartRibbon = GenericShape { size, _ ->
    val tip = size.height * 0.36f
    moveTo(0f, 0f)
    lineTo(size.width - tip, 0f)
    lineTo(size.width, size.height * 0.5f)
    lineTo(size.width - tip, size.height)
    lineTo(0f, size.height)
    close()
}

@Composable
fun StartGameButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier
            .fillMaxHeight()
            .graphicsLayer {
                val s = if (pressed) 0.97f else 1f
                scaleX = s
                scaleY = s
            }
            .clip(StartRibbon)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF8A6420), Color(0xFFF0C85A), Color(0xFFFFF3C4)),
                ),
            )
            .medhaClickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(start = 16.dp, end = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "START",
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 2.2.sp,
                    color = Color(0xFF1A1204),
                    maxLines = 1,
                )
                Text(
                    "TEST YOUR MIND. WIN YOUR WAY.",
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Medium,
                    fontSize = 8.sp,
                    letterSpacing = 0.3.sp,
                    color = Color(0xCC2A1C08),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "››",
                fontFamily = Outfit,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF1A1204),
            )
        }
    }
}
