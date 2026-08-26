package cm.oyenga.app.data.local

import android.content.Context
import android.util.Log
import cm.oyenga.app.data.model.OyengaDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persistance de l'état applicatif dans un unique fichier JSON.
 *
 * L'écriture passe par un fichier temporaire puis un renommage atomique : une coupure
 * en pleine sauvegarde ne peut pas laisser un fichier à moitié écrit — le cas qui,
 * sur mobile, fait perdre le programme de la messe du dimanche.
 */
class DbStore(context: Context) {

    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)
    private val mutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    suspend fun load(): OyengaDb? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!file.exists()) return@withLock null
            runCatching { json.decodeFromString<OyengaDb>(file.readText()) }
                .onFailure { Log.w(TAG, "État local illisible, retour aux données livrées", it) }
                .getOrNull()
        }
    }

    suspend fun save(db: OyengaDb) = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val tmp = File(appContext.filesDir, "$FILE_NAME.tmp")
                tmp.writeText(json.encodeToString(OyengaDb.serializer(), db))
                if (!tmp.renameTo(file)) {
                    file.writeText(tmp.readText())
                    tmp.delete()
                }
            }.onFailure { Log.e(TAG, "Sauvegarde impossible", it) }
        }
    }

    private companion object {
        const val FILE_NAME = "oyenga_db_v4.json"
        const val TAG = "OyengaDbStore"
    }
}
