package io.github.halilozel1903.pokeapi.transport

import io.github.halilozel1903.pokeapi.PokeApiClient
import io.github.halilozel1903.pokeapi.PokeApiException

/**
 * Serves PokeAPI responses from local JSON files, for tests, previews and offline demos. [read] gets a relative
 * path and returns the file's text, or `null` when there is no such file. Urls map to paths like this:
 *
 * | Url | Path |
 * | --- | --- |
 * | `.../pokemon/charizard/` | `pokemon/charizard.json` |
 * | `.../evolution-chain/67/` | `evolution-chain/67.json` |
 * | `.../berry?offset=30&limit=30` | `berry/list-30-30.json` |
 *
 * ```kotlin
 * val client = PokeApiClient {
 *     transport = FixtureTransport { path -> javaClass.getResource("/fixtures/$path")?.readText() }
 * }
 * ```
 */
public class FixtureTransport(
    private val baseUrl: String = PokeApiClient.DEFAULT_BASE_URL,
    private val read: (path: String) -> String?,
) : PokeApiTransport {

    private val lock = Any()
    private val recorded = mutableListOf<String>()

    /** Every url requested so far, in order. */
    public val requests: List<String> get() = synchronized(lock) { recorded.toList() }

    override suspend fun get(url: String): String {
        synchronized(lock) { recorded += url }
        val path = pathFor(url, baseUrl) ?: throw PokeApiException.NotFound(url)
        return read(path) ?: throw PokeApiException.NotFound(url)
    }

    public companion object {
        /** The fixture path for [url], or `null` when the url is not a PokeAPI resource or list url. */
        public fun pathFor(url: String, baseUrl: String = PokeApiClient.DEFAULT_BASE_URL): String? {
            val base = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
            val relative = when {
                url.startsWith(base) -> url.removePrefix(base)
                "/api/v2/" in url -> url.substringAfter("/api/v2/")
                else -> return null
            }
            val path = relative.substringBefore('?').trim('/')
            val query = relative.substringAfter('?', missingDelimiterValue = "")
            val segments = path.split('/').filter { it.isNotEmpty() }
            if (query.isNotEmpty()) {
                val params = query.split('&').associate { it.substringBefore('=') to it.substringAfter('=', "") }
                val offset = params["offset"] ?: "0"
                val limit = params["limit"] ?: "20"
                return if (segments.size == 1) "${segments[0]}/list-$offset-$limit.json" else null
            }
            return when (segments.size) {
                1 -> "${segments[0]}/list-0-20.json"
                2 -> "${segments[0]}/${segments[1]}.json"
                else -> null
            }
        }
    }
}
