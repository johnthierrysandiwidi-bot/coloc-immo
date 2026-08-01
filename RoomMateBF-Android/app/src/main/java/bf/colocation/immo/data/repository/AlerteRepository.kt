package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.AlerteDto
import bf.colocation.immo.data.remote.dto.AlerteRequest
import bf.colocation.immo.data.remote.dto.IdRef
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Alertes de recherche. Le serveur filtre déjà la liste sur le titulaire
 * connecté : aucun critère n'est envoyé depuis le téléphone.
 */
@Singleton
class AlerteRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun mesAlertes(): List<AlerteDto> = api.alertes().body().orEmpty()

    suspend fun creer(
        titulaireId: Long,
        titre: String,
        typeAnnonce: String?,
        prixMin: Double?,
        prixMax: Double?,
        surfaceMin: Double?,
        nombreChambresMin: Int?,
        meubleUniquement: Boolean,
        frequence: String,
        localiteId: Long?,
        quartierId: Long?,
        typeImmobilierId: Long?
    ): AlerteDto = api.creerAlerte(
        AlerteRequest(
            titre = titre.trim(),
            typeAnnonce = typeAnnonce,
            prixMin = prixMin,
            prixMax = prixMax,
            surfaceMin = surfaceMin,
            nombreChambresMin = nombreChambresMin,
            meubleUniquement = meubleUniquement,
            frequence = frequence,
            titulaire = IdRef(titulaireId),
            localite = localiteId?.let { IdRef(it) },
            quartier = quartierId?.let { IdRef(it) },
            typeImmobilier = typeImmobilierId?.let { IdRef(it) }
        )
    )

    suspend fun supprimer(id: Long) {
        val resp = api.supprimerAlerte(id)
        if (!resp.isSuccessful) error("Suppression refusée (code ${resp.code()}).")
    }
}
