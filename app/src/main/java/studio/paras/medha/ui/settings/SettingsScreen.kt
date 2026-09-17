package studio.paras.medha.ui.settings

import androidx.compose.runtime.Composable
import studio.paras.medha.core.ui.SettingsOverlay
import studio.paras.medha.domain.model.PlayerProfile

@Composable
fun SettingsScreen(
    profile: PlayerProfile,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleVoice: () -> Unit,
    onBack: () -> Unit,
) {
    SettingsOverlay(
        musicOn = profile.musicEnabled,
        sfxOn = profile.sfxEnabled,
        voiceOn = profile.voiceEnabled,
        onToggleMusic = onToggleMusic,
        onToggleSfx = onToggleSfx,
        onToggleVoice = onToggleVoice,
        onClose = onBack,
    )
}
