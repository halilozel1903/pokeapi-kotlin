package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.cache.CachePolicy
import io.github.halilozel1903.pokeapi.cache.PokeApiCache
import io.github.halilozel1903.pokeapi.transport.PokeApiTransport
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

/** Settings for [PokeApiClient], set in its builder block. */
public class PokeApiConfig internal constructor() {
    /** The API root. Change it for a self-hosted PokeAPI. */
    public var baseUrl: String = PokeApiClient.DEFAULT_BASE_URL

    /**
     * The Ktor engine. `null` uses CIO. Pass `OkHttp.create()` (add `io.ktor:ktor-client-okhttp`) to share your
     * app's OkHttp stack, or a `MockEngine` in tests. The client does not close an engine you pass.
     */
    public var engine: HttpClientEngine? = null

    /** A ready Ktor client to use as is, instead of [engine]. [requestTimeoutMillis] and [retries] then do not apply. */
    public var httpClient: HttpClient? = null

    /** Replaces HTTP entirely, for example with a [io.github.halilozel1903.pokeapi.transport.FixtureTransport]. */
    public var transport: PokeApiTransport? = null

    /** Where responses are cached. An in-memory LRU cache by default; `null` turns caching off. */
    public var cache: PokeApiCache? = PokeApiCache.memory()

    /** How the cache is used. */
    public var cachePolicy: CachePolicy = CachePolicy.CacheFirst

    /** Sent with every request. PokeAPI asks API consumers to identify themselves. */
    public var userAgent: String = "pokeapi-kotlin/1.0.0 (+https://github.com/halilozel1903/pokeapi-kotlin)"

    /** Timeout of one request, in milliseconds. */
    public var requestTimeoutMillis: Long = 30_000

    /** Retries on network errors and 5xx responses, with exponential backoff. */
    public var retries: Int = 2
}
