@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.ui.Filters
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.Sheet
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.ListRow
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.SongIcon
import cm.oyenga.app.ui.components.formatDuration
import cm.oyenga.app.ui.components.subtitle
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.Moments
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

@UnstableApi
@Composable
fun CatalogueScreen(viewModel: OyengaViewModel) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()

    val results = remember(songs, ui.search, ui.filters) {
        filterSongs(songs, ui.search, ui.filters)
    }

    LazyColumn(
        contentPadding = PaddingValues(start = Space.s6, end = Space.s6, top = Space.s4),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Répertoire", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                FilledTonalButton(onClick = { viewModel.openSheet(Sheet.Playlists) }) {
                    OyIcon(
                        Ic.book, null,
                        size = IconSize.inline,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(Modifier.size(Space.s2))
                    Text("Ma musique")
                }
            }
        }

        item {
            Spacer(Modifier.height(Space.s4))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.s2),
            ) {
                OutlinedTextField(
                    value = ui.search,
                    onValueChange = viewModel::setSearch,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher un chant, une chorale…") },
                    leadingIcon = { OyIcon(Ic.search, null, size = IconSize.inline) },
                    trailingIcon = {
                        if (ui.search.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                OyIcon(Ic.close, "Effacer la recherche", size = IconSize.inline)
                            }
                        }
                    },
                    singleLine = true,
                    shape = OyengaShapes.full,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                )
                BadgedBox(
                    badge = { if (ui.filters.count > 0) Badge { Text(ui.filters.count.toString()) } },
                ) {
                    FilledTonalIconButton(onClick = { viewModel.openSheet(Sheet.Filters) }) {
                        OyIcon(
                            Ic.filter, "Filtrer le répertoire",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }

        item {
            Text(
                buildString {
                    append(results.size)
                    append(if (results.size > 1) " chants" else " chant")
                    if (ui.filters.count > 0) append(" · filtré")
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Space.s3),
            )
        }

        if (results.isEmpty()) {
            item {
                EmptyState(
                    icon = Ic.search,
                    message = "Aucun chant ne correspond à ta recherche.",
                    actionLabel = if (ui.filters.count > 0) "Effacer les filtres" else null,
                    onAction = { viewModel.setFilters(Filters()) },
                )
            }
        } else {
            items(results, key = { it.id }) { song ->
                val isPlaying = playback.isPlaying && playback.current?.id == song.id
                ListRow(
                    leading = { SongIcon(song.moment) },
                    title = song.title,
                    subtitle = song.subtitle(),
                    onClick = { viewModel.play(song) },
                    trailing = {
                        if (isPlaying) {
                            OyIcon(
                                Ic.equalizer, "En lecture",
                                size = IconSize.inline,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                formatDuration(song.duration),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        }

        item { BottomSpacer() }
    }
}

/**
 * Filtrage du répertoire.
 *
 * La recherche porte sur le titre, le crédit et le moment liturgique : on cherche
 * autant « communion » qu'un titre précis ou le nom d'une chorale.
 */
internal fun filterSongs(songs: List<Song>, query: String, filters: Filters): List<Song> {
    var result = songs
    val q = query.trim().lowercase()
    if (q.isNotEmpty()) {
        result = result.filter { song ->
            song.title.lowercase().contains(q) ||
                song.credit.lowercase().contains(q) ||
                Moments.label(song.moment).lowercase().contains(q)
        }
    }
    if (filters.moments.isNotEmpty()) result = result.filter { it.moment in filters.moments }
    if (filters.langs.isNotEmpty()) result = result.filter { it.lang in filters.langs }
    if (filters.temps.isNotEmpty()) result = result.filter { song -> song.temps.any { it in filters.temps } }
    if (filters.themes.isNotEmpty()) result = result.filter { song -> song.themes.any { it in filters.themes } }
    return result
}

/** Ligne de chant réutilisée par les feuilles (file d'attente, playlists, programme). */
@Composable
internal fun SongListRow(
    song: Song,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListRow(
        leading = { SongIcon(song.moment, size = 44.dp) },
        title = song.title,
        subtitle = song.subtitle(),
        onClick = onClick,
        trailing = trailing ?: {
            Box(Modifier.size(0.dp))
        },
    )
}

/** Colonne de contenu standard des feuilles modales. */
@Composable
internal fun SheetColumn(content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = Space.s6).fillMaxWidth()) { content() }
}
