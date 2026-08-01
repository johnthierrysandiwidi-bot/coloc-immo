package bf.colocation.immo.core

import bf.colocation.immo.BuildConfig

object Constants {
    /**
     * URL de base du backend Spring Boot (JHipster).
     *
     * L'URL est nettoyée avant usage : une simple espace saisie par mégarde dans
     * build.gradle.kts (« http:// 192.168.1.89:8080/ ») faisait échouer OkHttp au
     * moment de construire Retrofit, donc planter l'application dès son lancement,
     * avant même l'affichage du premier écran. On supprime les espaces et on garantit
     * la barre oblique finale qu'exige Retrofit.
     */
    val BASE_URL: String = BuildConfig.BASE_URL
        .filterNot { it.isWhitespace() }
        .let { if (it.endsWith("/")) it else "$it/" }

    /** Racine serveur sans le / final, pour construire les URLs d'images. */
    val SERVER_ROOT: String = BASE_URL.trimEnd('/')

    const val DEFAULT_PAGE_SIZE = 20
}

/**
 * Construit une URL d'image affichable à partir de ce que renvoie le backend
 * (photoUrl / photos[]). Gère les 3 cas possibles :
 *  - URL absolue (http...) -> telle quelle
 *  - chemin absolu (/api/files/...) -> préfixé par la racine serveur
 *  - simple nom de fichier -> supposé dans /api/files/images/
 * Ajuste la dernière branche si ton backend stocke autrement.
 */
fun String?.toImageUrl(): String? {
    if (this.isNullOrBlank()) return null
    return when {
        startsWith("http", ignoreCase = true) -> this
        startsWith("/") -> Constants.SERVER_ROOT + this
        else -> Constants.SERVER_ROOT + "/api/files/images/" + this
    }
}
