package studio.paras.medha.data.cache

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import studio.paras.medha.data.cloud.HostBoard
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.AnswerOption
import studio.paras.medha.domain.model.Difficulty
import studio.paras.medha.domain.model.QuestionRecord
import java.io.File

object QuestionBankCache {
    val pools = listOf(
        "gk", "sci", "his", "spo", "mov", "tech", "fusion", "nexus",
        "classic", "quick", "practice",
    )

    fun file(context: Context): File = File(context.filesDir, "question-bank.json")

    fun read(context: Context): List<QuestionRecord> = readFile(file(context))

    fun readFile(file: File): List<QuestionRecord> {
        if (!file.exists() || file.length() <= 2L) return emptyList()
        return runCatching { decode(file.readText()) }.getOrDefault(emptyList())
    }

    fun write(context: Context, questions: List<QuestionRecord>) {
        writeFile(file(context), questions)
    }

    fun writeFile(file: File, questions: List<QuestionRecord>) {
        file.writeText(encode(questions))
    }

    fun merge(file: File, incoming: List<QuestionRecord>) {
        if (incoming.isEmpty()) return
        val current = readFile(file).associateBy { it.id }.toMutableMap()
        incoming.forEach { current[it.id] = it }
        writeFile(file, current.values.toList())
    }

    fun forCategory(all: List<QuestionRecord>, category: String?): List<QuestionRecord> =
        if (category.isNullOrBlank()) all else all.filter { it.category == category }

    private fun encode(questions: List<QuestionRecord>): String {
        val array = JSONArray()
        questions.forEach { q ->
            array.put(
                JSONObject()
                    .put("id", q.id)
                    .put("prompt", q.prompt)
                    .put("optionA", q.optionA)
                    .put("optionB", q.optionB)
                    .put("optionC", q.optionC)
                    .put("optionD", q.optionD)
                    .put("correct", q.correct.name)
                    .put("category", q.category)
                    .put("tags", JSONArray(q.tags))
                    .put("difficulty", q.difficulty.name)
                    .put("explanation", q.explanation)
                    .put("hints", JSONArray(q.hints))
                    .put("prizeLevel", q.prizeLevel)
                    .put("language", q.language)
                    .put("active", q.active),
            )
        }
        return array.toString()
    }

    private fun decode(raw: String): List<QuestionRecord> {
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val tags = o.optJSONArray("tags")
                val hints = o.optJSONArray("hints")
                add(
                    QuestionRecord(
                        id = o.getString("id"),
                        prompt = o.getString("prompt"),
                        optionA = o.optString("optionA"),
                        optionB = o.optString("optionB"),
                        optionC = o.optString("optionC"),
                        optionD = o.optString("optionD"),
                        correct = runCatching { AnswerLetter.valueOf(o.optString("correct", "A").take(1)) }
                            .getOrDefault(AnswerLetter.A),
                        category = o.optString("category"),
                        tags = jsonStrings(tags),
                        difficulty = runCatching { Difficulty.valueOf(o.optString("difficulty", "MEDIUM")) }
                            .getOrDefault(Difficulty.MEDIUM),
                        explanation = o.optString("explanation"),
                        hints = jsonStrings(hints),
                        prizeLevel = o.optInt("prizeLevel"),
                        language = o.optString("language", "en"),
                        active = o.optBoolean("active", true),
                    ),
                )
            }
        }
    }

    private fun jsonStrings(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val value = array.optString(i)
                if (value.isNotBlank()) add(value)
            }
        }
    }
}

class DevicePackCache(context: Context) {
    private val app = context.applicationContext
    private val voiceDir = File(app.filesDir, "host-voice").also { it.mkdirs() }
    private val db = FirebaseFirestore.getInstance(FirebaseApp.getInstance(), "default")
    private val auth = FirebaseAuth.getInstance()

    suspend fun sync(onProgress: (line: String, detail: String, fraction: Float) -> Unit) {
        copyQuestions(onProgress)
        unpackVoice(onProgress)
    }

