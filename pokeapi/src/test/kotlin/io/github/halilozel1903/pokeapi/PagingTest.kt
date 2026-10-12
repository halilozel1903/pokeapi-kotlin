package io.github.halilozel1903.pokeapi

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PagingTest {

    @Test
    fun listsOnePage() = runTest {
        MockPokeApi().use { api ->
            val page = api.client.list(PokeApiEndpoint.Pokemon, offset = 0, limit = 151)
            assertEquals(1302, page.count)
            assertEquals(151, page.results.size)
            assertEquals("mew", page.results.last().name)
            assertEquals(151, page.results.last().id)
            assertNull(page.previous)
            assertEquals("https://pokeapi.co/api/v2/pokemon?offset=0&limit=151", api.urls.single())
        }
    }

    @Test
    fun followsNextLinks() = runTest {
        MockPokeApi().use { api ->
            val pages = api.client.pages(PokeApiEndpoint.Berry, pageSize = 30).toList()
            assertEquals(listOf(30, 30, 4), pages.map { it.results.size })
            assertEquals(
                listOf(
                    "https://pokeapi.co/api/v2/berry?offset=0&limit=30",
                    "https://pokeapi.co/api/v2/berry?offset=30&limit=30",
                    "https://pokeapi.co/api/v2/berry?offset=60&limit=30",
                ),
                api.urls,
            )
            assertNull(pages.last().next)
            assertEquals(pages[1], api.client.previousPage(pages[2]))
            assertEquals(pages[2], api.client.nextPage(pages[1]))
        }
    }

    @Test
    fun allEmitsEveryResourceLazily() = runTest {
        MockPokeApi().use { api ->
            assertEquals(64, api.client.all(PokeApiEndpoint.Berry, pageSize = 30).count())
            assertEquals("rowap", api.client.all(PokeApiEndpoint.Berry, pageSize = 30).toList().last().name)
        }
        MockPokeApi().use { api ->
            val firstFive = api.client.all(PokeApiEndpoint.Berry, pageSize = 30).take(5).map { it.name }.toList()
            assertEquals(listOf("cheri", "chesto", "pecha", "rawst", "aspear"), firstFive)
            assertEquals(1, api.requests.size)
        }
        MockPokeApi().use { api ->
            assertEquals("lum", api.client.all(PokeApiEndpoint.Berry, pageSize = 30).first { it.id == 9 }.name)
        }
    }
}
