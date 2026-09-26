package studio.paras.medha.ui.challenge

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.min
import studio.paras.medha.core.design.Cormorant
import studio.paras.medha.core.design.Outfit
import studio.paras.medha.core.ui.medhaClickable
import studio.paras.medha.core.ui.rememberAssetImage
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.AnswerOption
import studio.paras.medha.domain.model.GamePhase

private val GoldHi = Color(0xFFFFF1C2)
private val Ivory = Color(0xFFF3EBE0)
private val TimerInk = Color(0xFFFFE08A)
private val NoPad = PlatformTextStyle(includeFontPadding = false)
private val TightLine = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both,
)
private fun String.words(): List<String> =
    trim().split(Regex("\\s+")).filter { it.isNotBlank() }

private fun String.isLongPrompt(): Boolean = words().size > 12 || length > 80

private fun String.isLongOption(): Boolean = words().size > 7 || length > 38

@Composable
private fun typedLetters(text: String, paceMs: Long): String {
    var count by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        count = 0
        if (text.isEmpty()) return@LaunchedEffect
        val step = if (paceMs > 0L) {
            (paceMs / text.length.coerceAtLeast(1)).coerceIn(32L, 70L)
        } else {
            36L
        }
        for (i in text.indices) {
            delay(step)
            count = i + 1
        }
    }
    return text.take(count)
}

private val PlateAspect = 2172f / 724f
private val LockAspect = 1284f / 1225f

