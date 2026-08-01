package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.*
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Publication d'une annonce. Le backend impose l'enchaînement suivant :
 *   1. un Immobilier doit exister (relation @NotNull sur AnnonceDTO) ;
 *   2. l'annonce est créée en BROUILLON ;
 *   3. PATCH /annonces/{id}/publier applique les règles métier
 *      (profil validé, quota, date d'expiration).
 * Court-circuiter l'étape 3 laisserait l'annonce invisible du catalogue.
 */
@Singleton
class PublicationRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun mesBiens(proprietaireId: Long): List<ImmobilierDto> =
        api.mesBiens(proprietaireId).body().orEmpty()

    suspend fun mesAnnonces(auteurId: Long): List<AnnonceDto> =
        api.mesAnnonces(auteurId).body().orEmpty()

    suspend fun creerBien(
        proprietaireId: Long,
        nom: String,
        description: String?,
        adresse: String?,
        surface: Double?,
        nombreChambres: Int?,
        nombreSallesBain: Int?,
        nombreSalons: Int?,
        meuble: Boolean,
        garage: Boolean,
        localiteId: Long?,
        quartierId: Long?,
        typeImmobilierId: Long?
    ): ImmobilierDto = api.creerImmobilier(
        ImmobilierRequest(
            nom = nom.trim(),
            description = description?.trim().takeUnless { it.isNullOrBlank() },
            adresse = adresse?.trim().takeUnless { it.isNullOrBlank() },
            surface = surface,
            nombrePieces = nombreChambres,
            nombreChambres = nombreChambres,
            nombreSallesBain = nombreSallesBain,
            nombreSalons = nombreSalons,
            garage = garage,
            piscine = false,
            meuble = meuble,
            dateCreation = Instant.now().toString(),
            proprietaire = IdRef(proprietaireId),
            localite = localiteId?.let { IdRef(it) },
            quartier = quartierId?.let { IdRef(it) },
            typeImmobilier = typeImmobilierId?.let { IdRef(it) }
        )
    )

    suspend fun creerAnnonce(
        auteurId: Long,
        immobilierId: Long,
        titre: String,
        contenu: String?,
        type: String,
        prix: Double
    ): AnnonceDto = api.creerAnnonce(
        AnnonceRequest(
            titre = titre.trim(),
            contenu = contenu?.trim().takeUnless { it.isNullOrBlank() },
            type = type,
            prix = prix,
            immobilier = IdRef(immobilierId),
            auteur = IdRef(auteurId)
        )
    )

    suspend fun publier(annonceId: Long): AnnonceDto = api.publierAnnonce(annonceId)
}
