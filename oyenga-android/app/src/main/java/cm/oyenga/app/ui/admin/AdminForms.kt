@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/** Champ texte standard de l'administration. */
@Composable
fun AdminField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supporting: String? = null,
    singleLine: Boolean = true,
    minHeight: Int = 0,
    numeric: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Space.s3)
            .let { if (minHeight > 0) it.height(minHeight.dp) else it },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supporting?.let { { Text(it) } },
        singleLine = singleLine,
        shape = OyengaShapes.md,
        keyboardOptions = if (numeric) {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        } else {
            KeyboardOptions.Default
        },
    )
}

/**
 * Groupe de chips à choix multiple.
 *
 * C'est l'outil d'étiquetage des chants : temps liturgiques, fêtes, thèmes. Le choix
 * multiple est délibéré — un chant marial convient souvent à plusieurs fêtes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChips(
    title: String,
    options: List<Pair<String, String>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    supporting: String? = null,
) {
    Column(Modifier.padding(bottom = Space.s4)) {
        Kicker(title)
        if (supporting != null) {
            Text(
                supporting,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Space.s2))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            options.forEach { (key, label) ->
                FilterChip(
                    selected = key in selected,
                    onClick = { onToggle(key) },
                    label = { Text(label) },
                )
            }
        }
    }
}

/** Groupe de chips à choix unique. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSingleChoice(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(Modifier.padding(bottom = Space.s4)) {
        Kicker(title)
        Spacer(Modifier.height(Space.s2))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            options.forEach { (key, label) ->
                FilterChip(
                    selected = key == selected,
                    onClick = { onSelect(key) },
                    label = { Text(label) },
                )
            }
        }
    }
}
