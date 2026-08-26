package cm.oyenga.app

import cm.oyenga.app.data.Seed
import cm.oyenga.app.data.model.MASS_PARTS
import cm.oyenga.app.data.model.OyengaDb
import cm.oyenga.app.data.model.Song
import cm.oyenga.app.data.model.SongOverride
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }

/**
 * Le corpus livré, lu depuis les ressources du module.
 *
 * Gradle exécute les tests unitaires depuis le répertoire du module ; le second chemin
 * couvre les exécutions lancées depuis la racine du projet.
 */
private fun loadCorpus(): List<Song> {
    val candidates = listOf("src/main/assets/songs.json", "app/src/main/assets/songs.json")
    val file = candidates.map(::File).firstOrNull { it.exists() }
        ?: error("songs.json introuvable depuis ${File(".").absolutePath}")
    return json.decodeFromString(file.readText())
}

/** Le corpus livré est une donnée de production : s'il ne se lit plus, l'application est vide. */
class CorpusTest {

    private val songs = loadCorpus()

    @Test
    fun `le corpus se lit entierement`() {
        assertEquals(254, songs.size)
        assertTrue(songs.all { it.id.isNotBlank() && it.title.isNotBlank() })
    }

    /**
     * Les écrans indexent leurs listes par identifiant : un doublon fait planter la
     * liste, il ne la dégrade pas. Ce test garde la porte fermée.
     */
    @Test
    fun `les identifiants sont uniques`() {
        val doublons = songs.map { it.id }.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertTrue("identifiants en double : ${doublons.keys}", doublons.isEmpty())
    }

    @Test
    fun `chaque chant a une duree exploitable par le lecteur`() {
        assertTrue(songs.all { it.duration > 0 })
    }
}

class SongOverrideTest {

    private val base = Song(id = "X", title = "Titre", moment = "entree", credit = "Auteur")

    @Test
    fun `seuls les champs modifies sont conserves`() {
        val diff = SongOverride.diff(base, base.copy(title = "Nouveau titre"))
        assertEquals("Nouveau titre", diff.title)
        assertEquals(null, diff.credit)
        assertEquals(null, diff.moment)
    }

    @Test
    fun `un chant inchange ne produit aucune modification`() {
        assertTrue(SongOverride.diff(base, base).isEmpty)
    }

    @Test
    fun `appliquer une modification n'ecrase pas les autres champs`() {
        val applied = SongOverride(title = "Autre").applyTo(base)
        assertEquals("Autre", applied.title)
        assertEquals("Auteur", applied.credit)
        assertEquals("entree", applied.moment)
    }
}

class SeedTest {

    private val db = Seed.build(loadCorpus())

    @Test
    fun `le programme de demonstration couvre les parties qui ont un repertoire`() {
        val program = db.program
        assertNotNull(program)
        assertTrue(program!!.items.isNotEmpty())
        program.items.forEach { (key, item) ->
            assertTrue("partie inconnue : $key", MASS_PARTS.any { it.key == key })
            assertNotNull("aucun chant recommandé pour $key", item.recommended)
            assertTrue(item.list.contains(item.recommended))
        }
    }

    @Test
    fun `l'etiquetage initial alimente l'algorithme de suggestion`() {
        val tagged = db.overrides.values.count { it.temps?.isNotEmpty() == true }
        assertTrue("aucun chant étiqueté : la suggestion n'aurait rien à proposer", tagged > 20)
    }

    @Test
    fun `l'utilisateur par defaut fait partie des comptes`() {
        assertTrue(db.accounts.any { it.id == db.user.id })
        assertTrue(db.user.isAdmin)
    }

    @Test
    fun `l'etat se serialise et se relit sans perte`() {
        val encoded = json.encodeToString(OyengaDb.serializer(), db)
        val decoded = json.decodeFromString(OyengaDb.serializer(), encoded)
        assertEquals(db.programs, decoded.programs)
        assertEquals(db.posts, decoded.posts)
        assertEquals(db.overrides, decoded.overrides)
        assertEquals(db.user, decoded.user)
    }

    @Test
    fun `aucune annonce n'est placee hors de l'espace communaute`() {
        assertFalse(db.ads.any { it.placement != "community" })
    }
}
