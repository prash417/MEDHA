package studio.paras.medha.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import studio.paras.medha.core.ui.medhaClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import studio.paras.medha.R
import androidx.compose.ui.res.painterResource
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.PlayerProfile

@Composable
fun HomeScreen(
    profile: PlayerProfile,
    pingMs: Int? = null,
    onPlay: (GameMode, String?) -> Unit,
    onChallenge: (String) -> Unit,
    onProfile: () -> Unit,
    onAddCoins: () -> Unit,
    onAddGems: () -> Unit,
    onMessages: () -> Unit,
    onSettings: () -> Unit,
    onLeaderboard: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val ui by vm.state.collectAsStateWithLifecycle()
    val player = profile.toLobbyPlayer()
    var showPickToast by remember { mutableStateOf(false) }
    LaunchedEffect(showPickToast) {
        if (!showPickToast) return@LaunchedEffect
        delay(2400)
        showPickToast = false
    }

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
                                LobbyDest.HOME -> vm.select(LobbyDest.HOME)
                                else -> vm.select(dest)
                            }
                        },
                    )
                    ModePlayList(
                        onPlay = { mode -> onPlay(mode, ui.selectedCategory) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            CategoriesBar(
                selectedId = ui.selectedCategory,
                onSelectCategory = vm::selectCategory,
                onStart = {
                    val category = ui.selectedCategory
                    if (category == null) {
                        if (!profile.doNotDisturb) showPickToast = true
                    } else {
                        onChallenge(category)
                    }
                },
            )
        }
        TopGameHud(
            coins = player.coins,
            gems = player.gems,
            level = player.level,
            onAddCoins = onAddCoins,
            onAddGems = onAddGems,
            onMessages = onMessages,
            onSettings = onSettings,
            pingMs = pingMs,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 14.dp),
        )
        LobbyOverlayHost(
            overlay = ui.overlay,
            catalog = ui.catalog,
            onDismiss = vm::dismissOverlay,
        )
        AnimatedVisibility(
            visible = showPickToast,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.Center),
        ) {
            Image(
                painter = painterResource(R.drawable.pick_any_cg_toast),
                contentDescription = "Pick any category first",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(180.dp)
                    .medhaClickable { showPickToast = false },
            )
        }
    }
}
