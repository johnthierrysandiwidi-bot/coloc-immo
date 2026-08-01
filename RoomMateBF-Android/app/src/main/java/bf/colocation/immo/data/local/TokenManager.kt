package bf.colocation.immo.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

/**
 * Stocke l'access token et le refresh token de façon persistante et chiffrée.
 *
 * Les valeurs sont chiffrées par [CoffreCle] avant d'être écrites dans DataStore, et
 * déchiffrées à la lecture. L'API publique est inchangée : les appelants manipulent
 * toujours des jetons en clair, le chiffrement reste interne. Une valeur illisible
 * (ancien format en clair, clé régénérée) est traitée comme une absence de jeton, ce
 * qui provoque simplement une reconnexion — sans plantage.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")

    private fun lire(brut: String?): String? = brut?.let { CoffreCle.dechiffrer(it) }

    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { lire(it[accessKey]) }

    /** Indique si un utilisateur est connecté (jeton présent et déchiffrable). */
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { !lire(it[accessKey]).isNullOrBlank() }

    suspend fun saveTokens(access: String, refresh: String?) {
        context.dataStore.edit {
            it[accessKey] = CoffreCle.chiffrer(access)
            if (refresh != null) it[refreshKey] = CoffreCle.chiffrer(refresh)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /** Lectures synchrones utilisées par les intercepteurs OkHttp (hors coroutine). */
    fun accessTokenBlocking(): String? = runBlocking { lire(context.dataStore.data.first()[accessKey]) }
    fun refreshTokenBlocking(): String? = runBlocking { lire(context.dataStore.data.first()[refreshKey]) }
    suspend fun refreshToken(): String? = lire(context.dataStore.data.first()[refreshKey])
}
