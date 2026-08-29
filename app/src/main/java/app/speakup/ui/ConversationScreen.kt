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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.speakup.R
import app.speakup.capture.TurnRecorder
import kotlinx.coroutines.launch
import java.io.File

/**
 * The turn: hold to speak, release to think, press again to carry on, send when it is said.
 *
 * Only the capture is wired. What the sent file goes on to -- recognition, the language
 * model, the voice coming back, the marks -- is the next step, and this screen is where it
 * lands (`../../../../../../TODO.md`, chantier 0).
 */
@Composable
fun ConversationScreen(recorder: TurnRecorder, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by recorder.state.collectAsState()
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var kept by remember { mutableStateOf<File?>(null) }
    val ask = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
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

        Text(
            when {
                state.recording -> stringResource(R.string.capture_recording, seconds(state.elapsedMs))
                state.hasAudio -> stringResource(R.string.capture_held, seconds(state.elapsedMs))
                else -> stringResource(R.string.capture_hold)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Surface(
            shape = CircleShape,
            color = if (state.recording) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(160.dp)
                .pointerInput(state.full) {
                    detectTapGestures(
                        onPress = {
                            recorder.hold(scope)
                            tryAwaitRelease()
                            recorder.release()
                        }
                    )
                },
        ) {}

        if (state.full) {
            Text(
                stringResource(R.string.capture_full, TurnRecorder.CEILING_MS / 1000),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        if (state.hasAudio) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                OutlinedButton(onClick = { recorder.discard(); kept = null }) {
                    Text(stringResource(R.string.capture_redo))
                }
                Button(onClick = { scope.launch { kept = recorder.finish() } }) {
                    Text(stringResource(R.string.capture_send))
                }
            }
        }

        kept?.let {
            Text(
                stringResource(R.string.capture_kept, it.name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun seconds(ms: Int): String = "%.1f s".format(ms / 1000f)
