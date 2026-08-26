@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Rôles de couleur Material Design 3.
 *
 * Généré par l'algorithme HCT officiel de Google (material-color-utilities) à partir
 * de la couleur de marque OYENGA #9B221B — celle du bandeau liturgique de la PWA.
 *
 * Ne pas modifier à la main : régénérer depuis `google-design-kit/tokens/generateur-theme.html`.
 * Aucune couleur d'interface ne doit être écrite en dur ailleurs dans l'application ;
 * on passe toujours par un rôle (`MaterialTheme.colorScheme.*`).
 */
object OyengaPalette {
    val Seed = Color(0xFF9B221B)
}

/* ---------- Schéma clair ---------- */
internal val LightPrimary = Color(0xFFAE3027)
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFFFDAD5)
internal val LightOnPrimaryContainer = Color(0xFF410001)
internal val LightInversePrimary = Color(0xFFFFB4AA)
internal val LightSecondary = Color(0xFF775652)
internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFFFDAD5)
internal val LightOnSecondaryContainer = Color(0xFF2C1512)
internal val LightTertiary = Color(0xFF715C2E)
internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFFFCDFA6)
internal val LightOnTertiaryContainer = Color(0xFF261A00)
internal val LightBackground = Color(0xFFFFF8F7)
internal val LightOnBackground = Color(0xFF201A19)
internal val LightSurface = Color(0xFFFFF8F7)
internal val LightOnSurface = Color(0xFF201A19)
internal val LightSurfaceVariant = Color(0xFFF5DDDA)
internal val LightOnSurfaceVariant = Color(0xFF534341)
internal val LightSurfaceTint = Color(0xFFAE3027)
internal val LightInverseSurface = Color(0xFF362F2E)
internal val LightInverseOnSurface = Color(0xFFFBEEEC)
internal val LightError = Color(0xFFBA1A1A)
internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFFDAD6)
internal val LightOnErrorContainer = Color(0xFF410002)
internal val LightOutline = Color(0xFF857370)
internal val LightOutlineVariant = Color(0xFFD8C2BE)
internal val LightScrim = Color(0xFF000000)
internal val LightSurfaceBright = Color(0xFFFFF8F7)
internal val LightSurfaceDim = Color(0xFFE4D7D5)
internal val LightSurfaceContainer = Color(0xFFF8EBE9)
internal val LightSurfaceContainerHigh = Color(0xFFF3E5E3)
internal val LightSurfaceContainerHighest = Color(0xFFEDE0DE)
internal val LightSurfaceContainerLow = Color(0xFFFEF1EF)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)

/* ---------- Schéma sombre ---------- */
internal val DarkPrimary = Color(0xFFFFB4AA)
internal val DarkOnPrimary = Color(0xFF690003)
internal val DarkPrimaryContainer = Color(0xFF8C1712)
internal val DarkOnPrimaryContainer = Color(0xFFFFDAD5)
internal val DarkInversePrimary = Color(0xFFAE3027)
internal val DarkSecondary = Color(0xFFE7BDB7)
internal val DarkOnSecondary = Color(0xFF442926)
internal val DarkSecondaryContainer = Color(0xFF5D3F3B)
internal val DarkOnSecondaryContainer = Color(0xFFFFDAD5)
internal val DarkTertiary = Color(0xFFDFC38C)
internal val DarkOnTertiary = Color(0xFF3F2E04)
internal val DarkTertiaryContainer = Color(0xFF574419)
internal val DarkOnTertiaryContainer = Color(0xFFFCDFA6)
internal val DarkBackground = Color(0xFF181211)
internal val DarkOnBackground = Color(0xFFEDE0DE)
internal val DarkSurface = Color(0xFF181211)
internal val DarkOnSurface = Color(0xFFEDE0DE)
internal val DarkSurfaceVariant = Color(0xFF534341)
internal val DarkOnSurfaceVariant = Color(0xFFD8C2BE)
internal val DarkSurfaceTint = Color(0xFFFFB4AA)
internal val DarkInverseSurface = Color(0xFFEDE0DE)
internal val DarkInverseOnSurface = Color(0xFF362F2E)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFB4AB)
internal val DarkOutline = Color(0xFFA08C8A)
internal val DarkOutlineVariant = Color(0xFF534341)
internal val DarkScrim = Color(0xFF000000)
internal val DarkSurfaceBright = Color(0xFF3F3736)
internal val DarkSurfaceDim = Color(0xFF181211)
internal val DarkSurfaceContainer = Color(0xFF251E1D)
internal val DarkSurfaceContainerHigh = Color(0xFF2F2827)
internal val DarkSurfaceContainerHighest = Color(0xFF3B3332)
internal val DarkSurfaceContainerLow = Color(0xFF201A19)
internal val DarkSurfaceContainerLowest = Color(0xFF120D0C)
