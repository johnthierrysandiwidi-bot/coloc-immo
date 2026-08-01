package bf.colocation.immo.ui.rendezvous

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.Constants
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.PaiementDto
import bf.colocation.immo.data.remote.dto.RendezVousDto
import bf.colocation.immo.data.repository.RendezVousRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RendezVousUiState(
    val rendezVous: List<RendezVousDto> = emptyList(),
    /** Paiement connu pour chaque rendez-vous, indexé par identifiant de rendez-vous. */
    val paiements: Map<Long, PaiementDto> = emptyMap(),
    val montantFrais: Double? = null,
    val loading: Boolean = true,
    val error: String? = null,
    /** Rendez-vous dont le paiement est en cours de règlement, s'il y en a un. */
    val paiementEnCours: Long? = null,
    val messagePaiement: String? = null,
    /** Paiement qui vient d'aboutir : sert à présenter le reçu et sa référence. */
    val recu: PaiementDto? = null
)

/** Statuts pour lesquels la visite est encore à venir. */
private val STATUTS_ACTIFS = setOf("DEMANDE", "REPORTE", "ACCEPTE")

@HiltViewModel
class RendezVousViewModel @Inject constructor(
    private val repository: RendezVousRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RendezVousUiState())
    val state: StateFlow<RendezVousUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.liste(0, Constants.DEFAULT_PAGE_SIZE) }
                .onSuccess { page ->
                    _state.update { it.copy(loading = false, rendezVous = page.items) }
                    chargerFrais()
                    chargerPaiements(page.items)
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.toFrenchMessage()) }
                }
        }
    }

    private fun chargerFrais() {
        viewModelScope.launch {
            runCatching { repository.fraisDeVisite() }
                .onSuccess { montant -> _state.update { it.copy(montantFrais = montant) } }
        }
    }

    /**
     * Interroge l'état de règlement des visites à venir. Un échec sur l'une d'elles
     * (403 pour un rendez-vous qui ne nous concerne pas) ne doit pas vider la liste :
     * on ignore simplement l'entrée.
     */
    private fun chargerPaiements(liste: List<RendezVousDto>) {
        viewModelScope.launch {
            val trouves = mutableMapOf<Long, PaiementDto>()
            liste.filter { it.statut in STATUTS_ACTIFS }.forEach { rdv ->
                val id = rdv.id ?: return@forEach
                runCatching { repository.paiementDuRendezVous(id) }
                    .getOrNull()
                    ?.let { trouves[id] = it }
            }
            _state.update { it.copy(paiements = trouves) }
        }
    }

    fun annuler(id: Long, motif: String?) {
        viewModelScope.launch {
            runCatching { repository.annuler(id, motif) }
                .onSuccess { maj ->
                    _state.update { s ->
                        s.copy(rendezVous = s.rendezVous.map { if (it.id == id) maj else it })
                    }
                }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
        }
    }

    /**
     * Déclare la visite effectuée.
     *
     * Le serveur distingue qui appelle : confirmée par le locataire, la clôture
     * libère les frais séquestrés ; déclarée par l'annonceur, elle appelle
     * l'arbitrage d'un administrateur.
     */
    /** Dépose un avis (note 1–5 et commentaire) sur une visite effectuée. */
    fun deposerAvis(rendezVousId: Long, note: Int, commentaire: String?) {
        viewModelScope.launch {
            runCatching { repository.deposerAvis(rendezVousId, note, commentaire) }
                .onSuccess { _state.update { it.copy(messagePaiement = "Merci pour votre avis.") } }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
        }
    }

        fun terminer(id: Long) {
        viewModelScope.launch {
            runCatching { repository.terminer(id) }
                .onSuccess { maj ->
                    _state.update { s ->
                        s.copy(
                            rendezVous = s.rendezVous.map { if (it.id == id) maj else it },
                            messagePaiement = "Visite confirmée."
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
        }
    }

    /** Règle les frais : création du paiement puis passage par la passerelle. */
    fun payer(rendezVousId: Long, moyen: String) {
        _state.update { it.copy(paiementEnCours = rendezVousId, messagePaiement = null) }
        viewModelScope.launch {
            runCatching {
                val paiement = repository.initierPaiement(rendezVousId)
                val id = paiement.id ?: error("Paiement sans identifiant")
                repository.reglerPaiement(id, moyen)
            }.onSuccess { regle ->
                _state.update { s ->
                    s.copy(
                        paiementEnCours = null,
                        paiements = s.paiements + (rendezVousId to regle),
                        // Le reçu est conservé : la référence est la seule preuve dont
                        // dispose l'utilisateur si la visite est contestée.
                        recu = regle
                    )
                }
                charger()
            }.onFailure { e ->
                _state.update {
                    it.copy(paiementEnCours = null, messagePaiement = e.toFrenchMessage())
                }
            }
        }
    }

    fun effacerMessage() = _state.update { it.copy(messagePaiement = null, error = null) }

    fun fermerRecu() = _state.update { it.copy(recu = null) }
}
