package io.github.halilozel1903.pokeapi.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.model.BaseStat
import io.github.halilozel1903.pokeapi.model.PokemonStat

/** A color for a base stat value: red for low, through yellow and green, to teal for very high. */
public fun statColor(value: Int): Color = when {
    value < 50 -> Color(0xFFF2554B)
    value < 70 -> Color(0xFFFF8A3D)
    value < 90 -> Color(0xFFF5C944)
    value < 110 -> Color(0xFF9BD045)
    value < 130 -> Color(0xFF3CC46A)
    else -> Color(0xFF1FB5B0)
}

/**
 * One labelled stat bar: "Sp. Atk  109  ▇▇▇▇▇▇▇──". The bar fills from zero when it first appears.
 *
 * @param maxValue the value of a full bar; 255 is the highest base stat in the games.
 */
@Composable
public fun StatBar(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    maxValue: Int = 255,
    color: Color = statColor(value),
    labelWidth: Dp = 64.dp,
    animate: Boolean = true,
) {
    val target = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    val progress = remember { Animatable(if (animate) 0f else target) }
    LaunchedEffect(target) {
        if (animate) progress.animateTo(target, tween(durationMillis = 700)) else progress.snapTo(target)
    }
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = "$label $value" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            modifier = Modifier.width(labelWidth),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            value.toString(),
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.value)
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
        }
    }
}

/**
 * The six base stats in game order with their total, from `pokemon.stats`.
 *
 * @param longLabels "Sp. Atk" instead of "SpA".
 */
@Composable
public fun BaseStats(
    stats: List<PokemonStat>,
    modifier: Modifier = Modifier,
    longLabels: Boolean = true,
    totalLabel: String = "Total",
    animate: Boolean = true,
) {
    val ordered = BaseStat.entries.mapNotNull { stat ->
        stats.firstOrNull { it.stat.name == stat.apiName }?.let { stat to it.baseStat }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for ((stat, value) in ordered) {
            StatBar(
                label = if (longLabels) stat.label else stat.shortLabel,
                value = value,
                modifier = Modifier.fillMaxWidth(),
                animate = animate,
            )
        }
        val total = ordered.sumOf { it.second }
        StatBar(
            label = totalLabel,
            value = total,
            modifier = Modifier.fillMaxWidth(),
            maxValue = 720,
            color = MaterialTheme.colorScheme.primary,
            animate = animate,
        )
    }
}
