package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.paras.medha.R
import studio.paras.medha.domain.model.GameMode

private val ModeHeight = 60.dp
private val ClassicHeight = 56.dp
private val QuickHeight = 66.dp
private val ModeGap = 0.dp
private val ModePadStart = 0.dp
private val ModePadEnd = 16.dp
private val ModePadTop = 6.dp
private val ModePadBottom = 0.dp

private data class ModeArt(
    val mode: GameMode,
    val drawable: Int,
    val label: String,
    val height: Dp,
)

private val ModeItems = listOf(
    ModeArt(GameMode.CLASSIC, R.drawable.mode_classic, "Classic", ClassicHeight),
    ModeArt(GameMode.QUICK, R.drawable.mode_quick_play, "Quick Play", QuickHeight),
    ModeArt(GameMode.PRACTICE, R.drawable.mode_practice, "Practice", ModeHeight),
)

@Composable
fun ModePlayList(
    onPlay: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(
                start = ModePadStart,
                end = ModePadEnd,
                bottom = ModePadBottom,
            )
            .offset(y = ModePadTop),
        horizontalArrangement = Arrangement.spacedBy(ModeGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModeItems.forEach { item ->
            Image(
                painter = painterResource(item.drawable),
                contentDescription = item.label,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .weight(1f)
                    .height(item.height)
                    .clickable { onPlay(item.mode) },
            )
        }
    }
}
