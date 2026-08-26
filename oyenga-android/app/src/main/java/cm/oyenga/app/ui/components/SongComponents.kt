@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.Moments
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Vignette d'un chant : la couleur ET l'icône disent le moment liturgique.
 * Jamais la couleur seule — environ 8 % des hommes ont une déficience de la vision
 * des couleurs, et une vignette de communion doit rester distinguable d'un Kyrie.
 */
@Composable
fun SongIcon(moment: String, size: Dp = 48.dp) {
    val dark = isSystemInDarkTheme()
    val style = Moments.of(moment)
    Box(
        Modifier
            .size(size)
            .clip(OyengaShapes.lg)
            .background(style.container(dark)),
        contentAlignment = Alignment.Center,
    ) {
        OyIcon(
            style.iconRes,
            contentDescription = null,
            size = if (size >= 48.dp) IconSize.default else IconSize.inline,
            tint = style.onContainer(dark),
        )
    }
}

/** Étiquette d'un moment liturgique, à poser près d'un titre de chant. */
@Composable
fun MomentBadge(moment: String, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val style = Moments.of(moment)
    Row(
        modifier
            .clip(OyengaShapes.full)
            .background(style.container(dark))
            .padding(horizontal = Space.s3, vertical = Space.s1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s1),
    ) {
        OyIcon(style.iconRes, null, size = 16.dp, tint = style.onContainer(dark))
        Text(
            style.short,
            style = MaterialTheme.typography.labelSmall,
            color = style.onContainer(dark),
        )
    }
}

/** Sous-titre normalisé d'un chant : « Communion · ROBERT AKAMBA ». */
fun Song.subtitle(): String {
    val label = Moments.label(moment)
    return if (credit.isBlank()) label else "$label · $credit"
}
