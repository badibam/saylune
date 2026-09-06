package app.speakup.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

/**
 * The grid: one drawing pixel of the font, blown up by a whole number.
 *
 * **The grid is horizontal** (`pixel-ui.md`). What it protects is the marking's anchoring to
 * the characters: a rule covers a whole number of cells, the corners of sounds no letter
 * carries fall on exact bounds, and a whole class of rounding leaves `ui/MarkedTurn.kt`,
 * which measures at the sub-pixel today.
 *
 * What whole anchoring asks for is not the monospace but the **pixel font at a whole scale**:
 * in a pixel font the advances are whole numbers of pixels by construction, and multiplied by
 * a whole factor they stay so. The monospace comes from the register, not from the marking.
 *
 * **In the vertical, nothing is anchored.** The height a line of text sits at inside its box
 * is free, as long as it is a whole number of drawing pixels. That is what lets the marking
 * be given air without breaking anything. Frames, panels and margins do sit on the cell in
 * both directions -- that is what makes them compose with each other.
 *
 * If this did not live in the theme, every composable would work it out again and they would
 * drift.
 */
@Immutable
class Grid(
    /** How many screen pixels one drawing pixel becomes. A whole number, and that is the hard rule. */
    val scale: Int,
    private val density: Density,
) {
    /** [pixels] drawing pixels, as the length Compose lays out with. */
    fun drawn(pixels: Int): Dp = with(density) { (pixels * scale).toDp() }

    /** [pixels] drawing pixels, as screen pixels -- what a `Canvas` paints in. */
    fun painted(pixels: Int): Float = (pixels * scale).toFloat()

    /** The tile: the frame piece's own box, and the width one character advances. */
    val cell: Dp = drawn(CELL)

    /** How many characters fit across [width]. Varies by device and is never a target. */
    fun columns(width: Dp): Int = (width / cell).toInt()

    companion object {
        /** The tile is square, eleven drawing pixels each way (`font/README.md`). */
        const val CELL = 11

        /** Capital height in drawing pixels; the yardstick the scale is chosen against. */
        const val CAP = 10
    }
}

/**
 * The scale for this screen: the whole number that puts a capital nearest **10 dp**, the size
 * of an app's ordinary text.
 *
 * **The factor is a whole number, and that is the only hard constraint** (`pixel-ui.md`): at
 * 3.5 some drawing pixels would come out three screen pixels wide and others four, strokes
 * would land at uneven thicknesses and the effect would collapse. There is **no computable
 * criterion** behind its value -- it is the trade between the legibility of the letters and
 * the number of columns, and it is settled by looking. What the looking settled is the
 * capital height, not the factor: hence rounding here rather than a table of densities.
 *
 * A capital is [Grid.CAP] drawing pixels, so `cap in dp = 10 * scale / density`, and the
 * scale nearest 10 dp is the density rounded. Density varies from device to device, so **the
 * number of columns does too** -- a 720 screen gives half what a 1440 does at a constant
 * factor, and a layout has to hold across a range rather than aim at a count.
 */
fun gridFor(density: Density): Grid =
    Grid(density.density.roundToInt().coerceAtLeast(1), density)
