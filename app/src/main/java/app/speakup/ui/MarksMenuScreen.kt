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
 * The conversation menu: which marks are shown.
 *
 * One line per channel, each carrying the dot that says where it stands -- filled for a channel
 * that draws, hollow for one that does not. The eight are the eight marks, and there is nothing
 * else on this screen: **turning one off turns off its display and never its measure**, which
 * the screen says once at the bottom rather than eight times.
 */
@Composable
fun MarksMenuScreen(
    store: SecretStore,
    hidden: Set<Channel>,
    /** Whether the passage's notes are pushed between two passages. */
    pushed: Boolean,
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
            .padding(horizontal = grid.cell, vertical = grid.cell),
        verticalArrangement = Arrangement.spacedBy(grid.cell),
    ) {
        Channel.entries.forEach { channel ->
            val on = channel !in hidden
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            store.write(
                                Secret.HiddenMarks,
                                Channel.store(if (on) hidden + channel else hidden - channel),
                            )
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(grid.cell),
            ) {
                Text(
                    (if (on) Glyphs.DOT_FILLED else Glyphs.DOT_HOLLOW).toString(),
                    style = type.text,
                    color = if (on) palette.ink.srgb else palette.dim.srgb,
                )
                Text(
                    stringResource(channel.says),
                    style = type.text,
                    color = if (on) palette.ink.srgb else palette.dim.srgb,
                )
            }
        }
        // **What the mode decides is the letters; what the learner sets is whether the screen
        // is pushed.** It sits with the channels because it answers the same question -- what
        // shows and what does not -- and not with the providers and the keys.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = grid.cell)
                .clickable {
                    scope.launch {
                        store.write(Secret.NotesUnpushed, if (pushed) UNPUSHED else "")
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(grid.cell),
        ) {
            Text(
                (if (pushed) Glyphs.DOT_FILLED else Glyphs.DOT_HOLLOW).toString(),
                style = type.text,
                color = if (pushed) palette.ink.srgb else palette.dim.srgb,
            )
            Text(
                stringResource(R.string.notes_pushed),
                style = type.text,
                color = if (pushed) palette.ink.srgb else palette.dim.srgb,
            )
        }
        Text(
            stringResource(R.string.channels_measured),
            style = type.thin,
            color = palette.dim.srgb,
            modifier = Modifier.padding(top = grid.cell),
        )
    }
}

/** What the store holds when the notes are **not** pushed: the refusal, so nothing is the default. */
const val UNPUSHED = "unpushed"
