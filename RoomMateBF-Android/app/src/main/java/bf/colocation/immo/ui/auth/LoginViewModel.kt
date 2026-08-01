package bf.colocation.immo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsername(v: String) = _state.update { it.copy(username = v, error = null) }
    fun onPassword(v: String) = _state.update { it.copy(password = v, error = null) }

    fun connexion() {
        val s = _state.value
        if (s.username.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Renseigne ton identifiant et ton mot de passe.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { authRepository.login(s.username, s.password) }
                .onSuccess { _state.update { st -> st.copy(loading = false, success = true) } }
                .onFailure { e ->
                    val msg = if ((e.message ?: "").contains("401")) "Identifiant ou mot de passe incorrect."
                    else e.toFrenchMessage()
                    _state.update { st -> st.copy(loading = false, error = msg) }
                }
        }
    }
}
