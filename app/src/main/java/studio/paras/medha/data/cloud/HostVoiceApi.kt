package studio.paras.medha.data.cloud

import android.content.Context
import android.net.Uri
import studio.paras.medha.domain.model.AnswerOption
import java.io.File
import java.security.MessageDigest

class HostVoiceApi(
    context: Context,
) {
    private val cacheDir = File(context.filesDir, "host-voice").also { it.mkdirs() }

    fun peekBoard(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val file = cachedFile(cacheKey("v10|$script"))
        return if (file == null) emptyList() else listOf(file)
    }

    suspend fun speakBoard(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val file = cachedFile(cacheKey("v10|$script"))
        return if (file == null) emptyList() else listOf(file)
    }

    suspend fun speakLine(kind: String, detail: String = "", line: String): String? {
        return cachedFile(cacheKey("v10|$line"))
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

    private fun cacheKey(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(24)
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
