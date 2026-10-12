package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /item/{id or name}`. */
@Serializable
public data class Item(
    public val id: Int,
    public val name: String,
    public val cost: Int = 0,
    @SerialName("fling_power") public val flingPower: Int? = null,
    @SerialName("fling_effect") public val flingEffect: NamedApiResource? = null,
    public val attributes: List<NamedApiResource> = emptyList(),
    public val category: NamedApiResource? = null,
    @SerialName("effect_entries") public val effectEntries: List<VerboseEffect> = emptyList(),
    @SerialName("flavor_text_entries") public val flavorTextEntries: List<VersionGroupFlavorText> = emptyList(),
    public val names: List<Name> = emptyList(),
    public val sprites: ItemSprites? = null,
    @SerialName("held_by_pokemon") public val heldByPokemon: List<ItemHolderPokemon> = emptyList(),
    @SerialName("baby_trigger_for") public val babyTriggerFor: ApiResource? = null,
    @SerialName("game_indices") public val gameIndices: List<GenerationGameIndex> = emptyList(),
) {
    /** The one line effect in [language]. */
    public fun shortEffect(language: String = "en"): String? = effectEntries.localized(language, short = true)
}

@Serializable
public data class ItemSprites(
    @SerialName("default") public val defaultSprite: String? = null,
)

@Serializable
public data class ItemHolderPokemon(
    public val pokemon: NamedApiResource,
    @SerialName("version_details") public val versionDetails: List<ItemHolderPokemonVersionDetail> = emptyList(),
)

@Serializable
public data class ItemHolderPokemonVersionDetail(
    public val rarity: Int,
    public val version: NamedApiResource,
)
