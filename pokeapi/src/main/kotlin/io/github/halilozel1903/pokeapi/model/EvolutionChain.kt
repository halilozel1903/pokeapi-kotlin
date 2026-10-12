package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /evolution-chain/{id}`. [chain] is the base species; each link's `evolves_to` holds the next stages,
 * so branching families (Eevee) have several. See `io.github.halilozel1903.pokeapi.evolution` for helpers.
 */
@Serializable
public data class EvolutionChain(
    public val id: Int,
    @SerialName("baby_trigger_item") public val babyTriggerItem: NamedApiResource? = null,
    public val chain: ChainLink,
)

/** One species in an evolution chain, with how it evolves from the previous one and what it evolves into. */
@Serializable
public data class ChainLink(
    @SerialName("is_baby") public val isBaby: Boolean = false,
    public val species: NamedApiResource,
    @SerialName("evolution_details") public val evolutionDetails: List<EvolutionDetail> = emptyList(),
    @SerialName("evolves_to") public val evolvesTo: List<ChainLink> = emptyList(),
)

/** One way to reach a species. Every condition is optional; `trigger` says what starts the evolution. */
@Serializable
public data class EvolutionDetail(
    public val item: NamedApiResource? = null,
    public val trigger: NamedApiResource? = null,
    public val gender: Int? = null,
    @SerialName("held_item") public val heldItem: NamedApiResource? = null,
    @SerialName("known_move") public val knownMove: NamedApiResource? = null,
    @SerialName("known_move_type") public val knownMoveType: NamedApiResource? = null,
    public val location: NamedApiResource? = null,
    @SerialName("min_level") public val minLevel: Int? = null,
    @SerialName("min_happiness") public val minHappiness: Int? = null,
    @SerialName("min_beauty") public val minBeauty: Int? = null,
    @SerialName("min_affection") public val minAffection: Int? = null,
    @SerialName("needs_overworld_rain") public val needsOverworldRain: Boolean = false,
    @SerialName("party_species") public val partySpecies: NamedApiResource? = null,
    @SerialName("party_type") public val partyType: NamedApiResource? = null,
    @SerialName("relative_physical_stats") public val relativePhysicalStats: Int? = null,
    @SerialName("time_of_day") public val timeOfDay: String = "",
    @SerialName("trade_species") public val tradeSpecies: NamedApiResource? = null,
    @SerialName("turn_upside_down") public val turnUpsideDown: Boolean = false,
)
