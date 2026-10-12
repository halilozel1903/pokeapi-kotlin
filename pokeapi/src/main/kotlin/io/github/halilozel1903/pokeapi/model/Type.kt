package io.github.halilozel1903.pokeapi.model

import io.github.halilozel1903.pokeapi.types.PokeType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /type/{id or name}`: damage relations and every Pokémon and move of the type. */
@Serializable
public data class Type(
    public val id: Int,
    public val name: String,
    @SerialName("damage_relations") public val damageRelations: TypeRelations = TypeRelations(),
    @SerialName("past_damage_relations") public val pastDamageRelations: List<TypeRelationsPast> = emptyList(),
    @SerialName("game_indices") public val gameIndices: List<GenerationGameIndex> = emptyList(),
    public val generation: NamedApiResource? = null,
    @SerialName("move_damage_class") public val moveDamageClass: NamedApiResource? = null,
    public val names: List<Name> = emptyList(),
    public val pokemon: List<TypePokemon> = emptyList(),
    public val moves: List<NamedApiResource> = emptyList(),
) {
    /** This type as a [PokeType], or `null` for "unknown", "shadow" and "stellar". */
    public val pokeType: PokeType? get() = PokeType.fromApiName(name)
}

/** Which types this type hits or is hit by for double, half or no damage. */
@Serializable
public data class TypeRelations(
    @SerialName("no_damage_to") public val noDamageTo: List<NamedApiResource> = emptyList(),
    @SerialName("half_damage_to") public val halfDamageTo: List<NamedApiResource> = emptyList(),
    @SerialName("double_damage_to") public val doubleDamageTo: List<NamedApiResource> = emptyList(),
    @SerialName("no_damage_from") public val noDamageFrom: List<NamedApiResource> = emptyList(),
    @SerialName("half_damage_from") public val halfDamageFrom: List<NamedApiResource> = emptyList(),
    @SerialName("double_damage_from") public val doubleDamageFrom: List<NamedApiResource> = emptyList(),
)

@Serializable
public data class TypeRelationsPast(
    public val generation: NamedApiResource,
    @SerialName("damage_relations") public val damageRelations: TypeRelations,
)

@Serializable
public data class TypePokemon(
    public val slot: Int,
    public val pokemon: NamedApiResource,
)
