package bf.colocation.immo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.data.remote.dto.ProfilInscription
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.repository.AuthRepository
import bf.colocation.immo.data.repository.normaliserLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val login: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val telephone: String = "",
    val profil: ProfilInscription = ProfilInscription.UTILISATEUR,
    val conditions: Boolean = false,
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onLogin(v: String) = _state.update { it.copy(login = v.normaliserLogin(), error = null) }
    fun onEmail(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onFirstName(v: String) = _state.update { it.copy(firstName = v) }
    fun onLastName(v: String) = _state.update { it.copy(lastName = v) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirm(v: String) = _state.update { it.copy(confirm = v, error = null) }
    fun onTelephone(v: String) = _state.update { it.copy(telephone = v.filter { c -> c.isDigit() }.take(8), error = null) }
    fun onProfil(v: ProfilInscription) = _state.update { it.copy(profil = v, error = null) }
    fun onConditions(v: Boolean) = _state.update { it.copy(conditions = v, error = null) }

    fun inscription() {
        val s = _state.value
        val err = valider(s)
        if (err != null) { _state.update { it.copy(error = err) }; return }

        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                authRepository.register(
                    s.login, s.email, s.password, s.firstName, s.lastName,
                    s.telephone, s.profil.role
                )
            }.onSuccess {
                _state.update { st -> st.copy(loading = false, success = true) }
            }.onFailure { e ->
                _state.update { st -> st.copy(loading = false, error = e.message ?: e.toFrenchMessage()) }
            }
        }
    }

    // Le rapport §4.3.1 impose le recueil du nom, du prénom, du courriel, du
    // téléphone et du mot de passe, ainsi que l'acceptation des conditions.
    private fun valider(s: RegisterUiState): String? = when {
        s.lastName.isBlank() -> "Le nom est obligatoire."
        s.firstName.isBlank() -> "Le prénom est obligatoire."
        s.login.isBlank() -> "L'identifiant est obligatoire."
        s.login.length < 3 -> "L'identifiant doit faire au moins 3 caractères."
        !Regex("^[_.@a-z0-9-]+\$").matches(s.login) ->
            "Identifiant invalide : lettres, chiffres, . _ - @ uniquement."
        s.email.isBlank() || !Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$").matches(s.email.trim()) ->
            "Adresse email invalide."
        s.telephone.length != 8 -> "Le numéro de téléphone doit comporter 8 chiffres."
        s.password.length < 4 -> "Le mot de passe doit faire au moins 4 caractères."
        s.password != s.confirm -> "Les mots de passe ne correspondent pas."
        !s.conditions -> "Vous devez accepter les conditions d'utilisation."
        else -> null
    }
}
