package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.speakup.R
import app.speakup.conversation.TurnPipeline
import app.speakup.store.ActivityRow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * Every conversation, most recent first, and the way to start another.
 *
 * **Nothing has to be finished for a new one to begin.** So the list holds them all rather
 * than only the ones left open: an unfinished conversation is the ordinary state of every
 * conversation but the one being had, and a filter on status would hide the list. Sorting and
 * filtering are for later.
 *
 * A row shows the name the model gave the conversation and when it started. The name is the
 * activity's matter, which for a conversation is what is being talked about -- and it arrives
 * on the first answer, so only a conversation nobody has spoken in yet has none.
 */
@Composable
fun ConversationsScreen(
    pipeline: TurnPipeline,
    onOpened: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val rows by pipeline.conversations().collectAsState(initial = emptyList())
    val open by pipeline.state.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { scope.launch { pipeline.begin(); onOpened() } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.conversations_new))
        }

        if (rows.isEmpty()) {
            Text(
                stringResource(R.string.conversations_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(rows, key = { it.id }) { row ->
                Row(row, current = row.id == open.activity.id) {
                    scope.launch { pipeline.open(row.id); onOpened() }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun Row(row: ActivityRow, current: Boolean, onOpen: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(vertical = 10.dp),
    ) {
        Text(
            row.matter.ifBlank { stringResource(R.string.conversation_unnamed) },
            style = MaterialTheme.typography.bodyLarge,
            // The one being had is marked rather than hidden: opening it again is harmless,
            // and a list that leaves out where you are is a list you cannot read yourself in.
            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
            color = if (row.matter.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(row.createdAt)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
