package bf.colocation.immo.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class Service(
    val route: String,
    val titre: String,
    val sousTitre: String,
    val icone: ImageVector
)

/**
 * La barre inférieure de Material 3 ne tient que cinq entrées : au-delà, les
 * libellés se coupent. Les parcours secondaires (visites, paiements, favoris,
 * alertes, notifications) sont donc regroupés ici plutôt que dispersés.
 */
private val services = listOf(
    Service(Routes.RENDEZ_VOUS, "Mes visites", "Demandes, reports et annulations", Icons.Default.CalendarMonth),
    Service(Routes.MESSAGES, "Messages", "Vos conversations", Icons.AutoMirrored.Filled.Chat),
    Service(Routes.PAIEMENTS, "Mes paiements", "Frais de visite et reçus", Icons.Default.CreditCard),
    Service(Routes.FAVORIS, "Mes favoris", "Annonces mises de côté", Icons.Default.Favorite),
    Service(Routes.ALERTES, "Mes alertes", "Être prévenu des nouvelles annonces", Icons.Default.NotificationsActive),
    Service(Routes.NOTIFICATIONS, "Notifications", "Historique des messages reçus", Icons.Default.Notifications)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onOuvrir: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Services") }) }) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(services.size) { index ->
                val s = services[index]
                ListItem(
                    headlineContent = { Text(s.titre) },
                    supportingContent = { Text(s.sousTitre) },
                    leadingContent = { Icon(s.icone, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                    modifier = Modifier.clickable { onOuvrir(s.route) }
                )
                HorizontalDivider()
            }
        }
    }
}
