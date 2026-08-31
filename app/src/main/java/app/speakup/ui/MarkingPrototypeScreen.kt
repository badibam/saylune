package app.speakup.ui

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
import app.speakup.R
import app.speakup.marking.CLEAN_TURN
import app.speakup.marking.MULTI_FAULT_TURN
import app.speakup.marking.TurnFile
import app.speakup.marking.TurnMarking

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
    val turn = remember { TurnFile.read(context) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        if (turn == null) {
            Note(stringResource(R.string.no_turn_file,
                                TurnFile.path(context)?.path ?: TurnFile.NAME))
        } else {
            Sample(stringResource(R.string.measured_turn, turn.take), turn.marking)
        }
        Sample(stringResource(R.string.sample_multi_fault_turn), MULTI_FAULT_TURN)
        Sample(stringResource(R.string.sample_clean_turn), CLEAN_TURN)
    }
}

@Composable
private fun Sample(label: String, marking: TurnMarking) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp),
    )
    MarkedTurn(marking, modifier = Modifier.padding(bottom = 40.dp))
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
