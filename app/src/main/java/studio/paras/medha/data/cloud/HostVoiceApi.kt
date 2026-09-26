package studio.paras.medha.data.cloud

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await
import studio.paras.medha.domain.model.AnswerOption
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class HostVoiceApi(
    context: Context,
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("asia-south1"),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    private val cacheDir = File(context.filesDir, "host-voice").also { it.mkdirs() }
    private val lock = Any()
    private val inflight = mutableMapOf<String, CompletableDeferred<List<String>>>()

    fun peekBoard(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val file = cachedFile(cacheKey("v10|$script"))
        return if (file == null) emptyList() else listOf(file)
    }

    suspend fun speakBoard(number: Int, prompt: String, options: List<AnswerOption>): List<String> {
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val hash = cacheKey("v10|$script")
        cachedFile(hash)?.let { return listOf(it) }
        downloadStored(hash)?.let { return listOf(it) }
        val data = call(
            hashMapOf(
                "kind" to "board",
                "number" to number,
                "prompt" to prompt,
                "options" to options.map { opt ->
                    hashMapOf("letter" to opt.letter.name, "text" to opt.text)
                },
            ),
        ) ?: return emptyList()
        val clip = (data["clips"] as? List<*>)?.firstOrNull() as? Map<*, *> ?: return emptyList()
        val b64 = clip["audioBase64"] as? String ?: return emptyList()
        return listOfNotNull(saveClip(hash, b64))
    }

    suspend fun speakLine(kind: String, detail: String = "", line: String): String? {
        val hash = cacheKey("v10|$line")
        cachedFile(hash)?.let { return it }
        downloadStored(hash)?.let { return it }
        val data = call(
            hashMapOf(
                "kind" to kind,
                "detail" to detail,
                "line" to line,
            ),
        ) ?: return null
        val b64 = data["audioBase64"] as? String ?: return null
        return saveClip(hash, b64)
    }

    private fun saveClip(hash: String, b64: String): String? {
        val bytes = Base64.decode(b64, Base64.DEFAULT)
        if (bytes.isEmpty()) return null
        val file = File(cacheDir, "$hash.mp3")
        file.writeBytes(bytes)
        return Uri.fromFile(file).toString()
    }

    private suspend fun fetchBoard(
        boardKey: String,
        number: Int,
        prompt: String,
        options: List<AnswerOption>,
    ): List<String> {
        cachedBoard(boardKey)?.let { return it }
        val script = HostBoard.scripts(number, prompt, options).joinToString(" ")
        val stored = downloadStored(cacheKey("v10|$script"))
        if (stored != null) return listOf(stored)
        val payload = hashMapOf<String, Any>(
            "kind" to "board",
            "number" to number,
            "prompt" to prompt,
            "options" to options.map { opt ->
                hashMapOf("letter" to opt.letter.name, "text" to opt.text)
            },
        )
        val data = call(payload) ?: return emptyList()
        val clips = data["clips"] as? List<*> ?: return emptyList()
        val dir = File(cacheDir, boardKey).also { it.mkdirs() }
        return clips.mapIndexedNotNull { index, raw ->
            val map = raw as? Map<*, *> ?: return@mapIndexedNotNull null
            val b64 = map["audioBase64"] as? String ?: return@mapIndexedNotNull null
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            if (bytes.isEmpty()) return@mapIndexedNotNull null
            val file = File(dir, "$index.mp3")
            file.writeBytes(bytes)
            Uri.fromFile(file).toString()
        }
    }

    private suspend fun downloadStored(hash: String): String? {
        if (hash.isBlank()) return null
        cachedFile(hash)?.let { return it }
        runCatching {
            if (auth.currentUser == null) auth.signInAnonymously().await()
        }
        val file = File(cacheDir, "$hash.mp3")
        return try {
            val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                .reference
                .child("voice/$hash.mp3")
            ref.getFile(file).await()
            if (file.length() > 0L) Uri.fromFile(file).toString() else null
        } catch (_: Exception) {
            if (file.exists() && file.length() <= 0L) file.delete()
            null
        }
    }

    private suspend fun call(payload: Map<String, Any>): Map<*, *>? {
        runCatching {
            if (auth.currentUser == null) auth.signInAnonymously().await()
        }
        val callable = functions.getHttpsCallable("hostSpeak")
        callable.setTimeout(45, TimeUnit.SECONDS)
        val snap = callable.call(payload).await()
        return snap.data as? Map<*, *>
    }

    private fun cachedBoard(key: String): List<String>? {
        val dir = File(cacheDir, key)
        if (!dir.isDirectory) return null
        val files = dir.listFiles()?.filter { it.name.endsWith(".mp3") }?.sortedBy {
            it.name.removeSuffix(".mp3").toIntOrNull() ?: 0
        } ?: return null
        if (files.isEmpty()) return null
        if (files.any { it.length() <= 0L }) return null
        return files.map { Uri.fromFile(it).toString() }
    }

    private fun cachedFile(hash: String): String? {
        if (hash.isBlank()) return null
        val file = File(cacheDir, "$hash.mp3")
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
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
