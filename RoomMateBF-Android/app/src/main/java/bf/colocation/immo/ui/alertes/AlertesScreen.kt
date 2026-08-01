package bf.colocation.immo.ui.alertes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.data.remote.dto.AlerteDto
import bf.colocation.immo.data.remote.dto.FrequenceUi
import bf.colocation.immo.data.remote.dto.TypeAnnonceUi
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral
import bf.colocation.immo.ui.publication.Selecteur

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertesScreen(
    viewModel: AlertesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes alertes") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::ouvrirFormulaire,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Créer une alerte") }
            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                state.loading -> LoadingBox()
                state.alertes.isEmpty() -> MessageCentral(
                    "Aucune alerte pour le moment.\nCrée-en une pour être prévenu dès qu'un logement " +
                        "correspond à tes critères."
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.alertes, key = { it.id ?: 0L }) { alerte ->
                        CarteAlerte(alerte) { alerte.id?.let(viewModel::supprimer) }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }

            state.succes?.let { message ->
                Snackbar(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = viewModel::effacerMessages) { Text("OK") } }
                ) { Text(message) }
            }
            if (state.succes == null) {
                state.error?.let { message ->
                    Snackbar(
                        Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        action = { TextButton(onClick = viewModel::effacerMessages) { Text("Fermer") } }
                    ) { Text(message) }
                }
            }
        }
    }

    if (state.formulaireOuvert) {
        DialogueAlerte(state, viewModel)
    }
}

@Composable
private fun CarteAlerte(alerte: AlerteDto, onSupprimer: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    alerte.titre ?: "Alerte",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                val criteres = buildList {
                    alerte.typeAnnonce?.let { add(it.lowercase().replaceFirstChar(Char::uppercase)) }
                    alerte.typeImmobilier?.nom?.let { add(it) }
                    alerte.localite?.nom?.let { add(it) }
                    alerte.quartier?.nom?.let { add(it) }
                    if (alerte.prixMin != null || alerte.prixMax != null) {
                        val min = alerte.prixMin?.toLong()?.toString() ?: "0"
                        val max = alerte.prixMax?.toLong()?.toString() ?: "∞"
                        add(min + " – " + max + " FCFA")
                    }
                    alerte.nombreChambresMin?.let { add(it.toString() + " chambre(s) min.") }
                    alerte.surfaceMin?.let { add(it.toLong().toString() + " m² min.") }
                    if (alerte.meubleUniquement == true) add("Meublé uniquement")
                }
                Text(
                    if (criteres.isEmpty()) "Tous les logements" else criteres.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            FrequenceUi.values().firstOrNull { it.code == alerte.frequence }?.libelle
                                ?: (alerte.frequence ?: "—")
                        )
                    }
                )
            }
            IconButton(onClick = onSupprimer) {
                Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogueAlerte(state: AlertesUiState, viewModel: AlertesViewModel) {
    val f = state.formulaire
    AlertDialog(
        onDismissRequest = viewModel::fermerFormulaire,
        title = { Text("Nouvelle alerte") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = f.titre,
                    onValueChange = viewModel::onTitre,
                    singleLine = true,
                    label = { Text("Nom de l'alerte *") },
                    placeholder = { Text("Ex. : Studio à Karpala") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                )

                var menuType by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = menuType, onExpandedChange = { menuType = !menuType }) {
                    OutlinedTextField(
                        value = f.typeAnnonce?.libelle ?: "Tous",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type d'annonce") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = menuType, onDismissRequest = { menuType = false }) {
                        DropdownMenuItem(
                            text = { Text("Tous") },
                            onClick = { viewModel.onTypeAnnonce(null); menuType = false }
                        )
                        TypeAnnonceUi.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.libelle) },
                                onClick = { viewModel.onTypeAnnonce(t); menuType = false }
                            )
                        }
                    }
                }

                Selecteur(
                    label = "Type de bien",
                    valeur = state.types.firstOrNull { it.id == f.typeImmobilierId }?.nom,
                    options = state.types.mapNotNull { t -> t.id?.let { id -> id to (t.nom ?: "—") } },
                    onChoix = viewModel::onTypeImmobilier,
                    avecOptionTous = true
                )
                Selecteur(
                    label = "Ville",
                    valeur = state.localites.firstOrNull { it.id == f.localiteId }?.nom,
                    options = state.localites.mapNotNull { l -> l.id?.let { id -> id to (l.nom ?: "—") } },
                    onChoix = viewModel::onLocalite,
                    avecOptionTous = true
                )
                Selecteur(
                    label = "Quartier",
                    valeur = state.quartiersFiltres.firstOrNull { it.id == f.quartierId }?.nom,
                    options = state.quartiersFiltres.mapNotNull { q -> q.id?.let { id -> id to (q.nom ?: "—") } },
                    onChoix = viewModel::onQuartier,
                    avecOptionTous = true
                )

                Row {
                    ChampNombre("Prix min", f.prixMin, viewModel::onPrixMin, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    ChampNombre("Prix max", f.prixMax, viewModel::onPrixMax, Modifier.weight(1f))
                }
                Row {
                    ChampNombre("Chambres min", f.chambresMin, viewModel::onChambresMin, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    ChampNombre("Surface min (m²)", f.surfaceMin, viewModel::onSurfaceMin, Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = f.meubleUniquement, onCheckedChange = viewModel::onMeuble)
                    Text("Meublé uniquement", style = MaterialTheme.typography.bodyMedium)
                }

                var menuFreq by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = menuFreq, onExpandedChange = { menuFreq = !menuFreq }) {
                    OutlinedTextField(
                        value = f.frequence.libelle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Me prévenir") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuFreq) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = menuFreq, onDismissRequest = { menuFreq = false }) {
                        FrequenceUi.values().forEach { fr ->
                            DropdownMenuItem(
                                text = { Text(fr.libelle) },
                                onClick = { viewModel.onFrequence(fr); menuFreq = false }
                            )
                        }
                    }
                }

                state.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = viewModel::enregistrer, enabled = !state.enregistrement) {
                if (state.enregistrement) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Créer l'alerte")
                }
            }
        },
        dismissButton = { TextButton(onClick = viewModel::fermerFormulaire) { Text("Annuler") } }
    )
}

@Composable
private fun ChampNombre(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.padding(vertical = 6.dp)
    )
}
