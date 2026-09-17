package studio.paras.medha.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import studio.paras.medha.domain.model.GameMode

data class HomeUiState(
    val dest: LobbyDest = LobbyDest.HOME,
    val overlay: LobbyOverlay? = null,
    val catalog: LobbyCatalog = LobbyCatalog.sample(),
    val nowMs: Long = System.currentTimeMillis(),
    val selectedMode: GameMode = GameMode.CLASSIC,
    val selectedCategory: String = "gk",
)

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(nowMs = System.currentTimeMillis()) }
                delay(1_000)
            }
        }
    }

    fun selectMode(mode: GameMode) {
        _state.update { it.copy(selectedMode = mode, dest = LobbyDest.HOME) }
    }

    fun selectCategory(id: String) {
        _state.update { it.copy(selectedCategory = id, selectedMode = GameMode.PRACTICE, dest = LobbyDest.HOME) }
    }

    fun select(dest: LobbyDest) {
        val overlay = when (dest) {
            LobbyDest.MISSIONS -> LobbyOverlay.MISSIONS
            LobbyDest.ACHIEVEMENTS -> LobbyOverlay.ACHIEVEMENTS
            LobbyDest.INVENTORY -> LobbyOverlay.INVENTORY
            else -> null
        }
        _state.update { it.copy(dest = dest, overlay = overlay) }
    }

    fun open(overlay: LobbyOverlay) {
        _state.update { it.copy(overlay = overlay) }
    }

    fun dismissOverlay() {
        _state.update { it.copy(overlay = null, dest = LobbyDest.HOME) }
    }
}
