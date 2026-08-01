package bf.colocation.immo.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import bf.colocation.immo.data.remote.dto.ProfilInscription
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Créer un compte") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") }
            }
        )
    }) { pad ->
        if (state.success) {
            Column(
                Modifier.fillMaxSize().padding(pad).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text("Compte créé !", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Un email d'activation t'a été envoyé. Active ton compte puis connecte-toi.",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Aller à la connexion") }
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            // Choix du profil : le rapport §4.3.1 le place en tête du parcours.
            var menuOuvert by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = menuOuvert,
                onExpandedChange = { menuOuvert = !menuOuvert }
            ) {
                OutlinedTextField(
                    value = state.profil.libelle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Je suis…") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOuvert) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = menuOuvert, onDismissRequest = { menuOuvert = false }) {
                    ProfilInscription.values().forEach { profil ->
                        DropdownMenuItem(
                            text = { Text(profil.libelle) },
                            onClick = { viewModel.onProfil(profil); menuOuvert = false }
                        )
                    }
                }
            }

            if (state.profil == ProfilInscription.DEMARCHEUR) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Après inscription, vous devrez déposer une pièce justificative. " +
                        "La publication reste bloquée tant qu'un administrateur ne l'a pas validée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Champ("Nom *", state.lastName, viewModel::onLastName)
            Champ("Prénom *", state.firstName, viewModel::onFirstName)
            Champ("Identifiant *", state.login, viewModel::onLogin)
            Champ("Email *", state.email, viewModel::onEmail, KeyboardType.Email)
            Champ("Téléphone * (8 chiffres)", state.telephone, viewModel::onTelephone, KeyboardType.Phone)
            Champ("Mot de passe *", state.password, viewModel::onPassword, KeyboardType.Password, true)
            Champ("Confirmer le mot de passe *", state.confirm, viewModel::onConfirm, KeyboardType.Password, true)

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.conditions, onCheckedChange = viewModel::onConditions)
                Text(
                    "J'accepte les conditions d'utilisation.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::inscription,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.loading) CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else Text("S'inscrire")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Champ(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
}
