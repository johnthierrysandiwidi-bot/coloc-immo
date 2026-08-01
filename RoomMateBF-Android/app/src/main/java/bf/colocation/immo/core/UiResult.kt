package bf.colocation.immo.core

/** Résultat générique d'un appel réseau/repository. */
sealed interface UiResult<out T> {
    data class Success<T>(val data: T) : UiResult<T>
    data class Error(val message: String) : UiResult<Nothing>
}

/**
 * Traduit une exception en message lisible en français.
 *
 * Le cas le plus fréquent en développement — le serveur joignable en théorie mais
 * qui refuse la connexion — n'était pas couvert : l'utilisateur voyait le message
 * brut d'OkHttp, en anglais (« Failed to connect to /10.0.0.1:8080 »).
 */
fun Throwable.toFrenchMessage(): String = when (this) {
    is java.net.UnknownHostException ->
        "Serveur introuvable. Vérifie l'adresse du backend."
    is java.net.SocketTimeoutException ->
        "Délai dépassé. Le serveur met trop de temps à répondre."
    is java.net.ConnectException ->
        "Connexion au serveur impossible. Vérifie qu'il est démarré, que le téléphone " +
            "est sur le même réseau Wi-Fi et que le pare-feu autorise le port 8080."
    is javax.net.ssl.SSLException ->
        "Connexion sécurisée impossible avec le serveur."
    is java.io.IOException ->
        "Problème de réseau. Vérifie ta connexion."
    is retrofit2.HttpException -> when (code()) {
        401 -> "Session expirée. Reconnecte-toi."
        403 -> "Tu n'as pas les droits pour cette action."
        404 -> "Élément introuvable."
        409 -> "Action impossible dans l'état actuel."
        in 500..599 -> "Le serveur a rencontré une erreur. Réessaie plus tard."
        else -> "La requête a échoué (code ${code()})."
    }
    else -> message ?: "Une erreur inattendue est survenue."
}
