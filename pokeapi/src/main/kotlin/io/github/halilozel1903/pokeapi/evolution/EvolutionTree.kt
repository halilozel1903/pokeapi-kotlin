package io.github.halilozel1903.pokeapi.evolution

import io.github.halilozel1903.pokeapi.PokeApiNames
import io.github.halilozel1903.pokeapi.model.ChainLink
import io.github.halilozel1903.pokeapi.model.EvolutionChain
import io.github.halilozel1903.pokeapi.model.EvolutionDetail
import io.github.halilozel1903.pokeapi.model.NamedApiResource

/**
 * One species of an evolution chain, flattened out of the `evolves_to` tree.
 *
 * @property depth 0 for the base species, 1 for its evolutions, and so on.
 * @property parent the species it evolves from, `null` for the base species.
 * @property details every way to evolve into this species (several for Leafeon, Glaceon, ...).
 */
public data class EvolutionStage(
    public val species: NamedApiResource,
    public val depth: Int,
    public val parent: NamedApiResource?,
    public val details: List<EvolutionDetail>,
    public val isBaby: Boolean = false,
) {
    /** The species name, such as "vaporeon". */
    public val name: String get() = species.name

    /** The species id from its url. */
    public val id: Int? get() = species.id

    /** A short English description of how to reach this stage ("Level 16", "Use Water Stone"), `null` for the base. */
    public val condition: String? get() = details.describe()
}

/** Every species of the chain, depth first: Bulbasaur, Ivysaur, Venusaur; Eevee, Vaporeon, Jolteon, ... */
public fun ChainLink.flatten(): List<EvolutionStage> = buildList<EvolutionStage> {
    fun visit(link: ChainLink, depth: Int, parent: NamedApiResource?) {
        add(EvolutionStage(link.species, depth, parent, link.evolutionDetails, link.isBaby))
        for (next in link.evolvesTo) visit(next, depth + 1, link.species)
    }
    visit(this@flatten, 0, null)
}

/** Every species of the chain, depth first. */
public fun EvolutionChain.flatten(): List<EvolutionStage> = chain.flatten()

/** The stages grouped by depth: `[[eevee], [vaporeon, jolteon, flareon, ...]]`. */
public fun ChainLink.levels(): List<List<EvolutionStage>> =
    flatten().groupBy { it.depth }.toSortedMap().values.toList()

/** Every path from the base species to a final one: Eevee has eight, Bulbasaur one. */
public fun ChainLink.paths(): List<List<EvolutionStage>> {
    val result = mutableListOf<List<EvolutionStage>>()
    fun walk(link: ChainLink, depth: Int, parent: NamedApiResource?, prefix: List<EvolutionStage>) {
        val path = prefix + EvolutionStage(link.species, depth, parent, link.evolutionDetails, link.isBaby)
        if (link.evolvesTo.isEmpty()) result += path
        for (next in link.evolvesTo) walk(next, depth + 1, link.species, path)
    }
    walk(this, 0, null, emptyList())
    return result
}

/** `true` when no species in the chain branches (each evolves into at most one). */
public val ChainLink.isLinear: Boolean
    get() = evolvesTo.size <= 1 && evolvesTo.all { it.isLinear }

/** The link of species [name], or `null` when it is not in this chain. */
public fun ChainLink.find(name: String): ChainLink? {
    if (species.name == name) return this
    for (next in evolvesTo) next.find(name)?.let { return it }
    return null
}

/** The number of species in the chain. */
public val ChainLink.speciesCount: Int get() = 1 + evolvesTo.sumOf { it.speciesCount }

/**
 * A short English description of one evolution method:
 * "Level 16", "Use Thunder Stone", "Trade holding Metal Coat", "High friendship, day", "High affection, knows a Fairy move".
 */
public fun EvolutionDetail.describe(): String {
    val triggerName = this.trigger?.name.orEmpty()
    val conditions = buildList<String> {
        minHappiness?.let { add("high friendship") }
        minAffection?.let { add("high affection") }
        minBeauty?.let { add("high beauty") }
        if (timeOfDay.isNotBlank()) add(timeOfDay)
        knownMove?.let { add("knows ${PokeApiNames.prettify(it.name)}") }
        knownMoveType?.let { add("knows a ${PokeApiNames.prettify(it.name)} move") }
        if (triggerName != "trade") heldItem?.let { add("holding ${PokeApiNames.prettify(it.name)}") }
        location?.let { add("at ${PokeApiNames.prettify(it.name)}") }
        if (needsOverworldRain) add("in rain")
        partySpecies?.let { add("with ${PokeApiNames.prettify(it.name)} in party") }
        partyType?.let { add("with a ${PokeApiNames.prettify(it.name)} type in party") }
        when (relativePhysicalStats) {
            1 -> add("Attack > Defense")
            -1 -> add("Attack < Defense")
            0 -> add("Attack = Defense")
        }
        when (gender) {
            1 -> add("female")
            2 -> add("male")
        }
        if (turnUpsideDown) add("device upside down")
    }
    val base = when (triggerName) {
        "level-up" -> minLevel?.let { "Level $it" } ?: if (location != null || knownMove != null) "Level up" else null
        "use-item" -> item?.let { "Use ${PokeApiNames.prettify(it.name)}" } ?: "Use item"
        "trade" -> buildString {
            append("Trade")
            heldItem?.let { append(" holding ${PokeApiNames.prettify(it.name)}") }
            tradeSpecies?.let { append(" for ${PokeApiNames.prettify(it.name)}") }
        }
        "shed" -> "Level 20, empty party slot"
        "" -> null
        else -> PokeApiNames.prettify(triggerName)
    }
    val parts = listOfNotNull(base) + conditions
    if (parts.isEmpty()) return if (triggerName == "level-up") "Level up" else "Evolve"
    return parts.joinToString(", ").replaceFirstChar { it.uppercaseChar() }
}

/**
 * The best single description of how to reach a stage. Methods that need no specific location are preferred
 * (Leafeon reads "Use Leaf Stone", not one of its moss rock locations). `null` for an empty list.
 */
public fun List<EvolutionDetail>.describe(): String? {
    if (isEmpty()) return null
    val preferred = firstOrNull { it.location == null } ?: first()
    return preferred.describe()
}
