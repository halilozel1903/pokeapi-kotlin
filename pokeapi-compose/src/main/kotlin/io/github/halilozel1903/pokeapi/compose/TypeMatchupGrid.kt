package io.github.halilozel1903.pokeapi.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.types.PokeType
import io.github.halilozel1903.pokeapi.types.TypeMatchups

/** "×4", "×2", "×½", "×¼", "×0" or "×1". */
public fun formatMultiplier(multiplier: Double): String = when (multiplier) {
    0.0 -> "×0"
    0.25 -> "×¼"
    0.5 -> "×½"
    else -> if (multiplier % 1.0 == 0.0) "×${multiplier.toInt()}" else "×$multiplier"
}

/**
 * The defensive matchups of a Pokémon in three groups: weaknesses, resistances and immunities, each type with its
 * multiplier. Empty groups are left out. Pass `pokemon.defensiveMatchups()` or `TypeChart.defending(...)`.
 */
@Composable
public fun TypeMatchupGrid(
    matchups: TypeMatchups,
    modifier: Modifier = Modifier,
    weakLabel: String = "Weak to",
    resistLabel: String = "Resists",
    immuneLabel: String = "Immune to",
) {
    BoxWithConstraints(modifier) {
        val columns = (maxWidth / 112.dp).toInt().coerceIn(2, 6)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MatchupGroup(weakLabel, matchups.weaknesses, matchups, columns)
            MatchupGroup(resistLabel, matchups.resistances, matchups, columns)
            MatchupGroup(immuneLabel, matchups.immunities, matchups, columns)
        }
    }
}

@Composable
private fun MatchupGroup(title: String, types: List<PokeType>, matchups: TypeMatchups, columns: Int) {
    if (types.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        for (row in types.chunked(columns)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (type in row) {
                    MatchupChip(type, matchups.multiplier(type), Modifier.weight(1f))
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A type with its damage multiplier, such as Rock ×4. */
@Composable
public fun MatchupChip(type: PokeType, multiplier: Double, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(type.color)
            .padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TypeIcon(type, Modifier.size(14.dp))
        Text(
            type.displayName,
            modifier = Modifier.weight(1f),
            color = type.onColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            formatMultiplier(multiplier),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(type.onColor.copy(alpha = 0.18f))
                .padding(horizontal = 6.dp, vertical = 1.dp),
            color = type.onColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}
