package bf.colocation.immo.ui.annonces

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnonceListScreen(
    onOpenAnnonce: (Long) -> Unit,
    viewModel: AnnonceListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Déclenche le chargement de la page suivante en approchant du bas.
    // On ne lit que listState ici (référence stable) ; chargerPlus() se protège
    // lui-même via peutChargerPlus, donc pas besoin de lire l'état ici.
    val prochePied by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val dernier = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && dernier >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(prochePied) { if (prochePied) viewModel.chargerPlus() }

    Column(Modifier.fillMaxSize()) {
        // Barre de recherche
        OutlinedTextField(
            value = state.recherche,
            onValueChange = viewModel::onRecherche,
            placeholder = { Text("Rechercher un logement…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { viewModel.appliquerRecherche() }
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Filtres par type.
        // Un Row simple répartissait la largeur disponible entre les quatre puces :
        // sur un écran étroit, la dernière était comprimée au point que son libellé
        // se cassait lettre par lettre à la verticale. Une LazyRow laisse à chaque
        // puce sa largeur naturelle et fait défiler la rangée horizontalement.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { FiltreChip("Tous", state.typeFiltre == null) { viewModel.onTypeFiltre(null) } }
            item { FiltreChip("Location", state.typeFiltre == "LOCATION") { viewModel.onTypeFiltre("LOCATION") } }
            item { FiltreChip("Colocation", state.typeFiltre == "COLOCATION") { viewModel.onTypeFiltre("COLOCATION") } }
            item { FiltreChip("Vente", state.typeFiltre == "VENTE") { viewModel.onTypeFiltre("VENTE") } }
        }

        when {
            state.loading -> LoadingBox()
            state.error != null -> MessageCentral(state.error!!) {
                Button(onClick = viewModel::rafraichir) { Text("Réessayer") }
            }
            state.annonces.isEmpty() -> MessageCentral("Aucune annonce trouvée.")
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.annonces, key = { it.id }) { annonce ->
                    AnnonceCard(annonce) { onOpenAnnonce(annonce.id) }
                }
                if (state.loadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltreChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) }
    )
}
