package app.speakup.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.speakup.R
import app.speakup.capture.TurnRecorder
import app.speakup.chain.Exchange
import app.speakup.conversation.Phase
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.conversation.TurnPipeline
import app.speakup.marking.TurnMarking
import kotlinx.coroutines.launch

/**
 * The turn, end to end: hold to speak, release to think, press again to carry on, send.
 *
 * A turn of the learner is drawn with its marks once the analysis has read it, and plainly
 * until then -- the two are told apart on purpose, since a turn not yet analysed is not a
 * turn with nothing to report.
 *
 * The screen is bare otherwise, and that is deliberate: what shape the marking should take
 * is what the real use of these turns is meant to decide, and the doc already knows the
 * binary paint will not do -- most words of real learner speech carry something
 * (`../../../../../../TODO.md`, chantier 0).
 */
@Composable
fun ConversationScreen(
    recorder: TurnRecorder,
    pipeline: TurnPipeline,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val capture by recorder.state.collectAsState()
    val turn by pipeline.state.collectAsState()
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val ask = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    LaunchedEffect(Unit) { pipeline.prepare() }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!granted) {
            Text(
                stringResource(R.string.capture_permission),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = { ask.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text(stringResource(R.string.capture_grant))
            }
            return@Column
        }

        if (turn.exchanges.isEmpty()) {
            Text(
                stringResource(R.string.conversation_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        turn.exchanges.forEachIndexed { at, exchange ->
            Said(exchange, turn.marking[at], at in turn.faulty, turn.sounds[at])
        }

        (turn.analysis as? Readiness.Off)?.let { off ->
            // An option that is off carries its reason, or it reads as a bug in the app
            // rather than as something the app has not been given.
            Text(
                stringResource(off.reason) + (off.detail?.let { " ($it)" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        turn.failure?.let { said ->
            Text(
                stringResource(R.string.turn_failed, said),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = { scope.launch { pipeline.submit() } }) {
                Text(stringResource(R.string.turn_retry))
            }
        }

        val busy = turn.phase != Phase.Idle
        Text(
            when {
                turn.phase == Phase.Hearing -> stringResource(R.string.phase_hearing)
                turn.phase == Phase.Thinking -> stringResource(R.string.phase_thinking)
                turn.phase == Phase.Speaking -> stringResource(R.string.phase_speaking)
                capture.recording ->
                    stringResource(R.string.capture_recording, seconds(capture.elapsedMs))
                capture.hasAudio ->
                    stringResource(R.string.capture_held, seconds(capture.elapsedMs))
                else -> stringResource(R.string.capture_hold)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Surface(
            shape = CircleShape,
            color = when {
                busy -> MaterialTheme.colorScheme.surfaceVariant
                capture.recording -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .size(150.dp)
                .pointerInput(busy, capture.full) {
                    detectTapGestures(
                        onPress = {
                            // A held button while the chain is busy would record over an
                            // answer the learner is still hearing.
                            if (busy) return@detectTapGestures
                            recorder.hold(scope)
                            tryAwaitRelease()
                            recorder.release()
                        }
                    )
                },
        ) {}

        if (capture.full) {
            Text(
                stringResource(R.string.capture_full, TurnRecorder.CEILING_MS / 1000),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        if (capture.hasAudio && !busy) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { recorder.discard() }) {
                    Text(stringResource(R.string.capture_redo))
                }
                Button(onClick = {
                    scope.launch { recorder.finish()?.let { pipeline.submit(it) } }
                }) {
                    Text(stringResource(R.string.capture_send))
                }
            }
        }

        Text(
            stringResource(R.string.capture_source, stringResource(recorder.source.label)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        DebugPanel()
    }
}

@Composable
private fun Said(
    exchange: Exchange,
    marking: TurnMarking?,
    faulty: Boolean,
    sounds: List<AnalysedSound>?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(
                if (exchange.fromLearner) R.string.speaker_learner else R.string.speaker_ai
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (marking != null) MarkedTurn(marking, modifier = Modifier.fillMaxWidth())
        else Text(exchange.text, style = MaterialTheme.typography.bodyMedium)
        if (sounds != null && Trace.on) {
            var open by rememberSaveable { mutableStateOf(false) }
            TextButton(onClick = { open = !open }) {
                Text(
                    stringResource(
                        if (open) R.string.readout_hide else R.string.readout_show,
                        sounds.count { it.points > NOISE_BAND },
                        sounds.size,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            if (open) AnalysisReadout(sounds)
        }
        if (faulty) {
            // The whole turn, for want of the span. The doc asks for the portion concerned
            // and the model does not return one yet (`../../../../../../TODO.md`), so this
            // says where the fault is only as far as the sentence -- and says nothing about
            // pronunciation, which behind a closed gate was never measured.
            Text(
                stringResource(R.string.turn_grammar_marked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

private fun seconds(ms: Int): String = "%.1f s".format(ms / 1000f)
