package bf.colocation.immo.ui.annonces

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogDemandeVisite(
    enCours: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (dateIso: String, message: String?) -> Unit
) {
    var dateChoisie by remember { mutableStateOf<LocalDate?>(null) }
    var heureChoisie by remember { mutableStateOf<LocalTime?>(null) }
    var message by remember { mutableStateOf("") }
    var afficherDate by remember { mutableStateOf(false) }
    var afficherHeure by remember { mutableStateOf(false) }

    val fmtDate = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE) }
    val pret = dateChoisie != null && heureChoisie != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Demander une visite") },
        text = {
            Column {
                OutlinedButton(onClick = { afficherDate = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(dateChoisie?.format(fmtDate) ?: "Choisir une date")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { afficherHeure = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(heureChoisie?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "Choisir une heure")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = dateChoisie!!; val h = heureChoisie!!
                    // Instant UTC au format ISO attendu par le backend.
                    val instant = d.atTime(h).atZone(ZoneId.systemDefault()).toInstant()
                    onConfirm(instant.toString(), message.ifBlank { null })
                },
                enabled = pret && !enCours
            ) {
                if (enCours) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Envoyer")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )

    if (afficherDate) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { afficherDate = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let {
                        dateChoisie = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    afficherDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { afficherDate = false }) { Text("Annuler") } }
        ) { DatePicker(state = dpState) }
    }

    if (afficherHeure) {
        val now = LocalTime.now()
        val tpState = rememberTimePickerState(initialHour = now.hour, initialMinute = now.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { afficherHeure = false },
            confirmButton = {
                TextButton(onClick = {
                    heureChoisie = LocalTime.of(tpState.hour, tpState.minute)
                    afficherHeure = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { afficherHeure = false }) { Text("Annuler") } },
            text = { TimePicker(state = tpState) }
        )
    }
}
