package bf.colocation.immo.ui.tableaudebord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.AnnonceDto
import bf.colocation.immo.data.repository.AnnonceRepository
import bf.colocation.immo.data.repository.AuthRepository
import bf.colocation.immo.data.repository.FavoriRepository
import bf.colocation.immo.data.repository.NotificationRepository
import bf.colocation.immo.data.repository.RendezVousRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TableauDeBordUiState(
    val prenom: String = "",
    val favoris: Int = 0,
    val visites: Int = 0,
    val alertes: Int = 0,
    val recentes: List<AnnonceDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

/** Statuts pour lesquels la visite est encore à venir. */
private val STATUTS_ACTIFS = setOf("DEMANDE", "REPORTE", "ACCEPTE")

/**
 * Données du tableau de bord.
 *
 * Aucun nouvel appel n'a été ajouté au serveur : les compteurs sont dérivés des
 * listes déjà exposées (favoris, rendez-vous, notifications). Chaque chargement
 * est indépendant, de sorte qu'un échec sur l'un d'eux — un droit manquant, par
 * exemple — n'efface pas tout l'écran.
 */
@HiltViewModel
class TableauDeBordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val annonceRepository: AnnonceRepository,
    private val favoriRepository: FavoriRepository,
    private val rendezVousRepository: RendezVousRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TableauDeBordUiState())
    val state: StateFlow<TableauDeBordUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }

        viewModelScope.launch {
            runCatching { authRepository.account() }
                .onSuccess { compte ->
                    val nom = compte.firstName?.takeIf { it.isNotBlank() } ?: compte.login
                    _state.update { it.copy(prenom = nom) }
                }
        }

        viewModelScope.launch {
            runCatching { annonceRepository.liste(page = 0, size = 6, recherche = null, type = null) }
                .onSuccess { page -> _state.update { it.copy(loading = false, recentes = page.items) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toFrenchMessage()) } }
        }

        viewModelScope.launch {
            runCatching { favoriRepository.liste(0, 1) }
                .onSuccess { p -> _state.update { it.copy(favoris = p.total) } }
        }

        viewModelScope.launch {
            runCatching { rendezVousRepository.liste(0, 50) }
                .onSuccess { p ->
                    val actifs = p.items.count { it.statut in STATUTS_ACTIFS }
                    _state.update { it.copy(visites = actifs) }
                }
        }

        viewModelScope.launch {
            runCatching { notificationRepository.liste(0, 50) }
                .onSuccess { p ->
                    val nonLues = p.items.count { it.lue != true }
                    _state.update { it.copy(alertes = nonLues) }
                }
        }
    }
}
