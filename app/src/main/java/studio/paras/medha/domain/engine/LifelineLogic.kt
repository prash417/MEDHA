package studio.paras.medha.domain.engine

import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.Difficulty
import studio.paras.medha.domain.model.ExpertAdvice
import studio.paras.medha.domain.model.LifelineKind
import kotlin.random.Random

object LifelineLogic {

    fun fiftyFifty(
        correct: AnswerLetter,
        alreadyHidden: Set<AnswerLetter>,
        random: Random = Random.Default,
    ): Set<AnswerLetter> {
        val wrong = AnswerLetter.entries.filter { it != correct && it !in alreadyHidden }.shuffled(random)
        return alreadyHidden + wrong.take(2)
    }

    fun audienceBars(
        correct: AnswerLetter,
        difficulty: Difficulty,
        hidden: Set<AnswerLetter>,
        random: Random = Random.Default,
    ): Map<AnswerLetter, Float> {
        val visible = AnswerLetter.entries.filter { it !in hidden }
        val correctBias = when (difficulty) {
            Difficulty.EASY -> 0.62f
            Difficulty.MEDIUM -> 0.48f
            Difficulty.HARD -> 0.38f
            Difficulty.EXPERT -> 0.31f
        }
        val noise = { random.nextFloat() * 0.08f }
        val raw = visible.associateWith { letter ->
            if (letter == correct) correctBias + noise() else (1f - correctBias) / (visible.size - 1).coerceAtLeast(1) + noise()
        }
        val sum = raw.values.sum().coerceAtLeast(0.001f)
        return AnswerLetter.entries.associateWith { letter ->
            if (letter in hidden) 0f else (raw[letter] ?: 0f) / sum
        }
    }

    fun expertAdvice(
        correct: AnswerLetter,
        difficulty: Difficulty,
        hidden: Set<AnswerLetter>,
        random: Random = Random.Default,
    ): ExpertAdvice {
        val visible = AnswerLetter.entries.filter { it !in hidden }
        val accuracy = when (difficulty) {
            Difficulty.EASY -> 0.92f
            Difficulty.MEDIUM -> 0.78f
            Difficulty.HARD -> 0.64f
            Difficulty.EXPERT -> 0.52f
        }
        val pick = if (random.nextFloat() < accuracy) correct else visible.filter { it != correct }.random(random)
        val confidence = when (difficulty) {
            Difficulty.EASY -> 78 + random.nextInt(16)
            Difficulty.MEDIUM -> 64 + random.nextInt(18)
            Difficulty.HARD -> 52 + random.nextInt(18)
            Difficulty.EXPERT -> 44 + random.nextInt(16)
        }
        val note = if (pick == correct) {
            "If I had to lock one in, it would be this."
        } else {
            "I am not certain — this is my best read of the field."
        }
        return ExpertAdvice(
            name = "Archivist Ila",
            title = "Keeper of the Chamber",
            letter = pick,
            confidence = confidence.coerceIn(40, 96),
            note = note,
        )
    }
}

data class XpPlace(val level: Int, val into: Int, val need: Int)

object XpRules {
    fun need(level: Int): Int = 20_000 * level.coerceAtLeast(1)

    fun place(xp: Int): XpPlace {
        var level = 1
        var remain = xp.coerceAtLeast(0)
        while (remain >= need(level)) {
            remain -= need(level)
            level++
        }
        return XpPlace(level, remain, need(level))
    }

    fun levelFor(xp: Int): Int = place(xp).level

    fun addedPercent(startXp: Int, gained: Int): Float {
        val level = place(startXp).level
        return gained * 100f / need(level).toFloat()
    }

    fun coinsFor(prize: Long, completed: Boolean): Int {
        val fromPrize = (prize / 20_000L).toInt().coerceAtLeast(if (prize > 0) 5 else 0)
        return fromPrize + if (completed) 40 else 0
    }
}
