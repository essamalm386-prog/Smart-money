@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.Community
import cm.oyenga.app.data.model.TEMPS_LITURGIQUES
import cm.oyenga.app.data.model.THEMES
import cm.oyenga.app.ui.Filters
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.InitialsAvatar
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.ListRow
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.SongIcon
import cm.oyenga.app.ui.components.formatDuration
import cm.oyenga.app.ui.components.subtitle
import cm.oyenga.app.ui.screens.readableDate
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.Moments
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.OyengaText
import cm.oyenga.app.ui.theme.Space

// ------------------------------------------------------------------ paroles

@UnstableApi
@Composable
fun LyricsSheet(viewModel: OyengaViewModel) {
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val song = playback.current ?: return

    SheetHeader(title = song.title, subtitle = song.subtitle())
    SheetBody {
        if (song.lyrics.isBlank()) {
            EmptyState(Ic.lyrics, "Les paroles de ce chant ne sont pas encore saisies.")
        } else {
            // Les paroles sont un contenu long : police à empattements, interligne large.
            Text(song.lyrics, style = OyengaText.serifBodyLarge)
        }
        Spacer(Modifier.height(Space.s6))
    }
}

// ------------------------------------------------------------------ partition

@UnstableApi
@Composable
fun PartitionSheet(viewModel: OyengaViewModel) {
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val song = playback.current ?: return
    val uriHandler = LocalUriHandler.current

    SheetHeader(title = "Partition", subtitle = song.title)
    SheetBody {
        if (!song.hasPartition) {
            EmptyState(
                Ic.score,
                "Aucune partition n'est encore rattachée à ce chant. " +
                    "Un responsable peut en ajouter le lien depuis l'administration.",
            )
        } else {
            Surface(
                shape = OyengaShapes.lg,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(Space.s5)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OyIcon(Ic.pdf, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(Space.s3))
                        Text(
                            song.partitionUrl,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(Space.s4))
                    Button(
                        onClick = { runCatching { uriHandler.openUri(song.partitionUrl) } },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OyIcon(
                            Ic.openExternal, null,
                            size = IconSize.inline,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.size(Space.s2))
                        Text("Ouvrir la partition")
                    }
                }
            }
            Spacer(Modifier.height(Space.s3))
            InfoNotice(
                Ic.info,
                "La partition s'ouvre dans le lecteur de documents du téléphone : " +
                    "elle reste lisible et imprimable hors de l'application.",
            )
        }
        Spacer(Modifier.height(Space.s6))
    }
}

// ------------------------------------------------------------------ file d'attente

