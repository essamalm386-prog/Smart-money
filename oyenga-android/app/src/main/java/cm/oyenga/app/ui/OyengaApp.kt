@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.ui.admin.AdminScreen
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.screens.CatalogueScreen
import cm.oyenga.app.ui.screens.CommunityScreen
import cm.oyenga.app.ui.screens.HomeScreen
import cm.oyenga.app.ui.screens.MiniPlayer
import cm.oyenga.app.ui.screens.PlayerScreen
import cm.oyenga.app.ui.sheets.OyengaSheets
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.Motion

/**
 * L'ossature de l'application : les quatre destinations, le mini-lecteur, le lecteur
 * plein écran, l'espace d'administration et les feuilles modales.
 *
 * Tout tient dans une seule activité : les transitions restent fluides et l'état de
 * lecture n'est jamais reconstruit en changeant d'onglet.
 */
@UnstableApi
@Composable
fun OyengaApp(viewModel: OyengaViewModel) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.toast) {
        val message = ui.toast ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        viewModel.consumeToast()
    }

    // On note la position d'écoute à chaque changement : reprendre là où on s'était
    // arrêté n'a de valeur que si l'application peut aussi être tuée sans prévenir.
    LaunchedEffect(playback.current?.id, playback.positionSeconds / 5) {
        viewModel.rememberPosition()
    }

    if (ui.adminOpen) {
        AdminScreen(viewModel)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = playback.current != null && !ui.playerOpen,
                    enter = slideInVertically(tween(Motion.MEDIUM)) { it } + fadeIn(),
                    exit = slideOutVertically(tween(Motion.SHORT)) { it } + fadeOut(),
                ) {
                    MiniPlayer(viewModel, playback)
                }
                BottomNavigation(
                    current = ui.tab,
                    playerOpen = ui.playerOpen,
                    onSelect = viewModel::selectTab,
                )
            }
        },
    ) { padding ->
        // Le fil communauté est plein écran : le média passe SOUS la barre de
        // navigation et le mini-lecteur, qui flottent par-dessus. Les surcouches
        // reçoivent la hauteur à réserver pour ne rien masquer d'important.
        val immersive = ui.tab == Tab.COMMUNAUTE
        Box(
            Modifier
                .fillMaxSize()
                .padding(if (immersive) PaddingValues() else padding),
        ) {
            when (ui.tab) {
                Tab.ACCUEIL -> HomeScreen(viewModel)
                Tab.CATALOGUE -> CatalogueScreen(viewModel)
                Tab.COMMUNAUTE -> CommunityScreen(
                    viewModel = viewModel,
                    bottomInset = padding.calculateBottomPadding(),
                )
                Tab.LECTEUR -> HomeScreen(viewModel)
            }
        }
    }

    AnimatedVisibility(
        visible = ui.playerOpen && playback.current != null,
        enter = slideInVertically(tween(Motion.LONG, easing = Motion.emphasizedDecelerate)) { it },
        exit = slideOutVertically(tween(Motion.MEDIUM, easing = Motion.emphasizedAccelerate)) { it },
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            PlayerScreen(viewModel, playback)
        }
    }

    OyengaSheets(viewModel)
}

@Composable
private fun BottomNavigation(
    current: Tab,
    playerOpen: Boolean,
    onSelect: (Tab) -> Unit,
) {
    val entries = listOf(
        Triple(Tab.ACCUEIL, "Accueil", Ic.home to Ic.homeFill),
        Triple(Tab.CATALOGUE, "Catalogue", Ic.search to Ic.searchFill),
        Triple(Tab.LECTEUR, "Lecteur", Ic.player to Ic.playerFill),
        Triple(Tab.COMMUNAUTE, "Communauté", Ic.community to Ic.communityFill),
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        entries.forEach { (tab, label, icons) ->
            val selected = if (tab == Tab.LECTEUR) playerOpen else current == tab && !playerOpen
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(tab) },
                icon = {
                    // L'icône se remplit quand la destination est active : l'état ne
                    // repose pas seulement sur la couleur de l'indicateur.
                    androidx.compose.material3.Icon(
                        painter = painterResource(if (selected) icons.second else icons.first),
                        contentDescription = null,
                    )
                },
                label = { Text(label) },
                alwaysShowLabel = true,
            )
        }
    }
}

/** Petit utilitaire partagé : l'icône d'un onglet dans les écrans secondaires. */
@Composable
internal fun TabIcon(res: Int, description: String?, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        OyIcon(res, description)
    }
}
