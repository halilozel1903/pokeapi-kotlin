package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.cache.DiskCache
import io.github.halilozel1903.pokeapi.cache.MemoryCache
import io.github.halilozel1903.pokeapi.cache.PokeApiCache
import io.github.halilozel1903.pokeapi.cache.plus
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CacheTest {
    private var now = 1_000_000L
    private val clock = { now }

    @Test
    fun memoryCacheEvictsLeastRecentlyUsed() = runTest {
        val cache = MemoryCache(maxEntries = 2)
        cache.put("a", "1")
        cache.put("b", "2")
        cache.get("a")
        cache.put("c", "3")
        assertEquals("1", cache.get("a"))
        assertNull(cache.get("b"))
        assertEquals("3", cache.get("c"))
        assertEquals(2, cache.size)
    }

    @Test
    fun memoryCacheExpires() = runTest {
        val cache = PokeApiCache.memory(ttl = 10.minutes, clock = clock)
        cache.put("a", "1")
        now += 9.minutes.inWholeMilliseconds
        assertEquals("1", cache.get("a"))
        now += 1.minutes.inWholeMilliseconds
        assertNull(cache.get("a"))
    }

    @Test
    fun diskCacheStoresExpiresAndClears() = runTest {
        val dir = Files.createTempDirectory("pokeapi-cache").toFile()
        try {
            val cache = PokeApiCache.disk(dir, ttl = 1.hours, clock = clock)
            val body = fixture("pokemon/bulbasaur.json")!!
            cache.put("https://pokeapi.co/api/v2/pokemon/bulbasaur/", body)
            assertEquals(body, cache.get("https://pokeapi.co/api/v2/pokemon/bulbasaur/"))
            // A second instance on the same folder sees the entry.
            assertEquals(body, DiskCache(dir, clock = clock).get("https://pokeapi.co/api/v2/pokemon/bulbasaur/"))
            assertNull(cache.get("https://pokeapi.co/api/v2/pokemon/ivysaur/"))

            now += 1.hours.inWholeMilliseconds
            assertNull(cache.get("https://pokeapi.co/api/v2/pokemon/bulbasaur/"))
            assertEquals(0, dir.listFiles()!!.size)

            cache.put("x", "1")
            cache.clear()
            assertNull(cache.get("x"))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun diskCacheIgnoresCorruptFilesAndTrims() = runTest {
        val dir = Files.createTempDirectory("pokeapi-cache").toFile()
        try {
            val cache = DiskCache(dir, maxBytes = 2_500, clock = clock)
            cache.put("a", "x".repeat(1_000))
            dir.listFiles()!!.single().writeText("garbage")
            assertNull(cache.get("a"))

            for (key in listOf("b", "c", "d")) {
                cache.put(key, "y".repeat(1_000))
                // Make modification times differ so the oldest file is trimmed first.
                dir.listFiles()!!.forEach { it.setLastModified(it.lastModified() - 10_000) }
            }
            assertTrue(cache.sizeBytes() <= 2_500)
            assertNull(cache.get("b"))
            assertEquals("y".repeat(1_000), cache.get("d"))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun tieredCacheFillsUpperLevels() = runTest {
        val memory = MemoryCache()
        val disk = MemoryCache()
        val tiered = memory + disk
        disk.put("a", "1")
        assertEquals("1", tiered.get("a"))
        assertEquals("1", memory.get("a"))
        tiered.put("b", "2")
        assertEquals("2", disk.get("b"))
        tiered.remove("b")
        assertNull(memory.get("b"))
        assertNull(disk.get("b"))
    }

    @Test
    fun clientUsesDiskCacheAcrossInstances() = runTest {
        val dir = Files.createTempDirectory("pokeapi-cache").toFile()
        try {
            MockPokeApi(cache = PokeApiCache.memory() + PokeApiCache.disk(dir)).use { api ->
                api.client.pokemon("eevee")
            }
            MockPokeApi(cache = PokeApiCache.memory() + PokeApiCache.disk(dir)).use { api ->
                assertEquals(133, api.client.pokemon("eevee").id)
                assertTrue(api.requests.isEmpty())
            }
        } finally {
            dir.deleteRecursively()
        }
    }
}
