package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.speakup.ui.theme.Speakup

/**
 * The bottom of the conversation: **the buttons that command the moment**, and nothing else --
 * the entry points went up to the title line, so this row is the whole foot of the screen
 * (`ui.md`).
 *
 * **Each says one thing and one thing only, and stays put whether it can be pressed or not.**
 * That is what replaces a single object changing meaning on the way -- `SPEAK` becoming the
 * pause while one speaks, then greyed by the capture position, would be three personalities on
 * one button, and a visible change does not save one from having to learn it.
 *
 * **The row's shape does not depend on the state.** Read on the phone, buttons coming and going
 * moved the two beside them at every turn of the cycle, so the thumb aimed at what had just
 * shifted; and the space they freed went to labels rather than to nothing, which is a second
 * cost hidden in the first. So all four are always drawn, and greying alone says what may be
 * pressed. Absence is kept for what has no object **on this screen at all** -- a mode with no
 * discard hides that button for the whole sitting, which is not a state changing under the hand.
 *
 * - **The big one says a turn of speech and not a next page**: it closes the previous passage
 *   and opens mine, which is exactly what happens. `CARRY ON` was set aside, which would suggest
 *   a turn was being skipped. It is greyed while anything at all records, whoever started it --
 *   one does not begin a fresh turn while speaking -- and in *waits* until the attempts run out.
 * - **The pause is a glyph and not a word**, and it is the one place a control shows the state
 *   rather than saying it: the two bars while it records, the disc while it is held. It exists
 *   at the first capture position and nowhere else -- greyed at the other two, where nothing
 *   pauses, and the lever's own phrase says why at the moment one chooses the position.
 * - **`SEND` exists at all three positions**, and not only at the two that send by hand: what
 *   changes from one position to the next is what *arms* the mic, never what sends. It is greyed
 *   where nothing records, there being no take to send.
 *
 * **Greyed says nothing more, and the status line carries the reason** (`ui.md`): what one
 * *can* do is said by the greying, what one *must* do by the line at the top -- the door of the
 * moment, *reword it* or *say it again*, and the count left. A greyed `MY TURN` that displayed
 * something else would be the second personality this row exists to refuse, and on a screen
 * where the buttons take three rows at the bottom and the status line two at the top, the two
 * are seen together.
 *
 * **The bottom commands everything that records, whichever button started it.** The big one
 * opens a fresh turn, a passage's small one opens a retake, and in both cases these are what
 * follow. What sits above it is the row of commands under each passage, which never records. Doubling them into the row under each passage would fit -- it is not the room that
 * rules it out -- but it would be two `SEND`s doing the same work in two places, the one to
 * press depending on what was started. What makes the sharing safe is that the status line
 * **names what is running**: not *recording* but *a turn*, *a retake*, running or paused.
 *
 * **The fourth control is not the doc's**, and it is here because nothing else places it.
 * Throwing a take away is a real gesture with a lever of its own, and the doc draws three
 * buttons without saying where it goes -- as it leaves open where the two countdowns and the
 * recording symbol go. It rides beside `SEND` as the circular arrow until that question is
 * answered for all of them at once (`../../../../../../TODO.md`).
 */
@Composable
fun Buttons(
    /** What the big button reads, which is a turn of speech and never a next page. */
    myTurn: String,
    /** What `SEND` reads. One word, the row's width being spent on the big button. */
    send: String,
    /** Whether the big button may be pressed. Greyed carries no reason; the status line does. */
    mayOpen: Boolean,
    /** Whether the capture position has a pause at all. Greyed where it has none. */
    pauses: Boolean,
    mayPause: Boolean,
    /** Whether something is recording right now, which is what the pause's glyph shows. */
    recording: Boolean,
    maySend: Boolean,
    /** Whether a take may be thrown away: the lever, and the position that has an object for it. */
    mayDiscard: Boolean,
    onOpen: () -> Unit,
    onPause: () -> Unit,
    onSend: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    Row(
        modifier
            .fillMaxWidth()
            .height(grid.cell * ROWS)
            .padding(horizontal = grid.cell),
        horizontalArrangement = Arrangement.spacedBy(grid.cell),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The two words take the width and the two glyphs take a fixed four columns each, which
        // is what the second size in a frame costs. How the words divide what is left is the
        // bench's (`ui.md`); the big one keeps the larger share it had when there were two.
        Key(myTurn, enabled = mayOpen, modifier = Modifier.weight(BIG_SHARE), onClick = onOpen)
        // **The one control that shows a state**: the two bars while it records, the disc while
        // it is held. Reading it is reading what pressing does, which no word does as briefly.
        Key(
            glyph = if (recording) Glyphs.PAUSE else Glyphs.RECORD,
            enabled = pauses && mayPause,
            modifier = Modifier.width(grid.cell * GLYPH_COLUMNS),
            onClick = onPause,
        )
        if (mayDiscard) {
            Key(
                glyph = Glyphs.REDO,
                enabled = maySend,
                modifier = Modifier.width(grid.cell * GLYPH_COLUMNS),
                onClick = onDiscard,
            )
        }
        Key(send, enabled = maySend, modifier = Modifier.weight(1f), onClick = onSend)
    }
}

/** One button: a frame with its word inside, on the rows the row is tall. */
@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = Key(modifier, enabled, onClick) {
    Text(
        label,
        style = Speakup.type.text,
        // Off is dimmed and still there: greying is what says a thing cannot be done now,
        // where absence says it has no object at all.
        color = if (enabled) Speakup.palette.ink.srgb else Speakup.palette.dim.srgb,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** The same button with a glyph inside it, at the register's second size. */
@Composable
private fun Key(
    glyph: Char,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = Key(modifier, enabled, onClick) {
    Text(
        glyph.toString(),
        style = Speakup.type.big,
        color = if (enabled) Speakup.palette.ink.srgb else Speakup.palette.dim.srgb,
    )
}

@Composable
private fun Key(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Framed(modifier.fillMaxSize().clickable(enabled = enabled, onClick = onClick)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

/**
 * How tall the row is, in grid cells: a glyph at the second size is two lines tall and a frame
 * costs a cell of border each way.
 */
private const val ROWS = 4

/** What the big button held when there were two of them, and the bench will settle. */
private const val BIG_SHARE = 2f

/** A glyph button's own columns: two cells of ink, and the frame's border each side. */
private const val GLYPH_COLUMNS = 4
