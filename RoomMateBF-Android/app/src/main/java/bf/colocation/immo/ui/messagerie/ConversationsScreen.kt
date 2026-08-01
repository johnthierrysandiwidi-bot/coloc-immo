package bf.colocation.immo.ui.messagerie

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/** Liste des conversations de l'utilisateur, la plus récemment active en tête. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onOuvrir: (Long) -> Unit,
    onRetour: () -> Unit,
    viewModel: MessagerieViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.chargerConversations() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                state.chargement -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.conversations.isEmpty() -> Text(
                    "Aucune conversation pour le moment.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.conversations, key = { it.id }) { c ->
                        ListItem(
                            headlineContent = { Text(c.interlocuteurLogin ?: "Interlocuteur") },
                            supportingContent = {
                                c.annonceTitre?.let {
                                    Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            },
                            leadingContent = {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text((c.interlocuteurLogin ?: "?").take(1).uppercase())
                                    }
                                }
                            },
                            modifier = Modifier.clickable { onOuvrir(c.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
