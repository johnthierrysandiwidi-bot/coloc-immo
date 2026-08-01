package bf.colocation.immo.ui.rendezvous

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Download
import bf.colocation.immo.core.RecuPdf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.core.formatDateTime
import bf.colocation.immo.data.remote.dto.MoyenPaiement
import bf.colocation.immo.data.remote.dto.PaiementDto
import bf.colocation.immo.data.remote.dto.RendezVousDto
import bf.colocation.immo.ui.annonces.formatPrix
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral

/**
 * Date affichable. On réutilise le formateur partagé de core/DateUtils plutôt que
 * d'en redéfinir un, et on tolère une date absente ou mal formée sans planter.
 */
private fun dateLisible(iso: String?): String =
    iso.formatDateTime() ?: "Date à préciser"

private fun libelleStatut(statut: String?): String = when (statut?.uppercase()) {
    "DEMANDE" -> "En attente"
    "ACCEPTE" -> "Accepté"
    "REPORTE" -> "Reporté"
    "ANNULE" -> "Annulé"
    "REFUSE" -> "Refusé"
    "TERMINE" -> "Visite effectuée"
    else -> statut ?: "—"
}

private fun libellePaiement(statut: String?): String = when (statut?.uppercase()) {
    "EN_ATTENTE" -> "À finaliser"
    "EN_SEQUESTRE" -> "En séquestre"
    "LIBERE" -> "Versés au démarcheur"
    "REMBOURSE" -> "Remboursés"
    else -> statut ?: "—"
}

@Composable
fun RendezVousScreen(
    viewModel: RendezVousViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var rdvAPayer by remember { mutableStateOf<Long?>(null) }
    var rdvAAnnuler by remember { mutableStateOf<Long?>(null) }
    var rdvANoter by remember { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.messagePaiement) {
        state.messagePaiement?.let {
            snackbar.showSnackbar(it)
            viewModel.effacerMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { interieur ->
        Box(Modifier.fillMaxSize().padding(interieur)) {
            when {
                state.loading -> LoadingBox()

                state.error != null -> MessageCentral(state.error!!) {
                    Button(onClick = viewModel::charger) { Text("Réessayer") }
                }

                state.rendezVous.isEmpty() -> MessageCentral(
                    "Aucun rendez-vous pour le moment.\nDemande une visite depuis une annonce."
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.rendezVous, key = { it.id ?: 0L }) { rdv ->
                        CarteRendezVous(
                            rdv = rdv,
                            paiement = rdv.id?.let { state.paiements[it] },
                            enCours = state.paiementEnCours == rdv.id,
                            onPayer = { rdv.id?.let { rdvAPayer = it } },
                            onTerminer = { rdv.id?.let { viewModel.terminer(it) } },
                            onAnnuler = { rdv.id?.let { rdvAAnnuler = it } },
                            onNoter = { rdv.id?.let { rdvANoter = it } }
                        )
                    }
                }
            }
        }
    }

    if (rdvAPayer != null) {
        DialoguePaiement(
            montant = state.montantFrais,
            onFermer = { rdvAPayer = null },
            // Le numéro n'est pas transmis au serveur : la passerelle est simulée.
            // Il est demandé et validé pour refléter le vrai parcours mobile money.
            onPayerAvec = { moyen, _ ->
                viewModel.payer(rdvAPayer!!, moyen.code)
                rdvAPayer = null
            }
        )
    }

    // Reçu, présenté dès que le règlement aboutit.
    state.recu?.let { recu ->
        DialogueRecu(paiement = recu, onFermer = viewModel::fermerRecu)
    }

    if (rdvAAnnuler != null) {
        DialogueAnnulation(
            onFermer = { rdvAAnnuler = null },
            onConfirmer = { motif ->
                viewModel.annuler(rdvAAnnuler!!, motif)
                rdvAAnnuler = null
            }
        )
    }

    if (rdvANoter != null) {
        DialogueAvis(
            onFermer = { rdvANoter = null },
            onEnvoyer = { note, commentaire ->
                viewModel.deposerAvis(rdvANoter!!, note, commentaire)
                rdvANoter = null
            }
        )
    }
}

@Composable
private fun CarteRendezVous(
    rdv: RendezVousDto,
    paiement: PaiementDto?,
    enCours: Boolean,
    onPayer: () -> Unit,
    onTerminer: () -> Unit,
    onAnnuler: () -> Unit,
    onNoter: () -> Unit
) {
    val statut = rdv.statut?.uppercase()
    val actif = statut in setOf("DEMANDE", "REPORTE", "ACCEPTE")

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                rdv.annonce?.titre ?: "Annonce",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    dateLisible(rdv.dateReportee ?: rdv.dateHeure),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!rdv.lieu.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        rdv.lieu,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(libelleStatut(rdv.statut)) },
                    leadingIcon = {
                        if (statut == "ACCEPTE") {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                if (paiement != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Frais : ${libellePaiement(paiement.statut)}") }
                    )
                }
            }

            if (actif) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (paiement == null) {
                        Button(onClick = onPayer, enabled = !enCours) {
                            if (enCours) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CreditCard, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Payer les frais")
                            }
                        }
                    }
                    // Clôture : le statut TERMINE n'était jamais posé, si bien que les
                    // frais étaient débloqués sans trace de la visite.
                    if (statut == "ACCEPTE") {
                        OutlinedButton(onClick = onTerminer) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Visite faite")
                        }
                    }
                    OutlinedButton(onClick = onAnnuler) { Text("Annuler") }
                }
            }

            // Après une visite effectuée, le locataire peut noter le démarcheur.
            if (statut == "TERMINE") {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onNoter) {
                    Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Noter la visite")
                }
            }
        }
    }
}

