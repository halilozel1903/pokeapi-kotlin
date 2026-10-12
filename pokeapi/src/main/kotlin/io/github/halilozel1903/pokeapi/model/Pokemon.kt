package io.github.halilozel1903.pokeapi.model

import io.github.halilozel1903.pokeapi.PokeApiNames
import io.github.halilozel1903.pokeapi.types.PokeType
import io.github.halilozel1903.pokeapi.types.TypeChart
import io.github.halilozel1903.pokeapi.types.TypeMatchups
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /pokemon/{id or name}`. Height is in decimetres and weight in hectograms, as PokeAPI returns them. */
@Serializable
public data class Pokemon(
    public val id: Int,
    public val name: String,
    @SerialName("base_experience") public val baseExperience: Int? = null,
    public val height: Int = 0,
    public val weight: Int = 0,
    @SerialName("is_default") public val isDefault: Boolean = true,
    public val order: Int = 0,
    public val abilities: List<PokemonAbility> = emptyList(),
    public val forms: List<NamedApiResource> = emptyList(),
    @SerialName("game_indices") public val gameIndices: List<VersionGameIndex> = emptyList(),
    @SerialName("held_items") public val heldItems: List<PokemonHeldItem> = emptyList(),
    @SerialName("location_area_encounters") public val locationAreaEncounters: String? = null,
    public val moves: List<PokemonMove> = emptyList(),
    @SerialName("past_types") public val pastTypes: List<PokemonTypePast> = emptyList(),
    public val sprites: PokemonSprites = PokemonSprites(),
    public val cries: PokemonCries? = null,
    public val species: NamedApiResource,
    public val stats: List<PokemonStat> = emptyList(),
    public val types: List<PokemonTypeSlot> = emptyList(),
) {
    /** The official artwork (`sprites.other["official-artwork"].front_default`), or the default sprite. */
    public val officialArtworkUrl: String?
        get() = sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault

    /** Height in metres. */
    public val heightMeters: Double get() = height / 10.0

    /** Weight in kilograms. */
    public val weightKilograms: Double get() = weight / 10.0

    /** The types in slot order, skipping types this library does not know (such as "stellar"). */
    public val pokeTypes: List<PokeType>
        get() = types.sortedBy { it.slot }.mapNotNull { PokeType.fromApiName(it.type.name) }

    /** The base value of [stat], or `null` when PokeAPI did not return it. */
    public fun baseStat(stat: BaseStat): Int? = stats.firstOrNull { it.stat.name == stat.apiName }?.baseStat

    /** The sum of all base stats. */
    public val baseStatTotal: Int get() = stats.sumOf { it.baseStat }

    /** How much damage each attacking type does to this Pokémon, from its current types. */
    public fun defensiveMatchups(): TypeMatchups = TypeChart.defending(pokeTypes)

    /** A display name such as "Mr. Mime". Prefer [PokemonSpecies.localizedName] when you have the species. */
    public val displayName: String get() = PokeApiNames.prettify(name)

    /** "#0006". */
    public val dexNumber: String get() = PokeApiNames.dexNumber(id)
}

/** The six base stats, with PokeAPI's names. */
public enum class BaseStat(public val apiName: String, public val shortLabel: String, public val label: String) {
    Hp("hp", "HP", "HP"),
    Attack("attack", "Atk", "Attack"),
    Defense("defense", "Def", "Defense"),
    SpecialAttack("special-attack", "SpA", "Sp. Atk"),
    SpecialDefense("special-defense", "SpD", "Sp. Def"),
    Speed("speed", "Spe", "Speed"),
    ;

    public companion object {
        /** The stat with PokeAPI name [apiName], or `null` (for example "accuracy"). */
        public fun fromApiName(apiName: String): BaseStat? = entries.firstOrNull { it.apiName == apiName }
    }
}

@Serializable
public data class PokemonAbility(
    @SerialName("is_hidden") public val isHidden: Boolean = false,
    public val slot: Int,
    public val ability: NamedApiResource,
)

@Serializable
public data class PokemonHeldItem(
    public val item: NamedApiResource,
    @SerialName("version_details") public val versionDetails: List<PokemonHeldItemVersion> = emptyList(),
)

@Serializable
public data class PokemonHeldItemVersion(
    public val version: NamedApiResource,
    public val rarity: Int,
)

@Serializable
public data class PokemonMove(
    public val move: NamedApiResource,
    @SerialName("version_group_details") public val versionGroupDetails: List<PokemonMoveVersion> = emptyList(),
)

@Serializable
public data class PokemonMoveVersion(
    @SerialName("level_learned_at") public val levelLearnedAt: Int = 0,
    @SerialName("version_group") public val versionGroup: NamedApiResource,
    @SerialName("move_learn_method") public val moveLearnMethod: NamedApiResource,
    public val order: Int? = null,
)

@Serializable
public data class PokemonTypePast(
    public val generation: NamedApiResource,
    public val types: List<PokemonTypeSlot> = emptyList(),
)

/** An entry of `pokemon.types`: `{"slot": 1, "type": {"name": "fire", ...}}`. */
@Serializable
public data class PokemonTypeSlot(
    public val slot: Int,
    public val type: NamedApiResource,
)

@Serializable
public data class PokemonStat(
    public val stat: NamedApiResource,
    public val effort: Int = 0,
    @SerialName("base_stat") public val baseStat: Int,
)

/** Cry sound urls (`.ogg`). */
@Serializable
public data class PokemonCries(
    public val latest: String? = null,
    public val legacy: String? = null,
)

/** `pokemon.sprites`. The per-game `versions` sprites are skipped. */
@Serializable
public data class PokemonSprites(
    @SerialName("front_default") public val frontDefault: String? = null,
    @SerialName("front_shiny") public val frontShiny: String? = null,
    @SerialName("front_female") public val frontFemale: String? = null,
    @SerialName("front_shiny_female") public val frontShinyFemale: String? = null,
    @SerialName("back_default") public val backDefault: String? = null,
    @SerialName("back_shiny") public val backShiny: String? = null,
    @SerialName("back_female") public val backFemale: String? = null,
    @SerialName("back_shiny_female") public val backShinyFemale: String? = null,
    public val other: OtherSprites? = null,
)

/** `pokemon.sprites.other`. */
@Serializable
public data class OtherSprites(
    @SerialName("dream_world") public val dreamWorld: SpriteSet? = null,
    public val home: SpriteSet? = null,
    @SerialName("official-artwork") public val officialArtwork: SpriteSet? = null,
    public val showdown: SpriteSet? = null,
)

@Serializable
public data class SpriteSet(
    @SerialName("front_default") public val frontDefault: String? = null,
    @SerialName("front_shiny") public val frontShiny: String? = null,
    @SerialName("front_female") public val frontFemale: String? = null,
    @SerialName("front_shiny_female") public val frontShinyFemale: String? = null,
    @SerialName("back_default") public val backDefault: String? = null,
    @SerialName("back_shiny") public val backShiny: String? = null,
    @SerialName("back_female") public val backFemale: String? = null,
    @SerialName("back_shiny_female") public val backShinyFemale: String? = null,
)
