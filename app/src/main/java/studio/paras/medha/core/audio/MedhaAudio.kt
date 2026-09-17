package studio.paras.medha.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
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

enum class MusicTrack(val res: Int) {
    LOBBY(R.raw.music_final_answer),
    GAMEPLAY(R.raw.music_gameplay),
    HIGH_STAKES(R.raw.music_highstakes),
    VICTORY(R.raw.music_victory),
    GAME_OVER(R.raw.music_gameover),
}

enum class VoiceCue {
    QUESTION_NUMBER,
    OPTION_SELECTED,
    FINAL_ANSWER,
    CORRECT,
    INCORRECT,
}

interface VoiceAnnouncer {
    fun speak(cue: VoiceCue, detail: String = "")
}

class SilentVoice : VoiceAnnouncer {
    override fun speak(cue: VoiceCue, detail: String) = Unit
}

class MedhaAudio(context: Context) {
    private val appContext = context.applicationContext
    private val loaded = AtomicBoolean(false)
    private val soundIds = mutableMapOf<Sfx, Int>()
    @Volatile var sfxEnabled: Boolean = true
    @Volatile var musicEnabled: Boolean = true

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
    private var currentTrack: MusicTrack? = null

    fun warm() {
        if (loaded.getAndSet(true)) return
        Sfx.entries.forEach { sfx ->
            soundIds[sfx] = pool.load(appContext, sfx.res, 1)
        }
    }

    fun play(sfx: Sfx) {
        if (!sfxEnabled) return
        val id = soundIds[sfx] ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun setMusic(track: MusicTrack?) {
        if (track == null || !musicEnabled) {
            currentTrack = track
            music?.pause()
            return
        }
        if (track == currentTrack && music?.isPlaying == true) return
        currentTrack = track
        val player = music ?: ExoPlayer.Builder(appContext).build().also { created ->
            created.repeatMode = Player.REPEAT_MODE_ALL
            created.volume = 0.62f
            music = created
        }
        player.setMediaItem(MediaItem.fromUri("android.resource://${appContext.packageName}/${track.res}"))
        player.prepare()
        player.play()
    }

    fun pauseMusic() {
        music?.pause()
    }

    fun resumeMusic() {
        if (musicEnabled && currentTrack != null) music?.play()
    }

    fun release() {
        pool.release()
        music?.release()
        music = null
        loaded.set(false)
    }
}
