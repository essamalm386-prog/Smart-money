package cm.oyenga.app

import cm.oyenga.app.data.liturgy.IntroductionBuilder
import cm.oyenga.app.data.liturgy.LiturgicalContext
import cm.oyenga.app.data.liturgy.SuggestionEngine
import cm.oyenga.app.data.model.MASS_PARTS
import cm.oyenga.app.data.model.ReadingDay
import cm.oyenga.app.data.model.ReadingText
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.data.remote.AelfClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IntroductionBuilderTest {

    private val day = ReadingDay(
        id = "t",
        date = "2026-07-19",
        annee = "Année A",
        title = "16e dimanche du Temps Ordinaire",
        readings = listOf(
            ReadingText(
                label = ReadingDay.PREMIERE,
                ref = "Sg 12, 13.16-19",
                source = "Lecture du livre de la Sagesse",
                quote = "« Après la faute tu accordes la conversion »",
            ),
            ReadingText(
                label = ReadingDay.EVANGILE,
                ref = "Mt 13, 24-43",
                source = "Évangile de Jésus Christ selon saint Matthieu",
                quote = "« Laissez-les pousser ensemble jusqu'à la moisson »",
            ),
        ),
    )

    @Test
    fun `l'introduction suit la structure attendue`() {
        val text = IntroductionBuilder.build(day)
        assertTrue(text.startsWith("Bien-aimés de Dieu, loué soit Jésus-Christ."))
        assertTrue(text.contains("Nous célébrons aujourd'hui le 16e dimanche du Temps Ordinaire, année A."))
        assertTrue(text.contains("Tout d'abord, dans la première lecture"))
        assertTrue(text.contains("Enfin, dans l'Évangile"))
        assertTrue(text.trimEnd().endsWith("Amen."))
    }

    @Test
    fun `la deuxieme lecture est omise quand elle n'existe pas`() {
        val text = IntroductionBuilder.build(day)
        assertFalse(text.contains("deuxième lecture"))
    }

    @Test
    fun `un texte saisi a la main remplace la generation`() {
        val text = IntroductionBuilder.build(day, override = "  Texte du diacre.  ")
        assertEquals("Texte du diacre.", text)
    }

    @Test
    fun `sans lectures il n'y a pas d'introduction`() {
        assertEquals("", IntroductionBuilder.build(day.copy(readings = emptyList())))
        assertEquals("", IntroductionBuilder.build(null))
    }

    @Test
    fun `la source est reformulee sans le mot Lecture`() {
        val text = IntroductionBuilder.build(day)
        assertTrue(text.contains("tirée du livre de la Sagesse"))
        assertFalse(text.contains("tirée de Lecture du livre"))
    }
}

class SuggestionEngineTest {

    private fun song(id: String, moment: String, temps: List<String> = emptyList(), fetes: List<String> = emptyList()) =
        Song(id = id, title = id, moment = moment, temps = temps, fetes = fetes)

    private val part = MASS_PARTS.first { it.key == "entree" }

    @Test
    fun `seuls les chants du bon moment sont proposes`() {
        val songs = listOf(song("a", "entree"), song("b", "communion"))
        val result = SuggestionEngine.suggest(songs, part, LiturgicalContext())
        assertEquals(listOf("a"), result.map { it.song.id })
    }

    @Test
    fun `une fete pese plus lourd qu'un temps liturgique`() {
        val songs = listOf(
            song("temps", "entree", temps = listOf("Carême")),
            song("fete", "entree", fetes = listOf("Pâques")),
        )
        val result = SuggestionEngine.suggest(songs, part, LiturgicalContext(saison = "Carême", fete = "Pâques"))
        assertEquals("fete", result.first().song.id)
    }

    @Test
    fun `un chant etiquete pour un autre temps est retrograde sans etre exclu`() {
        val songs = listOf(
            song("neutre", "entree"),
            song("hors-saison", "entree", temps = listOf("Avent")),
        )
        val result = SuggestionEngine.suggest(songs, part, LiturgicalContext(saison = "Carême"))
        assertEquals("neutre", result.first().song.id)
        assertEquals(2, result.size)
    }

    @Test
    fun `topFor rend au plus trois propositions`() {
        val songs = (1..10).map { song("s$it", "entree") }
        assertEquals(3, SuggestionEngine.topFor(songs, part, LiturgicalContext()).size)
    }
}

class AelfParsingTest {

    @Test
    fun `le html est reduit en texte lisible`() {
        val html = "<p>Premi&eacute;re ligne<br/>seconde ligne</p><p>&laquo;&nbsp;cit&eacute;&nbsp;&raquo;</p>"
        val text = AelfClient.stripHtml(html)
        assertTrue(text.contains("Premiére ligne\nseconde ligne"))
        assertTrue(text.contains("« cité »"))
        assertFalse(text.contains("<"))
    }

    @Test
    fun `le resume s'arrete sur une phrase entiere`() {
        val long = "Première phrase courte. " + "Deuxième phrase ".repeat(40) + "."
        val summary = AelfClient.summarize(long)
        assertTrue(summary.length <= 230)
        assertTrue(summary.startsWith("Première phrase courte."))
    }

    @Test
    fun `le prochain dimanche est calcule correctement`() {
        // 2026-08-26 est un mercredi ; le dimanche suivant est le 30.
        assertEquals(LocalDate.parse("2026-08-30"), AelfClient.nextSunday(LocalDate.parse("2026-08-26")))
        // Un dimanche se renvoie lui-même : la messe du jour reste la bonne cible.
        assertEquals(LocalDate.parse("2026-08-30"), AelfClient.nextSunday(LocalDate.parse("2026-08-30")))
    }
}
