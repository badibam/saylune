package app.speakup.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.speakup.ui.theme.Speakup
import kotlinx.coroutines.delay

/**
 * The scaffold: **at the top what is true, at the bottom what one can do**.
 *
 * One rule settles the cases to come without their being argued again, and it puts the
 * gestures where the thumb is (`ui.md`, "La charpente").
 *
 * **Two lines at the top.** The first is the **title line**: the short title on the left, the
 * fields the mode uses in the middle, and **the entry points at the right end, framed**. Each
 * field is simply absent where the mode does not use one, in a fixed order so that nothing
 * moves when a value changes. The second is the **turn's status line**, in its own frame,
 * **always full and up to date** -- not an alert box but the narrator of the cycle, and the
 * place `docs/reference.md` asks for everywhere: the one where a thing that is unavailable
 * carries its reason.
 *
 * **The entry points are at the top, and that reverses what this file used to argue.** The old
 * reading was that four of them up there would take 192 dp of the screen's 360 and sit far from
 * the thumb, so they went to a bare row at the bottom. Read on the phone, it failed on the first
 * half of its own premise: a glyph drawn at the ordinary size is eleven pixels of ink, and no
 * amount of invisible margin around it makes it look like a button one may press. Drawn at the
 * register's second size and framed, an entry costs four columns; three of them cost fourteen
 * of the twenty-six a padded worst screen gives, which the title line had spare and the bottom
 * row does not. So they take the width nobody was using, and **the bottom is left to what
 * commands the moment** -- which is the split this scaffold wanted all along: what is true at
 * the top, what one does now at the bottom.
 *
 * **Air is structural here.** A cell separates the title line from the status line and the
 * status line from the content, and one separates the content from whatever the screen puts at
 * its foot. Without them the thread runs into the buttons and the two read as one object.
 *
 * ## What is not here, and why
 *
 * The status line's right-hand side carries the lives, the **score** and the **note**. Only the
 * lives have a producer: the score's screen is out of the perimeter, and the note is a letter,
 * which is what the access layer cuts -- no layer shows letters yet, and in a free conversation
 * the default is marks alone. Two fields nothing writes would be two fields drawn from nothing
 * (`../../../../../../TODO.md`).
 */
@Composable
fun Scaffold(
    /** The short title, ten characters at most, declared by the definition. */
    title: String,
    /** How many lives are left, or null where the mode counts none. */
    lives: Int?,
    /** What the narrator says right now. Never empty. */
    status: String,
    /**
     * What pressing the line does, or null where it is only read.
     *
     * **The sound's gate's notification gives the model to listen to** (`ui.md`), and it
     * is a state rather than a receipt, so it lives in this line. Making the line itself the
     * target is what gives it the listening without adding a second object saying the same
     * thing: the remedy for a sound fault has never been a written instruction, it is hearing
     * the model and saying it again.
     */
    onStatus: (() -> Unit)? = null,
    actions: List<Action>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type

    // What an entry that is off says when it is pressed. It goes in the status line rather
    // than beside the bar, that line being the place the project puts a reason; and it clears
    // itself, the line's job being to be true now.
    var reason by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(status) { reason = null }
    LaunchedEffect(reason) {
        if (reason != null) {
            delay(REASON_MS)
            reason = null
        }
    }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(grid.cell * ENTRY_ROWS)
                .padding(horizontal = grid.cell),
            horizontalArrangement = Arrangement.spacedBy(grid.cell),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = type.text,
                color = palette.ink.srgb,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            lives?.let {
                Text(hearts(it), style = type.text, color = palette.ramp.last().srgb, maxLines = 1)
            }
            actions.forEach { action -> Entry(action, onReason = { reason = it }) }
        }
        // The cell this file's header calls structural, and which was not being laid: the
        // title line and the status line were touching, so the framed entries and the framed
        // status read as one stack of boxes instead of two lines saying two things.
        Spacer(Modifier.height(grid.cell))
        Framed(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = grid.cell)
                .let { if (onStatus == null) it else it.clickable(onClick = onStatus) },
        ) {
            Text(
                reason?.let { stringResource(it) } ?: status,
                modifier = Modifier.align(Alignment.CenterStart),
                style = type.text,
                color = palette.ink.srgb,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = grid.cell)
        ) { content() }
    }
}

/** One entry of the action bar. */
data class Action(
    val glyph: Char,
    /** Why it is off, or null when it works. An entry that is off still shows. */
    @param:StringRes val reason: Int? = null,
    val press: () -> Unit = {},
)

/**
 * One entry point: its glyph at the register's second size, in a frame.
 *
 * **The frame is what makes it a button**, and it is what the passage's own row of commands
 * uses for the same reason: framed says *object*, and one can say what one does with it -- press,
 * and it opens. The glyph at the ordinary size, bare, was a drawing with nothing around it.
 */
@Composable
private fun Entry(action: Action, onReason: (Int) -> Unit) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    Framed(
        Modifier
            .width(grid.cell * ENTRY_COLUMNS)
            .height(grid.cell * ENTRY_ROWS)
            .clickable { action.reason?.let(onReason) ?: action.press() },
    ) {
        Text(
            action.glyph.toString(),
            modifier = Modifier.align(Alignment.Center),
            style = Speakup.type.big,
            // Off is dimmed and still there: greying says what one can do, and an entry that
            // vanished would change the line's shape with the state.
            color = if (action.reason == null) palette.ink.srgb else palette.dim.srgb,
        )
    }
}

/**
 * The lives, in a field of **three columns, always**: up to three, hearts one reads at a
 * glance without counting; beyond, one heart and the number.
 *
 * The display stops at 99, and a challenge granting more than that has no lives, it has
 * scenery.
 */
private fun hearts(left: Int): String = when {
    left <= LIVES_SHOWN -> Glyphs.HEART.toString().repeat(left).padStart(LIVES_SHOWN)
    else -> Glyphs.HEART + left.coerceAtMost(LIVES_MOST).toString()
}

private const val LIVES_SHOWN = 3
private const val LIVES_MOST = 99

/** How long a reason stands in the status line before the narrator has it back. */
private const val REASON_MS = 4_000L

/**
 * One entry's frame, in grid cells: a glyph at the second size is two cells across and its box
 * two lines tall, and a frame costs a cell of border each way.
 *
 * Four rows is also what the title line is tall, the two being the same line.
 */
private const val ENTRY_COLUMNS = 4
private const val ENTRY_ROWS = 4
