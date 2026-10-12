package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.cache.CachePolicy
import io.github.halilozel1903.pokeapi.model.BaseStat
import io.github.halilozel1903.pokeapi.types.PokeType
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PokeApiClientTest {

    @Test
    fun decodesPokemon() = runTest {
        MockPokeApi().use { api ->
            val charizard = api.client.pokemon("charizard")
            assertEquals(6, charizard.id)
            assertEquals("charizard", charizard.name)
            assertEquals(listOf(PokeType.Fire, PokeType.Flying), charizard.pokeTypes)
            assertEquals(1.7, charizard.heightMeters)
            assertEquals(90.5, charizard.weightKilograms)
            assertEquals(267, charizard.baseExperience)
            assertEquals(109, charizard.baseStat(BaseStat.SpecialAttack))
            assertEquals(534, charizard.baseStatTotal)
            assertEquals("#0006", charizard.dexNumber)
            assertEquals(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/6.png",
                charizard.officialArtworkUrl,
            )
            assertEquals(PokeApiSprites.officialArtwork(6), charizard.officialArtworkUrl)
            val hidden = charizard.abilities.single { it.isHidden }
            assertEquals("solar-power", hidden.ability.name)
            assertEquals(94, hidden.ability.id)
            assertEquals("pokemon-species", charizard.species.url.split('/').dropLast(2).last())
            assertTrue(charizard.moves.any { it.move.name == "flamethrower" })
            assertNotNull(charizard.cries?.latest)
        }
    }

    @Test
    fun decodesPastTypesAndHeldItems() = runTest {
        MockPokeApi().use { api ->
            val clefairy = api.client.pokemon("clefairy")
            assertEquals(listOf(PokeType.Fairy), clefairy.pokeTypes)
            assertEquals("normal", clefairy.pastTypes.single().types.single().type.name)
            assertEquals("generation-v", clefairy.pastTypes.single().generation.name)
            assertEquals("moon-stone", clefairy.heldItems.single().item.name)
        }
    }

    @Test
    fun normalizesNamesAndSendsUserAgent() = runTest {
        MockPokeApi().use { api ->
            api.client.pokemon("  Bulbasaur ")
            val request = api.requests.single()
            assertEquals("https://pokeapi.co/api/v2/pokemon/bulbasaur/", request.url.toString())
            assertTrue(request.headers[HttpHeaders.UserAgent].orEmpty().startsWith("pokeapi-kotlin/"))
        }
    }

    @Test
    fun speciesTextsAndEvolutionChain() = runTest {
        MockPokeApi().use { api ->
            val species = api.client.pokemonSpecies("bulbasaur")
            assertEquals("Seed Pokémon", species.genus())
            assertEquals("たねポケモン", species.genus("ja-Hrkt"))
            assertEquals("Bulbizarre", species.localizedName("fr"))
            assertEquals("Bulbasaur", species.localizedName())
            assertEquals(
                "A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.",
                species.flavorText(version = "red"),
            )
            assertEquals(
                "It carries a seed on its back right from birth. As it grows older, the seed also grows larger.",
                species.flavorText(),
            )
            assertNull(species.flavorText(language = "ko"))
            assertEquals(0.125, species.femaleRatio)
            assertEquals(1, species.evolutionChainId)
            assertEquals(listOf("monster", "plant"), species.eggGroups.map { it.name })

            val chain = assertNotNull(api.client.evolutionChain(species))
            assertEquals(1, chain.id)
            assertEquals("ivysaur", chain.chain.evolvesTo.single().species.name)
            assertEquals("https://pokeapi.co/api/v2/evolution-chain/1/", api.urls.last())
        }
    }

    @Test
    fun loadsByIdAndFollowsLinks() = runTest {
        MockPokeApi().use { api ->
            val chain = api.client.evolutionChain(67)
            assertEquals(8, chain.chain.evolvesTo.size)
            assertEquals("https://pokeapi.co/api/v2/evolution-chain/67/", api.urls.single())

            val charizard = api.client.pokemon("charizard")
            val species = api.client.get(PokeApiEndpoint.PokemonSpecies, charizard.species.name)
            assertEquals("charmeleon", species.evolvesFromSpecies?.name)
            assertEquals(4, species.varieties.size)
        }
    }

    @Test
    fun otherEndpoints() = runTest {
        MockPokeApi().use { api ->
            val client = api.client
            val overgrow = client.ability("overgrow")
            assertEquals("Strengthens grass moves to inflict 1.5× damage at 1/3 max HP or less.", overgrow.shortEffect())
            assertEquals("Notdünger", overgrow.names.first { it.language.name == "de" }.name)

            val thunderbolt = client.move("thunderbolt")
            assertEquals(90, thunderbolt.power)
            assertEquals(PokeType.Electric, thunderbolt.pokeType)
            assertEquals("special", thunderbolt.damageClass?.name)
            assertEquals("Has a 10% chance to paralyze the target.", thunderbolt.shortEffect())
            assertEquals("paralysis", thunderbolt.meta?.ailment?.name)

            val stone = client.item("thunder-stone")
            assertEquals(3000, stone.cost)
            assertEquals("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/thunder-stone.png", stone.sprites?.defaultSprite)
            assertTrue(stone.flavorTextEntries.last().text.startsWith("A peculiar stone"))

            val generation = client.generation("generation-i")
            assertEquals(151, generation.pokemonSpecies.size)
            assertEquals("kanto", generation.mainRegion?.name)

            val kanto = client.pokedex("kanto")
            assertEquals(151, kanto.pokemonEntries.size)
            assertEquals("mew", kanto.pokemonEntries.last().pokemonSpecies.name)

            val cheri = client.berry("cheri")
            assertEquals(10, cheri.flavors.first { it.flavor.name == "spicy" }.potency)
            assertEquals("fire", cheri.naturalGiftType?.name)

            val fire = client.type(PokeType.Fire)
            assertEquals(PokeType.Fire, fire.pokeType)
            assertEquals(listOf("grass", "ice", "bug", "steel"), fire.damageRelations.doubleDamageTo.map { it.name })
        }
    }

    @Test
    fun cachesResponses() = runTest {
        MockPokeApi().use { api ->
            api.client.pokemon("bulbasaur")
            api.client.pokemon("bulbasaur")
            api.client.pokemon("BULBASAUR")
            assertEquals(1, api.requests.size)

            api.client.clearCache()
            api.client.pokemon("bulbasaur")
            assertEquals(2, api.requests.size)
        }
    }

    @Test
    fun sharesConcurrentRequests() = runTest {
        MockPokeApi(override = { delay(50); null }).use { api ->
            val results = List(10) { async { api.client.pokemon("eevee") } }.awaitAll()
            assertTrue(results.all { it.id == 133 })
            assertEquals(1, api.requests.size)
        }
    }

    @Test
    fun networkOnlySkipsTheCache() = runTest {
        MockPokeApi(cachePolicy = CachePolicy.NetworkOnly).use { api ->
            api.client.pokemon("bulbasaur")
            api.client.pokemon("bulbasaur")
            assertEquals(2, api.requests.size)
        }
    }

    @Test
    fun networkFirstFallsBackToTheCacheWhenOffline() = runTest {
        var offline = false
        MockPokeApi(
            cachePolicy = CachePolicy.NetworkFirst,
            override = { if (offline) throw IOException("No route to host") else null },
        ).use { api ->
            assertEquals(1, api.client.pokemon("bulbasaur").id)
            offline = true
            assertEquals(1, api.client.pokemon("bulbasaur").id)
            assertEquals(2, api.requests.size)
            assertFailsWith<PokeApiException.Network> { api.client.pokemon("charizard") }
        }
    }

    @Test
    fun mapsErrors() = runTest {
        MockPokeApi().use { api ->
            val notFound = assertFailsWith<PokeApiException.NotFound> { api.client.pokemon("missingno") }
            assertEquals("https://pokeapi.co/api/v2/pokemon/missingno/", notFound.url)
        }
        MockPokeApi(override = { respondError(HttpStatusCode.TooManyRequests) }).use { api ->
            val error = assertFailsWith<PokeApiException.Http> { api.client.pokemon("bulbasaur") }
            assertEquals(429, error.status)
        }
        MockPokeApi(override = { respond("<html>Bad gateway</html>", HttpStatusCode.OK) }).use { api ->
            assertFailsWith<PokeApiException.Decoding> { api.client.pokemon("bulbasaur") }
            // A response that could not be decoded is not kept.
            assertFailsWith<PokeApiException.Decoding> { api.client.pokemon("bulbasaur") }
            assertEquals(2, api.requests.size)
        }
        MockPokeApi(override = { throw IOException("Connection reset") }).use { api ->
            val error = assertFailsWith<PokeApiException.Network> { api.client.pokemon("bulbasaur") }
            assertTrue(error.cause is IOException)
        }
    }

    @Test
    fun rejectsInvalidArguments() = runTest {
        MockPokeApi().use { api ->
            assertFailsWith<IllegalArgumentException> { api.client.pokemon(0) }
            assertFailsWith<IllegalArgumentException> { api.client.pokemon("   ") }
            assertFailsWith<IllegalArgumentException> { api.client.list(PokeApiEndpoint.Berry, limit = 0) }
            assertFalse(api.requests.isNotEmpty())
        }
    }

    @Test
    fun typeIndexColoursAGrid() = runTest {
        MockPokeApi().use { api ->
            val index = api.client.typeIndex()
            assertEquals(18, api.requests.size)
            assertEquals(156, index.size)
            assertEquals(listOf(PokeType.Grass, PokeType.Poison), index["bulbasaur"])
            assertEquals(listOf(PokeType.Fire, PokeType.Flying), index["charizard"])
            assertEquals(listOf(PokeType.Rock, PokeType.Water), index["omanyte"])
            assertEquals(listOf(PokeType.Fairy), index["sylveon"])
        }
    }

    @Test
    fun customBaseUrl() = runTest {
        MockPokeApi().use { api ->
            PokeApiClient {
                baseUrl = "https://pokeapi.example.com/api/v2"
                engine = api.engine
                retries = 0
            }.use { client ->
                client.berry("cheri")
            }
            assertEquals("https://pokeapi.example.com/api/v2/berry/cheri/", api.urls.single())
        }
    }
}
