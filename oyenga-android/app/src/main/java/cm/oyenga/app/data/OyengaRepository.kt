package cm.oyenga.app.data

import android.content.Context
import cm.oyenga.app.data.local.DbStore
import cm.oyenga.app.data.local.FrenchCollator
import cm.oyenga.app.data.local.Prefs
import cm.oyenga.app.data.local.SongCatalog
import cm.oyenga.app.data.model.OyengaDb
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.data.remote.AelfClient
import cm.oyenga.app.data.remote.AnthropicIntroWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Source de vérité unique de l'application.
 *
 * Toute modification passe par [update] : l'état est mis à jour en mémoire puis
 * sauvegardé. Les écrans observent [db] et ne persistent jamais rien eux-mêmes —
 * c'est ce qui garantit qu'un chant modifié en administration apparaît immédiatement
 * dans le répertoire et dans le programme.
 */
class OyengaRepository(
    context: Context,
    private val scope: CoroutineScope,
) {

    private val appContext = context.applicationContext
    private val store = DbStore(appContext)
    private val writeLock = Mutex()

    val catalog: SongCatalog = SongCatalog.get(appContext)
    val prefs = Prefs(appContext)
    val aelf = AelfClient()
    val introWriter = AnthropicIntroWriter()

    private val _db = MutableStateFlow(Seed.build(catalog.songs))
    val db: StateFlow<OyengaDb> = _db.asStateFlow()

    private val _songs = MutableStateFlow(computeSongs(_db.value))
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    /**
     * Restaure l'état sauvegardé par-dessus les données livrées. La fusion est
     * délibérément asymétrique : un corpus de chants enrichi par une mise à jour de
     * l'application reste visible, alors que le travail de la chorale (programmes,
     * publications, comptes) écrase toujours la démonstration.
     */
    suspend fun restore() {
        val saved = store.load()
        if (saved != null) {
            _db.value = saved.copy(
                user = saved.accounts.firstOrNull { it.id == saved.user.id } ?: saved.user,
            )
            _songs.value = computeSongs(_db.value)
        }
        _ready.value = true
    }

    /** Applique une transformation à l'état et la persiste. */
    fun update(transform: (OyengaDb) -> OyengaDb) {
        val next = transform(_db.value)
        _db.value = next
        _songs.value = computeSongs(next)
        scope.launch { writeLock.withLock { store.save(next) } }
    }

    fun song(id: String?): Song? = id?.let { key -> _songs.value.firstOrNull { it.id == key } }

    /** Le répertoire effectif : corpus livré + modifications + chants ajoutés. */
    private fun computeSongs(db: OyengaDb): List<Song> {
        val merged = catalog.songs.map { song -> db.overrides[song.id]?.applyTo(song) ?: song }
        return (merged + db.addedSongs).sortedWith(compareBy(FrenchCollator) { it.title })
    }

    /** Les langues présentes dans le répertoire, pour alimenter les filtres. */
    fun languages(): List<String> = _songs.value.map { it.lang }.distinct().sorted()
}
