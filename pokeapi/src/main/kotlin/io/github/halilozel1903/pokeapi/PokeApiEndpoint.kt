package io.github.halilozel1903.pokeapi

import kotlinx.serialization.KSerializer
import io.github.halilozel1903.pokeapi.model.Ability as AbilityModel
import io.github.halilozel1903.pokeapi.model.Berry as BerryModel
import io.github.halilozel1903.pokeapi.model.EvolutionChain as EvolutionChainModel
import io.github.halilozel1903.pokeapi.model.Generation as GenerationModel
import io.github.halilozel1903.pokeapi.model.Item as ItemModel
import io.github.halilozel1903.pokeapi.model.Move as MoveModel
import io.github.halilozel1903.pokeapi.model.Pokedex as PokedexModel
import io.github.halilozel1903.pokeapi.model.Pokemon as PokemonModel
import io.github.halilozel1903.pokeapi.model.PokemonSpecies as PokemonSpeciesModel
import io.github.halilozel1903.pokeapi.model.Type as TypeModel

/**
 * A PokeAPI endpoint and the model it returns. Use the predefined ones with [PokeApiClient.get],
 * [PokeApiClient.list] and [PokeApiClient.pages]:
 *
 * ```kotlin
 * client.pages(PokeApiEndpoint.Berry, pageSize = 50).collect { page -> ... }
 * ```
 */
public class PokeApiEndpoint<T> internal constructor(
    /** The path segment, such as "pokemon-species". */
    public val path: String,
    internal val serializer: KSerializer<T>,
) {
    override fun toString(): String = "PokeApiEndpoint($path)"

    public companion object {
        public val Pokemon: PokeApiEndpoint<PokemonModel> = PokeApiEndpoint("pokemon", PokemonModel.serializer())
        public val PokemonSpecies: PokeApiEndpoint<PokemonSpeciesModel> =
            PokeApiEndpoint("pokemon-species", PokemonSpeciesModel.serializer())
        public val EvolutionChain: PokeApiEndpoint<EvolutionChainModel> =
            PokeApiEndpoint("evolution-chain", EvolutionChainModel.serializer())
        public val Type: PokeApiEndpoint<TypeModel> = PokeApiEndpoint("type", TypeModel.serializer())
        public val Ability: PokeApiEndpoint<AbilityModel> = PokeApiEndpoint("ability", AbilityModel.serializer())
        public val Move: PokeApiEndpoint<MoveModel> = PokeApiEndpoint("move", MoveModel.serializer())
        public val Item: PokeApiEndpoint<ItemModel> = PokeApiEndpoint("item", ItemModel.serializer())
        public val Generation: PokeApiEndpoint<GenerationModel> = PokeApiEndpoint("generation", GenerationModel.serializer())
        public val Pokedex: PokeApiEndpoint<PokedexModel> = PokeApiEndpoint("pokedex", PokedexModel.serializer())
        public val Berry: PokeApiEndpoint<BerryModel> = PokeApiEndpoint("berry", BerryModel.serializer())

        /** Every predefined endpoint. */
        public val entries: List<PokeApiEndpoint<*>> by lazy {
            listOf(Pokemon, PokemonSpecies, EvolutionChain, Type, Ability, Move, Item, Generation, Pokedex, Berry)
        }
    }
}
