package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /pokedex/{id or name}`: a regional Pokédex with its entry numbers ("kanto", "national", "paldea"). */
@Serializable
public data class Pokedex(
    public val id: Int,
    public val name: String,
    @SerialName("is_main_series") public val isMainSeries: Boolean = true,
    public val descriptions: List<Description> = emptyList(),
    public val names: List<Name> = emptyList(),
    @SerialName("pokemon_entries") public val pokemonEntries: List<PokemonEntry> = emptyList(),
    public val region: NamedApiResource? = null,
    @SerialName("version_groups") public val versionGroups: List<NamedApiResource> = emptyList(),
)

@Serializable
public data class PokemonEntry(
    @SerialName("entry_number") public val entryNumber: Int,
    @SerialName("pokemon_species") public val pokemonSpecies: NamedApiResource,
)