    private fun copyQuestions(onProgress: (String, String, Float) -> Unit) {
        val dest = QuestionBankCache.file(app)
        val stamp = File(app.filesDir, "question-bank.bundled")
        if (stamp.exists() && dest.exists() && dest.length() > 1_000L) {
            onProgress("Downloading questions", mbPair(dest.length(), dest.length()), 0.36f)
            return
        }
        val total = app.assets.openFd("offline/questions.bank").use { it.length }.coerceAtLeast(1L)
        var got = 0L
        app.assets.open("offline/questions.bank").use { input ->
            dest.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    got += read
                    onProgress(
                        "Downloading questions",
                        mbPair(got, total),
                        0.08f + 0.28f * (got.toFloat() / total),
                    )
                }
            }
        }
        stamp.writeText("ok")
    }

    private fun unpackVoice(onProgress: (String, String, Float) -> Unit) {
        val marker = File(voiceDir, ".unpacked")
        if (marker.exists()) {
            val bytes = voiceBytes()
            onProgress("Downloading voice pack", mbPair(bytes, bytes), 0.96f)
            return
        }
        val total = app.assets.openFd("offline/voice-packs.zip").use { it.length }.coerceAtLeast(1L)
        var got = 0L
        java.util.zip.ZipInputStream(app.assets.open("offline/voice-packs.zip")).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val out = File(voiceDir, File(entry.name).name)
                    out.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = zip.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            got += read
                        }
                    }
                    onProgress(
                        "Downloading voice pack",
                        mbPair(got, total),
                        0.40f + 0.56f * (got.toFloat() / total).coerceAtMost(1f),
                    )
                }
                zip.closeEntry()
            }
        }
        marker.writeText("ok")
        val bytes = voiceBytes()
        onProgress("Downloading voice pack", mbPair(bytes, bytes), 0.96f)
    }

    private suspend fun fetchQuestions(
        onProgress: (String, String, Float) -> Unit,
    ): List<QuestionRecord> {
        val cached = QuestionBankCache.read(app)
        if (cached.size >= 5000) {
            val bytes = QuestionBankCache.file(app).length()
            onProgress("Downloading questions", mbPair(bytes, bytes), 0.36f)
            return cached
        }
        val all = mutableListOf<QuestionRecord>()
        var bytes = 0L
        QuestionBankCache.pools.forEachIndexed { index, category ->
            val snap = runCatching {
                db.collection("questions").whereEqualTo("category", category).get().await()
            }.getOrNull()
            snap?.documents?.mapNotNull { doc ->
                val prompt = doc.getString("prompt") ?: doc.getString("question") ?: return@mapNotNull null
                val correctRaw = (doc.getString("correctAnswer") ?: doc.getString("correct") ?: "A").uppercase()
                QuestionRecord(
                    id = doc.id,
                    prompt = prompt,
                    optionA = doc.getString("optionA").orEmpty(),
                    optionB = doc.getString("optionB").orEmpty(),
                    optionC = doc.getString("optionC").orEmpty(),
                    optionD = doc.getString("optionD").orEmpty(),
                    correct = runCatching { AnswerLetter.valueOf(correctRaw.take(1)) }.getOrDefault(AnswerLetter.A),
                    category = doc.getString("category").orEmpty(),
                    tags = (doc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    difficulty = runCatching {
                        Difficulty.valueOf((doc.getString("difficulty") ?: "MEDIUM").uppercase())
                    }.getOrDefault(Difficulty.MEDIUM),
                    explanation = doc.getString("explanation").orEmpty(),
                    hints = (doc.get("hints") as? List<*>)?.mapNotNull { it as? String }?.filter { it.isNotBlank() }
                        ?: emptyList(),
                    prizeLevel = (doc.getLong("prizeLevel") ?: 0L).toInt(),
                    language = doc.getString("language") ?: "en",
                    active = doc.getBoolean("active") ?: true,
                )
            }?.filter { it.active && it.language == "en" }?.let { batch ->
                all += batch
                bytes += batch.sumOf { questionBytes(it) }
            }
            val fraction = 0.08f + 0.28f * ((index + 1f) / QuestionBankCache.pools.size)
            val seen = index + 1
            val projected = if (bytes == 0L) 0L else bytes * QuestionBankCache.pools.size / seen
            onProgress("Downloading questions", mbPair(bytes, projected), fraction)
        }
        return all.ifEmpty { cached }
    }

    private suspend fun fetchVoice(
        questions: List<QuestionRecord>,
        onProgress: (String, String, Float) -> Unit,
    ) {
        val scripts = buildList {
            addAll(HostBoard.sharedLines())
            questions.forEach { q ->
                val options = listOf(
                    AnswerOption(AnswerLetter.A, q.optionA),
                    AnswerOption(AnswerLetter.B, q.optionB),
                    AnswerOption(AnswerLetter.C, q.optionC),
                    AnswerOption(AnswerLetter.D, q.optionD),
                )
                add(HostBoard.boardScript(q.prompt, options))
                add(HostBoard.wrongScript(q.correct.name, options.first { it.letter == q.correct }.text))
            }
        }.distinct()
        val missing = scripts.filter { script ->
            val file = File(voiceDir, "${HostBoard.cacheKey(script)}.mp3")
            !file.exists() || file.length() <= 0L
        }
        val total = scripts.size.coerceAtLeast(1)
        var got = total - missing.size
        val have = (total - missing.size).coerceAtLeast(0)
        val onDisk = voiceBytes()
        val projected = when {
            missing.isEmpty() -> onDisk
            have == 0 -> onDisk
            else -> onDisk * total / have
        }
        onProgress("Downloading voice pack", mbPair(onDisk, projected), 0.40f + 0.56f * (have.toFloat() / total))
        if (missing.isEmpty()) return
        val gate = Semaphore(4)
        val storage = FirebaseStorage.getInstance().reference
        coroutineScope {
            missing.chunked(24).forEach { chunk ->
                chunk.map { script ->
                    async {
                        gate.withPermit {
                            val file = File(voiceDir, "${HostBoard.cacheKey(script)}.mp3")
                            runCatching {
                                storage.child("voice/${file.name}").getFile(file).await()
                            }
                            if (file.exists() && file.length() <= 0L) file.delete()
                        }
                    }
                }.awaitAll()
                got = (got + chunk.size).coerceAtMost(total)
                val fraction = 0.40f + 0.56f * (got.toFloat() / total)
                val haveNow = got
                val onDiskNow = voiceBytes()
                val projectedNow = if (haveNow <= 0) onDiskNow else onDiskNow * total / haveNow
                onProgress("Downloading voice pack", mbPair(onDiskNow, projectedNow), fraction)
            }
        }
    }

    private fun voiceBytes(): Long =
        voiceDir.listFiles()?.sumOf { if (it.isFile) it.length() else 0L } ?: 0L

    private fun questionBytes(q: QuestionRecord): Long =
        (q.prompt.length + q.optionA.length + q.optionB.length + q.optionC.length + q.optionD.length +
            q.explanation.length + q.hints.sumOf { it.length }).toLong()

    private fun mbPair(got: Long, total: Long): String {
        fun whole(bytes: Long) = ((bytes + 500_000L) / 1_000_000L).toInt().coerceAtLeast(if (bytes > 0L) 1 else 0)
        val saved = whole(got)
        val all = whole(total).coerceAtLeast(saved)
        return "$saved/${all}MB"
    }
}
