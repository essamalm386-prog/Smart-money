package cm.oyenga.app.data.liturgy

import cm.oyenga.app.data.model.MassPart
import cm.oyenga.app.data.model.Song

/** Contexte liturgique d'une célébration : ce sur quoi on fait porter la suggestion. */
data class LiturgicalContext(
    val saison: String = "",
    val fete: String = "",
    val theme: String = "",
)

data class ScoredSong(val song: Song, val score: Int)

/**
 * Suggestion de chants par attributs.
 *
 * Le score n'est pas un classement d'opinion : il additionne des correspondances
 * factuelles (moment de la messe, temps liturgique, fête, thème) et pénalise un chant
 * explicitement étiqueté pour un AUTRE temps. Une fête pèse plus lourd qu'un temps,
 * qui pèse plus lourd qu'un thème — c'est l'ordre de contrainte de la liturgie.
 */
object SuggestionEngine {

    fun suggest(songs: List<Song>, part: MassPart, context: LiturgicalContext): List<ScoredSong> {
        val allowed = part.moments.toSet()
        return songs
            .filter { it.moment in allowed }
            .map { song ->
                var score = 1 // socle : le chant correspond à la partie de la messe
                if (context.saison.isNotBlank() && context.saison in song.temps) score += 3
                if (context.fete.isNotBlank() && context.fete in song.fetes) score += 4
                if (context.theme.isNotBlank() && context.theme in song.themes) score += 2
                // Étiqueté pour un autre temps : on le rétrograde sans l'exclure.
                if (context.saison.isNotBlank() && song.temps.isNotEmpty() && context.saison !in song.temps) {
                    score -= 1
                }
                ScoredSong(song, score)
            }
            .sortedWith(compareByDescending<ScoredSong> { it.score }.thenBy { it.song.title })
    }

    /** Les trois meilleures propositions, la première recommandée. */
    fun topFor(songs: List<Song>, part: MassPart, context: LiturgicalContext, count: Int = 3): List<Song> =
        suggest(songs, part, context).take(count).map { it.song }
}
