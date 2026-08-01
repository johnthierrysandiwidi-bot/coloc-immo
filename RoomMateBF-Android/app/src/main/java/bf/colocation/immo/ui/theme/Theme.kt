package bf.colocation.immo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * Schémas de couleurs complets.
 *
 * Auparavant, seule une poignée de rôles était renseignée : Material 3 comblait le
 * reste avec sa palette violacée par défaut. Concrètement, les fonds de cartes, les
 * contours des champs et les icônes secondaires tiraient vers le gris-violet alors
 * que la marque est verte. Renseigner tous les rôles corrige cela partout d'un coup,
 * sans toucher au moindre écran.
 */

private val LightColors = lightColorScheme(
    primary = VertBF,
    onPrimary = Color.White,
    primaryContainer = VertBFClair,
    onPrimaryContainer = VertBFDark,

    secondary = OrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = OrangeClair,
    onSecondaryContainer = Color(0xFF7A3600),

    tertiary = RougeBF,
    onTertiary = Color.White,
    tertiaryContainer = RougeBFClair,
    onTertiaryContainer = Color(0xFF7A0A16),

    background = Fond,
    onBackground = TexteSombre,
    surface = Surface,
    onSurface = TexteSombre,
    surfaceVariant = SurfaceVariante,
    onSurfaceVariant = TexteGris,
    outline = Contour,
    outlineVariant = SurfaceVariante,

    error = RougeBF,
    onError = Color.White,
    errorContainer = RougeBFClair,
    onErrorContainer = Color(0xFF7A0A16)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5FD08A),
    onPrimary = Color(0xFF00391B),
    primaryContainer = VertBFDark,
    onPrimaryContainer = VertBFClair,

    secondary = Color(0xFFFFB870),
    onSecondary = Color(0xFF4A2300),
    secondaryContainer = Color(0xFF6B3200),
    onSecondaryContainer = OrangeClair,

    tertiary = Color(0xFFFFB3B6),
    onTertiary = Color(0xFF5F0512),
    tertiaryContainer = Color(0xFF8C1220),
    onTertiaryContainer = RougeBFClair,

    background = FondSombre,
    onBackground = TexteClair,
    surface = SurfaceSombre,
    onSurface = TexteClair,
    surfaceVariant = SurfaceVarianteSombre,
    onSurfaceVariant = TexteGrisClair,
    outline = ContourSombre,
    outlineVariant = SurfaceVarianteSombre,

    error = Color(0xFFFFB3B6),
    onError = Color(0xFF5F0512),
    errorContainer = Color(0xFF8C1220),
    onErrorContainer = RougeBFClair
)

@Composable
fun RoomMateBFTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
