package studio.paras.medha.ui.navigation

import android.os.SystemClock
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import studio.paras.medha.SwipeBackGuard
import studio.paras.medha.core.audio.MusicTrack
import studio.paras.medha.core.audio.Sfx
import studio.paras.medha.di.AppContainer
import studio.paras.medha.core.ui.LocalTapSound
import studio.paras.medha.core.ui.ProvideHudControls
import studio.paras.medha.domain.engine.LoginStreak
import studio.paras.medha.domain.engine.Missions
import studio.paras.medha.ui.profile.NamePrompt
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.GameOutcome
import studio.paras.medha.domain.model.GamePhase
import studio.paras.medha.domain.model.LifelineKind
import studio.paras.medha.domain.model.PlayerProfile
import studio.paras.medha.ui.SwipeBackController
import studio.paras.medha.ui.challenge.ChallengeScreen
import studio.paras.medha.ui.game.GameRoute
import studio.paras.medha.ui.game.GameViewModel
import studio.paras.medha.ui.home.HomeScreen
import studio.paras.medha.ui.leaderboard.LeaderboardScreen
import studio.paras.medha.ui.profile.ProfileScreen
import studio.paras.medha.ui.result.ResultScreen
import studio.paras.medha.ui.home.ArtFramePopup
import studio.paras.medha.ui.home.CurrencyShopPopup
import studio.paras.medha.ui.home.ShopRow
import studio.paras.medha.domain.engine.AdLedger
import studio.paras.medha.domain.engine.AdReward
import studio.paras.medha.domain.engine.GemExchange
import studio.paras.medha.ui.home.StreakPopup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import studio.paras.medha.ui.home.GameSettingsPopup
import studio.paras.medha.ui.settings.SettingsScreen
import studio.paras.medha.ui.splash.SplashScreen

