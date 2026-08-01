package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.AvisDto
import bf.colocation.immo.data.remote.dto.AvisRequest
import bf.colocation.immo.data.remote.dto.DemandeVisiteRequest
import bf.colocation.immo.data.remote.dto.MotifRequest
import bf.colocation.immo.data.remote.dto.PaiementDto
import bf.colocation.immo.data.remote.dto.RendezVousDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RendezVousRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun demanderVisite(annonceId: Long, dateSouhaitee: String, message: String?): RendezVousDto =
        api.demanderVisite(DemandeVisiteRequest(annonceId, dateSouhaitee, message))

    /**
     * Rendez-vous de l'utilisateur connecté.
     *
     * Le serveur restreint lui-même la liste aux rendez-vous qui concernent l'appelant
     * (ceux qu'il a demandés et ceux portant sur ses annonces) : aucun filtre n'est
     * envoyé depuis le téléphone, où il serait facile à contourner.
     */
    suspend fun liste(page: Int, size: Int): Paged<RendezVousDto> {
        val resp = api.mesRendezVous(page, size)
        val total = resp.headers()["X-Total-Count"]?.toIntOrNull() ?: resp.body()?.size ?: 0
        return Paged(resp.body().orEmpty(), total)
    }

    /** Déclare la visite effectuée ; le serveur décide de la suite selon qui appelle. */
    suspend fun terminer(id: Long): RendezVousDto = api.terminerRendezVous(id)

    /** Dépose un avis (note et commentaire) sur la visite effectuée. */
    suspend fun deposerAvis(id: Long, note: Int, commentaire: String?): AvisDto =
        api.deposerAvis(id, AvisRequest(note = note, commentaire = commentaire?.trim()?.ifBlank { null }))

    suspend fun annuler(id: Long, motif: String?): RendezVousDto =
        api.annulerRendezVous(id, MotifRequest(motif))

    // ---- Frais de visite ----

    suspend fun fraisDeVisite(): Double = api.fraisDeVisite()

    suspend fun initierPaiement(rendezVousId: Long): PaiementDto =
        api.initierPaiement(rendezVousId)

    suspend fun reglerPaiement(paiementId: Long, moyen: String): PaiementDto =
        api.simulerReglement(paiementId, moyen)

    /**
     * État du règlement, ou null quand aucun paiement n'a été engagé : le backend
     * répond alors 204 avec un corps vide, que Retrofit ne saurait désérialiser.
     */
    suspend fun paiementDuRendezVous(rendezVousId: Long): PaiementDto? {
        val resp = api.paiementDuRendezVous(rendezVousId)
        return if (resp.isSuccessful) resp.body() else null
    }
}
