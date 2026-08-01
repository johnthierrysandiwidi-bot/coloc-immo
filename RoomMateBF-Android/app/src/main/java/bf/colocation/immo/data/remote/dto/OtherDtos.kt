package bf.colocation.immo.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoriDto(
    val id: Long?,
    val dateAjout: String?,
    val annonce: AnnonceDto?,
    val utilisateur: UserDto?
)

/** Corps minimal pour créer un favori. */
@JsonClass(generateAdapter = true)
data class FavoriRequest(
    val annonce: IdRef,
    val utilisateur: IdRef
)

@JsonClass(generateAdapter = true)
data class IdRef(val id: Long)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: Long,
    val type: String?,
    val titre: String?,
    val message: String?,
    val lien: String?,
    val lue: Boolean?,
    val dateCreation: String?
)

@JsonClass(generateAdapter = true)
data class RendezVousDto(
    val id: Long?,
    val dateHeure: String?,
    val dateReportee: String?,
    val lieu: String?,
    val contenu: String?,
    val motif: String?,
    val statut: String?,
    val annonce: AnnonceDto?,
    val demandeur: UserDto?
)

/** POST /api/rendez-vous/demander */
@JsonClass(generateAdapter = true)
data class DemandeVisiteRequest(
    val annonceId: Long,
    val dateSouhaitee: String,   // Instant ISO-8601, ex "2025-07-20T14:00:00Z"
    val message: String?
)

@JsonClass(generateAdapter = true)
data class MotifRequest(val motif: String?)

@JsonClass(generateAdapter = true)
data class DeviceTokenRequest(
    val token: String,
    val plateforme: String,
    val utilisateur: IdRef?
)

// ---- Paiement des frais de visite ----

@JsonClass(generateAdapter = true)
data class PaiementDto(
    val id: Long?,
    val montant: Double?,
    val statut: String?,      // EN_ATTENTE, EN_SEQUESTRE, LIBERE, REMBOURSE
    val moyen: String?,       // ORANGE_MONEY, MOOV_MONEY, CARTE
    val reference: String?,
    val dateCreation: String?
)

/**
 * Moyens de règlement acceptés par la passerelle simulée du backend
 * (énumération MoyenPaiement côté serveur).
 */
enum class MoyenPaiement(val code: String, val libelle: String) {
    ORANGE_MONEY("ORANGE_MONEY", "Orange Money"),
    MOOV_MONEY("MOOV_MONEY", "Moov Money"),
    CARTE("CARTE", "Carte bancaire")
}
