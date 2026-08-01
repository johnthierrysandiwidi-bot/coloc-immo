package bf.colocation.immo.ui.navigation

object Routes {
    /** Accueil public : visible avant toute authentification. */
    const val ACCUEIL = "accueil"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val TABLEAU = "tableau-de-bord"
    const val ANNONCES = "annonces"
    const val PUBLIER = "publier"
    const val SERVICES = "services"
    const val FAVORIS = "favoris"
    const val ALERTES = "alertes"
    const val PAIEMENTS = "paiements"
    const val NOTIFICATIONS = "notifications"
    const val RENDEZ_VOUS = "rendez-vous"
    const val PROFIL = "profil"

    const val MESSAGES = "messages"
    const val FIL_MESSAGES = "messages/{conversationId}"
    fun fil(conversationId: Long) = "messages/$conversationId"

    const val DETAIL = "annonce/{id}"
    fun detail(id: Long) = "annonce/$id"
}
