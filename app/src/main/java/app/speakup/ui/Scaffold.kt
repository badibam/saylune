package app.speakup.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
 * gestures where the thumb is (`pixel-ui.md`, "La charpente").
 *
 * **Two lines at the top.** The first is the **status line**: the short title on the left, and
 * on the right, aligned, the fields the mode uses. Each field is simply absent where the mode
 * does not use one, in a fixed order so that nothing moves when a value changes. No buttons:
 * four entry points there would take 192 dp of the screen's 360, and they would be far from
 * the thumb. The second is the **turn's status line**, in its own frame, **always full and up
 * to date** -- not an alert box but the narrator of the cycle, and the place `docs/reference.md`
 * asks for everywhere: the one where a thing that is unavailable carries its reason.
 *
 * **At the bottom, the action bar**, one line, in glyphs. It carries the **entry points**,
 * which are constant for a screen; what carries the action of the moment is above it, and
 * changes from one instant to the next. Mixing the two would move an entry from place to place
 * according to the state of the passage.
 *
 * **A fine drawing does not force a fine target.** The bar is drawn on one line and its
 * touchable area runs three rows up; a one-column glyph gets the same invisible margin. That
 * is what reconciles the 48 dp recommendation with a scaffold that spends one row on ink.
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
            Modifier.fillMaxWidth().padding(horizontal = grid.cell),
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
        }
        Framed(Modifier.fillMaxWidth().padding(horizontal = grid.cell)) {
            Text(
                reason?.let { stringResource(it) } ?: status,
                modifier = Modifier.align(Alignment.CenterStart),
                style = type.text,
                color = palette.ink.srgb,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(Modifier.fillMaxWidth().weight(1f)) { content() }
        ActionBar(actions, onReason = { reason = it })
    }
}

/** One entry of the action bar. */
data class Action(
    val glyph: Char,
    /** Why it is off, or null when it works. An entry that is off still shows. */
    @param:StringRes val reason: Int? = null,
    val press: () -> Unit = {},
)

@Composable
private fun ActionBar(actions: List<Action>, onReason: (Int) -> Unit) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    Row(
        Modifier.fillMaxWidth().height(grid.cell * TOUCH_ROWS).padding(horizontal = grid.cell),
        horizontalArrangement = Arrangement.spacedBy(grid.cell),
    ) {
        actions.forEach { action ->
            Box(
                Modifier
                    .width(grid.cell * TOUCH_COLUMNS)
                    .fillMaxSize()
                    .clickable {
                        action.reason?.let(onReason) ?: action.press()
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    action.glyph.toString(),
                    style = type.text,
                    // Off is dimmed and still there: greying says what one can do, and an
                    // entry that vanished would change the bar's shape with the state.
                    color = if (action.reason == null) palette.ink.srgb else palette.dim.srgb,
                )
            }
        }
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

/** The touchable area of one entry, in grid rows and columns. The ink is on the bottom row. */
private const val TOUCH_ROWS = 3
private const val TOUCH_COLUMNS = 3
