package bf.colocation.immo.ui.publication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.*
import bf.colocation.immo.data.repository.AuthRepository
import bf.colocation.immo.data.repository.PublicationRepository
import bf.colocation.immo.data.repository.ReferentielRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicationUiState(
    // Identité
    val userId: Long? = null,
    val peutPublier: Boolean = false,
    // Bien
    val utiliserBienExistant: Boolean = false,
    val biens: List<ImmobilierDto> = emptyList(),
    val bienChoisiId: Long? = null,
    val nomBien: String = "",
    val adresse: String = "",
    val surface: String = "",
    val chambres: String = "",
    val sallesBain: String = "",
    val salons: String = "",
    val meuble: Boolean = false,
    val garage: Boolean = false,
    val localiteId: Long? = null,
    val quartierId: Long? = null,
    val typeImmobilierId: Long? = null,
    // Annonce
    val titre: String = "",
    val description: String = "",
    val type: TypeAnnonceUi = TypeAnnonceUi.LOCATION,
    val prix: String = "",
    val publierImmediatement: Boolean = true,
    // Référentiel
    val localites: List<LocaliteDto> = emptyList(),
    val quartiers: List<QuartierDto> = emptyList(),
    val types: List<TypeImmobilierDto> = emptyList(),
    // Retour
    val loading: Boolean = false,
    val error: String? = null,
    val succes: String? = null
) {
    val quartiersFiltres: List<QuartierDto>
        get() = quartiers.filter { localiteId == null || it.localite?.id == localiteId }
}

