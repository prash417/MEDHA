package studio.paras.medha.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import studio.paras.medha.R
import java.util.concurrent.atomic.AtomicBoolean

enum class Sfx(val res: Int) {
    LAUNCH(R.raw.sfx_launch),
    TAP(R.raw.sfx_tap),
    SELECT(R.raw.sfx_select),
    QUESTION(R.raw.sfx_question),
    SUSPENSE(R.raw.sfx_suspense),
    HEARTBEAT(R.raw.sfx_heartbeat),
    LIFELINE(R.raw.sfx_lifeline),
    CORRECT(R.raw.sfx_correct),
    WRONG(R.raw.sfx_wrong),
    PRIZE(R.raw.sfx_prize),
    PROGRESS(R.raw.sfx_progress),
    VICTORY(R.raw.sfx_victory),
    GAME_OVER(R.raw.sfx_gameover),
    TIMER(R.raw.sfx_timer),
}

enum class MusicTrack(val assetFile: String? = null, val res: Int = 0, val loop: Boolean = true) {
    LOBBY(assetFile = "music/music_final_answer.mp3"),
    GAMEPLAY(res = R.raw.music_gameplay),
    HIGH_STAKES(res = R.raw.music_highstakes),
    VICTORY(res = R.raw.music_victory),
    GAME_OVER(res = R.raw.music_gameover),
    THINK_CAREFULLY(assetFile = "music/Think Carefully.mp3", loop = false),
    TICKING_MYSTERY(assetFile = "music/Ticking Mystery.mp3"),
}

enum class VoiceCue {
    QUESTION_NUMBER,
    OPTION_SELECTED,
    FINAL_ANSWER,
    CORRECT,
    INCORRECT,
}

sealed class HostBeat {
    data class Question(val durationMs: Long) : HostBeat()
    data class Option(val index: Int, val durationMs: Long) : HostBeat()
}

interface VoiceAnnouncer {
    var enabled: Boolean
    fun speak(cue: VoiceCue, detail: String = "", onDone: () -> Unit = {})
    fun readQuestion(
        number: Int,
        prompt: String,
        options: List<studio.paras.medha.domain.model.AnswerOption>,
        onBeat: (HostBeat) -> Unit = {},
        onDone: () -> Unit = {},
    )
    fun prefetchQuestion(
        number: Int,
        prompt: String,
        options: List<studio.paras.medha.domain.model.AnswerOption>,
    ) = Unit
    fun stop()
    /** Stop speech and still finish the current read so the question can continue. */
    fun hush() { stop() }
}

class SilentVoice : VoiceAnnouncer {
    override var enabled: Boolean = false
    override fun speak(cue: VoiceCue, detail: String, onDone: () -> Unit) = onDone()
    override fun readQuestion(
        number: Int,
        prompt: String,
        options: List<studio.paras.medha.domain.model.AnswerOption>,
        onBeat: (HostBeat) -> Unit,
        onDone: () -> Unit,
    ) {
        val slices = studio.paras.medha.data.cloud.HostBoard.pacedSlices(
            studio.paras.medha.data.cloud.HostBoard.scripts(number, prompt, options),
        )
        onBeat(HostBeat.Question(slices.firstOrNull() ?: 1_600L))
        options.indices.forEach { index ->
            onBeat(HostBeat.Option(index, slices.getOrElse(index + 1) { 900L }))
        }
        onDone()
    }
    override fun stop() = Unit
}

class MedhaAudio(context: Context) {
    private val appContext = context.applicationContext
    private val loaded = AtomicBoolean(false)
    private val soundIds = mutableMapOf<Sfx, Int>()
    @Volatile var sfxEnabled: Boolean = true
    @Volatile var musicEnabled: Boolean = true
    @Volatile var timerMusicEnabled: Boolean = true
    @Volatile var clockTicksEnabled: Boolean = true
    @Volatile var hapticsEnabled: Boolean = true
    @Volatile var reducedMotion: Boolean = false

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private var music: ExoPlayer? = null
    private var voiceClip: ExoPlayer? = null
    private var currentTrack: MusicTrack? = null
    private var preparedTrack: MusicTrack? = null
    @Volatile private var ducked: Boolean = false

