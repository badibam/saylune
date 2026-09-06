package app.speakup.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.speakup.R
import app.speakup.capture.TurnRecorder
import app.speakup.conversation.Speaker
import app.speakup.conversation.Utterance
import app.speakup.providers.words
import app.speakup.conversation.Phase
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.levers.At
import app.speakup.levers.Levers
import app.speakup.conversation.TurnPipeline
import app.speakup.capture.Playback
import app.speakup.capture.Reference
import app.speakup.conversation.Side
import app.speakup.marking.TurnMarking
import kotlinx.coroutines.launch

/**
 * The turn, end to end: press to speak, pause to think, press again to carry on, send.
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

    // Whose recording is running: null for a new turn, the identity of the passage being
    // said again otherwise. **The bottom commands everything that records, whoever started
    // it**, so this is what tells the shared `send` where to hand the take -- and what lets
    // the status line name what is running, without which two shared buttons would be
    // ambiguous. Saveable: a rotation must not turn a repeat into a new turn.
    var repeating by rememberSaveable { mutableStateOf<String?>(null) }

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

        if (turn.utterances.isEmpty()) {
            Text(
                stringResource(R.string.conversation_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // The passage still open, which is the last one the learner said. Saying it again
        // stops when a passage closes, so it is the only one that offers the small button.
        // Nothing closes a passage today but starting the next -- the big button that will
        // close it by hand comes with the passage's four states, further down the plan.
        val open = turn.utterances.lastOrNull { it.speaker == Speaker.Learner && it.repeats == null }
        turn.utterances.forEach { spoken ->
            // An utterance that says another again is not drawn where it sits in the run: it
            // is one of the readings grouped under the one it repeats, which is where the
            // learner is looking. The run keeps the order; the screen keeps the grouping.
            if (spoken.repeats != null) return@forEach
            val readings = turn.readings(spoken.id)
            Said(
                spoken = spoken,
                readings = readings,
                open = spoken.id == open?.id,
                busy = turn.phase != Phase.Idle,
                mine = repeating == spoken.id,
                running = capture.recording || capture.hasAudio,
                side = turn.side,
                speed = turn.speed,
                onSide = pipeline::side,
                onSpeed = pipeline::speed,
                onHear = { where -> scope.launch { pipeline.hear(where) } },
                onHearSpan = { where, from, to ->
                    scope.launch { pipeline.hear(where, from, to) }
                },
                onHearSound = { where, sound, side ->
                    scope.launch { pipeline.hear(where, sound, side) }
                },
                // The small button only opens; the bottom is what pauses and sends, and it
                // is `repeating` that says where the take goes when it does.
                onOpenRepeat = {
                    repeating = spoken.id
                    recorder.open(scope)
                },
            )
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
        // **The status line names what is running**, and that is what makes the shared
        // buttons safe: not *recording* but *a turn*, *a repeat*, running or paused. Without
        // the name, `PAUSE` and `SEND` would be two buttons whose effect depends on
        // something the screen never said.
        Text(
            when {
                turn.phase == Phase.Hearing -> stringResource(R.string.phase_hearing)
                turn.phase == Phase.Thinking -> stringResource(R.string.phase_thinking)
                turn.phase == Phase.Speaking -> stringResource(R.string.phase_speaking)
                capture.recording && repeating != null ->
                    stringResource(R.string.capture_repeat_running, seconds(capture.elapsedMs))
                capture.recording ->
                    stringResource(R.string.capture_turn_running, seconds(capture.elapsedMs))
                capture.hasAudio && repeating != null ->
                    stringResource(R.string.capture_repeat_paused, seconds(capture.elapsedMs))
                capture.hasAudio ->
                    stringResource(R.string.capture_turn_paused, seconds(capture.elapsedMs))
                else -> stringResource(R.string.capture_press)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        // **The big button says a turn of speech and not a next page**: it opens one of the
        // learner's own. It is greyed while anything records, whoever started it -- one does
        // not begin a new turn while speaking -- rather than turning into the pause, which
        // would be a second personality on one button.
        val recordingSomething = capture.recording || capture.hasAudio
        Surface(
            shape = CircleShape,
            color = when {
                busy || recordingSomething -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .size(150.dp)
                .clickable(enabled = !busy && !recordingSomething && !capture.full) {
                    repeating = null
                    recorder.open(scope)
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

        // **The bottom commands everything that records, whichever button started it.** The
        // big one opens a new turn, a passage's small one opens a repeat, and in both cases
        // these are what follow. Doubling them into the row under each passage would fit,
        // and it is not the room that rules it out: it would be two `SEND`s doing the same
        // work in two places, the one to press depending on what was started.
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // **`PAUSE` exists at the first capture position and nowhere else** -- the one
            // position that has a pause at all. Its absence is the right shape on screen,
            // and the lever's phrase is the right shape at the moment one chooses.
            if ((turn.positions.of(Levers.CAPTURE.key) as? At)?.name == Levers.BY_HAND) {
                OutlinedButton(
                    enabled = capture.recording || (capture.hasAudio && !busy),
                    onClick = {
                        if (capture.recording) recorder.pause() else recorder.open(scope)
                    },
                ) {
                    Text(
                        stringResource(
                            if (capture.recording) R.string.capture_pause
                            else R.string.capture_resume
                        )
                    )
                }
            }
            // Absent and not greyed when nothing records: there is no take to send.
            if (capture.hasAudio && !busy) {
                OutlinedButton(onClick = { recorder.discard(); repeating = null }) {
                    Text(stringResource(R.string.capture_redo))
                }
                Button(onClick = {
                    scope.launch {
                        val said = repeating
                        recorder.send()?.let { file ->
                            if (said != null) pipeline.redo(said, file)
                            else pipeline.submit(file)
                        }
                        repeating = null
                    }
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

/**
 * Which recording the play button and a tap on a word reach.
 *
 * Two words rather than an icon: "model" and "you" are the two things being compared
 * everywhere else on this screen, and a glyph for either would have to be learnt.
 */
