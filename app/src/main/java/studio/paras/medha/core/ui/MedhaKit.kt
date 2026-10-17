package studio.paras.medha.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import studio.paras.medha.R
import studio.paras.medha.core.design.Outfit

/**
 * MEDHA's reusable gold-and-blue pieces, drawn in code instead of PNGs.
 * Any text, number or state goes on top as Compose, so one piece serves every screen.
 */
object MedhaGold {
    val Hi = Color(0xFFFFF1C2)
    val Gold = Color(0xFFFFE08A)
    val Deep = Color(0xFFC98408)
    val Ink = Color(0xFF00193E)
    val Panel = Brush.verticalGradient(listOf(Color(0xF2132A56), Color(0xF2071531)))
    val Rim = Brush.verticalGradient(listOf(Color(0xFFFFF6B8), Color(0xFFF0C42A), Color(0xFFC98408)))
    val Button = Brush.verticalGradient(listOf(Color(0xFFFFE9A0), Color(0xFFF0B62A), Color(0xFFB87406)))
    val ButtonOff = Brush.verticalGradient(listOf(Color(0xFF55617A), Color(0xFF2E3850)))
    val Scrim = Color(0xB3050810)
    val PanelShape: Shape = RoundedCornerShape(18.dp)
    val PillShape: Shape = RoundedCornerShape(50)
}

/** Gold-rimmed blue panel. */
@Composable
fun GoldPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(MedhaGold.PanelShape)
            .background(MedhaGold.Panel)
            .border(2.dp, MedhaGold.Rim, MedhaGold.PanelShape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        if (title != null) {
            Text(
                title.uppercase(),
                fontFamily = Outfit,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 2.4.sp,
                color = MedhaGold.Gold,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        content()
    }
}

/** Pill-shaped gold button. */
@Composable
fun GoldButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 40.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .height(height)
            .clip(MedhaGold.PillShape)
            .background(if (enabled) MedhaGold.Button else MedhaGold.ButtonOff)
            .border(1.5.dp, if (enabled) MedhaGold.Rim else MedhaGold.ButtonOff, MedhaGold.PillShape)
            .medhaClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            leading?.invoke()
            Text(
                label,
                fontFamily = Outfit,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                color = if (enabled) Color(0xFF2A1700) else Color(0xFFB9C3D6),
                maxLines = 1,
            )
        }
    }
}

/** Small "coin + amount" chip, used for lifeline prices and rewards. */
@Composable
fun CoinPriceTag(
    amount: Int,
    modifier: Modifier = Modifier,
    gem: Boolean = false,
) {
    Row(
        modifier
            .clip(MedhaGold.PillShape)
            .background(Color(0xE6071531))
            .border(1.dp, MedhaGold.Rim, MedhaGold.PillShape)
            .padding(start = 3.dp, end = 6.dp, top = 1.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(if (gem) R.drawable.hud_gem else R.drawable.hud_coin),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(12.dp),
        )
        Text(
            amount.toString(),
            fontFamily = Outfit,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = MedhaGold.Hi,
            modifier = Modifier.padding(start = 3.dp),
        )
    }
}

/** Short message strip, e.g. "Not enough coins". */
@Composable
fun GoldToast(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .widthIn(max = 420.dp)
            .clip(MedhaGold.PillShape)
            .background(Color(0xF2071531))
            .border(1.5.dp, MedhaGold.Rim, MedhaGold.PillShape)
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        Text(
            text,
            fontFamily = Outfit,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MedhaGold.Hi,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Full-screen scrim that swallows taps and closes on an outside tap. */
@Composable
fun GoldScrim(
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(MedhaGold.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismiss?.invoke() },
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Swallows taps so a click inside a panel does not reach the scrim behind it. */
fun Modifier.blockTaps(): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {},
    )
}

/** Title, message and one or two buttons. */
@Composable
fun GoldDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    GoldScrim(onDismiss = null) {
        GoldPanel(
            title = title,
            modifier = Modifier
                .widthIn(min = 300.dp, max = 460.dp)
                .blockTaps(),
        ) {
            Text(
                body,
                fontFamily = Outfit,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = Color(0xFFD5E4F5),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (dismissLabel != null && onDismiss != null) {
                    GoldButton(dismissLabel, onDismiss, Modifier.weight(1f), enabled = true)
                }
                GoldButton(confirmLabel, onConfirm, Modifier.weight(1f))
            }
        }
    }
}

/** Thin gold progress bar. */
@Composable
fun GoldProgress(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(6.dp)
            .clip(MedhaGold.PillShape)
            .background(Color(0x55000000)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(MedhaGold.PillShape)
                .background(MedhaGold.Rim),
        )
    }
}
