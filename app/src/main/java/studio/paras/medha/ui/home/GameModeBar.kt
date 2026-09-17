package studio.paras.medha.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.domain.model.GameMode

@Composable
fun GameModeBar(
    modes: List<LobbyMode>,
    selected: GameMode,
    onSelect: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        modes.forEach { mode ->
            GameModeItem(
                item = mode,
                selected = mode.mode == selected,
                onClick = { onSelect(mode.mode) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
fun GameModeItem(
    item: LobbyMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val accent = item.accent.color()
    val shape = RoundedCornerShape(16.dp)
    val fill = when (item.mode) {
        GameMode.CLASSIC -> if (selected) {
            Brush.horizontalGradient(listOf(Color(0xFFFFE08A), Color(0xFFC9A227)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xE00C141C), Color(0xCC080E16)))
        }
        GameMode.QUICK, GameMode.DAILY -> if (selected) {
            Brush.horizontalGradient(listOf(Color(0xFF00D4E8), Color(0xFF0E7490)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xE00A1828), Color(0xCC061018)))
        }
        GameMode.PRACTICE -> if (selected) {
            Brush.horizontalGradient(listOf(Color(0xFFD084FF), Color(0xFF6A1B9A)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xFF9B4DCA), Color(0xFF5A189A)))
        }
    }
    val onGold = selected && item.mode == GameMode.CLASSIC
    val fg = if (onGold) Color(0xFF1A1204) else LobbyPalette.White
    Box(
        modifier
            .graphicsLayer {
                val s = if (pressed) 0.97f else 1f
                scaleX = s
                scaleY = s
            }
            .clip(shape)
            .background(fill)
            .then(
                if (selected && item.mode == GameMode.CLASSIC) {
                    Modifier
                } else {
                    Modifier.border(1.dp, accent.copy(alpha = if (selected) 0.85f else 0.50f), shape)
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                iconFor(item.mode),
                contentDescription = null,
                tint = if (onGold) Color(0xFF1A1204) else accent,
                modifier = Modifier.size(22.dp),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 2.dp),
            ) {
                Text(
                    item.title,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.3.sp,
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    softWrap = false,
                )
                Text(
                    item.subtitle,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (onGold) Color(0xCC2A1C08) else LobbyPalette.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    softWrap = false,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun iconFor(mode: GameMode): ImageVector = when (mode) {
    GameMode.CLASSIC -> Icons.Filled.MilitaryTech
    GameMode.QUICK -> Icons.Outlined.Bolt
    GameMode.DAILY -> Icons.Filled.MilitaryTech
    GameMode.PRACTICE -> Icons.Outlined.GpsFixed
}
