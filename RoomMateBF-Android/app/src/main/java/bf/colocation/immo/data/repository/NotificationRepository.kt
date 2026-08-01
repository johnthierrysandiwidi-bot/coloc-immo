package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.NotificationDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun liste(page: Int, size: Int): Paged<NotificationDto> {
        val resp = api.notifications(page, size)
        val total = resp.headers()["X-Total-Count"]?.toIntOrNull() ?: resp.body()?.size ?: 0
        return Paged(resp.body().orEmpty(), total)
    }
}
