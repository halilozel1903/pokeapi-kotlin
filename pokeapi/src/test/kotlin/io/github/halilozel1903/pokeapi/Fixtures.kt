package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.cache.CachePolicy
import io.github.halilozel1903.pokeapi.cache.PokeApiCache
import io.github.halilozel1903.pokeapi.transport.FixtureTransport
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.util.concurrent.CopyOnWriteArrayList

/** Reads `src/test/resources/fixtures/<path>`. */
internal fun fixture(path: String): String? =
    Fixtures::class.java.getResource("/fixtures/$path")?.readText()

internal object Fixtures

/** A client on a Ktor [MockEngine] that serves the JSON fixtures and answers 404 for anything else. */
internal class MockPokeApi(
    cache: PokeApiCache? = PokeApiCache.memory(),
    cachePolicy: CachePolicy = CachePolicy.CacheFirst,
    private val override: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData?)? = null,
) : AutoCloseable {
    val requests: MutableList<HttpRequestData> = CopyOnWriteArrayList()

    val engine = MockEngine { request ->
        requests += request
        override?.invoke(this, request) ?: run {
            val path = FixtureTransport.pathFor(request.url.toString())
            val body = path?.let { fixture(it) }
            if (body == null) {
                respond("{\"detail\":\"Not found.\"}", HttpStatusCode.NotFound, headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
    }

    val client = PokeApiClient {
        engine = this@MockPokeApi.engine
        this.cache = cache
        this.cachePolicy = cachePolicy
        retries = 0
    }

    val urls: List<String> get() = requests.map { it.url.toString() }

    override fun close() {
        client.close()
        engine.close()
    }
}
