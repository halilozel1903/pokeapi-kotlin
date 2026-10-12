package io.github.halilozel1903.pokeapi.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.PokeApiNames
import io.github.halilozel1903.pokeapi.compose.TypeChip
import io.github.halilozel1903.pokeapi.compose.color
import io.github.halilozel1903.pokeapi.types.PokeType

/** The grid with search and up to two type filters (a Pokémon must have both). */
@Composable
fun PokedexScreen(
    entries: List<DexEntry>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: List<PokeType>,
    onFilterChange: (List<PokeType>) -> Unit,
    selected: String?,
    onSelect: (String) -> Unit,
    gridState: LazyGridState,
    minCellWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val visible = remember(entries, query, filter) { entries.filter { it.matches(query) && it.types.containsAll(filter) } }
    Column(modifier) {
        Row(
            Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                "Pokédex",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
            )
            Text(
                if (visible.size == entries.size) "${entries.size} Pokémon" else "${visible.size} of ${entries.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
                maxLines = 1,
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search by name or number", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingIcon = { SearchIcon(MaterialTheme.colorScheme.onSurfaceVariant, Modifier.size(20.dp)) },
            trailingIcon = if (query.isEmpty()) {
                null
            } else {
                { IconButton(onClick = { onQueryChange("") }) { CloseIcon(MaterialTheme.colorScheme.onSurfaceVariant, Modifier.size(18.dp)) } }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            items(PokeType.entries, key = { it.apiName }) { type ->
                val isSelected = type in filter
                TypeChip(
                    type = type,
                    selected = isSelected,
                    onClick = {
                        onFilterChange(
                            when {
                                isSelected -> filter - type
                                filter.size >= 2 -> filter.drop(1) + type
                                else -> filter + type
                            },
                        )
                    },
                )
            }
        }
        if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.TopCenter) {
                Text(
                    "No Pokémon match. Try another name or fewer types.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minCellWidth),
                state = gridState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(visible, key = { it.id }) { entry ->
                    PokemonCard(entry, selected = entry.name == selected, onClick = { onSelect(entry.name) })
                }
            }
        }
    }
}

private fun DexEntry.matches(query: String): Boolean {
    val text = query.trim()
    if (text.isEmpty()) return true
    text.removePrefix("#").toIntOrNull()?.let { return id == it }
    val key = PokeApiNames.normalize(text)
    return name.contains(key) || displayName.contains(text, ignoreCase = true)
}

@Composable
fun PokemonCard(entry: DexEntry, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val surface = MaterialTheme.colorScheme.surface
    val accent = (entry.types.firstOrNull() ?: PokeType.Normal).color
    val darkTheme = surface.red + surface.green + surface.blue < 1.5f
    val brush = Brush.verticalGradient(
        listOf(
            lerp(surface, accent, if (darkTheme) 0.42f else 0.34f),
            lerp(surface, accent, if (darkTheme) 0.2f else 0.14f),
        ),
    )
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .clip(shape)
            .background(brush)
            .border(if (selected) 2.dp else 0.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Text(
            entry.dexNumber,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f),
            contentAlignment = Alignment.Center,
        ) {
            val ring = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(ring, radius = size.minDimension * 0.46f, style = Stroke(width = size.minDimension * 0.07f))
            }
            PokemonImage(
                id = entry.id,
                name = entry.displayName,
                types = entry.types,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            entry.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (type in entry.types) {
                TypeChip(type, Modifier.weight(1f, fill = false), compact = true)
            }
        }
    }
}
