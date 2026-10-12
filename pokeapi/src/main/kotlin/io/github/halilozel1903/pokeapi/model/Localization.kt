package io.github.halilozel1903.pokeapi.model

/** The name in [language], or `null`. */
public fun List<Name>.localized(language: String = "en"): String? =
    firstOrNull { it.language.name == language }?.name

/**
 * The newest cleaned flavor text in [language]; with [version] only texts from that version
 * (or version group, for abilities and moves).
 */
public fun List<FlavorText>.latest(language: String = "en", version: String? = null): String? =
    lastOrNull {
        it.language.name == language &&
            (version == null || it.version?.name == version || it.versionGroup?.name == version)
    }?.flavorText?.cleanFlavorText()

/** The effect in [language]; [short] picks `short_effect`. */
public fun List<VerboseEffect>.localized(language: String = "en", short: Boolean = false): String? =
    firstOrNull { it.language.name == language }?.let { if (short) it.shortEffect else it.effect }

/**
 * Turns game text into one paragraph. PokeAPI keeps the original line breaks (`\n`), page breaks (`\f`) and
 * soft hyphens, so "A strange seed was\nplanted on its\nback at birth.\fThe plant sprouts" becomes
 * "A strange seed was planted on its back at birth. The plant sprouts".
 */
public fun String.cleanFlavorText(): String =
    replace("­\n", "")
        .replace("­", "")
        .replace("-\n", "-")
        .replace(Regex("[\\s\\u000C]+"), " ")
        .trim()
