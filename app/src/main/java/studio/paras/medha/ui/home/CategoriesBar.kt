package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import studio.paras.medha.R

private val CategoriesHeight = 100.dp
private val CategoriesPadStart = 16.dp
private val CategoriesPadEnd = 16.dp
private val CategoriesPadTop = 8.dp
private val CategoriesPadBottom = 0.dp

@Composable
fun CategoriesBar(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.categories_bar),
        contentDescription = "Categories",
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = CategoriesPadStart,
                end = CategoriesPadEnd,
                top = CategoriesPadTop,
                bottom = CategoriesPadBottom,
            )
            .requiredHeight(CategoriesHeight),
    )
}
