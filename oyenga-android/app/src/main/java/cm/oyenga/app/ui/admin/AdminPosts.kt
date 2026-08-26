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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InitialsAvatar
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.ListRow
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.Space

/** Publication et modération du fil de la communauté. */
@UnstableApi
@Composable
fun AdminPosts(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    var target by remember { mutableStateOf(db.communities.firstOrNull()?.id.orEmpty()) }
    var text by remember { mutableStateOf("") }

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Text("Nouvelle publication", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Space.s3))

            AdminSingleChoice(
                title = "Communauté",
                options = db.communities.map { it.id to it.name },
                selected = target,
                onSelect = { target = it },
            )

            AdminField(
                "Message",
                text,
                { text = it },
                singleLine = false,
                minHeight = 180,
                placeholder = "Répétition, annonce, remerciements…",
            )

            Button(
                onClick = {
                    viewModel.publish(target, text)
                    text = ""
                },
                enabled = text.isNotBlank() && target.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OyIcon(Ic.send, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(Space.s2))
                Text("Publier")
            }

            Spacer(Modifier.height(Space.s6))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))
            Kicker("Publications existantes")
            Spacer(Modifier.height(Space.s2))
        }

        if (db.posts.isEmpty()) {
            item { EmptyState(Ic.community, "Aucune publication.") }
        } else {
            items(db.posts, key = { it.id }) { post ->
                ListRow(
                    leading = { InitialsAvatar(post.author, post.authorInit, size = 40.dp) },
                    title = post.author,
                    subtitle = "${db.community(post.communityId)?.name ?: "Communauté"} · " +
                        "${post.likes} j'aime · ${post.commentsList.size} commentaires",
                    trailing = {
                        TextButton(onClick = { viewModel.deletePost(post.id) }) {
                            OyIcon(
                                Ic.delete, "Supprimer",
                                size = IconSize.inline,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
                Column {
                    Text(
                        post.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Space.s3))
                }
            }
        }

        item { BottomSpacer() }
    }
}
