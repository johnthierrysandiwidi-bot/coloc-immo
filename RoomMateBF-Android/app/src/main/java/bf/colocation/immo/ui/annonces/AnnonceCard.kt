package bf.colocation.immo.ui.annonces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bf.colocation.immo.core.Illustrations
import bf.colocation.immo.core.toImageUrl
import bf.colocation.immo.data.remote.dto.AnnonceDto
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

fun formatPrix(prix: Double?): String {
    if (prix == null) return "Prix N.C."
    val nf = NumberFormat.getIntegerInstance(Locale.FRANCE)
    return "${nf.format(prix.toLong())} FCFA"
}

fun libelleType(type: String?): String = when (type?.uppercase()) {
    "VENTE" -> "Vente"
    "LOCATION" -> "Location"
    "COLOCATION" -> "Colocation"
    else -> type ?: ""
}

@Composable
fun AnnonceCard(annonce: AnnonceDto, onClick: () -> Unit) {
    val immo = annonce.immobilier
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box {
            // Photo téléversée si elle existe, sinon l'illustration du type de bien.
            // Les adresses externes du jeu de démonstration sont écartées : elles
            // montraient des sujets sans rapport avec l'immobilier.
            val visuel = Illustrations.visuels(annonce).first()
            AsyncImage(
                model = visuel,
                contentDescription = annonce.titre,
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Default.BrokenImage),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomEnd = 12.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    libelleType(annonce.type),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Nombre de photos : sans cet indice, rien ne laissait deviner que
            // l'annonce en contient plusieurs avant de l'avoir ouverte.
            val nbPhotos = annonce.toutesLesImages.size
            if (nbPhotos > 1) {
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$nbPhotos",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
        Column(Modifier.padding(14.dp)) {
            Text(
                annonce.titre ?: "Sans titre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            val lieu = listOfNotNull(immo?.quartier?.nom, immo?.localite?.nom).joinToString(", ")
            if (lieu.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(lieu, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (immo?.nombreChambres != null) {
                    Caracteristique(Icons.Default.Bed, "${immo.nombreChambres} ch.")
                    Spacer(Modifier.width(12.dp))
                }
                if (immo?.surface != null) {
                    Caracteristique(Icons.Default.SquareFoot, "${immo.surface.toInt()} m²")
                }
                Spacer(Modifier.weight(1f))
                Text(
                    formatPrix(annonce.prix),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Caracteristique(icon: androidx.compose.ui.graphics.vector.ImageVector, texte: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(texte, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
