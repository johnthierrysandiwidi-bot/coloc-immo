package bf.colocation.immo.ui.annonces

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.AnnonceDto
import bf.colocation.immo.data.repository.AnnonceRepository
import bf.colocation.immo.data.repository.AuthRepository
import bf.colocation.immo.data.repository.FavoriRepository
import bf.colocation.immo.data.repository.RendezVousRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnnonceDetailUiState(
    val annonce: AnnonceDto? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val favoriEnCours: Boolean = false,
    val message: String? = null,       // toast/snackbar succès
    val visiteEnCours: Boolean = false,
    val conversationOuverte: Long? = null
)

@HiltViewModel
class AnnonceDetailViewModel @Inject constructor(
    private val annonceRepository: AnnonceRepository,
    private val favoriRepository: FavoriRepository,
    private val rendezVousRepository: RendezVousRepository,
    private val messagerieRepository: bf.colocation.immo.data.repository.MessagerieRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val annonceId: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: -1L

    private val _state = MutableStateFlow(AnnonceDetailUiState())
    val state: StateFlow<AnnonceDetailUiState> = _state.asStateFlow()

    init { charger() }

    /** Ouvre (ou retrouve) la conversation avec l'auteur, puis expose son id pour navigation. */
    fun contacter() {
        val id = _state.value.annonce?.id ?: return
        viewModelScope.launch {
            runCatching { messagerieRepository.ouvrirPourAnnonce(id) }
                .onSuccess { conv -> _state.update { it.copy(conversationOuverte = conv.id) } }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
        }
    }

    fun navigationConsommee() {
        _state.update { it.copy(conversationOuverte = null) }
    }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { annonceRepository.detail(annonceId) }
                .onSuccess { a -> _state.update { it.copy(loading = false, annonce = a) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toFrenchMessage()) } }
        }
    }

    fun ajouterFavori() {
        _state.update { it.copy(favoriEnCours = true, message = null) }
        viewModelScope.launch {
            runCatching {
                val moi = authRepository.account()
                favoriRepository.ajouter(annonceId, moi.id ?: error("Utilisateur inconnu"))
            }.onSuccess {
                _state.update { it.copy(favoriEnCours = false, message = "Ajouté aux favoris ✅") }
            }.onFailure { e ->
                val msg = if ((e.message ?: "").contains("400") || (e.message ?: "").contains("409"))
                    "Cette annonce est déjà dans tes favoris."
                else e.toFrenchMessage()
                _state.update { it.copy(favoriEnCours = false, message = msg) }
            }
        }
    }

    /** dateSouhaitee au format ISO Instant (ex "2025-07-20T14:00:00Z"). */
    fun demanderVisite(dateIso: String, message: String?) {
        _state.update { it.copy(visiteEnCours = true, message = null) }
        viewModelScope.launch {
            runCatching { rendezVousRepository.demanderVisite(annonceId, dateIso, message) }
                .onSuccess {
                    _state.update { it.copy(visiteEnCours = false, message = "Demande de visite envoyée 📅") }
                }
                .onFailure { e ->
                    _state.update { it.copy(visiteEnCours = false, message = e.toFrenchMessage()) }
                }
        }
    }

    fun messageConsomme() = _state.update { it.copy(message = null) }
}
