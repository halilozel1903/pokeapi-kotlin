package io.github.halilozel1903.pokeapi.sample

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.pokeapi.types.PokeType

/**
 * The whole app: a grid on phones that opens a detail screen, and grid and detail side by side on wide screens.
 * Screenshot [scene]s start on a fixed Pokémon and tab with the bundled demo data.
 */
@Composable
fun PokedexApp(
    scene: Scene?,
    liveRepository: () -> PokedexRepository,
    demoRepository: () -> PokedexRepository,
) {
    var demo by rememberSaveable { mutableStateOf(scene != null) }
    val repository = remember(demo) { if (demo) demoRepository() else liveRepository() }
    var reload by remember { mutableIntStateOf(0) }
    val dex by produceState<Load<List<DexEntry>>>(Load.Loading, repository, reload) {
        value = Load.Loading
        value = load { repository.loadDex() }
    }

    var selected by rememberSaveable { mutableStateOf(scene?.pokemon) }
    var tab by rememberSaveable { mutableStateOf(scene?.tab ?: DetailTab.About) }
    var query by rememberSaveable { mutableStateOf("") }
    // Saved as a comma separated list of type names so it survives rotation and process death.
    var filterKey by rememberSaveable { mutableStateOf("") }
    val filter: List<PokeType> = remember(filterKey) {
        filterKey.split(',').mapNotNull { PokeType.fromApiName(it) }
    }
    val gridState = rememberLazyGridState()

    CompositionLocalProvider(LocalDemoArtwork provides repository.isDemo) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val wide = maxWidth >= 840.dp
            when (val state = dex) {
                Load.Loading -> LoadingState()
                is Load.Failed -> ErrorState(
                    message = state.message,
                    onRetry = { reload++ },
                    onUseDemo = if (demo) null else ({ demo = true }),
                )
                is Load.Ready -> {
                    val entries = state.value
                    val typesByName = remember(entries) { entries.associate { it.name to it.types } }
                    val grid: @Composable (Modifier, Int) -> Unit = { modifier, columnsMin ->
                        PokedexScreen(
                            entries = entries,
                            query = query,
                            onQueryChange = { query = it },
                            filter = filter,
                            onFilterChange = { types -> filterKey = types.joinToString(",") { it.apiName } },
                            selected = if (wide) selected else null,
                            onSelect = { name ->
                                selected = name
                            },
                            gridState = gridState,
                            minCellWidth = columnsMin.dp,
                            modifier = modifier,
                        )
                    }
                    if (wide) {
                        Row(Modifier.fillMaxSize()) {
                            grid(Modifier.weight(0.56f), 156)
                            Box(
                                Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            )
                            Box(Modifier.weight(0.44f)) {
                                val name = selected
                                if (name == null) {
                                    EmptyDetail()
                                } else {
                                    key(name) {
                                        DetailPane(
                                            repository = repository,
                                            name = name,
                                            tab = tab,
                                            onTabChange = { tab = it },
                                            typesByName = typesByName,
                                            onBack = null,
                                            onSelectSpecies = { selected = it },
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val name = selected
                        if (name == null) {
                            grid(Modifier.fillMaxSize(), 156)
                        } else {
                            BackHandler { selected = null }
                            key(name) {
                                DetailPane(
                                    repository = repository,
                                    name = name,
                                    tab = tab,
                                    onTabChange = { tab = it },
                                    typesByName = typesByName,
                                    onBack = { selected = null },
                                    onSelectSpecies = { selected = it },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPane(
    repository: PokedexRepository,
    name: String,
    tab: DetailTab,
    onTabChange: (DetailTab) -> Unit,
    typesByName: Map<String, List<PokeType>>,
    onBack: (() -> Unit)?,
    onSelectSpecies: (String) -> Unit,
) {
    var reload by remember { mutableIntStateOf(0) }
    val detail by produceState<Load<PokemonDetail>>(Load.Loading, repository, name, reload) {
        value = Load.Loading
        value = load { repository.loadDetail(name) }
    }
    when (val state = detail) {
        Load.Loading -> LoadingState()
        is Load.Failed -> ErrorState(state.message, onRetry = { reload++ }, onUseDemo = null)
        is Load.Ready -> DetailScreen(
            detail = state.value,
            tab = tab,
            onTabChange = onTabChange,
            typesByName = typesByName,
            onBack = onBack,
            onSelectSpecies = onSelectSpecies,
        )
    }
}

@Composable
private fun EmptyDetail() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        CreatureArt(seed = 7, types = listOf(PokeType.Normal), modifier = Modifier.size(160.dp))
        Text(
            "Pick a Pokémon to see its stats, abilities, evolutions and type matchups.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
