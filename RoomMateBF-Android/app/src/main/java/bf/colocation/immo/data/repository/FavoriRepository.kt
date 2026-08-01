package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.FavoriDto
import bf.colocation.immo.data.remote.dto.FavoriRequest
import bf.colocation.immo.data.remote.dto.IdRef
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun liste(page: Int, size: Int): Paged<FavoriDto> {
        val resp = api.favoris(page, size)
        val total = resp.headers()["X-Total-Count"]?.toIntOrNull() ?: resp.body()?.size ?: 0
        return Paged(resp.body().orEmpty(), total)
    }

    suspend fun ajouter(annonceId: Long, utilisateurId: Long): FavoriDto =
        api.ajouterFavori(FavoriRequest(annonce = IdRef(annonceId), utilisateur = IdRef(utilisateurId)))

    suspend fun supprimer(favoriId: Long) {
        api.supprimerFavori(favoriId)
    }
}
