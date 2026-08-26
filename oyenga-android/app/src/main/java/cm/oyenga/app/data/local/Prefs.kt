package cm.oyenga.app.data.local

import android.content.Context

/**
 * Petits réglages non structurés : dernier onglet, dernier chant, horodatage du splash.
 * Tout ce qui est du domaine métier passe par [DbStore], pas par ici.
 */
class Prefs(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("oyenga", Context.MODE_PRIVATE)

    var splashShownAt: Long
        get() = prefs.getLong(KEY_SPLASH_TS, 0L)
        set(value) = prefs.edit().putLong(KEY_SPLASH_TS, value).apply()

    var lastTab: String?
        get() = prefs.getString(KEY_TAB, null)
        set(value) = prefs.edit().putString(KEY_TAB, value).apply()

    var lastSongId: String?
        get() = prefs.getString(KEY_SONG, null)
        set(value) = prefs.edit().putString(KEY_SONG, value).apply()

    var lastPositionSeconds: Int
        get() = prefs.getInt(KEY_POSITION, 0)
        set(value) = prefs.edit().putInt(KEY_POSITION, value).apply()

    /** Le splash ne se rejoue pas à chaque retour : une demi-heure de répit, comme la PWA. */
    fun shouldShowSplash(now: Long): Boolean = now - splashShownAt > SPLASH_COOLDOWN_MS

    private companion object {
        const val KEY_SPLASH_TS = "splash_ts"
        const val KEY_TAB = "last_tab"
        const val KEY_SONG = "last_song"
        const val KEY_POSITION = "last_position"
        const val SPLASH_COOLDOWN_MS = 30 * 60 * 1000L
    }
}
