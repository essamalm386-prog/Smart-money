@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.remote.AnthropicIntroWriter
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.BottomSpacer
import cm.oyenga.app.ui.components.InfoNotice
import cm.oyenga.app.ui.components.InitialsAvatar
import cm.oyenga.app.ui.components.Kicker
import cm.oyenga.app.ui.components.ListRow
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.IconSize
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Comptes et réglages.
 *
 * La clé API de rédaction assistée appartient au responsable : elle est saisie ici,
 * conservée sur son téléphone dans les données privées de l'application, et n'est
 * envoyée qu'à Anthropic au moment d'une rédaction.
 */
@UnstableApi
@Composable
fun AdminAccount(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    var key by remember(db.ai.key) { mutableStateOf(db.ai.key) }
    var model by remember(db.ai.model) { mutableStateOf(db.ai.model) }
    var revealed by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(horizontal = Space.s6, vertical = Space.s4)) {
        item {
            Text("Compte courant", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Space.s3))
            ListRow(
                leading = {
                    InitialsAvatar(db.user.displayName, db.user.initials, size = 48.dp, highlighted = true)
                },
                title = db.user.displayName,
                subtitle = (if (db.user.isAdmin) "Responsable" else "Membre") +
                    (if (db.user.premium) " · Premium" else "") +
                    " · ${db.user.subscriptions.size} abonnement(s)",
            )

            Spacer(Modifier.height(Space.s5))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))

            Text("Rédaction assistée", style = MaterialTheme.typography.titleLarge)
            Text(
                "Facultatif. Sans clé, l'introduction reste rédigée par l'application, hors ligne.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.s3))

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Clé API Anthropic") },
                placeholder = { Text("sk-ant-…") },
                singleLine = true,
                shape = OyengaShapes.md,
                visualTransformation = if (revealed) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(onClick = { revealed = !revealed }) {
                        OyIcon(
                            Ic.visibility,
                            if (revealed) "Masquer la clé" else "Afficher la clé",
                            size = IconSize.inline,
                        )
                    }
                },
            )

            Spacer(Modifier.height(Space.s3))
            AdminSingleChoice(
                title = "Modèle",
                options = AnthropicIntroWriter.MODELS,
                selected = model,
                onSelect = { model = it },
            )

            Button(
                onClick = { viewModel.setAiConfig(key, model) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OyIcon(Ic.save, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(Space.s2))
                Text("Enregistrer les réglages")
            }

            Spacer(Modifier.height(Space.s4))
            InfoNotice(
                Ic.lock,
                "La clé est stockée dans les données privées de l'application sur cet appareil. " +
                    "Elle n'est transmise qu'à l'API Anthropic, au moment d'une rédaction.",
            )

            Spacer(Modifier.height(Space.s6))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Space.s5))

            Kicker("Tous les comptes")
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
                        append(" · ${account.likes.size} favori(s)")
                    },
                    onClick = { if (!current) viewModel.switchAccount(account) },
                    trailing = {
                        if (current) {
                            Text(
                                "Actuel",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }

            Spacer(Modifier.height(Space.s5))
            Row {
                TextButton(onClick = viewModel::closeAdmin, modifier = Modifier.fillMaxWidth()) {
                    OyIcon(Ic.logout, null, size = IconSize.inline, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(Space.s2))
                    Text("Quitter l'administration")
                }
            }
            BottomSpacer()
        }
    }
}
