package app.speakup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity

/**
 * The project's own theme, held outside Material.
 *
 * Four axes: the **palette** in the current register; the **typography**, which comes down to
 * one font, two weights and a few sizes; the **grid** -- the whole scale derived from the
 * density, and the cell it gives -- and the **rhythms of the marking**, stroke widths, halo,
 * air, leading. The last two are numbers shared between the text and what is painted around
 * it: laid down at each site instead, they drift.
 *
 * It is a `staticCompositionLocalOf` and not a `compositionLocalOf` on purpose: none of the
 * three changes while a screen is up -- the register swaps with the system, the grid with the
 * device -- so there is nothing to gain from reading them one composable at a time.
 */
@Composable
fun SpeakupTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val grid = remember(density) { gridFor(density) }
    CompositionLocalProvider(
        LocalPalette provides if (dark) DarkRegister else LightRegister,
        LocalTypography provides remember(grid, density) { Typography(grid, density) },
        LocalGrid provides grid,
        LocalRhythm provides Rhythm(),
        content = content,
    )
}

/** The theme, read the way Material's is: `Speakup.palette`, `Speakup.grid`, `Speakup.rhythm`. */
object Speakup {
    val palette: Palette
        @Composable @ReadOnlyComposable get() = LocalPalette.current
    val type: Typography
        @Composable @ReadOnlyComposable get() = LocalTypography.current
    val grid: Grid
        @Composable @ReadOnlyComposable get() = LocalGrid.current
    val rhythm: Rhythm
        @Composable @ReadOnlyComposable get() = LocalRhythm.current
}

// No default worth having: a palette or a grid conjured outside the theme would be a silent
// fallback, and something drawn against it would be wrong without ever saying so.
private val LocalPalette = staticCompositionLocalOf<Palette> {
    error("no palette: this is outside SpeakupTheme")
}
private val LocalTypography = staticCompositionLocalOf<Typography> {
    error("no typography: this is outside SpeakupTheme")
}
private val LocalGrid = staticCompositionLocalOf<Grid> {
    error("no grid: this is outside SpeakupTheme")
}
private val LocalRhythm = staticCompositionLocalOf<Rhythm> {
    error("no marking rhythms: this is outside SpeakupTheme")
}
