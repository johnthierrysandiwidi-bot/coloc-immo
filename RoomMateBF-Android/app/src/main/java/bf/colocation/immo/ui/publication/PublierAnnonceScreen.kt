package bf.colocation.immo.ui.publication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.data.remote.dto.TypeAnnonceUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublierAnnonceScreen(
    viewModel: PublierAnnonceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Publier une annonce") }) }) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (!state.peutPublier) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        "Seuls les propriétaires et les démarcheurs peuvent publier. " +
                            "Change de profil depuis ton compte, ou fais valider ta pièce justificative.",
                        Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---------- L'annonce ----------
            Titre("L'annonce")

            Champ("Titre *", state.titre, viewModel::onTitre)

            var menuType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = menuType, onExpandedChange = { menuType = !menuType }) {
                OutlinedTextField(
                    value = state.type.libelle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type d'annonce *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = menuType, onDismissRequest = { menuType = false }) {
                    TypeAnnonceUi.values().forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.libelle) },
                            onClick = { viewModel.onType(t); menuType = false }
                        )
                    }
                }
            }

            Champ("Prix (FCFA) *", state.prix, viewModel::onPrix, KeyboardType.Number)

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescription,
                label = { Text("Description") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ---------- Le bien ----------
            Titre("Le bien concerné")

            if (state.biens.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = state.utiliserBienExistant,
                        onCheckedChange = viewModel::onUtiliserBienExistant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Utiliser un bien déjà enregistré", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (state.utiliserBienExistant) {
                var menuBien by remember { mutableStateOf(false) }
                val libelle = state.biens.firstOrNull { it.id == state.bienChoisiId }?.nom ?: "Choisir un bien"
                ExposedDropdownMenuBox(expanded = menuBien, onExpandedChange = { menuBien = !menuBien }) {
                    OutlinedTextField(
                        value = libelle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bien *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menuBien) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = menuBien, onDismissRequest = { menuBien = false }) {
                        state.biens.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.nom ?: "Bien") },
                                onClick = { viewModel.onBienChoisi(b.id); menuBien = false }
                            )
                        }
                    }
                }
            } else {
                Champ("Nom du bien *", state.nomBien, viewModel::onNomBien)
                Champ("Adresse", state.adresse, viewModel::onAdresse)

                Selecteur(
                    label = "Type de bien *",
                    valeur = state.types.firstOrNull { it.id == state.typeImmobilierId }?.nom,
                    options = state.types.mapNotNull { t -> t.id?.let { id -> id to (t.nom ?: "—") } },
                    onChoix = viewModel::onTypeImmobilier
                )
                Selecteur(
                    label = "Ville *",
                    valeur = state.localites.firstOrNull { it.id == state.localiteId }?.nom,
                    options = state.localites.mapNotNull { l -> l.id?.let { id -> id to (l.nom ?: "—") } },
                    onChoix = viewModel::onLocalite
                )
                Selecteur(
                    label = "Quartier",
                    valeur = state.quartiersFiltres.firstOrNull { it.id == state.quartierId }?.nom,
                    options = state.quartiersFiltres.mapNotNull { q -> q.id?.let { id -> id to (q.nom ?: "—") } },
                    onChoix = viewModel::onQuartier,
                    avecOptionTous = true
                )

                Row {
                    Champ("Chambres", state.chambres, viewModel::onChambres, KeyboardType.Number, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Champ("Salles de bain", state.sallesBain, viewModel::onSallesBain, KeyboardType.Number, Modifier.weight(1f))
                }
                Row {
                    Champ("Salons", state.salons, viewModel::onSalons, KeyboardType.Number, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Champ("Surface (m²)", state.surface, viewModel::onSurface, KeyboardType.Number, Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.meuble, onCheckedChange = viewModel::onMeuble)
                    Text("Meublé", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = state.garage, onCheckedChange = viewModel::onGarage)
                    Text("Garage", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.publierImmediatement,
                    onCheckedChange = viewModel::onPublierImmediatement
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.publierImmediatement) "Publier tout de suite" else "Enregistrer en brouillon",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            state.succes?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = viewModel::enregistrer,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.publierImmediatement) "Publier l'annonce" else "Enregistrer")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Titre(texte: String) {
    Text(texte, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun Champ(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = modifier.padding(vertical = 6.dp)
    )
}

/** Liste déroulante générique sur un référentiel (id, libellé). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Selecteur(
    label: String,
    valeur: String?,
    options: List<Pair<Long, String>>,
    onChoix: (Long?) -> Unit,
    avecOptionTous: Boolean = false
) {
    var ouvert by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = ouvert, onExpandedChange = { ouvert = !ouvert }) {
        OutlinedTextField(
            value = valeur ?: if (avecOptionTous) "Tous" else "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ouvert) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            if (avecOptionTous) {
                DropdownMenuItem(text = { Text("Tous") }, onClick = { onChoix(null); ouvert = false })
            }
            options.forEach { (id, libelle) ->
                DropdownMenuItem(text = { Text(libelle) }, onClick = { onChoix(id); ouvert = false })
            }
        }
    }
}
