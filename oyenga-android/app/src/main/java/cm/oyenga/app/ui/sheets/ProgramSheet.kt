@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.MASS_PARTS
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.SongIcon
import cm.oyenga.app.ui.components.subtitle
import cm.oyenga.app.ui.screens.readableDate
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Le programme complet de la célébration, partie par partie.
 *
 * Chaque partie montre le chant recommandé et laisse déplier les alternatives : le
 * jour de la messe, l'animateur doit pouvoir basculer sur un autre chant sans passer
 * par l'administration.
 */
@UnstableApi
@Composable
fun ProgramSheet(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val readingsState by viewModel.readings.collectAsStateWithLifecycle()
    val day = viewModel.currentReadings()
    val program = db.program
    var expanded by remember { mutableStateOf(setOf<String>()) }

    SheetHeader(
        title = "Programme de chants",
        subtitle = listOfNotNull(
            day.title.ifBlank { program?.fete }?.takeIf { it.isNotBlank() },
            readableDate(readingsState.targetDate).takeIf { it.isNotBlank() }
                ?.replaceFirstChar { it.uppercase() },
        ).joinToString(" · "),
    )

    SheetBody {
        if (program == null) {
            EmptyState(Ic.program, "Aucun programme publié pour le moment.")
            Spacer(Modifier.height(Space.s6))
            return@SheetBody
        }

        MASS_PARTS.forEach { part ->
            val item = program.items[part.key] ?: return@forEach
            val recommended = songs.firstOrNull { it.id == item.recommended } ?: return@forEach
            val alternatives = item.list
                .filterNot { it == recommended.id }
                .mapNotNull { id -> songs.firstOrNull { it.id == id } }
            val open = part.key in expanded

            Column(Modifier.padding(vertical = Space.s2)) {
                Kicker(part.label)
                Spacer(Modifier.height(Space.s2))

                Surface(
                    shape = OyengaShapes.lg,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(Space.s3),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.s3),
                    ) {
                        SongIcon(recommended.moment, size = 44.dp)
                        Column(Modifier.weight(1f)) {
                            Text(
                                recommended.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                recommended.subtitle(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        FilledTonalIconButton(onClick = { viewModel.play(recommended) }) {
                            OyIcon(
                                Ic.play, "Écouter ${recommended.title}",
                                size = IconSize.inline,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                if (alternatives.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            expanded = if (open) expanded - part.key else expanded + part.key
                        },
                    ) {
                        OyIcon(
                            if (open) Ic.chevronUp else Ic.chevronDown, null,
                            size = IconSize.inline,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.size(Space.s2))
                        Text(
                            if (open) {
                                "Masquer les alternatives"
                            } else {
                                "${alternatives.size} alternative" + if (alternatives.size > 1) "s" else ""
                            },
                        )
                    }

                    AnimatedVisibility(visible = open) {
                        Column {
                            alternatives.forEach { song ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = Space.s6, top = Space.s2, bottom = Space.s2),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.s3),
                                ) {
                                    SongIcon(song.moment, size = 36.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            song.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            song.subtitle(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = { viewModel.play(song) },
                                        modifier = Modifier.size(36.dp),
                                    ) {
                                        OyIcon(
                                            Ic.play, "Écouter ${song.title}",
                                            size = 18.dp,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Space.s2))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        Spacer(Modifier.height(Space.s6))
    }
}
