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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.FETES
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.data.model.TEMPS_LITURGIQUES
import cm.oyenga.app.data.model.THEMES
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.ListRow
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.SongIcon
import cm.oyenga.app.ui.components.subtitle
import cm.oyenga.app.ui.screens.filterSongs
import cm.oyenga.app.ui.Filters
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.Moments
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Gestion du répertoire.
 *
 * Un chant du recueil livré n'est jamais réécrit sur place : on enregistre l'écart avec
 * l'original, ce qui permet de revenir à la version imprimée d'un geste et de recevoir
 * les corrections du corpus dans une mise à jour sans perdre son travail.
 */
@UnstableApi
@Composable
fun AdminSongs(viewModel: OyengaViewModel) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Song?>(null) }
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Song?>(null) }

    val results = remember(songs, query) { filterSongs(songs, query, Filters()) }

    if (creating || editing != null) {
        SongEditor(
            viewModel = viewModel,
            original = editing,
            onDismiss = {
                creating = false
                editing = null
            },
        )
        return
    }

    confirmDelete?.let { song ->
        val isBase = viewModel.repository.catalog.byId.containsKey(song.id)
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(if (isBase) "Restaurer le chant du recueil ?" else "Supprimer ce chant ?") },
            text = {
                Text(
                    if (isBase) {
                        "« ${song.title} » revient à sa version d'origine. Tes modifications " +
                            "(paroles, liens, étiquettes) seront perdues."
                    } else {
                        "« ${song.title} » sera définitivement retiré du répertoire."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSong(song)
                    confirmDelete = null
                }) { Text(if (isBase) "Restaurer" else "Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Annuler") }
            },
        )
    }

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher un chant…") },
                    leadingIcon = { OyIcon(Ic.search, null, size = IconSize.inline) },
                    singleLine = true,
                    shape = OyengaShapes.full,
                )
                Button(onClick = { creating = true }) {
                    OyIcon(Ic.add, "Ajouter un chant", size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(Modifier.height(Space.s3))
            Text(
                "${results.size} chant(s) · ${viewModel.db.value.addedSongs.size} ajouté(s) · " +
                    "${viewModel.db.value.overrides.size} modifié(s)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.s3))
        }

        if (results.isEmpty()) {
            item { EmptyState(Ic.search, "Aucun chant ne correspond.") }
        } else {
            items(results, key = { it.id }) { song ->
                ListRow(
                    leading = { SongIcon(song.moment, size = 44.dp) },
                    title = song.title,
                    subtitle = buildString {
                        append(song.subtitle())
                        if (song.hasAudio) append(" · audio")
                        if (song.hasPartition) append(" · partition")
                    },
                    onClick = { editing = song },
                    trailing = {
                        Row {
                            TextButton(onClick = { editing = song }) { Text("Modifier") }
                            TextButton(onClick = { confirmDelete = song }) {
                                OyIcon(
                                    Ic.delete, "Supprimer",
                                    size = IconSize.inline,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    },
                )
            }
        }

        item { BottomSpacer() }
    }
}

/** Formulaire d'un chant : identité, contenu, ressources, étiquettes liturgiques. */
@UnstableApi
@Composable
private fun SongEditor(
    viewModel: OyengaViewModel,
    original: Song?,
    onDismiss: () -> Unit,
) {
    var draft by remember(original?.id) {
        mutableStateOf(
            original ?: Song(
                id = "OY-" + System.currentTimeMillis().toString().takeLast(8),
                title = "",
                moment = "entree",
            ),
        )
    }
    var durationText by remember(original?.id) { mutableStateOf(draft.duration.toString()) }
    val isBase = viewModel.repository.catalog.byId.containsKey(draft.id)

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    if (original == null) "Nouveau chant" else "Modifier le chant",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) { Text("Fermer") }
            }
            Spacer(Modifier.height(Space.s4))

            if (isBase) {
                InfoNotice(
                    Ic.info,
                    "Chant du recueil livré. Tes modifications sont enregistrées séparément : " +
                        "l'original reste récupérable à tout moment.",
                )
                Spacer(Modifier.height(Space.s4))
            }

            AdminField("Titre", draft.title, { draft = draft.copy(title = it) })
            AdminField(
                "Crédit",
                draft.credit,
                { draft = draft.copy(credit = it) },
                placeholder = "Auteur, chorale ou paroisse",
            )
            AdminField("Langue", draft.lang, { draft = draft.copy(lang = it) })

            AdminSingleChoice(
                title = "Moment de la messe",
                options = Moments.all.map { it.key to it.label },
                selected = draft.moment,
                onSelect = { draft = draft.copy(moment = it) },
            )

            AdminField(
                "Paroles",
                draft.lyrics,
                { draft = draft.copy(lyrics = it) },
                singleLine = false,
                minHeight = 220,
                supporting = "Une strophe par ligne. Le refrain peut être préfixé par « R. ».",
            )

            AdminField(
                "Lien audio",
                draft.audioUrl,
                { draft = draft.copy(audioUrl = it) },
                placeholder = "https://…/chant.mp3",
                supporting = "Un lien direct vers un fichier audio. Sans lien, la lecture reste simulée.",
            )
            AdminField(
                "Lien de la partition",
                draft.partitionUrl,
                { draft = draft.copy(partitionUrl = it) },
                placeholder = "https://…/partition.pdf",
            )
            AdminField(
                "Durée (secondes)",
                durationText,
                { value ->
                    durationText = value.filter { it.isDigit() }
                    draft = draft.copy(duration = durationText.toIntOrNull() ?: draft.duration)
                },
                numeric = true,
            )

            AdminChips(
                title = "Temps liturgiques",
                options = TEMPS_LITURGIQUES.map { it to it },
                selected = draft.temps.toSet(),
                onToggle = { value -> draft = draft.copy(temps = draft.temps.toggle(value)) },
                supporting = "Sert à proposer ce chant au bon moment de l'année.",
            )
            AdminChips(
                title = "Fêtes",
                options = FETES.map { it to it },
                selected = draft.fetes.toSet(),
                onToggle = { value -> draft = draft.copy(fetes = draft.fetes.toggle(value)) },
            )
            AdminChips(
                title = "Thèmes",
                options = THEMES.map { it to it },
                selected = draft.themes.toSet(),
                onToggle = { value -> draft = draft.copy(themes = draft.themes.toggle(value)) },
            )

            Spacer(Modifier.height(Space.s4))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Annuler") }
                Button(
                    onClick = {
                        if (draft.title.isBlank()) {
                            viewModel.toast("Le titre est obligatoire")
                        } else {
                            viewModel.saveSong(draft, original)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    OyIcon(Ic.save, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(Space.s2))
                    Text("Enregistrer")
                }
            }
            BottomSpacer()
        }
    }
}

private fun List<String>.toggle(value: String): List<String> =
    if (value in this) this - value else this + value
