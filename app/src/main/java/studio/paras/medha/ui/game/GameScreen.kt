package studio.paras.medha.ui.game

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import studio.paras.medha.core.design.LocalReducedMotion
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.ui.AnswerFacet
import studio.paras.medha.core.ui.ArenaScaffold
import studio.paras.medha.core.ui.EnergyButton
import studio.paras.medha.core.ui.GlowWell
import studio.paras.medha.core.ui.rememberArenaPulse
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.GamePhase
import studio.paras.medha.domain.model.LifelineKind
import studio.paras.medha.domain.model.PrizeLadder

@Composable
fun GameRoute(
    viewModel: GameViewModel,
    onExitToResult: () -> Unit,
    onQuit: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.GAME_COMPLETE) onExitToResult()
    }
    GameScreen(
        state = state,
        onSelect = viewModel::select,
        onSeal = viewModel::sealAnswer,
        onChange = viewModel::changeAnswer,
        onLifeline = viewModel::useLifeline,
        onContinueWrong = viewModel::continueAfterWrong,
        onToggleLadder = viewModel::toggleLadder,
        onWalkAway = viewModel::requestWalkAway,
        onStay = viewModel::cancelWalkAway,
        onTakePrize = viewModel::confirmWalkAway,
        onTakeSeat = viewModel::takeTheSeat,
        onQuit = onQuit,
        onDismissError = viewModel::dismissError,
    )
}

@Composable
fun GameScreen(
    state: GameUiState,
    onSelect: (AnswerLetter) -> Unit,
    onSeal: () -> Unit,
    onChange: () -> Unit,
    onLifeline: (LifelineKind) -> Unit,
    onContinueWrong: () -> Unit,
    onToggleLadder: () -> Unit,
    onWalkAway: () -> Unit,
    onStay: () -> Unit,
    onTakePrize: () -> Unit,
    onTakeSeat: () -> Unit,
    onQuit: () -> Unit,
    onDismissError: () -> Unit,
) {
    val reduced = LocalReducedMotion.current || state.reducedMotion
    val tense = state.phase == GamePhase.ANSWER_SELECTED || state.phase == GamePhase.CONFIRMING
    val haptics = LocalHapticFeedback.current
    var qEnter by remember { mutableFloatStateOf(if (reduced) 1f else 0f) }
    LaunchedEffect(state.question?.id, state.phase) {
        if (state.phase == GamePhase.QUESTION_DISPLAY || state.phase == GamePhase.CONFIRMING) {
            if (reduced) qEnter = 1f else {
                val t0 = SystemClock.uptimeMillis()
                while (isActive) {
                    qEnter = ((SystemClock.uptimeMillis() - t0) / 420f).coerceIn(0f, 1f)
                    if (qEnter >= 1f) break
                    delay(16)
                }
            }
        } else if (state.phase == GamePhase.QUESTION_STING) {
            qEnter = 0f
        }
    }
    LaunchedEffect(state.phase) {
        when (state.phase) {
            GamePhase.CONFIRMING, GamePhase.ANSWER_SELECTED, GamePhase.CORRECT, GamePhase.WRONG ->
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            else -> Unit
        }
    }

    val walk = state.walkAwayPrize
    ArenaScaffold(
        accent = if (tense || state.isHighStakes) MedhaColors.Copper else MedhaColors.Teal,
        onBack = { if (walk != null && state.phase != GamePhase.SEAT_INTRO) onWalkAway() else onQuit() },
    ) {
        if (state.phase == GamePhase.CORRECT && !reduced) {
            SuccessDust(Modifier.fillMaxSize())
        }
        when {
            state.phase == GamePhase.LOADING -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("PREPARING THE CHAMBER", style = MedhaTypography.labelLarge, color = MedhaColors.Teal)
                }
            }
            state.phase == GamePhase.IDLE && state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(state.error, style = MedhaTypography.bodyLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        EnergyButton("LEAVE", onClick = onQuit, filled = false)
                    }
                }
            }
            state.phase == GamePhase.SEAT_INTRO -> SeatIntro(state, onTakeSeat)
            state.phase == GamePhase.QUESTION_STING -> QuestionSting(state)
            else -> HotSeat(
                state = state,
                qEnter = qEnter,
                onSelect = onSelect,
                onLifeline = onLifeline,
                onToggleLadder = onToggleLadder,
                onWalkAway = onWalkAway,
                onQuit = onQuit,
            )
        }

        OverlayHost(state.ladderOpen) {
            PrizeOverlay(state.mode, state.index, state.correctCount, onToggleLadder)
        }
        OverlayHost(state.phase == GamePhase.CONFIRMING && state.selected != null) {
            val letter = state.selected ?: AnswerLetter.A
            LockSheet(
                letter = letter,
                text = state.question?.options?.find { it.letter == letter }?.text.orEmpty(),
                onChange = onChange,
                onSeal = onSeal,
            )
        }
        OverlayHost(state.phase == GamePhase.WALK_AWAY_CONFIRM) {
            val take = state.walkAwayPrize
            if (take != null) {
                WalkAwaySheet(
                    takeHome = take,
                    risking = state.nextPrize,
                    dropTo = state.dropTo,
                    onStay = onStay,
                    onTake = onTakePrize,
                )
            }
        }
        OverlayHost(state.phase == GamePhase.WRONG) {
            WrongSheet(
                timedOut = state.selected == null,
                correctText = state.question?.options?.find { it.letter == state.correct }?.text.orEmpty(),
                explanation = state.explanation.orEmpty(),
                dropToLabel = state.dropTo?.label ?: "₹0",
                onContinue = onContinueWrong,
            )
        }
        if (state.error != null && state.question != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismissError),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(state.error, style = MedhaTypography.bodyMedium, color = MedhaColors.Wrong, modifier = Modifier.padding(24.dp))
            }
        }
    }
}

