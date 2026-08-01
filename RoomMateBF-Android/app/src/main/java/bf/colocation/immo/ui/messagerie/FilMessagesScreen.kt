package bf.colocation.immo.ui.messagerie

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Fil d'une conversation : bulles à droite pour moi, à gauche pour l'autre, et champ
 * d'envoi en bas. Le fil se rafraîchit périodiquement via le ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilMessagesScreen(
    conversationId: Long,
    monId: Long,
    titre: String,
    onRetour: () -> Unit,
    viewModel: MessagerieViewModel
) {
    val state by viewModel.state.collectAsState()
    var saisie by remember { mutableStateOf("") }
    val liste = rememberLazyListState()

    LaunchedEffect(conversationId) { viewModel.observerConversation(conversationId) }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) liste.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titre) },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = saisie,
                        onValueChange = { saisie = it },
                        placeholder = { Text("Votre message…") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.envoyer(conversationId, saisie)
                            saisie = ""
                        },
                        enabled = saisie.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                    }
                }
            }
        }
    ) { pad ->
        LazyColumn(
            state = liste,
            modifier = Modifier.padding(pad).fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(state.messages, key = { it.id }) { m ->
                val deMoi = m.expediteurId == monId
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (deMoi) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (deMoi) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            m.contenu,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (deMoi) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
