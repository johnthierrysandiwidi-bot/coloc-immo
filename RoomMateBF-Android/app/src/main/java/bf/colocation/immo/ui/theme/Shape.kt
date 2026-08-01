package bf.colocation.immo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Arrondis de l'application.
 *
 * Material 3 applique par défaut des rayons assez faibles (4 dp pour les petits
 * composants). Les cartes d'annonces utilisaient déjà 16 dp « en dur », ce qui créait
 * un décalage avec les boutons, champs et boîtes de dialogue restés anguleux.
 * Centraliser les formes ici harmonise l'ensemble et évite d'avoir à répéter des
 * valeurs dans chaque écran.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
