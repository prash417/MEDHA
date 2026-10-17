package studio.paras.medha.core.audio

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.net.Uri
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import studio.paras.medha.data.cloud.HostBoard
import studio.paras.medha.data.cloud.HostVoiceApi
import studio.paras.medha.domain.model.AnswerLetter
import studio.paras.medha.domain.model.AnswerOption
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Host reader: Google Indian-English neural voice when installed.
 * Reads the board, then [onDone] so the 30s countdown can start.
 */
class MedhaVoice(
    context: Context,
    private val audio: MedhaAudio,
    private val hostApi: HostVoiceApi,
    private val onSpeaking: (Boolean) -> Unit = {},
) : VoiceAnnouncer {

    @Volatile override var enabled: Boolean = true

    private val app = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val jobs = SupervisorJob()
    private val warmJobs = SupervisorJob()
    private val scope = CoroutineScope(jobs + Dispatchers.Main.immediate)
    private val warmScope = CoroutineScope(warmJobs + Dispatchers.IO)
    private var tts: TextToSpeech? = null
    private val ready = AtomicBoolean(false)
    private val queued = mutableListOf<() -> Unit>()
    private var lastUtterance: String? = null
    private var pendingDone: (() -> Unit)? = null
    private val doneOnce = AtomicBoolean(true)
    private val synthDone = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()
    @Volatile private var session = 0
    private val timeout = Runnable { if (!doneOnce.get()) fireDone() }

    fun warm() {
        if (tts != null) return
        bindEngine(GOOGLE_ENGINE) { ok ->
            if (ok) {
                finishInit()
            } else {
                tts?.shutdown()
                tts = null
                bindEngine(null) { finishInit() }
            }
        }
    }

    private fun bindEngine(engine: String?, done: (Boolean) -> Unit) {
        val listener = TextToSpeech.OnInitListener { status ->
            done(status == TextToSpeech.SUCCESS)
        }
        tts = if (engine != null) TextToSpeech(app, listener, engine) else TextToSpeech(app, listener)
    }

    private fun finishInit() {
        pickHostVoice()
        tts?.setSpeechRate(1.02f)
        tts?.setPitch(0.97f)
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                main.post { onSpeaking(true) }
            }
            override fun onDone(utteranceId: String?) {
                if (finishSynth(utteranceId, true)) return
                if (utteranceId == lastUtterance) main.post { fireDone() }
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (finishSynth(utteranceId, false)) return
                if (utteranceId == lastUtterance) main.post { fireDone() }
            }
            override fun onError(utteranceId: String?, errorCode: Int) {
                if (finishSynth(utteranceId, false)) return
                if (utteranceId == lastUtterance) main.post { fireDone() }
            }
        })
        ready.set(true)
        synchronized(queued) {
            queued.forEach { it() }
            queued.clear()
        }
    }

    override fun speak(cue: VoiceCue, detail: String, onDone: () -> Unit) {
        if (!enabled) {
            onDone()
            return
        }
        val line = cueLine(cue, detail)
        stop()
        val mine = session
        scope.launch {
            val url = withContext(Dispatchers.IO) {
                runCatching { hostApi.speakLine(cue.name, detail, line) }
                    .onFailure { android.util.Log.e("MedhaVoice", "host cue failed", it) }
                    .getOrNull()
            }
            if (session != mine) return@launch
            if (!url.isNullOrBlank()) {
                onSpeaking(true)
                audio.playVoiceClip(url) {
                    onSpeaking(false)
                    if (session == mine) onDone()
                }
            } else {
                say(line, flush = true, doneId = null)
                onDone()
            }
        }
    }

    override fun readQuestion(
        number: Int,
        prompt: String,
        options: List<AnswerOption>,
        onBeat: (HostBeat) -> Unit,
        onDone: () -> Unit,
    ) {
        if (!enabled) {
            val mine = session
            scope.launch {
                playSilentBoard(prompt, options, onBeat, mine)
                if (session == mine) onDone()
            }
            return
        }
        stop()
        pendingDone = onDone
        doneOnce.set(false)
        val mine = session
        scope.launch {
            val numberUri = withContext(Dispatchers.IO) {
                runCatching { hostApi.speakLine("QUESTION_NUMBER", "", "Question $number.") }.getOrNull()
            }
            if (session != mine || doneOnce.get() || pendingDone == null) return@launch
            if (!numberUri.isNullOrBlank()) playClip(numberUri)
            if (session != mine || doneOnce.get() || pendingDone == null) return@launch
            var clips = hostApi.peekBoard(number, prompt, options)
            if (clips.isEmpty()) {
                clips = withContext(Dispatchers.IO) {
                    runCatching { hostApi.speakBoard(number, prompt, options) }
                        .onFailure { android.util.Log.e("MedhaVoice", "local board missing", it) }
                        .getOrDefault(emptyList())
                }
            }
            if (session != mine || doneOnce.get() || pendingDone == null) return@launch
            val scripts = HostBoard.scripts(number, prompt, options)
            if (clips.isEmpty() || scripts.size < 2) {
                fireDone()
                return@launch
            }
            playTimedBoard(clips.first(), scripts, onBeat, mine)
        }
        main.removeCallbacks(timeout)
        main.postDelayed(timeout, 45_000)
    }

    private suspend fun playSilentBoard(
        prompt: String,
        options: List<AnswerOption>,
        onBeat: (HostBeat) -> Unit,
        mine: Int,
    ) {
        val slices = HostBoard.pacedSlices(HostBoard.scripts(0, prompt, options))
        slices.forEachIndexed { index, slice ->
            if (session != mine) return
            if (index == 0) onBeat(HostBeat.Question(slice))
            else onBeat(HostBeat.Option(index - 1, slice))
            delay(slice)
        }
    }

    private suspend fun playClip(uri: String) {
        suspendCancellableCoroutine { cont ->
            audio.playVoiceClip(uri, onReady = {}) {
                if (cont.isActive) cont.resume(Unit)
            }
            cont.invokeOnCancellation { audio.stopVoiceClip() }
        }
    }

    private suspend fun playTimedBoard(
        uri: String,
        scripts: List<String>,
        onBeat: (HostBeat) -> Unit,
        mine: Int,
    ) {
        onSpeaking(true)
        val weights = scripts.map { it.length.coerceAtLeast(12) }
        val total = weights.sum().coerceAtLeast(1)
        suspendCancellableCoroutine { cont ->
            audio.playVoiceClip(
                uri,
                onReady = { dur ->
                    if (session != mine || doneOnce.get()) return@playVoiceClip
                    var acc = 0
                    scripts.forEachIndexed { index, _ ->
                        val startMs = dur * acc / total
                        val slice = dur * weights[index] / total
                        acc += weights[index]
                        main.postDelayed({
                            if (session != mine || doneOnce.get()) return@postDelayed
                            if (index == 0) onBeat(HostBeat.Question(slice))
                            else onBeat(HostBeat.Option(index - 1, slice))
                        }, startMs.coerceAtLeast(0L))
                    }
                },
            ) {
                if (cont.isActive) cont.resume(Unit)
            }
            cont.invokeOnCancellation { audio.stopVoiceClip() }
        }
        fireDone()
    }

    override fun prefetchQuestion(
        number: Int,
        prompt: String,
        options: List<AnswerOption>,
    ) {
        if (!enabled) return
        warmScope.launch {
            runCatching { hostApi.speakBoard(number, prompt, options) }
        }
    }

    private fun startDeviceRead(
        number: Int,
        prompt: String,
        options: List<AnswerOption>,
        onBeat: (HostBeat) -> Unit,
        mine: Int,
    ) {
        val clean = prompt.trim().trimEnd('?', '.')
        val lines = buildList {
            add("Question $number. $clean?")
            options.sortedBy { it.letter.ordinal }.forEach { option ->
                if (option.text.isNotBlank()) {
                    add("Option ${letterWord(option.letter)}. ${option.text.trim().trimEnd('.')}.")
                }
            }
        }
        if (lines.isEmpty()) {
            fireDone()
            return
        }
        val slices = HostBoard.pacedSlices(lines)
        var elapsed = 0L
        slices.forEachIndexed { index, slice ->
            val start = elapsed
            elapsed += slice
            main.postDelayed({
                if (session != mine || doneOnce.get()) return@postDelayed
                if (index == 0) onBeat(HostBeat.Question(slice))
                else onBeat(HostBeat.Option(index - 1, slice))
            }, start)
        }
        val last = "medha-q-$number-end"
        lastUtterance = last
        say(lines.first(), flush = true, doneId = if (lines.size == 1) last else null)
        lines.dropLast(1).drop(1).forEach { say(it, flush = false, doneId = null) }
        if (lines.size > 1) say(lines.last(), flush = false, doneId = last)
    }

    override fun hush() {
        val done = pendingDone
        stop()
        done?.invoke()
    }

    override fun stop() {
        session++
        jobs.cancelChildren()
        main.removeCallbacks(timeout)
        pendingDone = null
        doneOnce.set(true)
        lastUtterance = null
        audio.stopVoiceClip()
        tts?.stop()
        synthDone.values.forEach { if (!it.isCompleted) it.complete(false) }
        synthDone.clear()
        onSpeaking(false)
    }

    fun release() {
        stop()
        warmJobs.cancel()
        tts?.shutdown()
        tts = null
        ready.set(false)
    }

    private fun fireDone() {
        if (!doneOnce.compareAndSet(false, true)) return
        onSpeaking(false)
        val cb = pendingDone
        pendingDone = null
        lastUtterance = null
        cb?.invoke()
    }

    /** Speaks a missing line once with the phone's Indian-English host voice and keeps the file. */
    private suspend fun ensureLocalBoard(script: String): String? {
        hostApi.findSpoken(script)?.let { return it }
        if (!ready.get()) {
            suspendCancellableCoroutine { cont ->
                val go = { if (cont.isActive) cont.resume(Unit) }
                if (ready.get()) go() else synchronized(queued) { queued.add(go) }
            }
        }
        val dest = hostApi.localWav(script)
        if (dest.exists()) dest.delete()
        val ok = synthesize(script, dest)
        if (!ok || !dest.exists() || dest.length() == 0L) {
            if (dest.exists()) dest.delete()
            return null
        }
        return Uri.fromFile(dest).toString()
    }

    private suspend fun synthesize(script: String, dest: File): Boolean {
        val engine = tts ?: return false
        val id = "synth-${System.nanoTime()}"
        val gate = CompletableDeferred<Boolean>()
        synthDone[id] = gate
        val result = engine.synthesizeToFile(script, Bundle(), dest, id)
        if (result == TextToSpeech.ERROR) {
            synthDone.remove(id)
            return false
        }
        return gate.await()
    }

    private fun finishSynth(utteranceId: String?, ok: Boolean): Boolean {
        val gate = utteranceId?.let { synthDone.remove(it) } ?: return false
        if (!gate.isCompleted) gate.complete(ok)
        return true
    }

    private fun say(text: String, flush: Boolean, doneId: String?) {
        val run: () -> Unit = {
            val engine = tts
            if (engine == null) {
                fireDone()
            } else {
                val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                val id = doneId ?: "medha-${System.nanoTime()}"
                val params = Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
                }
                engine.speak(text, mode, params, id)
            }
        }
        if (ready.get()) run() else synchronized(queued) { queued.add(run) }
    }

    private fun pickHostVoice() {
        val engine = tts ?: return
        val voices = engine.voices.orEmpty()
        val scored = voices.map { it to hostScore(it) }.filter { it.second > 0 }.sortedByDescending { it.second }
        val picked = scored.firstOrNull()?.first
        if (picked != null) {
            engine.voice = picked
            return
        }
        val hindi = Locale("hi", "IN")
        val india = Locale("en", "IN")
        engine.language = when {
            engine.isLanguageAvailable(india) >= TextToSpeech.LANG_AVAILABLE -> india
            engine.isLanguageAvailable(hindi) >= TextToSpeech.LANG_AVAILABLE -> hindi
            else -> Locale.US
        }
    }

    private fun hostScore(voice: Voice): Int {
        val name = voice.name.lowercase()
        val loc = voice.locale
        if (loc.language != "en") return 0
        var score = 0
        if (loc.country.equals("IN", true) || name.contains("en-in") || name.contains("indian")) score += 40
        if (loc.country.equals("US", true) || name.contains("en-us")) score += 20
        if (name.contains("network") || name.contains("neural") || name.contains("wavenet") || name.contains("chirp")) {
            score += 25
        }
        if (name.contains("local")) score += 8
        if (voice.isNetworkConnectionRequired) score += 4
        if (name.contains("female") || name.contains("ena") || name.contains("enc")) score += 2
        return score
    }

    private fun letterWord(letter: AnswerLetter): String = when (letter) {
        AnswerLetter.A -> "A"
        AnswerLetter.B -> "B"
        AnswerLetter.C -> "C"
        AnswerLetter.D -> "D"
    }

    private fun cueLine(cue: VoiceCue, detail: String): String = when (cue) {
        VoiceCue.QUESTION_NUMBER -> "Question $detail."
        VoiceCue.OPTION_SELECTED -> "You selected option $detail."
        VoiceCue.FINAL_ANSWER -> "Answer locked."
        VoiceCue.CORRECT -> "Your answer is correct."
        VoiceCue.INCORRECT ->
            if (detail.isBlank()) "Your answer is wrong."
            else "Your answer is wrong. The correct answer is $detail."
    }

    companion object {
        private const val GOOGLE_ENGINE = "com.google.android.tts"
    }
}
