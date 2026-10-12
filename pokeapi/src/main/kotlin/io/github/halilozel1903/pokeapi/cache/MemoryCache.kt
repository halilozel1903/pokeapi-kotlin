package io.github.halilozel1903.pokeapi.cache

import kotlin.time.Duration

/** A thread safe, least recently used, in-memory [PokeApiCache]. */
public class MemoryCache(
    public val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    public val ttl: Duration = Duration.INFINITE,
    private val clock: () -> Long = System::currentTimeMillis,
) : PokeApiCache {

    init {
        require(maxEntries > 0) { "maxEntries must be positive, was $maxEntries" }
    }

    private class Entry(val value: String, val storedAt: Long)

    private val lock = Any()
    private val entries = object : LinkedHashMap<String, Entry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?): Boolean = size > maxEntries
    }

    /** The number of stored entries, expired ones included until they are read. */
    public val size: Int get() = synchronized(lock) { entries.size }

    override suspend fun get(key: String): String? = synchronized(lock) {
        val entry = entries[key] ?: return@synchronized null
        if (isExpired(entry.storedAt)) {
            entries.remove(key)
            null
        } else {
            entry.value
        }
    }

    override suspend fun put(key: String, value: String) {
        synchronized(lock) { entries[key] = Entry(value, clock()) }
    }

    override suspend fun remove(key: String) {
        synchronized(lock) { entries.remove(key) }
    }

    override suspend fun clear() {
        synchronized(lock) { entries.clear() }
    }

    private fun isExpired(storedAt: Long): Boolean =
        !ttl.isInfinite() && clock() - storedAt >= ttl.inWholeMilliseconds

    public companion object {
        /** About 500 responses; a Pokémon is 5 to 300 KB of JSON, so tune this for your app. */
        public const val DEFAULT_MAX_ENTRIES: Int = 512
    }
}
