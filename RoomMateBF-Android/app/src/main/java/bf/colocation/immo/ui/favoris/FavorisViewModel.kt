package bf.colocation.immo.ui.favoris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.Constants
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.FavoriDto
import bf.colocation.immo.data.repository.FavoriRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavorisUiState(
    val favoris: List<FavoriDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FavorisViewModel @Inject constructor(
    private val repository: FavoriRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FavorisUiState())
    val state: StateFlow<FavorisUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.liste(0, Constants.DEFAULT_PAGE_SIZE) }
                .onSuccess { p -> _state.update { it.copy(loading = false, favoris = p.items) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toFrenchMessage()) } }
        }
    }

    fun supprimer(favoriId: Long) {
        viewModelScope.launch {
            runCatching { repository.supprimer(favoriId) }
                .onSuccess { _state.update { s -> s.copy(favoris = s.favoris.filterNot { it.id == favoriId }) } }
        }
    }
}
