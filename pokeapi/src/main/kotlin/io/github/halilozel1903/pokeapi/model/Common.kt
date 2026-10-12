package io.github.halilozel1903.pokeapi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A link to another resource, as PokeAPI returns it everywhere: `{"name": "bulbasaur", "url": ".../pokemon/1/"}`.
 * Resources that have no name (evolution chains) leave [name] empty.
 */
@Serializable
public data class NamedApiResource(
    public val name: String = "",
    public val url: String,
) {
    /** The numeric id at the end of [url], or `null` when the url does not end with one. */
    public val id: Int? get() = resourceIdFromUrl(url)
}

/** A link to an unnamed resource: `{"url": ".../evolution-chain/1/"}`. */
@Serializable
public data class ApiResource(
    public val url: String,
) {
    /** The numeric id at the end of [url]. */
    public val id: Int? get() = resourceIdFromUrl(url)
}

/** One page of a resource list (`GET /pokemon?offset=0&limit=20`). */
@Serializable
public data class NamedApiResourceList(
    public val count: Int,
    public val next: String? = null,
    public val previous: String? = null,
    public val results: List<NamedApiResource> = emptyList(),
)

/** A localized name. */
@Serializable
public data class Name(
    public val name: String,
    public val language: NamedApiResource,
)

/**
 * A localized flavor text. Pokémon species entries carry a [version]; abilities and moves carry a [versionGroup].
 * Use [cleanFlavorText] to remove the line breaks and form feeds from the game text.
 */
@Serializable
public data class FlavorText(
    @SerialName("flavor_text") public val flavorText: String,
    public val language: NamedApiResource,
    public val version: NamedApiResource? = null,
    @SerialName("version_group") public val versionGroup: NamedApiResource? = null,
)

/** A localized item flavor text (items use `text` instead of `flavor_text`). */
@Serializable
public data class VersionGroupFlavorText(
    public val text: String,
    public val language: NamedApiResource,
    @SerialName("version_group") public val versionGroup: NamedApiResource? = null,
)

/** A localized description. */
@Serializable
public data class Description(
    public val description: String,
    public val language: NamedApiResource,
)

/** A localized effect text. */
@Serializable
public data class Effect(
    public val effect: String,
    public val language: NamedApiResource,
)

/** A localized effect text with a one line summary. */
@Serializable
public data class VerboseEffect(
    public val effect: String,
    @SerialName("short_effect") public val shortEffect: String = "",
    public val language: NamedApiResource,
)

/** A localized genus, such as "Seed Pokémon". */
@Serializable
public data class Genus(
    public val genus: String,
    public val language: NamedApiResource,
)

/** The internal index of a resource in one game version. */
@Serializable
public data class VersionGameIndex(
    @SerialName("game_index") public val gameIndex: Int,
    public val version: NamedApiResource,
)

/** The internal index of a resource in one generation. */
@Serializable
public data class GenerationGameIndex(
    @SerialName("game_index") public val gameIndex: Int,
    public val generation: NamedApiResource,
)

internal fun resourceIdFromUrl(url: String): Int? =
    url.trimEnd('/').substringAfterLast('/').toIntOrNull()
