package bf.colocation.immo.ui.annonces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bf.colocation.immo.core.Illustrations
import bf.colocation.immo.core.formatDate
import bf.colocation.immo.core.toImageUrl
import bf.colocation.immo.data.remote.dto.ImmobilierDto
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.components.MessageCentral
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnonceDetailScreen(
    onBack: () -> Unit,
    /** Faux pour un visiteur : le catalogue étant public, on peut arriver ici sans compte. */
    connecte: Boolean = true,
    /** Ouvre l'inscription. Sans destination, la barre d'actions est simplement masquée. */
    onCreerCompte: (() -> Unit)? = null,
    onOuvrirConversation: ((Long) -> Unit)? = null,
    viewModel: AnnonceDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var afficherDialogVisite by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.messageConsomme() }
    }
    LaunchedEffect(state.conversationOuverte) {
        state.conversationOuverte?.let { convId ->
            onOuvrirConversation?.invoke(convId)
            viewModel.navigationConsommee()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail de l'annonce") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            val a = state.annonce
            if (a != null) {
                Surface(shadowElevation = 8.dp) {
                    if (!connecte) {
                        // Le catalogue est consultable sans compte : proposer ici des
                        // actions qui échoueraient en 401 n'aurait aucun sens. On
                        // annonce donc ce que le compte débloque, comme sur le site.
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                "Créez un compte pour réserver une visite et enregistrer ce bien.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { onCreerCompte?.invoke() },
                                enabled = onCreerCompte != null,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Créer un compte gratuit")
                            }
                        }
                        return@Surface
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = viewModel::ajouterFavori,
                            enabled = !state.favoriEnCours
                        ) {
                            Icon(Icons.Default.FavoriteBorder, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Favori")
                        }
                        OutlinedButton(onClick = viewModel::contacter) {
                            Icon(Icons.AutoMirrored.Filled.Chat, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Contacter")
                        }
                        Button(
                            onClick = { afficherDialogVisite = true },
                            enabled = !state.visiteEnCours,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Demander une visite")
                        }
                    }
                }
            }
        }
    ) { pad ->
        when {
            state.loading -> Box(Modifier.padding(pad)) { LoadingBox() }
            state.error != null -> Box(Modifier.padding(pad)) {
                MessageCentral(state.error!!) { Button(onClick = viewModel::charger) { Text("Réessayer") } }
            }
            state.annonce != null -> {
                val a = state.annonce!!
                val immo = a.immobilier
                Column(
                    Modifier.padding(pad).verticalScroll(rememberScrollState())
                ) {
                    // Carrousel de photos
                    // Même règle que sur la carte et sur le site : photos fiables,
                    // sinon la série d'illustrations correspondant au type de bien.
                    val images = Illustrations.visuels(a)
                    if (images.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { images.size })
                        Box {
                            HorizontalPager(state = pagerState) { page ->
                                AsyncImage(
                                    model = images[page],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(260.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                            if (images.size > 1) {
                                Row(
                                    Modifier.align(Alignment.BottomCenter).padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(images.size) { i ->
                                        val actif = pagerState.currentPage == i
                                        Box(
                                            Modifier.size(if (actif) 10.dp else 7.dp).clip(CircleShape)
                                                .background(if (actif) Color.White else Color.White.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Home, null, modifier = Modifier.size(64.dp), tint = Color.Gray) }
                    }

                    Column(Modifier.padding(16.dp)) {
                        AssistChip(onClick = {}, label = { Text(libelleType(a.type)) })
                        Spacer(Modifier.height(8.dp))
                        Text(a.titre ?: "Sans titre", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(formatPrix(a.prix), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)

                        val lieu = listOfNotNull(immo?.adresse, immo?.quartier?.nom, immo?.localite?.nom)
                            .joinToString(", ")
                        if (lieu.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(lieu, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(16.dp))

                        Text("Caractéristiques", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        CaracteristiquesGrille(immo)

                        // Localisation : ouvre l'application de cartes du téléphone.
                        // Les coordonnées appartiennent au bien (ImmobilierDto),
                        // pas à l'annonce elle-même.
                        BoutonLocalisation(
                            latitude = immo?.latitude,
                            longitude = immo?.longitude,
                            quartier = immo?.quartier?.nom,
                            ville = immo?.localite?.nom
                        )

                        if (!a.contenu.isNullOrBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Text("Description", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(a.contenu, style = MaterialTheme.typography.bodyLarge)
                        }

                        Spacer(Modifier.height(16.dp))
                        Row {
                            a.datePublication.formatDate()?.let {
                                Text("Publié le $it", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.weight(1f))
                            a.nombreVues?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(4.dp))
                                    Text("$it vues", style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
            else -> Box(Modifier.padding(pad)) { MessageCentral("Annonce introuvable.") }
        }
    }

    if (afficherDialogVisite) {
        DialogDemandeVisite(
            enCours = state.visiteEnCours,
            onDismiss = { afficherDialogVisite = false },
            onConfirm = { dateIso, msg ->
                viewModel.demanderVisite(dateIso, msg)
                afficherDialogVisite = false
            }
        )
    }
}

@Composable
private fun CaracteristiquesGrille(immo: ImmobilierDto?) {
    if (immo == null) { Text("—"); return }
    val items = buildList {
        immo.surface?.let { add(Icons.Default.SquareFoot to "${it.toInt()} m²") }
        immo.nombrePieces?.let { add(Icons.Default.Dashboard to "$it pièces") }
        immo.nombreChambres?.let { add(Icons.Default.Bed to "$it chambres") }
        immo.nombreSallesBain?.let { add(Icons.Default.Bathtub to "$it SDB") }
        immo.nombreSalons?.let { add(Icons.Default.Weekend to "$it salon(s)") }
        if (immo.garage == true) add(Icons.Default.Garage to "Garage")
        if (immo.piscine == true) add(Icons.Default.Pool to "Piscine")
        if (immo.meuble == true) add(Icons.Default.Chair to "Meublé")
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { paire ->
            Row(Modifier.fillMaxWidth()) {
                paire.forEach { (icon, texte) ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(texte, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (paire.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/**
 * Bouton d'ouverture de la localisation dans l'application de cartes du téléphone.
 *
 * Si les coordonnées précises existent, on ouvre un point géographique (geo:).
 * Sinon, on lance une recherche sur le quartier et la ville. En dernier recours —
 * aucune application de cartes installée — on bascule sur OpenStreetMap dans le
 * navigateur. La « carte interactive » annoncée au rapport est ainsi couverte.
 */
@Composable
private fun BoutonLocalisation(
    latitude: Double?,
    longitude: Double?,
    quartier: String?,
    ville: String?
) {
    val context = LocalContext.current
    val libelle = listOfNotNull(quartier, ville).joinToString(", ").ifBlank { "Localisation" }

    Spacer(Modifier.height(16.dp))
    OutlinedButton(
        onClick = {
            val uri = when {
                latitude != null && longitude != null ->
                    Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($libelle)")
                libelle.isNotBlank() ->
                    Uri.parse("geo:0,0?q=" + Uri.encode(libelle))
                else -> null
            }
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Aucune application de cartes : repli navigateur (OpenStreetMap).
                val web = if (latitude != null && longitude != null) {
                    "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=17/$latitude/$longitude"
                } else {
                    "https://www.openstreetmap.org/search?query=" + Uri.encode(libelle)
                }
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(web)))
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Voir sur la carte — $libelle")
    }
}
