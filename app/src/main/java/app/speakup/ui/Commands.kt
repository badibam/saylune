package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.speakup.R
import app.speakup.conversation.Side
import app.speakup.ui.theme.Speakup

/**
 * The row of commands, which **lives in the thread under each passage** and not pinned to the
 * bottom of the screen (`ui.md`).
 *
 * Six entries, at the same padding as the text above them, and **glyphs wherever possible**:
 * ten columns plus five separations make fifteen of the twenty-eight the worst screen gives.
 * A glyph replaces a word only where the room is missing, and this is the one row where it is
 * -- `MY TURN`, `PAUSE` and `SEND` stay words at the bottom, because they command under
 * pressure and one of them spends an attempt.
 *
 * | entry | columns |
 * |---|---|
 * | the small button, the **mic**, in a frame | 3 |
 * | listening, the triangle | 1 |
 * | which side the listening reaches, `ME` / `AI`, one label that toggles | 2 |
 * | the speed, one label that cycles | 2 |
 * | the passage's notes | 1 |
 * | the sound analysis, the magnifier | 1 |
 *
 * **The small button keeps its three columns by being framed**, where the five others are
 * bare. What the frame buys is telling it from a row where everything else listens or sets: it
 * is the only one that opens the mic. And what the glyph avoids is a width that depends on the
 * state -- a label saying which door is open would be three columns in one word and nine in
 * another, and would shift the five entries beside it every time a gate closed. A row whose
 * geometry depends on the state is what the grid exists to prevent.
 *
 * **The notes and the sounds are two entries and not one**: they do not talk about the same
 * thing. One renders the passage's summary, sheet by sheet; the other goes a notch down into
 * the inventory sound by sound.
 *
 * **Only the open passage carries the small button**, the attempts stopping at the close. The
 * five others hold on any passage that carries a recording -- listening back and reading the
 * measures is what a measured turn is for.
 */
@Composable
fun Commands(
    /** Whether this is the passage still open, the only one that can be taken again. */
    open: Boolean,
    /** Whether the chain holds the screen. */
    busy: Boolean,
    /** Whether the recording that is running, if any, is this passage's. */
    mine: Boolean,
    /** Whether anything at all is recording, whoever started it. */
    running: Boolean,
    side: Side,
    speed: Float,
    /** Whether the sound analysis has anything to open. */
    sounds: Boolean,
    onSide: (Side) -> Unit,
    onSpeed: (Float) -> Unit,
    onHear: () -> Unit,
    onNotes: () -> Unit,
    onRead: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    Row(
        modifier
            .padding(horizontal = grid.cell)
            .height(grid.cell * TOUCH_ROWS),
        horizontalArrangement = Arrangement.spacedBy(grid.cell),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Absent and not greyed on a closed passage: greying is for what is momentarily
        // impossible, absence for what has no object -- and a closed passage is never retaken.
        if (open) {
            Framed(
                Modifier
                    .width(grid.cell * MIC_COLUMNS)
                    .height(grid.cell * FRAME_ROWS)
                    .clickable(enabled = !busy && !running, onClick = onOpen),
            ) {
                Glyph(
                    Glyphs.MIC,
                    // Its own colour while this passage's recording runs: the small button
                    // behaves like the big one, so it shows itself running.
                    colour = when {
                        mine -> palette.ramp.last().srgb
                        busy || running -> palette.dim.srgb
                        else -> palette.ink.srgb
                    },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        Entry(columns = 1, enabled = !busy, onClick = onHear) { Glyph(Glyphs.PLAY) }
        // Beside the triangle, because their scope is it: one says which recording it
        // reaches, the other how fast. Both also govern a tap on a word, which is the same
        // gesture one notch finer.
        // **Sized by the wider of the two labels, never by the one in force.** What the doc
        // forbids is a width that depends on the *state* -- a label that grew when a gate closed
        // would shift the five entries beside it. The language is fixed for a sitting, so
        // measuring both here is what keeps the geometry still while `ME`/`AI` becomes
        // `MOI`/`IA` one language over.
        val ai = stringResource(R.string.side_short_model)
        val me = stringResource(R.string.side_short_learner)
        Entry(columns = maxOf(ai.length, me.length), onClick = { onSide(other(side)) }) {
            Label(if (side == Side.Model) ai else me)
        }
        Entry(columns = SPEED_COLUMNS, onClick = { onSpeed(next(speed)) }) {
            Label(times(speed), dimmed = speed == 1f)
        }
        Entry(columns = 1, onClick = onNotes) { Glyph(Glyphs.HISTOGRAM) }
        // Dimmed where the analysis produced nothing to open, which is an entry with no
        // object rather than one momentarily impossible. Whether the readout is open shows
        // in the readout being there, not in the glyph.
        Entry(columns = 1, enabled = sounds, onClick = onRead) {
            Glyph(Glyphs.MAGNIFIER, dimmed = !sounds)
        }
    }
}

/**
 * One entry: its ink on a column or two, and a touchable area three rows tall around it.
 *
 * **A fine drawing does not force a fine target** (`ui.md`): the recommendation is 48 dp
 * and a row is about 11 dp here, so a one-column glyph gets the invisible margin that
 * reconciles the two. It is not peculiar to this register -- list rows are padded for that
 * reason in any app; the only difference is that here the padding is visible, in empty cells.
 */
@Composable
private fun Entry(
    columns: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val grid = Speakup.grid
    Box(
        Modifier
            .width(grid.cell * columns)
            .fillMaxSize()
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Glyph(glyph: Char, dimmed: Boolean = false, modifier: Modifier = Modifier) {
    val palette = Speakup.palette
    Glyph(glyph, if (dimmed) palette.dim.srgb else palette.ink.srgb, modifier)
}

@Composable
private fun Glyph(glyph: Char, colour: Color, modifier: Modifier = Modifier) {
    Text(glyph.toString(), modifier = modifier, style = Speakup.type.text, color = colour)
}

/** A label that toggles or cycles, centred in its columns so nothing beside it moves. */
@Composable
private fun Label(text: String, dimmed: Boolean = false) {
    val palette = Speakup.palette
    Text(
        text,
        style = Speakup.type.text,
        color = if (dimmed) palette.dim.srgb else palette.ink.srgb,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

private fun other(side: Side): Side =
    if (side == Side.Model) Side.Learner else Side.Model

/**
 * How fast anything played by hand is played, cycling through the three.
 *
 * Slower keeps the pitch: the stretch is a time stretch and not a resampling, which would take
 * the formants down with the rate and turn one vowel into another. A third of speed is where a
 * fast reduction stops being a blur and starts being a sequence of sounds.
 */
private fun next(speed: Float): Float =
    SPEEDS[(SPEEDS.indexOfFirst { it == speed }.coerceAtLeast(0) + 1) % SPEEDS.size]

/** Two columns, which is what the fraction glyphs of the font make possible. */
private fun times(speed: Float): String = when (speed) {
    HALF -> "×½"
    THIRD -> "×⅓"
    else -> "×1"
}

private val SPEEDS = listOf(1f, 0.5f, 0.33f)

private const val HALF = 0.5f
private const val THIRD = 0.33f

/** The small button's own columns, which the frame is what keeps it at. */
private const val MIC_COLUMNS = 3

/** A frame is two rows at the least: four pixels of border, the box, four pixels of border. */
private const val FRAME_ROWS = 2

private const val SPEED_COLUMNS = 2

/** What every touchable entry of the app is tall, the ink sitting inside it. */
private const val TOUCH_ROWS = 3