@UnstableApi
@Composable
fun QueueSheet(viewModel: OyengaViewModel) {
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()

    SheetHeader(title = "File d'attente", subtitle = "${playback.queue.size} chants")
    SheetBody {
        if (playback.queue.isEmpty()) {
            EmptyState(Ic.queue, "La file est vide.")
        } else {
            playback.queue.forEach { song ->
                val active = song.id == playback.current?.id
                ListRow(
                    leading = { SongIcon(song.moment, size = 44.dp) },
                    title = song.title,
                    subtitle = song.subtitle(),
                    onClick = { viewModel.play(song) },
                    trailing = {
                        if (active) {
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
        Spacer(Modifier.height(Space.s6))
    }
}

// ------------------------------------------------------------------ filtres

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun FiltersSheet(viewModel: OyengaViewModel) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf(ui.filters) }
    val languages = remember { viewModel.repository.languages() }

    SheetHeader(
        title = "Filtrer",
        subtitle = if (draft.isEmpty) "Aucun filtre actif" else "${draft.count} filtre(s)",
        action = {
            TextButton(onClick = { draft = Filters() }) { Text("Tout effacer") }
        },
    )

    SheetBody {
        FilterGroup(
            title = "Moment de la messe",
            options = Moments.all.map { it.key to it.label },
            selected = draft.moments,
            onToggle = { key -> draft = draft.copy(moments = draft.moments.toggle(key)) },
        )
        FilterGroup(
            title = "Langue",
            options = languages.map { it to it },
            selected = draft.langs,
            onToggle = { key -> draft = draft.copy(langs = draft.langs.toggle(key)) },
        )
        FilterGroup(
            title = "Temps liturgique",
            options = TEMPS_LITURGIQUES.map { it to it },
            selected = draft.temps,
            onToggle = { key -> draft = draft.copy(temps = draft.temps.toggle(key)) },
        )
        FilterGroup(
            title = "Thème",
            options = THEMES.map { it to it },
            selected = draft.themes,
            onToggle = { key -> draft = draft.copy(themes = draft.themes.toggle(key)) },
        )

        Spacer(Modifier.height(Space.s4))
        Button(
            onClick = {
                viewModel.setFilters(draft)
                viewModel.closeSheet()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (draft.isEmpty) "Voir tout le répertoire" else "Appliquer les filtres")
        }
        Spacer(Modifier.height(Space.s6))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterGroup(
    title: String,
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (options.isEmpty()) return
    Column(Modifier.padding(bottom = Space.s5)) {
        Kicker(title)
        Spacer(Modifier.height(Space.s2))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            options.forEach { (key, label) ->
                FilterChip(
                    selected = key in selected,
                    onClick = { onToggle(key) },
                    label = { Text(label) },
                    leadingIcon = if (key in selected) {
                        { OyIcon(Ic.check, null, size = 18.dp, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value

// ------------------------------------------------------------------ playlists

@UnstableApi
@Composable
fun PlaylistsSheet(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val current = playback.current
    var newName by remember { mutableStateOf("") }

    SheetHeader(
        title = "Ma musique",
        subtitle = current?.let { "Ajouter « ${it.title} »" } ?: "${db.playlists.size} playlist(s)",
    )

    SheetBody {
        Kicker("Favoris")
        Spacer(Modifier.height(Space.s2))
        val liked = songs.filter { it.id in db.user.likes }
        if (liked.isEmpty()) {
            Text(
                "Aucun favori pour l'instant — touche le cœur dans le lecteur.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            liked.forEach { song ->
                ListRow(
                    leading = { SongIcon(song.moment, size = 44.dp) },
                    title = song.title,
                    subtitle = song.subtitle(),
                    onClick = { viewModel.play(song) },
                    trailing = {
                        OyIcon(Ic.heartFill, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.primary)
                    },
                )
            }
        }

        Spacer(Modifier.height(Space.s5))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(Space.s5))

        Kicker("Playlists")
        Spacer(Modifier.height(Space.s2))
        if (db.playlists.isEmpty()) {
            Text(
                "Crée une playlist pour préparer une veillée ou une répétition.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        db.playlists.forEach { playlist ->
            val contains = current != null && current.id in playlist.songIds
            ListRow(
                leading = {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(OyengaShapes.lg)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        OyIcon(
                            Ic.queue, null,
                            size = IconSize.inline,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                },
                title = playlist.name,
                subtitle = "${playlist.songIds.size} chant(s)",
                onClick = {
                    if (current != null) viewModel.togglePlaylistSong(playlist.id, current.id)
                },
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (current != null) {
                            OyIcon(
                                if (contains) Ic.check else Ic.add,
                                if (contains) "Retirer de la playlist" else "Ajouter à la playlist",
                                size = IconSize.inline,
                                tint = if (contains) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        Spacer(Modifier.size(Space.s2))
                        Box(Modifier.clickable { viewModel.deletePlaylist(playlist.id) }) {
                            OyIcon(Ic.delete, "Supprimer la playlist", size = IconSize.inline)
                        }
                    }
                },
            )
        }

        Spacer(Modifier.height(Space.s4))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                label = { Text("Nouvelle playlist") },
                singleLine = true,
                shape = OyengaShapes.md,
            )
            Button(onClick = {
                viewModel.createPlaylist(newName, current?.id)
                newName = ""
            }) {
                Text("Créer")
            }
        }
        Spacer(Modifier.height(Space.s6))
    }
}

// ------------------------------------------------------------------ compte

@UnstableApi
@Composable
fun AccountSheet(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var name by remember(db.user.id) { mutableStateOf(db.user.displayName) }

    SheetHeader(title = "Mon compte")
    SheetBody {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(db.user.displayName, db.user.initials, size = 56.dp, highlighted = true)
            Spacer(Modifier.size(Space.s4))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(db.user.displayName, style = MaterialTheme.typography.titleLarge)
                    if (db.user.premium) {
                        Spacer(Modifier.size(Space.s2))
                        OyIcon(
                            Ic.premiumFill, "Compte premium",
                            size = IconSize.inline,
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                Text(
                    (if (db.user.isAdmin) "Responsable · Administrateur" else "Membre") +
                        " · ${db.user.subscriptions.size} abonnement(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(Space.s5))

        if (editing) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nom complet") },
                singleLine = true,
                shape = OyengaShapes.md,
            )
            Spacer(Modifier.height(Space.s3))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlinedButton(
                    onClick = {
                        name = db.user.displayName
                        editing = false
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Annuler") }
                Button(
                    onClick = {
                        viewModel.renameProfile(name)
                        editing = false
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Enregistrer") }
            }
        } else {
            OutlinedButton(onClick = { editing = true }, modifier = Modifier.fillMaxWidth()) {
                OyIcon(Ic.edit, null, size = IconSize.inline)
                Spacer(Modifier.size(Space.s2))
                Text("Modifier mon profil")
            }
        }

        if (db.user.isAdmin) {
            Spacer(Modifier.height(Space.s3))
            Button(
                onClick = {
                    viewModel.closeSheet()
                    viewModel.openAdmin()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OyIcon(Ic.admin, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(Space.s2))
                Text("Espace Administration")
            }
        }

        Spacer(Modifier.height(Space.s6))
        Kicker("Changer d'utilisateur")
        Spacer(Modifier.height(Space.s2))
        db.accounts.forEach { account ->
            val current = account.id == db.user.id
            ListRow(
                leading = {
                    InitialsAvatar(account.displayName, account.initials, size = 42.dp, highlighted = current)
                },
                title = account.displayName,
                subtitle = buildString {
                    append(if (account.isAdmin) "Responsable" else "Membre")
                    if (account.premium) append(" · Premium")
                },
                onClick = { if (!current) viewModel.switchAccount(account) },
                trailing = {
                    if (current) {
                        Text(
                            "Actuel",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        Text("Se connecter", style = MaterialTheme.typography.labelMedium)
                    }
                },
            )
        }
        Spacer(Modifier.height(Space.s6))
    }
}

// ------------------------------------------------------------------ publier

@UnstableApi
@Composable
fun ComposeSheet(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val mine = db.communities.filter { it.joined }
    var target by remember { mutableStateOf(mine.firstOrNull()?.id ?: db.communities.firstOrNull()?.id.orEmpty()) }
    var text by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var songId by remember { mutableStateOf("") }
    var songQuery by remember { mutableStateOf("") }

    SheetHeader(title = "Nouvelle publication", subtitle = "Publiée sous ${db.user.displayName}")
    SheetBody {
        Kicker("Publier dans")
        Spacer(Modifier.height(Space.s2))
        db.communities.forEach { community ->
            ListRow(
                leading = {
                    OyIcon(
                        if (community.id == target) Ic.check else Ic.community,
                        null,
                        tint = if (community.id == target) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                },
                title = community.name,
                subtitle = if (community.isPrivate) "Groupe privé" else "Groupe public",
                onClick = { target = community.id },
            )
        }

        Spacer(Modifier.height(Space.s4))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            label = { Text("Ton message") },
            placeholder = { Text("Répétition, remerciements, partage d'un chant…") },
            shape = OyengaShapes.md,
        )

        Spacer(Modifier.height(Space.s3))
        OutlinedTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Lien de la vidéo") },
            placeholder = { Text("https://…/repetition.mp4") },
            supportingText = { Text("Facultatif. Sans vidéo, le fil affiche une carte de la chorale.") },
            singleLine = true,
            shape = OyengaShapes.md,
        )

        Spacer(Modifier.height(Space.s4))
        Kicker("Chant mis en avant")
        Text(
            "Il apparaît en bas de la publication ; le toucher lance la lecture.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.s2))
        OutlinedTextField(
            value = songQuery,
            onValueChange = { songQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Chercher un chant…") },
            leadingIcon = { OyIcon(Ic.search, null, size = IconSize.inline) },
            singleLine = true,
            shape = OyengaShapes.full,
        )
        val matches = remember(songs, songQuery) {
            if (songQuery.isBlank()) emptyList()
            else songs.filter { it.title.contains(songQuery, ignoreCase = true) }.take(6)
        }
        songs.firstOrNull { it.id == songId }?.let { chosen ->
            ListRow(
                leading = { SongIcon(chosen.moment, size = 40.dp) },
                title = chosen.title,
                subtitle = chosen.subtitle(),
                onClick = { songId = "" },
                trailing = {
                    OyIcon(Ic.close, "Retirer le chant", size = IconSize.inline)
                },
            )
        }
        matches.forEach { song ->
            ListRow(
                leading = { SongIcon(song.moment, size = 40.dp) },
                title = song.title,
                subtitle = song.subtitle(),
                onClick = {
                    songId = song.id
                    songQuery = ""
                },
            )
        }

        Spacer(Modifier.height(Space.s4))
        Button(
            onClick = { viewModel.publish(target, text, videoUrl, songId) },
            modifier = Modifier.fillMaxWidth(),
            enabled = text.isNotBlank() && target.isNotBlank(),
        ) {
            OyIcon(Ic.send, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.size(Space.s2))
            Text("Publier")
        }
        Spacer(Modifier.height(Space.s6))
    }
}

// ------------------------------------------------------------------ découvrir

/**
 * La recherche de communautés, sortie du fil.
 *
 * Le fil est plein écran : y loger un second mode d'affichage l'aurait alourdi.
 * La découverte devient une feuille, atteignable depuis la loupe du fil.
 */
@UnstableApi
@Composable
fun DiscoverSheet(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val results = remember(db.communities, query) {
        db.communities.filter { it.name.contains(query, ignoreCase = true) }
    }

    SheetHeader(
        title = "Communautés",
        subtitle = "${db.user.subscriptions.size} abonnement(s)",
    )
    SheetBody {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher une communauté…") },
            leadingIcon = { OyIcon(Ic.search, null, size = IconSize.inline) },
            singleLine = true,
            shape = OyengaShapes.full,
        )

        val mine = results.filter { it.joined }
        val others = results.filterNot { it.joined }

        if (mine.isNotEmpty()) {
            Spacer(Modifier.height(Space.s5))
            Kicker("Mes communautés")
            Spacer(Modifier.height(Space.s2))
            mine.forEach { community -> CommunityRow(viewModel, community) }
        }
        if (others.isNotEmpty()) {
            Spacer(Modifier.height(Space.s5))
            Kicker("Découvrir")
            Spacer(Modifier.height(Space.s2))
            others.forEach { community -> CommunityRow(viewModel, community) }
        }
        if (results.isEmpty()) {
            EmptyState(Ic.search, "Aucune communauté trouvée.")
        }
        Spacer(Modifier.height(Space.s6))
    }
}

@UnstableApi
@Composable
private fun CommunityRow(viewModel: OyengaViewModel, community: Community) {
    val subscribed = viewModel.isSubscribed(community.id)
    ListRow(
        leading = {
            InitialsAvatar(
                label = community.name,
                initials = community.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                size = 44.dp,
            )
        },
        title = community.name,
        subtitle = "${community.type} · ${community.members} membre" + if (community.members > 1) "s" else "",
        trailing = {
            if (subscribed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OyIcon(Ic.check, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(Space.s1))
                    Text("Abonné", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                OutlinedButton(onClick = { viewModel.joinCommunity(community) }) {
                    Text(if (community.isPrivate) "Demander" else "Rejoindre")
                }
            }
        },
    )
}

// ------------------------------------------------------------------ détail d'une publication

@UnstableApi
@Composable
fun PostDetailSheet(viewModel: OyengaViewModel, postId: String) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val post = db.post(postId) ?: return
    val community = db.community(post.communityId)
    var comment by remember { mutableStateOf("") }
    val context = LocalContext.current

    SheetHeader(
        title = community?.name ?: "Publication",
        subtitle = "${post.author} · ${post.timeAgo}",
        action = {
            if (db.user.isAdmin) {
                TextButton(onClick = {
                    viewModel.deletePost(post.id)
                    viewModel.closeSheet()
                }) { Text("Supprimer") }
            }
        },
    )

    SheetBody {
        Text(post.text, style = MaterialTheme.typography.bodyLarge)

        if (post.image == "choir") {
            Spacer(Modifier.height(Space.s4))
            cm.oyenga.app.ui.components.ChoirIllustration()
        }

        Spacer(Modifier.height(Space.s4))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clickable { viewModel.likePost(post) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OyIcon(
                        if (post.liked) Ic.thumbUpFill else Ic.thumbUp,
                        "J'aime",
                        size = IconSize.inline,
                        tint = if (post.liked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(Modifier.size(Space.s2))
                    Text("${post.likes} j'aime", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.size(Space.s5))
            Text(
                "${post.commentsList.size} commentaires",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Space.s4))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(Space.s4))

        post.commentsList.forEach { entry ->
            Row(Modifier.padding(bottom = Space.s4)) {
                InitialsAvatar(entry.author, entry.init, size = 36.dp)
                Spacer(Modifier.size(Space.s3))
                Column {
                    Text(entry.author, style = MaterialTheme.typography.titleSmall)
                    Text(entry.text, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        entry.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.weight(1f),
                label = { Text("Écrire un commentaire") },
                shape = OyengaShapes.md,
            )
            Button(
                onClick = {
                    viewModel.comment(post.id, comment)
                    comment = ""
                },
                enabled = comment.isNotBlank(),
            ) {
                OyIcon(Ic.send, "Envoyer", size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(Space.s6))
    }
}
