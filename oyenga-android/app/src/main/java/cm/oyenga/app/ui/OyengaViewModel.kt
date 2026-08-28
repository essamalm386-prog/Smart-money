@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import cm.oyenga.app.OyengaApplication
import cm.oyenga.app.data.OyengaRepository
import cm.oyenga.app.data.liturgy.IntroductionBuilder
import cm.oyenga.app.data.model.Account
import cm.oyenga.app.data.model.Ad
import cm.oyenga.app.data.model.Comment
import cm.oyenga.app.data.model.Community
import cm.oyenga.app.data.model.OyengaDb
import cm.oyenga.app.data.model.Playlist
import cm.oyenga.app.data.model.Post
import cm.oyenga.app.data.model.Program
import cm.oyenga.app.data.model.ProgramItem
import cm.oyenga.app.data.model.ReadingDay
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.data.model.SongOverride
import cm.oyenga.app.data.remote.AelfClient
import cm.oyenga.app.player.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Onglets de la barre de navigation. */
enum class Tab { ACCUEIL, CATALOGUE, LECTEUR, COMMUNAUTE }

/** Feuilles modales. Une seule est ouverte à la fois — l'empilement rendrait le retour illisible. */
sealed interface Sheet {
    data object Readings : Sheet
    data object Account : Sheet
    data object Compose : Sheet
    data object Discover : Sheet
    data object Filters : Sheet
    data object Lyrics : Sheet
    data object Partition : Sheet
    data object Queue : Sheet
    data object Playlists : Sheet
    data object Program : Sheet
    data class PostDetail(val postId: String) : Sheet
}

data class Filters(
    val moments: Set<String> = emptySet(),
    val langs: Set<String> = emptySet(),
    val temps: Set<String> = emptySet(),
    val themes: Set<String> = emptySet(),
) {
    val count: Int get() = moments.size + langs.size + temps.size + themes.size
    val isEmpty: Boolean get() = count == 0
}

data class ReadingsState(
    val loading: Boolean = true,
    val day: ReadingDay? = null,
    val targetDate: String = "",
    /** Vrai quand l'AELF n'a rien donné : on l'annonce plutôt que d'afficher une carte vide. */
    val offline: Boolean = false,
)

data class UiState(
    val tab: Tab = Tab.ACCUEIL,
    val playerOpen: Boolean = false,
    val adminOpen: Boolean = false,
    val sheet: Sheet? = null,
    val filters: Filters = Filters(),
    val search: String = "",
    val toast: String? = null,
)

