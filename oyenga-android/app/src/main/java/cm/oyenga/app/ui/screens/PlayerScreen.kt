@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.player.PlaybackState
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.Sheet
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.formatDuration
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.MiniPlayerHeight
import cm.oyenga.app.ui.theme.Moments
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.OyengaText
import cm.oyenga.app.ui.theme.Space

/** Le lecteur plein écran. */
@UnstableApi
@Composable
fun PlayerScreen(viewModel: OyengaViewModel, playback: PlaybackState) {
    val song = playback.current ?: return
    val dark = isSystemInDarkTheme()
    val moment = Moments.of(song.moment)
    val liked = viewModel.isLiked(song.id)

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.s6),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = Space.s3)) {
            FilledTonalIconButton(onClick = viewModel::closePlayer) {
                OyIcon(
                    Ic.chevronDown, "Réduire le lecteur",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "EN LECTURE · ${moment.label}".uppercase(),
                    style = OyengaText.kicker,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(onClick = { viewModel.openSheet(Sheet.Partition) }) {
                OyIcon(Ic.score, "Partition")
            }
            IconButton(onClick = { viewModel.toggleLike() }) {
                OyIcon(
                    if (liked) Ic.heartFill else Ic.heart,
                    if (liked) "Retirer des favoris" else "Ajouter aux favoris",
                    tint = if (liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Pochette : un dégradé porté par la couleur du moment liturgique, pas une image
        // générique. Le chant se reconnaît à sa place dans la messe.
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(vertical = Space.s4)
                .clip(OyengaShapes.xxl)
                .background(
                    Brush.linearGradient(
                        listOf(moment.accent(dark), moment.container(dark)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            OyIcon(
                moment.iconRes, null,
                size = 96.dp,
                tint = if (dark) moment.onContainer(true) else MaterialTheme.colorScheme.onPrimary,
            )
            if (playback.isPlaying) {
                Equalizer(Modifier.align(Alignment.BottomCenter).padding(bottom = Space.s8))
            }
        }

        Text(
            song.title,
            style = OyengaText.serifTitle,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (song.credit.isNotBlank()) {
            Text(
                song.credit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (playback.simulated) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = Space.s2),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OyIcon(Ic.info, null, size = 14.dp)
                Spacer(Modifier.width(Space.s1))
                Text(
                    "Lecture simulée — ajoute un lien audio dans l'administration",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(Space.s5))

        Slider(
            value = playback.positionSeconds.toFloat(),
            onValueChange = { viewModel.playback.seekTo(it.toInt()) },
            valueRange = 0f..(playback.durationSeconds.coerceAtLeast(1)).toFloat(),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(
                formatDuration(playback.positionSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                formatDuration(playback.durationSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Space.s5))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel.playback::toggleShuffle) {
                OyIcon(
                    Ic.shuffle,
                    if (playback.shuffle) "Lecture aléatoire activée" else "Lecture aléatoire",
                    tint = if (playback.shuffle) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            IconButton(onClick = viewModel.playback::previous) {
                OyIcon(Ic.previous, "Chant précédent", size = IconSize.card, tint = MaterialTheme.colorScheme.onSurface)
            }
            FilledIconButton(
                onClick = viewModel.playback::toggle,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                OyIcon(
                    if (playback.isPlaying) Ic.pause else Ic.play,
                    if (playback.isPlaying) "Pause" else "Lecture",
                    size = IconSize.card,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            IconButton(onClick = viewModel.playback::next) {
                OyIcon(Ic.next, "Chant suivant", size = IconSize.card, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = viewModel.playback::toggleRepeat) {
                OyIcon(
                    Ic.repeat,
                    if (playback.repeat) "Répétition activée" else "Répéter",
                    tint = if (playback.repeat) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }

        Spacer(Modifier.height(Space.s6))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            PlayerAction(Ic.lyrics, "Paroles", Modifier.weight(1f)) { viewModel.openSheet(Sheet.Lyrics) }
            PlayerAction(Ic.score, "Partition", Modifier.weight(1f)) { viewModel.openSheet(Sheet.Partition) }
            PlayerAction(Ic.queue, "File", Modifier.weight(1f)) { viewModel.openSheet(Sheet.Queue) }
            PlayerAction(Ic.playlistAdd, "Playlist", Modifier.weight(1f)) { viewModel.openSheet(Sheet.Playlists) }
        }

        Spacer(Modifier.height(Space.s8))
    }
}

@Composable
private fun PlayerAction(icon: Int, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = OyengaShapes.lg,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            Modifier.padding(vertical = Space.s3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OyIcon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Space.s1))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Huit barres qui respirent : le seul mouvement décoratif de l'application. */
@Composable
private fun Equalizer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "eq")
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(8) { index ->
            val height by transition.animateFloat(
                initialValue = 8f,
                targetValue = 26f,
                animationSpec = infiniteRepeatable(
                    animation = tween(560 + index * 70, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar$index",
            )
            Box(
                Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(OyengaShapes.full)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)),
            )
        }
    }
}

/**
 * Le mini-lecteur.
 *
 * Il reste accessible sur tous les écrans : pendant une messe, on doit pouvoir mettre
 * en pause sans chercher où l'on se trouve dans l'application.
 */
@UnstableApi
@Composable
fun MiniPlayer(viewModel: OyengaViewModel, playback: PlaybackState) {
    val song = playback.current ?: return
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            LinearProgressIndicator(
                progress = { playback.progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(MiniPlayerHeight)
                    .padding(horizontal = Space.s3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.s2),
            ) {
                Box(
                    Modifier
                        .clip(OyengaShapes.md)
                        .clickable(onClick = viewModel::openPlayer),
                ) {
                    cm.oyenga.app.ui.components.SongIcon(song.moment, size = 44.dp)
                }
                Column(
                    Modifier
                        .weight(1f)
                        .clip(OyengaShapes.sm)
                        .clickable(onClick = viewModel::openPlayer)
                        .padding(horizontal = Space.s1),
                ) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        Moments.label(song.moment) + if (playback.simulated) " · aperçu" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                IconButton(onClick = { viewModel.openSheet(Sheet.Lyrics) }) {
                    OyIcon(Ic.lyrics, "Paroles", size = IconSize.inline)
                }
                IconButton(onClick = viewModel.playback::toggle) {
                    OyIcon(
                        if (playback.isPlaying) Ic.pause else Ic.play,
                        if (playback.isPlaying) "Pause" else "Lecture",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = viewModel::stopPlayback) {
                    OyIcon(Ic.close, "Arrêter la lecture", size = IconSize.inline)
                }
            }
        }
    }
}
