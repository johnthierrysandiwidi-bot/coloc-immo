package bf.colocation.immo.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Chiffrement local des valeurs sensibles, adossé au Keystore Android.
 *
 * Les jetons d'authentification étaient jusqu'ici stockés en clair dans DataStore :
 * lisibles par quiconque accède au stockage privé de l'application (téléphone rooté,
 * sauvegarde, analyse hors ligne). On les chiffre désormais en AES/GCM.
 *
 * La clé n'est jamais manipulée par l'application : elle est générée et conservée
 * dans le Keystore, un composant isolé du système — souvent adossé à une puce
 * sécurisée. Le code ne peut que demander au Keystore de chiffrer ou déchiffrer ;
 * il ne voit jamais la clé elle-même, qui ne peut donc pas fuir avec le stockage.
 *
 * Le format produit est « iv:donnéesChiffrées », les deux en Base64. Le vecteur
 * d'initialisation (iv) est aléatoire à chaque écriture, comme l'exige GCM.
 */
object CoffreCle {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "colocimmo_token_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAILLE_TAG = 128
    private const val SEPARATEUR = ":"

    private fun cle(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        // Première utilisation : génération d'une clé AES 256 bits non exportable.
        val generateur = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generateur.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generateur.generateKey()
    }

    /** Chiffre une valeur ; renvoie « iv:données » en Base64. */
    fun chiffrer(clair: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, cle())
        val chiffre = cipher.doFinal(clair.toByteArray(Charsets.UTF_8))
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val donnees = Base64.encodeToString(chiffre, Base64.NO_WRAP)
        return iv + SEPARATEUR + donnees
    }

    /**
     * Déchiffre une valeur produite par [chiffrer].
     *
     * Tolérante : si l'entrée n'est pas au format attendu (ancienne valeur en clair,
     * clé changée après réinstallation), elle renvoie null plutôt que de faire planter
     * l'application. L'appelant traite alors ce cas comme une absence de jeton.
     */
    fun dechiffrer(stocke: String): String? = try {
        val parts = stocke.split(SEPARATEUR)
        if (parts.size != 2) return null
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val donnees = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, cle(), GCMParameterSpec(TAILLE_TAG, iv))
        String(cipher.doFinal(donnees), Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }
}