@Composable
private fun HotSeat(
    state: GameUiState,
    qEnter: Float,
    onSelect: (AnswerLetter) -> Unit,
    onLifeline: (LifelineKind) -> Unit,
    onToggleLadder: () -> Unit,
    onWalkAway: () -> Unit,
    onQuit: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 12.dp, top = 64.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrizeRibbon(
            state = state,
            modifier = Modifier
                .fillMaxHeight()
                .width(150.dp),
            onClick = onToggleLadder,
        )
        Column(
            Modifier
                .weight(1.15f)
                .fillMaxHeight(),
        ) {
            GameTopBar(state, onWalkAway, onQuit, onToggleLadder)
            Spacer(Modifier.height(6.dp))
            GlowWell(modifier = Modifier.fillMaxWidth().weight(1f), accent = MedhaColors.Teal) {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        (state.question?.category ?: "").uppercase(),
                        style = MedhaTypography.labelSmall,
                        color = MedhaColors.Teal,
                    )
                    Text(
                        state.question?.prompt.orEmpty(),
                        style = MedhaTypography.headlineLarge.copy(fontSize = 24.sp, lineHeight = 32.sp),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .graphicsLayer {
                                alpha = qEnter
                                translationY = (1f - qEnter) * 12f
                            }
                            .semantics { contentDescription = state.question?.prompt.orEmpty() },
                    )
                    if (state.audience != null) {
                        Spacer(Modifier.height(8.dp))
                        AudienceMeter(state.audience)
                    }
                    if (state.expert != null) {
                        Spacer(Modifier.height(8.dp))
                        ExpertCard(state.expert)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LifelinePad(state = state, onLifeline = onLifeline)
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val letters = listOf(
                listOf(AnswerLetter.A, AnswerLetter.B),
                listOf(AnswerLetter.C, AnswerLetter.D),
            )
            letters.forEach { row ->
                Row(
                    Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { letter ->
                        val i = letter.ordinal
                        val option = state.question?.options?.find { it.letter == letter }
                        val hidden = letter in state.hidden
                        val shown = i < state.optionsVisible && !hidden
                        val isSelected = state.selected == letter
                        val revealPhases = setOf(GamePhase.ANSWER_REVEAL, GamePhase.CORRECT, GamePhase.WRONG, GamePhase.GAME_COMPLETE)
                        val isCorrect = state.correct == letter && state.phase in revealPhases
                        val isWrong = isSelected && state.phase == GamePhase.WRONG
                        val dimOthers = state.phase in setOf(GamePhase.CONFIRMING, GamePhase.ANSWER_SELECTED) && !isSelected
                        AnswerFacet(
                            letter = letter.name,
                            text = option?.text.orEmpty(),
                            visible = shown,
                            selected = isSelected && !isWrong && !isCorrect,
                            revealedCorrect = isCorrect,
                            revealedWrong = isWrong,
                            enabled = state.canAnswer && shown,
                            onClick = { onSelect(letter) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .alpha(if (dimOthers) 0.32f else 1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrizeRibbon(
    state: GameUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val ladder = PrizeLadder.forMode(state.mode)
    Column(
        modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End,
    ) {
        ladder.asReversed().forEach { rung ->
            val active = rung.index == state.index
            val passed = rung.index < state.correctCount
            val color = when {
                active -> Color(0xFF04110E)
                passed -> MedhaColors.IvoryMute
                rung.isAnchor -> MedhaColors.Copper
                else -> MedhaColors.Mist.copy(alpha = 0.5f)
            }
            Text(
                text = (if (rung.isAnchor) "◆ " else "") + rung.label,
                style = MedhaTypography.bodyMedium.copy(
                    color = if (active) Color(0xFF04110E) else color,
                    fontSize = if (active) 13.sp else 11.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) MedhaColors.Teal else Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun GameTopBar(
    state: GameUiState,
    onWalkAway: () -> Unit,
    onQuit: () -> Unit,
    onLadder: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val walk = state.walkAwayPrize
        if (walk != null) {
            EnergyButton("TAKE ${walk.label}", onClick = onWalkAway, filled = false, modifier = Modifier.height(44.dp))
        } else {
            Spacer(Modifier.width(8.dp))
            onQuit.let { }
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onLadder)) {
            Text(
                state.playingFor?.label ?: "—",
                style = MedhaTypography.titleLarge.copy(color = MedhaColors.Copper, fontSize = 22.sp),
            )
            val locked = state.lockedIn
            Text(
                if (locked != null) "LOCKED ${locked.label}" else "NO LOCK YET",
                style = MedhaTypography.labelSmall.copy(fontSize = 10.sp),
                color = MedhaColors.Mist,
            )
        }
        Spacer(Modifier.weight(1f))
        TimerRing(state.secondsLeft, state.timerTotal, state.isHighStakes)
    }
}

@Composable
private fun SuccessDust(modifier: Modifier = Modifier) {
    val phase = rememberArenaPulse()
    Canvas(modifier) {
        repeat(36) { i ->
            val x = (size.width * ((i * 17) % 100) / 100f + phase * 40) % size.width
            val y = size.height * 0.2f + ((i * 37) % 90) + phase * -50
            drawCircle(
                color = MedhaColors.Teal.copy(alpha = 0.22f),
                radius = 2.8f + (i % 3),
                center = Offset(x, y),
            )
        }
    }
}
