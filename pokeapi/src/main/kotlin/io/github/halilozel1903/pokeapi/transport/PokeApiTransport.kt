package io.github.halilozel1903.pokeapi.transport

/**
 * Fetches the body of a PokeAPI url. The client uses a Ktor based transport by default; supply your own through
 * `PokeApiConfig.transport` to serve responses from files ([FixtureTransport]), a database or another HTTP stack.
 *
 * Throw [io.github.halilozel1903.pokeapi.PokeApiException.NotFound] for missing resources; other exceptions are
 * reported as [io.github.halilozel1903.pokeapi.PokeApiException.Network].
 */
public fun interface PokeApiTransport {
    public suspend fun get(url: String): String
}
