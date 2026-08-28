package cm.oyenga.app.data

import cm.oyenga.app.data.model.Account
import cm.oyenga.app.data.model.Ad
import cm.oyenga.app.data.model.AiConfig
import cm.oyenga.app.data.model.Comment
import cm.oyenga.app.data.model.Community
import cm.oyenga.app.data.model.MASS_PARTS
import cm.oyenga.app.data.model.OyengaDb
import cm.oyenga.app.data.model.Post
import cm.oyenga.app.data.model.Program
import cm.oyenga.app.data.model.ProgramItem
import cm.oyenga.app.data.model.ReadingDay
import cm.oyenga.app.data.model.ReadingText
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.data.model.SongOverride

/**
 * L'état livré avec l'application : un programme de démonstration bâti sur le vrai
 * répertoire BETI, une célébration de référence, trois communautés et le jeu de
 * comptes qui permet d'essayer les rôles (membre, responsable, premium).
 *
 * Ce n'est pas du remplissage : sans ces données, l'algorithme de suggestion n'a
 * rien à proposer et les écrans s'ouvrent vides à la première installation.
 */
object Seed {

    fun build(baseSongs: List<Song>): OyengaDb {
        val overrides = seedLiturgicalTags(baseSongs)
        val tagged = baseSongs.map { song -> overrides[song.id]?.applyTo(song) ?: song }
        return OyengaDb(
            overrides = overrides,
            addedSongs = emptyList(),
            programs = listOf(demoProgram(tagged)),
            readings = listOf(demoReadings()),
            communities = communities(),
            posts = posts(),
            ads = ads(),
            playlists = emptyList(),
            accounts = accounts(),
            user = accounts().first(),
            ai = AiConfig(),
            introOverrides = emptyMap(),
        )
    }

    // ---------------------------------------------------------------- programme

    /**
     * Pour chaque partie de la messe, jusqu'à trois chants compatibles : le premier est
     * recommandé, les autres restent proposés. La chorale choisit, l'application propose.
     */
    private fun demoProgram(songs: List<Song>): Program {
        val items = buildMap {
            for (part in MASS_PARTS) {
                val ids = mutableListOf<String>()
                for (moment in part.moments) {
                    for (song in songs) {
                        if (song.moment == moment && song.id !in ids) {
                            ids += song.id
                            if (ids.size >= 3) break
                        }
                    }
                    if (ids.size >= 3) break
                }
                if (ids.isNotEmpty()) put(part.key, ProgramItem(list = ids, rec = ids.first()))
            }
        }
        return Program(
            id = "prog-demo",
            date = "2026-07-19",
            saison = "Temps ordinaire",
            annee = "Année A",
            fete = "16e dimanche du Temps Ordinaire",
            items = items,
        )
    }

    // ---------------------------------------------------------------- lectures

    private fun demoReadings() = ReadingDay(
        id = "rd-2026-07-19",
        date = "2026-07-19",
        seed = true,
        saison = "Temps ordinaire",
        annee = "Année A",
        title = "16ème dimanche du Temps Ordinaire",
        phrase = "« Laissez-les pousser ensemble jusqu'à la moisson »",
        readings = listOf(
            ReadingText(
                label = ReadingDay.PREMIERE,
                ref = "Sg 12, 13.16-19",
                source = "Lecture du livre de la Sagesse",
                quote = "« Après la faute tu accordes la conversion »",
                summary = "Dieu, maître de toute chose, n'a pas besoin de rendre des comptes. " +
                    "Sa toute-puissance ne s'exprime pas dans la sévérité mais dans l'indulgence : " +
                    "parce qu'il dispose de la force, il juge avec douceur et laisse à l'homme le " +
                    "temps de se convertir.",
            ),
            ReadingText(
                label = ReadingDay.PSAUME,
                ref = "Ps 85 (86), 5-6, 9-10, 15-16a",
                source = "Psaume",
                quote = "« Toi qui es bon et qui pardonnes »",
                summary = "Le psalmiste chante un Dieu lent à la colère et plein d'amour, " +
                    "vers qui toutes les nations viendront se prosterner.",
            ),
            ReadingText(
                label = ReadingDay.DEUXIEME,
                ref = "Rm 8, 26-27",
                source = "Lecture de la lettre de saint Paul apôtre aux Romains",
                quote = "« L'Esprit lui-même intercède pour nous »",
                summary = "Nous ne savons pas prier comme il faut ; c'est l'Esprit qui vient au " +
                    "secours de notre faiblesse et qui intercède pour les saints selon Dieu.",
            ),
            ReadingText(
                label = ReadingDay.EVANGILE,
                ref = "Mt 13, 24-43",
                source = "Évangile de Jésus Christ selon saint Matthieu",
                quote = "« Laissez-les pousser ensemble jusqu'à la moisson »",
                summary = "Le Royaume est comparé au bon grain semé au milieu de l'ivraie, à la " +
                    "graine de moutarde et au levain : Dieu laisse le temps agir et fait grandir " +
                    "son œuvre à partir de commencements minuscules.",
            ),
        ),
    )

    // ---------------------------------------------------------------- communauté

