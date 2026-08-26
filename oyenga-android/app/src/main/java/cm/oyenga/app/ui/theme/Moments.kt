@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cm.oyenga.app.R

/**
 * Accents des moments liturgiques.
 *
 * Ce ne sont pas des couleurs d'interface mais des couleurs de CONTENU : elles
 * catégorisent un chant (entrée, Kyrie, communion…), au même titre qu'une palette
 * de série sur un graphique. Chaque teinte reprend celle de la PWA d'origine, puis
 * est passée dans une palette tonale HCT pour produire un couple accent /
 * conteneur dont le contraste est garanti en clair comme en sombre.
 *
 * L'information n'est jamais portée par la seule couleur : chaque moment a aussi
 * une icône et un libellé.
 */
@Immutable
data class MomentStyle(
    val key: String,
    val label: String,
    val short: String,
    val iconRes: Int,
    private val lightAccent: Color,
    private val lightContainer: Color,
    private val onLightContainer: Color,
    private val darkAccent: Color,
    private val darkContainer: Color,
    private val onDarkContainer: Color,
) {
    fun accent(dark: Boolean): Color = if (dark) darkAccent else lightAccent
    fun container(dark: Boolean): Color = if (dark) darkContainer else lightContainer
    fun onContainer(dark: Boolean): Color = if (dark) onDarkContainer else onLightContainer
}

