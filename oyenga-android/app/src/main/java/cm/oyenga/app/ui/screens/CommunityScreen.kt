@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import cm.oyenga.app.data.model.Ad
import cm.oyenga.app.data.model.Community
import cm.oyenga.app.data.model.Post
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.Sheet
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.ChoirIllustration
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.InitialsAvatar
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

private enum class CommunityView { FEED, DISCOVER }

@UnstableApi
@Composable
fun CommunityScreen(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    var view by remember { mutableStateOf(CommunityView.FEED) }
    var query by remember { mutableStateOf("") }

    // Ordre du fil : les communautés suivies d'abord, puis ce qui plaît au reste du
    // groupe. Le classement est explicité en bas de l'écran — un fil trié sans
    // explication laisse croire à un ordre chronologique.
    val feed = remember(db.posts, db.user.subscriptions) {
        db.posts.sortedByDescending { post ->
            (if (post.communityId in db.user.subscriptions) 100 else 0) + post.likes
        }
    }
    val communities = remember(db.communities, query) {
        db.communities.filter { it.name.contains(query, ignoreCase = true) }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(start = Space.s6, end = Space.s6, top = Space.s4)) {
            item {
                Text("Communauté", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(Space.s4))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = view == CommunityView.FEED,
                        onClick = { view = CommunityView.FEED },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("Publications") }
                    SegmentedButton(
                        selected = view == CommunityView.DISCOVER,
                        onClick = { view = CommunityView.DISCOVER },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("Rechercher") }
                }
                Spacer(Modifier.height(Space.s4))
            }

            if (view == CommunityView.FEED) {
                itemsIndexed(feed, key = { _, post -> post.id }) { index, post ->
                    PostCard(
                        post = post,
                        community = db.community(post.communityId),
                        subscribed = viewModel.isSubscribed(post.communityId),
                        onLike = { viewModel.likePost(post) },
                        onSubscribe = {
                            db.community(post.communityId)?.let(viewModel::joinCommunity)
                        },
                        onOpen = { viewModel.openSheet(Sheet.PostDetail(post.id)) },
                    )
                    // Une annonce toutes les trois publications, uniquement dans l'espace
                    // communauté : la liturgie reste sans publicité.
                    if (index % 3 == 2 && db.ads.isNotEmpty()) {
                        AdCard(db.ads[(index / 3) % db.ads.size])
                    }
                }
                item {
                    if (feed.isEmpty()) {
                        EmptyState(Ic.community, "Aucune publication pour le moment.")
                    }
                    Spacer(Modifier.height(Space.s4))
                    InfoNotice(
                        Ic.info,
                        "Les publications des groupes publics sont visibles par tous. " +
                            "Le fil met en avant les communautés que tu suis. Aime une " +
                            "publication pour t'abonner et ne rien manquer.",
                    )
                }
            } else {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Rechercher une communauté…") },
                        leadingIcon = { OyIcon(Ic.search, null, size = IconSize.inline) },
                        singleLine = true,
                        shape = OyengaShapes.full,
                    )
                    Spacer(Modifier.height(Space.s4))
                }

                val mine = communities.filter { it.joined }
                val others = communities.filterNot { it.joined }

                if (mine.isNotEmpty()) {
                    item { Kicker("Mes communautés") }
                    communityItems(mine, viewModel)
                }
                if (others.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(Space.s4))
                        Kicker("Découvrir")
                    }
                    communityItems(others, viewModel)
                }
                if (communities.isEmpty()) {
                    item { EmptyState(Ic.search, "Aucune communauté trouvée.") }
                }
            }

            item { BottomSpacer() }
        }

        FloatingActionButton(
            onClick = { viewModel.openSheet(Sheet.Compose) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Space.s6, bottom = Space.s6),
        ) {
            OyIcon(Ic.add, "Écrire une publication", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@UnstableApi
private fun androidx.compose.foundation.lazy.LazyListScope.communityItems(
    communities: List<Community>,
    viewModel: OyengaViewModel,
) {
    items(communities.size) { index ->
        val community = communities[index]
        CommunityRow(
            community = community,
            subscribed = viewModel.isSubscribed(community.id),
            onJoin = { viewModel.joinCommunity(community) },
        )
    }
}

@Composable
private fun PostCard(
    post: Post,
    community: Community?,
    subscribed: Boolean,
    onLike: () -> Unit,
    onSubscribe: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        onClick = onOpen,
        shape = OyengaShapes.xl,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Space.s4),
    ) {
        Column(Modifier.padding(Space.s5)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialsAvatar(label = post.author, initials = post.authorInit)
                Spacer(Modifier.size(Space.s3))
                Column(Modifier.weight(1f)) {
                    Text(
                        post.author,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${community?.name ?: "Communauté"} · ${post.timeAgo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!subscribed) {
                    TextButton(onClick = onSubscribe) { Text("S'abonner") }
                }
            }

            Spacer(Modifier.height(Space.s3))
            Text(post.text, style = MaterialTheme.typography.bodyMedium)

            if (post.image == "choir") {
                Spacer(Modifier.height(Space.s3))
                ChoirIllustration()
            }

            Spacer(Modifier.height(Space.s3))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.s5),
            ) {
                Row(
                    Modifier.clickable(onClick = onLike),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OyIcon(
                        if (post.liked) Ic.thumbUpFill else Ic.thumbUp,
                        if (post.liked) "Retirer mon j'aime" else "J'aime",
                        size = IconSize.inline,
                        tint = if (post.liked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(Modifier.size(Space.s2))
                    Text("${post.likes}", style = MaterialTheme.typography.labelMedium)
                }
                Row(
                    Modifier.clickable(onClick = onOpen),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OyIcon(Ic.comment, "Commentaires", size = IconSize.inline)
                    Spacer(Modifier.size(Space.s2))
                    Text("${post.commentsList.size}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CommunityRow(community: Community, subscribed: Boolean, onJoin: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Space.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialsAvatar(
            label = community.name,
            initials = community.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
            size = 48.dp,
        )
        Spacer(Modifier.size(Space.s3))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    community.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.size(Space.s2))
                OyIcon(
                    if (community.isPrivate) Ic.lock else Ic.public,
                    if (community.isPrivate) "Groupe privé" else "Groupe public",
                    size = 16.dp,
                )
            }
            Text(
                "${community.type} · ${community.members} membre" + if (community.members > 1) "s" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (community.joined && subscribed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OyIcon(Ic.check, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(Space.s1))
                Text("Abonné", style = MaterialTheme.typography.labelMedium)
            }
        } else {
            OutlinedButton(onClick = onJoin) {
                Text(if (community.isPrivate) "Demander" else "Rejoindre")
            }
        }
    }
}

/** Encart sponsorisé, annoncé comme tel : la mention n'est jamais implicite. */
@Composable
private fun AdCard(ad: Ad) {
    Surface(
        shape = OyengaShapes.xl,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Space.s4),
    ) {
        Column(Modifier.padding(Space.s5)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OyIcon(Ic.campaign, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.size(Space.s2))
                Kicker("Annonce", color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Spacer(Modifier.height(Space.s2))
            Text(ad.title, style = MaterialTheme.typography.titleSmall)
            Text(ad.text, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(Space.s2))
            Text(ad.cta, style = MaterialTheme.typography.labelLarge)
        }
    }
}
