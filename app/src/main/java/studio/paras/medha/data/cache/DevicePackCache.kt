package studio.paras.medha.data.cache

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import studio.paras.medha.domain.model.AnswerLetter
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
    private val BANK_VERSION = "v11"
    private val VOICE_VERSION = "v3"
    private val app = context.applicationContext
    private val voiceDir = File(app.filesDir, "host-voice").also { it.mkdirs() }

    /** True when questions and voice are already on the device, so nothing is copied. */
    fun alreadyOnDevice(): Boolean = questionsCurrent() && voiceCurrent()

    private fun voiceCurrent(): Boolean {
        val marker = File(voiceDir, ".unpacked")
        return marker.exists() && marker.readText() == VOICE_VERSION && voiceDir.list()?.isNotEmpty() == true
    }

    private fun questionsCurrent(): Boolean {
        val dest = QuestionBankCache.file(app)
        val stamp = File(app.filesDir, "question-bank.bundled")
        return stamp.exists() &&
            stamp.readText() == BANK_VERSION &&
            dest.exists() &&
            dest.length() > 1_000L
    }

    suspend fun sync(onProgress: (line: String, detail: String, fraction: Float) -> Unit) {
        if (!questionsCurrent()) copyQuestions(onProgress)
        if (!voiceCurrent()) unpackVoice(onProgress)
    }

    private fun copyQuestions(onProgress: (String, String, Float) -> Unit) {
        val dest = QuestionBankCache.file(app)
        val stamp = File(app.filesDir, "question-bank.bundled")
        if (questionsCurrent()) {
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
        stamp.writeText(BANK_VERSION)
    }

    private fun unpackVoice(onProgress: (String, String, Float) -> Unit) {
        val marker = File(voiceDir, ".unpacked")
        val total = app.assets.openFd("offline/voice-packs.zip").use { it.length }.coerceAtLeast(1L)
        var got = 0L
        java.util.zip.ZipInputStream(app.assets.open("offline/voice-packs.zip")).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory) {
                    val out = File(voiceDir, File(entry.name).name)
                    if (!out.exists() || out.length() == 0L) {
                        out.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                val read = zip.read(buffer)
                                if (read < 0) break
                                output.write(buffer, 0, read)
                                got += read
                            }
                        }
                    } else {
                        got += entry.size
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
        marker.writeText(VOICE_VERSION)
        val bytes = voiceBytes()
        onProgress("Downloading voice pack", mbPair(bytes, bytes), 0.96f)
    }

    private fun voiceBytes(): Long =
        voiceDir.listFiles()?.sumOf { if (it.isFile) it.length() else 0L } ?: 0L

    private fun mbPair(got: Long, total: Long): String {
        fun whole(bytes: Long) = ((bytes + 500_000L) / 1_000_000L).toInt().coerceAtLeast(if (bytes > 0L) 1 else 0)
        val saved = whole(got)
        val all = whole(total).coerceAtLeast(saved)
        return "$saved/${all}MB"
    }
}
