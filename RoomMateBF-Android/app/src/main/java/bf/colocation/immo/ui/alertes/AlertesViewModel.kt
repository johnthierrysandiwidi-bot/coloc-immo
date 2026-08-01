package bf.colocation.immo.ui.alertes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.*
import bf.colocation.immo.data.repository.AlerteRepository
import bf.colocation.immo.data.repository.AuthRepository
import bf.colocation.immo.data.repository.ReferentielRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormulaireAlerte(
    val titre: String = "",
    val typeAnnonce: TypeAnnonceUi? = null,
    val prixMin: String = "",
    val prixMax: String = "",
    val surfaceMin: String = "",
    val chambresMin: String = "",
    val meubleUniquement: Boolean = false,
    val frequence: FrequenceUi = FrequenceUi.IMMEDIATE,
    val localiteId: Long? = null,
    val quartierId: Long? = null,
    val typeImmobilierId: Long? = null
)

data class AlertesUiState(
    val userId: Long? = null,
    val alertes: List<AlerteDto> = emptyList(),
    val localites: List<LocaliteDto> = emptyList(),
    val quartiers: List<QuartierDto> = emptyList(),
    val types: List<TypeImmobilierDto> = emptyList(),
    val formulaire: FormulaireAlerte = FormulaireAlerte(),
    val formulaireOuvert: Boolean = false,
    val loading: Boolean = true,
    val enregistrement: Boolean = false,
    val error: String? = null,
    val succes: String? = null
) {
    val quartiersFiltres: List<QuartierDto>
        get() = quartiers.filter {
            formulaire.localiteId == null || it.localite?.id == formulaire.localiteId
        }
}

@HiltViewModel
class AlertesViewModel @Inject constructor(
    private val repository: AlerteRepository,
    private val referentiel: ReferentielRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlertesUiState())
    val state: StateFlow<AlertesUiState> = _state.asStateFlow()

    init {
        charger()
        viewModelScope.launch {
            runCatching { auth.account() }
                .onSuccess { c -> _state.update { it.copy(userId = c.id) } }
            runCatching {
                Triple(referentiel.localites(), referentiel.quartiers(), referentiel.typesImmobilier())
            }.onSuccess { (l, q, t) ->
                _state.update { it.copy(localites = l, quartiers = q, types = t) }
            }
        }
    }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.mesAlertes() }
                .onSuccess { liste -> _state.update { it.copy(loading = false, alertes = liste) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toFrenchMessage()) } }
        }
    }

    fun ouvrirFormulaire() =
        _state.update { it.copy(formulaireOuvert = true, formulaire = FormulaireAlerte(), error = null) }

    fun fermerFormulaire() = _state.update { it.copy(formulaireOuvert = false) }

    fun effacerMessages() = _state.update { it.copy(error = null, succes = null) }

    private fun maj(bloc: (FormulaireAlerte) -> FormulaireAlerte) =
        _state.update { it.copy(formulaire = bloc(it.formulaire), error = null) }

    fun onTitre(v: String) = maj { it.copy(titre = v) }
    fun onTypeAnnonce(v: TypeAnnonceUi?) = maj { it.copy(typeAnnonce = v) }
    fun onPrixMin(v: String) = maj { f -> f.copy(prixMin = v.filter { c -> c.isDigit() }) }
    fun onPrixMax(v: String) = maj { f -> f.copy(prixMax = v.filter { c -> c.isDigit() }) }
    fun onSurfaceMin(v: String) = maj { f -> f.copy(surfaceMin = v.filter { c -> c.isDigit() }) }
    fun onChambresMin(v: String) = maj { f -> f.copy(chambresMin = v.filter { c -> c.isDigit() }) }
    fun onMeuble(v: Boolean) = maj { it.copy(meubleUniquement = v) }
    fun onFrequence(v: FrequenceUi) = maj { it.copy(frequence = v) }
    fun onLocalite(id: Long?) = maj { it.copy(localiteId = id, quartierId = null) }
    fun onQuartier(id: Long?) = maj { it.copy(quartierId = id) }
    fun onTypeImmobilier(id: Long?) = maj { it.copy(typeImmobilierId = id) }

    fun enregistrer() {
        val s = _state.value
        val f = s.formulaire

        val erreur = when {
            s.userId == null -> "Session incomplète. Reconnecte-toi."
            f.titre.isBlank() -> "Donne un nom à ton alerte (ex. « Studio à Karpala »)."
            f.titre.length > 150 -> "Le nom de l'alerte est trop long."
            f.prixMin.toDoubleOrNull() != null && f.prixMax.toDoubleOrNull() != null &&
                f.prixMax.toDouble() < f.prixMin.toDouble() ->
                "Le prix maximum doit dépasser le prix minimum."
            else -> null
        }
        if (erreur != null) {
            _state.update { it.copy(error = erreur) }
            return
        }

        _state.update { it.copy(enregistrement = true, error = null) }
        viewModelScope.launch {
            runCatching {
                repository.creer(
                    titulaireId = s.userId!!,
                    titre = f.titre,
                    typeAnnonce = f.typeAnnonce?.code,
                    prixMin = f.prixMin.toDoubleOrNull(),
                    prixMax = f.prixMax.toDoubleOrNull(),
                    surfaceMin = f.surfaceMin.toDoubleOrNull(),
                    nombreChambresMin = f.chambresMin.toIntOrNull(),
                    meubleUniquement = f.meubleUniquement,
                    frequence = f.frequence.code,
                    localiteId = f.localiteId,
                    quartierId = f.quartierId,
                    typeImmobilierId = f.typeImmobilierId
                )
            }.onSuccess {
                _state.update {
                    it.copy(
                        enregistrement = false,
                        formulaireOuvert = false,
                        succes = "Alerte créée. Tu seras prévenu dès qu'une annonce y correspondra."
                    )
                }
                charger()
            }.onFailure { e ->
                _state.update { it.copy(enregistrement = false, error = e.toFrenchMessage()) }
            }
        }
    }

    fun supprimer(id: Long) {
        viewModelScope.launch {
            runCatching { repository.supprimer(id) }
                .onSuccess {
                    _state.update { s -> s.copy(alertes = s.alertes.filterNot { it.id == id }) }
                }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
        }
    }
}
