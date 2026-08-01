package bf.colocation.immo.ui.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.AccountDto
import bf.colocation.immo.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilUiState(
    val compte: AccountDto? = null,
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfilUiState())
    val state: StateFlow<ProfilUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { authRepository.account() }
                .onSuccess { c -> _state.update { it.copy(loading = false, compte = c) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toFrenchMessage()) } }
        }
    }

    fun deconnexion(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
