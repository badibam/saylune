package app.speakup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.speakup.capture.TurnRecorder
import app.speakup.conversation.TurnPipeline
import app.speakup.providers.AzureRecognition
import app.speakup.providers.DeepseekConversation
import app.speakup.providers.ElevenLabsSynthesis
import app.speakup.keys.SecretStore
import app.speakup.ui.ConversationScreen
import app.speakup.ui.MarkingPrototypeScreen
import app.speakup.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SecretStore(applicationContext)
        val recorder = TurnRecorder(applicationContext)
        val pipeline = TurnPipeline(
            context = applicationContext,
            store = store,
            recognition = AzureRecognition(store),
            conversation = DeepseekConversation(store),
            synthesis = ElevenLabsSynthesis(applicationContext, store),
        )
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Root(store, recorder, pipeline)
                }
            }
        }
    }
}

/**
 * Two screens and a switch between them.
 *
 * No navigation library: the `android` wisdom holds one off below three screens, and the
 * conversation screen that will make a third is not written yet. The switch is saveable, so
 * a rotation does not throw the user back out of the settings -- which is also why no
 * orientation lock is declared.
 */
@Composable
private fun Root(store: SecretStore, recorder: TurnRecorder, pipeline: TurnPipeline) {
    var showingSettings by rememberSaveable { mutableStateOf(false) }
    var showingMarks by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showingSettings || showingMarks) {
        showingSettings = false
        showingMarks = false
    }

    // Without this the top row sits under the status bar and its buttons pull the
    // notification shade instead of being pressed.
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { showingMarks = !showingMarks; showingSettings = false }) {
                Text(
                    stringResource(
                        if (showingMarks) R.string.conversation_open else R.string.measured_marks
                    )
                )
            }
            TextButton(onClick = { showingSettings = !showingSettings }) {
                Text(
                    stringResource(
                        if (showingSettings) R.string.settings_close else R.string.settings_open
                    )
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            if (showingSettings) {
                SettingsScreen(store, modifier = Modifier.fillMaxSize())
            } else if (showingMarks) {
                MarkingPrototypeScreen()
            } else {
                ConversationScreen(recorder, pipeline, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
