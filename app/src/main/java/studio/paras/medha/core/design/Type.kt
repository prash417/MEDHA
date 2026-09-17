package studio.paras.medha.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import studio.paras.medha.R

@OptIn(ExperimentalTextApi::class)
private fun outfit(weight: FontWeight) = Font(
    resId = R.font.outfit,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun cormorant(weight: FontWeight) = Font(
    resId = R.font.cormorant_garamond,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight.coerceIn(300, 700)),
    ),
)

val Outfit = FontFamily(
    outfit(FontWeight.Light),
    outfit(FontWeight.Normal),
    outfit(FontWeight.Medium),
    outfit(FontWeight.SemiBold),
    outfit(FontWeight.Bold),
    outfit(FontWeight.ExtraBold),
)

val Cormorant = FontFamily(
    cormorant(FontWeight.Normal),
    cormorant(FontWeight.Medium),
    cormorant(FontWeight.SemiBold),
    cormorant(FontWeight.Bold),
)

val MedhaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.SemiBold,
        fontSize = 64.sp,
        letterSpacing = 10.sp,
        color = MedhaColors.Ivory,
    ),
    displayMedium = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        letterSpacing = 6.sp,
        color = MedhaColors.Ivory,
    ),
    headlineLarge = TextStyle(
        fontFamily = Cormorant,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        color = MedhaColors.Ivory,
    ),
    headlineMedium = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 3.sp,
        color = MedhaColors.Copper,
    ),
    titleLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = MedhaColors.Ivory,
    ),
    bodyLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        color = MedhaColors.Ivory,
    ),
    bodyMedium = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = MedhaColors.IvoryMute,
    ),
    labelLarge = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 2.2.sp,
        color = MedhaColors.Mist,
    ),
    labelSmall = TextStyle(
        fontFamily = Outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.8.sp,
        color = MedhaColors.Mist,
    ),
)
