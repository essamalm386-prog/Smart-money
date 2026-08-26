@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formes de la charte. Material 3 Expressive est plus généreux que Material classique :
 * la carte par défaut est à 24dp, les boutons sont entièrement arrondis.
 */
object OyengaShapes {
    val xs = RoundedCornerShape(4.dp)     // champs de saisie
    val sm = RoundedCornerShape(8.dp)     // chips, petits éléments
    val md = RoundedCornerShape(12.dp)    // alertes, éléments de liste
    val lg = RoundedCornerShape(16.dp)    // FAB, conteneurs moyens
    val xl = RoundedCornerShape(24.dp)    // cartes — le défaut Expressive
    val xxl = RoundedCornerShape(32.dp)   // dialogues, grandes surfaces
    val full = RoundedCornerShape(percent = 50)

    /** Feuille modale : arrondie en haut, à plat en bas. */
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

val OyengaMaterialShapes = Shapes(
    extraSmall = OyengaShapes.xs,
    small = OyengaShapes.sm,
    medium = OyengaShapes.md,
    large = OyengaShapes.lg,
    extraLarge = OyengaShapes.xl.copy(CornerSize(24.dp)),
)
