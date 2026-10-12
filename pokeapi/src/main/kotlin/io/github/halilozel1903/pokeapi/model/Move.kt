package io.github.halilozel1903.pokeapi.model

import io.github.halilozel1903.pokeapi.types.PokeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /move/{id or name}`. Accuracy, power and PP are `null` for moves that have none. */
@Serializable
public data class Move(
    public val id: Int,
    public val name: String,
    public val accuracy: Int? = null,
    @SerialName("effect_chance") public val effectChance: Int? = null,
    public val pp: Int? = null,
    public val priority: Int = 0,
    public val power: Int? = null,
    @SerialName("damage_class") public val damageClass: NamedApiResource? = null,
    @SerialName("effect_entries") public val effectEntries: List<VerboseEffect> = emptyList(),
    @SerialName("flavor_text_entries") public val flavorTextEntries: List<FlavorText> = emptyList(),
    @SerialName("learned_by_pokemon") public val learnedByPokemon: List<NamedApiResource> = emptyList(),
    public val generation: NamedApiResource? = null,
    public val meta: MoveMeta? = null,
    public val names: List<Name> = emptyList(),
    @SerialName("stat_changes") public val statChanges: List<MoveStatChange> = emptyList(),
    public val target: NamedApiResource? = null,
    public val type: NamedApiResource? = null,
    @SerialName("contest_type") public val contestType: NamedApiResource? = null,
) {
    /** The move's type as a [PokeType]. */
    public val pokeType: PokeType? get() = type?.let { PokeType.fromApiName(it.name) }

    /**
     * The one line effect in [language], with `$effect_chance` filled in
     * ("Has a $effect_chance% chance to paralyze the target." becomes "... a 10% chance ...").
     */
    public fun shortEffect(language: String = "en"): String? =
        effectEntries.localized(language, short = true)?.replace("\$effect_chance", effectChance?.toString() ?: "")
}

@Serializable
public data class MoveMeta(
    public val ailment: NamedApiResource? = null,
    public val category: NamedApiResource? = null,
    @SerialName("min_hits") public val minHits: Int? = null,
    @SerialName("max_hits") public val maxHits: Int? = null,
    @SerialName("min_turns") public val minTurns: Int? = null,
    @SerialName("max_turns") public val maxTurns: Int? = null,
    public val drain: Int = 0,
    public val healing: Int = 0,
    @SerialName("crit_rate") public val critRate: Int = 0,
    @SerialName("ailment_chance") public val ailmentChance: Int = 0,
    @SerialName("flinch_chance") public val flinchChance: Int = 0,
    @SerialName("stat_chance") public val statChance: Int = 0,
)

@Serializable
public data class MoveStatChange(
    public val change: Int,
    public val stat: NamedApiResource,
)
