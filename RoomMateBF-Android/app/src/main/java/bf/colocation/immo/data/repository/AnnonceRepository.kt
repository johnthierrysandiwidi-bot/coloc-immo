package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.AnnonceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnonceRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun liste(
        page: Int,
        size: Int,
        recherche: String?,
        type: String?
    ): Paged<AnnonceDto> {
        val resp = api.annonces(
            page = page,
            size = size,
            titre = recherche?.takeUnless { it.isBlank() },
            type = type
        )
        val total = resp.headers()["X-Total-Count"]?.toIntOrNull()
            ?: resp.body()?.size ?: 0
        return Paged(resp.body().orEmpty(), total)
    }

    suspend fun detail(id: Long): AnnonceDto = api.annonce(id)
}
