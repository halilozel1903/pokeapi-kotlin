package io.github.halilozel1903.pokeapi.sample

/** Screenshot scenes, picked with the `scene` intent extra. They use the bundled fixtures, never the network. */
enum class Scene(val key: String, val pokemon: String?, val tab: DetailTab) {
    Grid("grid", null, DetailTab.About),
    Detail("detail", "charizard", DetailTab.Stats),
    Evolution("evolution", "eevee", DetailTab.Evolution),
    Tablet("tablet", "bulbasaur", DetailTab.About),
    ;

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}

enum class DetailTab(val label: String) {
    About("About"),
    Stats("Stats"),
    Evolution("Evolution"),
}
