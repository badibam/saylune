package app.speakup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.speakup.analysis.Analyses
import app.speakup.capture.TurnRecorder
import app.speakup.conversation.TurnPipeline
import app.speakup.providers.ChosenConversation
import app.speakup.providers.ChosenRecognition
import app.speakup.providers.ChosenSynthesis
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Levers
import app.speakup.levers.Positions
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import app.speakup.store.Archive
import androidx.compose.runtime.remember
import app.speakup.ui.Action
import app.speakup.ui.Channel
import app.speakup.ui.Channels
import app.speakup.ui.ConversationScreen
import app.speakup.ui.ConversationsScreen
import app.speakup.ui.Glyphs
import app.speakup.ui.MarkingPrototypeScreen
import app.speakup.ui.MarksMenuScreen
import app.speakup.ui.Scaffold
import app.speakup.ui.SettingsScreen
import app.speakup.ui.Tile
import app.speakup.ui.TitleScreen
import app.speakup.ui.theme.Speakup
import app.speakup.ui.theme.SpeakupTheme
import app.speakup.ui.turnStatus
import java.util.Locale

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
            val dark = isSystemInDarkTheme()
            SpeakupTheme(dark = dark, spare = stored[Secret.SparePalette] == SPARE) {
                // Material still dresses the buttons and the lists that have not been
                // rewritten yet, so it is told which register is in force: left to its own
                // default it painted a light scheme under a night palette, and the ink came
                // out light on a light ground.
                MaterialTheme(
                    colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
                ) {
                    // **The ground is the palette's**, and no longer Material's surface. The
                    // screens are ours now, and a ground the register does not own is a ground
                    // the marking's colours are not measured against.
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Speakup.palette.ground.srgb,
                        // Material works out its content colour from its own scheme, and a
                        // ground it does not know leaves it unspecified -- which came out as
                        // black text on the night plum. The register says what ink is.
                        contentColor = Speakup.palette.ink.srgb,
                    ) {
                        Root(store, recorder, pipeline)
                    }
                }
            }
        }
    }
}

/**
 * Every screen the app has, and the order they descend in.
 *
 * The four that were a four-position switch are a **stack** now, which is what the activity
 * model asks for: screens that go down into one another rather than four doors off one row of
 * buttons. Still no navigation library -- the `android` wisdom holds one off until a graph
 * needs one, and a list one pushes onto and pops off is not a graph.
 */
private enum class Screen { Title, Conversations, Conversation, MarksMenu, Settings, Marks }

/**
 * The app, from its root down.
 *
 * **The root is the title screen** (`pixel-ui.md`, settled 2026-09-06): the four modes, and
 * everything descends from there. Only *Free* has anything behind it, and behind it stands
 * the list of conversations -- which is where the theme tiles will go, a tile being a
 * definition and the list being what stands in for them until they are written.
 *
 * Everything below the root wears the **scaffold**: what is true at the top, what one can do
 * at the bottom. The title screen does not -- it is the root, so back has nowhere to go, and
 * its eight tiles are meant to take the height.
 */
