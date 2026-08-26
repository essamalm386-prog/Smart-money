package cm.oyenga.app.data.model

import kotlinx.serialization.Serializable

/**
 * Une partie de l'ordinaire de la messe. `moments` liste les catégories de chants
 * acceptables pour cette partie — c'est la base de l'algorithme de suggestion.
 */
data class MassPart(
    val key: String,
    val label: String,
    val moments: List<String>,
)

/** L'ordinaire complet, dans l'ordre de la célébration. */
val MASS_PARTS: List<MassPart> = listOf(
    MassPart("entree", "Chant d'entrée", listOf("entree")),
    MassPart("kyrie", "Kyrie — Seigneur, prends pitié", listOf("kyrie")),
    MassPart("gloria", "Gloria", listOf("gloria")),
    MassPart("psaume", "Psaume responsorial", listOf("psaume", "meditation")),
    MassPart("acclamation", "Acclamation — Alléluia", listOf("acclamation")),
    MassPart("credo", "Credo — Profession de foi", listOf("credo")),
    MassPart("priere_universelle", "Prière universelle", listOf("priere_universelle")),
    MassPart("offertoire", "Offertoire", listOf("offertoire")),
    MassPart("sanctus", "Sanctus", listOf("sanctus")),
    MassPart("anamnese", "Anamnèse", listOf("sanctus", "meditation")),
    MassPart("pater", "Notre Père — Pater", listOf("pater")),
    MassPart("agnus", "Agneau de Dieu — Agnus Dei", listOf("agnus")),
    MassPart("communion", "Communion", listOf("communion")),
    MassPart("action_grace", "Action de grâce / Méditation", listOf("meditation", "louange")),
    MassPart("marial", "Chant à la Vierge Marie", listOf("marial")),
    MassPart("envoi", "Chant d'envoi / Sortie", listOf("envoi")),
)

val TEMPS_LITURGIQUES = listOf(
    "Avent", "Noël", "Temps ordinaire", "Carême", "Temps pascal", "Pentecôte", "Fêtes mariales",
)

val FETES = listOf(
    "Nativité", "Épiphanie", "Rameaux", "Jeudi Saint", "Vendredi Saint", "Pâques", "Ascension",
    "Pentecôte", "Assomption", "Toussaint", "Christ Roi", "Immaculée Conception",
    "Corpus Christi", "Sacré-Cœur",
)

val THEMES = listOf(
    "Louange", "Adoration", "Miséricorde", "Action de grâce", "Confiance", "Conversion",
    "Joie", "Paix", "Amour", "Espérance", "Mission",
)

/**
 * Sélection d'une partie de messe : un chant recommandé et la liste des propositions
 * valables. La chorale garde toujours le choix — c'est le principe de la PWA.
 */
@Serializable
data class ProgramItem(
    val list: List<String> = emptyList(),
    val rec: String = "",
) {
    val alternatives: Int get() = (list.size - 1).coerceAtLeast(0)

    /** Le chant recommandé, en retombant sur le premier de la liste si besoin. */
    val recommended: String? get() = rec.ifBlank { list.firstOrNull() }
}

/** Le programme de chants d'une célébration. */
@Serializable
data class Program(
    val id: String,
    val date: String,
    val saison: String = "",
    val annee: String = "",
    val fete: String = "",
    val items: Map<String, ProgramItem> = emptyMap(),
)

/** Une lecture (1re lecture, psaume, 2e lecture, évangile). */
@Serializable
data class ReadingText(
    val label: String,
    val ref: String = "",
    val source: String = "",
    val quote: String = "",
    val summary: String = "",
    val text: String = "",
)

/**
 * Les textes d'une célébration.
 *
 * `live` marque les textes récupérés automatiquement chez l'AELF, `seed` les textes
 * de démonstration livrés avec l'application, `offline` la carte affichée quand le
 * réseau n'a rien donné. Une édition faite en administration n'a aucun de ces
 * marqueurs : c'est elle qui gagne sur tout le reste.
 */
@Serializable
data class ReadingDay(
    val id: String,
    val date: String,
    val degre: String = "",
    val isSunday: Boolean = true,
    val saison: String = "",
    val annee: String = "",
    val title: String = "",
    val phrase: String = "",
    val readings: List<ReadingText> = emptyList(),
    val intro: String = "",
    val live: Boolean = false,
    val seed: Boolean = false,
    val offline: Boolean = false,
) {
    fun reading(label: String): ReadingText? = readings.firstOrNull { it.label == label }

    companion object {
        const val PREMIERE = "1re LECTURE"
        const val PSAUME = "PSAUME"
        const val DEUXIEME = "2e LECTURE"
        const val EVANGILE = "ÉVANGILE"
    }
}