@HiltViewModel
class PublierAnnonceViewModel @Inject constructor(
    private val publication: PublicationRepository,
    private val referentiel: ReferentielRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PublicationUiState())
    val state: StateFlow<PublicationUiState> = _state.asStateFlow()

    init { initialiser() }

    private fun initialiser() {
        viewModelScope.launch {
            runCatching { auth.account() }.onSuccess { compte ->
                // Seuls propriétaires et démarcheurs publient (EF-01.1 / RG08).
                val autorise = compte.authorities.any {
                    it == "ROLE_PROPRIETAIRE" || it == "ROLE_DEMARCHEUR" || it == "ROLE_ADMIN"
                }
                _state.update { it.copy(userId = compte.id, peutPublier = autorise) }
                compte.id?.let { chargerBiens(it) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.toFrenchMessage()) }
            }
            runCatching {
                Triple(referentiel.localites(), referentiel.quartiers(), referentiel.typesImmobilier())
            }.onSuccess { (l, q, t) ->
                _state.update { it.copy(localites = l, quartiers = q, types = t) }
            }
        }
    }

    private fun chargerBiens(userId: Long) {
        viewModelScope.launch {
            runCatching { publication.mesBiens(userId) }
                .onSuccess { liste -> _state.update { it.copy(biens = liste) } }
        }
    }

    // --- Saisie ---
    fun onUtiliserBienExistant(v: Boolean) = _state.update { it.copy(utiliserBienExistant = v, error = null) }
    fun onBienChoisi(id: Long?) = _state.update { it.copy(bienChoisiId = id) }
    fun onNomBien(v: String) = _state.update { it.copy(nomBien = v) }
    fun onAdresse(v: String) = _state.update { it.copy(adresse = v) }
    fun onSurface(v: String) = _state.update { s -> s.copy(surface = v.filter { c -> c.isDigit() || c == '.' }) }
    fun onChambres(v: String) = _state.update { s -> s.copy(chambres = v.filter { c -> c.isDigit() }) }
    fun onSallesBain(v: String) = _state.update { s -> s.copy(sallesBain = v.filter { c -> c.isDigit() }) }
    fun onSalons(v: String) = _state.update { s -> s.copy(salons = v.filter { c -> c.isDigit() }) }
    fun onMeuble(v: Boolean) = _state.update { it.copy(meuble = v) }
    fun onGarage(v: Boolean) = _state.update { it.copy(garage = v) }
    fun onLocalite(id: Long?) = _state.update { it.copy(localiteId = id, quartierId = null) }
    fun onQuartier(id: Long?) = _state.update { it.copy(quartierId = id) }
    fun onTypeImmobilier(id: Long?) = _state.update { it.copy(typeImmobilierId = id) }
    fun onTitre(v: String) = _state.update { it.copy(titre = v) }
    fun onDescription(v: String) = _state.update { it.copy(description = v) }
    fun onType(v: TypeAnnonceUi) = _state.update { it.copy(type = v) }
    fun onPrix(v: String) = _state.update { s -> s.copy(prix = v.filter { c -> c.isDigit() }) }
    fun onPublierImmediatement(v: Boolean) = _state.update { it.copy(publierImmediatement = v) }
    fun effacerMessages() = _state.update { it.copy(error = null, succes = null) }

    private fun valider(s: PublicationUiState): String? = when {
        s.userId == null -> "Session incomplète. Reconnecte-toi."
        s.titre.isBlank() -> "Le titre de l'annonce est obligatoire."
        s.titre.length > 150 -> "Le titre ne doit pas dépasser 150 caractères."
        s.prix.toDoubleOrNull() == null || s.prix.toDouble() <= 0 -> "Indique un prix en FCFA."
        s.utiliserBienExistant && s.bienChoisiId == null -> "Choisis le bien concerné."
        !s.utiliserBienExistant && s.nomBien.isBlank() ->
            "Donne un nom au bien (ex. « Villa 3 pièces à Karpala »)."
        !s.utiliserBienExistant && s.typeImmobilierId == null -> "Choisis le type de bien."
        !s.utiliserBienExistant && s.localiteId == null -> "Choisis la ville."
        else -> null
    }

    fun enregistrer() {
        val s = _state.value
        val probleme = valider(s)
        if (probleme != null) {
            _state.update { it.copy(error = probleme) }
            return
        }

        _state.update { it.copy(loading = true, error = null, succes = null) }
        viewModelScope.launch {
            runCatching {
                val userId = s.userId!!

                // 1) Le bien : réutilisé ou créé à la volée.
                val bienId = if (s.utiliserBienExistant) {
                    s.bienChoisiId!!
                } else {
                    publication.creerBien(
                        proprietaireId = userId,
                        nom = s.nomBien,
                        description = s.description,
                        adresse = s.adresse,
                        surface = s.surface.toDoubleOrNull(),
                        nombreChambres = s.chambres.toIntOrNull(),
                        nombreSallesBain = s.sallesBain.toIntOrNull(),
                        nombreSalons = s.salons.toIntOrNull(),
                        meuble = s.meuble,
                        garage = s.garage,
                        localiteId = s.localiteId,
                        quartierId = s.quartierId,
                        typeImmobilierId = s.typeImmobilierId
                    ).id ?: error("Le bien a été créé sans identifiant.")
                }

                // 2) L'annonce, en brouillon.
                val annonce = publication.creerAnnonce(
                    auteurId = userId,
                    immobilierId = bienId,
                    titre = s.titre,
                    contenu = s.description,
                    type = s.type.code,
                    prix = s.prix.toDouble()
                )

                // 3) Publication effective si demandée.
                if (s.publierImmediatement) publication.publier(annonce.id) else annonce
            }.onSuccess {
                _state.update {
                    it.copy(
                        loading = false,
                        succes = if (s.publierImmediatement)
                            "Annonce publiée. Elle est visible dans le catalogue."
                        else
                            "Annonce enregistrée en brouillon. Tu pourras la publier plus tard.",
                        // On vide la saisie pour éviter un doublon involontaire.
                        titre = "", description = "", prix = "", nomBien = "", adresse = "",
                        surface = "", chambres = "", sallesBain = "", salons = "",
                        bienChoisiId = null
                    )
                }
                s.userId?.let { chargerBiens(it) }
            }.onFailure { e ->
                _state.update { it.copy(loading = false, error = e.toFrenchMessage()) }
            }
        }
    }
}
