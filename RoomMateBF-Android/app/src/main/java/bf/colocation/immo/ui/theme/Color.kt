package bf.colocation.immo.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Palette inspirée du drapeau burkinabè (vert / rouge / jaune), alignée sur le
 * client web pour que les deux plateformes se ressemblent.
 *
 * L'apport principal tient aux teintes qui manquaient : sans elles, Material 3
 * comblait les vides avec sa palette violacée par défaut, ce qui donnait des gris
 * froids jurant avec le vert de la marque (fonds de cartes, contours, icônes
 * secondaires). Un mode sombre complet est également défini.
 */

// ---- Teintes de marque ----
val VertBF = Color(0xFF0B7A3B)
val VertBFDark = Color(0xFF075C2C)
val VertBFClair = Color(0xFFD7EFE0)
val RougeBF = Color(0xFFCE1126)
val RougeBFClair = Color(0xFFFBDDE0)
val JauneBF = Color(0xFFFCD116)
val OrangeAccent = Color(0xFFEF6C00)
val OrangeClair = Color(0xFFFFE4CC)

// ---- Neutres (mode clair) ----
val Fond = Color(0xFFF7F8F6)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariante = Color(0xFFE7EBE6)
val TexteSombre = Color(0xFF1A1C19)
val TexteGris = Color(0xFF5B5F58)
val Contour = Color(0xFFC5CBC3)

// ---- Neutres (mode sombre) ----
val FondSombre = Color(0xFF111411)
val SurfaceSombre = Color(0xFF1A1F1B)
val SurfaceVarianteSombre = Color(0xFF2B322C)
val TexteClair = Color(0xFFE4E7E1)
val TexteGrisClair = Color(0xFFB6BDB3)
val ContourSombre = Color(0xFF3F473F)

// ---- États ----
val Succes = Color(0xFF2E7D32)
val SuccesClair = Color(0xFFD9EFDA)
val Alerte = Color(0xFFB26A00)
val AlerteClair = Color(0xFFFFECC7)
