package studio.paras.medha.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = MedhaColors.Teal,
    onPrimary = MedhaColors.Ink,
    secondary = MedhaColors.Copper,
    onSecondary = MedhaColors.Ink,
    background = MedhaColors.Ink,
    onBackground = MedhaColors.Ivory,
    surface = MedhaColors.Chamber,
    onSurface = MedhaColors.Ivory,
    error = MedhaColors.Wrong,
    onError = MedhaColors.Ivory,
    outline = MedhaColors.Line,
)

@Composable
fun MedhaTheme(
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = Scheme,
            typography = MedhaTypography,
            content = content,
        )
    }
}

fun Color.withAlpha(a: Float) = copy(alpha = a)
