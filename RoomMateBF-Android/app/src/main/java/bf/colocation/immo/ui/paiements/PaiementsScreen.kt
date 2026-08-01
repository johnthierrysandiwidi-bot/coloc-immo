package bf.colocation.immo.ui.paiements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
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
import bf.colocation.immo.ui.annonces.formatPrix
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral
import bf.colocation.immo.ui.rendezvous.RendezVousViewModel

private fun libellePaiement(statut: String?): String = when (statut?.uppercase()) {
    "EN_ATTENTE" -> "À régler"
    "EN_SEQUESTRE" -> "Payé — fonds en séquestre"
    "LIBERE" -> "Versé au démarcheur"
    "REMBOURSE" -> "Remboursé"
    else -> statut ?: "—"
}

/**
 * Frais de visite : état des règlements, paiement mobile money ou carte,
 * et reçu conservé après l'opération (la référence est la seule preuve
 * dont dispose l'utilisateur si la visite est contestée — RG23).
 *
 * GET /api/paiements est réservé aux administrateurs : côté utilisateur,
 * les paiements se lisent rendez-vous par rendez-vous.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaiementsScreen(
    viewModel: RendezVousViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var rdvAPayer by remember { mutableStateOf<Long?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Mes paiements") }) }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                state.loading -> LoadingBox()
                state.rendezVous.isEmpty() -> MessageCentral(
                    "Aucun paiement.\nLes frais de visite apparaissent ici dès qu'une visite est demandée."
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        state.montantFrais?.let {
                            Text(
                                "Frais de visite en vigueur : " + formatPrix(it),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(state.rendezVous, key = { it.id ?: 0L }) { rdv ->
                        val id = rdv.id
                        if (id != null) {
                            val paiement = state.paiements[id]
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        rdv.annonce?.titre ?: "Visite",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        rdv.dateHeure.formatDateTime() ?: "Date à préciser",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(10.dp))

                                    if (paiement == null || paiement.statut.equals("EN_ATTENTE", true)) {
                                        Text(
                                            "Frais de visite : " + formatPrix(paiement?.montant ?: state.montantFrais),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { rdvAPayer = id },
                                            enabled = state.paiementEnCours != id,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (state.paiementEnCours == id) {
                                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.CreditCard, null, Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Payer les frais")
                                            }
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                libellePaiement(paiement.statut),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        paiement.reference?.let {
                                            Text(
                                                "Référence : " + it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            state.messagePaiement?.let { message ->
                Snackbar(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = viewModel::effacerMessage) { Text("Fermer") } }
                ) { Text(message) }
            }
        }
    }

    // Choix du moyen de règlement.
    rdvAPayer?.let { id ->
        AlertDialog(
            onDismissRequest = { rdvAPayer = null },
            title = { Text("Moyen de paiement") },
            text = {
                Column {
                    Text(
                        "Les frais sont conservés en séquestre jusqu'à la visite, " +
                            "puis versés au démarcheur ou remboursés.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    MoyenPaiement.values().forEach { moyen ->
                        OutlinedButton(
                            onClick = { viewModel.payer(id, moyen.code); rdvAPayer = null },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) { Text(moyen.libelle) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { rdvAPayer = null }) { Text("Annuler") } }
        )
    }

    // Reçu après règlement.
    state.recu?.let { recu: PaiementDto ->
        AlertDialog(
            onDismissRequest = viewModel::fermerRecu,
            icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Paiement enregistré") },
            text = {
                Column {
                    Text("Montant : " + formatPrix(recu.montant))
                    recu.moyen?.let { code ->
                        val libelle = MoyenPaiement.values().firstOrNull { it.code == code }?.libelle ?: code
                        Text("Moyen : " + libelle)
                    }
                    recu.reference?.let { Text("Référence : " + it) }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Conserve cette référence : elle sert de preuve en cas de litige.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = { Button(onClick = viewModel::fermerRecu) { Text("OK") } }
        )
    }
}
