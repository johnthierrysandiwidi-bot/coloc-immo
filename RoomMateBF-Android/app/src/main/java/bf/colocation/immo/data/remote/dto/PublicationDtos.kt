package bf.colocation.immo.data.remote.dto

import com.squareup.moshi.JsonClass

/** Corps envoyé à POST /api/immobiliers (ImmobilierDTO côté serveur). */
@JsonClass(generateAdapter = true)
data class ImmobilierRequest(
    val nom: String,
    val description: String?,
    val adresse: String?,
    val surface: Double?,
    val nombrePieces: Int?,
    val nombreChambres: Int?,
    val nombreSallesBain: Int?,
    val nombreSalons: Int?,
    val garage: Boolean = false,
    val piscine: Boolean = false,
    val meuble: Boolean = false,
    /** StatutBien : le bien est disponible dès sa création. */
    val statut: String = "DISPONIBLE",
    val dateCreation: String,
    val proprietaire: IdRef,
    val localite: IdRef?,
    val quartier: IdRef?,
    val typeImmobilier: IdRef?
)

/** Corps envoyé à POST /api/annonces (AnnonceDTO côté serveur). */
@JsonClass(generateAdapter = true)
data class AnnonceRequest(
    val titre: String,
    val contenu: String?,
    /** TypeAnnonce : LOCATION | VENTE | COLOCATION */
    val type: String,
    val prix: Double,
    val nombreVues: Int = 0,
    /** StatutAnnonce : on crée en brouillon, la publication est une action séparée. */
    val statut: String = "BROUILLON",
    val immobilier: IdRef,
    val auteur: IdRef
)

/** Alerte de recherche (entité Alerte). */
@JsonClass(generateAdapter = true)
data class AlerteDto(
    val id: Long?,
    val titre: String?,
    val contenu: String?,
    val typeAnnonce: String?,
    val prixMin: Double?,
    val prixMax: Double?,
    val surfaceMin: Double?,
    val nombreChambresMin: Int?,
    val meubleUniquement: Boolean?,
    val active: Boolean = true,
    val frequence: String?,
    val localite: LocaliteDto?,
    val quartier: QuartierDto?,
    val typeImmobilier: TypeImmobilierDto?
)

@JsonClass(generateAdapter = true)
data class AlerteRequest(
    val titre: String,
    val typeAnnonce: String?,
    val prixMin: Double?,
    val prixMax: Double?,
    val surfaceMin: Double?,
    val nombreChambresMin: Int?,
    val meubleUniquement: Boolean = false,
    val active: Boolean = true,
    /** FrequenceAlerte : IMMEDIATE | QUOTIDIENNE | HEBDOMADAIRE */
    val frequence: String = "IMMEDIATE",
    val titulaire: IdRef,
    val localite: IdRef?,
    val quartier: IdRef?,
    val typeImmobilier: IdRef?
)

/** Types d'annonce proposés à la publication et au filtrage des alertes. */
enum class TypeAnnonceUi(val code: String, val libelle: String) {
    LOCATION("LOCATION", "Location"),
    VENTE("VENTE", "Vente"),
    COLOCATION("COLOCATION", "Colocation")
}

enum class FrequenceUi(val code: String, val libelle: String) {
    IMMEDIATE("IMMEDIATE", "Dès qu'une annonce correspond"),
    QUOTIDIENNE("QUOTIDIENNE", "Une fois par jour"),
    HEBDOMADAIRE("HEBDOMADAIRE", "Une fois par semaine")
}


/** Dépôt d'un avis après une visite effectuée. */
data class AvisRequest(
    val note: Int,
    val commentaire: String?
)

data class AvisDto(
    val id: Long? = null,
    val note: Int,
    val commentaire: String? = null,
    val dateCreation: String? = null
)

/** Conversation vue depuis l'utilisateur courant (l'interlocuteur est relatif). */
data class ConversationDto(
    val id: Long,
    val dernierMessageLe: String? = null,
    val annonceId: Long? = null,
    val annonceTitre: String? = null,
    val interlocuteurId: Long? = null,
    val interlocuteurLogin: String? = null
)

data class MessageDto(
    val id: Long,
    val contenu: String,
    val dateEnvoi: String,
    val lu: Boolean = false,
    val expediteurId: Long,
    val expediteurLogin: String? = null
)

data class EnvoiMessageRequest(val contenu: String)
