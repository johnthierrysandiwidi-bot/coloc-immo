package bf.colocation.immo.ui.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral

@Composable
fun ProfilScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfilViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmer by remember { mutableStateOf(false) }

    when {
        state.loading -> LoadingBox()
        state.error != null -> MessageCentral(state.error!!) { Button(onClick = viewModel::charger) { Text("Réessayer") } }
        state.compte != null -> {
            val c = state.compte!!
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        c.displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(c.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                c.email?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }

                Spacer(Modifier.height(24.dp))
                InfoLigne(Icons.Default.AccountCircle, "Identifiant", c.login)
                c.authorities.firstOrNull { it.startsWith("ROLE_") }?.let {
                    InfoLigne(Icons.Default.Badge, "Rôle", roleLisible(it))
                }
                InfoLigne(
                    if (c.activated) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    "Compte", if (c.activated) "Activé" else "Non activé"
                )

                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = { confirmer = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Se déconnecter")
                }
            }
        }
    }

    if (confirmer) {
        AlertDialog(
            onDismissRequest = { confirmer = false },
            title = { Text("Déconnexion") },
            text = { Text("Veux-tu vraiment te déconnecter ?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmer = false
                    viewModel.deconnexion(onLoggedOut)
                }) { Text("Oui") }
            },
            dismissButton = { TextButton(onClick = { confirmer = false }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun InfoLigne(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, valeur: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valeur, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun roleLisible(role: String): String = when (role) {
    "ROLE_ADMIN" -> "Administrateur"
    "ROLE_PROPRIETAIRE" -> "Propriétaire"
    "ROLE_DEMARCHEUR" -> "Démarcheur"
    "ROLE_UTILISATEUR", "ROLE_USER" -> "Utilisateur"
    else -> role.removePrefix("ROLE_")
}