@UnstableApi
class OyengaViewModel(
    val repository: OyengaRepository,
    val playback: PlaybackController,
) : ViewModel() {

    val db: StateFlow<OyengaDb> = repository.db
    val songs: StateFlow<List<Song>> = repository.songs

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val _readings = MutableStateFlow(ReadingsState())
    val readings: StateFlow<ReadingsState> = _readings.asStateFlow()

    private var toastCounter = 0

    init {
        loadReadings()
        restoreSession()
    }

    // ------------------------------------------------------------------ navigation

    fun selectTab(tab: Tab) {
        if (tab == Tab.LECTEUR) {
            if (playback.state.value.current != null) {
                _ui.value = _ui.value.copy(playerOpen = true)
            } else {
                toast("Choisis d'abord un chant")
            }
            return
        }
        _ui.value = _ui.value.copy(tab = tab, playerOpen = false)
        repository.prefs.lastTab = tab.name
    }

    fun openPlayer() {
        if (playback.state.value.current == null) {
            toast("Choisis d'abord un chant")
            return
        }
        _ui.value = _ui.value.copy(playerOpen = true)
    }

    fun closePlayer() {
        _ui.value = _ui.value.copy(playerOpen = false)
    }

    fun openAdmin() {
        _ui.value = _ui.value.copy(adminOpen = true, sheet = null, playerOpen = false)
    }

    fun closeAdmin() {
        _ui.value = _ui.value.copy(adminOpen = false)
    }

    fun openSheet(sheet: Sheet) {
        // La partition et les paroles n'ont de sens qu'avec un chant en cours.
        if ((sheet is Sheet.Lyrics || sheet is Sheet.Partition) && playback.state.value.current == null) {
            toast("Choisis d'abord un chant")
            return
        }
        _ui.value = _ui.value.copy(sheet = sheet)
    }

    fun closeSheet() {
        _ui.value = _ui.value.copy(sheet = null)
    }

    fun setSearch(value: String) {
        _ui.value = _ui.value.copy(search = value)
    }

    fun setFilters(filters: Filters) {
        _ui.value = _ui.value.copy(filters = filters)
    }

    fun toast(message: String) {
        toastCounter++
        _ui.value = _ui.value.copy(toast = message)
    }

    fun consumeToast() {
        _ui.value = _ui.value.copy(toast = null)
    }

    // ------------------------------------------------------------------ lecture

    fun play(song: Song, openFullPlayer: Boolean = false) {
        playback.play(song, repository.songs.value)
        repository.prefs.lastSongId = song.id
        if (openFullPlayer) _ui.value = _ui.value.copy(playerOpen = true)
    }

    fun stopPlayback() {
        playback.stop()
        repository.prefs.lastSongId = null
        repository.prefs.lastPositionSeconds = 0
        _ui.value = _ui.value.copy(playerOpen = false)
    }

    fun rememberPosition() {
        val state = playback.state.value
        val song = state.current ?: return
        repository.prefs.lastSongId = song.id
        repository.prefs.lastPositionSeconds = state.positionSeconds
    }

    private fun restoreSession() {
        viewModelScope.launch {
            repository.restore()
            repository.prefs.lastTab?.let { name ->
                runCatching { Tab.valueOf(name) }.getOrNull()?.let { tab ->
                    if (tab != Tab.LECTEUR) _ui.value = _ui.value.copy(tab = tab)
                }
            }
            val song = repository.song(repository.prefs.lastSongId)
            if (song != null) {
                playback.restoreWithoutPlaying(
                    song = song,
                    queue = repository.songs.value,
                    positionSeconds = repository.prefs.lastPositionSeconds,
                )
            }
        }
    }

    fun toggleLike() {
        val song = playback.state.value.current ?: return
        toggleLike(song.id)
    }

    fun toggleLike(songId: String) {
        var added = false
        repository.update { db ->
            val likes = db.user.likes
            added = songId !in likes
            val updated = db.user.copy(
                likes = if (added) likes + songId else likes - songId,
            )
            db.withUser(updated)
        }
        toast(if (added) "Ajouté aux favoris ♥" else "Retiré des favoris")
    }

    fun isLiked(songId: String?): Boolean =
        songId != null && songId in db.value.user.likes

    // ------------------------------------------------------------------ lectures du jour

    fun loadReadings() {
        val fallbackDate = AelfClient.nextSunday().toString()
        _readings.value = ReadingsState(loading = true, targetDate = fallbackDate)
        viewModelScope.launch {
            val result = repository.aelf.nextCelebration()
            val live = result.getOrNull()
            val date = live?.date ?: fallbackDate
            _readings.value = ReadingsState(
                loading = false,
                day = live,
                targetDate = date,
                offline = result.isFailure,
            )
        }
    }

    /**
     * La célébration affichée, par ordre de priorité : ce que le responsable a saisi,
     * puis les textes de l'AELF, puis la démonstration livrée, puis une carte d'attente.
     * L'édition humaine passe toujours devant l'automatique.
     */
    fun currentReadings(): ReadingDay {
        val state = _readings.value
        val date = state.targetDate
        val db = db.value
        val edited = db.readings.firstOrNull { it.date == date && !it.live && !it.seed }
        val seeded = db.readings.firstOrNull { it.date == date }
        return edited ?: state.day ?: seeded ?: ReadingDay(
            id = "off-$date",
            date = date,
            offline = true,
            isSunday = runCatching { LocalDate.parse(date).dayOfWeek.value == 7 }.getOrDefault(true),
            title = "Prochaine messe",
        )
    }

    fun introductionFor(day: ReadingDay): String =
        IntroductionBuilder.build(day, db.value.introOverrides[day.date])

    fun saveIntroduction(date: String, text: String) {
        repository.update { db ->
            db.copy(introOverrides = db.introOverrides + (date to text))
        }
        toast("Introduction enregistrée ✓")
    }

    fun clearIntroduction(date: String) {
        repository.update { db -> db.copy(introOverrides = db.introOverrides - date) }
        toast("Introduction remise à zéro")
    }

    fun generateIntroduction(day: ReadingDay, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.introWriter.write(day, db.value.ai)
            result.onSuccess { text ->
                saveIntroduction(day.date, text)
                onDone(true)
            }.onFailure { error ->
                toast("Rédaction assistée : ${error.message ?: "échec"}")
                onDone(false)
            }
        }
    }

    fun refreshReadingsFor(date: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val parsed = runCatching { LocalDate.parse(date) }.getOrNull()
            if (parsed == null) {
                toast("Date invalide — format attendu AAAA-MM-JJ")
                onDone(false)
                return@launch
            }
            repository.aelf.fetch(parsed)
                .onSuccess { day ->
                    repository.update { db ->
                        db.copy(readings = db.readings.filterNot { it.date == date } + day)
                    }
                    toast("Textes du $date récupérés ✓")
                    onDone(true)
                }
                .onFailure {
                    toast("Textes indisponibles pour cette date")
                    onDone(false)
                }
        }
    }

    fun saveReadings(day: ReadingDay) {
        repository.update { db ->
            db.copy(
                readings = db.readings.filterNot { it.date == day.date } +
                    day.copy(live = false, seed = false, offline = false),
            )
        }
        toast("Lectures enregistrées ✓")
    }

    // ------------------------------------------------------------------ communauté

    fun likePost(post: Post) {
        var subscribedTo: String? = null
        repository.update { db ->
            val posts = db.posts.map { existing ->
                if (existing.id == post.id) {
                    existing.copy(
                        liked = !existing.liked,
                        likes = existing.likes + if (existing.liked) -1 else 1,
                    )
                } else {
                    existing
                }
            }
            // Aimer une publication, c'est vouloir suivre : on abonne, et on le dit.
            val user = if (!post.liked && post.communityId !in db.user.subscriptions) {
                subscribedTo = db.community(post.communityId)?.name
                db.user.copy(subscriptions = db.user.subscriptions + post.communityId)
            } else {
                db.user
            }
            db.copy(posts = posts).withUser(user)
        }
        subscribedTo?.let { toast("Abonné à $it ✓") }
    }

    fun joinCommunity(community: Community) {
        if (community.isPrivate) {
            toast("Groupe privé — demande d'adhésion envoyée")
            return
        }
        repository.update { db ->
            val communities = db.communities.map {
                if (it.id == community.id && !it.joined) it.copy(joined = true, members = it.members + 1) else it
            }
            val user = db.user.copy(
                subscriptions = (db.user.subscriptions + community.id).distinct(),
            )
            db.copy(communities = communities).withUser(user)
        }
        toast("Abonné à ${community.name} ✓")
    }

    fun isSubscribed(communityId: String): Boolean = communityId in db.value.user.subscriptions

    fun publish(
        communityId: String,
        text: String,
        videoUrl: String = "",
        songId: String = "",
    ) {
        if (text.isBlank()) {
            toast("Écris d'abord ton message")
            return
        }
        repository.update { db ->
            val post = Post(
                id = "p-" + System.currentTimeMillis(),
                communityId = communityId,
                author = db.user.displayName,
                authorInit = db.user.initials,
                timeAgo = "À l'instant",
                ts = System.currentTimeMillis(),
                text = text.trim(),
                videoUrl = videoUrl.trim(),
                songId = songId,
            )
            db.copy(posts = listOf(post) + db.posts)
        }
        toast("Publication envoyée ✓")
        closeSheet()
    }

    fun comment(postId: String, text: String) {
        if (text.isBlank()) return
        repository.update { db ->
            val posts = db.posts.map { post ->
                if (post.id != postId) {
                    post
                } else {
                    post.copy(
                        commentsList = post.commentsList + Comment(
                            id = "cm-" + System.currentTimeMillis(),
                            author = db.user.displayName,
                            init = db.user.initials,
                            text = text.trim(),
                        ),
                    )
                }
            }
            db.copy(posts = posts)
        }
    }

    fun deletePost(postId: String) {
        repository.update { db -> db.copy(posts = db.posts.filterNot { it.id == postId }) }
        toast("Publication supprimée")
    }

    // ------------------------------------------------------------------ compte

    fun switchAccount(account: Account) {
        repository.update { db ->
            val accounts = db.accounts.map { if (it.id == db.user.id) db.user else it }
            val next = accounts.firstOrNull { it.id == account.id } ?: db.user
            db.copy(accounts = accounts, user = next)
        }
        toast("Connecté : ${account.displayName}")
        closeSheet()
    }

    fun renameProfile(fullName: String) {
        val trimmed = fullName.trim()
        if (trimmed.isEmpty()) {
            toast("Le nom ne peut pas être vide")
            return
        }
        repository.update { db ->
            db.withUser(
                db.user.copy(
                    name = trimmed.substringBefore(' '),
                    full = trimmed,
                    initials = trimmed.split(Regex("\\s+"))
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                        .ifBlank { "?" },
                ),
            )
        }
        toast("Profil enregistré ✓")
    }

    fun setAiConfig(key: String, model: String) {
        repository.update { db -> db.copy(ai = db.ai.copy(key = key.trim(), model = model)) }
        toast("Réglages enregistrés ✓")
    }

    // ------------------------------------------------------------------ playlists

    fun createPlaylist(name: String, firstSongId: String?) {
        if (name.isBlank()) {
            toast("Donne un nom à la playlist")
            return
        }
        repository.update { db ->
            db.copy(
                playlists = db.playlists + Playlist(
                    id = "pl-" + System.currentTimeMillis(),
                    name = name.trim(),
                    songIds = listOfNotNull(firstSongId),
                ),
            )
        }
        toast("Playlist créée ✓")
    }

    fun togglePlaylistSong(playlistId: String, songId: String) {
        var added = false
        repository.update { db ->
            db.copy(
                playlists = db.playlists.map { playlist ->
                    if (playlist.id != playlistId) {
                        playlist
                    } else {
                        added = songId !in playlist.songIds
                        playlist.copy(
                            songIds = if (added) playlist.songIds + songId else playlist.songIds - songId,
                        )
                    }
                },
            )
        }
        toast(if (added) "Ajouté à la playlist ✓" else "Retiré de la playlist")
    }

    fun deletePlaylist(playlistId: String) {
        repository.update { db -> db.copy(playlists = db.playlists.filterNot { it.id == playlistId }) }
        toast("Playlist supprimée")
    }

    // ------------------------------------------------------------------ administration

    fun saveSong(edited: Song, original: Song?) {
        repository.update { db ->
            val base = repository.catalog.byId[edited.id]
            when {
                // Chant du corpus livré : on ne stocke que l'écart avec l'original.
                base != null -> {
                    val diff = SongOverride.diff(base, edited)
                    db.copy(
                        overrides = if (diff.isEmpty) db.overrides - edited.id else db.overrides + (edited.id to diff),
                    )
                }
                db.addedSongs.any { it.id == edited.id } ->
                    db.copy(addedSongs = db.addedSongs.map { if (it.id == edited.id) edited else it })
                else -> db.copy(addedSongs = db.addedSongs + edited)
            }
        }
        toast(if (original == null) "Chant ajouté ✓" else "Chant enregistré ✓")
    }

    fun deleteSong(song: Song) {
        val isBase = repository.catalog.byId.containsKey(song.id)
        if (isBase) {
            // Le corpus livré n'est pas modifiable en profondeur : on annule les
            // modifications plutôt que de faire croire à une suppression définitive.
            repository.update { db -> db.copy(overrides = db.overrides - song.id) }
            toast("Modifications annulées — chant du recueil restauré")
        } else {
            repository.update { db -> db.copy(addedSongs = db.addedSongs.filterNot { it.id == song.id }) }
            toast("Chant supprimé")
        }
    }

    fun saveProgram(program: Program) {
        repository.update { db ->
            val others = db.programs.filterNot { it.id == program.id }
            db.copy(programs = listOf(program) + others)
        }
        toast("Programme publié ✓")
    }

    fun updateProgramItem(partKey: String, item: ProgramItem?) {
        repository.update { db ->
            val program = db.program ?: return@update db
            val items = if (item == null || item.list.isEmpty()) {
                program.items - partKey
            } else {
                program.items + (partKey to item)
            }
            db.copy(programs = listOf(program.copy(items = items)) + db.programs.drop(1))
        }
    }

    fun saveAd(ad: Ad) {
        repository.update { db ->
            val exists = db.ads.any { it.id == ad.id }
            db.copy(ads = if (exists) db.ads.map { if (it.id == ad.id) ad else it } else db.ads + ad)
        }
        toast("Publicité enregistrée ✓")
    }

    fun deleteAd(adId: String) {
        repository.update { db -> db.copy(ads = db.ads.filterNot { it.id == adId }) }
        toast("Publicité supprimée")
    }

    companion object {
        fun factory(app: OyengaApplication) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OyengaViewModel(app.repository, app.playback) as T
        }
    }
}
