package studio.paras.medha.data.cloud

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import studio.paras.medha.data.cache.QuestionBankCache
import studio.paras.medha.data.repository.LocalQuestionRepository
import studio.paras.medha.data.repository.QuestionRepository
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
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default"),
) : QuestionRepository {

    private val cache = linkedMapOf<String, QuestionRecord>()

    override suspend fun loadSession(
        mode: GameMode,
        seenIds: List<String>,
        category: String?,
    ): Result<List<QuestionRecord>> {
        val ladder = PrizeLadder.forMode(mode)
        val poolSource = cachedPool(category).ifEmpty { localFallback() }
        poolSource.forEach { cache[it.id] = it }
        val pool = poolSource
        if (pool.size < ladder.size) {
            return Result.failure(IllegalStateException("Not enough questions in the chamber."))
        }
        val random = if (mode == GameMode.DAILY) {
            Random(LocalDate.now().toEpochDay())
        } else {
            Random.Default
        }
        val unseen = pool.filter { it.id !in seenIds }.ifEmpty { pool }
        return Result.success(pick(unseen, ladder.size, random))
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

    override fun replacement(currentId: String, usedIds: Set<String>, difficulty: Difficulty): QuestionRecord? {
        val pool = cache.values.filter { it.id != currentId && it.id !in usedIds }
        return pool.filter { it.difficulty == difficulty }.randomOrNull()
            ?: pool.randomOrNull()
            ?: cache.values.filter { it.id != currentId }.randomOrNull()
            ?: local.replacement(currentId, usedIds, difficulty)
    }

    private suspend fun fetch(category: String?): List<QuestionRecord> {
        val snap = if (!category.isNullOrBlank()) {
            db.collection("questions").whereEqualTo("category", category).get().await()
        } else {
            db.collection("questions").whereEqualTo("active", true).get().await()
        }
        return snap.documents.mapNotNull { it.toRecord() }
            .filter { it.active && it.language == "en" }
    }

    private fun cachedPool(category: String?): List<QuestionRecord> {
        val file = bankFile ?: return emptyList()
        val all = QuestionBankCache.readFile(file)
        return QuestionBankCache.forCategory(all, category)
    }

    private fun localFallback(): List<QuestionRecord> =
        studio.paras.medha.data.local.SampleQuestionBank.all.filter { it.active && it.language == "en" }

    private fun pick(pool: List<QuestionRecord>, count: Int, random: Random): List<QuestionRecord> {
        val used = mutableSetOf<String>()
        val out = mutableListOf<QuestionRecord>()
        repeat(count) { index ->
            val want = PrizeLadder.difficultyForIndex(index)
            val pick = pool.filter { it.id !in used && it.difficulty == want }.randomOrNull(random)
                ?: pool.filter { it.id !in used }.minByOrNull {
                    kotlin.math.abs(it.difficulty.ordinal - want.ordinal)
                }
            if (pick != null) {
                used += pick.id
                out += pick
            }
        }
        return out
    }

    private fun DocumentSnapshot.toRecord(): QuestionRecord? {
        val prompt = getString("prompt") ?: getString("question") ?: return null
        val correctRaw = (getString("correctAnswer") ?: getString("correct") ?: "A").uppercase()
        val correct = runCatching { AnswerLetter.valueOf(correctRaw.take(1)) }.getOrDefault(AnswerLetter.A)
        val difficulty = runCatching {
            Difficulty.valueOf((getString("difficulty") ?: "MEDIUM").uppercase())
        }.getOrDefault(Difficulty.MEDIUM)
        return QuestionRecord(
            id = id,
            prompt = prompt,
            optionA = getString("optionA").orEmpty(),
            optionB = getString("optionB").orEmpty(),
            optionC = getString("optionC").orEmpty(),
            optionD = getString("optionD").orEmpty(),
            correct = correct,
            category = getString("category").orEmpty(),
            tags = (get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            difficulty = difficulty,
            explanation = getString("explanation").orEmpty(),
            hints = (get("hints") as? List<*>)?.mapNotNull { it as? String }?.filter { it.isNotBlank() }
                ?: emptyList(),
            prizeLevel = (getLong("prizeLevel") ?: 0L).toInt(),
            language = getString("language") ?: "en",
            active = getBoolean("active") ?: true,
        )
    }
}
