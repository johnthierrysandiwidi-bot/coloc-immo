package bf.colocation.immo.ui.annonces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.Constants
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.AnnonceDto
import bf.colocation.immo.data.repository.AnnonceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** null = tous. Sinon VENTE / LOCATION / COLOCATION. */
data class AnnonceListUiState(
    val annonces: List<AnnonceDto> = emptyList(),
    val recherche: String = "",
    val typeFiltre: String? = null,
    val loading: Boolean = false,      // premier chargement / refresh
    val loadingMore: Boolean = false,  // pagination
    val error: String? = null,
    val page: Int = 0,
    val total: Int = 0
) {
    val peutChargerPlus: Boolean get() = annonces.size < total && !loadingMore && !loading
}

@HiltViewModel
class AnnonceListViewModel @Inject constructor(
    private val repository: AnnonceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnnonceListUiState())
    val state: StateFlow<AnnonceListUiState> = _state.asStateFlow()

    init { rafraichir() }

    fun onRecherche(v: String) = _state.update { it.copy(recherche = v) }

    fun appliquerRecherche() = rafraichir()

    fun onTypeFiltre(type: String?) {
        if (_state.value.typeFiltre == type) return
        _state.update { it.copy(typeFiltre = type) }
        rafraichir()
    }

    fun rafraichir() {
        val s = _state.value
        _state.update { it.copy(loading = true, error = null, page = 0) }
        viewModelScope.launch {
            runCatching {
                repository.liste(0, Constants.DEFAULT_PAGE_SIZE, s.recherche, s.typeFiltre)
            }.onSuccess { paged ->
                _state.update { it.copy(loading = false, annonces = paged.items, total = paged.total, page = 0) }
            }.onFailure { e ->
                _state.update { it.copy(loading = false, error = e.toFrenchMessage()) }
            }
        }
    }

    fun chargerPlus() {
        val s = _state.value
        if (!s.peutChargerPlus) return
        val prochaine = s.page + 1
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            runCatching {
                repository.liste(prochaine, Constants.DEFAULT_PAGE_SIZE, s.recherche, s.typeFiltre)
            }.onSuccess { paged ->
                _state.update {
                    it.copy(
                        loadingMore = false,
                        annonces = it.annonces + paged.items,
                        page = prochaine,
                        total = paged.total
                    )
                }
            }.onFailure {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }
}
