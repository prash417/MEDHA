package studio.paras.medha.ui.challenge

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import studio.paras.medha.core.design.LocalReducedMotion
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.CtaShape
import studio.paras.medha.core.ui.medhaClickable
import studio.paras.medha.core.ui.rememberAssetImage
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.AnswerOption
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.GamePhase
import studio.paras.medha.ui.home.CurrencyHudChips
import studio.paras.medha.ui.home.HudAssetButton
import studio.paras.medha.ui.home.HudPing

private val QuestionBarHeight = 50.dp
private val QuestionBarAspect = 2106f / 385f
private val CategoryBarAspect = 2060f / 524f
private val OpRailWidth = 142.dp
private val LeftHudInset = 16.dp
private val RightHudInset = 28.dp
private val LadderShinePhase = floatArrayOf(
    0.00f, 0.47f, 0.18f, 0.71f, 0.33f,
    0.86f, 0.12f, 0.59f, 0.27f, 0.78f,
    0.41f, 0.93f, 0.08f, 0.64f, 0.52f,
)
private val LadderBars = floatArrayOf(
    0.9137f, 0.8609f, 0.8094f, 0.7547f, 0.6993f,
    0.6446f, 0.5899f, 0.5355f, 0.4818f, 0.4280f,
    0.3746f, 0.3176f, 0.2632f, 0.2091f, 0.1554f,
)
private data class ChallengeOpSpec(
    val file: String,
    val label: String,
    val used: Boolean,
    val available: Boolean,
    val onClick: () -> Unit,
)

private fun categoryBannerFile(id: String?): String = when (id) {
    "classic" -> "banner_classic.png"
    "quick" -> "banner_quick_play.png"
    "practice" -> "banner_practice.png"
    "sci" -> "Science.png"
    "his" -> "History.png"
    "spo" -> "Sports.png"
    "mov" -> "Movies.png"
    "tech" -> "Technology.png"
    "fusion" -> "Fusion.png"
    "nexus" -> "Nexus.png"
    else -> "General.png"
}
@Composable
internal fun Modifier.challengeArrive(
    shown: Boolean,
    delayMs: Int = 0,
    dx: Float = 0f,
    dy: Float = 0f,
    duration: Int = 480,
    scaleFrom: Float = 0.88f,
): Modifier {
    val calm = LocalReducedMotion.current
    val t by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(if (calm) 0 else duration, if (calm) 0 else delayMs, FastOutSlowInEasing),
        label = "challenge-arrive",
    )
    return this.graphicsLayer {
        alpha = t
        translationX = dx * (1f - t)
        translationY = dy * (1f - t)
        val s = scaleFrom + (1f - scaleFrom) * t
        scaleX = s
        scaleY = s
    }
}

private val QuestionInk = Color(0xFF1A1206)
private val QuestionGoldFill = Brush.verticalGradient(
    listOf(Color(0xFFFFF8E8), Color(0xFFFFE08A), Color(0xFFC99418)),
)
private val QuestionPad = PlatformTextStyle(includeFontPadding = false)
private val QuestionLine = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both,
)

