package studio.paras.medha.ui.splash

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class SplashUiState(
    val progress: Float = 0f,
    val percent: Int = 0,
    val line: String = " ",
    val detail: String = " ",
    val finished: Boolean = false,
)

private data class Pack(val file: String, val bytes: Int, val downloadMs: Long, val extractMs: Long)

private val Packs = listOf(
    Pack("studio/lights.pak", 2_180_000, 1_720, 1_180),
    Pack("audio/theme_final_answer.ogg", 1_463_269, 1_800, 1_120),
    Pack("bank/questions.qdb", 4_180_000, 2_160, 1_280),
    Pack("bank/answers.bin", 890_000, 1_240, 1_040),
    Pack("stage/hot_seat.pak", 1_120_000, 1_480, 1_100),
)

fun formatBytes(n: Long): String {
    val v = n.coerceAtLeast(0L)
    return when {
        v >= 1_000_000 -> String.format(Locale.US, "%.1f MB", v / 1_000_000.0)
        v >= 1_000 -> String.format(Locale.US, "%.0f KB", v / 1_000.0)
        else -> "$v B"
    }
}

class SplashViewModel : ViewModel() {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { runDummyBoot() }
    }

    private suspend fun runDummyBoot() {
        delay(3_050L)
        val totalBytes = Packs.sumOf { it.bytes.toLong() }
        val afterConnect = 0.05f
        val afterPacks = 0.84f

        runHold("Connecting to the arena...", 0f, afterConnect, 900L)

        var done = 0L
        Packs.forEach { pack ->
            val share = (pack.bytes.toFloat() / totalBytes) * (afterPacks - afterConnect)
            val start = afterConnect + (done.toFloat() / totalBytes) * (afterPacks - afterConnect)
            val afterDl = start + share * 0.74f
            val afterEx = start + share

            runDownload(pack, start, afterDl)
            runExtract(pack, afterDl, afterEx)
            done += pack.bytes
        }

        runHold("Adding user experience...", afterPacks, 0.96f, 1_250L)
        emit(1f, "Ready", " ")
        delay(HOLD_MS)
        _state.value = _state.value.copy(finished = true)
    }

    private suspend fun runHold(line: String, from: Float, to: Float, ms: Long) {
        val t0 = SystemClock.uptimeMillis()
        while (true) {
            val u = ((SystemClock.uptimeMillis() - t0).toFloat() / ms).coerceIn(0f, 1f)
            emit(from + (to - from) * u, line, " ")
            if (u >= 1f) break
            delay(16)
        }
    }

    private suspend fun runDownload(pack: Pack, from: Float, to: Float) {
        val t0 = SystemClock.uptimeMillis()
        while (true) {
            val u = ((SystemClock.uptimeMillis() - t0).toFloat() / pack.downloadMs).coerceIn(0f, 1f)
            val hitch = when {
                u < 0.12f -> u * 0.32f
                u > 0.86f -> 0.70f + (u - 0.86f) / 0.14f * 0.30f
                else -> 0.32f + (u - 0.12f) / 0.74f * 0.38f
            }.coerceIn(0f, 1f)
            val got = (pack.bytes * hitch).toLong()
            emit(
                from + (to - from) * hitch,
                "Downloading  ${pack.file}",
                "${formatBytes(got)}  /  ${formatBytes(pack.bytes.toLong())}",
            )
            if (u >= 1f) break
            delay(16)
        }
    }

    private suspend fun runExtract(pack: Pack, from: Float, to: Float) {
        val t0 = SystemClock.uptimeMillis()
        while (true) {
            val u = ((SystemClock.uptimeMillis() - t0).toFloat() / pack.extractMs).coerceIn(0f, 1f)
            emit(
                from + (to - from) * u,
                "Extracting  ${pack.file}",
                " ",
            )
            if (u >= 1f) break
            delay(16)
        }
    }

    private fun emit(progress: Float, line: String, detail: String) {
        val p = progress.coerceIn(0f, 1f)
        _state.value = SplashUiState(
            progress = p,
            percent = (p * 100f).toInt().coerceIn(0, 100),
            line = line,
            detail = detail,
        )
    }

    companion object {
        const val HOLD_MS = 700L
    }
}
