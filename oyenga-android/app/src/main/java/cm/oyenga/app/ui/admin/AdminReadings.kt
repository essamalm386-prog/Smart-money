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
import androidx.compose.foundation.lazy.item
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.ReadingDay
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaText
import cm.oyenga.app.ui.theme.Space

/**
 * Gestion des lectures et de l'introduction.
 *
 * Les textes viennent de l'AELF ; le travail du responsable est de vérifier, corriger
 * si besoin, et préparer l'introduction qu'il proclamera. L'introduction existe
 * toujours — construite localement — même sans réseau ni clé API.
 */
@UnstableApi
@Composable
fun AdminReadings(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val readingsState by viewModel.readings.collectAsStateWithLifecycle()
    val day = viewModel.currentReadings()

    var date by remember(readingsState.targetDate) { mutableStateOf(readingsState.targetDate) }
    var fetching by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }
    var draft by remember(day.date, db.introOverrides) {
        mutableStateOf(viewModel.introductionFor(day))
    }

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Text("Textes de la célébration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Space.s3))

            AdminField(
                "Date (AAAA-MM-JJ)",
                date,
                { date = it },
                supporting = "L'application suit d'elle-même le prochain dimanche ou la prochaine solennité.",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                Button(
                    onClick = {
                        fetching = true
                        viewModel.refreshReadingsFor(date.trim()) { fetching = false }
                    },
                    enabled = !fetching,
                    modifier = Modifier.weight(1f),
                ) {
                    if (fetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        OyIcon(Ic.refresh, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.size(Space.s2))
                    Text("Récupérer (AELF)")
                }
                OutlinedButton(
                    onClick = viewModel::loadReadings,
                    modifier = Modifier.weight(1f),
                ) { Text("Prochaine messe") }
            }

            Spacer(Modifier.height(Space.s4))
            InfoNotice(
                Ic.info,
                when {
                    readingsState.offline -> "AELF injoignable : les textes affichés viennent de l'application."
                    day.live -> "Textes chargés automatiquement depuis l'AELF."
                    day.seed -> "Textes de démonstration livrés avec l'application."
                    else -> "Textes enregistrés par un responsable."
                },
            )

            Spacer(Modifier.height(Space.s6))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lectures du jour", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(
                    "${day.readings.size} texte(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Space.s3))
        }

        readingItems(day)

        item {
            Spacer(Modifier.height(Space.s5))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))

            Text("Introduction à proclamer", style = MaterialTheme.typography.titleLarge)
            Text(
                "Ce texte est lu au micro avant la liturgie de la Parole.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.s3))

            AdminField(
                "Texte",
                draft,
                { draft = it },
                singleLine = false,
                minHeight = 300,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearIntroduction(day.date)
                        draft = viewModel.introductionFor(day.copy(intro = ""))
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Régénérer") }
                Button(
                    onClick = { viewModel.saveIntroduction(day.date, draft) },
                    modifier = Modifier.weight(1f),
                    enabled = draft.isNotBlank(),
                ) {
                    OyIcon(Ic.save, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(Space.s2))
                    Text("Enregistrer")
                }
            }

            Spacer(Modifier.height(Space.s4))
            Kicker("Rédaction assistée")
            Spacer(Modifier.height(Space.s2))
            if (db.ai.key.isBlank()) {
                InfoNotice(
                    Ic.info,
                    "Pour développer l'introduction avec Claude, enregistre ta clé API " +
                        "dans l'onglet Compte. Le texte ci-dessus fonctionne sans clé.",
                )
            } else {
                Button(
                    onClick = {
                        generating = true
                        viewModel.generateIntroduction(day) { success ->
                            generating = false
                            if (success) draft = viewModel.introductionFor(day)
                        }
                    },
                    enabled = !generating && day.readings.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (generating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        OyIcon(Ic.magic, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.size(Space.s2))
                    Text(if (generating) "Rédaction en cours…" else "Développer avec Claude")
                }
            }

            BottomSpacer()
        }
    }
}

@UnstableApi
private fun androidx.compose.foundation.lazy.LazyListScope.readingItems(day: ReadingDay) {
    if (day.readings.isEmpty()) {
        item {
            Text(
                "Aucun texte pour cette date. Récupère-les depuis l'AELF ci-dessus.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    items(day.readings.size) { index ->
        val reading = day.readings[index]
        var open by remember(reading.label) { mutableStateOf(false) }
        Column(Modifier.padding(bottom = Space.s4)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kicker(reading.label, MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                Text(
                    reading.ref,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(reading.source, style = MaterialTheme.typography.titleSmall)
            if (reading.quote.isNotBlank()) {
                Text(
                    reading.quote,
                    style = OyengaText.serifBody,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (reading.summary.isNotBlank()) {
                Text(
                    reading.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (reading.text.isNotBlank()) {
                TextButton(onClick = { open = !open }) {
                    Text(if (open) "Masquer le texte" else "Vérifier le texte intégral")
                }
                if (open) Text(reading.text, style = OyengaText.serifBody)
            }
            Spacer(Modifier.height(Space.s2))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
