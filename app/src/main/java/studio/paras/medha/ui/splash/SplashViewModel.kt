package studio.paras.medha.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.paras.medha.data.cache.DevicePackCache

data class SplashUiState(
    val progress: Float = 0f,
    val percent: Int = 0,
    val line: String = " ",
    val detail: String = " ",
    val ready: Boolean = false,
)

class SplashViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()
    private val packs = DevicePackCache(app)

    init {
        viewModelScope.launch { runBoot() }
    }

    private suspend fun runBoot() {
        val cached = packs.alreadyOnDevice()
        runCatching {
            packs.sync { line, detail, fraction ->
                emit(fraction, line, detail)
            }
        }
        if (cached) playWarmup()
        _state.value = _state.value.copy(progress = 1f, percent = 100, ready = true)
    }

    /** Packs are already saved, so the bar fills while short game lines play. */
    private suspend fun playWarmup() {
        delay(3_000)
        WarmupLines.forEachIndexed { index, beat ->
            emit((index + 1) / WarmupLines.size.toFloat(), beat.first, beat.second)
            delay(900)
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
        private val WarmupLines = listOf(
            "Please wait while the hall gets ready." to "Questions and the host voice are already on this phone.",
            "Read the whole question before you lock." to "Once you lock, that answer is final.",
            "A clean correct answer scores 100 XP." to "Use a lifeline and a correct answer scores 50 XP.",
            "A wrong answer or a timeout scores nothing." to "The clock keeps running while you think.",
            "Hint, Switch, and Dare sit on the left." to "Classic is fifteen questions. Quick Play is five.",
            "Practice plays the same board, with no XP." to "The host reads the question and every option.",
        )
    }
}
