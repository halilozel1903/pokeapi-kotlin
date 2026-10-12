package io.github.halilozel1903.pokeapi.sample

import android.content.Context
import io.github.halilozel1903.pokeapi.PokeApiClient
import io.github.halilozel1903.pokeapi.PokeApiEndpoint
import io.github.halilozel1903.pokeapi.PokeApiException
import io.github.halilozel1903.pokeapi.PokeApiNames
import io.github.halilozel1903.pokeapi.cache.PokeApiCache
import io.github.halilozel1903.pokeapi.cache.plus
import io.github.halilozel1903.pokeapi.model.EvolutionChain
import io.github.halilozel1903.pokeapi.model.Pokemon
import io.github.halilozel1903.pokeapi.model.PokemonSpecies
import io.github.halilozel1903.pokeapi.model.localized
import io.github.halilozel1903.pokeapi.transport.FixtureTransport
import io.github.halilozel1903.pokeapi.transport.PokeApiTransport
import io.github.halilozel1903.pokeapi.types.PokeType
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** One cell of the grid. */
data class DexEntry(val id: Int, val name: String, val types: List<PokeType>) {
    val displayName: String get() = PokeApiNames.prettify(name)
    val dexNumber: String get() = PokeApiNames.dexNumber(id)
}

data class AbilityInfo(val name: String, val hidden: Boolean, val effect: String?)

/** Everything the detail screen shows. */
data class PokemonDetail(
    val pokemon: Pokemon,
    val species: PokemonSpecies,
    val chain: EvolutionChain?,
    val abilities: List<AbilityInfo>,
)

/** The sample's only data source; every request goes through [PokeApiClient] and its cache. */
class PokedexRepository(private val client: PokeApiClient, val isDemo: Boolean) {

    /** The Kanto Pokédex with each Pokémon's types: one list request and 18 type requests. */
    suspend fun loadDex(): List<DexEntry> = coroutineScope {
        val page = async { client.list(PokeApiEndpoint.Pokemon, offset = 0, limit = DEX_SIZE) }
        val types = async { client.typeIndex() }
        val index = types.await()
        page.await().results.mapNotNull { resource ->
            val id = resource.id ?: return@mapNotNull null
            DexEntry(id, resource.name, index[resource.name].orEmpty())
        }
    }

    suspend fun loadDetail(name: String): PokemonDetail = coroutineScope {
        val pokemon = client.pokemon(name)
        val species = client.pokemonSpecies(pokemon.species.name)
        val chain = async {
            try {
                client.evolutionChain(species)
            } catch (e: PokeApiException) {
                null
            }
        }
        val abilities = pokemon.abilities.sortedBy { it.slot }.map { slot ->
            async {
                val ability = try {
                    client.ability(slot.ability.name)
                } catch (e: PokeApiException) {
                    null
                }
                AbilityInfo(
                    name = ability?.names?.localized("en") ?: PokeApiNames.prettify(slot.ability.name),
                    hidden = slot.isHidden,
                    effect = ability?.shortEffect(),
                )
            }
        }
        PokemonDetail(pokemon, species, chain.await(), abilities.awaitAll())
    }

    companion object {
        const val DEX_SIZE = 151
    }
}

/** One repository for the live API and one for the bundled fixtures, created on first use. */
object Repositories {
    private var live: PokedexRepository? = null
    private var demo: PokedexRepository? = null

    @Synchronized
    fun live(context: Context): PokedexRepository = live ?: PokedexRepository(
        PokeApiClient {
            engine = OkHttp.create()
            cache = PokeApiCache.memory() + PokeApiCache.disk(File(context.cacheDir, "pokeapi"))
            userAgent = "pokeapi-kotlin-sample/1.0.0 (+https://github.com/halilozel1903/pokeapi-kotlin)"
        },
        isDemo = false,
    ).also { live = it }

    @Synchronized
    fun demo(context: Context): PokedexRepository = demo ?: run {
        val assets = context.applicationContext.assets
        val fixtures = FixtureTransport { path ->
            try {
                assets.open("fixtures/$path").bufferedReader().use { it.readText() }
            } catch (e: IOException) {
                null
            }
        }
        PokedexRepository(
            PokeApiClient {
                transport = PokeApiTransport { url -> withContext(Dispatchers.IO) { fixtures.get(url) } }
            },
            isDemo = true,
        )
    }.also { demo = it }
}