@Composable
private fun SideChoice(side: Side, onSide: (Side) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Side.entries.forEach { option ->
            val picked = option == side
            Text(
                stringResource(
                    if (option == Side.Model) R.string.side_model else R.string.side_learner
                ),
                modifier = Modifier
                    .clickable { onSide(option) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (picked) FontWeight.Bold else FontWeight.Normal,
                color = if (picked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * How fast anything played by hand is played, cycling through the three.
 *
 * Slower keeps the pitch: the stretch is a time stretch and not a resampling, which would
 * take the formants down with the rate and turn one vowel into another. A third of speed is
 * where a fast reduction stops being a blur and starts being a sequence of sounds.
 */
@Composable
private fun SpeedChoice(speed: Float, onSpeed: (Float) -> Unit) {
    val next = SPEEDS[(SPEEDS.indexOfFirst { it == speed }.coerceAtLeast(0) + 1) % SPEEDS.size]
    Text(
        stringResource(R.string.speed_times, label(speed)),
        modifier = Modifier
            .clickable { onSpeed(next) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (speed != 1f) FontWeight.Bold else FontWeight.Normal,
        color = if (speed != 1f) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private val SPEEDS = listOf(1f, 0.5f, 0.33f)

private fun label(speed: Float) = when (speed) {
    1f -> "1"
    0.5f -> "0.5"
    else -> "0.33"
}

/**
 * The word [offset] falls in, as a range of the text -- or null between two words.
 *
 * Whitespace decides, which is the same cut the join makes when it hands the sounds out to
 * the words; anything finer here would name a word the analysis never spoke of.
 */
private fun spanOfWord(text: String, offset: Int): IntRange? {
    if (offset !in text.indices || text[offset].isWhitespace()) return null
    var start = offset
    while (start > 0 && !text[start - 1].isWhitespace()) start--
    var stop = offset
    while (stop + 1 < text.length && !text[stop + 1].isWhitespace()) stop++
    return start..stop
}

/**
 * Where [word] sits in one of the two recordings, in milliseconds.
 *
 * Read off the sounds the word covers rather than measured again: both sides already carry
 * their own bounds for every sound, so the model's word and the learner's are the same word
 * by construction and cannot drift apart.
 */
private fun heard(
    sounds: List<AnalysedSound>,
    word: IntRange,
    side: Side,
): Pair<Int, Int>? {
    val inside = sounds.filter { sound ->
        sound.at.any { it in word }
    }.map { if (side == Side.Model) it.modelMs else it.saidMs }
    if (inside.isEmpty()) return null
    return inside.minOf { it.first } to inside.maxOf { it.last }
}

/**
 * Hear the model, and say it again -- the two halves of the remedy, on the turn itself.
 *
 * Drawn rather than lettered: a glyph borrowed to stand for a control is the decoration
 * `dev_base` refuses, and an icon pack is a dependency to rebuild offline for a control that
 * is a triangle and a circle.
 *
 * **The small circle behaves like the big one**: a press opens, it shows itself running, and
 * what follows -- the pause, the send -- is at the bottom. One behaviour to learn for both,
 * which is the whole point of having put the three capture positions on the same gesture.
 * Saying a sentence again is not a new turn of conversation: it never reaches the language
 * model, and what comes back is the same sentence measured again.
 *
 * **Only the open passage carries it**, [open] saying so. Every passage keeps what listens
 * -- the triangle, the side, the speed -- because they read what is already measured; only
 * saying it again adds an attempt, and an attempt added to a closed passage would move a
 * note that the closing rules have already read.
 */
@Composable
private fun Redo(
    open: Boolean,
    busy: Boolean,
    /** Whether the recording that is running, if any, is this passage's. */
    mine: Boolean,
    /** Whether anything at all is recording, whoever started it. */
    running: Boolean,
    side: Side,
    speed: Float,
    onSide: (Side) -> Unit,
    onSpeed: (Float) -> Unit,
    onHear: () -> Unit,
    onOpen: () -> Unit,
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(34.dp).clickable(enabled = !busy, onClick = onHear),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val h = size.minDimension * 0.34f
                val x = size.width * 0.40f
                drawPath(
                    Path().apply {
                        moveTo(x - h * 0.4f, size.height / 2 - h)
                        lineTo(x + h, size.height / 2)
                        lineTo(x - h * 0.4f, size.height / 2 + h)
                        close()
                    },
                    color = Color.White,
                )
            }
        }
        if (open) {
            Surface(
                shape = CircleShape,
                color = when {
                    mine -> MaterialTheme.colorScheme.error
                    busy || running -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .size(34.dp)
                    .clickable(enabled = !busy && !running, onClick = onOpen),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(color = Color.White, radius = size.minDimension * 0.22f)
                }
            }
        }
        // Beside the play button, because their scope is it: the selector says which
        // recording it reaches, the speed says how fast. Both also govern a tap on a word,
        // which is the same gesture one notch finer.
        SideChoice(side, onSide)
        SpeedChoice(speed, onSpeed)
    }
}

@Composable
private fun Said(
    spoken: Utterance,
    /** Every reading of this turn, oldest first. Each one is addressed by its own identity. */
    readings: List<Utterance>,
    /** Whether this is the passage still open, the only one that can be said again. */
    open: Boolean,
    busy: Boolean,
    /** Whether the recording that is running, if any, is this passage's. */
    mine: Boolean,
    /** Whether anything at all is recording, whoever started it. */
    running: Boolean,
    side: Side,
    speed: Float,
    onSide: (Side) -> Unit,
    onSpeed: (Float) -> Unit,
    onHear: (String) -> Unit,
    onHearSpan: (String, Int, Int) -> Unit,
    onHearSound: (String, AnalysedSound, Side) -> Unit,
    onOpenRepeat: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(
                if (spoken.speaker == Speaker.Learner) R.string.speaker_learner
                else R.string.speaker_ai
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // The last, and only the last: saying it again is done to improve on the one before,
        // so the newest is what the learner knows how to say now. The earlier takes stay in
        // the base for the measures and for a bench, and no screen shows them.
        val scope = rememberCoroutineScope()
        val reading = readings.lastOrNull()
        val where = reading?.id
        val marking = reading?.marking
        val sounds = reading?.sounds

        if (marking != null) {
            MarkedTurn(
                marking,
                modifier = Modifier.fillMaxWidth(),
                // A tap anywhere in a word plays that word, on whichever side the selector
                // points at. Its bounds are read off the sounds it covers rather than
                // measured again, so the two recordings stay in step by construction.
                onTapCharacter = { offset ->
                    spanOfWord(spoken.text, offset)?.let { word ->
                        heard(sounds.orEmpty(), word, side)?.let { (from, to) ->
                            // The take being looked at, never the turn's first: its times
                            // are the ones just read off it.
                            where?.let { onHearSpan(it, from, to) }
                        }
                    }
                },
            )
        } else Text(spoken.text, style = MaterialTheme.typography.bodyMedium)
        // Every passage that carries a recording gets the row -- listening back is what a
        // measured turn is for. What the row holds depends on whether the passage is open.
        if (where != null) {
            Redo(open, busy, mine, running, side, speed, onSide, onSpeed,
                 { onHear(where) }, onOpenRepeat)
        }
        if (where != null && sounds != null && Trace.on) {
            val context = LocalContext.current
            var open by rememberSaveable { mutableStateOf(false) }
            TextButton(onClick = { open = !open }) {
                Text(
                    stringResource(
                        if (open) R.string.readout_hide else R.string.readout_show,
                        sounds.count { it.points > NOISE_BAND },
                        sounds.size,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            if (open) {
                AnalysisReadout(
                    spoken.text, sounds, marking?.added.orEmpty(),
                    onHearSound = { sound, which -> onHearSound(where, sound, which) },
                    // The pre-recorded set, played whole: a symbol on its own is already
                    // one sound and there is nothing in it to cut.
                    onHearSymbol = { symbol ->
                        Reference.of(context, symbol)?.let { wav ->
                            scope.launch { Playback.play(wav, speed) }
                        }
                    },
                )
            }
        }
        // The spans are marked and this only says that something was: drawing them on the
        // letters is the redrawn marked turn, further down the plan. Until then the line
        // says as much as the boolean it replaced, off data that says far more.
        if (spoken.judged?.words()?.correctness?.any { it.notch != "ok" } == true) {
            Text(
                stringResource(R.string.turn_grammar_marked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

private fun seconds(ms: Int): String = "%.1f s".format(ms / 1000f)
