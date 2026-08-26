package cm.oyenga.app.data.remote

import android.util.Log
import cm.oyenga.app.data.liturgy.IntroductionBuilder
import cm.oyenga.app.data.model.AiConfig
import cm.oyenga.app.data.model.ReadingDay
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Rédaction assistée de l'introduction aux lectures.
 *
 * Facultatif : l'application produit déjà l'introduction hors ligne
 * ([IntroductionBuilder]). Ceci sert au responsable qui veut un texte plus développé,
 * avec **sa propre** clé API — saisie dans l'espace administration, stockée sur son
 * téléphone, jamais transmise ailleurs qu'à Anthropic.
 */
class AnthropicIntroWriter {

    suspend fun write(day: ReadingDay, config: AiConfig): Result<String> = withContext(Dispatchers.IO) {
        if (config.key.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Aucune clé API enregistrée"))
        }
        runCatching {
            val client = AnthropicOkHttpClient.builder()
                .apiKey(config.key.trim())
                .build()

            val params = MessageCreateParams.builder()
                .model(config.model.ifBlank { DEFAULT_MODEL })
                .maxTokens(4000L)
                .addUserMessage(prompt(day))
                .build()

            val message = client.messages().create(params)

            // Un refus est une réponse valide côté HTTP : on le traite, on ne le confond
            // pas avec une panne, et on laisse l'appelant retomber sur le texte local.
            val refused = message.stopReason()
                .map { it.toString().contains("refusal", ignoreCase = true) }
                .orElse(false)
            if (refused) error("La demande n'a pas été traitée par le modèle")

            val text = message.content()
                .mapNotNull { block -> block.text().map { it.text() }.orElse(null) }
                .joinToString("\n")
                .trim()

            if (text.isEmpty()) error("Réponse vide")
            text
        }.onFailure { Log.w(TAG, "Rédaction assistée indisponible", it) }
    }

    private fun prompt(day: ReadingDay): String {
        val readings = day.readings.joinToString("\n\n") { reading ->
            buildString {
                append(reading.label).append(" — ").append(reading.source)
                append(" (").append(reading.ref).append(")\n")
                val quote = IntroductionBuilder.cleanQuote(reading.quote)
                if (quote.isNotBlank()) append("Thème : ").append(quote).append("\n")
                append("Texte : ")
                append(reading.text.replace(Regex("\\s+"), " ").take(1400))
            }
        }
        val annee = if (day.annee.isNotBlank()) {
            ", " + day.annee.replace(Regex("^Année", RegexOption.IGNORE_CASE), "année")
        } else {
            ""
        }

        return """
            Tu es un diacre catholique qui prépare une INTRODUCTION GÉNÉRALE aux lectures de la
            messe, à proclamer au micro devant l'assemblée avant la liturgie de la Parole.

            Rédige cette introduction EN FRANÇAIS, dans un style solennel, chaleureux et fluide :
            un texte suivi, cohérent, avec des mots de liaison (« Tout d'abord », « Ensuite »,
            « Enfin »). PAS de liste à puces, PAS de titres, PAS de gras — seulement le texte à
            lire, en paragraphes.

            Structure OBLIGATOIRE, dans cet ordre :
            1) Salutation exacte : « Bien-aimés de Dieu, loué soit Jésus-Christ. »
            2) Contexte : « Nous célébrons aujourd'hui le ${day.title}$annee. »
            3) Leçon générale : dégage en une ou deux phrases le fil conducteur qui unit les
               lectures du jour.
            4) Première lecture : « Dans la première lecture, tirée de [livre + référence], … »
               puis EXPLIQUE en 2 à 3 phrases le message transmis.
            5) Deuxième lecture (seulement si elle est présente ci-dessous) : de même, avec une
               transition.
            6) Évangile : « Enfin, dans l'Évangile selon [évangéliste] ([référence]), … » puis
               explique le message.
            7) Prière finale : « Au cours de cette célébration eucharistique, demandons au
               Seigneur … » — formule ce qu'il faut changer en nous ou la grâce à demander, en
               lien direct avec les textes. Termine par « Amen. ».

            Explique VRAIMENT le sens de chaque lecture à partir du contenu fourni (ne te contente
            pas de citer le thème). Reste fidèle à la doctrine catholique.

            LECTURES DU JOUR :
            $readings
        """.trimIndent()
    }

    companion object {
        private const val TAG = "AnthropicIntroWriter"
        const val DEFAULT_MODEL = "claude-opus-5"

        /** Modèles proposés dans l'écran d'administration. */
        val MODELS = listOf(
            "claude-opus-5" to "Claude Opus 5 — le plus capable",
            "claude-sonnet-5" to "Claude Sonnet 5 — équilibré",
            "claude-haiku-4-5" to "Claude Haiku 4.5 — rapide et économe",
        )
    }
}
