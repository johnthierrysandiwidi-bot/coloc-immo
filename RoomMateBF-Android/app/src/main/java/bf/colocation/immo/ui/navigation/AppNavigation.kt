package bf.colocation.immo.ui.navigation

import bf.colocation.immo.ui.messagerie.MessagerieViewModel

import bf.colocation.immo.ui.messagerie.FilMessagesScreen

import bf.colocation.immo.ui.messagerie.ConversationsScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import bf.colocation.immo.ui.accueil.AccueilScreen
import bf.colocation.immo.ui.annonces.AnnonceDetailScreen
import bf.colocation.immo.ui.alertes.AlertesScreen
import bf.colocation.immo.ui.annonces.AnnonceListScreen
import bf.colocation.immo.ui.paiements.PaiementsScreen
import bf.colocation.immo.ui.publication.PublierAnnonceScreen
import bf.colocation.immo.ui.auth.LoginScreen
import bf.colocation.immo.ui.auth.RegisterScreen
import bf.colocation.immo.ui.components.LoadingBox
import bf.colocation.immo.ui.favoris.FavorisScreen
import bf.colocation.immo.ui.notifications.NotificationsScreen
import bf.colocation.immo.ui.rendezvous.RendezVousScreen
import bf.colocation.immo.ui.profil.ProfilScreen
import bf.colocation.immo.ui.tableaudebord.TableauDeBordScreen

@Composable
fun AppNavigation(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val logged by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
    when (logged) {
        null -> LoadingBox()
        false -> AuthNavHost()
        true -> MainNavHost()
    }
}

@Composable
private fun AuthNavHost() {
    val nav = rememberNavController()
    // Le parcours visiteur commence par l'accueil, pas par le formulaire de
    // connexion : le catalogue est public et doit pouvoir être consulté d'abord.
    NavHost(navController = nav, startDestination = Routes.ACCUEIL) {
        composable(Routes.ACCUEIL) {
            AccueilScreen(
                onParcourir = { nav.navigate(Routes.ANNONCES) },
                onSeConnecter = { nav.navigate(Routes.LOGIN) },
                onCreerCompte = { nav.navigate(Routes.REGISTER) }
            )
        }
        // Catalogue consultable sans compte, comme sur le site.
        composable(Routes.ANNONCES) {
            AnnonceListScreen(onOpenAnnonce = { id -> nav.navigate(Routes.detail(id)) })
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            AnnonceDetailScreen(
                onBack = { nav.popBackStack() },
                connecte = false,
                onCreerCompte = { nav.navigate(Routes.REGISTER) },
                onOuvrirConversation = { convId -> nav.navigate(Routes.fil(convId)) }
            )
        }
        composable(Routes.LOGIN) {
            // Après login, le token est sauvé -> le flux isLoggedIn bascule et l'arbre est remplacé.
            LoginScreen(
                onLoggedIn = {},
                onGoRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onBack = { nav.popBackStack() })
        }
    }
}

private data class Onglet(val route: String, val label: String, val icon: ImageVector)

// Material 3 limite la barre inférieure à cinq entrées : au-delà, les libellés se
// compriment et se coupent. Les parcours secondaires (visites, paiements, favoris,
// alertes, notifications) sont donc regroupés dans l'onglet « Services ».
private val onglets = listOf(
    Onglet(Routes.TABLEAU, "Accueil", Icons.Default.Dashboard),
    Onglet(Routes.ANNONCES, "Annonces", Icons.Default.Home),
    Onglet(Routes.PUBLIER, "Publier", Icons.Default.AddCircle),
    Onglet(Routes.SERVICES, "Services", Icons.Default.Apps),
    Onglet(Routes.PROFIL, "Profil", Icons.Default.Person)
)

@Composable
private fun MainNavHost() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val routeActuelle = backStack?.destination?.route
    val afficherBarre = onglets.any { it.route == routeActuelle }

    Scaffold(
        bottomBar = {
            if (afficherBarre) {
                NavigationBar {
                    onglets.forEach { onglet ->
                        NavigationBarItem(
                            selected = routeActuelle == onglet.route,
                            onClick = {
                                nav.navigate(onglet.route) {
                                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(onglet.icon, null) },
                            label = {
                                // « Annonces » se cassait en deux lignes (« Annonce » / « s ») :
                                // le libellé est désormais tenu sur une seule ligne.
                                Text(
                                    onglet.label,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = Routes.TABLEAU,
            modifier = Modifier.padding(pad)
        ) {
            composable(Routes.TABLEAU) {
                TableauDeBordScreen(
                    onRechercher = { nav.navigate(Routes.ANNONCES) },
                    onFavoris = { nav.navigate(Routes.FAVORIS) },
                    onVisites = { nav.navigate(Routes.RENDEZ_VOUS) },
                    onAlertes = { nav.navigate(Routes.ALERTES) },
                    onOpenAnnonce = { id -> nav.navigate(Routes.detail(id)) }
                )
            }
            composable(Routes.ANNONCES) {
                AnnonceListScreen(onOpenAnnonce = { id -> nav.navigate(Routes.detail(id)) })
            }
            composable(Routes.PUBLIER) { PublierAnnonceScreen() }
            composable(Routes.SERVICES) {
                ServicesScreen(onOuvrir = { route -> nav.navigate(route) })
            }
            composable(Routes.FAVORIS) {
                FavorisScreen(onOpenAnnonce = { id -> nav.navigate(Routes.detail(id)) })
            }
            composable(Routes.ALERTES) { AlertesScreen() }
            composable(Routes.PAIEMENTS) { PaiementsScreen() }
            composable(Routes.RENDEZ_VOUS) { RendezVousScreen() }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen() }
            composable(Routes.MESSAGES) {
                ConversationsScreen(
                    onOuvrir = { convId -> nav.navigate(Routes.fil(convId)) },
                    onRetour = { nav.popBackStack() }
                )
            }
            composable(
                route = Routes.FIL_MESSAGES,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { entree ->
                val convId = entree.arguments?.getString("conversationId")?.toLongOrNull() ?: return@composable
                val vm: MessagerieViewModel = hiltViewModel()
                val st by vm.state.collectAsStateWithLifecycle()
                FilMessagesScreen(
                    conversationId = convId,
                    monId = st.monId ?: -1L,
                    titre = "Conversation",
                    onRetour = { nav.popBackStack() },
                    viewModel = vm
                )
            }
            composable(Routes.PROFIL) {
                // La déconnexion vide le token -> l'arbre repasse sur l'écran de login.
                ProfilScreen(onLoggedOut = {})
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                AnnonceDetailScreen(
                    onBack = { nav.popBackStack() },
                    onOuvrirConversation = { convId -> nav.navigate(Routes.fil(convId)) }
                )
            }
        }
    }
}
