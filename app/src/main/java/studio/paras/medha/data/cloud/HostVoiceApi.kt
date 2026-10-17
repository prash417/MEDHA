package studio.paras.medha.data.cloud

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import studio.paras.medha.domain.model.AnswerOption
import java.io.File
import java.security.MessageDigest

/**
 * Host voice clips. Each line of speech is an mp3 named by a hash of its script.
 * Clips are looked up on the phone first; if missing they are fetched from Firebase
 * Storage (voice/<hash>.mp3, publicly readable) and kept, so the app no longer has to
 * ship the large offline voice pack.
 */
class HostVoiceApi(
    context: Context,
) {
    private val cacheDir = File(context.filesDir, "host-voice").also { it.mkdirs() }
    private val storage by lazy { runCatching { FirebaseStorage.getInstance(VoiceBucket) }.getOrNull() }

    /** Hashes that were not on the server, with when we last tried, so we do not keep asking. */
    private val misses = ConcurrentHashMap<String, Long>()

    fun peekBoard(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val file = cachedFile(cacheKey("v10|$script"))
        return if (file == null) emptyList() else listOf(file)
    }

    /** Board clip for a question; downloads it if needed. Used ahead of time, so it may wait longer. */
    suspend fun speakBoard(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val hash = cacheKey("v10|$script")
        val file = cachedFile(hash) ?: download(hash, PrefetchTimeoutMs)
        return if (file == null) emptyList() else listOf(file)
    }

    /** Short host line ("Answer locked.") ; a quick fetch only, so play never stalls. */
    suspend fun speakLine(kind: String, detail: String = "", line: String): String? {
        val hash = cacheKey("v10|$line")
        return cachedFile(hash) ?: download(hash, LiveTimeoutMs)
    }

    /** Downloads every line in [scripts] that is not on the phone yet. Returns how many are now cached. */
    suspend fun prefetchLines(scripts: List<String>, onEach: (done: Int, total: Int) -> Unit = { _, _ -> }): Int {
        var ok = 0
        var failedInRow = 0
        for ((i, script) in scripts.withIndex()) {
            val hash = cacheKey("v10|$script")
            if (cachedFile(hash) != null || download(hash, PrefetchTimeoutMs) != null) {
                ok++
                failedInRow = 0
            } else {
                failedInRow++
                // Probably offline: stop instead of waiting on every line.
                if (failedInRow >= 3) break
            }
            onEach(i + 1, scripts.size)
        }
        return ok
    }

    fun findSpoken(script: String): String? = cachedFile(cacheKey("v10|$script"))

    fun localWav(script: String): File = File(cacheDir, cacheKey("v10|$script") + ".wav")

    private fun cachedFile(hash: String): String? {
        if (hash.isBlank()) return null
        val mp3 = File(cacheDir, "$hash.mp3")
        if (mp3.exists() && mp3.length() > 0L) return Uri.fromFile(mp3).toString()
        val wav = File(cacheDir, "$hash.wav")
        return if (wav.exists() && wav.length() > 0L) Uri.fromFile(wav).toString() else null
    }

    private suspend fun download(hash: String, timeoutMs: Long): String? = withContext(Dispatchers.IO) {
        val lastMiss = misses[hash]
        if (lastMiss != null && System.currentTimeMillis() - lastMiss < RetryAfterMs) return@withContext null
        val ref = storage?.reference?.child("voice/$hash.mp3") ?: return@withContext null
        val part = File(cacheDir, "$hash.part")
        val dest = File(cacheDir, "$hash.mp3")
        val ok = runCatching {
            withTimeout(timeoutMs) { ref.getFile(part).await() }
            part.length() > 0L && part.renameTo(dest)
        }.getOrDefault(false)
        if (!ok) {
            part.delete()
            misses[hash] = System.currentTimeMillis()
            return@withContext null
        }
        misses.remove(hash)
        Uri.fromFile(dest).toString()
    }

    private fun cacheKey(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
    }

    private companion object {
        /** Same bucket the hostSpeak Cloud Function writes clips to. */
        const val VoiceBucket = "gs://paras-medha.firebasestorage.app"
        const val LiveTimeoutMs = 2_500L
        const val PrefetchTimeoutMs = 12_000L
        const val RetryAfterMs = 10 * 60 * 1_000L
    }
}

internal object HostBoard {
    fun scripts(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val q = prompt.trim().trimEnd('?')
        val parts = mutableListOf("$q?")
        listOf("A", "B", "C", "D").forEach { letter ->
            val text = options.find { it.letter.name == letter }?.text?.trim()?.trimEnd('.') ?: return@forEach
            if (text.isNotBlank()) parts += "Option $letter. $text."
        }
        return parts
    }

    fun boardScript(prompt: String, options: List<AnswerOption>): String =
        scripts(1, prompt, options).joinToString(" ")

    fun wrongScript(letter: String, optionText: String): String {
        val text = optionText.trim().trimEnd('.', ' ')
        val detail = if (text.isBlank()) "option $letter" else "option $letter, $text"
        return "Your answer is wrong. The correct answer is $detail."
    }

    fun sharedLines(): List<String> = buildList {
        for (n in 1..15) add("Question $n.")
        listOf("A", "B", "C", "D").forEach { add("You selected option $it.") }
        add("Answer locked.")
        add("Your answer is correct.")
        add("Your answer is wrong.")
    }

    fun cacheKey(script: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest("v10|$script".toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
    }

    /** Same spoken pace the host uses, so text still unfolds when the voice is off. */
    fun pacedSlices(scripts: List<String>): List<Long> =
        scripts.map { script -> script.length.coerceAtLeast(12) * 68L }
}
