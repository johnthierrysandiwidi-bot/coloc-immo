package bf.colocation.immo.data.repository

import bf.colocation.immo.data.local.TokenManager
import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedInFlow

    suspend fun login(username: String, password: String) {
        val token = api.login(LoginRequest(username.trim(), password))
        tokenManager.saveTokens(token.idToken, token.refreshToken)
    }

    suspend fun register(
        login: String,
        email: String,
        password: String,
        firstName: String?,
        lastName: String?,
        telephone: String?,
        role: String
    ) {
        // Le serveur impose @Pattern ^[_.@A-Za-z0-9-]+$ : un identifiant accentué
        // ou contenant une espace était refusé sans que l'utilisateur sache pourquoi.
        val identifiant = login.normaliserLogin()

        val resp = api.register(
            RegisterRequest(
                login = identifiant,
                email = email.trim().lowercase(),
                password = password,
                firstName = firstName?.trim().takeUnless { it.isNullOrBlank() },
                lastName = lastName?.trim().takeUnless { it.isNullOrBlank() },
                telephone = telephone?.filter { it.isDigit() }.takeUnless { it.isNullOrBlank() },
                authorities = listOf(role)
            )
        )
        if (!resp.isSuccessful) {
            error(messageErreur(resp.code(), resp.errorBody()?.string()))
        }
    }

    suspend fun account(): AccountDto = api.account()

    suspend fun logout() {
        runCatching { api.logout() }
        tokenManager.clear()
    }

    /**
     * L'ancien code traduisait TOUT code 400 par « mot de passe trop court ? »,
     * ce qui masquait la vraie cause (login déjà pris, email déjà utilisé,
     * identifiant invalide…). On lit désormais le corps ProblemDetail de JHipster.
     */
    private fun messageErreur(code: Int, corps: String?): String {
        val detail = corps?.let { texte ->
            runCatching {
                val json = JSONObject(texte)
                listOf("detail", "title", "message", "errorKey")
                    .firstNotNullOfOrNull { cle -> json.optString(cle).takeIf { it.isNotBlank() } }
            }.getOrNull()
        }

        return when {
            detail?.contains("Login name already used", true) == true ||
                detail?.contains("userexists", true) == true ->
                "Cet identifiant est déjà utilisé. Choisis-en un autre."
            detail?.contains("Email is already in use", true) == true ||
                detail?.contains("emailexists", true) == true ->
                "Cette adresse email est déjà associée à un compte."
            detail?.contains("Incorrect password", true) == true ->
                "Mot de passe refusé : entre 4 et 100 caractères."
            !detail.isNullOrBlank() -> detail
            code == 400 -> "Données refusées par le serveur (code 400)."
            code == 409 -> "Cet identifiant ou cet email est déjà utilisé."
            else -> "Inscription impossible (code $code)."
        }
    }
}

/** Retire accents et espaces, passe en minuscules : conforme au LOGIN_REGEX serveur. */
fun String.normaliserLogin(): String =
    java.text.Normalizer.normalize(this.trim(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .replace(Regex("[^_.@a-z0-9-]"), "")