@Composable
private fun Root(store: SecretStore, recorder: TurnRecorder, pipeline: TurnPipeline) {
    val stack = rememberSaveable(
        saver = listSaver<SnapshotStateList<Screen>, String>(
            save = { it.map(Screen::name) },
            restore = { it.map(Screen::valueOf).toMutableStateList() },
        )
    ) { mutableStateListOf(Screen.Title) }

    // Asked once, at the root, and not on the way into a conversation: whether the
    // analysis can run at all is a fact about the device, and an app that discovered it
    // had no engine after somebody had spoken would be finding out too late.
    LaunchedEffect(Unit) { pipeline.prepare() }

    val here = stack.last()
    BackHandler(enabled = stack.size > 1) { stack.removeAt(stack.lastIndex) }

    val stored by store.values().collectAsState(initial = emptyMap())
    // Which marks the conversation menu has turned off. It is a preference of the learner's
    // and never a lever: an activity may take an aid away, none hides a mark.
    val hidden = remember(stored) { Channel.hidden(stored[Secret.HiddenMarks]) }
    val channels = remember(hidden) { Channels(hidden) }

    val turn by pipeline.state.collectAsState()
    val capture by recorder.state.collectAsState()

    // **Held here and not in the conversation screen**: the scaffold's status line is what
    // names what is running -- a turn, a repeat, running or paused -- and that name is what
    // makes `PAUSE` and `SEND` unambiguous at the bottom. Saveable, so a rotation does not
    // turn a repeat into a new turn.
    var repeating by rememberSaveable { mutableStateOf<String?>(null) }

    val back = Action(Glyphs.BACK) { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    // Without this the top line sits under the status bar and the bar at the bottom under the
    // gesture handle.
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        when (here) {
            Screen.Title -> TitleScreen(
                modes = listOf(
                    Tile(R.string.mode_story, reason = R.string.mode_unwritten),
                    Tile(R.string.mode_challenges, reason = R.string.mode_unwritten),
                    Tile(R.string.mode_arcade, reason = R.string.mode_unwritten),
                    Tile(R.string.mode_free) { stack.add(Screen.Conversations) },
                ),
                doors = listOf(
                    Tile(R.string.settings_open) { stack.add(Screen.Settings) },
                    Tile(R.string.measured_marks) { stack.add(Screen.Marks) },
                ),
                modifier = Modifier.fillMaxSize(),
            )

            Screen.Conversations -> Scaffold(
                title = stringResource(R.string.mode_free),
                lives = null,
                status = stringResource(R.string.conversations_pick),
                actions = listOf(back),
            ) {
                ConversationsScreen(
                    pipeline,
                    // Opening one puts the learner in it. Staying on the list after choosing
                    // would make the choice look like it had not registered.
                    onOpened = { stack.add(Screen.Conversation) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Screen.Conversation -> Scaffold(
                title = turn.definition.short.inLanguage(Locale.getDefault().language),
                lives = livesLeft(turn.positions),
                status = turnStatus(turn, capture, repeating),
                actions = listOf(
                    back,
                    // Both are the conversation's own, and both are off with their reason:
                    // the marks menu comes with the redrawn turn, and no screen sets a lever
                    // yet, before a sitting or during one.
                    Action(Glyphs.EYE) { stack.add(Screen.MarksMenu) },
                    Action(Glyphs.LEVERS, reason = R.string.action_levers_unwritten),
                ),
            ) {
                ConversationScreen(
                    recorder, pipeline,
                    repeating = repeating,
                    onRepeating = { repeating = it },
                    channels = channels,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Screen.MarksMenu -> Scaffold(
                title = stringResource(R.string.channels_what),
                lives = null,
                status = stringResource(R.string.channels_measured),
                actions = listOf(back),
            ) {
                MarksMenuScreen(store, hidden, modifier = Modifier.fillMaxSize())
            }

            Screen.Settings -> Scaffold(
                title = stringResource(R.string.settings_open),
                lives = null,
                status = stringResource(R.string.settings_what),
                actions = listOf(back),
            ) {
                SettingsScreen(store, modifier = Modifier.fillMaxSize())
            }

            Screen.Marks -> Scaffold(
                title = stringResource(R.string.measured_marks),
                lives = null,
                status = stringResource(R.string.marks_what),
                actions = listOf(back),
            ) {
                MarkingPrototypeScreen()
            }
        }
    }
}

/**
 * How many lives are left, or null where the sitting counts none.
 *
 * **The lives are a lever and their position is the number left** -- not an allowance set
 * beside a counter (`docs/design/activity-model.md`). So this reads the two levers the
 * catalogue declares and nothing else: whether lives are counted at all, and where the count
 * stands. A free conversation counts none, so the field is simply absent from its status line.
 */
private fun livesLeft(positions: Positions): Int? {
    val counted = (positions.of(Levers.LIVES.key) as? At)?.name == "counted"
    return if (counted) (positions.of(Levers.LIVES_LEFT.key) as? Count)?.n else null
}
