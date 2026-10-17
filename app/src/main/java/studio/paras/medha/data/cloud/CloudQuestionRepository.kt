package studio.paras.medha.data.cloud

import studio.paras.medha.data.cache.QuestionBankCache
import studio.paras.medha.data.repository.LocalQuestionRepository
import studio.paras.medha.data.repository.QuestionRepository
import studio.paras.medha.data.repository.drawUnseen
import studio.paras.medha.data.repository.pickFresh
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.AnswerVerdict
import studio.paras.medha.domain.model.Difficulty
import studio.paras.medha.domain.model.GameMode
import studio.paras.medha.domain.model.PrizeLadder
import studio.paras.medha.domain.model.QuestionRecord
import java.time.LocalDate
import kotlin.random.Random

class CloudQuestionRepository(
    private val local: LocalQuestionRepository,
    private val bankFile: java.io.File? = null,
) : QuestionRepository {

    private val cache = linkedMapOf<String, QuestionRecord>()

    override suspend fun loadSession(
        mode: GameMode,
        seenIds: List<String>,
        category: String?,
    ): Result<studio.paras.medha.data.repository.SessionDraw> {
        val ladder = PrizeLadder.forMode(mode)
        val pool = categoryQuestions(category)
        if (pool.size < ladder.size) {
            return Result.failure(IllegalStateException("Not enough questions in the chamber."))
        }
        val random = if (mode == GameMode.DAILY) {
            Random(LocalDate.now().toEpochDay())
        } else {
            Random.Default
        }
        return Result.success(drawUnseen(pool, seenIds, ladder.size, random))
    }

    override suspend fun validate(questionId: String, selected: AnswerLetter): Result<AnswerVerdict> {
        val q = cache[questionId] ?: local.peek(questionId)
            ?: return Result.failure(IllegalStateException("That question is no longer in the chamber."))
        return Result.success(
            AnswerVerdict(
                correct = q.correct == selected,
                correctLetter = q.correct,
                selected = selected,
                explanation = q.explanation,
            ),
        )
    }

    override fun peek(questionId: String): QuestionRecord? = cache[questionId] ?: local.peek(questionId)

    override fun replacement(
        currentId: String,
        usedIds: Set<String>,
        difficulty: Difficulty,
        category: String?,
        seenIds: Set<String>,
    ): QuestionRecord? {
        val pool = categoryQuestions(category)
        return pickFresh(pool, usedIds + currentId, seenIds, difficulty)
            ?: local.replacement(currentId, usedIds, difficulty, category, seenIds)
    }

    private fun categoryQuestions(category: String?): List<QuestionRecord> {
        val fromCache = cache.values.filter { category.isNullOrBlank() || it.category == category }
        if (fromCache.isNotEmpty()) return fromCache
        val loaded = cachedPool(category).ifEmpty {
            localFallback().filter { category.isNullOrBlank() || it.category == category }
        }
        loaded.forEach { cache[it.id] = it }
        return loaded
    }

    private fun cachedPool(category: String?): List<QuestionRecord> {
        val file = bankFile ?: return emptyList()
        val all = QuestionBankCache.readFile(file)
        return QuestionBankCache.forCategory(all, category)
    }

    private fun localFallback(): List<QuestionRecord> =
        studio.paras.medha.data.local.SampleQuestionBank.all.filter { it.active && it.language == "en" }
}
