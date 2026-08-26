package cm.oyenga.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Un chant du répertoire.
 *
 * Le corpus de base (254 chants BETI) est embarqué dans `assets/songs.json` : l'application
 * est utilisable sans réseau, comme la PWA. Les modifications faites en administration
 * sont stockées à part (voir [SongOverride]) pour que le corpus livré reste intact.
 */
@Serializable
data class Song(
    val id: String,
    val title: String,
    val moment: String = "variete",
    @SerialName("momentRaw") val momentRaw: String = "",
    val lang: String = "BETI",
    val credit: String = "",
    val creditType: String = "unknown",
    val lyrics: String = "",
    val temps: List<String> = emptyList(),
    val fetes: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val audioUrl: String = "",
    val partitionUrl: String = "",
    val duration: Int = 180,
) {
    /** Un chant sans lien audio est lu en mode simulé (défilement du temps sans son). */
    val hasAudio: Boolean get() = audioUrl.isNotBlank()

    val hasPartition: Boolean get() = partitionUrl.isNotBlank()
}

/**
 * Modification partielle d'un chant du corpus de base : seuls les champs renseignés
 * remplacent l'original. C'est ce qui permet de retagger un chant sans le dupliquer.
 */
@Serializable
data class SongOverride(
    val title: String? = null,
    val moment: String? = null,
    val lang: String? = null,
    val credit: String? = null,
    val creditType: String? = null,
    val lyrics: String? = null,
    val temps: List<String>? = null,
    val fetes: List<String>? = null,
    val themes: List<String>? = null,
    val audioUrl: String? = null,
    val partitionUrl: String? = null,
    val duration: Int? = null,
) {
    fun applyTo(song: Song): Song = song.copy(
        title = title ?: song.title,
        moment = moment ?: song.moment,
        lang = lang ?: song.lang,
        credit = credit ?: song.credit,
        creditType = creditType ?: song.creditType,
        lyrics = lyrics ?: song.lyrics,
        temps = temps ?: song.temps,
        fetes = fetes ?: song.fetes,
        themes = themes ?: song.themes,
        audioUrl = audioUrl ?: song.audioUrl,
        partitionUrl = partitionUrl ?: song.partitionUrl,
        duration = duration ?: song.duration,
    )

    val isEmpty: Boolean
        get() = title == null && moment == null && lang == null && credit == null &&
            creditType == null && lyrics == null && temps == null && fetes == null &&
            themes == null && audioUrl == null && partitionUrl == null && duration == null

    companion object {
        /** Différence entre un chant édité et son original : on ne stocke que ce qui change. */
        fun diff(base: Song, edited: Song): SongOverride = SongOverride(
            title = edited.title.takeIf { it != base.title },
            moment = edited.moment.takeIf { it != base.moment },
            lang = edited.lang.takeIf { it != base.lang },
            credit = edited.credit.takeIf { it != base.credit },
            creditType = edited.creditType.takeIf { it != base.creditType },
            lyrics = edited.lyrics.takeIf { it != base.lyrics },
            temps = edited.temps.takeIf { it != base.temps },
            fetes = edited.fetes.takeIf { it != base.fetes },
            themes = edited.themes.takeIf { it != base.themes },
            audioUrl = edited.audioUrl.takeIf { it != base.audioUrl },
            partitionUrl = edited.partitionUrl.takeIf { it != base.partitionUrl },
            duration = edited.duration.takeIf { it != base.duration },
        )
    }
}
