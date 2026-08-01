package bf.colocation.immo.data.repository

/** Page de résultats : contenu + total renvoyé par le header X-Total-Count. */
data class Paged<T>(
    val items: List<T>,
    val total: Int
)