    private fun communities() = listOf(
        Community(
            id = "c-oyenga",
            name = "OYENGA",
            type = "Chorale & Musique",
            members = 4,
            desc = "La communauté officielle de l'application",
            visibility = "public",
            joined = true,
            role = "Responsable",
        ),
        Community(
            id = "c-chorale",
            name = "Chorale Sainte-Cécile",
            type = "Chorale paroissiale",
            members = 1,
            desc = "Répétitions et animation des messes",
            visibility = "private",
            joined = false,
        ),
        Community(
            id = "c-jtp",
            name = "Jeunes de Tous Pays",
            type = "Chorale & Musique",
            members = 2,
            desc = "Chantons pour la gloire de Dieu",
            visibility = "public",
            joined = false,
        ),
    )

    private fun posts() = listOf(
        Post(
            id = "p1",
            communityId = "c-oyenga",
            author = "OYENGA · Louis Marie",
            authorInit = "LM",
            timeAgo = "Il y a 2 sem.",
            ts = 1,
            text = "Nous chanterons Ton Saint Nom Seigneur Jésus, pour tes merveilles dans nos vies 🙏",
            image = "choir",
            songId = "ENTREE-001",
            likes = 2,
            commentsList = listOf(
                Comment("cm1", "Bia", "BY", "Amen ! Hâte d'y être 🙌", "Il y a 2 sem."),
                Comment("cm2", "Sœur Cécile", "SC", "Magnifique, que Dieu soit loué.", "Il y a 12 j."),
                Comment("cm3", "Louis Marie", "LM", "Merci à toute la chorale 🎶", "Il y a 10 j."),
            ),
        ),
        Post(
            id = "p2",
            communityId = "c-jtp",
            author = "Jeunes de Tous Pays",
            authorInit = "JT",
            timeAgo = "Il y a 3 jours",
            ts = 2,
            text = "Nouvelle répétition dimanche à 15h. On travaille « TEBEGE BEBEGE » pour " +
                "l'entrée de la messe. Venez nombreux !",
            songId = "ENTREE-001",
            likes = 12,
            commentsList = listOf(
                Comment("cm4", "Bia", "BY", "Je serai là 👏", "Il y a 2 j."),
            ),
        ),
        Post(
            id = "p3",
            communityId = "c-chorale",
            author = "Chorale Sainte-Cécile",
            authorInit = "CS",
            timeAgo = "Il y a 5 jours",
            ts = 3,
            text = "Merci à tous pour la belle animation de la messe d'hier. La partition de " +
                "« MA NYOLO M'AZU » est disponible dans le répertoire. 🎶",
            likes = 8,
        ),
    )

    private fun ads() = listOf(
        Ad(
            id = "a1",
            title = "Éditions Liturgiques du Cameroun",
            text = "Recueils de chants BETI imprimés — livraison partout.",
            cta = "Découvrir",
        ),
        Ad(
            id = "a2",
            title = "Studio Nkolbisson",
            text = "Enregistrez votre chorale en qualité professionnelle.",
            cta = "Réserver",
        ),
    )

    private fun accounts() = listOf(
        Account("u-louis", "Louis", "Louis Marie", "LM", isAdmin = true, subscriptions = listOf("c-oyenga")),
        Account("u-bia", "Bia", "Bia", "BY", subscriptions = listOf("c-oyenga", "c-jtp")),
        Account("u-cecile", "Sœur Cécile", "Sœur Cécile", "SC", premium = true, isAdmin = true, subscriptions = listOf("c-oyenga", "c-chorale")),
        Account("u-guest", "Invité", "Invité", "IN"),
    )

    // ---------------------------------------------------------------- étiquetage

    /**
     * Le corpus livré n'est pas étiqueté par temps liturgique. On en marque un
     * échantillon représentatif — quelques chants par moment — pour que l'algorithme
     * de suggestion ait de quoi travailler dès la première ouverture. Le responsable
     * complète ensuite depuis l'administration.
     */
    private fun seedLiturgicalTags(songs: List<Song>): Map<String, SongOverride> {
        val overrides = mutableMapOf<String, SongOverride>()
        songs.groupBy { it.moment }.values.forEach { group ->
            group.forEachIndexed { index, song ->
                when {
                    index < 5 -> overrides[song.id] = SongOverride(
                        temps = listOf("Temps ordinaire"), fetes = emptyList(), themes = listOf("Louange"),
                    )
                    index < 7 -> overrides[song.id] = SongOverride(
                        temps = listOf("Avent", "Noël"), fetes = listOf("Nativité"), themes = listOf("Espérance"),
                    )
                    index < 9 -> overrides[song.id] = SongOverride(
                        temps = listOf("Carême", "Temps pascal"), fetes = listOf("Pâques"), themes = listOf("Conversion"),
                    )
                }
            }
        }
        songs.filter { it.moment == "marial" }.take(12).forEachIndexed { index, song ->
            overrides[song.id] = SongOverride(
                temps = listOf("Fêtes mariales"),
                fetes = listOf(if (index % 2 == 1) "Assomption" else "Immaculée Conception"),
                themes = listOf("Confiance"),
            )
        }
        return overrides
    }
}
