package studio.paras.medha.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, top = 12.dp),
            ) {
                PlayerProfileHud(
                    player = player,
                    onClick = onProfile,
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    HomeListNav(
                        selected = ui.dest,
                        onSelect = { dest ->
                            when (dest) {
                                LobbyDest.LEADERBOARD -> onLeaderboard()
                                LobbyDest.PROFILE -> onProfile()
                                LobbyDest.SETTINGS -> onSettings()
                                LobbyDest.HOME -> vm.select(LobbyDest.HOME)
                                else -> vm.select(dest)
                            }
                        },
                    )
                    ModePlayList(
                        onPlay = onPlay,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            CategoriesBar(onStart = { onPlay(GameMode.CLASSIC) })
        }
        TopGameHud(
            coins = player.coins,
            gems = player.gems,
            level = player.level,
            onAdd = { vm.open(LobbyOverlay.VAULT) },
            onFriends = { vm.open(LobbyOverlay.FRIENDS) },
            onMessages = { vm.open(LobbyOverlay.MESSAGES) },
            onSettings = onSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 14.dp),
        )
        LobbyOverlayHost(
            overlay = ui.overlay,
            catalog = ui.catalog,
            onDismiss = vm::dismissOverlay,
        )
    }
}
