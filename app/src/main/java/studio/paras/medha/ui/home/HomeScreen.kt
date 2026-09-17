package studio.paras.medha.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.PlayerProfile

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(
    profile: PlayerProfile,
    onPlay: (GameMode) -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onLeaderboard: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val player = profile.toLobbyPlayer()

    Box(Modifier.fillMaxSize()) {
        GameLobbyBackground(Modifier.fillMaxSize())
        Column(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
        ) {
            Column(Modifier.padding(start = 14.dp, top = 12.dp)) {
                PlayerProfileHud(
                    player = player,
                    onClick = onProfile,
                )
                HomeListNav(
                    onSelect = { dest ->
                        when (dest) {
                            LobbyDest.LEADERBOARD -> onLeaderboard()
                            LobbyDest.PROFILE -> onProfile()
                            LobbyDest.SETTINGS -> onSettings()
                            LobbyDest.HOME -> Unit
                            else -> vm.select(dest)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            CategoriesBar()
        }
        LobbyOverlayHost(
            overlay = ui.overlay,
            catalog = ui.catalog,
            onDismiss = vm::dismissOverlay,
        )
    }
}
