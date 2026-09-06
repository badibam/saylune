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
class Typography(private val grid: Grid, private val density: Density) {

    /** Ordinary text, at one drawing pixel per [Grid.scale] screen pixels. */
    val text: TextStyle = style(FontWeight.Normal)

    /** The lighter of the two weights, a one-pixel stroke against the Regular's two. */
    val thin: TextStyle = style(FontWeight.Thin)

    private fun style(weight: FontWeight) = TextStyle(
        fontFamily = SpeakupTile,
        fontWeight = weight,
        // The em is EM_PIXELS drawing pixels, so this is the size at which one drawing pixel
        // comes out exactly `scale` screen pixels. It goes through `toSp`, which divides by
        // the font scale as well as the density, so the system's text-size preference does
        // not land here: at 1.3 it would put a drawing pixel on 3.9 screen pixels and the
        // whole-factor rule -- the one hard constraint of the register -- would be broken.
        // Enlarging, when it comes, moves the scale by whole steps instead.
        fontSize = with(density) { grid.painted(EM_PIXELS).toSp() },
        lineHeight = with(density) { grid.painted(TEXT_STEP).toSp() },
    )

    companion object {
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
