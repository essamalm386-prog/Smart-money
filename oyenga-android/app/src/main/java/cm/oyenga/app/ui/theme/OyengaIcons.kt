@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.theme

import cm.oyenga.app.R

/**
 * Catalogue d'icônes de l'application.
 *
 * Un seul style dans tout le projet : Material Symbols **Rounded**, cohérent avec les
 * rayons généreux de la charte. Les fichiers viennent du kit (SVG officiels Google,
 * Apache 2.0) convertis en VectorDrawable.
 *
 * Règle de cohérence sémantique : une icône = un sens, partout. Ce fichier est la
 * table de correspondance ; on n'écrit jamais un `R.drawable.ic_*` ailleurs.
 */
object Ic {
    // Navigation principale
    val home = R.drawable.ic_home
    val homeFill = R.drawable.ic_home_fill
    val search = R.drawable.ic_search
    val searchFill = R.drawable.ic_search_fill
    val player = R.drawable.ic_play_circle
    val playerFill = R.drawable.ic_play_circle_fill
    val community = R.drawable.ic_group
    val communityFill = R.drawable.ic_group_fill

    // Transport audio
    val play = R.drawable.ic_play_arrow
    val pause = R.drawable.ic_pause
    val next = R.drawable.ic_skip_next
    val previous = R.drawable.ic_skip_previous
    val shuffle = R.drawable.ic_shuffle
    val repeat = R.drawable.ic_repeat
    val queue = R.drawable.ic_queue_music
    val volume = R.drawable.ic_volume_up
    val equalizer = R.drawable.ic_graphic_eq

    // Actions
    val add = R.drawable.ic_add
    val addCircle = R.drawable.ic_add_circle
    val close = R.drawable.ic_close
    val check = R.drawable.ic_check
    val edit = R.drawable.ic_edit
    val delete = R.drawable.ic_delete
    val save = R.drawable.ic_save
    val share = R.drawable.ic_share
    val download = R.drawable.ic_download
    val refresh = R.drawable.ic_refresh
    val copy = R.drawable.ic_content_copy
    val openExternal = R.drawable.ic_open_in_new
    val link = R.drawable.ic_link
    val filter = R.drawable.ic_tune
    val filterAlt = R.drawable.ic_filter_alt
    val sort = R.drawable.ic_sort
    val magic = R.drawable.ic_wand_stars

    // Direction
    val chevronRight = R.drawable.ic_chevron_right
    val chevronLeft = R.drawable.ic_chevron_left
    val chevronDown = R.drawable.ic_expand_more
    val chevronUp = R.drawable.ic_expand_less
    val arrowForward = R.drawable.ic_arrow_forward
    val arrowBack = R.drawable.ic_arrow_back

    // Contenu liturgique
    val book = R.drawable.ic_menu_book
    val readings = R.drawable.ic_auto_stories
    val lyrics = R.drawable.ic_description
    val score = R.drawable.ic_library_music
    val pdf = R.drawable.ic_picture_as_pdf
    val music = R.drawable.ic_music_note
    val musicFill = R.drawable.ic_music_note_fill
    val church = R.drawable.ic_church
    val calendar = R.drawable.ic_calendar_month
    val event = R.drawable.ic_event
    val program = R.drawable.ic_list
    val translate = R.drawable.ic_translate

    // Social
    val bell = R.drawable.ic_notifications
    val bellFill = R.drawable.ic_notifications_fill
    val heart = R.drawable.ic_favorite
    val heartFill = R.drawable.ic_favorite_fill
    val thumbUp = R.drawable.ic_thumb_up
    val thumbUpFill = R.drawable.ic_thumb_up_fill
    val comment = R.drawable.ic_chat_bubble
    val send = R.drawable.ic_send
    val bookmark = R.drawable.ic_bookmark
    val bookmarkFill = R.drawable.ic_bookmark_fill
    val person = R.drawable.ic_person
    val personFill = R.drawable.ic_person_fill
    val playlistAdd = R.drawable.ic_playlist_add
    val more = R.drawable.ic_more_horiz
    val campaign = R.drawable.ic_campaign

    // États et statut
    val info = R.drawable.ic_info
    val lock = R.drawable.ic_lock
    val public = R.drawable.ic_public
    val premium = R.drawable.ic_workspace_premium
    val premiumFill = R.drawable.ic_workspace_premium_fill
    val verified = R.drawable.ic_verified
    val admin = R.drawable.ic_admin_panel_settings
    val settings = R.drawable.ic_settings
    val logout = R.drawable.ic_logout
    val offline = R.drawable.ic_cloud_off
    val visibility = R.drawable.ic_visibility
    val star = R.drawable.ic_star
    val starFill = R.drawable.ic_star_fill
    val history = R.drawable.ic_history
    val mic = R.drawable.ic_mic
}
