package bf.colocation.immo.ui.tableaudebord

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.ui.annonces.AnnonceCard
import bf.colocation.immo.ui.components.LoadingBox

/**
 * Tableau de bord, premier écran après connexion.
 *
 * L'application ouvrait directement sur la liste des annonces, sans repère
 * personnel ni raccourci. La mise en page suit la maquette fournie — salutation
 * nommée, grille d'actions rapides, compteurs, puis sélection d'annonces — mais
 * conserve le vert ColocImmo plutôt que le vert menthe de la maquette, afin de
 * rester cohérent avec le site et avec le rapport.
 *
 * Les rubriques reprennent les modules du rapport : recherche, favoris,
 * rendez-vous et alertes.
 */
@Composable
fun TableauDeBordScreen(
    onRechercher: () -> Unit,
    onFavoris: () -> Unit,
    onVisites: () -> Unit,
    onAlertes: () -> Unit,
    onOpenAnnonce: (Long) -> Unit,
    viewModel: TableauDeBordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ---- Bandeau de salutation ----
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 26.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Bonjour${if (state.prenom.isNotBlank()) ", ${state.prenom}" else ""}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Trouvez votre prochain logement à Ouagadougou",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)) {
                    Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                        Text(
                            state.prenom.take(1).uppercase().ifBlank { "?" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---- Actions rapides ----
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionRapide(Icons.Default.Search, "Rechercher", Modifier.weight(1f), onRechercher)
            ActionRapide(Icons.Default.Favorite, "Favoris", Modifier.weight(1f), onFavoris)
            ActionRapide(Icons.Default.CalendarMonth, "Visites", Modifier.weight(1f), onVisites)
            ActionRapide(Icons.Default.Notifications, "Alertes", Modifier.weight(1f), onAlertes)
        }

        Spacer(Modifier.height(20.dp))

        // ---- Compteurs ----
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Compteur(state.favoris, "Favoris", Modifier.weight(1f))
            Compteur(state.visites, "Visites à venir", Modifier.weight(1f))
            Compteur(state.alertes, "Non lues", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // ---- Sélection d'annonces ----
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Récemment publiées",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRechercher) { Text("Voir tout") }
        }

        when {
            state.loading -> Box(Modifier.fillMaxWidth().height(200.dp)) { LoadingBox() }

            state.error != null -> Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error!!, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::charger) { Text("Réessayer") }
            }

            else -> LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.recentes.size) { index ->
                    val annonce = state.recentes[index]
                    Box(Modifier.width(280.dp)) {
                        AnnonceCard(annonce = annonce, onClick = { onOpenAnnonce(annonce.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRapide(
    icone: ImageVector,
    libelle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = onClick,
            modifier = Modifier.size(58.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icone,
                    contentDescription = libelle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            libelle,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Compteur(valeur: Int, libelle: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
            Text(
                "$valeur",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                libelle,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