    fun warm() {
        if (loaded.getAndSet(true)) return
        Sfx.entries.forEach { sfx ->
            soundIds[sfx] = pool.load(appContext, sfx.res, 1)
        }
    }

    fun play(sfx: Sfx) {
        val allowed = if (sfx == Sfx.TIMER) clockTicksEnabled else sfxEnabled
        if (!allowed) return
        val id = soundIds[sfx] ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun buzz(long: Boolean = false) {
        if (!hapticsEnabled) return
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return
        val ms = if (long) 46L else 22L
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    fun setMusic(track: MusicTrack?, restart: Boolean = false) {
        if (track == null) {
            currentTrack = null
            preparedTrack = null
            music?.pause()
            return
        }
        currentTrack = track
        if (!trackAllowed(track)) {
            music?.pause()
            return
        }
        val player = music ?: ExoPlayer.Builder(appContext).build().also { created ->
            created.volume = if (ducked) 0.22f else 0.68f
            created.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED && currentTrack == MusicTrack.THINK_CAREFULLY) {
                        setMusic(MusicTrack.TICKING_MYSTERY)
                    }
                }
            })
            music = created
        }
        val same = track == preparedTrack
        if (same && player.isPlaying && !restart) return
        if (same && !restart) {
            player.play()
            return
        }
        preparedTrack = track
        player.repeatMode = if (track.loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        player.setMediaItem(mediaItemFor(track))
        player.prepare()
        player.play()
    }

    /** Background music only. Timer music keeps playing when this is turned off. */
    fun applyBackgroundMusic(enabled: Boolean) {
        musicEnabled = enabled
        val track = currentTrack
        if (track == MusicTrack.THINK_CAREFULLY && timerMusicEnabled) return
        if (enabled && track != null) setMusic(track) else if (!enabled) pauseMusic()
    }

    fun duck(on: Boolean) {
        ducked = on
        music?.volume = if (on) 0.22f else 0.68f
    }

    fun playVoiceClip(
        uri: String,
        onReady: (durationMs: Long) -> Unit = {},
        onDone: () -> Unit,
    ) {
        stopVoiceClip()
        val player = ExoPlayer.Builder(appContext).build().also { voiceClip = it }
        player.volume = 1f
        player.repeatMode = Player.REPEAT_MODE_OFF
        val started = AtomicBoolean(false)
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && started.compareAndSet(false, true)) {
                    val dur = player.duration
                    onReady(if (dur > 0) dur else 0L)
                }
                if (state == Player.STATE_ENDED) {
                    stopVoiceClip()
                    onDone()
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                stopVoiceClip()
                onDone()
            }
        })
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    fun stopVoiceClip() {
        voiceClip?.release()
        voiceClip = null
    }

    fun haltPlayback() {
        stopVoiceClip()
        ducked = false
        currentTrack = null
        preparedTrack = null
        music?.volume = 0.68f
        music?.pause()
        music?.stop()
    }

    fun pauseMusic() {
        music?.pause()
    }

    fun dropTimerMusic() {
        if (currentTrack != MusicTrack.THINK_CAREFULLY) return
        if (musicEnabled) setMusic(MusicTrack.TICKING_MYSTERY) else pauseMusic()
    }

    fun resumeMusic() {
        val track = currentTrack ?: return
        if (trackAllowed(track)) setMusic(track)
    }

    private fun trackAllowed(track: MusicTrack): Boolean =
        if (track == MusicTrack.THINK_CAREFULLY) timerMusicEnabled else musicEnabled

    fun release() {
        pool.release()
        music?.release()
        music = null
        stopVoiceClip()
        loaded.set(false)
    }

    private fun mediaItemFor(track: MusicTrack): MediaItem {
        val asset = track.assetFile
        if (!asset.isNullOrBlank()) {
            val encoded = android.net.Uri.encode(asset, "/")
            return MediaItem.fromUri("asset:///$encoded")
        }
        return MediaItem.fromUri("android.resource://${appContext.packageName}/${track.res}")
    }
}