@Composable
fun ChallengePlayBoard(
    prompt: String,
    options: List<AnswerOption>,
    selected: AnswerLetter?,
    hidden: Set<AnswerLetter>,
    correct: AnswerLetter?,
    phase: GamePhase,
    optionsVisible: Int,
    questionPaceMs: Long = 0L,
    optionPaceMs: List<Long> = emptyList(),
    onSelect: (AnswerLetter) -> Unit,
    onLock: () -> Unit,
    onPresented: (Boolean) -> Unit = {},
    secondsLeft: Int? = null,
    timerTotal: Int? = null,
    unlimited: Boolean = false,
    entered: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var presented by remember(prompt) { mutableStateOf(false) }
    val clockReady = timerTotal == null || secondsLeft != null
    val canPick = presented && clockReady && (phase == GamePhase.QUESTION_DISPLAY || phase == GamePhase.CONFIRMING)
    val questionWords = remember(prompt) { prompt.words() }
    val questionBig = prompt.isLongPrompt()
    var questionCount by remember(prompt) { mutableIntStateOf(0) }
    LaunchedEffect(prompt, questionPaceMs) {
        questionCount = 0
        presented = false
        onPresented(false)
        if (prompt.isBlank() || questionPaceMs <= 0L) return@LaunchedEffect
        if (questionBig) {
            questionCount = questionWords.size
            delay(questionPaceMs.coerceIn(420L, 12_000L))
        } else {
            val step = (questionPaceMs / questionWords.size.coerceAtLeast(1)).coerceIn(40L, 480L)
            for (i in questionWords.indices) {
                delay(step)
                questionCount = i + 1
            }
        }
    }
    LaunchedEffect(optionPaceMs, options, prompt) {
        val lastPace = optionPaceMs.getOrNull(3) ?: return@LaunchedEffect
        if (prompt.isBlank() || lastPace <= 0L) return@LaunchedEffect
        val lastText = options.find { it.letter.ordinal == 3 }?.text.orEmpty()
        val wait = if (lastText.isLongOption()) 420L else lastPace
        delay(wait)
        presented = true
        onPresented(true)
    }
    val questionShown = if (questionBig) prompt else questionWords.take(questionCount).joinToString(" ")
    val canLock = selected != null && canPick
    val reveal = phase == GamePhase.ANSWER_REVEAL ||
        phase == GamePhase.CORRECT ||
        phase == GamePhase.WRONG ||
        phase == GamePhase.GAME_COMPLETE
    val letters = listOf(
        listOf(AnswerLetter.A, AnswerLetter.B),
        listOf(AnswerLetter.C, AnswerLetter.D),
    )
    val slide = with(LocalDensity.current) { 26.dp.toPx() }
    val lockPressed = phase == GamePhase.ANSWER_SELECTED ||
        phase == GamePhase.ANSWER_REVEAL ||
        phase == GamePhase.CORRECT ||
        phase == GamePhase.WRONG
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
        val qArt = rememberAssetImage("Question show.png", trim = true)
        val optArt = rememberAssetImage("Option A.png", trim = true)
        val lockArt = rememberAssetImage("Lock Answer.png", trim = true)
        val qAspect = (qArt.width.toFloat() / qArt.height.toFloat().coerceAtLeast(1f)).takeIf { it > 0.5f } ?: PlateAspect
        val optAspect = (optArt.width.toFloat() / optArt.height.toFloat().coerceAtLeast(1f)).takeIf { it > 0.5f } ?: PlateAspect
        val lockAspect = (lockArt.width.toFloat() / lockArt.height.toFloat().coerceAtLeast(1f)).takeIf { it > 0.5f } ?: LockAspect
        val gap = 6.dp
        val qHNat = maxWidth / qAspect
        val optHNat = ((maxWidth - gap) / 2f) / optAspect
        val lockHNat = min(maxWidth * 0.18f / lockAspect, 80.dp)
        val totalH = qHNat + optHNat * 2 + gap * 2
        val scale = (maxHeight / totalH).coerceAtMost(1f)
        val boardW = maxWidth * scale
        val qH = qHNat * scale * 0.86f
        val optH = optHNat * scale * 0.96f
        val lockH = lockHNat * scale
        Column(
            modifier = Modifier.width(boardW),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            QuestionPlate(
                prompt = questionShown,
                whole = questionBig && questionShown.isNotBlank(),
                secondsLeft = secondsLeft,
                unlimited = unlimited,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(qH)
                    .challengeArrive(entered, delayMs = 120, dy = slide),
            )
            Box(
                modifier = Modifier
                    .padding(top = 0.dp)
                    .offset(y = (-14).dp)
                    .fillMaxWidth()
                    .challengeArrive(entered, delayMs = 180, dy = slide),
                contentAlignment = Alignment.Center,
            ) {
                Column(Modifier.fillMaxWidth(0.96f)) {
                    letters.forEachIndexed { rowIndex, row ->
                        Row(
                            Modifier
                                .padding(top = if (rowIndex == 0) 0.dp else gap)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                        ) {
                            row.forEach { letter ->
                                val option = options.find { it.letter == letter }
                                val index = letter.ordinal
                                val isHidden = letter in hidden
                                val isSelected = selected == letter
                                val isCorrect = reveal && correct == letter
                                val isWrong = reveal && isSelected && correct != letter
                                val pace = optionPaceMs.getOrElse(index) { 0L }
                                val fullOption = if (isHidden) "" else option?.text.orEmpty()
                                val optionBig = fullOption.isLongOption()
                                val started = index < optionsVisible && !isHidden
                                val optionText = typedLetters(
                                    text = if (started) fullOption else "",
                                    paceMs = pace,
                                )
                                OptionPlate(
                                    letter = letter,
                                    text = optionText,
                                    whole = optionBig && started,
                                    selected = isSelected && !isWrong && !isCorrect,
                                    correct = isCorrect,
                                    wrong = isWrong,
                                    enabled = canPick && !isHidden,
                                    onClick = { onSelect(letter) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(optH),
                                )
                            }
                        }
                    }
                }
                Image(
                    bitmap = lockArt,
                    contentDescription = "Lock answer",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(lockH)
                        .choiceShine(lockPressed)
                        .medhaClickable(
                            enabled = canLock,
                            role = Role.Button,
                            onClick = onLock,
                        ),
                )
            }
        }
    }
}

