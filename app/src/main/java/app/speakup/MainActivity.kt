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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.speakup.analysis.Analyses
import app.speakup.capture.TurnRecorder
import app.speakup.conversation.TurnPipeline
import app.speakup.providers.ChosenConversation
import app.speakup.providers.ChosenRecognition
import app.speakup.providers.ChosenSynthesis
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import app.speakup.store.Archive
import app.speakup.ui.ConversationScreen
import app.speakup.ui.ConversationsScreen
import app.speakup.ui.MarkingPrototypeScreen
import app.speakup.ui.SettingsScreen
import app.speakup.ui.theme.SpeakupTheme

/** What the palette preference holds when the spare is the one in force. */
const val SPARE = "spare"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SecretStore(applicationContext)
        val recorder = TurnRecorder(applicationContext)
        // The three links are resolved at the moment they are used, not here: the user
        // picks a provider per link in the settings, and the next turn uses it.
        val pipeline = TurnPipeline(
            context = applicationContext,
            recognition = ChosenRecognition(store),
            conversation = ChosenConversation(store),
            synthesis = ChosenSynthesis(applicationContext, store),
            analysis = Analyses.onDevice(applicationContext),
            archive = Archive.of(applicationContext).dao(),
        )
        setContent {
            // Read here rather than inside the theme: the palette is a preference like any
            // other, and the store is what holds preferences.
            val stored by store.values().collectAsState(initial = emptyMap())
            // Material still dresses the buttons and the lists; what it no longer holds is
            // anything the marking rests on -- the ground a halo is punched out of, the grid,
            // the rhythms. Those come from SpeakupTheme, nested inside so both are readable.
            MaterialTheme {
                SpeakupTheme(spare = stored[Secret.SparePalette] == SPARE) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Root(store, recorder, pipeline)
                    }
                }
            }
        }
    }
}

/**
 * Four screens and a switch between them.
 *
 * Still no navigation library: the `android` wisdom holds one off below three screens, and
 * what is here is one switch with four positions rather than a graph -- every screen is
 * reached from the same row of buttons and none of them leads to another. A library would
 * buy a back stack nothing has. The switch is saveable, so a rotation does not throw the
 * user back out of the settings -- which is also why no orientation lock is declared.
 */
@Composable
private fun Root(store: SecretStore, recorder: TurnRecorder, pipeline: TurnPipeline) {
    var showingSettings by rememberSaveable { mutableStateOf(false) }
    var showingMarks by rememberSaveable { mutableStateOf(false) }
    var showingConversations by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showingSettings || showingMarks || showingConversations) {
        showingSettings = false
        showingMarks = false
        showingConversations = false
    }

    // Without this the top row sits under the status bar and its buttons pull the
    // notification shade instead of being pressed.
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = {
                showingConversations = !showingConversations
                showingSettings = false
                showingMarks = false
            }) {
                Text(
                    stringResource(
                        if (showingConversations) R.string.conversation_open
                        else R.string.conversations_open
                    )
                )
            }
            TextButton(onClick = {
                showingMarks = !showingMarks
                showingSettings = false
                showingConversations = false
            }) {
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
            } else if (showingConversations) {
                ConversationsScreen(
                    pipeline,
                    // Opening one puts the learner in it. Staying on the list after choosing
                    // would make the choice look like it had not registered.
                    onOpened = { showingConversations = false },
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (showingMarks) {
                MarkingPrototypeScreen()
            } else {
                ConversationScreen(recorder, pipeline, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