@Composable
fun ChallengeScreen(
    onBack: () -> Unit,
    coins: Int,
    gems: Int,
    onAddCoins: () -> Unit = {},
    onAddGems: () -> Unit = {},
    onSettings: () -> Unit = {},
    pingMs: Int? = null,
    categoryId: String? = null,
    questionNumber: Int = 1,
    totalQuestions: Int = GameMode.CLASSIC.questionCount,
    prompt: String = "",
    options: List<AnswerOption> = emptyList(),
    selected: AnswerLetter? = null,
    hidden: Set<AnswerLetter> = emptySet(),
    correct: AnswerLetter? = null,
    phase: GamePhase = GamePhase.QUESTION_DISPLAY,
    optionsVisible: Int = 0,
    questionPaceMs: Long = 0L,
    optionPaceMs: List<Long> = emptyList(),
    hintText: String? = null,
    hintUsed: Boolean = false,
    secondsLeft: Int? = null,
    timerTotal: Int? = null,
    unlimited: Boolean = false,
    onSelect: (AnswerLetter) -> Unit = {},
    onLock: () -> Unit = {},
    onHint: () -> Unit = {},
    onFiftyFifty: () -> Unit = {},
    fiftyUsed: Boolean = false,
    onExtraTime: () -> Unit = {},
    extraTimeUsed: Boolean = false,
    onSwitch: () -> Unit = {},
    switchUsed: Boolean = false,
    onProtect: () -> Unit = {},
    protectUsed: Boolean = false,
    onDare: () -> Unit = {},
    onConfirmDare: () -> Unit = {},
    onDismissDare: () -> Unit = {},
    dareUsed: Boolean = false,
    dareOffer: Boolean = false,
    dareArmed: Boolean = false,
    canSpendLifeline: Boolean = true,
    onDismissHint: () -> Unit = {},
    onHome: () -> Unit = {},
    onReplay: () -> Unit = {},
    clearedRungs: Int = 0,
    trackXp: Boolean = true,
    earnedXp: Int = 0,
    xpAwards: List<studio.paras.medha.ui.game.XpAward> = emptyList(),
    startXp: Int = 0,
    resultPrize: String? = null,
) {
    BackHandler(onBack = onBack)
    val total = totalQuestions.coerceAtLeast(1)
    val current = questionNumber.coerceIn(1, total)
    var entered by remember { mutableStateOf(false) }
    var presented by remember(prompt) { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val slide = with(LocalDensity.current) { 36.dp.toPx() }
    Box(Modifier.fillMaxSize()) {
        Image(
            bitmap = rememberAssetImage("Challenge your mind page.png"),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .challengeArrive(entered, duration = 280, scaleFrom = 1f),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = LeftHudInset, top = 4.dp)
                .size(64.dp)
                .challengeArrive(entered, delayMs = 40, dx = -slide)
                .medhaClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = rememberAssetImage("Back.png", trim = true),
                contentDescription = "Back",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(42.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
                .padding(start = LeftHudInset, top = 66.dp, bottom = 52.dp)
                .width(OpRailWidth),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            val loaded = presented && (timerTotal == null || secondsLeft != null)
            val thinking = loaded && (phase == GamePhase.QUESTION_DISPLAY || phase == GamePhase.CONFIRMING)
            val ops = listOf(
                ChallengeOpSpec("Hint op1.png", "Hint", hintUsed, loaded && phase == GamePhase.QUESTION_DISPLAY, onHint),
                ChallengeOpSpec("Dare op2.png", "Dare", dareUsed, thinking, onDare),
                ChallengeOpSpec("remove two op3.png", "Remove two", fiftyUsed, thinking, onFiftyFifty),
                ChallengeOpSpec("Switch op4.png", "Switch", switchUsed, thinking, onSwitch),
                ChallengeOpSpec("Extra Time op5.png", "Extra time", extraTimeUsed, thinking && timerTotal != null, onExtraTime),
                ChallengeOpSpec("Protect op6.png", "Protect", protectUsed, thinking, onProtect),
            )
            ops.forEachIndexed { i, op ->
                val enabled = !op.used && op.available && canSpendLifeline
                Image(
                    bitmap = rememberAssetImage(op.file, trim = true),
                    contentDescription = op.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .challengeArrive(entered, delayMs = 70 + i * 45, dx = -slide)
                        .medhaClickable(
                            enabled = enabled,
                            role = Role.Button,
                            onClick = op.onClick,
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(4f)
                .padding(top = 10.dp, end = 12.dp)
                .challengeArrive(entered, delayMs = 50, dy = -slide),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CurrencyHudChips(
                coins = coins,
                gems = gems,
                onAddCoins = onAddCoins,
                onAddGems = onAddGems,
            )
            if (pingMs != null) HudPing(pingMs)
            HudAssetButton("Settings.png", "Settings", onSettings)
        }
        ChallengeLadder(
            cleared = clearedRungs,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(start = 36.dp, end = RightHudInset, top = 46.dp, bottom = 44.dp)
                .challengeArrive(entered, delayMs = 90, dx = slide),
        )
        if (phase != GamePhase.GAME_COMPLETE) {
            ChallengePlayBoard(
                prompt = prompt,
                options = options,
                selected = selected,
                hidden = hidden,
                correct = correct,
                phase = phase,
                optionsVisible = optionsVisible,
                questionPaceMs = questionPaceMs,
                optionPaceMs = optionPaceMs,
                entered = entered,
                onSelect = onSelect,
                onLock = onLock,
                onPresented = { presented = it },
                secondsLeft = secondsLeft,
                timerTotal = timerTotal,
                unlimited = unlimited,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(start = 152.dp, end = 132.dp, top = 48.dp, bottom = 36.dp),
            )
        }
        QuestionProgressBar(
            current = clearedRungs,
            total = totalQuestions,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = LeftHudInset, bottom = 2.dp)
                .challengeArrive(entered, delayMs = 160, dy = slide),
        )
        Image(
            bitmap = rememberAssetImage(categoryBannerFile(categoryId), trim = true),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 2.dp)
                .height(QuestionBarHeight)
                .width(QuestionBarHeight * CategoryBarAspect)
                .challengeArrive(entered, delayMs = 190, dy = slide),
        )
        if (!hintText.isNullOrBlank()) {
            HintPopup(text = hintText, onDismiss = onDismissHint)
        }
        if (dareOffer) {
            val skipLine = if (current + 2 > total) {
                "Correct: skip one and win the game."
            } else {
                "Correct: skip one question — go to question ${current + 2}."
            }
            val failLine = if (current <= 3) {
                "Wrong: go back three — game over."
            } else {
                "Wrong: go back three to question ${current - 3}."
            }
            DarePopup(
                skipLine = skipLine,
                failLine = failLine,
                riskLine = "This question: 15 seconds. No hint, 50-50, or extra time.",
                onConfirm = onConfirmDare,
                onDismiss = onDismissDare,
            )
        }
        if (phase == GamePhase.GAME_COMPLETE) {
            TimeUpPopup(
                title = "GAME OVER",
                earnedXp = earnedXp,
                answered = clearedRungs,
                total = totalQuestions,
                startXp = startXp,
                trackXp = trackXp,
                onHome = onHome,
                onReplay = onReplay,
            )
        }
    }
}

@Composable
private fun ChallengeLadder(
    cleared: Int,
    modifier: Modifier = Modifier,
) {
    val ladder = rememberAssetImage("15 question.png", trim = true)
    val aspect = ladder.width.toFloat() / ladder.height.toFloat()
    val shine = rememberInfiniteTransition(label = "ladder-shine")
    val sweep by shine.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ladder-travel",
    )
    val lit = cleared.coerceIn(0, LadderBars.size)
    val newest by animateFloatAsState(
        targetValue = lit.toFloat(),
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "rung-shine",
    )
    Box(modifier.aspectRatio(aspect)) {
        Image(
            bitmap = ladder,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .ladderShine(lit, newest, sweep),
        )
    }
}

@Composable
private fun Modifier.ladderShine(lit: Int, newest: Float, sweep: Float): Modifier {
    if (lit <= 0) return this
    return graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            val barH = size.height * 0.052f
            val left = size.width * 0.15f
            val barW = size.width * 0.70f
            for (i in 0 until lit) {
                val local = (sweep + LadderShinePhase[i]) % 1f
                val y = size.height * LadderBars[i]
                val top = y - barH * 0.50f
                val x = left + barW * (local * 1.8f - 0.4f)
                val reach = barW * 0.62f
                val box = numberBox(left, top, barW, barH)
                clipPath(box) {
                    drawRect(
                        brush = Brush.linearGradient(
                            0f to Color.Transparent,
                            0.28f to Color(0x33FFE7A3),
                            0.5f to Color(0x99FFE7A3),
                            0.72f to Color(0x33FFE7A3),
                            1f to Color.Transparent,
                            start = Offset(x, top + barH),
                            end = Offset(x + reach, top),
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barW, barH),
                        blendMode = BlendMode.SrcAtop,
                    )
                }
            }
        }
}

