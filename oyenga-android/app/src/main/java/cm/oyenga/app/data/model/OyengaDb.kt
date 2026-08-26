package cm.oyenga.app.data.model

import kotlinx.serialization.Serializable

/**
 * L'état applicatif persisté.
 *
 * Le corpus de chants livré dans `assets/` n'en fait pas partie : on ne stocke que
 * ce que l'utilisateur a produit ou modifié. C'est ce qui permet de mettre à jour
 * le répertoire dans une nouvelle version sans écraser le travail de la chorale.
 */
@Serializable
data class OyengaDb(
    val overrides: Map<String, SongOverride> = emptyMap(),
    val addedSongs: List<Song> = emptyList(),
    val programs: List<Program> = emptyList(),
    val readings: List<ReadingDay> = emptyList(),
    val communities: List<Community> = emptyList(),
    val posts: List<Post> = emptyList(),
    val ads: List<Ad> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val user: Account = Account(id = "u-guest", name = "Invité", full = "Invité", initials = "IN"),
    val ai: AiConfig = AiConfig(),
    /** Introductions rédigées à la main ou par l'IA, indexées par date ISO. */
    val introOverrides: Map<String, String> = emptyMap(),
) {
    fun community(id: String): Community? = communities.firstOrNull { it.id == id }

    fun post(id: String): Post? = posts.firstOrNull { it.id == id }

    val program: Program? get() = programs.firstOrNull()

    /** Le compte courant est aussi stocké dans `accounts` : on garde les deux alignés. */
    fun withUser(updated: Account): OyengaDb = copy(
        user = updated,
        accounts = accounts.map { if (it.id == updated.id) updated else it },
    )
}
