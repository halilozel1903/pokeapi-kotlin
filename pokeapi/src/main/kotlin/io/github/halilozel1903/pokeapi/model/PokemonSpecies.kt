package io.github.halilozel1903.pokeapi.model

import io.github.halilozel1903.pokeapi.PokeApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /pokemon-species/{id or name}`: names, genera, Pokédex texts and the link to the evolution chain. */
@Serializable
public data class PokemonSpecies(
    public val id: Int,
    public val name: String,
    public val order: Int = 0,
    @SerialName("gender_rate") public val genderRate: Int = -1,
    @SerialName("capture_rate") public val captureRate: Int = 0,
    @SerialName("base_happiness") public val baseHappiness: Int? = null,
    @SerialName("is_baby") public val isBaby: Boolean = false,
    @SerialName("is_legendary") public val isLegendary: Boolean = false,
    @SerialName("is_mythical") public val isMythical: Boolean = false,
    @SerialName("hatch_counter") public val hatchCounter: Int? = null,
    @SerialName("has_gender_differences") public val hasGenderDifferences: Boolean = false,
    @SerialName("forms_switchable") public val formsSwitchable: Boolean = false,
    @SerialName("growth_rate") public val growthRate: NamedApiResource? = null,
    @SerialName("pokedex_numbers") public val pokedexNumbers: List<PokemonSpeciesDexEntry> = emptyList(),
    @SerialName("egg_groups") public val eggGroups: List<NamedApiResource> = emptyList(),
    public val color: NamedApiResource? = null,
    public val shape: NamedApiResource? = null,
    @SerialName("evolves_from_species") public val evolvesFromSpecies: NamedApiResource? = null,
    @SerialName("evolution_chain") public val evolutionChain: ApiResource? = null,
    public val habitat: NamedApiResource? = null,
    public val generation: NamedApiResource? = null,
    public val names: List<Name> = emptyList(),
    @SerialName("flavor_text_entries") public val flavorTextEntries: List<FlavorText> = emptyList(),
    @SerialName("form_descriptions") public val formDescriptions: List<Description> = emptyList(),
    public val genera: List<Genus> = emptyList(),
    public val varieties: List<PokemonSpeciesVariety> = emptyList(),
) {
    /** The name in [language] (a PokeAPI language name such as "en", "ja-Hrkt", "fr"), or a prettified [name]. */
    public fun localizedName(language: String = "en"): String =
        names.localized(language) ?: PokeApiNames.prettify(name)

    /** The genus in [language], such as "Seed Pokémon". */
    public fun genus(language: String = "en"): String? = genera.firstOrNull { it.language.name == language }?.genus

    /**
     * A cleaned Pokédex entry in [language]: from [version] when given (such as "red" or "scarlet"),
     * otherwise the newest one.
     */
    public fun flavorText(language: String = "en", version: String? = null): String? =
        flavorTextEntries.latest(language, version)

    /** The share of females from 0.0 to 1.0, or `null` for genderless species. */
    public val femaleRatio: Double? get() = if (genderRate < 0) null else genderRate / 8.0

    /** The evolution chain id, ready for `client.evolutionChain(id)`. */
    public val evolutionChainId: Int? get() = evolutionChain?.id
}

@Serializable
public data class PokemonSpeciesDexEntry(
    @SerialName("entry_number") public val entryNumber: Int,
    public val pokedex: NamedApiResource,
)

@Serializable
public data class PokemonSpeciesVariety(
    @SerialName("is_default") public val isDefault: Boolean = false,
    public val pokemon: NamedApiResource,
)
