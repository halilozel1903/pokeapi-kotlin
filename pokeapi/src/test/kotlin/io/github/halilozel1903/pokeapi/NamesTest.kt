package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.cache.PokeApiCache
import io.github.halilozel1903.pokeapi.model.NamedApiResource
import io.github.halilozel1903.pokeapi.model.cleanFlavorText
import io.github.halilozel1903.pokeapi.transport.FixtureTransport
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class NamesTest {

    @Test
    fun normalizesUserInput() {
        assertEquals("mr-mime", PokeApiNames.normalize("Mr. Mime"))
        assertEquals("farfetchd", PokeApiNames.normalize("Farfetch'd"))
        assertEquals("farfetchd", PokeApiNames.normalize("Farfetch’d"))
        assertEquals("nidoran-f", PokeApiNames.normalize("Nidoran♀"))
        assertEquals("nidoran-m", PokeApiNames.normalize("nidoran ♂"))
        assertEquals("tapu-koko", PokeApiNames.normalize("  Tapu   Koko "))
        assertEquals("type-null", PokeApiNames.normalize("Type: Null"))
        assertEquals("25", PokeApiNames.normalize("25"))
    }

    @Test
    fun prettifiesApiNames() {
        assertEquals("Mr. Mime", PokeApiNames.prettify("mr-mime"))
        assertEquals("Nidoran♀", PokeApiNames.prettify("nidoran-f"))
        assertEquals("Thunder Stone", PokeApiNames.prettify("thunder-stone"))
        assertEquals("Charizard Mega X", PokeApiNames.prettify("charizard-mega-x"))
        assertEquals("#0025", PokeApiNames.dexNumber(25))
        assertEquals("#1025", PokeApiNames.dexNumber(1025))
    }

    @Test
    fun cleansFlavorText() {
        assertEquals(
            "Its genetic code is irregular. It may mutate if it is exposed to radiation from element STONEs.",
            "Its genetic code\nis irregular.\nIt may mutate if\u000cit is exposed to\nradiation from\nelement STONEs.".cleanFlavorText(),
        )
        assertEquals("A well-known move", "A well-\nknown move".cleanFlavorText())
        assertEquals("Hyphenation", "Hyphen­\nation".cleanFlavorText())
    }

    @Test
    fun resourceIds() {
        assertEquals(133, NamedApiResource("eevee", "https://pokeapi.co/api/v2/pokemon/133/").id)
        assertEquals(67, NamedApiResource(url = "https://pokeapi.co/api/v2/evolution-chain/67").id)
        assertNull(NamedApiResource("x", "https://pokeapi.co/api/v2/pokemon/").id)
    }

    @Test
    fun fixturePaths() {
        assertEquals("pokemon/charizard.json", FixtureTransport.pathFor("https://pokeapi.co/api/v2/pokemon/charizard/"))
        assertEquals("evolution-chain/67.json", FixtureTransport.pathFor("https://pokeapi.co/api/v2/evolution-chain/67"))
        assertEquals("berry/list-30-30.json", FixtureTransport.pathFor("https://pokeapi.co/api/v2/berry?offset=30&limit=30"))
        assertEquals("berry/list-30-30.json", FixtureTransport.pathFor("https://pokeapi.co/api/v2/berry/?limit=30&offset=30"))
        assertEquals("type/fire.json", FixtureTransport.pathFor("http://localhost:8000/api/v2/type/fire/", "http://localhost:8000/api/v2/"))
        assertNull(FixtureTransport.pathFor("https://example.com/other"))
    }

    @Test
    fun fixtureTransportServesAClient() = runTest {
        val transport = FixtureTransport { path -> fixture(path) }
        PokeApiClient {
            this.transport = transport
            cache = PokeApiCache.memory()
        }.use { client ->
            assertEquals("Flame Pokémon", client.pokemonSpecies("charizard").genus())
            assertFailsWith<PokeApiException.NotFound> { client.pokemon("missingno") }
            assertEquals(2, transport.requests.size)
        }
    }
}
