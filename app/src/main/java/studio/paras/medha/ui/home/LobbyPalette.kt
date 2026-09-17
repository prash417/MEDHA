package studio.paras.medha.ui.home

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal object LobbyPalette {
    val Bg0 = Color(0xFF050810)
    val Gold = Color(0xFFF0C85A)
    val GoldHi = Color(0xFFFFF3C4)
    val GoldDeep = Color(0xFF8A6420)
    val Blue = Color(0xFF3D8CFF)
    val Teal = Color(0xFF5EE6D0)
    val Amber = Color(0xFFE2C48A)
    val Magenta = Color(0xFF9D4EDD)
    val Cyan = Color(0xFF00D4E8)
    val Green = Color(0xFF1FA971)
    val White = Color(0xFFF5F7FB)
    val Mute = Color(0xFFB8C0CC)
    val Dim = Color(0xFF8A93A3)
    val Panel = Color(0xCC070B14)
    val PanelSolid = Color(0xE608111F)
}

internal fun LobbyAccent.color(): Color = when (this) {
    LobbyAccent.GOLD -> LobbyPalette.Gold
    LobbyAccent.BLUE -> LobbyPalette.Blue
    LobbyAccent.AMBER -> LobbyPalette.Amber
    LobbyAccent.TEAL -> LobbyPalette.Cyan
    LobbyAccent.MAGENTA -> LobbyPalette.Magenta
    LobbyAccent.GREEN -> LobbyPalette.Green
}

internal val LobbyRound = RoundedCornerShape(10.dp)
internal val LobbyRoundSm = RoundedCornerShape(8.dp)
internal val LobbyPill = RoundedCornerShape(20.dp)
