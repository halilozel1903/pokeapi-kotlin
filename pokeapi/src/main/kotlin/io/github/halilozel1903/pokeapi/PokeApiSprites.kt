package io.github.halilozel1903.pokeapi

/**
 * Sprite urls built from a Pokémon id, without a request: handy for grids made from a resource list, where only
 * names and urls are known. The images are served from PokeAPI's sprites repository on GitHub.
 */
public object PokeApiSprites {
    /** The root of PokeAPI's Pokémon sprites. */
    public const val BASE_URL: String = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon"

    /** The official artwork (475 x 475 PNG), the same as `sprites.other["official-artwork"]`. */
    public fun officialArtwork(id: Int, shiny: Boolean = false): String =
        "$BASE_URL/other/official-artwork/${if (shiny) "shiny/" else ""}$id.png"

    /** The Pokémon HOME render. */
    public fun home(id: Int, shiny: Boolean = false): String =
        "$BASE_URL/other/home/${if (shiny) "shiny/" else ""}$id.png"

    /** The small in-game front sprite (96 x 96). */
    public fun front(id: Int, shiny: Boolean = false): String =
        "$BASE_URL/${if (shiny) "shiny/" else ""}$id.png"
}
