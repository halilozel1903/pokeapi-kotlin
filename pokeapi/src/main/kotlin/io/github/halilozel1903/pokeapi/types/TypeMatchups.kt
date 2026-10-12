package io.github.halilozel1903.pokeapi.types

import io.github.halilozel1903.pokeapi.model.Type
import io.github.halilozel1903.pokeapi.model.TypeRelations

/**
 * The damage multiplier of each attacking type against one defender, from [TypeChart.defending] or from
 * PokeAPI's own data with [fromTypes].
 */
public data class TypeMatchups(public val multipliers: Map<PokeType, Double>) {

    /** The multiplier of [attacking]; 1.0 when it is not in [multipliers]. */
    public fun multiplier(attacking: PokeType): Double = multipliers[attacking] ?: 1.0

    /** Types that do more than normal damage, strongest first. */
    public val weaknesses: List<PokeType> get() = filter { it > 1.0 }.sortedByDescending { multiplier(it) }

    /** Types that do 4x damage. */
    public val quadrupleWeaknesses: List<PokeType> get() = filter { it >= 4.0 }

    /** Types that do 2x damage. */
    public val doubleWeaknesses: List<PokeType> get() = filter { it > 1.0 && it < 4.0 }

    /** Types that do less than normal damage (but some), weakest last. */
    public val resistances: List<PokeType> get() = filter { it > 0.0 && it < 1.0 }.sortedByDescending { multiplier(it) }

    /** Types that do no damage. */
    public val immunities: List<PokeType> get() = filter { it == 0.0 }

    /** Types that do normal damage. */
    public val neutral: List<PokeType> get() = filter { it == 1.0 }

    private inline fun filter(predicate: (Double) -> Boolean): List<PokeType> =
        PokeType.entries.filter { predicate(multiplier(it)) }

    public companion object {
        /**
         * Defensive matchups from PokeAPI damage relations, one per type of the defender
         * (`double_damage_from` x2, `half_damage_from` x0.5, `no_damage_from` x0).
         */
        public fun fromDamageRelations(relations: Collection<TypeRelations>): TypeMatchups {
            val result = PokeType.entries.associateWith { 1.0 }.toMutableMap()
            for (relation in relations) {
                fun apply(types: List<io.github.halilozel1903.pokeapi.model.NamedApiResource>, factor: Double) {
                    for (resource in types) {
                        val type = PokeType.fromApiName(resource.name) ?: continue
                        result[type] = (result[type] ?: 1.0) * factor
                    }
                }
                apply(relation.doubleDamageFrom, 2.0)
                apply(relation.halfDamageFrom, 0.5)
                apply(relation.noDamageFrom, 0.0)
            }
            return TypeMatchups(result.toMap())
        }

        /** Defensive matchups of a Pokémon with the given PokeAPI [types] (one or two). */
        public fun fromTypes(types: Collection<Type>): TypeMatchups =
            fromDamageRelations(types.map { it.damageRelations })
    }
}
