package app.speakup.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.speakup.R
import app.speakup.marking.CLEAN_TURN
import app.speakup.marking.MULTI_FAULT_TURN
import app.speakup.marking.TurnMarking

/**
 * The prototype that decides: the marking rendered on a real multi-fault turn, next to a
 * clean one. What it has to show is that a turn with nothing to report stays quiet while a
 * crowded one stays readable.
 */
@Composable
fun MarkingPrototypeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
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
