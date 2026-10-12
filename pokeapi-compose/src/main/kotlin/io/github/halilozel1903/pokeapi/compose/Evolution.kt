package io.github.halilozel1903.pokeapi.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.evolution.EvolutionStage
import io.github.halilozel1903.pokeapi.evolution.flatten
import io.github.halilozel1903.pokeapi.evolution.isLinear
import io.github.halilozel1903.pokeapi.evolution.levels
import io.github.halilozel1903.pokeapi.model.ChainLink

/**
 * Shows any evolution chain: a [EvolutionRow] for short linear families (Bulbasaur, Ivysaur, Venusaur) and an
 * [EvolutionTree] for branching or long ones (Eevee). [node] draws one species; the conditions are drawn for you.
 */
@Composable
public fun EvolutionChainView(
    chain: ChainLink,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    node: @Composable (EvolutionStage) -> Unit,
) {
    val stages = chain.flatten()
    if (chain.isLinear && stages.size <= 3) {
        EvolutionRow(stages, modifier, node = node)
    } else {
        EvolutionTree(chain, modifier, columns = columns, node = node)
    }
}

/** Species side by side with an arrow and the condition between each pair. */
@Composable
public fun EvolutionRow(
    stages: List<EvolutionStage>,
    modifier: Modifier = Modifier,
    arrowWidth: Dp = 64.dp,
    node: @Composable (EvolutionStage) -> Unit,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        stages.forEachIndexed { index, stage ->
            if (index > 0) {
                EvolutionArrow(stage.condition, Modifier.width(arrowWidth))
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { node(stage) }
        }
    }
}

/**
 * The chain level by level, top to bottom: the base species, an arrow, then its evolutions in rows of [columns],
 * each with its condition underneath.
 */
@Composable
public fun EvolutionTree(
    chain: ChainLink,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    node: @Composable (EvolutionStage) -> Unit,
) {
    val levels = chain.levels()
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        levels.forEachIndexed { depth, level ->
            if (depth > 0) {
                DownArrow(Modifier.padding(vertical = 6.dp).size(width = 24.dp, height = 28.dp))
            }
            val perRow = if (level.size == 1) 1 else columns.coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in level.chunked(perRow)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (stage in row) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                node(stage)
                                stage.condition?.let { ConditionLabel(it, Modifier.padding(top = 6.dp)) }
                            }
                        }
                        // Keep the last row's cells the same width as the others.
                        repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

/** A right arrow with an optional condition label above it, as used by [EvolutionRow]. */
@Composable
public fun EvolutionArrow(label: String?, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outline) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
        }
        Canvas(Modifier.fillMaxWidth().height(12.dp)) {
            val y = size.height / 2
            val stroke = 2.dp.toPx()
            drawLine(color, Offset(4.dp.toPx(), y), Offset(size.width - 4.dp.toPx(), y), strokeWidth = stroke, cap = StrokeCap.Round)
            val head = 6.dp.toPx()
            val tip = Offset(size.width - 4.dp.toPx(), y)
            drawLine(color, tip, Offset(tip.x - head, y - head), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, tip, Offset(tip.x - head, y + head), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun DownArrow(modifier: Modifier) {
    val color = MaterialTheme.colorScheme.outline
    Canvas(modifier) {
        val x = size.width / 2
        val stroke = 2.dp.toPx()
        drawLine(color, Offset(x, 2.dp.toPx()), Offset(x, size.height - 2.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        val head = Path().apply {
            moveTo(x - 6.dp.toPx(), size.height - 8.dp.toPx())
            lineTo(x, size.height - 2.dp.toPx())
            lineTo(x + 6.dp.toPx(), size.height - 8.dp.toPx())
        }
        drawPath(head, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

/** A small rounded label for an evolution condition ("Use Water Stone"). */
@Composable
public fun ConditionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