object Moments {
    val all: List<MomentStyle> = listOf(
        MomentStyle(
            key = "entree",
            label = "Entrée",
            short = "Entrée",
            iconRes = R.drawable.ic_directions_walk,
            lightAccent = Color(0xFF595D78),
            lightContainer = Color(0xFFDEE1FF),
            onLightContainer = Color(0xFF151A31),
            darkAccent = Color(0xFFC1C5E4),
            darkContainer = Color(0xFF41455F),
            onDarkContainer = Color(0xFFDEE1FF),
        ),
        MomentStyle(
            key = "kyrie",
            label = "Kyrie",
            short = "Kyrie",
            iconRes = R.drawable.ic_church,
            lightAccent = Color(0xFFA7392F),
            lightContainer = Color(0xFFFFDAD5),
            onLightContainer = Color(0xFF410001),
            darkAccent = Color(0xFFFFB4AA),
            darkContainer = Color(0xFF86211A),
            onDarkContainer = Color(0xFFFFDAD5),
        ),
        MomentStyle(
            key = "gloria",
            label = "Gloria",
            short = "Gloria",
            iconRes = R.drawable.ic_star_shine,
            lightAccent = Color(0xFF7B5900),
            lightContainer = Color(0xFFFFDEA4),
            onLightContainer = Color(0xFF261900),
            darkAccent = Color(0xFFF0BF5C),
            darkContainer = Color(0xFF5D4200),
            onDarkContainer = Color(0xFFFFDEA4),
        ),
        MomentStyle(
            key = "psaume",
            label = "Psaume",
            short = "Psaume",
            iconRes = R.drawable.ic_menu_book,
            lightAccent = Color(0xFFA23D3B),
            lightContainer = Color(0xFFFFDAD7),
            onLightContainer = Color(0xFF410005),
            darkAccent = Color(0xFFFFB3AF),
            darkContainer = Color(0xFF822526),
            onDarkContainer = Color(0xFFFFDAD7),
        ),
        MomentStyle(
            key = "acclamation",
            label = "Acclamation",
            short = "Acclam.",
            iconRes = R.drawable.ic_campaign,
            lightAccent = Color(0xFF9B450A),
            lightContainer = Color(0xFFFFDBCB),
            onLightContainer = Color(0xFF341100),
            darkAccent = Color(0xFFFFB691),
            darkContainer = Color(0xFF793100),
            onDarkContainer = Color(0xFFFFDBCB),
        ),
        MomentStyle(
            key = "credo",
            label = "Credo",
            short = "Credo",
            iconRes = R.drawable.ic_church,
            lightAccent = Color(0xFF546343),
            lightContainer = Color(0xFFD7E9C0),
            onLightContainer = Color(0xFF121F06),
            darkAccent = Color(0xFFBBCDA5),
            darkContainer = Color(0xFF3C4B2D),
            onDarkContainer = Color(0xFFD7E9C0),
        ),
        MomentStyle(
            key = "priere_universelle",
            label = "Prière universelle",
            short = "Prière univ.",
            iconRes = R.drawable.ic_local_fire_department,
            lightAccent = Color(0xFF755A2A),
            lightContainer = Color(0xFFFFDEAA),
            onLightContainer = Color(0xFF271900),
            darkAccent = Color(0xFFE5C187),
            darkContainer = Color(0xFF5B4314),
            onDarkContainer = Color(0xFFFFDEAA),
        ),
        MomentStyle(
            key = "offertoire",
            label = "Offertoire",
            short = "Offertoire",
            iconRes = R.drawable.ic_redeem,
            lightAccent = Color(0xFFA23D3B),
            lightContainer = Color(0xFFFFDAD7),
            onLightContainer = Color(0xFF410005),
            darkAccent = Color(0xFFFFB3AF),
            darkContainer = Color(0xFF822526),
            onDarkContainer = Color(0xFFFFDAD7),
        ),
        MomentStyle(
            key = "sanctus",
            label = "Sanctus",
            short = "Sanctus",
            iconRes = R.drawable.ic_star,
            lightAccent = Color(0xFF7D5800),
            lightContainer = Color(0xFFFFDEA9),
            onLightContainer = Color(0xFF271900),
            darkAccent = Color(0xFFF3BE5D),
            darkContainer = Color(0xFF5F4100),
            onDarkContainer = Color(0xFFFFDEA9),
        ),
        MomentStyle(
            key = "pater",
            label = "Pater",
            short = "Pater",
            iconRes = R.drawable.ic_church,
            lightAccent = Color(0xFF685685),
            lightContainer = Color(0xFFECDCFF),
            onLightContainer = Color(0xFF23123E),
            darkAccent = Color(0xFFD3BDF3),
            darkContainer = Color(0xFF503E6C),
            onDarkContainer = Color(0xFFECDCFF),
        ),
        MomentStyle(
            key = "agnus",
            label = "Agnus Dei",
            short = "Agnus",
            iconRes = R.drawable.ic_local_fire_department,
            lightAccent = Color(0xFF924B2A),
            lightContainer = Color(0xFFFFDBCD),
            onLightContainer = Color(0xFF360F00),
            darkAccent = Color(0xFFFFB597),
            darkContainer = Color(0xFF743415),
            onDarkContainer = Color(0xFFFFDBCD),
        ),
        MomentStyle(
            key = "communion",
            label = "Communion",
            short = "Communion",
            iconRes = R.drawable.ic_wine_bar,
            lightAccent = Color(0xFF9B450A),
            lightContainer = Color(0xFFFFDBCB),
            onLightContainer = Color(0xFF341100),
            darkAccent = Color(0xFFFFB691),
            darkContainer = Color(0xFF793100),
            onDarkContainer = Color(0xFFFFDBCB),
        ),
        MomentStyle(
            key = "meditation",
            label = "Méditation",
            short = "Méditation",
            iconRes = R.drawable.ic_music_note,
            lightAccent = Color(0xFF77583B),
            lightContainer = Color(0xFFFFDCC0),
            onLightContainer = Color(0xFF2C1602),
            darkAccent = Color(0xFFE8BF9C),
            darkContainer = Color(0xFF5D4126),
            onDarkContainer = Color(0xFFFFDCC0),
        ),
        MomentStyle(
            key = "marial",
            label = "Marial",
            short = "Marial",
            iconRes = R.drawable.ic_local_florist,
            lightAccent = Color(0xFFA7382E),
            lightContainer = Color(0xFFFFDAD5),
            onLightContainer = Color(0xFF410001),
            darkAccent = Color(0xFFFFB4AA),
            darkContainer = Color(0xFF872019),
            onDarkContainer = Color(0xFFFFDAD5),
        ),
        MomentStyle(
            key = "louange",
            label = "Louange",
            short = "Louange",
            iconRes = R.drawable.ic_music_note,
            lightAccent = Color(0xFFA7382E),
            lightContainer = Color(0xFFFFDAD5),
            onLightContainer = Color(0xFF410001),
            darkAccent = Color(0xFFFFB4AA),
            darkContainer = Color(0xFF872019),
            onDarkContainer = Color(0xFFFFDAD5),
        ),
        MomentStyle(
            key = "saint_esprit",
            label = "Saint-Esprit",
            short = "St-Esprit",
            iconRes = R.drawable.ic_local_fire_department,
            lightAccent = Color(0xFF7B5900),
            lightContainer = Color(0xFFFFDEA4),
            onLightContainer = Color(0xFF261900),
            darkAccent = Color(0xFFF0BF5C),
            darkContainer = Color(0xFF5D4200),
            onDarkContainer = Color(0xFFFFDEA4),
        ),
        MomentStyle(
            key = "envoi",
            label = "Envoi / Sortie",
            short = "Envoi",
            iconRes = R.drawable.ic_send,
            lightAccent = Color(0xFFA04111),
            lightContainer = Color(0xFFFFDBCE),
            onLightContainer = Color(0xFF360F00),
            darkAccent = Color(0xFFFFB598),
            darkContainer = Color(0xFF7E2C00),
            onDarkContainer = Color(0xFFFFDBCE),
        ),
        MomentStyle(
            key = "variete",
            label = "Variété",
            short = "Variété",
            iconRes = R.drawable.ic_music_note,
            lightAccent = Color(0xFF785839),
            lightContainer = Color(0xFFFFDCBF),
            onLightContainer = Color(0xFF2C1601),
            darkAccent = Color(0xFFE9BF99),
            darkContainer = Color(0xFF5E4124),
            onDarkContainer = Color(0xFFFFDCBF),
        ),
    )

    private val byKey: Map<String, MomentStyle> = all.associateBy { it.key }

    /** Un moment inconnu retombe sur « Variété », comme dans la PWA. */
    fun of(key: String?): MomentStyle = byKey[key] ?: byKey.getValue("variete")

    fun label(key: String?): String = of(key).label
}
