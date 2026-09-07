package app.speakup.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import app.speakup.R

/**
 * Speakup Tile, embedded, at a whole scale.
 *
 * The font is written and not borrowed: its source of truth is a set of pixel maps in
 * `font/`, which `build.py` compiles into the two TTF beside them. It is embedded in
 * `res/font/` and never taken from the downloadable Google Fonts provider, which is backed by
 * Play services and so a proprietary dependency the `fdroid` wisdom forbids.
 *
 * **Square and meaty, that is to say a tile font and not a terminal font.** The two families
 * are both fixed-width and are easily confused: a terminal font is tall, narrow and
 * thin-stroked, because it serves to stack code; a tile font is square and thick, because it
 * serves to speak inside a dialogue box. The marking needs the second -- the tint has matter
 * to occupy only if the glyph is dense.
 */
@Immutable
class Typography(
    private val grid: Grid,
    private val density: Density,
    /**
     * Which of the two weights ordinary text is set in, a preference of the app's settings.
     *
     * **It moves the ordinary and never the light**: [thin] is the register's secondary ink --
     * a date under a name, a reason under an entry -- and it is defined by being lighter than
     * what it sits under. Made to follow, it would collapse onto [text] the moment someone
     * chose the light one, and the two levels of a screen would become one.
     */
    private val base: FontWeight = FontWeight.Normal,
) {

    /** Ordinary text, at one drawing pixel per [Grid.scale] screen pixels. */
    val text: TextStyle = style(base)

    /** The lighter of the two weights, a one-pixel stroke against the Regular's two. */
    val thin: TextStyle = style(FontWeight.Thin)

    /**
     * **The second size of the register: the same font at twice the factor.**
     *
     * A whole multiple of a whole factor is still whole, so the one hard rule holds -- every
     * drawing pixel comes out `2 * scale` screen pixels, none of them three wide and none four.
     * That is the whole reason there can be a second size at all and no third between the two.
     *
     * It is for what has to be **read rather than scanned**: a note, which is one letter and the
     * point of the screen it sits on, and the glyph of a button, whose ink at the ordinary size
     * is a drawing eleven pixels across at the end of an arm.
     *
     * A character of it is **two cells across and two lines tall**, so a layout takes it in
     * whole cells like everything else -- what changes is how many.
     */
    val big: TextStyle = style(base, DOUBLE)

    /**
     * **The furniture at the second size, drawn for it rather than doubled.**
     *
     * [big] takes the same drawing and paints each of its pixels four times over, which is what
     * the register says and what a letter has no choice but to do -- there is one alphabet and
     * it is drawn once. A glyph that stands for a button has a choice: `font/big.py` draws the
     * furniture again in a box of 22 by 22, so the shape has four times the pixels to be made
     * of and each of them is **the size of the text's own**. Same place on the grid, same room
     * in a frame, same character asked for -- only the family differs.
     *
     * What it costs is that the two sizes of the register no longer have one pixel between
     * them: a note letter at [big] beside a button at this style shows two pixel sizes on one
     * screen, which is what `ui.md` refuses of an illustration. It is bought knowingly, and the
     * doc carries it.
     */
    val furniture: TextStyle = TextStyle(
        fontFamily = SpeakupBig,
        fontWeight = base,
        // The em is EM_PIXELS pixels of the same size as the text's, so this is the ordinary
        // size and not the doubled one: what makes the glyph twice as big is its own box.
        fontSize = with(density) { grid.painted(EM_PIXELS).toSp() },
        lineHeight = with(density) { grid.painted(BIG_BOX).toSp() },
    )

    private fun style(weight: FontWeight, times: Int = 1) = TextStyle(
        fontFamily = SpeakupTile,
        fontWeight = weight,
        // The em is EM_PIXELS drawing pixels, so this is the size at which one drawing pixel
        // comes out exactly `scale` screen pixels. It goes through `toSp`, which divides by
        // the font scale as well as the density, so the system's text-size preference does
        // not land here: at 1.3 it would put a drawing pixel on 3.9 screen pixels and the
        // whole-factor rule -- the one hard constraint of the register -- would be broken.
        // Enlarging, when it comes, moves the scale by whole steps instead.
        fontSize = with(density) { grid.painted(EM_PIXELS * times).toSp() },
        lineHeight = with(density) { grid.painted(TEXT_STEP * times).toSp() },
    )

    companion object {

        /** What [big] multiplies the factor by. Two, and there is no room for a third size. */
        const val DOUBLE = 2
        /** How many drawing pixels the em box is: 1024 units at 64 per pixel (`font/README.md`). */
        const val EM_PIXELS = 16

        /**
         * **A line of text sits at a step of 15, a frame row at 11**, and the two are never
         * mixed in one chain. The text step is never left to the font's own default, which is
         * 14 and would stick a descender to the next line's accent.
         *
         * The box is 11 by 14, from y=11 down to y=-2: capitals hold rows 0 to 9, accents 10
         * and 11, descenders -1 and -2, and one row separates two lines.
         */
        const val TEXT_STEP = 15

        /** A frame row: the square tile, which is what makes frames and panels compose. */
        const val FRAME_STEP = Grid.CELL

        /** The ink box, rows 11 down to -2. Everything laid under a line is measured off it. */
        const val BOX = 14

        /** From the box's top to the baseline: the accents and the capitals, twelve rows. */
        const val ASCENT = 12

        /** The furniture's own box: two cells each way, all of it above the baseline. */
        const val BIG_BOX = 2 * Grid.CELL
    }
}

/**
 * The two weights, declared at what they actually are.
 *
 * Both are offered as a user preference, and the bench confirmed the ramp stays legible in
 * Thin. Nothing between them exists: a span asking for a weight this family does not carry
 * gets the nearest one it does.
 */
val SpeakupTile = FontFamily(
    Font(R.font.speakup_tile_thin, FontWeight.Thin),
    Font(R.font.speakup_tile_regular, FontWeight.Normal),
)

/**
 * The furniture, in its own box of 22 by 22 and on the same codepoints.
 *
 * A second family rather than a second block of the first: a glyph of two cells inside a font
 * whose every other glyph is one cell would make the box a lie, and `check.py` proves the
 * letters did not move by measuring exactly that box. Two weights, because what is a stroke
 * thins with the rest here as it does there.
 */
val SpeakupBig = FontFamily(
    Font(R.font.speakup_big_thin, FontWeight.Thin),
    Font(R.font.speakup_big_regular, FontWeight.Normal),
)
