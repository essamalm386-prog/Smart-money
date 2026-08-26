@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.MASS_PARTS
import cm.oyenga.app.data.model.Post
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.Sheet
import cm.oyenga.app.ui.Tab
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InitialsAvatar
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.components.SectionHeader
import cm.oyenga.app.ui.components.SongIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@UnstableApi
@Composable
fun HomeScreen(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val readingsState by viewModel.readings.collectAsStateWithLifecycle()
    val day = viewModel.currentReadings()
    val program = db.program

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = Space.s6, end = Space.s6, top = Space.s4,
        ),
    ) {
        item {
            HomeHeader(
                greeting = greeting(),
                name = db.user.name,
                today = todayLabel(),
                season = listOf(day.saison, day.annee).filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { "Chants liturgiques" },
                initials = db.user.initials,
                activity = db.posts.count { it.communityId in db.user.subscriptions }.coerceAtMost(99),
                onBell = { viewModel.selectTab(Tab.COMMUNAUTE) },
                onAvatar = { viewModel.openSheet(Sheet.Account) },
            )
        }

        item {
            Spacer(Modifier.height(Space.s4))
            ReadingsBanner(
                title = day.title.ifBlank { "Prochaine messe" },
                dateLabel = readableDate(day.date),
                isToday = day.date == LocalDate.now().toString(),
                readingsCount = day.readings.size,
                phrase = when {
                    readingsState.loading -> "Chargement automatique des textes…"
                    day.offline -> "Les textes se chargeront automatiquement une fois en ligne."
                    else -> day.phrase
                },
                ctaLabel = if (day.offline) "Voir la prochaine messe" else "Lire le résumé & les textes",
                onClick = { viewModel.openSheet(Sheet.Readings) },
            )
        }

        item {
            SectionHeader(
                title = "Programme de chants",
                subtitle = "Liturgie · sélection par la chorale",
                actionLabel = if (program != null) "Voir tout" else null,
                onAction = { viewModel.openSheet(Sheet.Program) },
            )
        }

        if (program == null) {
            item {
                EmptyState(
                    icon = Ic.program,
                    message = "Aucun programme publié pour le moment.",
                )
            }
        } else {
            item {
                Card(
                    onClick = { viewModel.openSheet(Sheet.Program) },
                    shape = OyengaShapes.xl,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(Modifier.padding(Space.s5)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(IconSize.card + Space.s2)
                                    .clip(OyengaShapes.lg)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                OyIcon(
                                    Ic.calendar, null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                            Spacer(Modifier.size(Space.s3))
                            Column(Modifier.weight(1f)) {
                                Kicker(
                                    "Prochaine messe" + readableDate(readingsState.targetDate)
                                        .let { if (it.isBlank()) "" else " · $it" },
                                )
                                Text(
                                    day.title.ifBlank { program.fete },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            OyIcon(Ic.chevronRight, null)
                        }

                        Spacer(Modifier.height(Space.s3))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Trois parties suffisent en aperçu : le programme complet a sa feuille.
                        MASS_PARTS
                            .mapNotNull { part -> program.items[part.key]?.let { part to it } }
                            .take(3)
                            .forEach { (part, item) ->
                                val song = songs.firstOrNull { it.id == item.recommended } ?: return@forEach
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Space.s3),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.s3),
                                ) {
                                    SongIcon(song.moment, size = 44.dp)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            part.label + alternativesLabel(item.alternatives),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            song.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    FilledTonalIconButton(onClick = { viewModel.play(song) }) {
                                        OyIcon(
                                            Ic.play,
                                            "Écouter ${song.title}",
                                            size = IconSize.inline,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }

                        TextButton(
                            onClick = { viewModel.openSheet(Sheet.Program) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Voir le programme complet")
                            Spacer(Modifier.size(Space.s2))
                            OyIcon(
                                Ic.arrowForward, null,
                                size = IconSize.inline,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Fil d'actualité",
                subtitle = "Aperçus de la communauté",
                actionLabel = "Voir tout",
                onAction = { viewModel.selectTab(Tab.COMMUNAUTE) },
            )
        }

        items(db.posts.take(3), key = { it.id }) { post ->
            PostTeaser(
                post = post,
                communityName = db.community(post.communityId)?.name ?: "Communauté",
                onClick = { viewModel.openSheet(Sheet.PostDetail(post.id)) },
            )
        }

        item { BottomSpacer() }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    name: String,
    today: String,
    season: String,
    initials: String,
    activity: Int,
    onBell: () -> Unit,
    onAvatar: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                today,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text("$greeting, $name", style = MaterialTheme.typography.headlineSmall)
            Text(
                season,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BadgedBox(
            badge = { if (activity > 0) Badge { Text(activity.toString()) } },
        ) {
            IconButton(onClick = onBell) {
                OyIcon(Ic.bell, "Activité de mes communautés")
            }
        }
        Box(Modifier.clickable(onClick = onAvatar)) {
            InitialsAvatar(label = name, initials = initials)
        }
    }
}

/**
 * Le bandeau des lectures : l'élément le plus consulté de l'accueil, donc le seul
 * à porter la couleur `primary` en aplat sur cet écran.
 */
@Composable
private fun ReadingsBanner(
    title: String,
    dateLabel: String,
    isToday: Boolean,
    readingsCount: Int,
    phrase: String,
    ctaLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = OyengaShapes.xl,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Space.s5)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kicker(
                    if (isToday) "Textes du jour" else "Prochaine messe",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.weight(1f))
                if (readingsCount > 0) {
                    Surface(
                        shape = OyengaShapes.full,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    ) {
                        Text(
                            "$readingsCount lectures",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = Space.s3, vertical = Space.s1),
                        )
                    }
                }
            }
            Spacer(Modifier.height(Space.s2))
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (dateLabel.isNotBlank()) {
                Text(
                    dateLabel.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            if (phrase.isNotBlank()) {
                Spacer(Modifier.height(Space.s3))
                Text(
                    phrase,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(Space.s4))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ctaLabel, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.size(Space.s2))
                OyIcon(
                    Ic.arrowForward, null,
                    size = IconSize.inline,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun PostTeaser(post: Post, communityName: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(OyengaShapes.lg)
            .clickable(onClick = onClick)
            .padding(vertical = Space.s3, horizontal = Space.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        InitialsAvatar(label = post.author, initials = post.authorInit)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    post.author.substringBefore('·').trim(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.size(Space.s2))
                Surface(
                    shape = OyengaShapes.full,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        communityName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = Space.s2, vertical = 2.dp),
                    )
                }
            }
            Text(
                post.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${post.timeAgo} · ${post.likes} j'aime · ${post.commentsList.size} commentaires",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OyIcon(Ic.chevronRight, null)
    }
}

// ---------------------------------------------------------------- formats de date

private val DAY_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
private val FULL_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)

internal fun todayLabel(): String = LocalDate.now().format(FULL_FORMAT)

internal fun readableDate(iso: String): String =
    runCatching { LocalDate.parse(iso).format(DAY_FORMAT) }.getOrDefault("")

/** « Bonjour » avant 18 h, « Bonsoir » ensuite — l'application sert surtout le soir. */
internal fun greeting(now: LocalTime = LocalTime.now()): String = when {
    now.hour < 12 -> "Bonjour"
    now.hour < 18 -> "Bon après-midi"
    else -> "Bonsoir"
}

private fun alternativesLabel(count: Int): String = when (count) {
    0 -> ""
    1 -> " · 1 alternative"
    else -> " · $count alternatives"
}
