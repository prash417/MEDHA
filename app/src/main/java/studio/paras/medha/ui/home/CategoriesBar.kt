package studio.paras.medha.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.paras.medha.R

private val CategoriesHeight = 100.dp
private val CategoriesPadStart = 16.dp
private val CategoriesPadEnd = 16.dp
private val CategoriesPadTop = 0.dp
private val CategoriesPadBottom = 0.dp
private val CatSlotPadStart = 0.012f
private val CatSlotPadEnd = 0.344f
private val CatSlotPadTop = 0.30f
private val CatSlotPadBottom = 0.16f
private val CatSlotGap = 0.008f
private val CatCuePadStart = 0.718f
private val CatCuePadEnd = 0.042f
private val CatCuePadTop = 0.28f
private val CatCuePadBottom = 0.20f

private data class CatTile(
    val icon: Int,
    val padStart: Dp = 0.dp,
    val padEnd: Dp = 0.dp,
    val padTop: Dp = 0.dp,
    val padBottom: Dp = 0.dp,
)

private val CategoryTiles = listOf(
    CatTile(R.drawable.cat_cg1),
    CatTile(R.drawable.cat_cg2),
    CatTile(R.drawable.cat_cg3),
    CatTile(R.drawable.cat_cg4),
    CatTile(R.drawable.cat_cg5),
    CatTile(R.drawable.cat_cg6),
)

@Composable
fun CategoriesBar(
    onStart: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .padding(
                start = CategoriesPadStart,
                end = CategoriesPadEnd,
                top = CategoriesPadTop,
                bottom = CategoriesPadBottom,
            )
            .requiredHeight(CategoriesHeight),
    ) {
        Image(
            painter = painterResource(R.drawable.categories_chrome),
            contentDescription = "Categories",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(
                    start = maxWidth * CatSlotPadStart,
                    end = maxWidth * CatSlotPadEnd,
                    top = maxHeight * CatSlotPadTop,
                    bottom = maxHeight * CatSlotPadBottom,
                ),
            horizontalArrangement = Arrangement.spacedBy(maxWidth * CatSlotGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryTiles.forEach { tile ->
                Image(
                    painter = painterResource(tile.icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(
                            start = tile.padStart,
                            end = tile.padEnd,
                            top = tile.padTop,
                            bottom = tile.padBottom,
                        ),
                )
            }
        }
        val infinite = rememberInfiniteTransition(label = "challengeCue")
        val pulse by infinite.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    start = maxWidth * CatCuePadStart,
                    end = maxWidth * CatCuePadEnd,
                    top = maxHeight * CatCuePadTop,
                    bottom = maxHeight * CatCuePadBottom,
                )
                .clickable(onClick = onStart),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.challenge_your_mind),
                contentDescription = "Challenge your mind",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    },
            )
        }
    }
}
