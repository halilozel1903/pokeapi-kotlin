package io.github.halilozel1903.pokeapi

import io.github.halilozel1903.pokeapi.evolution.describe
import io.github.halilozel1903.pokeapi.evolution.find
import io.github.halilozel1903.pokeapi.evolution.flatten
import io.github.halilozel1903.pokeapi.evolution.isLinear
import io.github.halilozel1903.pokeapi.evolution.levels
import io.github.halilozel1903.pokeapi.evolution.paths
import io.github.halilozel1903.pokeapi.evolution.speciesCount
import io.github.halilozel1903.pokeapi.model.EvolutionChain
import io.github.halilozel1903.pokeapi.model.EvolutionDetail
import io.github.halilozel1903.pokeapi.model.NamedApiResource
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvolutionTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun chain(id: Int): EvolutionChain = json.decodeFromString(fixture("evolution-chain/$id.json")!!)

    @Test
    fun linearChain() {
        val bulbasaur = chain(1)
        assertTrue(bulbasaur.chain.isLinear)
        assertEquals(3, bulbasaur.chain.speciesCount)
        val stages = bulbasaur.flatten()
        assertEquals(listOf("bulbasaur", "ivysaur", "venusaur"), stages.map { it.name })
        assertEquals(listOf(0, 1, 2), stages.map { it.depth })
        assertEquals(listOf(null, "bulbasaur", "ivysaur"), stages.map { it.parent?.name })
        assertEquals(listOf(null, "Level 16", "Level 32"), stages.map { it.condition })
        assertEquals(listOf(1, 2, 3), stages.map { it.id })
        assertEquals(1, bulbasaur.chain.paths().size)
    }

    @Test
    fun branchingChain() {
        val eevee = chain(67).chain
        assertFalse(eevee.isLinear)
        assertEquals(9, eevee.speciesCount)
        val levels = eevee.levels()
        assertEquals(listOf(1, 8), levels.map { it.size })
        val paths = eevee.paths()
        assertEquals(8, paths.size)
        assertTrue(paths.all { it.size == 2 && it.first().name == "eevee" })
        assertEquals("sylveon", paths.last().last().name)

        val conditions = levels[1].associate { it.name to it.condition }
        assertEquals("Use Water Stone", conditions["vaporeon"])
        assertEquals("Use Thunder Stone", conditions["jolteon"])
        assertEquals("Use Fire Stone", conditions["flareon"])
        assertEquals("High friendship, day", conditions["espeon"])
        assertEquals("High friendship, night", conditions["umbreon"])
        assertEquals("Use Leaf Stone", conditions["leafeon"])
        assertEquals("Use Ice Stone", conditions["glaceon"])
        assertEquals("High affection, knows a Fairy move", conditions["sylveon"])

        val leafeon = assertNotNull(eevee.find("leafeon"))
        assertEquals("Level up, at Eterna Forest", leafeon.evolutionDetails.first().describe())
        assertNull(eevee.find("pikachu"))
        assertEquals(700, eevee.find("sylveon")?.species?.id)
    }

    @Test
    fun tradeWithHeldItem() {
        val steelix = chain(43).chain.evolvesTo.single()
        assertEquals("Trade holding Metal Coat", steelix.evolutionDetails.describe())
    }

    @Test
    fun describesOtherMethods() {
        fun trigger(name: String) = NamedApiResource(name, "https://pokeapi.co/api/v2/evolution-trigger/1/")
        fun res(name: String) = NamedApiResource(name, "https://pokeapi.co/api/v2/x/1/")
        assertEquals("Level up", EvolutionDetail(trigger = trigger("level-up")).describe())
        assertEquals("Level 30, night", EvolutionDetail(trigger = trigger("level-up"), minLevel = 30, timeOfDay = "night").describe())
        assertEquals("Level 20, Attack > Defense", EvolutionDetail(trigger = trigger("level-up"), minLevel = 20, relativePhysicalStats = 1).describe())
        assertEquals("Trade for Shelmet", EvolutionDetail(trigger = trigger("trade"), tradeSpecies = res("shelmet")).describe())
        assertEquals("Level 50, in rain", EvolutionDetail(trigger = trigger("level-up"), minLevel = 50, needsOverworldRain = true).describe())
        assertEquals("Level up, knows Ancient Power", EvolutionDetail(trigger = trigger("level-up"), knownMove = res("ancient-power")).describe())
        assertEquals("Spin", EvolutionDetail(trigger = trigger("spin")).describe())
        assertNull(emptyList<EvolutionDetail>().describe())
    }
}
