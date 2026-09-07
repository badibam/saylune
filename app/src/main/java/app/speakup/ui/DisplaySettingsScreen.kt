package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.speakup.R
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import app.speakup.ui.theme.Speakup
import kotlinx.coroutines.launch

/**
 * The display settings: **what shows and what does not**.
 *
 * **Two families, and the screen says so.** *Marks shown* is one line per channel, each carrying
 * the dot that says where it stands -- filled for a channel that draws, hollow for one that does
 * not; **turning one off turns off its display and never its measure**, which the screen says
 * once at the foot of that family rather than eight times. *Other settings* is everything that
 * is not a mark, and it is a family of its own precisely because reading a lone ninth dot under
 * eight identical ones as *another mark* is what the flat list made one do.
 */
@Composable
fun DisplaySettingsScreen(
    store: SecretStore,
    hidden: Set<Channel>,
    /** Whether the passage's summary is pushed when the next take is sent. */
    pushed: Boolean,
    /** Whether the word marks come off a passage the words' gate has let through. */
    dropped: Boolean,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    val scope = rememberCoroutineScope()
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = grid.cell),
        verticalArrangement = Arrangement.spacedBy(grid.cell),
    ) {
        Text(
            stringResource(R.string.display_marks),
            style = type.text,
            color = palette.ink.srgb,
        )
        Channel.entries.forEach { channel ->
            Pick(
                on = channel !in hidden,
                says = stringResource(channel.says),
            ) {
                scope.launch {
                    val on = channel !in hidden
                    store.write(
                        Secret.HiddenMarks,
                        Channel.store(if (on) hidden + channel else hidden - channel),
                    )
                }
            }
        }
        Text(
            stringResource(R.string.channels_measured),
            style = type.thin,
            color = palette.dim.srgb,
        )
        // **What the mode decides is the letters; what the learner sets is whether the screen
        // is pushed.** It is on this screen because it answers the same question -- what shows
        // and what does not -- and in its own family because it is not a mark.
        Text(
            stringResource(R.string.display_other),
            modifier = Modifier.padding(top = grid.cell),
            style = type.text,
            color = palette.ink.srgb,
        )
        Pick(on = pushed, says = stringResource(R.string.notes_pushed)) {
            scope.launch { store.write(Secret.NotesUnpushed, if (pushed) UNPUSHED else "") }
        }
        // The ninth, and it is not a mark either: what it settles is how long a mark stays,
        // never whether it was made. A passage that passes still carries its marks -- the gate
        // reads a note and not the absence of one -- and here one says whether to keep looking
        // at them once there is nothing left to do about them.
        Pick(on = dropped, says = stringResource(R.string.display_drop_words)) {
            scope.launch { store.write(Secret.MarksDropped, if (dropped) "" else DROPPED) }
        }
    }
}

/** What the store holds when the notes are **not** pushed: the refusal, so nothing is the default. */
const val UNPUSHED = "unpushed"

/** What it holds when the word marks come off a passed passage: the wish, keeping being the default. */
const val DROPPED = "dropped"
