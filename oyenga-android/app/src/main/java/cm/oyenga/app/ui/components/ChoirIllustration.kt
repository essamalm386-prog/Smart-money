@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package cm.oyenga.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import cm.oyenga.app.ui.theme.OyengaShapes
import cm.oyenga.app.ui.theme.Space

/**
 * Illustration de chorale, dessinée à la main.
 *
 * Reprise du visuel de la PWA : aucune image n'est téléchargée, l'illustration
 * s'affiche donc instantanément et hors ligne, et pèse zéro octet dans l'APK.
 */
@Composable
fun ChoirIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(OyengaShapes.lg),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(Modifier.fillMaxWidth().height(200.dp)) {
            val w = size.width
            val h = size.height
            val sky = Color(0xFFA6432E)
            val ground = Color(0xFF8B2C2C)
            val moon = Color(0xFFF4D9A6)
            val faces = listOf(
                Color(0xFFF0CFA0), Color(0xFFE8BE8C), Color(0xFFF4D9A6),
                Color(0xFFD9AE82), Color(0xFFF0CFA0), Color(0xFFE8BE8C),
            )
            val robes = listOf(Color(0xFFEDE7DA), Color(0xFFF2EEE6))

            drawRect(ground, size = Size(w, h))
            drawRect(sky, size = Size(w, h * 0.625f))
            drawCircle(moon, radius = w * 0.085f, center = Offset(w / 2f, h * 0.25f))

            // Étoile décorative, à gauche du soleil.
            val star = Path().apply {
                val cx = w * 0.36f
                val cy = h * 0.2f
                val r = w * 0.03f
                moveTo(cx, cy - r)
                lineTo(cx + r * 0.35f, cy - r * 0.3f)
                lineTo(cx + r, cy - r * 0.15f)
                lineTo(cx + r * 0.5f, cy + r * 0.35f)
                lineTo(cx + r * 0.65f, cy + r)
                lineTo(cx, cy + r * 0.6f)
                lineTo(cx - r * 0.65f, cy + r)
                lineTo(cx - r * 0.5f, cy + r * 0.35f)
                lineTo(cx - r, cy - r * 0.15f)
                lineTo(cx - r * 0.35f, cy - r * 0.3f)
                close()
            }
            drawPath(star, moon.copy(alpha = 0.9f))

            // Les choristes : une tête et une aube, régulièrement espacées.
            val positions = listOf(0.15f, 0.30f, 0.45f, 0.60f, 0.75f, 0.87f)
            positions.forEachIndexed { index, ratio ->
                val cx = w * ratio
                val headRadius = w * 0.065f
                drawCircle(faces[index % faces.size], headRadius, Offset(cx, h * 0.625f))
                drawRoundRect(
                    color = robes[index % robes.size],
                    topLeft = Offset(cx - headRadius * 0.92f, h * 0.72f),
                    size = Size(headRadius * 1.84f, h * 0.29f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                )
            }
        }
        Text(
            "Chorale OYENGA",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = Space.s2),
        )
    }
}
