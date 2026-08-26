@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import cm.oyenga.app.data.model.Ad
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.ListRow
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.Space

/**
 * Gestion des annonces.
 *
 * Elles n'apparaissent que dans le fil de la communauté, une toutes les trois
 * publications, et toujours sous une mention « Annonce ». La partie liturgique de
 * l'application n'en reçoit jamais.
 */
@UnstableApi
@Composable
fun AdminAds(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Ad?>(null) }
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var cta by remember { mutableStateOf("Découvrir") }
    var url by remember { mutableStateOf("") }

    fun reset() {
        editing = null
        title = ""
        text = ""
        cta = "Découvrir"
        url = ""
    }

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Text(
                if (editing == null) "Nouvelle annonce" else "Modifier l'annonce",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(Space.s3))

            AdminField("Annonceur", title, { title = it }, placeholder = "Éditions Liturgiques du Cameroun")
            AdminField("Texte", text, { text = it }, singleLine = false, minHeight = 120)
            AdminField("Libellé du bouton", cta, { cta = it }, placeholder = "Découvrir")
            AdminField("Lien", url, { url = it }, placeholder = "https://…")

            Button(
                onClick = {
                    if (title.isBlank()) {
                        viewModel.toast("Le nom de l'annonceur est obligatoire")
                    } else {
                        viewModel.saveAd(
                            Ad(
                                id = editing?.id ?: "ad-" + System.currentTimeMillis(),
                                title = title.trim(),
                                text = text.trim(),
                                cta = cta.trim().ifBlank { "Découvrir" },
                                url = url.trim(),
                            ),
                        )
                        reset()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OyIcon(Ic.save, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(Space.s2))
                Text("Enregistrer")
            }
            if (editing != null) {
                TextButton(onClick = { reset() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Annuler la modification")
                }
            }

            Spacer(Modifier.height(Space.s4))
            InfoNotice(
                Ic.info,
                "Les annonces sont insérées toutes les trois publications dans le fil de la " +
                    "communauté, avec la mention « Annonce ». Aucune ne s'affiche dans les " +
                    "écrans liturgiques.",
            )

            Spacer(Modifier.height(Space.s6))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))
            Kicker("Annonces en ligne")
            Spacer(Modifier.height(Space.s2))
        }

        if (db.ads.isEmpty()) {
            item { EmptyState(Ic.campaign, "Aucune annonce.") }
        } else {
            items(db.ads, key = { it.id }) { ad ->
                ListRow(
                    leading = { OyIcon(Ic.campaign, null, tint = MaterialTheme.colorScheme.tertiary) },
                    title = ad.title,
                    subtitle = ad.text,
                    onClick = {
                        editing = ad
                        title = ad.title
                        text = ad.text
                        cta = ad.cta
                        url = ad.url
                    },
                    trailing = {
                        TextButton(onClick = { viewModel.deleteAd(ad.id) }) {
                            OyIcon(
                                Ic.delete, "Supprimer",
                                size = IconSize.inline,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
            }
        }

        item { BottomSpacer() }
    }
}
