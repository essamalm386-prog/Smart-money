@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.components.OyIcon
import cm.oyenga.app.ui.theme.Ic
import cm.oyenga.app.ui.theme.Space

private val ADMIN_TABS = listOf(
    "Chants", "Programmes", "Lectures", "Publications", "Publicités", "Compte",
)

/**
 * L'espace d'administration.
 *
 * Il occupe tout l'écran, sans barre de navigation ni mini-lecteur : on n'y entre pas
 * par hasard et on en sort explicitement. C'est ici que la chorale prépare la messe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun AdminScreen(viewModel: OyengaViewModel) {
    val db by viewModel.db.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.toast) {
        val message = ui.toast ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        viewModel.consumeToast()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Administration", style = MaterialTheme.typography.titleLarge)
                            Text(
                                db.user.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = viewModel::closeAdmin) {
                            OyIcon(Ic.arrowBack, "Quitter l'administration")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                ScrollableTabRow(
                    selectedTabIndex = selected,
                    edgePadding = Space.s6,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    ADMIN_TABS.forEachIndexed { index, label ->
                        Tab(
                            selected = selected == index,
                            onClick = { selected = index },
                            text = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selected) {
                0 -> AdminSongs(viewModel)
                1 -> AdminPrograms(viewModel)
                2 -> AdminReadings(viewModel)
                3 -> AdminPosts(viewModel)
                4 -> AdminAds(viewModel)
                else -> AdminAccount(viewModel)
            }
        }
    }
}
