package studio.paras.medha.core.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object MedhaColors {
    val Ink = Color(0xFF07090C)
    val Chamber = Color(0xFF0C1218)
    val Veil = Color(0xFF151E27)
    val Line = Color(0x33C9956C)
    val Teal = Color(0xFF5EE6D0)
    val TealDim = Color(0xFF163833)
    val Copper = Color(0xFFC9956C)
    val CopperDeep = Color(0xFF8A5A38)
    val Ivory = Color(0xFFF3EBE0)
    val IvoryMute = Color(0xFFB7AFA4)
    val Mist = Color(0xFF7D8B96)
    val Selected = Color(0xFFE2C48A)
    val Correct = Color(0xFF3DDC97)
    val CorrectDim = Color(0xFF10291F)
    val Wrong = Color(0xFFE07A5F)
    val WrongDim = Color(0xFF3A1C16)
    val Glow = Color(0x665EE6D0)
}

val LocalReducedMotion = staticCompositionLocalOf { false }
