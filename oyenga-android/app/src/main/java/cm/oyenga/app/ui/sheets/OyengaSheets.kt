@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.Sheet
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Toutes les feuilles modales de l'application, montées au même endroit.
 *
 * Une seule feuille est visible à la fois : empiler des modales sur mobile rend le
 * geste de retour imprévisible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun OyengaSheets(viewModel: OyengaViewModel) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sheet = ui.sheet ?: return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        sheetState = state,
        shape = OyengaShapes.sheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = Space.s6),
        ) {
            when (sheet) {
                Sheet.Readings -> ReadingsSheet(viewModel)
                Sheet.Account -> AccountSheet(viewModel)
                Sheet.Compose -> ComposeSheet(viewModel)
                Sheet.Filters -> FiltersSheet(viewModel)
                Sheet.Lyrics -> LyricsSheet(viewModel)
                Sheet.Partition -> PartitionSheet(viewModel)
                Sheet.Queue -> QueueSheet(viewModel)
                Sheet.Playlists -> PlaylistsSheet(viewModel)
                Sheet.Program -> ProgramSheet(viewModel)
                is Sheet.PostDetail -> PostDetailSheet(viewModel, sheet.postId)
            }
        }
    }
}
