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
 * The bottom of the conversation: **three buttons on three grid rows**, above the scaffold's
 * one-row action bar (`pixel-ui.md`).
 *
 * **Each says one thing and one thing only, and disappears where it has no work.** That is what
 * replaces a single object changing meaning on the way -- `MY TURN` becoming the pause while one
 * speaks, then greyed by the capture position, would be three personalities on one button, and
 * a visible change does not save one from having to learn it.
 *
 * - **The big one says a turn of speech and not a next page**: it closes the previous passage
 *   and opens mine, which is exactly what happens. `CARRY ON` was set aside, which would suggest
 *   a turn was being skipped. It is greyed while anything at all records, whoever started it --
 *   one does not begin a fresh turn while speaking -- and in *waits* until the attempts run out.
 * - **`PAUSE` exists at the first capture position and nowhere else**, the one position that has
 *   a pause at all. Its absence is the right shape on screen, and the lever's own phrase is the
 *   right shape at the moment one chooses the position.
 * - **`SEND` exists at all three positions**, and not only at the two that send by hand: what
 *   changes from one position to the next is what *arms* the mic, never what sends. It is absent
 *   where nothing records, there being no take to send.
 *
 * **Greyed says nothing more, and the status line carries the reason** (`pixel-ui.md`): what one
 * *can* do is said by the greying, what one *must* do by the line at the top -- the door of the
 * moment, *reword it* or *say it again*, and the count left. A greyed `MY TURN` that displayed
 * something else would be the second personality this row exists to refuse, and on a screen
 * where the buttons take three rows at the bottom and the status line two at the top, the two
 * are seen together.
 *
 * **The bottom commands everything that records, whichever button started it.** The big one
 * opens a fresh turn, a passage's small one opens a retake, and in both cases these are what
 * follow. Doubling them into the row under each passage would fit -- it is not the room that
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
    pause: String?,
    send: String?,
    /** Whether the big button may be pressed. Greyed carries no reason; the status line does. */
    mayOpen: Boolean,
    mayPause: Boolean,
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
        // The big one takes the larger share, which is what it had when there were two of them.
        // How the three divide the width exactly is left to the bench (`pixel-ui.md`).
        Key(myTurn, enabled = mayOpen, modifier = Modifier.weight(BIG_SHARE), onClick = onOpen)
        pause?.let {
            Key(it, enabled = mayPause, modifier = Modifier.weight(1f), onClick = onPause)
        }
        send?.let {
            if (mayDiscard) {
                Key(
                    Glyphs.REDO.toString(),
                    modifier = Modifier.width(grid.cell * DISCARD_COLUMNS),
                    onClick = onDiscard,
                )
            }
            Key(it, modifier = Modifier.weight(1f), onClick = onSend)
        }
    }
}

/** One button: a frame with its word inside, on the three rows the row is tall. */
@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val palette = Speakup.palette
    Framed(modifier.fillMaxSize().clickable(enabled = enabled, onClick = onClick)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = Speakup.type.text,
                // Off is dimmed and still there: greying is what says a thing cannot be done
                // now, where absence says it has no object at all.
                color = if (enabled) palette.ink.srgb else palette.dim.srgb,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The bottom is four grid rows: three of buttons, and the action bar's one under them. */
private const val ROWS = 3

/** What the big button held when there were two of them, and the bench will settle. */
private const val BIG_SHARE = 2f

/** The circular arrow's own columns, which the frame is what keeps it at. */
private const val DISCARD_COLUMNS = 3
