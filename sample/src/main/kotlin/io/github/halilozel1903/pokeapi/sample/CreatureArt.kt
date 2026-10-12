package io.github.halilozel1903.pokeapi.sample

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.halilozel1903.pokeapi.compose.color
import io.github.halilozel1903.pokeapi.types.PokeType
import kotlin.random.Random

/**
 * An original creature drawn from a seed, colored by its types. The offline demo and the README screenshots use
 * it instead of official artwork, which this repository does not bundle.
 */
@Composable
fun CreatureArt(seed: Int, types: List<PokeType>, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val genes = remember(seed) { CreatureGenes.from(seed) }
    val primary = (types.firstOrNull() ?: PokeType.Normal).color
    val secondary = types.getOrNull(1)?.color ?: lerp(primary, Color.White, 0.35f)
    val description = contentDescription
    val a11y = if (description != null) Modifier.semantics { this.contentDescription = description } else Modifier
    Canvas(modifier.then(a11y)) {
        drawCreature(genes, primary, secondary)
    }
}

private class CreatureGenes(
    val bodyWidth: Float,
    val bodyHeight: Float,
    val headRadius: Float,
    val headShift: Float,
    val ears: Int,
    val tail: Int,
    val spots: Boolean,
    val cheeks: Boolean,
    val mark: Boolean,
    val eyeScale: Float,
) {
    companion object {
        fun from(seed: Int): CreatureGenes {
            val random = Random(seed * 7919 + 104729)
            return CreatureGenes(
                bodyWidth = 0.44f + random.nextFloat() * 0.16f,
                bodyHeight = 0.34f + random.nextFloat() * 0.1f,
                headRadius = 0.17f + random.nextFloat() * 0.05f,
                headShift = (random.nextFloat() - 0.5f) * 0.08f,
                ears = random.nextInt(4),
                tail = random.nextInt(4),
                spots = random.nextFloat() < 0.4f,
                cheeks = random.nextFloat() < 0.6f,
                mark = random.nextFloat() < 0.35f,
                eyeScale = 0.9f + random.nextFloat() * 0.35f,
            )
        }
    }
}

