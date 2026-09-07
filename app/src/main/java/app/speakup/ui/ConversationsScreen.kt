package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.speakup.R
import app.speakup.activity.Definitions
import app.speakup.conversation.TurnPipeline
import app.speakup.store.ActivityRow
import app.speakup.store.Sitting
import app.speakup.ui.theme.Speakup
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Every conversation, most recent first, and the way to start another.
 *
 * **Nothing has to be finished for a new one to begin.** So the list holds them all rather
 * than only the ones left open: an unfinished conversation is the ordinary state of every
 * conversation but the one being had, and a filter on status would hide the list. Sorting and
 * filtering are for later.
 *
 * A row shows **the name of the definition it came from**, and when it started. Nothing here
 * is named by the model any more: what a sitting is called is decided by the file it was
 * opened from, before a word is said.
 *
 * **In the register, like every screen it stands beside.** It was the last screen still dressed
 * by Material -- a filled button, a divider, a bold row -- so arriving here from the title
 * screen changed typeface, ink and shape at once, and the one that opened a conversation looked
 * like an app the others were not. What it costs is nothing new: a framed line to begin, framed
 * rows to reopen, the grid for every measurement.
 *
 * **The one being had wears a filled dot**, where the others wear a hollow one. Marked rather
 * than hidden: opening it again is harmless, and a list that leaves out where you are is a list
 * you cannot read yourself in. It replaces the bold, which the family has only two weights to
 * say and says the wrong way round -- Thin is the lighter, and asking for bold renders Regular.
 *
 * **This screen is the catalogue the proof of concept left behind**, and it is on its way out:
 * the free door is to show one tile per shipped definition rather than one row per sitting
 * (`docs/ui.md`). It stays until those tiles exist, because it is the only way to
 * reach a conversation that is not the open one.
 */
@Composable
fun ConversationsScreen(
    pipeline: TurnPipeline,
    onOpened: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    val scope = rememberCoroutineScope()
    val rows by pipeline.conversations().collectAsState(initial = emptyList())
    val open by pipeline.state.collectAsState()

    Column(
        modifier.padding(horizontal = grid.cell),
        verticalArrangement = Arrangement.spacedBy(grid.cell),
    ) {
        Framed(
            Modifier
                .fillMaxWidth()
                .height(grid.cell * NEW_ROWS)
                .clickable { scope.launch { pipeline.begin(); onOpened() } },
        ) {
            Text(
                stringResource(R.string.conversations_new),
                modifier = Modifier.align(Alignment.Center),
                style = type.text,
                color = palette.ink.srgb,
                maxLines = 1,
            )
        }

        if (rows.isEmpty()) {
            Text(
                stringResource(R.string.conversations_none),
                style = type.thin,
                color = palette.dim.srgb,
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(grid.cell)) {
            items(rows, key = { it.id }) { row ->
                Row(row, current = row.id == open.activity.id) {
                    scope.launch { pipeline.open(row.id); onOpened() }
                }
            }
        }
    }
}

/**
 * One sitting: what it is called, when it began, and whether it is the one being had.
 *
 * Framed, which is the rule the frame carries -- one can say what one does with it: press, and
 * it opens. Two lines inside, the name over the date, so a long name is not competing with a
 * date for the width of a phone.
 */
@Composable
private fun Row(row: ActivityRow, current: Boolean, onOpen: () -> Unit) {
    val context = LocalContext.current
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    val language = Locale.getDefault().language
    Framed(
        Modifier
            .fillMaxWidth()
            .height(grid.cell * ROW_ROWS)
            .clickable(onClick = onOpen),
    ) {
        val named = row.origin?.let { origin ->
            runCatching {
                Definitions.of(context, Sitting.readOrigin(origin).definition)
                    .title.inLanguage(language)
            }.getOrNull()
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (if (current) Glyphs.DOT_FILLED else Glyphs.DOT_HOLLOW).toString(),
                    style = type.text,
                    color = if (current) palette.ink.srgb else palette.dim.srgb,
                )
                Spacer(Modifier.width(grid.cell))
                Text(
                    named ?: stringResource(R.string.conversation_unnamed),
                    style = type.text,
                    color = if (named == null) palette.dim.srgb else palette.ink.srgb,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(row.createdAt)),
                modifier = Modifier.padding(start = grid.cell * DATE_INDENT),
                style = type.thin,
                color = palette.dim.srgb,
                maxLines = 1,
            )
        }
    }
}

/** The line that begins one: a frame with one line of text in it. */
private const val NEW_ROWS = 3

/** A row's frame: two lines inside, and the border each side. */
private const val ROW_ROWS = 5

/** The date sits under the name and not under the dot, so the two lines read as one block. */
private const val DATE_INDENT = 2
