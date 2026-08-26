package cm.oyenga.app.data.remote

import android.util.Log
import cm.oyenga.app.data.model.ReadingDay
import cm.oyenga.app.data.model.ReadingText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Lecture des textes de la messe chez l'AELF (Association Épiscopale Liturgique
 * pour les pays Francophones).
 *
 * L'API renvoie du HTML dans les champs de texte ; on le réduit en texte brut lisible
 * avant de l'afficher. Aucune clé n'est nécessaire, l'appel est public.
 */
class AelfClient(
    private val http: OkHttpClient = defaultClient(),
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Cherche la prochaine célébration à partir d'aujourd'hui : le prochain DIMANCHE,
     * ou une SOLENNITÉ en semaine si elle tombe avant. Les simples fêtes et mémoires
     * ne sont pas retenues — une chorale prépare les dimanches et les solennités.
     */
    suspend fun nextCelebration(from: LocalDate = LocalDate.now()): Result<ReadingDay?> =
        withContext(Dispatchers.IO) {
            var reachedNetwork = false
            for (offset in 0 until LOOKAHEAD_DAYS) {
                val day = fetch(from.plusDays(offset.toLong())).getOrNull() ?: continue
                reachedNetwork = true
                val isSolemnity = day.degre.contains("solenn", ignoreCase = true)
                if (day.isSunday || isSolemnity) return@withContext Result.success(day)
            }
            if (reachedNetwork) {
                Result.success(null)
            } else {
                Result.failure(IllegalStateException("AELF injoignable"))
            }
        }

    suspend fun fetch(date: LocalDate): Result<ReadingDay> = withContext(Dispatchers.IO) {
        val iso = date.toString()
        runCatching {
            val request = Request.Builder()
                .url("https://api.aelf.org/v1/messes/$iso/france")
                .header("Accept", "application/json")
                .build()

            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body?.string().orEmpty()
                parse(iso, date, json.parseToJsonElement(body).jsonObject)
                    ?: error("aucune lecture exploitable")
            }
        }.onFailure { Log.w(TAG, "Lectures AELF indisponibles pour $iso", it) }
    }

    private fun parse(iso: String, date: LocalDate, root: JsonObject): ReadingDay? {
        val info = root["informations"]?.jsonObject
        val messe = root["messes"]?.jsonArray?.firstOrNull()?.jsonObject ?: return null
        val lectures = messe["lectures"]?.jsonArray ?: return null

        fun str(obj: JsonObject, key: String): String =
            obj[key]?.jsonPrimitive?.contentOrNull.orEmpty()

        val byType = lectures.associate { element ->
            val obj = element.jsonObject
            str(obj, "type") to obj
        }

        val readings = ORDER.mapNotNull { (type, label) ->
            val obj = byType[type] ?: return@mapNotNull null
            val text = stripHtml(str(obj, "contenu").ifBlank { str(obj, "texte") })
            ReadingText(
                label = label,
                ref = str(obj, "ref"),
                source = str(obj, "titre"),
                quote = stripHtml(str(obj, "intro_lue")).trim(),
                summary = summarize(text),
                text = text,
            )
        }
        if (readings.isEmpty()) return null

        val anneeRaw = info?.let { str(it, "annee") }.orEmpty()
        val annee = if (anneeRaw.isNotBlank()) {
            "Année " + anneeRaw.replace(Regex("^Année\\s*", RegexOption.IGNORE_CASE), "").trim()
        } else {
            ""
        }

        return ReadingDay(
            id = "live-$iso",
            date = iso,
            degre = info?.let { str(it, "degre") }.orEmpty(),
            isSunday = date.dayOfWeek == DayOfWeek.SUNDAY,
            saison = info?.let { str(it, "temps_liturgique") }.orEmpty(),
            annee = annee,
            title = info?.let { str(it, "jour_liturgique_nom") }.orEmpty().ifBlank { "Messe du jour" },
            phrase = readings.firstOrNull { it.label == ReadingDay.EVANGILE }?.quote
                ?: readings.first().quote,
            readings = readings,
            live = true,
        )
    }

    companion object {
        private const val TAG = "AelfClient"
        private const val LOOKAHEAD_DAYS = 8

        private val ORDER = listOf(
            "lecture_1" to ReadingDay.PREMIERE,
            "psaume" to ReadingDay.PSAUME,
            "lecture_2" to ReadingDay.DEUXIEME,
            "evangile" to ReadingDay.EVANGILE,
        )

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()

        /** Le prochain dimanche à partir d'une date — le repère par défaut hors ligne. */
        fun nextSunday(from: LocalDate = LocalDate.now()): LocalDate {
            val shift = (DayOfWeek.SUNDAY.value - from.dayOfWeek.value + 7) % 7
            return from.plusDays(shift.toLong())
        }

        internal fun stripHtml(html: String): String = html
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&laquo;", "«")
            .replace("&raquo;", "»")
            .replace("&rsquo;", "’")
            .replace("&eacute;", "é")
            .replace("&egrave;", "è")
            .replace("&agrave;", "à")
            .replace("&amp;", "&")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()

        /** Résumé d'accroche : les premières phrases entières, dans la limite de 230 signes. */
        internal fun summarize(text: String): String {
            val flat = text.replace(Regex("\\s+"), " ").trim()
            if (flat.isEmpty()) return ""
            val builder = StringBuilder()
            for (sentence in flat.split(Regex("(?<=[.!?»])\\s+"))) {
                if (builder.length + sentence.length + 1 > 230) break
                if (builder.isNotEmpty()) builder.append(' ')
                builder.append(sentence)
            }
            return builder.toString().ifBlank { flat.take(230) }
        }
    }
}
