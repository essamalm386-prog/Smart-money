package cm.oyenga.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.ui.OyengaApp
import cm.oyenga.app.ui.OyengaViewModel
import cm.oyenga.app.ui.theme.OyengaTheme

@UnstableApi
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: OyengaViewModel

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* facultatif */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as OyengaApplication
        viewModel = ViewModelProvider(
            this,
            OyengaViewModel.factory(app),
        )[OyengaViewModel::class.java]

        // On garde le splash système tant que le répertoire n'est pas chargé : ouvrir sur
        // une liste vide donnerait l'impression d'une application sans contenu.
        splash.setKeepOnScreenCondition { !viewModel.repository.ready.value }

        app.playback.connect()
        askNotificationPermission()

        setContent {
            OyengaTheme {
                OyengaApp(viewModel)
            }
        }
    }

    /**
     * La notification porte les contrôles de lecture. Sans elle, l'utilisateur doit
     * rouvrir l'application pour mettre en pause — on la demande donc, mais l'application
     * reste entièrement utilisable si elle est refusée.
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
