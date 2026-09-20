package studio.paras.medha.ui.navigation

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import studio.paras.medha.core.audio.MusicTrack
import studio.paras.medha.core.audio.Sfx
import studio.paras.medha.di.AppContainer
import studio.paras.medha.core.ui.ProvideHudControls
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.ui.SwipeBackController
import studio.paras.medha.ui.challenge.ChallengeScreen
import studio.paras.medha.ui.game.GameRoute
import studio.paras.medha.ui.game.GameViewModel
import studio.paras.medha.ui.home.HomeScreen
import studio.paras.medha.ui.leaderboard.LeaderboardScreen
import studio.paras.medha.ui.profile.ProfileScreen
import studio.paras.medha.ui.result.ResultScreen
import studio.paras.medha.ui.settings.SettingsScreen
import studio.paras.medha.ui.splash.SplashScreen

object Routes {
    const val HOME = "home"
    const val GAME = "game/{mode}"
    const val RESULT = "result"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val LEADERBOARD = "leaderboard"
    const val CHALLENGE = "challenge"
    fun game(mode: GameMode) = "game/${mode.id}"
}

/** Flip to false to skip the cinematic splash and go straight to home. */
private const val PlaySplashOnLaunch = true

@Composable
fun MedhaNav(
    container: AppContainer,
    profile: PlayerProfile,
    swipeBack: SwipeBackController,
    modifier: Modifier = Modifier,
) {
    val nav = rememberNavController()
    swipeBack.nav = nav
    val dest by nav.currentBackStackEntryAsState()
    val route = dest?.destination?.route
    val onHome = route == Routes.HOME
    nav.enableOnBackPressed(!onHome)
    LaunchedEffect(onHome, route) {
        swipeBack.route = route
        swipeBack.callback.isEnabled = onHome
    }
    val scope = rememberCoroutineScope()
    var bootDone by remember { mutableStateOf(!PlaySplashOnLaunch) }
    var overlay by remember { mutableStateOf(PlaySplashOnLaunch) }
    var overlayAlpha by remember { mutableFloatStateOf(1f) }
    val startDest = Routes.HOME
    val toggleMusic: () -> Unit = {
        scope.launch {
            container.playerStore.update { it.copy(musicEnabled = !it.musicEnabled) }
            val next = !profile.musicEnabled
            container.audio.musicEnabled = next
            if (next) container.audio.setMusic(MusicTrack.LOBBY) else container.audio.setMusic(null)
        }
        Unit
    }
    val toggleSfx: () -> Unit = {
        scope.launch {
            container.playerStore.update { it.copy(sfxEnabled = !it.sfxEnabled) }
            container.audio.sfxEnabled = !profile.sfxEnabled
        }
        Unit
    }
    val toggleVoice: () -> Unit = {
        scope.launch { container.playerStore.update { it.copy(voiceEnabled = !it.voiceEnabled) } }
        Unit
    }

    ProvideHudControls(
        musicOn = profile.musicEnabled,
        sfxOn = profile.sfxEnabled,
        voiceOn = profile.voiceEnabled,
        onToggleMusic = toggleMusic,
        onToggleSfx = toggleSfx,
        onToggleVoice = toggleVoice,
    ) {
    Box(modifier.fillMaxSize()) {
        if (bootDone) {
            NavHost(
                navController = nav,
                startDestination = startDest,
            ) {
                composable(Routes.HOME) {
                    LaunchedEffect(Unit) { container.audio.setMusic(MusicTrack.LOBBY) }
                    HomeScreen(
                        profile = profile,
                        onPlay = { mode, category ->
                            container.playCategory = category
                            container.audio.play(Sfx.TAP)
                            nav.navigate(Routes.game(mode))
                        },
                        onChallenge = {
                            container.audio.play(Sfx.TAP)
                            nav.navigate(Routes.CHALLENGE)
                        },
                        onProfile = { nav.navigate(Routes.PROFILE) },
                        onSettings = { nav.navigate(Routes.SETTINGS) },
                        onLeaderboard = { nav.navigate(Routes.LEADERBOARD) },
                    )
                }
                composable(
                    Routes.GAME,
                    arguments = listOf(navArgument("mode") { type = NavType.StringType }),
                ) { entry ->
                    val mode = GameMode.entries.find { it.id == entry.arguments?.getString("mode") } ?: GameMode.CLASSIC
                    val vm: GameViewModel = viewModel(
                        viewModelStoreOwner = entry,
                        factory = GameViewModel.Factory(mode, container),
                    )
                    GameRoute(
                        viewModel = vm,
                        onExitToResult = {
                            nav.navigate(Routes.RESULT) { popUpTo(Routes.HOME) }
                        },
                        onQuit = { vm.abandon() },
                    )
                }
                composable(Routes.RESULT) {
                    ResultScreen(
                        result = container.lastResult,
                        onHome = {
                            nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                            container.audio.setMusic(MusicTrack.LOBBY)
                        },
                        onReplay = {
                            val mode = container.lastResult?.mode ?: GameMode.CLASSIC
                            nav.navigate(Routes.game(mode)) { popUpTo(Routes.HOME) }
                        },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        profile = profile,
                        onToggleMusic = toggleMusic,
                        onToggleSfx = toggleSfx,
                        onToggleVoice = toggleVoice,
                        onBack = { nav.popBackStack() },
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(profile = profile, onBack = { nav.popBackStack() })
                }
                composable(Routes.LEADERBOARD) {
                    LeaderboardScreen(profile = profile, onBack = { nav.popBackStack() })
                }
                composable(Routes.CHALLENGE) {
                    ChallengeScreen(onBack = { nav.popBackStack() })
                }
            }
        }

        if (overlay) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = overlayAlpha },
            ) {
                LaunchedEffect(Unit) {
                    container.audio.setMusic(MusicTrack.LOBBY)
                }
                SplashScreen {
                    bootDone = true
                    scope.launch {
                        val t0 = SystemClock.uptimeMillis()
                        val dur = 700L
                        while (isActive) {
                            val t = ((SystemClock.uptimeMillis() - t0).toFloat() / dur).coerceIn(0f, 1f)
                            overlayAlpha = 1f - t
                            if (t >= 1f) break
                            delay(16)
                        }
                        overlay = false
                    }
                }
            }
        }
    }
    }
}


