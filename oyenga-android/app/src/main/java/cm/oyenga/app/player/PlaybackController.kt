package cm.oyenga.app.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cm.oyenga.app.R
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.ui.theme.Moments
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Ce que l'interface a besoin de savoir sur la lecture, et rien de plus. */
data class PlaybackState(
    val current: Song? = null,
    val queue: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val shuffle: Boolean = false,
    val repeat: Boolean = false,
) {
    /** Un chant sans lien audio est « lu » en simulation : le temps défile, sans son. */
    val simulated: Boolean get() = current != null && !current.hasAudio

    val progress: Float
        get() = if (durationSeconds > 0) {
            (positionSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f)
        } else {
            0f
        }
}

/**
 * Le moteur de lecture.
 *
 * Deux régimes cohabitent, comme dans la PWA : lecture réelle par ExoPlayer quand le
 * chant a un lien audio, et défilement simulé sinon. Le répertoire BETI livré n'a pas
 * encore d'enregistrements — sans ce second régime, l'application aurait l'air cassée
 * sur ses propres données.
 */
@UnstableApi
class PlaybackController(
    private val context: Context,
    private val scope: CoroutineScope,
) {

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var ticker: Job? = null
    private var simulation: Job? = null

    private val artworkUri: Uri = Uri.parse(
        "android.resource://${context.packageName}/${R.drawable.logo_oyenga}",
    )

    fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, OyengaPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = runCatching { future.get() }.getOrNull()?.also { attach(it) }
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        ticker?.cancel()
        simulation?.cancel()
        controller?.release()
        controller = null
    }

    private fun attach(player: MediaController) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (_state.value.simulated) return
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) startTicker()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val seconds = (player.duration / 1000).toInt()
                    if (seconds > 0) _state.value = _state.value.copy(durationSeconds = seconds)
                }
                if (playbackState == Player.STATE_ENDED) onTrackFinished()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                // Un lien mort ne doit pas figer le lecteur : on s'arrête proprement et
                // l'écran affiche le chant à l'arrêt plutôt qu'une lecture fantôme.
                _state.value = _state.value.copy(isPlaying = false)
            }
        })
    }

    // ------------------------------------------------------------------ commandes

    fun play(song: Song, queue: List<Song>) {
        _state.value = _state.value.copy(
            current = song,
            queue = queue,
            positionSeconds = 0,
            durationSeconds = song.duration,
            isPlaying = true,
        )
        if (song.hasAudio) {
            simulation?.cancel()
            controller?.apply {
                setMediaItem(song.toMediaItem())
                prepare()
                play()
            }
            startTicker()
        } else {
            controller?.stop()
            controller?.clearMediaItems()
            startSimulation()
        }
    }

    fun toggle() {
        val state = _state.value
        val song = state.current ?: return
        val playing = !state.isPlaying
        _state.value = state.copy(isPlaying = playing)
        if (song.hasAudio) {
            controller?.let { if (playing) it.play() else it.pause() }
            if (playing) startTicker()
        } else {
            if (playing) startSimulation() else simulation?.cancel()
        }
    }

    /** Met en pause sans oublier le chant : le fil vidéo prend la main sur le son. */
    fun pause() {
        if (!_state.value.isPlaying) return
        _state.value = _state.value.copy(isPlaying = false)
        simulation?.cancel()
        controller?.pause()
    }

    fun stop() {
        simulation?.cancel()
        ticker?.cancel()
        controller?.stop()
        controller?.clearMediaItems()
        _state.value = PlaybackState(queue = _state.value.queue)
    }

    fun seekTo(seconds: Int) {
        val state = _state.value
        val bounded = seconds.coerceIn(0, state.durationSeconds.coerceAtLeast(0))
        _state.value = state.copy(positionSeconds = bounded)
        if (state.current?.hasAudio == true) controller?.seekTo(bounded * 1000L)
    }

    fun next() = moveBy(1)

    /** Avant trois secondes, « précédent » revient au début du chant — convention usuelle. */
    fun previous() {
        val state = _state.value
        if (state.positionSeconds > 3) {
            seekTo(0)
            return
        }
        moveBy(-1)
    }

    fun toggleShuffle() {
        _state.value = _state.value.copy(shuffle = !_state.value.shuffle)
    }

    fun toggleRepeat() {
        _state.value = _state.value.copy(repeat = !_state.value.repeat)
    }

    /** Reprise de session : le chant est chargé mais on ne démarre pas le son sans geste. */
    fun restoreWithoutPlaying(song: Song, queue: List<Song>, positionSeconds: Int) {
        _state.value = PlaybackState(
            current = song,
            queue = queue,
            isPlaying = false,
            positionSeconds = positionSeconds.coerceAtMost(song.duration),
            durationSeconds = song.duration,
        )
    }

    private fun moveBy(delta: Int) {
        val state = _state.value
        val queue = state.queue
        val current = state.current ?: return
        if (queue.isEmpty()) return
        val index = queue.indexOfFirst { it.id == current.id }.takeIf { it >= 0 } ?: 0
        val target = if (state.shuffle && delta > 0) {
            randomOtherIndex(queue.size, index)
        } else {
            (index + delta + queue.size) % queue.size
        }
        play(queue[target], queue)
    }

    private fun randomOtherIndex(size: Int, current: Int): Int {
        if (size <= 1) return current
        var pick = current
        while (pick == current) pick = (0 until size).random()
        return pick
    }

    private fun onTrackFinished() {
        if (_state.value.repeat) {
            seekTo(0)
            _state.value.current?.let { play(it, _state.value.queue) }
        } else {
            next()
        }
    }

    // ------------------------------------------------------------------ horloges

    /** Suit la position réelle du lecteur, une fois par seconde. */
    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                val player = controller
                if (player != null && _state.value.current?.hasAudio == true) {
                    val position = (player.currentPosition / 1000).toInt().coerceAtLeast(0)
                    val duration = (player.duration / 1000).toInt()
                    _state.value = _state.value.copy(
                        positionSeconds = position,
                        durationSeconds = if (duration > 0) duration else _state.value.durationSeconds,
                        isPlaying = player.isPlaying,
                    )
                }
                delay(1000)
            }
        }
    }

    /** Lecture simulée : le temps avance seul jusqu'à la fin du chant. */
    private fun startSimulation() {
        simulation?.cancel()
        simulation = scope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                delay(1000)
                val state = _state.value
                val song = state.current ?: break
                if (!state.isPlaying || song.hasAudio) break
                val next = state.positionSeconds + 1
                if (next >= song.duration) {
                    if (state.repeat) {
                        _state.value = state.copy(positionSeconds = 0)
                    } else {
                        _state.value = state.copy(positionSeconds = song.duration)
                        onTrackFinished()
                        break
                    }
                } else {
                    _state.value = state.copy(positionSeconds = next)
                }
            }
        }
    }

    private fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(audioUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(credit.ifBlank { "OYENGA" })
                .setAlbumTitle(Moments.label(moment))
                .setArtworkUri(artworkUri)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .build(),
        )
        .build()
}
