package io.github.halilozel1903.pokeapi.compose

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.halilozel1903.pokeapi.types.PokeType
import kotlin.math.cos
import kotlin.math.sin

/**
 * A simple drawn glyph for [type] (a flame, a drop, a leaf, a bolt, ...). Original vector shapes, no bitmaps.
 * Give it a size with [modifier]; [tint] defaults to the color that reads well on the type color.
 */
@Composable
public fun TypeIcon(
    type: PokeType,
    modifier: Modifier = Modifier,
    tint: Color = type.onColor,
    contentDescription: String? = null,
) {
    val description = contentDescription
    val a11y = if (description != null) Modifier.semantics { this.contentDescription = description } else Modifier
    Canvas(modifier.then(a11y)) {
        drawTypeGlyph(type, tint)
    }
}

/** Draws the glyph of [type] into the largest centered square of this scope. */
public fun DrawScope.drawTypeGlyph(type: PokeType, tint: Color) {
    val s = size.minDimension
    val left = (size.width - s) / 2f
    val top = (size.height - s) / 2f
    fun x(f: Float) = left + f * s
    fun y(f: Float) = top + f * s
    fun pt(fx: Float, fy: Float) = Offset(x(fx), y(fy))
    fun polygon(vararg points: Float): Path = Path().apply {
        moveTo(x(points[0]), y(points[1]))
        var i = 2
        while (i < points.size) {
            lineTo(x(points[i]), y(points[i + 1]))
            i += 2
        }
        close()
    }
    fun Path.curve(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
        cubicTo(x(x1), y(y1), x(x2), y(y2), x(x3), y(y3))

    when (type) {
        PokeType.Normal -> drawCircle(tint, radius = 0.3f * s, center = pt(0.5f, 0.5f), style = Stroke(width = 0.13f * s))
        PokeType.Fire -> drawPath(
            Path().apply {
                moveTo(x(0.5f), y(0.06f))
                curve(0.64f, 0.3f, 0.86f, 0.42f, 0.83f, 0.66f)
                curve(0.8f, 0.86f, 0.65f, 0.95f, 0.5f, 0.95f)
                curve(0.35f, 0.95f, 0.18f, 0.86f, 0.17f, 0.66f)
                curve(0.16f, 0.5f, 0.28f, 0.4f, 0.35f, 0.28f)
                curve(0.38f, 0.42f, 0.44f, 0.48f, 0.5f, 0.5f)
                curve(0.45f, 0.36f, 0.44f, 0.2f, 0.5f, 0.06f)
                close()
            },
            tint,
        )
        PokeType.Water -> drawPath(
            Path().apply {
                moveTo(x(0.5f), y(0.05f))
                curve(0.62f, 0.24f, 0.82f, 0.44f, 0.82f, 0.63f)
                curve(0.82f, 0.82f, 0.67f, 0.95f, 0.5f, 0.95f)
                curve(0.33f, 0.95f, 0.18f, 0.82f, 0.18f, 0.63f)
                curve(0.18f, 0.44f, 0.38f, 0.24f, 0.5f, 0.05f)
                close()
            },
            tint,
        )
        PokeType.Grass -> {
            drawPath(
                Path().apply {
                    moveTo(x(0.14f), y(0.86f))
                    curve(0.12f, 0.42f, 0.42f, 0.12f, 0.88f, 0.12f)
                    curve(0.9f, 0.56f, 0.6f, 0.86f, 0.14f, 0.86f)
                    close()
                },
                tint,
            )
        }
        PokeType.Electric -> drawPath(polygon(0.6f, 0.04f, 0.18f, 0.56f, 0.46f, 0.56f, 0.36f, 0.96f, 0.82f, 0.4f, 0.54f, 0.4f, 0.68f, 0.04f), tint)
        PokeType.Ice -> {
            for (i in 0 until 3) {
                val angle = Math.toRadians(90.0 + i * 60.0)
                val dx = (cos(angle) * 0.42).toFloat()
                val dy = (sin(angle) * 0.42).toFloat()
                drawLine(tint, pt(0.5f - dx, 0.5f - dy), pt(0.5f + dx, 0.5f + dy), strokeWidth = 0.11f * s, cap = StrokeCap.Round)
            }
        }
        PokeType.Fighting -> {
            drawRoundRect(tint, topLeft = pt(0.18f, 0.36f), size = Size(0.64f * s, 0.52f * s), cornerRadius = CornerRadius(0.14f * s))
            for (i in 0 until 4) {
                drawCircle(tint, radius = 0.085f * s, center = pt(0.27f + i * 0.153f, 0.3f))
            }
        }
        PokeType.Poison -> {
            drawCircle(tint, radius = 0.25f * s, center = pt(0.38f, 0.62f))
            drawCircle(tint, radius = 0.17f * s, center = pt(0.7f, 0.36f))
            drawCircle(tint, radius = 0.09f * s, center = pt(0.75f, 0.78f))
        }
        PokeType.Ground -> drawPath(polygon(0.06f, 0.86f, 0.42f, 0.2f, 0.6f, 0.52f, 0.72f, 0.36f, 0.94f, 0.86f), tint)
        PokeType.Flying -> drawPath(
            Path().apply {
                moveTo(x(0.08f), y(0.72f))
                curve(0.26f, 0.24f, 0.66f, 0.14f, 0.94f, 0.18f)
                curve(0.82f, 0.32f, 0.72f, 0.38f, 0.56f, 0.42f)
                curve(0.7f, 0.46f, 0.64f, 0.58f, 0.5f, 0.6f)
                curve(0.56f, 0.7f, 0.42f, 0.82f, 0.08f, 0.72f)
                close()
            },
            tint,
        )
        PokeType.Psychic -> {
            drawCircle(tint, radius = 0.34f * s, center = pt(0.5f, 0.5f), style = Stroke(width = 0.1f * s))
            drawCircle(tint, radius = 0.14f * s, center = pt(0.5f, 0.5f))
        }
        PokeType.Bug -> {
            drawOval(tint, topLeft = pt(0.27f, 0.34f), size = Size(0.46f * s, 0.58f * s))
            drawCircle(tint, radius = 0.12f * s, center = pt(0.5f, 0.26f))
            drawLine(tint, pt(0.44f, 0.18f), pt(0.3f, 0.05f), strokeWidth = 0.07f * s, cap = StrokeCap.Round)
            drawLine(tint, pt(0.56f, 0.18f), pt(0.7f, 0.05f), strokeWidth = 0.07f * s, cap = StrokeCap.Round)
        }
        PokeType.Rock -> drawPath(polygon(0.2f, 0.88f, 0.08f, 0.5f, 0.3f, 0.16f, 0.68f, 0.1f, 0.92f, 0.44f, 0.82f, 0.88f), tint)
        PokeType.Ghost -> drawPath(
            Path().apply {
                moveTo(x(0.2f), y(0.9f))
                lineTo(x(0.2f), y(0.46f))
                curve(0.2f, 0.1f, 0.8f, 0.1f, 0.8f, 0.46f)
                lineTo(x(0.8f), y(0.9f))
                lineTo(x(0.7f), y(0.8f))
                lineTo(x(0.6f), y(0.9f))
                lineTo(x(0.5f), y(0.8f))
                lineTo(x(0.4f), y(0.9f))
                lineTo(x(0.3f), y(0.8f))
                close()
            },
            tint,
        )
        PokeType.Dragon -> drawPath(polygon(0.5f, 0.04f, 0.86f, 0.42f, 0.62f, 0.5f, 0.5f, 0.96f, 0.38f, 0.5f, 0.14f, 0.42f), tint)
        PokeType.Dark -> {
            val moon = Path().apply { addOval(Rect(pt(0.14f, 0.14f), Size(0.72f * s, 0.72f * s))) }
            val bite = Path().apply { addOval(Rect(pt(0.34f, 0.04f), Size(0.66f * s, 0.66f * s))) }
            drawPath(Path().apply { op(moon, bite, PathOperation.Difference) }, tint)
        }
        PokeType.Steel -> {
            val nut = polygon(0.5f, 0.06f, 0.88f, 0.28f, 0.88f, 0.72f, 0.5f, 0.94f, 0.12f, 0.72f, 0.12f, 0.28f)
            val hole = Path().apply { addOval(Rect(pt(0.33f, 0.33f), Size(0.34f * s, 0.34f * s))) }
            drawPath(Path().apply { op(nut, hole, PathOperation.Difference) }, tint)
        }
        PokeType.Fairy -> drawPath(
            Path().apply {
                moveTo(x(0.5f), y(0.04f))
                curve(0.55f, 0.4f, 0.6f, 0.45f, 0.96f, 0.5f)
                curve(0.6f, 0.55f, 0.55f, 0.6f, 0.5f, 0.96f)
                curve(0.45f, 0.6f, 0.4f, 0.55f, 0.04f, 0.5f)
                curve(0.4f, 0.45f, 0.45f, 0.4f, 0.5f, 0.04f)
                close()
            },
            tint,
        )
    }
}
