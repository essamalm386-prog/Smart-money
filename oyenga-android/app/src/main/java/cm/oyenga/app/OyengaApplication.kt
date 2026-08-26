package cm.oyenga.app

import android.app.Application
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.data.OyengaRepository
import cm.oyenga.app.player.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Point d'assemblage de l'application.
 *
 * Le dépôt et le moteur de lecture vivent aussi longtemps que le processus : la lecture
 * ne doit pas s'interrompre à la rotation de l'écran, et l'état ne doit pas être
 * rechargé à chaque retour sur un écran.
 */
@UnstableApi
class OyengaApplication : Application() {

    /**
     * Portée de vie du processus, sur le thread principal.
     *
     * Le contrôleur média ne se consulte que depuis le looper principal : lancer ses
     * boucles ailleurs lève une exception dès la première lecture. Les travaux longs
     * (fichier, réseau) basculent eux-mêmes sur `Dispatchers.IO`.
     */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    lateinit var repository: OyengaRepository
        private set

    lateinit var playback: PlaybackController
        private set

    override fun onCreate() {
        super.onCreate()
        repository = OyengaRepository(this, appScope)
        playback = PlaybackController(this, appScope)
    }
}
