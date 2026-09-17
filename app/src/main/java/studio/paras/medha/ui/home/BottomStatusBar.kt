package studio.paras.medha.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit

@Composable
fun BottomStatusBar(
    quote: String,
    right: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            quote,
            fontFamily = Cormorant,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = LobbyPalette.Dim.copy(alpha = 0.85f),
        )
        Text(
            right,
            fontFamily = Outfit,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            letterSpacing = 1.8.sp,
            color = LobbyPalette.Dim.copy(alpha = 0.8f),
        )
    }
}
