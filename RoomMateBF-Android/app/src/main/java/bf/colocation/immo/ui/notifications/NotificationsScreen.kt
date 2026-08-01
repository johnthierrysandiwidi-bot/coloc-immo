package bf.colocation.immo.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.core.formatDateTime
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral

@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> LoadingBox()
        state.error != null -> MessageCentral(state.error!!) { Button(onClick = viewModel::charger) { Text("Réessayer") } }
        state.notifications.isEmpty() -> MessageCentral("Aucune notification.")
        else -> LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.notifications, key = { it.id }) { notif ->
                ListItem(
                    leadingContent = {
                        Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    headlineContent = { Text(notif.titre ?: "Notification") },
                    supportingContent = {
                        Column {
                            notif.message?.let { Text(it) }
                            notif.dateCreation.formatDateTime()?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    trailingContent = {
                        if (notif.lue == false) Icon(Icons.Default.Circle, null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(10.dp))
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
