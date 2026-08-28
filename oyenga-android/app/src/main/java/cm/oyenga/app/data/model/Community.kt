package cm.oyenga.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    val author: String,
    val init: String = "",
    val text: String = "",
    val timeAgo: String = "À l'instant",
)

@Serializable
data class Post(
    val id: String,
    val communityId: String,
    val author: String,
    val authorInit: String = "",
    val timeAgo: String = "À l'instant",
    val ts: Long = 0,
    val text: String = "",
    /** Illustration décorative : `"choir"` ou vide. Dessinée dans l'application, jamais chargée du réseau. */
    val image: String = "",
    /**
     * Vidéo de la publication. Vide tant qu'aucune n'est rattachée : le fil affiche
     * alors une carte de remplacement plutôt qu'un écran noir.
     */
    val videoUrl: String = "",
    /**
     * Le chant du répertoire associé à la publication — l'équivalent du « son » d'un
     * réseau social. Le toucher lance la lecture dans le lecteur de l'application.
     */
    val songId: String = "",
    val likes: Int = 0,
    val liked: Boolean = false,
    val commentsList: List<Comment> = emptyList(),
)

@Serializable
data class Community(
    val id: String,
    val name: String,
    val type: String = "",
    val members: Int = 0,
    val desc: String = "",
    /** `"public"` ou `"private"`. Un groupe privé ne se rejoint pas d'un simple geste. */
    val visibility: String = "public",
    val joined: Boolean = false,
    val role: String = "",
) {
    val isPrivate: Boolean get() = visibility == "private"
}

@Serializable
data class Ad(
    val id: String,
    val title: String,
    val text: String = "",
    val cta: String = "Découvrir",
    val url: String = "",
    /** Les annonces vivent dans l'espace communauté, jamais dans la liturgie. */
    val placement: String = "community",
)

@Serializable
data class Account(
    val id: String,
    val name: String,
    val full: String = "",
    val initials: String = "?",
    val premium: Boolean = false,
    val isAdmin: Boolean = false,
    val subscriptions: List<String> = emptyList(),
    val likes: List<String> = emptyList(),
) {
    val displayName: String get() = full.ifBlank { name }
}

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String> = emptyList(),
)

/** Réglages de la rédaction assistée : la clé appartient au responsable, jamais à l'application. */
@Serializable
data class AiConfig(
    val key: String = "",
    val model: String = "claude-sonnet-4-5",
)
