package bf.colocation.immo.ui.accueil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import bf.colocation.immo.core.Constants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MapsHomeWork
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Accueil public de l'application mobile.
 *
 * L'application s'ouvrait directement sur le formulaire de connexion : un visiteur
 * ne pouvait rien voir sans compte, alors que le catalogue est public côté web et
 * que le backend autorise la consultation des annonces sans authentification.
 * Cet écran rétablit la symétrie : on parcourt d'abord, on s'inscrit ensuite.
 */
@Composable
fun AccueilScreen(
    onParcourir: () -> Unit,
    onSeConnecter: () -> Unit,
    onCreerCompte: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ---- Bannière : photo pleine largeur, identité par-dessus ----
        // L'écran s'ouvrait sur une icône dans un cercle, sur fond blanc : peu
        // engageant pour une plateforme immobilière. Une vue réelle de logement
        // installe le sujet dès la première seconde, comme le hero du site.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = Constants.SERVER_ROOT + "/photos/photo-17.jpg",
                contentDescription = "Logement à Ouagadougou",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Dégradé sombre : le texte reste lisible quelle que soit la photo.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.20f),
                            1f to Color.Black.copy(alpha = 0.78f)
                        )
                    )
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.93f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MapsHomeWork,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "ColocImmo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Louez, achetez ou partagez un logement au Burkina Faso, " +
                        "avec des intermédiaires vérifiés.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // ---- Contenu sous la bannière ----
        // Marge propre à ce bloc, la bannière restant bord à bord.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Action principale : voir les biens. Un visiteur veut regarder avant
            // de créer un compte ; l'inscription vient ensuite.
            Button(
                onClick = onParcourir,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Search, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Parcourir les annonces", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(Modifier.height(12.dp))

            // Contour marqué : sur fond blanc, un trait fin se confondait avec le
            // bouton-texte situé juste en dessous.
            OutlinedButton(
                onClick = onCreerCompte,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Créer un compte gratuit", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
            }

            Spacer(Modifier.height(10.dp))

            // Fond léger : en simple texte, ce troisième niveau se confondait avec
            // le bouton précédent.
            // clickable sur le Modifier plutôt que Surface(onClick = …) : cette
            // seconde forme relève d'une API expérimentale selon les versions.
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(onClick = onSeConnecter)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "J'ai déjà un compte — Se connecter",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Argument(
                icone = Icons.Default.VerifiedUser,
                titre = "Des intermédiaires vérifiés",
                texte = "Chaque démarcheur dépose une pièce d'identité, validée avant toute publication."
            )
            Spacer(Modifier.height(12.dp))
            Argument(
                icone = Icons.Default.Bolt,
                titre = "Des frais de visite sécurisés",
                texte = "La somme reste en séquestre et n'est versée qu'après la visite."
            )
            Spacer(Modifier.height(12.dp))
            Argument(
                icone = Icons.Default.Search,
                titre = "Partout au Burkina",
                texte = "Location, vente et colocation à Ouagadougou et dans sept autres villes."
            )

            Spacer(Modifier.height(24.dp))

            // Contacts de la plateforme : un appui compose le numéro.
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Besoin d'aide ? Contactez-nous",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(10.dp))
                    listOf("+226 54 56 40 01", "+226 71 49 05 08").forEach { numero ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero.replace(" ", "")))
                                    )
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(numero, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Argument(icone: ImageVector, titre: String, texte: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    texte,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