@Composable
private fun QuestionPlate(
    prompt: String,
    whole: Boolean,
    secondsLeft: Int?,
    unlimited: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val art = rememberAssetImage("Question show.png", trim = true)
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val circle = min(maxWidth * 0.14f, maxHeight * 0.40f)
        Image(
            bitmap = art,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (prompt.isNotBlank()) {
            var shown by remember(prompt) { mutableStateOf(!whole) }
            LaunchedEffect(prompt, whole) { shown = true }
            val fade by animateFloatAsState(
                targetValue = if (shown) 1f else 0f,
                animationSpec = tween(if (whole) 420 else 0),
                label = "question-whole",
            )
            val size = when {
                prompt.length > 150 -> 14.sp
                prompt.length > 90 -> 16.sp
                else -> 20.sp
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(
                        start = maxWidth * 0.12f,
                        end = maxWidth * 0.12f,
                        top = if (whole) maxHeight * 0.38f else maxHeight * 0.46f,
                        bottom = if (whole) maxHeight * 0.16f else maxHeight * 0.26f,
                    )
                    .clipToBounds()
                    .graphicsLayer { alpha = if (whole) fade else 1f },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = prompt,
                    color = Ivory,
                    fontFamily = Cormorant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = size,
                    lineHeight = size * 1.15f,
                    textAlign = TextAlign.Center,
                    maxLines = if (whole) 4 else 2,
                    overflow = if (whole) TextOverflow.Clip else TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = NoPad,
                        lineHeightStyle = TightLine,
                    ),
                )
            }
        }
        PlateTimer(
            secondsLeft = secondsLeft,
            unlimited = unlimited,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = maxHeight * 0.055f)
                .size(circle),
        )
    }
}

@Composable
private fun OptionPlate(
    letter: AnswerLetter,
    text: String,
    whole: Boolean,
    selected: Boolean,
    correct: Boolean,
    wrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val file = when (letter) {
        AnswerLetter.A -> "Option A.png"
        AnswerLetter.B -> "Option B.png"
        AnswerLetter.C -> "Option C.png"
        AnswerLetter.D -> "Option D.png"
    }
    val art = rememberAssetImage(file, trim = true)
    val ink = when {
        correct -> Color(0xFFB8FFD4)
        wrong -> Color(0xFFFFC4B0)
        selected -> GoldHi
        else -> Ivory
    }
    BoxWithConstraints(
        modifier.medhaClickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        ),
    ) {
        Image(
            bitmap = art,
            contentDescription = letter.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .choiceShine(selected),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(
                    start = maxWidth * 0.32f,
                    end = maxWidth * 0.16f,
                    top = maxHeight * if (whole) 0.08f else 0.12f,
                    bottom = maxHeight * if (whole) 0.12f else 0.20f,
                )
                .height(maxHeight * if (whole) 0.56f else 0.34f)
                .clipToBounds(),
            contentAlignment = Alignment.TopStart,
        ) {
            val size = if (whole) 13.sp else 16.sp
            Text(
                text = text,
                color = ink,
                fontFamily = Outfit,
                fontWeight = FontWeight.SemiBold,
                fontSize = size,
                lineHeight = size * 1.15f,
                textAlign = TextAlign.Start,
                maxLines = if (whole) 3 else 2,
                overflow = if (whole) TextOverflow.Clip else TextOverflow.Ellipsis,
                style = TextStyle(
                    platformStyle = NoPad,
                    lineHeightStyle = TightLine,
                ),
            )
        }
    }
}

@Composable
private fun Modifier.choiceShine(on: Boolean): Modifier {
    if (!on) return this
    val shine = rememberInfiniteTransition(label = "choice-shine")
    val sweep by shine.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "choice-travel",
    )
    return graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            val x = size.width * (sweep * 1.8f - 0.4f)
            val reach = size.width * 0.62f
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent,
                    0.28f to Color(0x33FFE7A3),
                    0.5f to Color(0xD9FFF6D0),
                    0.72f to Color(0x33FFE7A3),
                    1f to Color.Transparent,
                    start = Offset(x, size.height),
                    end = Offset(x + reach, 0f),
                ),
                blendMode = BlendMode.SrcAtop,
            )
        }
}

@Composable
private fun PlateTimer(
    secondsLeft: Int?,
    unlimited: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (unlimited) {
        Image(
            bitmap = rememberAssetImage("Infinite.png", trim = true),
            contentDescription = "No time limit",
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
        return
    }
    if (secondsLeft == null) return
    val hot = secondsLeft <= 8
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text = secondsLeft.toString(),
            color = if (hot) Color(0xFFFFD27A) else TimerInk,
            fontFamily = Outfit,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            letterSpacing = 0.6.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = TextStyle(
                platformStyle = NoPad,
                lineHeightStyle = TightLine,
            ),
        )
    }
}
