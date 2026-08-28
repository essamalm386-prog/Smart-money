@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.screens

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import cm.oyenga.app.data.model.Ad
import cm.oyenga.app.data.model.Community
import cm.oyenga.app.data.model.Post
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.Sheet
import cm.oyenga.app.ui.components.ChoirIllustration
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InitialsAvatar
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Le fil de la communauté, en plein écran.
 *
 * Une publication occupe tout l'écran et on passe à la suivante d'un glissement
 * vertical : c'est la grammaire que tout le monde connaît, et c'est celle qui met
 * la chorale en avant plutôt que l'interface.
 *
 * Le média est le fond ; tout le reste flotte par-dessus. Les textes sont blancs
 * sur un voile sombre : sur une vidéo quelconque, aucun rôle de couleur du thème ne
 * peut garantir le contraste — c'est le voile qui le garantit. C'est la seule zone
 * de l'application où la couleur n'est pas lue dans le thème.
 */
private sealed interface FeedItem {
    data class Publication(val post: Post) : FeedItem
    data class Sponsored(val ad: Ad) : FeedItem
}

/** Une annonce toutes les quatre publications, en page entière et signalée comme telle. */
private fun buildFeed(posts: List<Post>, ads: List<Ad>, subscriptions: List<String>): List<FeedItem> {
    val ordered = posts.sortedByDescending { post ->
        (if (post.communityId in subscriptions) 100 else 0) + post.likes
    }
    if (ads.isEmpty()) return ordered.map(FeedItem::Publication)

    val feed = mutableListOf<FeedItem>()
    ordered.forEachIndexed { index, post ->
        feed += FeedItem.Publication(post)
        if (index % 4 == 3) feed += FeedItem.Sponsored(ads[(index / 4) % ads.size])
    }
    return feed
}

@UnstableApi
@Composable
fun CommunityScreen(viewModel: OyengaViewModel, bottomInset: Dp) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()

    val feed = remember(db.posts, db.ads, db.user.subscriptions) {
        buildFeed(db.posts, db.ads, db.user.subscriptions)
    }

    if (feed.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Ic.community,
                message = "Aucune publication pour le moment.",
                actionLabel = "Publier la première",
                onAction = { viewModel.openSheet(Sheet.Compose) },
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { feed.size })
    val player = rememberFeedPlayer()

    // Seule la page posée à l'écran joue. Changer de page arrête la précédente :
    // deux vidéos qui se parlent par-dessus, c'est l'erreur classique de ce format.
    LaunchedEffect(pagerState.settledPage, feed) {
        val item = feed.getOrNull(pagerState.settledPage)
        val url = (item as? FeedItem.Publication)?.post?.videoUrl.orEmpty()
        if (url.isBlank()) {
            player.pause()
            player.clearMediaItems()
        } else {
            // La vidéo prend la main sur le son : on ne superpose pas un chant et une vidéo.
            viewModel.playback.pause()
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            when (val item = feed[page]) {
                is FeedItem.Publication -> PublicationPage(
                    viewModel = viewModel,
                    post = item.post,
                    community = db.community(item.post.communityId),
                    song = songs.firstOrNull { it.id == item.post.songId },
                    subscribed = viewModel.isSubscribed(item.post.communityId),
                    active = page == pagerState.settledPage,
                    player = player,
                    bottomInset = bottomInset,
                )

                is FeedItem.Sponsored -> SponsoredPage(item.ad, bottomInset)
            }
        }

        FeedTopBar(
            onSearch = { viewModel.openSheet(Sheet.Discover) },
            onCompose = { viewModel.openSheet(Sheet.Compose) },
        )
    }
}

/** Un lecteur vidéo pour tout le fil, libéré avec l'écran. */
@UnstableApi
@Composable
private fun rememberFeedPlayer(): ExoPlayer {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false
        }
    }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }
    return player
}

// ---------------------------------------------------------------- page publication

