package cm.oyenga.app.data.local

import android.content.Context
import cm.oyenga.app.data.model.Song
import kotlinx.serialization.json.Json

/**
 * Le corpus de base, lu une seule fois depuis `assets/songs.json`.
 *
 * 254 chants BETI embarqués : l'application démarre et fonctionne sans réseau,
 * exactement comme la PWA d'origine.
 */
class SongCatalog private constructor(raw: List<Song>) {

    /**
     * Les identifiants sont rendus uniques au chargement.
     *
     * Les listes de l'interface sont indexées par identifiant : un doublon ne dégrade
     * pas l'affichage, il fait planter l'écran. Le corpus livré est propre, mais un
     * recueil réimporté plus tard ne le sera pas forcément.
     */
    val songs: List<Song> = deduplicate(raw)

    val byId: Map<String, Song> = songs.associateBy { it.id }

    /** Ordre alphabétique français — l'ordre de référence de la file de lecture. */
    val sorted: List<Song> = songs.sortedWith(compareBy(FrenchCollator) { it.title })

    companion object {
        private fun deduplicate(songs: List<Song>): List<Song> {
            val seen = mutableSetOf<String>()
            return songs.map { song ->
                if (seen.add(song.id)) {
                    song
                } else {
                    var suffix = 2
                    while (!seen.add("${song.id}-$suffix")) suffix++
                    song.copy(id = "${song.id}-$suffix")
                }
            }
        }

        @Volatile
        private var instance: SongCatalog? = null

        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        fun get(context: Context): SongCatalog =
            instance ?: synchronized(this) {
                instance ?: load(context).also { instance = it }
            }

        private fun load(context: Context): SongCatalog {
            val raw = context.applicationContext.assets
                .open("songs.json")
                .bufferedReader()
                .use { it.readText() }
            return SongCatalog(json.decodeFromString<List<Song>>(raw))
        }
    }
}

/** Tri français : « É » se range avec « E », comme `localeCompare("fr")` côté web. */
internal object FrenchCollator : Comparator<String> {
    private val collator = java.text.Collator.getInstance(java.util.Locale.FRENCH).apply {
        strength = java.text.Collator.SECONDARY
    }

    override fun compare(a: String, b: String): Int = collator.compare(a, b)
}
