package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.LocaliteDto
import bf.colocation.immo.data.remote.dto.QuartierDto
import bf.colocation.immo.data.remote.dto.TypeImmobilierDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Villes, quartiers et types de bien. Ces listes changent rarement :
 * on les met en cache mémoire pour éviter trois appels à chaque ouverture
 * du formulaire de publication ou de création d'alerte.
 */
@Singleton
class ReferentielRepository @Inject constructor(
    private val api: ApiService
) {
    private var cacheLocalites: List<LocaliteDto>? = null
    private var cacheQuartiers: List<QuartierDto>? = null
    private var cacheTypes: List<TypeImmobilierDto>? = null

    suspend fun localites(): List<LocaliteDto> =
        cacheLocalites ?: api.localites().body().orEmpty().also { cacheLocalites = it }

    suspend fun quartiers(): List<QuartierDto> =
        cacheQuartiers ?: api.quartiers().body().orEmpty().also { cacheQuartiers = it }

    suspend fun typesImmobilier(): List<TypeImmobilierDto> =
        cacheTypes ?: api.typesImmobilier().body().orEmpty().also { cacheTypes = it }

    /** Quartiers d'une ville donnée, ou tous si aucune ville n'est choisie. */
    suspend fun quartiersDe(localiteId: Long?): List<QuartierDto> =
        quartiers().filter { localiteId == null || it.localite?.id == localiteId }
}
