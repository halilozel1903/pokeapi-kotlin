package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /generation/{id or name}`: what each generation introduced. */
@Serializable
public data class Generation(
    public val id: Int,
    public val name: String,
    public val abilities: List<NamedApiResource> = emptyList(),
    public val names: List<Name> = emptyList(),
    @SerialName("main_region") public val mainRegion: NamedApiResource? = null,
    public val moves: List<NamedApiResource> = emptyList(),
    @SerialName("pokemon_species") public val pokemonSpecies: List<NamedApiResource> = emptyList(),
    public val types: List<NamedApiResource> = emptyList(),
    @SerialName("version_groups") public val versionGroups: List<NamedApiResource> = emptyList(),
)
