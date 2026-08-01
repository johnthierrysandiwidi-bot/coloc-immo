package bf.colocation.immo.ui.favoris

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.ui.annonces.AnnonceCard
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral

@Composable
fun FavorisScreen(
    onOpenAnnonce: (Long) -> Unit,
    viewModel: FavorisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> LoadingBox()
        state.error != null -> MessageCentral(state.error!!) { Button(onClick = viewModel::charger) { Text("Réessayer") } }
        state.favoris.isEmpty() -> MessageCentral("Tu n'as pas encore de favoris.")
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.favoris, key = { it.id ?: 0L }) { favori ->
                val annonce = favori.annonce
                if (annonce != null) {
                    Box {
                        AnnonceCard(annonce) { onOpenAnnonce(annonce.id) }
                        FilledTonalIconButton(
                            onClick = { favori.id?.let(viewModel::supprimer) },
                            modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(8.dp)
                        ) { Icon(Icons.Default.Delete, "Retirer") }
                    }
                }
            }
        }
    }
}
