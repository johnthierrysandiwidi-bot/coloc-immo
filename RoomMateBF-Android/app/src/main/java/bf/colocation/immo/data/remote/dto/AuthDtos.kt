package bf.colocation.immo.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = true
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class JwtTokenDto(
    @Json(name = "id_token") val idToken: String,
    @Json(name = "refresh_token") val refreshToken: String?
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val login: String,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    /** Recueilli à l'inscription, conformément au rapport §4.3.1. */
    val telephone: String?,
    val password: String,
    val langKey: String = "fr",
    /** Profil choisi : locataire, propriétaire ou démarcheur. */
    val authorities: List<String> = listOf("ROLE_UTILISATEUR")
)

/** Profils proposés à l'inscription. Le rôle administrateur ne s'auto-attribue pas. */
enum class ProfilInscription(val role: String, val libelle: String) {
    UTILISATEUR("ROLE_UTILISATEUR", "Je cherche un logement"),
    PROPRIETAIRE("ROLE_PROPRIETAIRE", "Je suis propriétaire"),
    DEMARCHEUR("ROLE_DEMARCHEUR", "Je suis démarcheur")
}

@JsonClass(generateAdapter = true)
data class AccountDto(
    val id: Long?,
    val login: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val imageUrl: String?,
    val activated: Boolean = false,
    val langKey: String?,
    val authorities: List<String> = emptyList()
) {
    val displayName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { login }
}
