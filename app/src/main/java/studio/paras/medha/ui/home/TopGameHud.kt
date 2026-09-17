package studio.paras.medha.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.Outfit

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
        modifier.height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        CurrencyChip(kind = CurrencyKind.COIN, value = formatCount(coins), onAdd = onAdd)
        CurrencyChip(kind = CurrencyKind.GEM, value = formatCount(gems), onAdd = onAdd)
        HudIcon(Icons.Outlined.Email, "Messages", onMessages, badge = true)
        HudIcon(Icons.Outlined.Group, "Friends", onFriends)
        HudIcon(Icons.Outlined.Settings, "Settings", onSettings)
    }
}

private enum class CurrencyKind { COIN, GEM }

@Composable
private fun CurrencyChip(kind: CurrencyKind, value: String, onAdd: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .height(36.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Color(0xE60B1018))
            .border(1.dp, Color(0x44C9A45A), shape)
            .padding(start = 5.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (kind == CurrencyKind.COIN) {
            GoldCoin(Modifier.size(22.dp))
        } else {
            GemIcon(Modifier.size(18.dp))
        }
        Text(
            value,
            fontFamily = Outfit,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFFF5F7FB),
            modifier = Modifier.padding(start = 5.dp).widthIn(min = 12.dp),
        )
        Text(
            "+",
            fontFamily = Outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFFF0C85A),
            modifier = Modifier
                .padding(start = 5.dp)
                .clickable(onClick = onAdd),
        )
    }
}

@Composable
private fun GoldCoin(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = Offset(r, r)
        drawCircle(Color(0xFF6A450C), radius = r, center = c)
        drawCircle(Color(0xFFD4A017), radius = r * 0.92f, center = Offset(r, r - r * 0.05f))
        drawCircle(Color(0xFFF3CC5C), radius = r * 0.78f, center = Offset(r, r - r * 0.06f))
        drawCircle(
            Color(0xFFB8860B),
            radius = r * 0.58f,
            center = Offset(r, r - r * 0.04f),
            style = Stroke(width = size.minDimension * 0.07f),
        )
        drawCircle(
            Color(0xFFFFF6D0).copy(alpha = 0.75f),
            radius = r * 0.16f,
            center = Offset(r - r * 0.28f, r - r * 0.32f),
        )
    }
}

@Composable
private fun GemIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val path = Path().apply {
            moveTo(cx, size.height * 0.08f)
            lineTo(size.width * 0.86f, cy)
            lineTo(cx, size.height * 0.92f)
            lineTo(size.width * 0.14f, cy)
            close()
        }
        drawPath(path, Color(0xFF2EC4FF))
        val shine = Path().apply {
            moveTo(cx, size.height * 0.08f)
            lineTo(size.width * 0.86f, cy)
            lineTo(cx, cy)
            close()
        }
        drawPath(shine, Color(0xFF8AE8FF).copy(alpha = 0.55f))
    }
}

@Composable
private fun HudIcon(icon: ImageVector, label: String, onClick: () -> Unit, badge: Boolean = false) {
    Box(contentAlignment = Alignment.TopEnd) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFFF5F7FB),
            modifier = Modifier
                .size(36.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xE60B1018))
                .border(1.dp, Color(0x55C9A45A), CircleShape)
                .clickable(onClick = onClick)
                .padding(7.dp),
        )
        if (badge) {
            Box(
                Modifier
                    .padding(top = 1.dp, end = 1.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935)),
            )
        }
    }
}
