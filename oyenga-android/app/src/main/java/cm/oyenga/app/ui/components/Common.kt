@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.OyengaText
import cm.oyenga.app.ui.theme.Space

/**
 * Icône de l'application.
 *
 * La taille de rendu est toujours l'une des tailles de la charte ; passer par ce
 * composable évite les 17dp ou 31dp qui rendent les traits flous.
 */
@Composable
fun OyIcon(
    @DrawableRes res: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = IconSize.default,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(
        painter = painterResource(res),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}

/** Sur-titre en capitales : « TEXTES DU JOUR », « PROCHAINE MESSE ». */
@Composable
fun Kicker(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text.uppercase(), style = OyengaText.kicker, color = color)
}

/** En-tête de section : titre, sous-titre facultatif, action facultative. */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Space.s8, bottom = Space.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/**
 * État vide.
 *
 * Il dit ce qui manque et, quand c'est possible, quoi faire — un écran vide sans
 * explication est le moment où l'utilisateur croit que l'application est cassée.
 */
@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.s12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(80.dp)
                .clip(OyengaShapes.full)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            OyIcon(icon, null, size = IconSize.empty, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(Space.s4))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Space.s2))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/**
 * Pastille d'initiales.
 *
 * La teinte est dérivée du nom : deux personnes différentes gardent des pastilles
 * différentes d'un écran à l'autre, sans qu'on ait à stocker une couleur par compte.
 */
@Composable
fun InitialsAvatar(
    label: String,
    initials: String,
    size: Dp = 44.dp,
    highlighted: Boolean = false,
) {
    val palette = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
    )
    val (background, foreground) = if (highlighted) {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    } else {
        palette[(label.hashCode().mod(palette.size))]
    }

    Box(
        Modifier
            .size(size)
            .clip(OyengaShapes.full)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials.take(2).uppercase(),
            style = MaterialTheme.typography.titleSmall,
            color = foreground,
        )
    }
}

/** Ligne cliquable générique d'une liste : icône de tête, textes, contenu de queue. */
@Composable
fun ListRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(OyengaShapes.md)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = Space.s3, horizontal = Space.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        leading()
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}

/** Bandeau d'information discret — jamais en `error`, qui doit rester le signal d'un problème. */
@Composable
fun InfoNotice(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OyengaShapes.lg,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            Modifier.padding(Space.s4),
            horizontalArrangement = Arrangement.spacedBy(Space.s3),
        ) {
            OyIcon(icon, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.primary)
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Espace réservé sous les listes pour que le mini-lecteur ne masque pas le dernier élément. */
@Composable
fun BottomSpacer(extra: Dp = 0.dp) {
    Spacer(Modifier.height(120.dp + extra))
}

/** Formatage `m:ss` des durées. */
fun formatDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "${safe / 60}:${(safe % 60).toString().padStart(2, '0')}"
}
