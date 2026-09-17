package studio.paras.medha.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.paras.medha.core.design.MedhaColors
import studio.paras.medha.core.design.MedhaTypography
import studio.paras.medha.core.ui.EnergyButton
import studio.paras.medha.core.ui.MedhaMark
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.LifelineKind
import studio.paras.medha.domain.model.PrizeLadder
import studio.paras.medha.domain.model.PrizeRung

@Composable
fun SeatIntro(
    state: GameUiState,
    onTakeSeat: () -> Unit,
) {
    val ladder = PrizeLadder.forMode(state.mode)
    Row(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            MedhaMark(glow = 1.1f)
            Spacer(Modifier.height(12.dp))
            Text(state.mode.title.uppercase(), style = MedhaTypography.labelLarge, color = MedhaColors.Copper)
            Text(
                state.playerName.ifBlank { "GUEST" },
                style = MedhaTypography.displayMedium.copy(fontSize = 40.sp, letterSpacing = 2.sp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${state.total} QUESTIONS   ·   TOP ${ladder.last().label}",
                style = MedhaTypography.bodyMedium,
            )
            Spacer(Modifier.height(20.dp))
            EnergyButton("TAKE THE SEAT", onClick = onTakeSeat, modifier = Modifier.fillMaxWidth())
        }
        Column(
            Modifier
                .weight(0.85f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("LOCKS", style = MedhaTypography.labelSmall, color = MedhaColors.Copper)
            ladder.filter { it.isAnchor }.forEach { rung ->
                Text(rung.label, style = MedhaTypography.headlineLarge.copy(fontSize = 32.sp, color = MedhaColors.Ivory))
            }
            Text("Wrong answers drop to the last lock.", style = MedhaTypography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun QuestionSting(state: GameUiState) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "QUESTION ${state.question?.number ?: state.index + 1}",
                style = MedhaTypography.labelLarge,
                color = MedhaColors.Teal,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                state.playingFor?.label ?: "",
                style = MedhaTypography.displayMedium.copy(fontSize = 64.sp, letterSpacing = 2.sp),
                textAlign = TextAlign.Center,
            )
            Text("FOR THIS QUESTION", style = MedhaTypography.labelSmall, color = MedhaColors.Mist)
        }
    }
}

@Composable
fun LockSheet(
    letter: AnswerLetter,
    text: String,
    onChange: () -> Unit,
    onSeal: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MedhaColors.Ink.copy(alpha = 0.78f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MedhaColors.Chamber)
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            Text("SEAL THIS?", style = MedhaTypography.labelLarge, color = MedhaColors.Selected)
            Spacer(Modifier.height(10.dp))
            Text("Option ${letter.name}", style = MedhaTypography.labelSmall)
            Text(text, style = MedhaTypography.headlineLarge.copy(fontSize = 24.sp, lineHeight = 30.sp))
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnergyButton("CHANGE", onClick = onChange, filled = false, modifier = Modifier.weight(1f))
                EnergyButton("SEAL IT", onClick = onSeal, filled = true, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WalkAwaySheet(
    takeHome: PrizeRung,
    risking: PrizeRung?,
    dropTo: PrizeRung?,
    onStay: () -> Unit,
    onTake: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MedhaColors.Ink.copy(alpha = 0.82f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MedhaColors.Chamber)
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            Text("LEAVE WITH THIS?", style = MedhaTypography.labelLarge, color = MedhaColors.Copper)
            Spacer(Modifier.height(10.dp))
            Text(takeHome.label, style = MedhaTypography.displayMedium.copy(fontSize = 40.sp, letterSpacing = 1.sp))
            Spacer(Modifier.height(8.dp))
            Text(
                buildString {
                    append("Next step is ${risking?.label ?: "the summit"}.")
                    append(" A wrong answer drops you to ${dropTo?.label ?: "₹0"}.")
                },
                style = MedhaTypography.bodyMedium,
            )
            Spacer(Modifier.height(22.dp))
            EnergyButton("KEEP PLAYING", onClick = onStay, filled = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            EnergyButton("TAKE ${takeHome.label}", onClick = onTake, filled = false, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun WrongSheet(
    timedOut: Boolean,
    correctText: String,
    explanation: String,
    dropToLabel: String,
    onContinue: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MedhaColors.Ink.copy(alpha = 0.84f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MedhaColors.Chamber)
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            Text(
                if (timedOut) "TIME HAS CLOSED" else "THAT IS NOT THE ONE",
                style = MedhaTypography.labelLarge,
                color = MedhaColors.Wrong,
            )
            Spacer(Modifier.height(12.dp))
            Text("You take home", style = MedhaTypography.labelSmall)
            Text(dropToLabel, style = MedhaTypography.headlineLarge.copy(fontSize = 28.sp, color = MedhaColors.Copper))
            Spacer(Modifier.height(14.dp))
            Text("Correct answer", style = MedhaTypography.labelSmall)
            Text(correctText, style = MedhaTypography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text("Did you know?", style = MedhaTypography.labelSmall, color = MedhaColors.Copper)
            Text(explanation, style = MedhaTypography.bodyMedium.copy(color = MedhaColors.IvoryMute))
            Spacer(Modifier.height(22.dp))
            EnergyButton("SEE RESULT", onClick = onContinue, filled = true, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PrizeOverlay(
    mode: studio.paras.medha.domain.model.GameMode,
    current: Int,
    correctCount: Int,
    onClose: () -> Unit,
) {
    val ladder = PrizeLadder.forMode(mode)
    Box(
        Modifier
            .fillMaxSize()
            .background(MedhaColors.Ink.copy(alpha = 0.94f))
            .clickable(onClick = onClose)
            .statusBarsPadding()
            .padding(24.dp),
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("THE ASCENT", style = MedhaTypography.labelLarge, color = MedhaColors.Copper)
            Spacer(Modifier.height(16.dp))
            ladder.asReversed().forEach { rung ->
                val active = rung.index == current
                val passed = rung.index < correctCount
                val color = when {
                    active -> MedhaColors.Teal
                    passed -> MedhaColors.IvoryMute
                    rung.isAnchor -> MedhaColors.Copper
                    else -> MedhaColors.Mist.copy(alpha = 0.5f)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Text(
                        if (rung.isAnchor) "◆" else "·",
                        color = color,
                        modifier = Modifier.width(20.dp),
                        fontSize = 12.sp,
                    )
                    Text(
                        rung.label,
                        style = MedhaTypography.bodyLarge.copy(
                            color = color,
                            fontSize = if (active) 22.sp else 16.sp,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("◆  locked if you miss later", style = MedhaTypography.labelSmall)
            Spacer(Modifier.height(8.dp))
            Text("TAP TO CLOSE", style = MedhaTypography.labelSmall)
        }
    }
}

@Composable
fun PrizeSpine(
    mode: studio.paras.medha.domain.model.GameMode,
    current: Int,
    correctCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val ladder = PrizeLadder.forMode(mode)
    Column(
        modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .semantics { contentDescription = "Prize ladder" },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ladder.asReversed().forEach { rung ->
            val active = rung.index == current
            val passed = rung.index < correctCount
            val color = when {
                active -> MedhaColors.Teal
                passed -> MedhaColors.IvoryMute
                rung.isAnchor -> MedhaColors.Copper
                else -> MedhaColors.Mist.copy(alpha = 0.35f)
            }
            Box(
                Modifier
                    .size(if (active || rung.isAnchor) 8.dp else 5.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
fun TimerRing(secondsLeft: Int?, total: Int?, highStakes: Boolean) {
    if (secondsLeft == null || total == null || total == 0) {
        Text(
            if (highStakes) "NO CLOCK" else " ",
            style = MedhaTypography.labelSmall,
            color = MedhaColors.Mist,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.Center,
        )
        return
    }
    val progress by animateFloatAsState(
        targetValue = secondsLeft / total.toFloat(),
        animationSpec = tween(250),
        label = "timer",
    )
    val hot = secondsLeft <= 8
    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(MedhaColors.Veil, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx()))
            drawArc(
                color = if (hot) MedhaColors.Wrong else MedhaColors.Teal,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            secondsLeft.toString(),
            style = MedhaTypography.labelLarge.copy(
                color = if (hot) MedhaColors.Wrong else MedhaColors.Ivory,
                fontSize = 13.sp,
                letterSpacing = 0.sp,
            ),
        )
    }
}

@Composable
fun LifelinePad(
    state: GameUiState,
    onLifeline: (LifelineKind) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LifelineKind.entries.forEach { kind ->
            val used = kind in state.usedLifelines
            val label = when (kind) {
                LifelineKind.FIFTY_FIFTY -> "50:50"
                LifelineKind.AUDIENCE -> "PULSE"
                LifelineKind.EXPERT -> "ILA"
                LifelineKind.SWITCH -> "SHIFT"
            }
            val hint = when (kind) {
                LifelineKind.FIFTY_FIFTY -> "50:50"
                LifelineKind.AUDIENCE -> "Audience"
                LifelineKind.EXPERT -> "Expert"
                LifelineKind.SWITCH -> "Switch"
            }
            EnergyButton(
                label = label,
                onClick = { onLifeline(kind) },
                filled = !used,
                enabled = !used && state.canLifeline,
                accent = if (used) MedhaColors.Mist else MedhaColors.Teal,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = if (used) "$hint used" else hint },
            )
        }
    }
}

@Composable
fun AudienceMeter(bars: Map<AnswerLetter, Float>) {
    Column(Modifier.padding(bottom = 10.dp)) {
        Text("THE ROOM READS", style = MedhaTypography.labelSmall, color = MedhaColors.Copper)
        Spacer(Modifier.height(8.dp))
        AnswerLetter.entries.forEach { letter ->
            val v = bars[letter] ?: 0f
            val fill by animateFloatAsState(v, tween(700), label = "bar$letter")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                Text(letter.name, style = MedhaTypography.labelSmall, modifier = Modifier.width(16.dp), color = MedhaColors.Ivory)
                Canvas(
                    Modifier
                        .weight(1f)
                        .height(7.dp),
                ) {
                    drawLine(
                        MedhaColors.Veil,
                        Offset(0f, size.height / 2),
                        Offset(size.width, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        MedhaColors.Teal,
                        Offset(0f, size.height / 2),
                        Offset(size.width * fill, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round,
                    )
                }
                Text("${(v * 100).toInt()}%", style = MedhaTypography.labelSmall, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun ExpertCard(advice: studio.paras.medha.domain.model.ExpertAdvice) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(MedhaColors.Veil.copy(alpha = 0.55f))
            .padding(14.dp),
    ) {
        Text(advice.name.uppercase(), style = MedhaTypography.labelSmall, color = MedhaColors.Copper)
        Text(advice.title, style = MedhaTypography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Leans ${advice.letter.name}  ·  ${advice.confidence}% sure",
            style = MedhaTypography.bodyLarge.copy(color = MedhaColors.Teal, fontSize = 15.sp),
        )
        Text(advice.note, style = MedhaTypography.bodyMedium)
    }
}

@Composable
fun OverlayHost(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 6 }, exit = fadeOut()) {
        content()
    }
}
