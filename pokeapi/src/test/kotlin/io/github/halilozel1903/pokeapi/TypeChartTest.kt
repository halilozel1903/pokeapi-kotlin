package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.model.Type
import io.github.halilozel1903.pokeapi.types.PokeType
import io.github.halilozel1903.pokeapi.types.PokeType.Bug
import io.github.halilozel1903.pokeapi.types.PokeType.Dark
import io.github.halilozel1903.pokeapi.types.PokeType.Psychic
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
import io.github.halilozel1903.pokeapi.types.PokeType.Rock
import io.github.halilozel1903.pokeapi.types.PokeType.Steel
import io.github.halilozel1903.pokeapi.types.PokeType.Water
import io.github.halilozel1903.pokeapi.types.TypeChart
import io.github.halilozel1903.pokeapi.types.TypeMatchups
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TypeChartTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun type(name: String): Type = json.decodeFromString(fixture("type/$name.json")!!)

    @Test
    fun singleTypes() {
        assertEquals(2.0, TypeChart.effectiveness(Water, Fire))
        assertEquals(0.5, TypeChart.effectiveness(Fire, Water))
        assertEquals(0.0, TypeChart.effectiveness(Electric, Ground))
        assertEquals(0.0, TypeChart.effectiveness(Normal, Ghost))
        assertEquals(0.0, TypeChart.effectiveness(Dragon, Fairy))
        assertEquals(1.0, TypeChart.effectiveness(Normal, Normal))
        assertEquals(2.0, TypeChart.effectiveness(Fairy, Dragon))
    }

    @Test
    fun dualTypesMultiply() {
        assertEquals(4.0, TypeChart.effectiveness(Rock, listOf(Fire, Flying)))
        assertEquals(0.0, TypeChart.effectiveness(Ground, listOf(Fire, Flying)))
        assertEquals(4.0, TypeChart.effectiveness(Grass, listOf(Water, Ground)))
        assertEquals(0.25, TypeChart.effectiveness(Grass, listOf(Fire, Flying)))
        assertEquals(1.0, TypeChart.effectiveness(Water, listOf(Water, Ground)))
        assertEquals(0.0, TypeChart.effectiveness(Fighting, listOf(Ghost, Poison)))
        // The same type twice counts once.
        assertEquals(2.0, TypeChart.effectiveness(Water, listOf(Fire, Fire)))
    }

    @Test
    fun charizardMatchups() {
        val matchups = TypeChart.defending(Fire, Flying)
        assertEquals(listOf(Rock), matchups.quadrupleWeaknesses)
        assertEquals(listOf(Water, Electric), matchups.doubleWeaknesses)
        assertEquals(listOf(Rock, Water, Electric), matchups.weaknesses)
        assertEquals(listOf(Ground), matchups.immunities)
        assertEquals(listOf(Fire, Fighting, Steel, Fairy, Grass, Bug), matchups.resistances)
        assertEquals(0.25, matchups.multiplier(Bug))
        assertEquals(18, matchups.multipliers.size)
    }

    @Test
    fun staticChartMatchesPokeApiDamageRelations() {
        val types = PokeType.entries.associateWith { type(it.apiName) }
        for ((pokeType, type) in types) {
            assertEquals(pokeType, type.pokeType)
            assertEquals(TypeChart.defending(pokeType), TypeMatchups.fromTypes(listOf(type)), "defending $pokeType")
            val attacking = TypeChart.attacking(pokeType)
            assertEquals(attacking.filterValues { it == 2.0 }.keys, type.damageRelations.doubleDamageTo.mapNotNull { PokeType.fromApiName(it.name) }.toSet())
            assertEquals(attacking.filterValues { it == 0.5 }.keys, type.damageRelations.halfDamageTo.mapNotNull { PokeType.fromApiName(it.name) }.toSet())
            assertEquals(attacking.filterValues { it == 0.0 }.keys, type.damageRelations.noDamageTo.mapNotNull { PokeType.fromApiName(it.name) }.toSet())
        }
        for (first in PokeType.entries) for (second in PokeType.entries) {
            if (first == second) continue
            assertEquals(
                TypeChart.defending(first, second),
                TypeMatchups.fromTypes(listOf(types.getValue(first), types.getValue(second))),
                "defending $first/$second",
            )
        }
    }

    @Test
    fun coverage() {
        val coverage = TypeChart.coverage(listOf(Ice, Electric))
        assertEquals(2.0, coverage[Dragon])
        assertEquals(2.0, coverage[Water])
        assertEquals(2.0, coverage[Ground])
        assertEquals(1.0, coverage[Steel])
        val gaps = coverage.filterValues { it <= 1.0 }.keys
        assertEquals(setOf(Normal, Fire, Ice, Fighting, Poison, Rock, Ghost, Dark, Steel, Fairy, Psychic, Bug, Electric), gaps)
    }

    @Test
    fun parsesApiNames() {
        assertEquals(PokeType.Psychic, PokeType.fromApiName("Psychic"))
        assertNull(PokeType.fromApiName("stellar"))
        assertNull(PokeType.fromApiName("unknown"))
    }
}
