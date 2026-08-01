package bf.colocation.immo.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnnonceDto(
    val id: Long,
    val titre: String?,
    val contenu: String?,
    val type: String?,            // VENTE | LOCATION | COLOCATION
    val prix: Double?,
    val nombreVues: Int?,
    val datePublication: String?,
    val dateExpiration: String?,
    val statut: String?,          // BROUILLON | PUBLIEE | ...
    val immobilier: ImmobilierDto?,
    val auteur: UserDto?,
    val photoUrl: String?,
    val photos: List<String> = emptyList()
) {
    /** Toutes les images disponibles (photoUrl + photos), dédupliquées. */
    val toutesLesImages: List<String>
        get() = (listOfNotNull(photoUrl) + photos).distinct()
}

@JsonClass(generateAdapter = true)
data class ImmobilierDto(
    val id: Long?,
    val nom: String?,
    val description: String?,
    val adresse: String?,
    val surface: Double?,
    val nombrePieces: Int?,
    val nombreChambres: Int?,
    val nombreSallesBain: Int?,
    val nombreSalons: Int?,
    val garage: Boolean?,
    val piscine: Boolean?,
    val meuble: Boolean?,
    val disponibleA: String?,
    val statut: String?,
    val latitude: Double?,
    val longitude: Double?,
    val localite: LocaliteDto?,
    val quartier: QuartierDto?,
    val typeImmobilier: TypeImmobilierDto?
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Long?,
    val login: String?
)

@JsonClass(generateAdapter = true)
data class LocaliteDto(val id: Long?, val nom: String?)

@JsonClass(generateAdapter = true)
data class QuartierDto(val id: Long?, val nom: String?, val localite: LocaliteDto?)

@JsonClass(generateAdapter = true)
data class TypeImmobilierDto(val id: Long?, val nom: String?)
