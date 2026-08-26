@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.model.ReadingText
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.EmptyState
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.screens.readableDate
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.OyengaText
import cm.oyenga.app.ui.theme.Space

/**
 * Les textes de la prochaine célébration.
 *
 * Trois niveaux de lecture, du plus court au plus long : l'introduction à proclamer,
 * le résumé de chaque lecture, puis le texte intégral. C'est l'ordre dans lequel on
 * s'en sert : on prépare, on situe, on lit.
 */
@UnstableApi
@Composable
fun ReadingsSheet(viewModel: OyengaViewModel) {
    val readingsState by viewModel.readings.collectAsStateWithLifecycle()
    val day = viewModel.currentReadings()
    val introduction = viewModel.introductionFor(day)
    val clipboard = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(setOf<String>()) }

    SheetHeader(
        title = day.title.ifBlank { "Prochaine messe" },
        subtitle = listOfNotNull(
            readableDate(day.date).takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() },
            day.saison.takeIf { it.isNotBlank() },
            day.annee.takeIf { it.isNotBlank() },
        ).joinToString(" · "),
    )

    SheetBody {
        when {
            readingsState.loading -> {
                InfoNotice(Ic.history, "Chargement des textes auprès de l'AELF…")
            }

            day.readings.isEmpty() -> {
                EmptyState(
                    icon = Ic.offline,
                    message = if (readingsState.offline) {
                        "Textes indisponibles hors ligne. Ils se chargeront à la prochaine connexion."
                    } else {
                        "Aucun texte enregistré pour cette date."
                    },
                    actionLabel = "Réessayer",
                    onAction = viewModel::loadReadings,
                )
            }

            else -> {
                if (introduction.isNotBlank()) {
                    Surface(
                        shape = OyengaShapes.xl,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(Space.s5)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
                                OyIcon(
                                    Ic.mic, null,
                                    size = IconSize.inline,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Kicker("Introduction à proclamer", MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(Modifier.height(Space.s3))
                            Text(introduction, style = OyengaText.serifBody)
                            Spacer(Modifier.height(Space.s2))
                            TextButton(onClick = {
                                clipboard.setText(AnnotatedString(introduction))
                                viewModel.toast("Introduction copiée ✓")
                            }) {
                                OyIcon(
                                    Ic.copy, null,
                                    size = IconSize.inline,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Spacer(Modifier.height(Space.s1))
                                Text("Copier le texte")
                            }
                        }
                    }
                    Spacer(Modifier.height(Space.s5))
                }

                day.readings.forEach { reading ->
                    ReadingBlock(
                        reading = reading,
                        expanded = reading.label in expanded,
                        onToggle = {
                            expanded = if (reading.label in expanded) {
                                expanded - reading.label
                            } else {
                                expanded + reading.label
                            }
                        },
                    )
                }

                Spacer(Modifier.height(Space.s4))
                InfoNotice(
                    Ic.info,
                    if (day.live) {
                        "Textes fournis par l'AELF, chargés automatiquement."
                    } else {
                        "Textes enregistrés dans l'application."
                    },
                )
            }
        }
        Spacer(Modifier.height(Space.s6))
    }
}

@Composable
private fun ReadingBlock(reading: ReadingText, expanded: Boolean, onToggle: () -> Unit) {
    Column(Modifier.padding(bottom = Space.s5)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            Kicker(reading.label, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            if (reading.ref.isNotBlank()) {
                Text(
                    reading.ref,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(Space.s2))

        if (reading.source.isNotBlank()) {
            Text(reading.source, style = MaterialTheme.typography.titleSmall)
        }
        if (reading.quote.isNotBlank()) {
            Text(
                reading.quote,
                style = OyengaText.serifBody,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = Space.s1),
            )
        }
        if (reading.summary.isNotBlank()) {
            Text(
                reading.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Space.s2),
            )
        }

        if (reading.text.isNotBlank()) {
            Spacer(Modifier.height(Space.s2))
            AssistChip(
                onClick = onToggle,
                label = { Text(if (expanded) "Masquer le texte intégral" else "Lire le texte intégral") },
                leadingIcon = {
                    OyIcon(
                        if (expanded) Ic.chevronUp else Ic.chevronDown,
                        null,
                        size = IconSize.inline,
                    )
                },
            )
            if (expanded) {
                Spacer(Modifier.height(Space.s3))
                Text(reading.text, style = OyengaText.serifBodyLarge)
            }
        }

        Spacer(Modifier.height(Space.s4))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
