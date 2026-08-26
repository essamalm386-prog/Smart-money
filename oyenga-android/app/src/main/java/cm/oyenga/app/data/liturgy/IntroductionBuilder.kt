package cm.oyenga.app.data.liturgy

import cm.oyenga.app.data.model.ReadingDay
import cm.oyenga.app.data.model.ReadingText

/**
 * Rédige l'introduction générale aux lectures, celle que le diacre proclame au micro
 * avant la liturgie de la Parole.
 *
 * Le texte est construit localement à partir des lectures du jour : salutation,
 * contexte, fil conducteur, une explication par lecture, prière finale. Aucun réseau,
 * aucune clé — ce qui compte pour un office : le texte est là même sans connexion.
 * L'IA (voir `AnthropicIntroWriter`) ne fait qu'améliorer ce socle quand le responsable
 * le demande.
 */
object IntroductionBuilder {

    fun build(day: ReadingDay?, override: String? = null): String {
        if (!override.isNullOrBlank()) return override.trim()
        if (day == null) return ""
        if (day.intro.isNotBlank()) return day.intro.trim()
        if (day.readings.isEmpty()) return ""

        val first = day.reading(ReadingDay.PREMIERE)
        val second = day.reading(ReadingDay.DEUXIEME)
        val gospel = day.reading(ReadingDay.EVANGILE)

        val title = day.title.ifBlank { "ce jour du Seigneur" }
        val annee = if (day.annee.isNotBlank()) {
            ", " + day.annee.replace(Regex("^Année", RegexOption.IGNORE_CASE), "année")
        } else {
            ""
        }
        val theme = cleanQuote(gospel?.quote).ifBlank { cleanQuote(first?.quote) }

        return buildString {
            append("Bien-aimés de Dieu, loué soit Jésus-Christ.\n\n")
            append("Nous célébrons aujourd'hui le ").append(title).append(annee).append(". ")
            append(
                "Les textes soumis à notre méditation nous invitent à accueillir la Parole de " +
                    "Dieu et à laisser le Christ transformer notre vie",
            )
            if (theme.isNotBlank()) {
                append(", en particulier à travers cet appel qui traverse toute la liturgie : « ")
                append(theme).append(" ».")
            } else {
                append(".")
            }
            append("\n\n")

            if (first != null) {
                append("Tout d'abord, dans la première lecture, tirée ")
                append(sourceDe(first.source, first.ref)).append(" (").append(first.ref).append("), ")
                append(frame(first, "le Seigneur nous adresse cette parole", "il nous est rapporté que"))
                append(". ")
            }
            if (second != null) {
                append("Ensuite, dans la deuxième lecture, tirée ")
                append(sourceDe(second.source, second.ref)).append(" (").append(second.ref).append("), ")
                append(frame(second, "l'Apôtre nous exhorte en ces termes", "l'Apôtre nous rappelle que"))
                append(". ")
            }
            if (first != null || second != null) append("\n\n")

            if (gospel != null) {
                append("Enfin, dans l'Évangile ")
                append(sourceDe(gospel.source, gospel.ref)).append(" (").append(gospel.ref).append("), ")
                append(frame(gospel, "le Seigneur lui-même nous révèle", "le Seigneur nous montre que"))
                append(".\n\n")
            }

            append("Au cours de cette célébration eucharistique, demandons au Seigneur la grâce ")
            append("de convertir nos cœurs, afin de vivre pleinement ce que sa Parole nous enseigne ")
            append("aujourd'hui")
            if (theme.isNotBlank()) append(" — « ").append(theme).append(" » —")
            append(" et d'en témoigner dans notre vie de chaque jour. Amen.")
        }
    }

    /** Met en forme le message d'une lecture : sa citation si elle existe, sinon son résumé. */
    private fun frame(reading: ReadingText, quoteLead: String, plainLead: String): String {
        val quote = cleanQuote(reading.quote)
        if (quote.isNotBlank()) return "$quoteLead : « $quote »"
        val sentence = firstSentence(reading.summary)
        return if (sentence.isNotBlank()) "$plainLead ${lowerFirst(sentence)}" else quoteLead
    }

    /** « Lecture du livre de la Sagesse » → « du livre de la Sagesse ». */
    private fun sourceDe(source: String?, ref: String?): String {
        val s = (source ?: "").trim()
        if (s.isBlank()) return "de $ref"
        val cleaned = s
            .replace(Regex("^Lecture\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^Évangile de Jésus Christ\\s+", RegexOption.IGNORE_CASE), "")
            .trim()
        return if (cleaned.startsWith("de ", ignoreCase = true) ||
            cleaned.startsWith("du ", ignoreCase = true) ||
            cleaned.startsWith("des ", ignoreCase = true) ||
            cleaned.startsWith("selon ", ignoreCase = true)
        ) {
            cleaned
        } else {
            "de $cleaned"
        }
    }

    internal fun cleanQuote(quote: String?): String =
        (quote ?: "").trim().trim('«', '»', '"', '\'', ' ').trim()

    private fun firstSentence(text: String?): String {
        val t = (text ?: "").replace(Regex("\\s+"), " ").trim()
        if (t.isEmpty()) return ""
        val end = Regex("[.!?»]").find(t)?.range?.last ?: return t
        return t.substring(0, end + 1)
    }

    private fun lowerFirst(s: String): String =
        if (s.isEmpty()) s else s[0].lowercaseChar() + s.substring(1)
}
