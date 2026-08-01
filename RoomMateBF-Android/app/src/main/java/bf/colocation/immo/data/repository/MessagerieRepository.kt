package bf.colocation.immo.data.repository

import bf.colocation.immo.data.remote.ApiService
import bf.colocation.immo.data.remote.dto.ConversationDto
import bf.colocation.immo.data.remote.dto.EnvoiMessageRequest
import bf.colocation.immo.data.remote.dto.MessageDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Messagerie interne. Le serveur cloisonne déjà chaque conversation à ses deux
 * participants ; le téléphone ne fait que présenter et envoyer.
 */
@Singleton
class MessagerieRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun conversations(): List<ConversationDto> = api.conversations()

    suspend fun ouvrirPourAnnonce(annonceId: Long): ConversationDto = api.ouvrirConversation(annonceId)

    suspend fun messages(conversationId: Long): List<MessageDto> = api.messages(conversationId)

    suspend fun envoyer(conversationId: Long, contenu: String): MessageDto =
        api.envoyerMessage(conversationId, EnvoiMessageRequest(contenu.trim()))

    suspend fun nombreNonLus(): Long = api.messagesNonLus()["nombre"] ?: 0L
}