@UnstableApi
@Composable
private fun PublicationPage(
    viewModel: OyengaViewModel,
    post: Post,
    community: Community?,
    song: Song?,
    subscribed: Boolean,
    active: Boolean,
    player: ExoPlayer,
    bottomInset: Dp,
) {
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        MediaStage(post = post, active = active, player = player)
        Scrims()

        // Bloc de gauche : qui parle, ce qu'il dit, et le chant associé.
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.74f)
                .padding(start = Space.s5, end = Space.s3, bottom = bottomInset + Space.s5),
            verticalArrangement = Arrangement.spacedBy(Space.s3),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    post.author,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W700),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (community != null) {
                    Spacer(Modifier.width(Space.s2))
                    Surface(
                        shape = OyengaShapes.full,
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = Space.s3, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OyIcon(
                                if (community.isPrivate) Ic.lock else Ic.public,
                                null,
                                size = 14.dp,
                                tint = Color.White,
                            )
                            Spacer(Modifier.width(Space.s1))
                            Text(
                                community.name,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Text(
                post.text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                post.timeAgo,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
            )

            if (song != null) {
                SoundRow(
                    title = song.title,
                    credit = song.credit,
                    playing = active,
                    onClick = { viewModel.play(song, openFullPlayer = true) },
                )
            }
        }

        // Rail de droite : les gestes du réseau social, empilés sous le pouce.
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Space.s4, bottom = bottomInset + Space.s5),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.s5),
        ) {
            AuthorBadge(
                post = post,
                subscribed = subscribed,
                onFollow = { community?.let(viewModel::joinCommunity) },
            )
            RailAction(
                icon = if (post.liked) Ic.heartFill else Ic.heart,
                label = post.likes.toString(),
                description = if (post.liked) "Retirer mon j'aime" else "J'aime",
                highlighted = post.liked,
                onClick = { viewModel.likePost(post) },
            )
            RailAction(
                icon = Ic.comment,
                label = post.commentsList.size.toString(),
                description = "Commentaires",
                onClick = { viewModel.openSheet(Sheet.PostDetail(post.id)) },
            )
            RailAction(
                icon = Ic.share,
                label = "Partager",
                description = "Partager la publication",
                onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            buildString {
                                append(post.author).append(" — ").append(post.text)
                                if (song != null) append("\n♪ ").append(song.title)
                                append("\n\nPartagé depuis OYENGA")
                            },
                        )
                    }
                    context.startActivity(Intent.createChooser(share, "Partager la publication"))
                },
            )
            if (song != null) {
                SpinningDisc(playing = active)
            }
        }
    }
}

/**
 * Le fond de la page.
 *
 * Une vidéo quand la publication en porte une, sinon une carte de remplacement :
 * le répertoire OYENGA n'a pas encore de vidéos, et un écran noir donnerait
 * l'impression d'une application cassée.
 */
@UnstableApi
@Composable
private fun MediaStage(post: Post, active: Boolean, player: ExoPlayer) {
    when {
        post.videoUrl.isNotBlank() && active -> AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            update = { view -> view.player = player },
            onReset = { view -> view.player = null },
        )

        post.image == "choir" -> Box(Modifier.fillMaxSize()) {
            PlaceholderStage(post, showBadge = false)
            ChoirIllustration(Modifier.align(Alignment.Center).padding(horizontal = Space.s5))
        }

        else -> PlaceholderStage(post)
    }
}

/**
 * La carte de remplacement.
 *
 * Le dégradé est tiré de l'identité de la publication, pas d'une couleur au hasard :
 * deux publications d'une même chorale gardent le même fond d'un passage à l'autre.
 */
@Composable
private fun PlaceholderStage(post: Post, showBadge: Boolean = true) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiaryContainer,
    )
    val (start, end) = palette[post.communityId.hashCode().mod(palette.size)]

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(start, end))),
        contentAlignment = Alignment.Center,
    ) {
        if (showBadge) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OyIcon(Ic.player, null, size = 72.dp, tint = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(Space.s4))
                Surface(shape = OyengaShapes.full, color = Color.Black.copy(alpha = 0.28f)) {
                    Text(
                        "Vidéo à venir",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = Space.s4, vertical = Space.s2),
                    )
                }
            }
        }
    }
}

/** Voiles haut et bas : c'est eux qui rendent le texte lisible sur n'importe quel média. */
@Composable
private fun Scrims() {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.45f),
                    0.22f to Color.Transparent,
                    0.55f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.78f),
                ),
            ),
    )
}

// ---------------------------------------------------------------- éléments d'overlay

