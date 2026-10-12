package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.cache.CachePolicy
import io.github.halilozel1903.pokeapi.model.Ability
import io.github.halilozel1903.pokeapi.model.ApiResource
import io.github.halilozel1903.pokeapi.model.Berry
import io.github.halilozel1903.pokeapi.model.EvolutionChain
import io.github.halilozel1903.pokeapi.model.Generation
import io.github.halilozel1903.pokeapi.model.Item
import io.github.halilozel1903.pokeapi.model.Move
import io.github.halilozel1903.pokeapi.model.NamedApiResource
import io.github.halilozel1903.pokeapi.model.NamedApiResourceList
import io.github.halilozel1903.pokeapi.model.Pokedex
import io.github.halilozel1903.pokeapi.model.Pokemon
import io.github.halilozel1903.pokeapi.model.PokemonSpecies
import io.github.halilozel1903.pokeapi.model.Type
import io.github.halilozel1903.pokeapi.transport.KtorTransport
import io.github.halilozel1903.pokeapi.transport.PokeApiTransport
import io.github.halilozel1903.pokeapi.types.PokeType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * A coroutine client for [PokeAPI v2](https://pokeapi.co/docs/v2).
 *
 * ```kotlin
 * val client = PokeApiClient {
 *     cache = PokeApiCache.memory() + PokeApiCache.disk(File(cacheDir, "pokeapi"))
 * }
 * val charizard = client.pokemon("charizard")
 * val species = client.pokemonSpecies(charizard.species.name)
 * val chain = client.evolutionChain(species)
 * ```
 *
 * Every function is main-safe and throws a [PokeApiException] on failure. Responses are cached by url (see
 * [PokeApiConfig.cache]) and identical requests running at the same time share one network call.
 * Close the client when you no longer need it.
 */
public class PokeApiClient(configure: PokeApiConfig.() -> Unit = {}) : AutoCloseable {

    private val config = PokeApiConfig().apply(configure)

    /** The API root, always ending with a slash. */
    public val baseUrl: String = config.baseUrl.let { if (it.endsWith('/')) it else "$it/" }

    private val transport: PokeApiTransport = config.transport ?: KtorTransport(
        httpClient = config.httpClient,
        engine = config.engine,
        userAgent = config.userAgent,
        requestTimeoutMillis = config.requestTimeoutMillis,
        retries = config.retries,
    )
    private val cache = config.cache.takeIf { config.cachePolicy != CachePolicy.NetworkOnly }
    private val cachePolicy = config.cachePolicy
    private val inFlightLock = Mutex()
    private val inFlight = HashMap<String, CompletableDeferred<String>>()

    // region Resources

    /** `GET /pokemon/{id}`. */
    public suspend fun pokemon(id: Int): Pokemon = get(PokeApiEndpoint.Pokemon, id)

    /** `GET /pokemon/{name}`. The name is normalized: "Mr. Mime" works. */
    public suspend fun pokemon(name: String): Pokemon = get(PokeApiEndpoint.Pokemon, name)

    /** `GET /pokemon-species/{id}`. */
    public suspend fun pokemonSpecies(id: Int): PokemonSpecies = get(PokeApiEndpoint.PokemonSpecies, id)

    /** `GET /pokemon-species/{name}`. */
    public suspend fun pokemonSpecies(name: String): PokemonSpecies = get(PokeApiEndpoint.PokemonSpecies, name)

    /** The species of [pokemon] (its `species` link). */
    public suspend fun pokemonSpecies(pokemon: Pokemon): PokemonSpecies = follow(pokemon.species, PokeApiEndpoint.PokemonSpecies)

    /** `GET /evolution-chain/{id}`. */
    public suspend fun evolutionChain(id: Int): EvolutionChain = get(PokeApiEndpoint.EvolutionChain, id)

    /** The evolution chain of [species], or `null` when it has none. */
    public suspend fun evolutionChain(species: PokemonSpecies): EvolutionChain? =
        species.evolutionChain?.let { follow(it, PokeApiEndpoint.EvolutionChain) }

    /** `GET /type/{id}`. */
    public suspend fun type(id: Int): Type = get(PokeApiEndpoint.Type, id)

    /** `GET /type/{name}`. */
    public suspend fun type(name: String): Type = get(PokeApiEndpoint.Type, name)

    /** `GET /type/{name}` for [type]. */
    public suspend fun type(type: PokeType): Type = get(PokeApiEndpoint.Type, type.apiName)

    /** `GET /ability/{id}`. */
    public suspend fun ability(id: Int): Ability = get(PokeApiEndpoint.Ability, id)

    /** `GET /ability/{name}`. */
    public suspend fun ability(name: String): Ability = get(PokeApiEndpoint.Ability, name)

    /** `GET /move/{id}`. */
    public suspend fun move(id: Int): Move = get(PokeApiEndpoint.Move, id)

    /** `GET /move/{name}`. */
    public suspend fun move(name: String): Move = get(PokeApiEndpoint.Move, name)

    /** `GET /item/{id}`. */
    public suspend fun item(id: Int): Item = get(PokeApiEndpoint.Item, id)

    /** `GET /item/{name}`. */
    public suspend fun item(name: String): Item = get(PokeApiEndpoint.Item, name)

    /** `GET /generation/{id}`. */
    public suspend fun generation(id: Int): Generation = get(PokeApiEndpoint.Generation, id)

    /** `GET /generation/{name}`, such as "generation-i". */
    public suspend fun generation(name: String): Generation = get(PokeApiEndpoint.Generation, name)

    /** `GET /pokedex/{id}`. */
    public suspend fun pokedex(id: Int): Pokedex = get(PokeApiEndpoint.Pokedex, id)

    /** `GET /pokedex/{name}`, such as "kanto" or "national". */
    public suspend fun pokedex(name: String): Pokedex = get(PokeApiEndpoint.Pokedex, name)

    /** `GET /berry/{id}`. */
    public suspend fun berry(id: Int): Berry = get(PokeApiEndpoint.Berry, id)

    /** `GET /berry/{name}`. */
    public suspend fun berry(name: String): Berry = get(PokeApiEndpoint.Berry, name)

    /** `GET /{endpoint}/{id}` for any [PokeApiEndpoint]. */
    public suspend fun <T> get(endpoint: PokeApiEndpoint<T>, id: Int): T {
        require(id > 0) { "PokeAPI ids start at 1, was $id" }
        return fetch(resourceUrl(endpoint, id.toString()), endpoint.serializer)
    }

    /** `GET /{endpoint}/{name}` for any [PokeApiEndpoint]. The name is normalized with [PokeApiNames.normalize]. */
    public suspend fun <T> get(endpoint: PokeApiEndpoint<T>, name: String): T {
        val key = PokeApiNames.normalize(name)
        require(key.isNotEmpty()) { "The name is empty" }
        return fetch(resourceUrl(endpoint, key), endpoint.serializer)
    }

    /** Loads the resource a [NamedApiResource] link points to, such as `pokemon.species`. */
    public suspend fun <T> follow(resource: NamedApiResource, endpoint: PokeApiEndpoint<T>): T =
        fetch(resource.url, endpoint.serializer)

    /** Loads the resource an [ApiResource] link points to, such as `species.evolution_chain`. */
    public suspend fun <T> follow(resource: ApiResource, endpoint: PokeApiEndpoint<T>): T =
        fetch(resource.url, endpoint.serializer)

    /** Loads and decodes any PokeAPI url with [deserializer], through the cache. */
    public suspend fun <T> fetch(url: String, deserializer: DeserializationStrategy<T>): T {
        val body = load(url)
        return try {
            json.decodeFromString(deserializer, body)
        } catch (e: SerializationException) {
            cache?.remove(url)
            throw PokeApiException.Decoding(url, e)
        } catch (e: IllegalArgumentException) {
            cache?.remove(url)
            throw PokeApiException.Decoding(url, e)
        }
    }

    // endregion

    // region Lists and paging

    /** One page of [endpoint]'s resource list: `GET /{endpoint}?offset={offset}&limit={limit}`. */
    public suspend fun list(endpoint: PokeApiEndpoint<*>, offset: Int = 0, limit: Int = DEFAULT_PAGE_SIZE): NamedApiResourceList {
        require(offset >= 0) { "offset must not be negative, was $offset" }
        require(limit > 0) { "limit must be positive, was $limit" }
        return fetch(listUrl(endpoint, offset, limit), NamedApiResourceList.serializer())
    }

    /** The page that follows [page], or `null` on the last page. */
    public suspend fun nextPage(page: NamedApiResourceList): NamedApiResourceList? =
        page.next?.let { fetch(it, NamedApiResourceList.serializer()) }

    /** The page before [page], or `null` on the first page. */
    public suspend fun previousPage(page: NamedApiResourceList): NamedApiResourceList? =
        page.previous?.let { fetch(it, NamedApiResourceList.serializer()) }

    /**
     * Every page of [endpoint], requested lazily as the flow is collected, following each page's `next` link.
     * Stop early with `take(n)` or `first { }`.
     */
    public fun pages(endpoint: PokeApiEndpoint<*>, pageSize: Int = DEFAULT_PAGE_SIZE, startOffset: Int = 0): Flow<NamedApiResourceList> = flow {
        var page: NamedApiResourceList? = list(endpoint, startOffset, pageSize)
        while (page != null) {
            emit(page)
            page = nextPage(page)
        }
    }

    /** Every resource of [endpoint] one by one, loading pages of [pageSize] as needed. */
    public fun all(endpoint: PokeApiEndpoint<*>, pageSize: Int = DEFAULT_PAGE_SIZE): Flow<NamedApiResource> = flow {
        pages(endpoint, pageSize).collect { page -> page.results.forEach { emit(it) } }
    }

    // endregion

    // region Helpers

    /**
     * The types of every Pokémon, by Pokémon name, from one `GET /type/{name}` per type instead of one request per
     * Pokémon. Handy for colouring a grid built from a resource list.
     */
    public suspend fun typeIndex(types: Collection<PokeType> = PokeType.entries): Map<String, List<PokeType>> = coroutineScope {
        val loaded = types.map { pokeType -> async { pokeType to type(pokeType) } }.awaitAll()
        val slots = HashMap<String, MutableList<Pair<Int, PokeType>>>()
        for ((pokeType, type) in loaded) {
            for (entry in type.pokemon) {
                slots.getOrPut(entry.pokemon.name) { mutableListOf() }.add(entry.slot to pokeType)
            }
        }
        slots.mapValues { (_, list) -> list.sortedBy { it.first }.map { it.second } }
    }

    /** Removes every cached response. */
    public suspend fun clearCache() {
        config.cache?.clear()
    }

    /** Releases the HTTP client this instance created. */
    override fun close() {
        (transport as? AutoCloseable)?.close()
    }

    // endregion

    private fun resourceUrl(endpoint: PokeApiEndpoint<*>, key: String): String = "$baseUrl${endpoint.path}/$key/"

    private fun listUrl(endpoint: PokeApiEndpoint<*>, offset: Int, limit: Int): String =
        "$baseUrl${endpoint.path}?offset=$offset&limit=$limit"

    private suspend fun load(url: String): String {
        if (cachePolicy == CachePolicy.CacheFirst) {
            cache?.get(url)?.let { return it }
        }
        return loadShared(url)
    }

    /** Joins a request for the same url that is already running, or starts one. */
    private suspend fun loadShared(url: String): String {
        var owner = false
        val deferred = inFlightLock.withLock {
            inFlight[url] ?: CompletableDeferred<String>().also {
                inFlight[url] = it
                owner = true
            }
        }
        if (!owner) {
            return try {
                deferred.await()
            } catch (e: CancellationException) {
                // The coroutine that made the request was cancelled, not this one: try again.
                currentCoroutineContext().ensureActive()
                loadShared(url)
            }
        }
        val result = runCatching { request(url) }
        withContext(NonCancellable) { inFlightLock.withLock { inFlight.remove(url) } }
        result.fold(
            onSuccess = { deferred.complete(it) },
            onFailure = { deferred.completeExceptionally(it) },
        )
        return result.getOrThrow()
    }

    private suspend fun request(url: String): String {
        val body = try {
            transport.get(url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val error = e as? PokeApiException ?: PokeApiException.Network(url, e)
            if (cachePolicy == CachePolicy.NetworkFirst && error !is PokeApiException.NotFound) {
                cache?.get(url)?.let { return it }
            }
            throw error
        }
        cache?.put(url, body)
        return body
    }

    public companion object {
        /** `https://pokeapi.co/api/v2/`. */
        public const val DEFAULT_BASE_URL: String = "https://pokeapi.co/api/v2/"

        /** The page size PokeAPI uses when none is given. */
        public const val DEFAULT_PAGE_SIZE: Int = 20

        internal val json: Json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            isLenient = true
        }
    }
}
