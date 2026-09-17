package studio.paras.medha.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.R
import studio.paras.medha.core.design.Outfit

private val HudHeight = 46.dp
private val ChipWidth = 118.dp
private val CountWidth = 40.dp
private val TileHeight = 36.dp
private val TileIcon = 16.dp
private val TokenSize = 28.dp
private val PlusSize = 22.dp
private val HudGap = 0.dp

@Composable
fun TopGameHud(
    coins: Int,
    gems: Int,
    onAdd: () -> Unit,
    onFriends: () -> Unit,
    onMessages: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.height(HudHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HudGap),
    ) {
        CurrencyChip(
            chip = R.drawable.hud_chip_gold,
            token = R.drawable.hud_coin,
            plus = R.drawable.hud_plus,
            value = formatCount(coins),
            valueColor = Color(0xFFFFF1C2),
            onAdd = onAdd,
        )
        CurrencyChip(
            chip = R.drawable.hud_chip_cyan,
            token = R.drawable.hud_gem,
            plus = R.drawable.hud_plus_cyan,
            value = formatCount(gems),
            valueColor = Color(0xFFB8F6FF),
            onAdd = onAdd,
        )
        HudTile(Icons.Filled.Email, "Messages", onMessages, R.drawable.hud_tile_mail, Color(0xFFFFE08A))
        HudTile(Icons.Filled.Person, "Friends", onFriends, R.drawable.hud_tile_friends, Color(0xFFB8F6FF))
        HudTile(Icons.Filled.Settings, "Settings", onSettings, R.drawable.hud_tile_settings, Color(0xFFE4C2FF))
    }
}

@Composable
private fun CurrencyChip(
    chip: Int,
    token: Int,
    plus: Int,
    value: String,
    valueColor: Color,
    onAdd: () -> Unit,
) {
    Box(
        Modifier
            .requiredWidth(ChipWidth)
            .requiredHeight(HudHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        Image(
            painter = painterResource(chip),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(token),
                contentDescription = null,
                modifier = Modifier.size(TokenSize),
            )
            Text(
                value,
                style = TextStyle(
                    fontFamily = Outfit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = valueColor,
                    shadow = Shadow(valueColor.copy(alpha = 0.45f), Offset.Zero, 8f),
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .padding(start = 2.dp, end = 4.dp)
                    .width(CountWidth),
            )
            Image(
                painter = painterResource(plus),
                contentDescription = "Add",
                modifier = Modifier
                    .size(PlusSize)
                    .clickable(onClick = onAdd),
            )
        }
    }
}

@Composable
private fun HudTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tile: Int,
    tint: Color,
) {
    Box(
        Modifier
            .size(TileHeight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(tile),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Icon(
            icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(TileIcon),
        )
    }
}