object Routes {
    const val HOME = "home"
    const val GAME = "game/{mode}"
    const val RESULT = "result"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val LEADERBOARD = "leaderboard"
    const val CHALLENGE = "challenge/{category}"
    fun challenge(category: String) = "challenge/$category"
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
    val leavePlay: (GameViewModel) -> Unit = { vm ->
        vm.leaveSession()
        if (!nav.popBackStack(Routes.HOME, false)) {
            nav.navigate(Routes.HOME) { launchSingleTop = true }
        }
        container.audio.setMusic(MusicTrack.LOBBY)
    }
    fun openChallenge(category: String, mode: GameMode, awardXp: Boolean) {
        container.playMode = mode
        container.playCategory = category
        container.awardXp = awardXp
        nav.navigate(Routes.challenge(category)) {
            popUpTo(Routes.HOME)
        }
    }
    LaunchedEffect(onHome, route) {
        swipeBack.route = route
        swipeBack.callback.isEnabled = onHome
    }
    var settingsOpen by remember { mutableStateOf(false) }
    var streakOpen by remember { mutableStateOf(false) }
    var frameFile by remember { mutableStateOf<String?>(null) }
    var shopNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(shopNotice) {
        if (shopNotice != null) {
            delay(2_400)
            shopNotice = null
        }
    }
    val openSettings = { settingsOpen = true }
    val openCoins = { frameFile = "coins.png" }
    val openGems = { frameFile = "Dimonds.png" }
    val openMail = { if (!profile.doNotDisturb) frameFile = "Mail box.png" }
    val scope = rememberCoroutineScope()
    var bootDone by remember { mutableStateOf(!PlaySplashOnLaunch) }
    var overlay by remember { mutableStateOf(PlaySplashOnLaunch) }
    var overlayAlpha by remember { mutableFloatStateOf(1f) }
    swipeBack.splash = overlay
    val startDest = Routes.HOME
    val toggleSound: () -> Unit = {
        scope.launch {
            var next = true
            container.playerStore.update {
                next = !it.musicEnabled
                it.copy(
                    musicEnabled = next,
                    timerMusicEnabled = next,
                    sfxEnabled = next,
                    clockTicksEnabled = next,
                )
            }
            container.audio.musicEnabled = next
            container.audio.timerMusicEnabled = next
            container.audio.sfxEnabled = next
            container.audio.clockTicksEnabled = next
            container.audio.applyBackgroundMusic(next)
            if (!next) container.audio.dropTimerMusic()
        }
        Unit
    }
    val toggleVoice: () -> Unit = {
        scope.launch {
            var next = true
            container.playerStore.update {
                next = !it.voiceEnabled
                it.copy(voiceEnabled = next)
            }
            container.voice.enabled = next
            if (!next) container.voice.hush()
        }
        Unit
    }
    val toggleHaptics: () -> Unit = {
        scope.launch {
            var next = true
            container.playerStore.update {
                next = !it.hapticsEnabled
                it.copy(hapticsEnabled = next)
            }
            container.audio.hapticsEnabled = next
        }
        Unit
    }
    val toggleDisturb: () -> Unit = {
        scope.launch {
            container.playerStore.update { it.copy(doNotDisturb = !it.doNotDisturb) }
        }
        Unit
    }
    val togglePing: () -> Unit = {
        scope.launch {
            container.playerStore.update { it.copy(showPing = !it.showPing) }
        }
        Unit
    }
    var pingMs by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(profile.doNotDisturb) { SwipeBackGuard.quiet = profile.doNotDisturb }
    LaunchedEffect(profile.showPing) {
        if (!profile.showPing) {
            pingMs = null
            return@LaunchedEffect
        }
        while (isActive) {
            pingMs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val started = SystemClock.elapsedRealtime()
                runCatching {
                    val link = java.net.URL("https://www.gstatic.com/generate_204").openConnection() as java.net.HttpURLConnection
                    link.connectTimeout = 2500
                    link.readTimeout = 2500
                    link.instanceFollowRedirects = false
                    link.connect()
                    link.responseCode
                    link.disconnect()
                }
                (SystemClock.elapsedRealtime() - started).toInt().coerceIn(1, 9999)
            }
            delay(4000)
        }
    }

    CompositionLocalProvider(LocalTapSound provides { container.audio.play(Sfx.TAP) }) {
    ProvideHudControls(
        musicOn = profile.musicEnabled,
        sfxOn = profile.sfxEnabled,
        voiceOn = profile.voiceEnabled,
        onToggleMusic = toggleSound,
        onToggleSfx = toggleSound,
        onToggleVoice = toggleVoice,
    ) {
    Box(modifier.fillMaxSize()) {
        if (bootDone) {
            NavHost(
                navController = nav,
                startDestination = startDest,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable(Routes.HOME) {
                    LaunchedEffect(Unit) {
                        container.audio.setMusic(MusicTrack.LOBBY)
                        val today = LocalDate.now().toEpochDay()
                        container.playerStore.update { LoginStreak.ensure(it, today) }
                    }
                    HomeScreen(
                        profile = profile,
                        pingMs = pingMs,
                        onPlay = { mode, _ ->
                            // Every mode, Daily included, plays on the Challenge screen.
                            val pool = if (mode == GameMode.DAILY) GameViewModel.DailyCategory else mode.id
                            openChallenge(pool, mode, awardXp = mode != GameMode.PRACTICE)
                        },
                        onChallenge = { category ->
                            openChallenge(category, GameMode.CLASSIC, awardXp = true)
                        },
                        onProfile = { nav.navigate(Routes.PROFILE) },
                        onAddCoins = openCoins,
                        onAddGems = openGems,
                        onMessages = openMail,
                        onSettings = openSettings,
                        onLeaderboard = { nav.navigate(Routes.LEADERBOARD) },
                        onStreak = {
                            scope.launch {
                                val today = LocalDate.now().toEpochDay()
                                container.playerStore.update { LoginStreak.ensure(it, today) }
                                streakOpen = true
                            }
                        },
                        streakMask = LoginStreak.week(profile, LocalDate.now().toEpochDay()).mask,
                        onClaimMission = { id ->
                            scope.launch {
                                container.playerStore.update { Missions.claim(it, id) ?: it }
                                container.audio.play(Sfx.PRIZE)
                            }
                        },
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
                        onLeave = { leavePlay(vm) },
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
                            val pool = if (mode == GameMode.DAILY) GameViewModel.DailyCategory else mode.id
                            openChallenge(pool, mode, awardXp = mode != GameMode.PRACTICE)
                        },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        profile = profile,
                        onToggleMusic = toggleSound,
                        onToggleSfx = toggleSound,
                        onToggleVoice = toggleVoice,
                        onBack = { nav.popBackStack() },
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        profile = profile,
                        onBack = { nav.popBackStack() },
                        onRename = { name ->
                            scope.launch { container.playerStore.update { it.copy(displayName = name) } }
                        },
                        onAvatar = { id ->
                            scope.launch { container.playerStore.update { it.copy(avatarId = id) } }
                        },
                    )
                }
                composable(Routes.LEADERBOARD) {
                    LeaderboardScreen(profile = profile, cloud = container.cloud, onBack = { nav.popBackStack() })
                }
                composable(
                    Routes.CHALLENGE,
                    arguments = listOf(navArgument("category") { type = NavType.StringType }),
                ) { entry ->
                    val category = entry.arguments?.getString("category").orEmpty()
                    val playMode = container.playMode
                    val vm: GameViewModel = viewModel(
                        viewModelStoreOwner = entry,
                        key = "medha-challenge-${playMode.id}-$category",
                        factory = remember(playMode, category) {
                            GameViewModel.Factory(
                                playMode,
                                container,
                                stepBackOnTimeout = true,
                                category = category,
                            )
                        },
                    )
                    val play by vm.state.collectAsStateWithLifecycle()
                    LaunchedEffect(play.phase) {
                        if (play.phase == GamePhase.SEAT_INTRO) vm.takeTheSeat()
                    }
                    ChallengeScreen(
                        pingMs = pingMs,
                        onBack = { leavePlay(vm) },
                        coins = profile.coins,
                        gems = profile.gems,
                        onAddCoins = openCoins,
                        onAddGems = openGems,
                        onSettings = openSettings,
                        categoryId = category,
                        questionNumber = (play.index + 1).coerceAtLeast(1),
                        totalQuestions = play.total.coerceAtLeast(1),
                        prompt = play.promptShown,
                        options = play.question?.options.orEmpty(),
                        selected = play.selected,
                        hidden = play.hidden,
                        correct = play.correct,
                        phase = play.phase,
                        optionsVisible = play.optionsVisible,
                        questionPaceMs = play.questionPaceMs,
                        optionPaceMs = play.optionPaceMs,
                        hintText = play.hintText,
                        hintUsed = play.hintUsed,
                        secondsLeft = play.secondsLeft,
                        timerTotal = play.timerTotal,
                        unlimited = playMode == GameMode.PRACTICE,
                        onSelect = { letter ->
                            val now = vm.state.value
                            if (now.phase == GamePhase.CONFIRMING && now.selected != letter) {
                                vm.changeAnswer()
                            }
                            vm.select(letter)
                        },
                        onLock = vm::sealAnswer,
                        onHint = vm::useHint,
                        onFiftyFifty = { vm.useLifeline(LifelineKind.FIFTY_FIFTY) },
                        fiftyUsed = LifelineKind.FIFTY_FIFTY in play.usedLifelines,
                        onExtraTime = { vm.useLifeline(LifelineKind.EXTRA_TIME) },
                        extraTimeUsed = LifelineKind.EXTRA_TIME in play.usedLifelines,
                        onSwitch = { vm.useLifeline(LifelineKind.SWITCH) },
                        switchUsed = LifelineKind.SWITCH in play.usedLifelines,
                        onProtect = { vm.useLifeline(LifelineKind.PROTECT) },
                        protectUsed = LifelineKind.PROTECT in play.usedLifelines,
                        onDare = vm::offerDare,
                        onConfirmDare = vm::confirmDare,
                        onDismissDare = vm::dismissDare,
                        dareUsed = LifelineKind.DARE in play.usedLifelines,
                        dareOffer = play.dareOffer,
                        dareArmed = play.dareArmed,
                        canSpendLifeline = play.canSpendLifeline,
                        onDismissHint = vm::dismissHint,
                        onQuit = vm::quitKeepingReward,
                        onHome = { leavePlay(vm) },
                        onReplay = {
                            vm.leaveSession()
                            openChallenge(category, playMode, awardXp = container.awardXp)
                        },
                        clearedRungs = play.correctCount,
                        trackXp = playMode != GameMode.PRACTICE,
                        earnedXp = play.earnedXp,
                        xpAwards = play.xpAwards,
                        startXp = play.startXp,
                        resultPrize = play.resultPrize,
                        hintCost = play.hintCost,
                        fiftyCost = play.lifelineCost(LifelineKind.FIFTY_FIFTY),
                        switchCost = play.lifelineCost(LifelineKind.SWITCH),
                        extraTimeCost = play.lifelineCost(LifelineKind.EXTRA_TIME),
                        protectCost = play.lifelineCost(LifelineKind.PROTECT),
                        notice = play.notice,
                        onNoticeShown = vm::clearNotice,
                        errorText = play.error,
                        onRetry = vm::restartFresh,
                        outcome = play.outcome,
                        coinsEarned = play.coinsEarned,
                    )
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
        if (bootDone && !overlay && !profile.onboardingComplete) {
            NamePrompt(
                current = profile.displayName,
                title = "Welcome to MEDHA",
                onSave = { name ->
                    scope.launch {
                        container.playerStore.update { it.copy(displayName = name, onboardingComplete = true) }
                    }
                },
                onDismiss = {
                    scope.launch { container.playerStore.update { it.copy(onboardingComplete = true) } }
                },
            )
        }
        if (settingsOpen) {
            GameSettingsPopup(
                soundOn = profile.musicEnabled,
                voiceOn = profile.voiceEnabled,
                hapticsOn = profile.hapticsEnabled,
                disturbOn = profile.doNotDisturb,
                pingOn = profile.showPing,
                onToggleSound = toggleSound,
                onToggleVoice = toggleVoice,
                onToggleHaptics = toggleHaptics,
                onToggleDisturb = toggleDisturb,
                onTogglePing = togglePing,
                onDismiss = { settingsOpen = false },
            )
        }
        if (streakOpen) {
            StreakPopup(
                profile = profile,
                onDayChanged = { day ->
                    scope.launch {
                        container.playerStore.update { LoginStreak.ensure(it, day) }
                    }
                },
                onClaim = {
                    scope.launch {
                        container.playerStore.update { current ->
                            val now = System.currentTimeMillis()
                            val claimDay = Instant.ofEpochMilli(now)
                                .atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                            LoginStreak.claim(current, claimDay, now) ?: current
                        }
                    }
                },
                onDismiss = { streakOpen = false },
            )
        }
        frameFile?.let { file ->
            when (file) {
                "coins.png", "Dimonds.png" -> {
                    val today = LocalDate.now().toEpochDay()
                    val gemShop = file == "Dimonds.png"
                    val rewards = if (gemShop) {
                        listOf(AdReward.GEMS, AdReward.GEM_BONUS)
                    } else {
                        listOf(AdReward.COINS, AdReward.COIN_BONUS)
                    }
                    val adRows = rewards.map { reward ->
                        val left = AdLedger.left(profile, reward, today)
                        ShopRow(
                            title = if (reward.coins > 0) "${reward.coins} Coins" else "${reward.gems} Diamonds",
                            detail = "${reward.title}  ·  $left left today",
                            action = if (left > 0) "WATCH" else "DONE",
                            enabled = left > 0,
                            onClick = {
                                container.ads.showRewarded(
                                    onReward = {
                                        scope.launch {
                                            container.playerStore.update {
                                                AdLedger.grant(it, reward, LocalDate.now().toEpochDay()) ?: it
                                            }
                                            container.audio.play(Sfx.PRIZE)
                                            shopNotice = if (reward.coins > 0) {
                                                "+${reward.coins} coins added"
                                            } else {
                                                "+${reward.gems} diamonds added"
                                            }
                                        }
                                    },
                                    onUnavailable = {
                                        shopNotice = "No ad ready yet. Check your internet and try again."
                                    },
                                )
                            },
                        )
                    }
                    val tradeRow = ShopRow(
                        title = "${GemExchange.Coins} Coins",
                        detail = "Trade ${GemExchange.Gems} diamonds",
                        action = "TRADE",
                        enabled = profile.gems >= GemExchange.Gems,
                        onClick = {
                            scope.launch {
                                container.playerStore.update { GemExchange.trade(it) ?: it }
                                shopNotice = "+${GemExchange.Coins} coins added"
                            }
                        },
                    )
                    CurrencyShopPopup(
                        fileName = file,
                        rows = if (gemShop) adRows else adRows + tradeRow,
                        notice = shopNotice,
                        onDismiss = {
                            frameFile = null
                            shopNotice = null
                        },
                    )
                }
                else -> ArtFramePopup(fileName = file, onDismiss = { frameFile = null })
            }
        }
    }
    }
    }
}


