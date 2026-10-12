package io.github.halilozel1903.pokeapi.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFC8243A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD9),
    onPrimaryContainer = Color(0xFF40000B),
    secondary = Color(0xFF3D5BA9),
    onSecondary = Color.White,
    background = Color(0xFFFCF8F7),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFCF8F7),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF2E6E5),
    onSurfaceVariant = Color(0xFF574343),
    outline = Color(0xFF8C7473),
    outlineVariant = Color(0xFFDFC9C8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB3B3),
    onPrimary = Color(0xFF68001A),
    primaryContainer = Color(0xFF8F0D27),
    onPrimaryContainer = Color(0xFFFFDAD9),
    secondary = Color(0xFFB2C5FF),
    onSecondary = Color(0xFF002A75),
    background = Color(0xFF161212),
    onBackground = Color(0xFFEDE0DF),
    surface = Color(0xFF161212),
    onSurface = Color(0xFFEDE0DF),
    surfaceVariant = Color(0xFF2E2626),
    onSurfaceVariant = Color(0xFFD7C1C0),
    outline = Color(0xFFA08C8B),
    outlineVariant = Color(0xFF524343),
)

@Composable
fun PokedexTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
