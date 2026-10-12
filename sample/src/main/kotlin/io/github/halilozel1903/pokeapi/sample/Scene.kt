package io.github.halilozel1903.pokeapi.sample

/**
 * Screenshot scenes, picked with the `scene` intent extra. They load live data from PokeAPI like a normal launch;
 * [loads] is how many loads (the grid, then the detail of [pokemon]) must finish before the scene is complete.
 */
enum class Scene(val key: String, val pokemon: String?, val tab: DetailTab) {
    Grid("grid", null, DetailTab.About),
    Detail("detail", "pikachu", DetailTab.Stats),
    Evolution("evolution", "eevee", DetailTab.Evolution),
    Tablet("tablet", "bulbasaur", DetailTab.About),
    ;

    val loads: Int get() = if (pokemon == null) 1 else 2

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}

enum class DetailTab(val label: String) {
    About("About"),
    Stats("Stats"),
    Evolution("Evolution"),
}