@Composable
private fun FeedTopBar(onSearch: () -> Unit, onCompose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Space.s4, vertical = Space.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Communauté",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        OverlayButton(Ic.search, "Rechercher une communauté", onSearch)
        Spacer(Modifier.width(Space.s2))
        OverlayButton(Ic.add, "Écrire une publication", onCompose)
    }
}

@Composable
private fun OverlayButton(icon: Int, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = OyengaShapes.full,
        color = Color.Black.copy(alpha = 0.32f),
        modifier = Modifier.size(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            OyIcon(icon, description, tint = Color.White)
        }
    }
}

/** Une action du rail : l'icône porte le geste, le libellé porte le compte. */
@Composable
private fun RailAction(
    icon: Int,
    label: String,
    description: String,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(OyengaShapes.md)
            .clickable(onClick = onClick)
            .padding(horizontal = Space.s2, vertical = Space.s1),
    ) {
        OyIcon(
            icon,
            description,
            size = IconSize.card - 4.dp,
            tint = if (highlighted) MaterialTheme.colorScheme.primary else Color.White,
        )
        Spacer(Modifier.height(Space.s1))
        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** L'auteur, avec le « + » qui disparaît une fois abonné. */
@Composable
private fun AuthorBadge(post: Post, subscribed: Boolean, onFollow: () -> Unit) {
    Box(contentAlignment = Alignment.BottomCenter) {
        Box(
            Modifier
                .size(52.dp)
                .clip(OyengaShapes.full)
                .background(Color.White.copy(alpha = 0.9f))
                .padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            InitialsAvatar(label = post.author, initials = post.authorInit, size = 48.dp)
        }
        if (!subscribed) {
            Surface(
                onClick = onFollow,
                shape = OyengaShapes.full,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 0.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    OyIcon(
                        Ic.add,
                        "S'abonner à cette communauté",
                        size = 16.dp,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

/** La ligne du « son » : ici, le chant du répertoire que la publication met en avant. */
@Composable
private fun SoundRow(title: String, credit: String, playing: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(OyengaShapes.full)
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(onClick = onClick)
            .padding(horizontal = Space.s3, vertical = Space.s2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OyIcon(Ic.musicFill, null, size = 16.dp, tint = Color.White)
        Spacer(Modifier.width(Space.s2))
        Text(
            if (credit.isBlank()) title else "$title · $credit",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (playing) {
            Spacer(Modifier.width(Space.s2))
            OyIcon(Ic.equalizer, "Chant en avant", size = 14.dp, tint = Color.White)
        }
    }
}

/** Le disque qui tourne tant que la page est à l'écran — le repère visuel du « son ». */
@Composable
private fun SpinningDisc(playing: Boolean) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "disc")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    Box(
        Modifier
            .size(44.dp)
            .rotate(if (playing) angle else 0f)
            .clip(OyengaShapes.full)
            .background(
                Brush.radialGradient(
                    listOf(MaterialTheme.colorScheme.primary, Color.Black),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        OyIcon(Ic.musicFill, null, size = 18.dp, tint = Color.White)
    }
}

// ---------------------------------------------------------------- page sponsorisée

/**
 * Une annonce occupe une page entière, comme sur n'importe quel fil — mais elle
 * s'annonce en haut de page, et jamais dans les écrans liturgiques.
 */
@Composable
private fun SponsoredPage(ad: Ad, bottomInset: Dp) {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.tertiary,
                    ),
                ),
            ),
    ) {
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = Space.s6, end = Space.s6, bottom = bottomInset + Space.s6),
            verticalArrangement = Arrangement.spacedBy(Space.s3),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OyIcon(Ic.campaign, null, size = IconSize.inline, tint = Color.White)
                Spacer(Modifier.width(Space.s2))
                Kicker("Annonce", Color.White)
            }
            Text(
                ad.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                ad.text,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Surface(
                onClick = {
                    if (ad.url.isNotBlank()) {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(ad.url)),
                            )
                        }
                    }
                },
                shape = OyengaShapes.full,
                color = Color.White,
                contentColor = MaterialTheme.colorScheme.tertiary,
            ) {
                Text(
                    ad.cta,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = Space.s6, vertical = Space.s3),
                )
            }
        }
    }
}
