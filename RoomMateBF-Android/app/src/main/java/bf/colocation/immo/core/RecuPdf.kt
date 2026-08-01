package bf.colocation.immo.core

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import bf.colocation.immo.data.remote.dto.PaiementDto
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/**
 * Génère un reçu PDF de frais de visite, puis propose de le partager.
 *
 * Utilise l'API native [PdfDocument] d'Android : aucune bibliothèque externe, donc
 * rien à télécharger ni à faire grossir l'APK. Le fichier est écrit dans le cache de
 * l'application et exposé via un FileProvider — le seul moyen sûr de partager un
 * fichier avec d'autres applications depuis Android 7.
 *
 * Le reçu reprend, à l'identique du site, les informations du paiement et la mention
 * « démonstration » : la passerelle étant simulée, aucun débit réel n'a lieu.
 */
object RecuPdf {

    private const val LARGEUR = 595 // A4 en points (72 dpi)
    private const val HAUTEUR = 842
    private val VERT = Color.rgb(27, 94, 67)
    private val GRIS = Color.rgb(90, 90, 90)

    private fun moyenLisible(code: String?): String = when (code) {
        "ORANGE_MONEY" -> "Orange Money"
        "MOOV_MONEY" -> "Moov Money"
        "CARTE" -> "Carte bancaire"
        else -> code ?: "—"
    }

    private fun montantLisible(montant: Double?): String =
        if (montant == null) "—"
        else NumberFormat.getInstance(Locale.FRANCE).format(montant.toLong()) + " FCFA"

    fun genererEtPartager(context: Context, paiement: PaiementDto) {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(LARGEUR, HAUTEUR, 1).create())
        val canvas = page.canvas
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // En-tête vert plein
        p.color = VERT
        canvas.drawRect(0f, 0f, LARGEUR.toFloat(), 90f, p)
        p.color = Color.WHITE
        p.textSize = 30f
        p.isFakeBoldText = true
        canvas.drawText("ColocImmo", 40f, 55f, p)
        p.textSize = 14f
        p.isFakeBoldText = false
        canvas.drawText("Reçu de frais de visite", 350f, 55f, p)

        // Montant mis en avant
        p.color = VERT
        p.textSize = 34f
        p.isFakeBoldText = true
        p.textAlign = Paint.Align.CENTER
        canvas.drawText(montantLisible(paiement.montant), LARGEUR / 2f, 160f, p)
        p.color = GRIS
        p.textSize = 14f
        p.isFakeBoldText = false
        canvas.drawText("Somme placée en séquestre", LARGEUR / 2f, 185f, p)

        // Lignes de détail
        p.textAlign = Paint.Align.LEFT
        p.textSize = 14f
        var y = 250f
        val lignes = listOf(
            "Référence" to (paiement.reference ?: "—"),
            "Moyen de paiement" to moyenLisible(paiement.moyen),
            "Statut" to (paiement.statut ?: "—"),
            "Date" to (paiement.dateCreation?.take(10) ?: "—")
        )
        for ((libelle, valeur) in lignes) {
            p.color = GRIS
            canvas.drawText(libelle, 40f, y, p)
            p.color = Color.rgb(30, 30, 30)
            p.textAlign = Paint.Align.RIGHT
            canvas.drawText(valeur, LARGEUR - 40f, y, p)
            p.textAlign = Paint.Align.LEFT
            y += 28f
        }

        // Mention démonstration
        p.color = Color.rgb(150, 150, 150)
        p.textSize = 10f
        p.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Document généré par ColocImmo. Passerelle simulée — aucun montant réel n'est débité.",
            LARGEUR / 2f,
            HAUTEUR - 60f,
            p
        )

        doc.finishPage(page)

        // Écriture dans le cache, sous un sous-dossier connu du FileProvider.
        val dossier = File(context.cacheDir, "recus").apply { mkdirs() }
        val fichier = File(dossier, "recu-${paiement.reference ?: paiement.id ?: "colocimmo"}.pdf")
        fichier.outputStream().use { doc.writeTo(it) }
        doc.close()

        // Partage via FileProvider (obligatoire depuis Android 7 pour exposer un fichier).
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", fichier)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Partager le reçu"))
    }
}