private fun numberBox(left: Float, top: Float, width: Float, height: Float): Path {
    val tip = height * 0.42f
    val mid = top + height / 2f
    return Path().apply {
        moveTo(left + tip, top)
        lineTo(left + width - tip, top)
        lineTo(left + width, mid)
        lineTo(left + width - tip, top + height)
        lineTo(left + tip, top + height)
        lineTo(left, mid)
        close()
    }
}

@Composable
private fun QuestionProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(QuestionBarHeight)
            .width(QuestionBarHeight * QuestionBarAspect),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = rememberAssetImage("Question.png", trim = true),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 30.dp),
        ) {
            GoldHudText(
                text = "QUESTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.4.sp,
                strokeWidth = 3.6f,
            )
            GoldHudText(
                text = "  $current",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp,
                strokeWidth = 5.4f,
            )
            GoldHudText(
                text = "  /  $total",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                strokeWidth = 4.2f,
            )
        }
    }
}

@Composable
private fun GoldHudText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    letterSpacing: TextUnit,
    strokeWidth: Float,
    modifier: Modifier = Modifier,
) {
    val style = TextStyle(
        fontFamily = Outfit,
        fontWeight = fontWeight,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        platformStyle = QuestionPad,
        lineHeightStyle = QuestionLine,
    )
    Box(modifier) {
        Text(
            text,
            style = style.copy(
                color = QuestionInk,
                shadow = Shadow(Color(0xAA120C04), Offset(0f, 1.5f), 6f),
                drawStyle = Stroke(width = strokeWidth, join = StrokeJoin.Round),
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        Text(
            text,
            style = style.copy(
                brush = QuestionGoldFill,
                shadow = Shadow(Color(0x66FFD56A), Offset.Zero, 8f),
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun HintPopup(
    text: String,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(text) {
        delay(10_000)
        onDismiss()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99070A10))
            .medhaClickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 80.dp)
                .fillMaxWidth()
                .clip(CtaShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xF21A2740), Color(0xF2081018)),
                    ),
                )
                .border(1.8.dp, Color(0xFFE8C86A), CtaShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .padding(horizontal = 32.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "HINT",
                color = Color(0xFFFFF1C2),
                fontFamily = Outfit,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 3.sp,
            )
            Text(
                text,
                color = Color(0xFFF3EBE0),
                fontFamily = Outfit,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun DarePopup(
    skipLine: String,
    failLine: String,
    riskLine: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC070A10)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 72.dp)
                .fillMaxWidth()
                .clip(CtaShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xF21A2740), Color(0xF2081018)),
                    ),
                )
                .border(1.8.dp, Color(0xFFE8C86A), CtaShape)
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "DARE",
                color = Color(0xFFFFF1C2),
                fontFamily = Outfit,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 3.sp,
            )
            Text(
                skipLine,
                color = Color(0xFFF3EBE0),
                fontFamily = Outfit,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                failLine,
                color = Color(0xFFE8C86A),
                fontFamily = Outfit,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                riskLine,
                color = Color(0xFFC4B8A8),
                fontFamily = Outfit,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GoldPopupButton("NOT NOW", onDismiss, Modifier.weight(1f))
                GoldPopupButton("DARE", onConfirm, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeUpPopup(
    title: String,
    earnedXp: Int,
    answered: Int,
    total: Int,
    startXp: Int,
    trackXp: Boolean,
    onHome: () -> Unit,
    onReplay: () -> Unit,
) {
    var run by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { run = true }
    val play by animateFloatAsState(
        targetValue = if (run) 1f else 0f,
        animationSpec = tween(1800, easing = FastOutSlowInEasing),
        label = "over-play",
    )
    val counted = (earnedXp * play).toInt()
    val shown = studio.paras.medha.domain.engine.XpRules.place(startXp + counted)
    val bar = (shown.into.toFloat() / shown.need.toFloat()).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(8f)
            .background(Color(0xCC070A10))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.62f),
        ) {
            val art = rememberAssetImage("game over.png", trim = true)
            val aspect = art.width.toFloat() / art.height.toFloat().coerceAtLeast(1f)
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect),
            ) {
                    Image(
                        bitmap = art,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Text(
                        title,
                        color = Color(0xFFFFE08A),
                        fontFamily = Outfit,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.4.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = maxHeight * 0.42f),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = maxHeight * 0.60f)
                            .fillMaxWidth(0.52f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "QUESTION  $answered  /  $total",
                            color = Color(0xFFFFE08A),
                            fontFamily = Outfit,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                        )
                        if (trackXp) {
                            Box(
                                Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CtaShape)
                                    .background(Color(0xFF101820)),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(bar)
                                        .background(Color(0xFFFFE08A)),
                                )
                            }
                            Text(
                                "+$counted XP",
                                color = Color(0xFFFFE08A),
                                fontFamily = Outfit,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    bitmap = rememberAssetImage("game over home.png", trim = true),
                    contentDescription = "Home",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .medhaClickable(role = Role.Button, onClick = onHome),
                )
                Image(
                    bitmap = rememberAssetImage("game over replay.png", trim = true),
                    contentDescription = "Replay",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .medhaClickable(role = Role.Button, onClick = onReplay),
                )
            }
        }
    }
}

@Composable
private fun GoldPopupButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CtaShape)
            .background(Color(0xE6181A12))
            .border(1.4.dp, Color(0xFFE8C86A), CtaShape)
            .medhaClickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color(0xFFFFF1C2),
            fontFamily = Outfit,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            letterSpacing = 1.6.sp,
        )
    }
}
