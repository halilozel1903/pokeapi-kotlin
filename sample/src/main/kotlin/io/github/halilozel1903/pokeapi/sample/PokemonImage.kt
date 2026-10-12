package io.github.halilozel1903.pokeapi.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import coil3.compose.AsyncImage
import io.github.halilozel1903.pokeapi.PokeApiSprites
import io.github.halilozel1903.pokeapi.types.PokeType

/** `true` when the app shows bundled demo data: images are then drawn creatures instead of downloaded artwork. */
val LocalDemoArtwork = staticCompositionLocalOf { false }

/**
 * The official artwork from PokeAPI's sprite repository, built from the id with [PokeApiSprites] so the grid needs
 * no request per Pokémon. In demo mode a drawn creature takes its place.
 */
@Composable
fun PokemonImage(id: Int, name: String, types: List<PokeType>, modifier: Modifier = Modifier) {
    if (LocalDemoArtwork.current) {
        CreatureArt(seed = id, types = types, modifier = modifier, contentDescription = name)
        return
    }
    var loaded by remember(id) { mutableStateOf(false) }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (!loaded) {
            val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(color, radius = size.minDimension * 0.3f, style = Stroke(width = size.minDimension * 0.06f))
            }
        }
        AsyncImage(
            model = PokeApiSprites.officialArtwork(id),
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            onSuccess = { loaded = true },
        )
    }
}
