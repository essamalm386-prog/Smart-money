@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.ui.text.ExperimentalTextApi::class,
)

package cm.oyenga.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import cm.oyenga.app.R

/**
 * Typographie du kit : Roboto Flex pour les titres (identité de marque),
 * Roboto pour le texte courant, Roboto Serif pour les contenus longs
 * (lectures du jour, paroles des chants).
 *
 * Les trois fichiers sont des polices VARIABLES : un seul fichier couvre
 * toutes les graisses. On règle l'axe `wght` par déclaration `Font(...)`.
 */
private fun robotoFlex(weight: Int) = Font(
    R.font.roboto_flex,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun roboto(weight: Int) = Font(
    R.font.roboto_regular,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun robotoSerif(weight: Int) = Font(
    R.font.roboto_serif,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Titres et identité — deux familles maximum par projet, comme le prescrit la charte. */
val BrandFamily = FontFamily(
    robotoFlex(300), robotoFlex(400), robotoFlex(500), robotoFlex(600), robotoFlex(700), robotoFlex(800),
)

/** Texte courant, libellés, interface. */
val PlainFamily = FontFamily(
    roboto(300), roboto(400), roboto(500), roboto(600), roboto(700), roboto(900),
)

/** Contenus longs : lectures, paroles, introduction de la messe. */
val SerifFamily = FontFamily(
    robotoSerif(400), robotoSerif(500), robotoSerif(600), robotoSerif(700),
)

private val LineHeightFill = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    family: FontFamily,
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
    lineHeightStyle = LineHeightFill,
)

/**
 * L'échelle à 15 styles de Material 3. Aucune taille intermédiaire n'est inventée :
 * si un niveau paraît trop grand ou trop petit, c'est la hiérarchie qu'il faut revoir.
 */
val OyengaTypography = Typography(
    displayLarge = style(BrandFamily, 57, 64, FontWeight.W400, -0.25),
    displayMedium = style(BrandFamily, 45, 52, FontWeight.W400),
    displaySmall = style(BrandFamily, 36, 44, FontWeight.W400),

    headlineLarge = style(BrandFamily, 32, 40, FontWeight.W600),
    headlineMedium = style(BrandFamily, 28, 36, FontWeight.W600),
    headlineSmall = style(BrandFamily, 24, 32, FontWeight.W600),

    titleLarge = style(BrandFamily, 22, 28, FontWeight.W600),
    titleMedium = style(PlainFamily, 16, 24, FontWeight.W600, 0.15),
    titleSmall = style(PlainFamily, 14, 20, FontWeight.W600, 0.1),

    bodyLarge = style(PlainFamily, 16, 24, FontWeight.W400, 0.5),
    bodyMedium = style(PlainFamily, 14, 20, FontWeight.W400, 0.25),
    bodySmall = style(PlainFamily, 12, 16, FontWeight.W400, 0.4),

    labelLarge = style(PlainFamily, 14, 20, FontWeight.W600, 0.1),
    labelMedium = style(PlainFamily, 12, 16, FontWeight.W600, 0.5),
    labelSmall = style(PlainFamily, 11, 16, FontWeight.W600, 0.5),
)

/** Styles hors échelle réservés aux contenus longs. */
object OyengaText {
    val serifBody = style(SerifFamily, 16, 27, FontWeight.W400)
    val serifBodyLarge = style(SerifFamily, 17, 29, FontWeight.W400)
    val serifTitle = style(SerifFamily, 22, 30, FontWeight.W700)
    val kicker = style(PlainFamily, 11, 16, FontWeight.W700, 1.2)
}
