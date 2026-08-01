package bf.colocation.immo.data.remote

import bf.colocation.immo.core.Constants
import bf.colocation.immo.data.local.TokenManager
import bf.colocation.immo.data.remote.dto.JwtTokenDto
import bf.colocation.immo.data.remote.dto.RefreshRequest
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sur une réponse 401, tente un refresh du token puis rejoue la requête.
 * Utilise un OkHttpClient dédié (sans authenticator) pour éviter les boucles.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val moshi: Moshi
) : Authenticator {

    private val bareClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Si on a déjà réessayé, on abandonne (évite la boucle infinie).
        if (responseCount(response) >= 2) return null

        val refresh = tokenManager.refreshTokenBlocking() ?: return null
        val newAccess = runBlocking { tryRefresh(refresh) } ?: run {
            runBlocking { tokenManager.clear() }
            return null
        }
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun tryRefresh(refreshToken: String): String? {
        return try {
            val adapter = moshi.adapter(RefreshRequest::class.java)
            val body = adapter.toJson(RefreshRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url(Constants.BASE_URL + "api/auth/refresh")
                .post(body)
                .build()
            bareClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val json = resp.body?.string() ?: return null
                val token = moshi.adapter(JwtTokenDto::class.java).fromJson(json) ?: return null
                runBlocking { tokenManager.saveTokens(token.idToken, token.refreshToken) }
                token.idToken
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
