package io.github.halilozel1903.pokeapi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * A Pokédex built on pokeapi-kotlin. It loads the live PokeAPI (cached in memory and on disk) and the official
 * artwork with Coil. `scripts/screenshots.sh` starts it with `--es scene <scene>` to open a fixed screen, loaded
 * live like a normal launch:
 *
 * - `grid`: the Kanto grid with search and type filters, starting at Bulbasaur
 * - `detail`: Pikachu's base stats and type matchups
 * - `evolution`: Eevee's branching evolution chain
 * - `tablet`: grid and Bulbasaur's detail side by side (tablets in landscape)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            PokedexTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    PokedexApp(
                        scene = scene,
                        repository = if (scene != null) Repositories.scenes(applicationContext) else Repositories.live(applicationContext),
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
