package bf.colocation.immo.data.remote

import bf.colocation.immo.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Ajoute le header Authorization: Bearer <token> sauf sur les routes publiques. */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val publicPaths = listOf(
        "api/authenticate", "api/auth/refresh", "api/register", "api/activate"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')
        if (publicPaths.any { path.startsWith(it) }) return chain.proceed(request)

        val token = tokenManager.accessTokenBlocking()
        val newRequest = if (!token.isNullOrBlank()) {
            request.newBuilder().header("Authorization", "Bearer $token").build()
        } else request
        return chain.proceed(newRequest)
    }
}
