package bf.colocation.immo.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.Constants
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.dto.NotificationDto
import bf.colocation.immo.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init { charger() }

    fun charger() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.liste(0, Constants.DEFAULT_PAGE_SIZE) }
                .onSuccess { p -> _state.update { it.copy(loading = false, notifications = p.items) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toFrenchMessage()) } }
        }
    }
}
