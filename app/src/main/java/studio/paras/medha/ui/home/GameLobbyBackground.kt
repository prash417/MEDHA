package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import studio.paras.medha.R
import androidx.compose.ui.res.painterResource

@Composable
fun GameLobbyBackground(
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(LobbyPalette.Bg0)) {
        Image(
            painter = painterResource(R.drawable.home_page_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
