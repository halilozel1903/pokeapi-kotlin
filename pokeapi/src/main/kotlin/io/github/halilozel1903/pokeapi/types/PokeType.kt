package io.github.halilozel1903.pokeapi.types

/** The 18 battle types, in the games' order. [apiName] is the PokeAPI name ("fire"). */
public enum class PokeType(public val apiName: String) {
    Normal("normal"),
    Fire("fire"),
    Water("water"),
    Electric("electric"),
    Grass("grass"),
    Ice("ice"),
    Fighting("fighting"),
    Poison("poison"),
    Ground("ground"),
    Flying("flying"),
    Psychic("psychic"),
    Bug("bug"),
    Rock("rock"),
    Ghost("ghost"),
    Dragon("dragon"),
    Dark("dark"),
    Steel("steel"),
    Fairy("fairy"),
    ;

    /** "Fire". */
    public val displayName: String get() = name

    public companion object {
        /** The type with PokeAPI name [apiName] (case insensitive), or `null` for "unknown", "shadow" and "stellar". */
        public fun fromApiName(apiName: String): PokeType? {
            val key = apiName.trim().lowercase()
            return entries.firstOrNull { it.apiName == key }
        }
    }
}
