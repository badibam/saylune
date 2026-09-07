package app.saylune.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.saylune.R
import app.saylune.judged.Judgement
import app.saylune.marking.CLEAN_JUDGED
import app.saylune.marking.CLEAN_TURN
import app.saylune.marking.MULTI_FAULT_JUDGED
import app.saylune.marking.MULTI_FAULT_TURN
import app.saylune.marking.TurnFile
import app.saylune.marking.TurnMarking

/**
 * The marking, on what the bench actually measured and on what it cannot measure yet.
 *
 * The measured turn comes first because it is the one that decides: real audio, real
 * weights, real grid. The two invented turns stay below it, and stay for a reason -- they
 * carry stress and melody, which bricks 7 and 8 do not yet compute, so they are the only
 * place those two channels can be looked at at all.
 */
@Composable
fun MarkingPrototypeScreen() {
    val context = LocalContext.current
    // **A file it cannot read carries its reason on the screen rather than taking the app
    // down with it.** `TurnFile` throws on a malformed file on purpose -- a silently empty
    // screen would read exactly like a turn with nothing to report -- and thrown from a
    // composable that stops the process, which says nothing to anybody. Nothing is
    // defaulted here: what fails is shown, where the project puts what is unavailable.
    val turn = remember { runCatching { TurnFile.read(context) } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        val read = turn.getOrNull()
        when {
            turn.isFailure -> Note(stringResource(
                R.string.unreadable_turn_file,
                TurnFile.path(context)?.path ?: TurnFile.NAME,
                turn.exceptionOrNull()?.message ?: "",
            ))
            read == null -> Note(stringResource(R.string.no_turn_file,
                                                TurnFile.path(context)?.path ?: TurnFile.NAME))
            else -> Sample(stringResource(R.string.measured_turn, read.take), read.marking, null)
        }
        Sample(stringResource(R.string.sample_multi_fault_turn), MULTI_FAULT_TURN, MULTI_FAULT_JUDGED)
        Sample(stringResource(R.string.sample_clean_turn), CLEAN_TURN, CLEAN_JUDGED)
    }
}

@Composable
private fun Sample(label: String, marking: TurnMarking, judged: Judgement?) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp),
    )
    MarkedTurn(marking, judged, modifier = Modifier.padding(bottom = 40.dp))
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 40.dp),
    )
}
