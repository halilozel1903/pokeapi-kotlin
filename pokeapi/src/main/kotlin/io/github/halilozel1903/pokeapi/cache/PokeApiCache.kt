package io.github.halilozel1903.pokeapi.cache

import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Stores response bodies by request url. PokeAPI's data rarely changes and its fair use policy asks clients to
 * cache what they request, so [io.github.halilozel1903.pokeapi.PokeApiClient] uses a [memory] cache by default.
 *
 * ```kotlin
 * cache = PokeApiCache.memory() + PokeApiCache.disk(File(context.cacheDir, "pokeapi"))
 * ```
 *
 * Implement it to store responses elsewhere (a database, a key value store).
 */
public interface PokeApiCache {
    /** The body stored for [key], or `null` when there is none or it expired. */
    public suspend fun get(key: String): String?

    /** Stores [value] for [key]. */
    public suspend fun put(key: String, value: String)

    /** Removes the entry for [key]. */
    public suspend fun remove(key: String)

    /** Removes every entry. */
    public suspend fun clear()

    public companion object {
        /** An in-memory LRU cache of up to [maxEntries] responses that live for [ttl]. */
        public fun memory(
            maxEntries: Int = MemoryCache.DEFAULT_MAX_ENTRIES,
            ttl: Duration = Duration.INFINITE,
            clock: () -> Long = System::currentTimeMillis,
        ): MemoryCache = MemoryCache(maxEntries, ttl, clock)

        /** A cache of files in [directory], trimmed to [maxBytes], with entries that live for [ttl]. */
        public fun disk(
            directory: File,
            maxBytes: Long = DiskCache.DEFAULT_MAX_BYTES,
            ttl: Duration = 7.days,
            clock: () -> Long = System::currentTimeMillis,
        ): DiskCache = DiskCache(directory, maxBytes, ttl, clock)
    }
}

/**
 * Looks in this cache first and then in [other]; a hit in [other] is copied into this one. Writes go to both.
 * `memory + disk` keeps recent responses in memory and everything on disk.
 */
public operator fun PokeApiCache.plus(other: PokeApiCache): PokeApiCache {
    val levels = buildList<PokeApiCache> {
        if (this@plus is TieredCache) addAll(this@plus.levels) else add(this@plus)
        if (other is TieredCache) addAll(other.levels) else add(other)
    }
    return TieredCache(levels)
}

/** Several caches checked in order. Create one with `memory + disk`. */
public class TieredCache(public val levels: List<PokeApiCache>) : PokeApiCache {
    override suspend fun get(key: String): String? {
        for ((index, level) in levels.withIndex()) {
            val value = level.get(key) ?: continue
            for (upper in 0 until index) levels[upper].put(key, value)
            return value
        }
        return null
    }

    override suspend fun put(key: String, value: String) {
        for (level in levels) level.put(key, value)
    }

    override suspend fun remove(key: String) {
        for (level in levels) level.remove(key)
    }

    override suspend fun clear() {
        for (level in levels) level.clear()
    }
}

/** How [io.github.halilozel1903.pokeapi.PokeApiClient] uses its cache. */
public enum class CachePolicy {
    /** Use a cached response when there is one; otherwise request it and cache it. The default. */
    CacheFirst,

    /** Always request; fall back to a cached response when the request fails (offline). */
    NetworkFirst,

    /** Always request and never read or write the cache. */
    NetworkOnly,
}
