package bf.colocation.immo.core

import bf.colocation.immo.data.remote.dto.AnnonceDto
import kotlin.math.abs

/**
 * Visuels d'annonce, alignés sur le comportement du site.
 *
 * Le classement des photos par sujet repose sur une analyse d'image et reste
 * approximatif. La logique est donc conçue pour qu'une erreur soit sans
 * conséquence : la catégorie « terrain » est resserrée aux photos les plus sûres,
 * et une annonce de terrain puise dans un ensemble « extérieurs » — jamais dans
 * les intérieurs. Une façade mal rangée sur un terrain reste plausible ; une salle
 * de bain ne le serait pas.
 *
 * Les mêmes listes existent côté site, dans utils/illustrations.ts : les tenir
 * identiques garantit qu'une annonce s'illustre pareillement sur les deux clients.
 */
object Illustrations {

    private val TERRAINS_STRICTS = listOf(3, 7, 65, 66, 70)
    private val EXTERIEURS = listOf(
        4, 5, 11, 12, 13, 15, 17, 21, 23, 24, 27, 29, 39, 45, 46,
        51, 55, 56, 60, 64, 67, 69, 71, 72, 77, 80
    )
    private val INTERIEURS = listOf(
        1, 2, 6, 8, 9, 10, 14, 16, 18, 19, 20, 22, 25, 26, 28,
        30, 31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 47,
        48, 49, 50, 52, 53, 54, 57, 58, 59, 61, 62, 63, 68, 73, 74,
        75, 76, 78, 79, 81
    )

    // Une annonce de terrain ne voit que des vues d'extérieur (parcelles + façades).
    private val POOL_TERRAIN = TERRAINS_STRICTS + EXTERIEURS

    private const val PAR_ANNONCE = 4

    /** Seuls les fichiers hébergés par la plateforme sont dignes de confiance. */
    fun estFiable(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val chemin = url.removePrefix(Constants.SERVER_ROOT)
        return chemin.startsWith("/api/files/") ||
            chemin.startsWith("/photos/") ||
            chemin.startsWith("/illustrations/")
    }

    private fun url(numero: Int): String =
        Constants.SERVER_ROOT + "/photos/photo-" + numero.toString().padStart(2, '0') + ".jpg"

    /** Sélection déterministe de n éléments, à partir d'une graine. */
    private fun choisir(source: List<Int>, n: Int, graine: Int): List<String> {
        val debut = graine % source.size
        return (0 until n).map { url(source[(debut + it) % source.size]) }
    }

    private fun estTerrain(annonce: AnnonceDto): Boolean {
        val t = (annonce.titre.orEmpty() + " " + annonce.type.orEmpty()).lowercase()
        return t.contains("terrain") || t.contains("parcelle")
    }

    /**
     * Série d'une annonce : une vue d'ensemble d'abord, puis l'intérieur.
     *
     * La sélection dépend de l'identifiant : une annonce montre toujours les mêmes
     * photos, ce qu'un tirage aléatoire ne permettrait pas.
     */
    fun serie(annonce: AnnonceDto): List<String> {
        val graine = abs(annonce.id.toInt())
        if (estTerrain(annonce)) {
            return choisir(POOL_TERRAIN, minOf(3, POOL_TERRAIN.size), graine)
        }
        return choisir(EXTERIEURS, 1, graine) + choisir(INTERIEURS, PAR_ANNONCE - 1, graine * 3)
    }

    /** Visuel principal, utilisé sur les cartes du catalogue. */
    fun principale(annonce: AnnonceDto): String = serie(annonce).first()

    /** Photos de l'annonce si elles sont fiables, sinon le fonds photographique. */
    fun visuels(annonce: AnnonceDto): List<String> {
        val photos = annonce.toutesLesImages.mapNotNull { it.toImageUrl() }.filter { estFiable(it) }
        return photos.ifEmpty { serie(annonce) }
    }
}
