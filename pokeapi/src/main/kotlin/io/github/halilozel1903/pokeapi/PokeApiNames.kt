package io.github.halilozel1903.pokeapi

/** Turns user input into PokeAPI names and PokeAPI names into display names. */
public object PokeApiNames {
    private val displayOverrides = mapOf(
        "nidoran-f" to "Nidoran♀",
        "nidoran-m" to "Nidoran♂",
        "mr-mime" to "Mr. Mime",
        "mime-jr" to "Mime Jr.",
        "mr-rime" to "Mr. Rime",
        "farfetchd" to "Farfetch’d",
        "sirfetchd" to "Sirfetch’d",
        "type-null" to "Type: Null",
        "ho-oh" to "Ho-Oh",
        "porygon-z" to "Porygon-Z",
        "jangmo-o" to "Jangmo-o",
        "hakamo-o" to "Hakamo-o",
        "kommo-o" to "Kommo-o",
    )

    /**
     * The PokeAPI name for what a user typed: "Mr. Mime" -> "mr-mime", "Farfetch'd" -> "farfetchd",
     * "Nidoran♀" -> "nidoran-f", " PIKACHU " -> "pikachu".
     */
    public fun normalize(name: String): String =
        name.trim()
            .lowercase()
            .replace("♀", "-f")
            .replace("♂", "-m")
            .replace(Regex("[.'’:]"), "")
            .replace(Regex("[\\s_]+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')

    /** A display name for a PokeAPI name: "mr-mime" -> "Mr. Mime", "thunder-stone" -> "Thunder Stone". */
    public fun prettify(apiName: String): String =
        displayOverrides[apiName] ?: apiName.split('-').filter { it.isNotEmpty() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercaseChar() } }

    /** A Pokédex number such as "#0025". */
    public fun dexNumber(id: Int): String = "#" + id.toString().padStart(4, '0')
}
