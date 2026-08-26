@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import cm.oyenga.app.data.liturgy.LiturgicalContext
import cm.oyenga.app.data.liturgy.SuggestionEngine
import cm.oyenga.app.data.model.FETES
import cm.oyenga.app.data.model.MASS_PARTS
import cm.oyenga.app.data.model.Program
import cm.oyenga.app.data.model.ProgramItem
import cm.oyenga.app.data.model.TEMPS_LITURGIQUES
import cm.oyenga.app.data.model.THEMES
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.SongIcon
import cm.oyenga.app.ui.components.subtitle
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Préparation du programme de la messe.
 *
 * Le bouton « Proposer » ne décide rien : il classe le répertoire selon le contexte
 * liturgique saisi et pré-remplit trois candidats par partie. Le choix final reste
 * celui de la chorale, et chaque proposition retenue peut être remplacée.
 */
@UnstableApi
@Composable
fun AdminPrograms(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val existing = db.program

    var date by remember(existing?.id) { mutableStateOf(existing?.date ?: "") }
    var fete by remember(existing?.id) { mutableStateOf(existing?.fete ?: "") }
    var saison by remember(existing?.id) { mutableStateOf(existing?.saison ?: "") }
    var annee by remember(existing?.id) { mutableStateOf(existing?.annee ?: "") }
    var feteTag by remember(existing?.id) { mutableStateOf("") }
    var themeTag by remember(existing?.id) { mutableStateOf("") }
    var items by remember(existing?.id) { mutableStateOf(existing?.items ?: emptyMap()) }
    var openPart by remember { mutableStateOf<String?>(null) }

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Text("Célébration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Space.s3))
            AdminField("Date (AAAA-MM-JJ)", date, { date = it }, placeholder = "2026-07-19")
            AdminField("Fête ou dimanche", fete, { fete = it }, placeholder = "16e dimanche du Temps Ordinaire")
            AdminField("Année liturgique", annee, { annee = it }, placeholder = "Année A")

            AdminSingleChoice(
                title = "Temps liturgique",
                options = TEMPS_LITURGIQUES.map { it to it },
                selected = saison,
                onSelect = { saison = if (saison == it) "" else it },
            )
            AdminSingleChoice(
                title = "Fête particulière",
                options = FETES.map { it to it },
                selected = feteTag,
                onSelect = { feteTag = if (feteTag == it) "" else it },
            )
            AdminSingleChoice(
                title = "Thème dominant",
                options = THEMES.map { it to it },
                selected = themeTag,
                onSelect = { themeTag = if (themeTag == it) "" else it },
            )

            Button(
                onClick = {
                    val context = LiturgicalContext(saison = saison, fete = feteTag, theme = themeTag)
                    items = MASS_PARTS.mapNotNull { part ->
                        val suggested = SuggestionEngine.topFor(songs, part, context)
                        if (suggested.isEmpty()) {
                            null
                        } else {
                            part.key to ProgramItem(
                                list = suggested.map { it.id },
                                rec = suggested.first().id,
                            )
                        }
                    }.toMap()
                    viewModel.toast("${items.size} parties pré-remplies — à valider")
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OyIcon(Ic.magic, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(Space.s2))
                Text("Proposer un programme")
            }

            Spacer(Modifier.height(Space.s3))
            InfoNotice(
                Ic.info,
                "La proposition s'appuie sur les étiquettes des chants (temps, fête, thème). " +
                    "Plus le répertoire est étiqueté dans l'onglet Chants, plus elle est juste.",
            )

            Spacer(Modifier.height(Space.s6))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))
            Text("Ordinaire de la messe", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Space.s3))
        }

        MASS_PARTS.forEach { part ->
            item(key = part.key) {
                val item = items[part.key]
                val recommended = songs.firstOrNull { it.id == item?.recommended }
                val open = openPart == part.key

                Column(Modifier.padding(bottom = Space.s4)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Kicker(part.label)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { openPart = if (open) null else part.key }) {
                            Text(if (open) "Fermer" else if (recommended == null) "Choisir" else "Changer")
                        }
                    }

                    if (recommended == null) {
                        Text(
                            "Aucun chant retenu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
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
                                        recommended.subtitle() +
                                            (item?.alternatives?.takeIf { it > 0 }
                                                ?.let { " · $it alternative(s)" } ?: ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(onClick = { items = items - part.key }) {
                                    OyIcon(
                                        Ic.close, "Retirer ce chant",
                                        size = IconSize.inline,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }

                    if (open) {
                        Spacer(Modifier.height(Space.s2))
                        val context = LiturgicalContext(saison = saison, fete = feteTag, theme = themeTag)
                        val candidates = SuggestionEngine.suggest(songs, part, context).take(12)
                        if (candidates.isEmpty()) {
                            Text(
                                "Aucun chant du répertoire ne correspond à cette partie.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        candidates.forEach { scored ->
                            val selected = scored.song.id == item?.recommended
                            val inList = item?.list?.contains(scored.song.id) == true
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Space.s2),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.s3),
                            ) {
                                SongIcon(scored.song.moment, size = 36.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        scored.song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        scored.song.subtitle(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(onClick = {
                                    val current = items[part.key] ?: ProgramItem()
                                    val list = if (inList) current.list else current.list + scored.song.id
                                    items = items + (part.key to current.copy(list = list, rec = scored.song.id))
                                }) {
                                    Text(if (selected) "Retenu" else "Retenir")
                                }
                                if (!inList) {
                                    TextButton(onClick = {
                                        val current = items[part.key] ?: ProgramItem()
                                        items = items + (
                                            part.key to current.copy(
                                                list = current.list + scored.song.id,
                                                rec = current.rec.ifBlank { scored.song.id },
                                            )
                                            )
                                    }) { Text("Alternative") }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Space.s2))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        item {
            Spacer(Modifier.height(Space.s4))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlinedButton(
                    onClick = { items = emptyMap() },
                    modifier = Modifier.weight(1f),
                ) { Text("Vider") }
                Button(
                    onClick = {
                        if (date.isBlank()) {
                            viewModel.toast("Renseigne la date de la célébration")
                        } else if (items.isEmpty()) {
                            viewModel.toast("Choisis au moins un chant")
                        } else {
                            viewModel.saveProgram(
                                Program(
                                    id = existing?.id ?: "prog-" + System.currentTimeMillis(),
                                    date = date.trim(),
                                    saison = saison,
                                    annee = annee.trim(),
                                    fete = fete.trim(),
                                    items = items,
                                ),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    OyIcon(Ic.save, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(Space.s2))
                    Text("Publier")
                }
            }
            BottomSpacer()
        }
    }
}