private fun DrawScope.drawCreature(genes: CreatureGenes, primary: Color, secondary: Color) {
    val s = size.minDimension
    val left = (size.width - s) / 2f
    val top = (size.height - s) / 2f
    fun pt(x: Float, y: Float) = Offset(left + x * s, top + y * s)
    fun sz(w: Float, h: Float) = Size(w * s, h * s)

    val outline = lerp(primary, Color.Black, 0.5f)
    val light = lerp(primary, Color.White, 0.6f)
    val stroke = Stroke(width = 0.022f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val bodyCenterY = 0.66f
    val bw = genes.bodyWidth
    val bh = genes.bodyHeight
    val bodyTop = bodyCenterY - bh / 2f
    val r = genes.headRadius
    val hx = 0.5f + genes.headShift
    val hy = bodyTop - r * 0.15f

    // Ground shadow.
    drawOval(Color.Black.copy(alpha = 0.12f), topLeft = pt(0.5f - bw * 0.55f, bodyCenterY + bh / 2f - 0.02f), size = sz(bw * 1.1f, 0.07f))

    // Tail, behind the body.
    val tailStart = pt(0.5f + bw * 0.42f, bodyCenterY + bh * 0.1f)
    when (genes.tail) {
        1 -> {
            val path = Path().apply {
                moveTo(tailStart.x, tailStart.y)
                cubicTo(left + 0.98f * s, top + 0.7f * s, left + 0.98f * s, top + 0.36f * s, left + 0.84f * s, top + 0.38f * s)
            }
            drawPath(path, outline, style = Stroke(width = 0.075f * s, cap = StrokeCap.Round))
            drawPath(path, primary, style = Stroke(width = 0.045f * s, cap = StrokeCap.Round))
            drawCircle(secondary, radius = 0.055f * s, center = pt(0.84f, 0.38f))
            drawCircle(outline, radius = 0.055f * s, center = pt(0.84f, 0.38f), style = stroke)
        }
        2 -> {
            val path = Path().apply {
                moveTo(tailStart.x, tailStart.y)
                cubicTo(left + 0.9f * s, top + 0.66f * s, left + 0.9f * s, top + 0.5f * s, left + 0.86f * s, top + 0.44f * s)
            }
            drawPath(path, outline, style = Stroke(width = 0.07f * s, cap = StrokeCap.Round))
            drawPath(path, primary, style = Stroke(width = 0.042f * s, cap = StrokeCap.Round))
            val tip = Path().apply {
                moveTo(left + 0.86f * s, top + 0.2f * s)
                cubicTo(left + 0.94f * s, top + 0.3f * s, left + 0.96f * s, top + 0.4f * s, left + 0.88f * s, top + 0.46f * s)
                cubicTo(left + 0.8f * s, top + 0.48f * s, left + 0.76f * s, top + 0.38f * s, left + 0.8f * s, top + 0.32f * s)
                cubicTo(left + 0.82f * s, top + 0.28f * s, left + 0.85f * s, top + 0.25f * s, left + 0.86f * s, top + 0.2f * s)
                close()
            }
            drawPath(tip, secondary)
            drawPath(tip, outline, style = stroke)
        }
        3 -> {
            val path = Path().apply {
                moveTo(tailStart.x, tailStart.y)
                lineTo(left + 0.84f * s, top + 0.62f * s)
                lineTo(left + 0.78f * s, top + 0.5f * s)
                lineTo(left + 0.92f * s, top + 0.44f * s)
                lineTo(left + 0.86f * s, top + 0.3f * s)
            }
            drawPath(path, outline, style = Stroke(width = 0.075f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, secondary, style = Stroke(width = 0.045f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        else -> Unit
    }

    // Feet.
    for (side in listOf(-1f, 1f)) {
        val footCenter = 0.5f + side * bw * 0.26f
        drawOval(lerp(primary, Color.Black, 0.15f), topLeft = pt(footCenter - 0.07f, bodyCenterY + bh / 2f - 0.06f), size = sz(0.14f, 0.08f))
        drawOval(outline, topLeft = pt(footCenter - 0.07f, bodyCenterY + bh / 2f - 0.06f), size = sz(0.14f, 0.08f), style = stroke)
    }

    // Body and belly.
    drawOval(primary, topLeft = pt(0.5f - bw / 2f, bodyTop), size = sz(bw, bh))
    drawOval(light, topLeft = pt(0.5f - bw * 0.28f, bodyCenterY - bh * 0.18f), size = sz(bw * 0.56f, bh * 0.6f))
    if (genes.spots) {
        drawCircle(secondary, radius = 0.03f * s, center = pt(0.5f - bw * 0.36f, bodyCenterY - bh * 0.05f))
        drawCircle(secondary, radius = 0.022f * s, center = pt(0.5f - bw * 0.3f, bodyCenterY + bh * 0.2f))
        drawCircle(secondary, radius = 0.026f * s, center = pt(0.5f + bw * 0.36f, bodyCenterY + bh * 0.02f))
    }
    drawOval(outline, topLeft = pt(0.5f - bw / 2f, bodyTop), size = sz(bw, bh), style = stroke)

    // Ears, behind the head.
    when (genes.ears) {
        0 -> for (side in listOf(-1f, 1f)) {
            val ear = Path().apply {
                moveTo(left + (hx + side * r * 0.85f) * s, top + (hy - r * 0.25f) * s)
                lineTo(left + (hx + side * r * 1.0f) * s, top + (hy - r * 1.6f) * s)
                lineTo(left + (hx + side * r * 0.15f) * s, top + (hy - r * 0.85f) * s)
                close()
            }
            drawPath(ear, primary)
            drawPath(ear, outline, style = stroke)
            val inner = Path().apply {
                moveTo(left + (hx + side * r * 0.75f) * s, top + (hy - r * 0.55f) * s)
                lineTo(left + (hx + side * r * 0.9f) * s, top + (hy - r * 1.3f) * s)
                lineTo(left + (hx + side * r * 0.4f) * s, top + (hy - r * 0.85f) * s)
                close()
            }
            drawPath(inner, secondary)
        }
        1 -> for (side in listOf(-1f, 1f)) {
            val center = pt(hx + side * r * 0.78f, hy - r * 0.78f)
            drawCircle(primary, radius = r * 0.42f * s, center = center)
            drawCircle(outline, radius = r * 0.42f * s, center = center, style = stroke)
            drawCircle(light, radius = r * 0.2f * s, center = center)
        }
        2 -> for (side in listOf(-1f, 1f)) {
            val topLeft = pt(hx + side * r * 0.5f - r * 0.24f, hy - r * 2.0f)
            rotate(degrees = side * 14f, pivot = pt(hx + side * r * 0.5f, hy - r * 0.6f)) {
                drawOval(primary, topLeft = topLeft, size = sz(r * 0.48f, r * 1.5f))
                drawOval(outline, topLeft = topLeft, size = sz(r * 0.48f, r * 1.5f), style = stroke)
                drawOval(secondary, topLeft = pt(hx + side * r * 0.5f - r * 0.1f, hy - r * 1.75f), size = sz(r * 0.2f, r * 0.9f))
            }
        }
        else -> {
            val horn = Path().apply {
                moveTo(left + (hx - r * 0.3f) * s, top + (hy - r * 0.8f) * s)
                lineTo(left + (hx + r * 0.05f) * s, top + (hy - r * 1.75f) * s)
                lineTo(left + (hx + r * 0.32f) * s, top + (hy - r * 0.8f) * s)
                close()
            }
            drawPath(horn, secondary)
            drawPath(horn, outline, style = stroke)
        }
    }

    // Head.
    drawCircle(primary, radius = r * s, center = pt(hx, hy))
    drawCircle(outline, radius = r * s, center = pt(hx, hy), style = stroke)
    if (genes.mark) {
        val mark = Path().apply {
            moveTo(left + hx * s, top + (hy - r * 0.75f) * s)
            lineTo(left + (hx + r * 0.16f) * s, top + (hy - r * 0.5f) * s)
            lineTo(left + hx * s, top + (hy - r * 0.25f) * s)
            lineTo(left + (hx - r * 0.16f) * s, top + (hy - r * 0.5f) * s)
            close()
        }
        drawPath(mark, secondary)
    }

    // Face.
    val eyeW = r * 0.32f * genes.eyeScale
    val eyeH = r * 0.4f * genes.eyeScale
    for (side in listOf(-1f, 1f)) {
        val ex = hx + side * r * 0.38f
        val ey = hy + r * 0.02f
        drawOval(Color.White, topLeft = pt(ex - eyeW / 2f, ey - eyeH / 2f), size = sz(eyeW, eyeH))
        drawOval(outline, topLeft = pt(ex - eyeW / 2f, ey - eyeH / 2f), size = sz(eyeW, eyeH), style = Stroke(width = 0.012f * s))
        drawCircle(Color(0xFF1F1A24), radius = eyeW * 0.36f * s, center = pt(ex - side * eyeW * 0.08f, ey + eyeH * 0.1f))
        drawCircle(Color.White, radius = eyeW * 0.12f * s, center = pt(ex - side * eyeW * 0.16f, ey - eyeH * 0.06f))
        if (genes.cheeks) {
            drawCircle(Color(0xFFFF7E95).copy(alpha = 0.55f), radius = r * 0.14f * s, center = pt(hx + side * r * 0.64f, hy + r * 0.4f))
        }
    }
    drawArc(
        outline,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = pt(hx - r * 0.14f, hy + r * 0.2f),
        size = sz(r * 0.28f, r * 0.22f),
        style = Stroke(width = 0.016f * s, cap = StrokeCap.Round),
    )
}
