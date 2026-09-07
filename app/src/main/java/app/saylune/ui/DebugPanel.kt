package app.saylune.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.saylune.R
import app.saylune.debug.Brick
import app.saylune.debug.Step
import app.saylune.debug.Trace

/**
 * The steps of the turn, as they were: what went out, what came back, and when.
 *
 * It sits under the conversation rather than on a screen of its own, because a step is only
 * readable next to the turn that produced it. Folded away by default -- it is an instrument,
 * not the app.
 *
 * A short body is written out; a long one is a name that opens. That is the only rule, and
 * it is what makes a prompt of two thousand characters and a status code of three fit in the
 * same list.
 */
@Composable
fun DebugPanel(modifier: Modifier = Modifier) {
    if (!Trace.on) return

    val steps by Trace.steps.collectAsState()
    var open by rememberSaveable { mutableStateOf(false) }
    var opened by remember { mutableStateOf<Brick?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { open = !open }) {
                Text(stringResource(R.string.debug_steps, steps.count { it.onScreen }))
            }
            if (open && steps.isNotEmpty()) {
                TextButton(onClick = { Trace.clear() }) {
                    Text(stringResource(R.string.debug_clear))
                }
            }
        }

        if (!open) return@Column

        if (steps.isEmpty()) {
            Text(
                stringResource(R.string.debug_nothing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        steps.filter { it.onScreen }.forEach { step -> Recorded(step) { opened = it } }
    }

    opened?.let { brick ->
        AlertDialog(
            onDismissRequest = { opened = null },
            confirmButton = {
                TextButton(onClick = { opened = null }) {
                    Text(stringResource(R.string.debug_dialog_close))
                }
            },
            title = { Text(brick.label, style = MaterialTheme.typography.titleSmall) },
            text = {
                SelectionContainer {
                    Text(
                        brick.body,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            },
        )
    }
}

@Composable
private fun Recorded(step: Step, onOpen: (Brick) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            stringResource(R.string.debug_step, step.name, step.atMs / 1000f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color =
                if (step.failed) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        step.bricks.forEach { brick ->
            if (brick.long) {
                Text(
                    stringResource(R.string.debug_open, brick.label, brick.body.length),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onOpen(brick) }
                        .padding(vertical = 2.dp),
                )
            } else {
                SelectionContainer {
                    Text(
                        stringResource(R.string.debug_brick, brick.label, brick.body),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}
