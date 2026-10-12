package io.github.halilozel1903.pokeapi.types

import io.github.halilozel1903.pokeapi.types.PokeType.Bug
import io.github.halilozel1903.pokeapi.types.PokeType.Dark
import io.github.halilozel1903.pokeapi.types.PokeType.Dragon
import io.github.halilozel1903.pokeapi.types.PokeType.Electric
import io.github.halilozel1903.pokeapi.types.PokeType.Fairy
import io.github.halilozel1903.pokeapi.types.PokeType.Fighting
import io.github.halilozel1903.pokeapi.types.PokeType.Fire
import io.github.halilozel1903.pokeapi.types.PokeType.Flying
import io.github.halilozel1903.pokeapi.types.PokeType.Ghost
import io.github.halilozel1903.pokeapi.types.PokeType.Grass
import io.github.halilozel1903.pokeapi.types.PokeType.Ground
import io.github.halilozel1903.pokeapi.types.PokeType.Ice
import io.github.halilozel1903.pokeapi.types.PokeType.Normal
import io.github.halilozel1903.pokeapi.types.PokeType.Poison
import io.github.halilozel1903.pokeapi.types.PokeType.Psychic
import io.github.halilozel1903.pokeapi.types.PokeType.Rock
import io.github.halilozel1903.pokeapi.types.PokeType.Steel
import io.github.halilozel1903.pokeapi.types.PokeType.Water

/**
 * The type effectiveness chart of the current games (generation 6 and later), built in so matchups need no
 * network request. For dual types the multipliers are multiplied: Rock against Fire/Flying is 2 x 2 = 4.
 *
 * ```kotlin
 * TypeChart.effectiveness(PokeType.Rock, listOf(PokeType.Fire, PokeType.Flying))   // 4.0
 * TypeChart.defending(PokeType.Water, PokeType.Ground).quadrupleWeaknesses          // [Grass]
 * ```
 *
 * Abilities (Levitate, Flash Fire, ...) and Tera types are not taken into account.
 */
public object TypeChart {
    private val size = PokeType.entries.size
    private val table: Array<DoubleArray> = Array(size) { DoubleArray(size) { 1.0 } }

    init {
        fun set(attacking: PokeType, multiplier: Double, vararg defending: PokeType) {
            for (d in defending) table[attacking.ordinal][d.ordinal] = multiplier
        }
        set(Normal, 0.5, Rock, Steel)
        set(Normal, 0.0, Ghost)
        set(Fire, 2.0, Grass, Ice, Bug, Steel)
        set(Fire, 0.5, Fire, Water, Rock, Dragon)
        set(Water, 2.0, Fire, Ground, Rock)
        set(Water, 0.5, Water, Grass, Dragon)
        set(Electric, 2.0, Water, Flying)
        set(Electric, 0.5, Electric, Grass, Dragon)
        set(Electric, 0.0, Ground)
        set(Grass, 2.0, Water, Ground, Rock)
        set(Grass, 0.5, Fire, Grass, Poison, Flying, Bug, Dragon, Steel)
        set(Ice, 2.0, Grass, Ground, Flying, Dragon)
        set(Ice, 0.5, Fire, Water, Ice, Steel)
        set(Fighting, 2.0, Normal, Ice, Rock, Dark, Steel)
        set(Fighting, 0.5, Poison, Flying, Psychic, Bug, Fairy)
        set(Fighting, 0.0, Ghost)
        set(Poison, 2.0, Grass, Fairy)
        set(Poison, 0.5, Poison, Ground, Rock, Ghost)
        set(Poison, 0.0, Steel)
        set(Ground, 2.0, Fire, Electric, Poison, Rock, Steel)
        set(Ground, 0.5, Grass, Bug)
        set(Ground, 0.0, Flying)
        set(Flying, 2.0, Grass, Fighting, Bug)
        set(Flying, 0.5, Electric, Rock, Steel)
        set(Psychic, 2.0, Fighting, Poison)
        set(Psychic, 0.5, Psychic, Steel)
        set(Psychic, 0.0, Dark)
        set(Bug, 2.0, Grass, Psychic, Dark)
        set(Bug, 0.5, Fire, Fighting, Poison, Flying, Ghost, Steel, Fairy)
        set(Rock, 2.0, Fire, Ice, Flying, Bug)
        set(Rock, 0.5, Fighting, Ground, Steel)
        set(Ghost, 2.0, Psychic, Ghost)
        set(Ghost, 0.5, Dark)
        set(Ghost, 0.0, Normal)
        set(Dragon, 2.0, Dragon)
        set(Dragon, 0.5, Steel)
        set(Dragon, 0.0, Fairy)
        set(Dark, 2.0, Psychic, Ghost)
        set(Dark, 0.5, Fighting, Dark, Fairy)
        set(Steel, 2.0, Ice, Rock, Fairy)
        set(Steel, 0.5, Fire, Water, Electric, Steel)
        set(Fairy, 2.0, Fighting, Dragon, Dark)
        set(Fairy, 0.5, Fire, Poison, Steel)
    }

    /** The multiplier of an [attacking] move against a single [defending] type: 0, 0.5, 1 or 2. */
    public fun effectiveness(attacking: PokeType, defending: PokeType): Double =
        table[attacking.ordinal][defending.ordinal]

    /** The multiplier against a Pokémon with one or two [defending] types: 0, 0.25, 0.5, 1, 2 or 4. */
    public fun effectiveness(attacking: PokeType, defending: Collection<PokeType>): Double =
        defending.distinct().fold(1.0) { acc, type -> acc * effectiveness(attacking, type) }

    /** How every attacking type fares against a Pokémon with [types]. */
    public fun defending(types: Collection<PokeType>): TypeMatchups =
        TypeMatchups(PokeType.entries.associateWith { effectiveness(it, types) })

    /** How every attacking type fares against a Pokémon with [types]. */
    public fun defending(vararg types: PokeType): TypeMatchups = defending(types.toList())

    /** The multiplier of [attacking] against each single type. */
    public fun attacking(attacking: PokeType): Map<PokeType, Double> =
        PokeType.entries.associateWith { effectiveness(attacking, it) }

    /**
     * Offensive coverage of a move set: for each defending type, the best multiplier any of [moveTypes] reaches.
     * Types mapped to 1.0 or less are the gaps in the coverage.
     */
    public fun coverage(moveTypes: Collection<PokeType>): Map<PokeType, Double> =
        PokeType.entries.associateWith { defending ->
            moveTypes.maxOfOrNull { effectiveness(it, defending) } ?: 1.0
        }
}
