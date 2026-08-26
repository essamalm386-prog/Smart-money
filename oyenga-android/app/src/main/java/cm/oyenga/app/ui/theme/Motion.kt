@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween

/**
 * Durées et courbes Material 3 Expressive. Plus lentes que Material classique :
 * c'est délibéré, c'est ce qui produit la sensation fluide plutôt que sèche.
 * On n'anime jamais en `linear`, sauf rotation continue.
 */
object Motion {
    const val SHORT = 200
    const val MEDIUM = 350
    const val LONG = 500
    const val EXTRA_LONG = 800

    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> enter(durationMillis: Int = MEDIUM) =
        tween<T>(durationMillis = durationMillis, easing = emphasizedDecelerate)

    fun <T> exit(durationMillis: Int = SHORT) =
        tween<T>(durationMillis = durationMillis, easing = emphasizedAccelerate)

    fun <T> standardTween(durationMillis: Int = SHORT) =
        tween<T>(durationMillis = durationMillis, easing = standard)
}
