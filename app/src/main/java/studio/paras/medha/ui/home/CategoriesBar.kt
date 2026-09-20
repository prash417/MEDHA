package studio.paras.medha.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.paras.medha.R
import androidx.compose.ui.res.painterResource
import kotlin.math.max

private val CategoriesHeight = 100.dp
private val CategoriesPadStart = 16.dp
private val CategoriesPadEnd = 16.dp
private val CategoriesPadTop = 8.dp
private val CategoriesPadBottom = 0.dp
private val CatSlotPadStart = 0.012f
private val CatSlotPadEnd = 0.344f
private val CatRowPadEnd = 0.304f
private val CatSlotPadTop = 0.30f
private val CatSlotPadBottom = 0.16f
private val CatSlotGap = 0.003f
private val CatCuePadStart = 0.718f
private val CatCuePadEnd = 0.042f
private val CatCuePadTop = 0.28f
private val CatCuePadBottom = 0.20f

private data class CatTile(
    val id: String,
    val icon: Int,
    val padStart: Dp = 0.dp,
    val padEnd: Dp = 0.dp,
    val padTop: Dp = 0.dp,
    val padBottom: Dp = 0.dp,
)

private val CategoryTiles = listOf(
    CatTile("gk", R.drawable.cat_cg1),
    CatTile("sci", R.drawable.cat_cg2),
    CatTile("his", R.drawable.cat_cg3),
    CatTile("spo", R.drawable.cat_cg4),
    CatTile("mov", R.drawable.cat_cg5),
    CatTile("tech", R.drawable.cat_cg6),
    CatTile("fusion", R.drawable.cat_cg7),
    CatTile("nexus", R.drawable.cat_cg8),
)

@Composable
fun CategoriesBar(
    selectedId: String?,
    onSelectCategory: (String) -> Unit,
    onStart: () -> Unit,
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
        val gap = maxWidth * CatSlotGap
        val slotW = maxWidth * (1f - CatSlotPadStart - CatSlotPadEnd)
        val tileW = (slotW - gap * 5) / 6
        val peekW = maxWidth * (CatSlotPadEnd - CatRowPadEnd)
        val listState = rememberLazyListState()
        val snapped = listState.firstVisibleItemScrollOffset < 8
        val fadeStart = listState.canScrollBackward && !snapped
        val fadeEnd = listState.canScrollForward
        val layout = listState.layoutInfo
        val infinite = rememberInfiniteTransition(label = "catBar")
        val shine by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "catShine",
        )
        val cuePulse by infinite.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        )
        LazyRow(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(end = peekW),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = maxWidth * CatSlotPadStart,
                    end = maxWidth * CatRowPadEnd,
                    top = maxHeight * CatSlotPadTop,
                    bottom = maxHeight * CatSlotPadBottom,
                )
                .clipToBounds()
                .wellMask(fadeStart = fadeStart, fadeEnd = fadeEnd),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(CategoryTiles, key = { _, tile -> tile.id }) { index, tile ->
                val on = tile.id == selectedId
                val item = layout.visibleItemsInfo.find { it.index == index }
                val cut = item?.cutFraction(layout.viewportStartOffset, layout.viewportEndOffset) ?: 0f
                Image(
                    painter = painterResource(tile.icon),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .width(tileW)
                        .fillMaxHeight()
                        .padding(
                            start = tile.padStart,
                            end = tile.padEnd,
                            top = tile.padTop,
                            bottom = tile.padBottom,
                        )
                        .graphicsLayer {
                            val s = 1f - cut * 0.16f
                            scaleX = s
                            scaleY = s
                            alpha = 1f - cut * 0.42f
                            if (on) compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .catShine(shine, on)
                        .medhaClickable { onSelectCategory(tile.id) },
                )
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(
                    start = maxWidth * CatCuePadStart,
                    end = maxWidth * CatCuePadEnd,
                    top = maxHeight * CatCuePadTop,
                    bottom = maxHeight * CatCuePadBottom,
                )
                .medhaClickable(onClick = onStart),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.challenge_your_mind),
                contentDescription = "Challenge your mind",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cuePulse
                        scaleY = cuePulse
                    },
            )
        }
    }
}

private fun LazyListItemInfo.cutFraction(viewportStart: Int, viewportEnd: Int): Float {
    val left = (viewportStart - offset).coerceAtLeast(0)
    val right = (offset + size - viewportEnd).coerceAtLeast(0)
    return max(left, right).toFloat() / size.coerceAtLeast(1)
}

private fun Modifier.wellMask(fadeStart: Boolean, fadeEnd: Boolean): Modifier {
    if (!fadeStart && !fadeEnd) return this
    return graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val fade = size.width * 0.12f
            val start = (fade / size.width).coerceIn(0.04f, 0.2f)
            val brush = Brush.horizontalGradient(
                0f to if (fadeStart) Color.Transparent else Color.White,
                start to Color.White,
                1f - start to Color.White,
                1f to if (fadeEnd) Color.Transparent else Color.White,
            )
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.DstIn)
            }
        }
}

private fun Modifier.catShine(progress: Float, enabled: Boolean): Modifier {
    if (!enabled) return this
    return drawWithContent {
        drawContent()
        val x = size.width * (progress * 1.75f - 0.4f)
        val band = size.width * 0.42f
        drawRect(
            brush = Brush.linearGradient(
                0f to Color.Transparent,
                0.42f to Color(0x66FFFFFF),
                0.5f to Color(0xAAFFFFFF),
                0.58f to Color(0x66FFFFFF),
                1f to Color.Transparent,
                start = Offset(x, -size.height * 0.2f),
                end = Offset(x + band, size.height * 1.2f),
            ),
            blendMode = BlendMode.SrcAtop,
        )
    }
}
