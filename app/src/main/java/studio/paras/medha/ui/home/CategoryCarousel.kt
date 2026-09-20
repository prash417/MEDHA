package studio.paras.medha.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit

@Composable
fun CategoryCarousel(
    categories: List<LobbyCategory>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Text(
            "CATEGORIES",
            fontFamily = Outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
            color = LobbyPalette.GoldHi,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.take(6).forEach { cat ->
                val on = cat.id == selectedId
                val shape = RoundedCornerShape(12.dp)
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(shape)
                        .background(
                            if (on) {
                                Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFC9A227)))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xF20A101C), Color(0xE6080E18)))
                            },
                        )
                        .then(
                            if (on) Modifier else Modifier.border(1.dp, LobbyPalette.Gold.copy(alpha = 0.38f), shape),
                        )
                        .medhaClickable { onSelect(cat.id) }
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        iconFor(cat.id),
                        contentDescription = cat.name,
                        tint = if (on) Color(0xFF1A1204) else LobbyPalette.Gold,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        cat.name,
                        fontFamily = Outfit,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = if (on) Color(0xFF1A1204) else LobbyPalette.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}

private fun iconFor(id: String): ImageVector = when (id) {
    "sci" -> Icons.Outlined.Biotech
    "his" -> Icons.Outlined.AccountBalance
    "spo" -> Icons.Outlined.SportsSoccer
    "mov" -> Icons.Outlined.Movie
    "tech" -> Icons.Outlined.Laptop
    "fusion" -> Icons.Outlined.Shuffle
    "nexus" -> Icons.Outlined.Casino
    "art" -> Icons.Outlined.Palette
    "env" -> Icons.Outlined.Park
    else -> Icons.Outlined.Public
}
