package app.saylune.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import app.saylune.R
import app.saylune.conversation.Side
import app.saylune.ui.theme.Saylune

/**
 * The row of commands, which **lives in the thread under each passage** and not pinned to the
 * bottom of the screen (`ui.md`).
 *
 * Six entries, at the same padding as the text above them, and **glyphs wherever possible**:
 * a glyph replaces a word only where the room is missing, and this is the one row where it is
 * -- `SPEAK` and `SEND` stay words at the bottom, because they command under pressure and one
 * of them spends an attempt.
 *
 * **Every entry is framed, at four columns, and the glyphs are at the second size.** The mic
 * alone was framed before and the five others were bare glyphs eleven pixels across: what the
 * frame said of the mic -- *this is a thing you press* -- was true of all six, and the five
 * that lacked it read as decoration beside the one that had it. Framed and doubled, an entry
 * costs the mic's own four columns, so six of them cost twenty-four of the twenty-six a padded
 * worst screen gives.
 *
 * **The two remaining columns are the two seams**, and the row is read as three groups rather
 * than six things: what opens the mic, what listens, what reads. Within a group the frames
 * touch; between them there is a cell.
 *
 * | group | entries | columns |
 * |---|---|---|
 * | retake | the small button, the **mic** | 4 |
 * | listen | the triangle, the side `ME`/`AI`, the speed | 12 |
 * | read | the passage's notes, the sound analysis | 8 |
 *
 * **What the glyphs avoid is a width that depends on the state** -- a label saying which door
 * is open would be three columns in one word and nine in another, and would shift the entries
 * beside it every time a gate closed. A row whose geometry depends on the state is what the
 * grid exists to prevent.
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
    val grid = Saylune.grid
    val palette = Saylune.palette
    Row(
        modifier
            .padding(horizontal = grid.cell)
            .height(grid.cell * ENTRY_ROWS),
        horizontalArrangement = Arrangement.spacedBy(grid.cell),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Absent and not greyed on a closed passage: greying is for what is momentarily
        // impossible, absence for what has no object -- and a closed passage is never retaken.
        if (open) {
            Entry(enabled = !busy && !running, onClick = onOpen) {
                Glyph(
                    Glyphs.MIC,
                    // Its own colour while this passage's recording runs: the small button
                    // behaves like the big one, so it shows itself running.
                    colour = when {
                        mine -> palette.ramp.last().srgb
                        busy || running -> palette.dim.srgb
                        else -> palette.ink.srgb
                    },
                )
            }
        }
        // The three that listen. Side and speed sit beside the triangle because their scope is
        // it: one says which recording it reaches, the other how fast. Both also govern a tap
        // on a word, which is the same gesture one notch finer.
        Row {
            Entry(enabled = !busy, onClick = onHear) { Glyph(Glyphs.PLAY, dimmed = busy) }
            // **Two cells of ink whichever label is in force.** What the doc forbids is a width
            // that depends on the state, and the frame settles it here: a four-column frame
            // holds two cells of content, which is what `ME`, `AI`, `MOI` and `IA` all are.
            Entry(onClick = { onSide(other(side)) }) {
                Label(
                    stringResource(
                        if (side == Side.Model) R.string.side_short_model
                        else R.string.side_short_learner
                    )
                )
            }
            Entry(onClick = { onSpeed(next(speed)) }) {
                Label(times(speed), dimmed = speed == 1f)
            }
        }
        // The two that read.
        Row {
            Entry(onClick = onNotes) { Glyph(Glyphs.HISTOGRAM) }
            // Dimmed where the analysis produced nothing to open, which is an entry with no
            // object rather than one momentarily impossible. Whether the readout is open shows
            // in the readout being there, not in the glyph.
            Entry(enabled = sounds, onClick = onRead) {
                Glyph(Glyphs.MAGNIFIER, dimmed = !sounds)
            }
        }
    }
}

/**
 * One entry: a frame four columns wide, holding two cells of ink.
 *
 * **A fine drawing does not force a fine target** (`ui.md`), and the frame is what settles
 * both at once here: it says *this is a thing you press*, it gives the target the 48 dp the
 * recommendation wants, and it fixes the width whatever is drawn inside.
 */
@Composable
private fun Entry(
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val grid = Saylune.grid
    Framed(
        Modifier
            .width(grid.cell * ENTRY_COLUMNS)
            .height(grid.cell * ENTRY_ROWS)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun Glyph(glyph: Char, dimmed: Boolean = false) {
    val palette = Saylune.palette
    Glyph(glyph, if (dimmed) palette.dim.srgb else palette.ink.srgb)
}

@Composable
private fun Glyph(glyph: Char, colour: Color) {
    Text(glyph.toString(), style = Saylune.type.furniture, color = colour)
}

/**
 * A label that toggles or cycles, centred in its frame so nothing beside it moves.
 *
 * At the ordinary size, where the glyphs are doubled: two letters doubled would be four cells
 * of ink and would make this entry twice the others. A word is read as a word at eleven pixels;
 * a glyph is not.
 */
@Composable
private fun Label(text: String, dimmed: Boolean = false) {
    val palette = Saylune.palette
    Text(
        text,
        style = Saylune.type.text,
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

/**
 * One entry's frame, in grid cells -- the same four by four the scaffold's entry points are:
 * a glyph at the second size is two cells across and its box two lines tall, and a frame costs
 * a cell of border each way.
 */
private const val ENTRY_COLUMNS = 4
private const val ENTRY_ROWS = 4
