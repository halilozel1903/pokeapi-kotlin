package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /ability/{id or name}`. */
@Serializable
public data class Ability(
    public val id: Int,
    public val name: String,
    @SerialName("is_main_series") public val isMainSeries: Boolean = true,
    public val generation: NamedApiResource? = null,
    public val names: List<Name> = emptyList(),
    @SerialName("effect_entries") public val effectEntries: List<VerboseEffect> = emptyList(),
    @SerialName("effect_changes") public val effectChanges: List<AbilityEffectChange> = emptyList(),
    @SerialName("flavor_text_entries") public val flavorTextEntries: List<FlavorText> = emptyList(),
    public val pokemon: List<AbilityPokemon> = emptyList(),
) {
    /** The one line effect in [language]. */
    public fun shortEffect(language: String = "en"): String? = effectEntries.localized(language, short = true)
}

@Serializable
public data class AbilityEffectChange(
    @SerialName("effect_entries") public val effectEntries: List<Effect> = emptyList(),
    @SerialName("version_group") public val versionGroup: NamedApiResource,
)

@Serializable
public data class AbilityPokemon(
    @SerialName("is_hidden") public val isHidden: Boolean = false,
    public val slot: Int,
    public val pokemon: NamedApiResource,
)
