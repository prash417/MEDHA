package studio.paras.medha.core.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

val LocalTapSound = staticCompositionLocalOf { {} }

fun Modifier.medhaClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val tap = LocalTapSound.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = source,
        indication = indication,
        onClick = {
            if (enabled) tap()
            onClick()
        },
    )
}
