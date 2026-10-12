package io.github.halilozel1903.pokeapi.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.halilozel1903.pokeapi.types.PokeType

/** The usual color of this type, for chips, card backgrounds and accents. */
public val PokeType.color: Color
    get() = when (this) {
        PokeType.Normal -> Color(0xFFA8A77A)
        PokeType.Fire -> Color(0xFFEE8130)
        PokeType.Water -> Color(0xFF6390F0)
        PokeType.Electric -> Color(0xFFF7D02C)
        PokeType.Grass -> Color(0xFF7AC74C)
        PokeType.Ice -> Color(0xFF96D9D6)
        PokeType.Fighting -> Color(0xFFC22E28)
        PokeType.Poison -> Color(0xFFA33EA1)
        PokeType.Ground -> Color(0xFFE2BF65)
        PokeType.Flying -> Color(0xFFA98FF3)
        PokeType.Psychic -> Color(0xFFF95587)
        PokeType.Bug -> Color(0xFFA6B91A)
        PokeType.Rock -> Color(0xFFB6A136)
        PokeType.Ghost -> Color(0xFF735797)
        PokeType.Dragon -> Color(0xFF6F35FC)
        PokeType.Dark -> Color(0xFF705746)
        PokeType.Steel -> Color(0xFFB7B7CE)
        PokeType.Fairy -> Color(0xFFD685AD)
    }

/** Text and icon color with enough contrast on [color]: near black on light types, white on dark ones. */
public val PokeType.onColor: Color
    get() = if (color.luminance() > 0.4f) Color(0xFF1D1B20) else Color.White