@Composable
private fun DialoguePaiement(
    montant: Double?,
    onFermer: () -> Unit,
    onPayerAvec: (MoyenPaiement, String) -> Unit
) {
    var choix by remember { mutableStateOf(MoyenPaiement.ORANGE_MONEY) }
    var telephone by remember { mutableStateOf("") }

    // Numéro burkinabè : 8 chiffres, préfixe cohérent avec l'opérateur retenu.
    val prefixes = when (choix) {
        MoyenPaiement.ORANGE_MONEY -> listOf('5', '6', '7')
        MoyenPaiement.MOOV_MONEY -> listOf('0', '1', '5', '6')
        MoyenPaiement.CARTE -> emptyList()
    }
    val mobile = choix != MoyenPaiement.CARTE
    val numeroValide = !mobile ||
        (telephone.filter { it.isDigit() }.length == 8 && telephone.first() in prefixes)

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Régler les frais de visite") },
        text = {
            Column {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        "La somme est conservée en séquestre. Elle est versée au démarcheur " +
                            "après la visite, ou remboursée si celle-ci n'a pas lieu.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    formatPrix(montant),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                Text("Moyen de paiement", style = MaterialTheme.typography.labelLarge)
                MoyenPaiement.values().forEach { moyen ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = choix == moyen,
                            onClick = { choix = moyen; telephone = "" }
                        )
                        Text(moyen.libelle)
                    }
                }

                if (mobile) {
                    OutlinedTextField(
                        value = telephone,
                        onValueChange = { v -> telephone = v.filter { it.isDigit() }.take(8) },
                        label = { Text("Numéro ${choix.libelle}") },
                        placeholder = { Text("70123456") },
                        prefix = { Text("+226 ") },
                        singleLine = true,
                        isError = telephone.isNotEmpty() && !numeroValide,
                        supportingText = {
                            Text(
                                if (telephone.isNotEmpty() && !numeroValide)
                                    "8 chiffres, commençant par ${prefixes.joinToString(", ")}"
                                else "Le numéro qui recevra la demande de confirmation."
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Démonstration : aucun montant réel n'est débité et le numéro n'est pas " +
                        "transmis à l'opérateur.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPayerAvec(choix, telephone) },
                enabled = numeroValide
            ) { Text("Payer") }
        },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler") } }
    )
}

/**
 * Reçu affiché après règlement.
 *
 * L'ancienne version se contentait d'un message fugace : la référence du paiement,
 * seule preuve en cas de contestation sur la visite, n'était jamais montrée.
 */
@Composable
private fun DialogueRecu(paiement: PaiementDto, onFermer: () -> Unit) {
    AlertDialog(
        onDismissRequest = onFermer,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        },
        title = { Text("Paiement confirmé") },
        text = {
            Column {
                Text(
                    formatPrix(paiement.montant),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Somme placée en séquestre",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                LigneRecu("Référence", paiement.reference ?: "—")
                LigneRecu(
                    "Moyen",
                    MoyenPaiement.values().firstOrNull { it.code == paiement.moyen }?.libelle
                        ?: paiement.moyen ?: "—"
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    "Conservez la référence : elle sert de preuve en cas de contestation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = onFermer) { Text("Terminer") } },
        dismissButton = {
            val context = LocalContext.current
            // Génère le reçu PDF et ouvre le sélecteur de partage.
            TextButton(onClick = { RecuPdf.genererEtPartager(context, paiement) }) {
                Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reçu PDF")
            }
        }
    )
}

@Composable
private fun LigneRecu(libelle: String, valeur: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            libelle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(valeur, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DialogueAnnulation(
    onFermer: () -> Unit,
    onConfirmer: (String?) -> Unit
) {
    var motif by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Annuler le rendez-vous") },
        text = {
            Column {
                Text("Indique brièvement la raison, elle sera transmise à l'autre partie.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = motif,
                    onValueChange = { motif = it },
                    label = { Text("Motif") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmer(motif.ifBlank { null }) }) { Text("Confirmer") }
        },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Retour") }
        }
    )
}

@Composable
private fun DialogueAvis(
    onFermer: () -> Unit,
    onEnvoyer: (note: Int, commentaire: String?) -> Unit
) {
    var note by remember { mutableStateOf(0) }
    var commentaire by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onFermer,
        title = { Text("Noter la visite") },
        text = {
            Column {
                Text(
                    "Votre avis aide les futurs visiteurs à faire confiance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                // Rangée d'étoiles cliquables : la note va de 1 à 5.
                Row {
                    (1..5).forEach { valeur ->
                        IconButton(onClick = { note = valeur }) {
                            Icon(
                                if (valeur <= note) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$valeur étoile(s)",
                                tint = if (valeur <= note) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = commentaire,
                    onValueChange = { commentaire = it },
                    label = { Text("Commentaire (facultatif)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEnvoyer(note, commentaire) },
                enabled = note in 1..5
            ) { Text("Envoyer") }
        },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler") } }
    )
}
