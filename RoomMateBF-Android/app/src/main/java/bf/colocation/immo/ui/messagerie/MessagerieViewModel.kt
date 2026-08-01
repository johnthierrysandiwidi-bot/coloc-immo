package bf.colocation.immo.ui.messagerie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bf.colocation.immo.core.toFrenchMessage
import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.ConversationDto
import bf.colocation.immo.data.remote.dto.MessageDto
import bf.colocation.immo.data.repository.MessagerieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagerieUiState(
    val conversations: List<ConversationDto> = emptyList(),
    val messages: List<MessageDto> = emptyList(),
    val chargement: Boolean = false,
    val monId: Long? = null,
    val error: String? = null
)

/**
 * Gère la liste des conversations et le fil ouvert. Le fil est rafraîchi par un
 * sondage léger (toutes les dix secondes) tant que l'écran l'observe.
 */
@HiltViewModel
class MessagerieViewModel @Inject constructor(
    private val repository: MessagerieRepository,
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(MessagerieUiState())
    val state: StateFlow<MessagerieUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { api.account() }.onSuccess { compte ->
                _state.update { it.copy(monId = compte.id) }
            }
        }
    }

    fun chargerConversations() {
        _state.update { it.copy(chargement = true) }
        viewModelScope.launch {
            runCatching { repository.conversations() }
                .onSuccess { liste -> _state.update { it.copy(conversations = liste, chargement = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage(), chargement = false) } }
        }
    }

    fun observerConversation(conversationId: Long) {
        viewModelScope.launch {
            while (isActive) {
                runCatching { repository.messages(conversationId) }
                    .onSuccess { msgs -> _state.update { it.copy(messages = msgs) } }
                    .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
                delay(10000)
            }
        }
    }

    fun envoyer(conversationId: Long, contenu: String) {
        if (contenu.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.envoyer(conversationId, contenu) }
                .onSuccess { m -> _state.update { it.copy(messages = it.messages + m) } }
                .onFailure { e -> _state.update { it.copy(error = e.toFrenchMessage()) } }
        }
    }
}
