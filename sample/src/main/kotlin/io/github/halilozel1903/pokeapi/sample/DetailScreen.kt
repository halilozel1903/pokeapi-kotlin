package io.github.halilozel1903.pokeapi.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.PokeApiNames
import io.github.halilozel1903.pokeapi.PokeApiSprites
import io.github.halilozel1903.pokeapi.compose.BaseStats
import io.github.halilozel1903.pokeapi.compose.EvolutionChainView
import io.github.halilozel1903.pokeapi.compose.TypeChip
import io.github.halilozel1903.pokeapi.compose.TypeMatchupGrid
import io.github.halilozel1903.pokeapi.compose.color
import io.github.halilozel1903.pokeapi.evolution.EvolutionStage
import io.github.halilozel1903.pokeapi.evolution.paths
import io.github.halilozel1903.pokeapi.types.PokeType
import java.util.Locale

@Composable
fun DetailScreen(
    detail: PokemonDetail,
    tab: DetailTab,
    onTabChange: (DetailTab) -> Unit,
    typesByName: Map<String, List<PokeType>>,
    onBack: (() -> Unit)?,
    onSelectSpecies: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        DetailHeader(detail, onBack)
        DetailTabs(tab, onTabChange)
        when (tab) {
            DetailTab.About -> AboutTab(detail)
            DetailTab.Stats -> StatsTab(detail)
            DetailTab.Evolution -> EvolutionTab(detail, typesByName, onSelectSpecies)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailHeader(detail: PokemonDetail, onBack: (() -> Unit)?) {
    val pokemon = detail.pokemon
    val types = pokemon.pokeTypes
    val accent = (types.firstOrNull() ?: PokeType.Normal).color
    val surface = MaterialTheme.colorScheme.background
    val darkTheme = surface.red + surface.green + surface.blue < 1.5f
    val brush = Brush.verticalGradient(
        listOf(lerp(surface, accent, if (darkTheme) 0.5f else 0.42f), surface),
    )
    Box(
        Modifier
            .fillMaxWidth()
            .background(brush),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .clickable(onClickLabel = "Back", role = Role.Button, onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        BackIcon(MaterialTheme.colorScheme.onSurface, Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    pokemon.dexNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            PokemonImage(
                url = pokemon.officialArtworkUrl,
                name = detail.species.localizedName(),
                modifier = Modifier.size(176.dp),
            )
            Text(
                detail.species.localizedName(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            detail.species.genus()?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (type in types) TypeChip(type)
            }
        }
    }
}

@Composable
private fun DetailTabs(tab: DetailTab, onTabChange: (DetailTab) -> Unit) {
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            for (item in DetailTab.entries) {
                val selected = item == tab
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Tab, onClick = { onTabChange(item) })
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        Modifier
                            .padding(top = 8.dp)
                            .width(36.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun Section(title: String, modifier: Modifier = Modifier, subtitle: String? = null, content: @Composable () -> Unit) {
    Column(modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
private fun AboutTab(detail: PokemonDetail) {
    val pokemon = detail.pokemon
    val species = detail.species
    Column(Modifier.padding(top = 8.dp)) {
        species.flavorText()?.let { text ->
            Text(
                text,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
            )
        }
        val gender = species.femaleRatio?.let { female -> "♂ ${percent(1 - female)}   ♀ ${percent(female)}" } ?: "Genderless"
        val facts = listOf(
            "Height" to "${format(pokemon.heightMeters)} m",
            "Weight" to "${format(pokemon.weightKilograms)} kg",
            "Category" to (species.genus() ?: "Unknown"),
            "Gender" to gender,
            "Catch rate" to species.captureRate.toString(),
            "Egg groups" to species.eggGroups.joinToString(", ") { PokeApiNames.prettify(it.name) }.ifEmpty { "None" },
        )
        Section("Profile") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in facts.chunked(2)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for ((label, value) in row) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                Text(
                                    value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
        Section("Abilities") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (ability in detail.abilities) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                ability.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (ability.hidden) {
                                Text(
                                    "Hidden",
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                )
                            }
                        }
                        ability.effect?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsTab(detail: PokemonDetail) {
    val pokemon = detail.pokemon
    Column(Modifier.padding(top = 8.dp)) {
        Section("Base stats") {
            BaseStats(pokemon.stats, Modifier.fillMaxWidth())
        }
        Section("Type matchups", subtitle = "Damage taken from each attacking type") {
            TypeMatchupGrid(pokemon.defensiveMatchups(), Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EvolutionTab(detail: PokemonDetail, typesByName: Map<String, List<PokeType>>, onSelectSpecies: (String) -> Unit) {
    val chain = detail.chain
    if (chain == null || chain.chain.evolvesTo.isEmpty()) {
        Section("Evolution") {
            Text("${detail.species.localizedName()} does not evolve.", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }
    val paths = chain.chain.paths().size
    Section(
        "Evolution chain",
        subtitle = if (paths > 1) "$paths ways to evolve. Tap a Pokémon to open it." else "Tap a Pokémon to open it.",
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth < 600.dp) 3 else 4
            EvolutionChainView(chain.chain, Modifier.fillMaxWidth(), columns = columns) { stage ->
                EvolutionNode(
                    stage = stage,
                    types = typesByName[stage.name].orEmpty(),
                    current = stage.name == detail.species.name,
                    onClick = { onSelectSpecies(stage.name) },
                )
            }
        }
    }
}

@Composable
private fun EvolutionNode(stage: EvolutionStage, types: List<PokeType>, current: Boolean, onClick: () -> Unit) {
    val accent = (types.firstOrNull() ?: PokeType.Normal).color
    val surface = MaterialTheme.colorScheme.surface
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .widthIn(max = 96.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(lerp(surface, accent, 0.3f))
                .border(if (current) 3.dp else 0.dp, if (current) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            PokemonImage(
                url = stage.id?.let { PokeApiSprites.officialArtwork(it) },
                name = PokeApiNames.prettify(stage.name),
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            PokeApiNames.prettify(stage.name),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        stage.id?.let {
            Text(
                PokeApiNames.dexNumber(it),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (types.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (type in types) TypeChip(type, Modifier.weight(1f, fill = false), compact = true)
            }
        }
    }
}

private fun format(value: Double): String = String.format(Locale.US, "%.1f", value)

private fun percent(ratio: Double): String {
    val value = ratio * 100
    return if (value % 1.0 == 0.0) "${value.toInt()}%" else String.format(Locale.US, "%.1f%%", value)
}
