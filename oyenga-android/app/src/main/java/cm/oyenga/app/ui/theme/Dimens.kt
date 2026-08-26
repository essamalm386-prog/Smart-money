@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Grille de 4dp. En cas de doute : 24dp — c'est le défaut Material 3 Expressive
 * pour le padding de carte et les gouttières.
 */
object Space {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 20.dp
    val s6 = 24.dp
    val s8 = 32.dp
    val s12 = 48.dp
    val s16 = 64.dp
}

/** Tailles d'icône de la charte. Ne jamais rendre une icône à une taille intermédiaire. */
object IconSize {
    val inline = 20.dp   // dans un texte, un chip, un badge
    val default = 24.dp  // interface par défaut
    val card = 40.dp     // carte, en-tête de section
    val empty = 48.dp    // état vide, illustration
}

/** Cible tactile minimale sur mobile. */
val MinTouchTarget = 48.dp

/** Hauteur du mini-lecteur, utilisée pour réserver l'espace en bas des écrans. */
val MiniPlayerHeight = 68.dp
