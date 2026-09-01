package app.speakup.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
import app.speakup.conversation.Phase
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.conversation.TurnPipeline
import app.speakup.capture.Playback
import app.speakup.capture.Reference
import app.speakup.conversation.Side
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

        if (turn.utterances.isEmpty()) {
            Text(
                stringResource(R.string.conversation_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        turn.utterances.forEachIndexed { at, spoken ->
            // An utterance that says another again is not drawn where it sits in the run: it
            // is one of the readings grouped under the one it repeats, which is where the
            // learner is looking. The run keeps the order; the screen keeps the grouping.
            if (spoken.repeats != null) return@forEachIndexed
            val readings = turn.readings(at)
            Said(
                spoken = spoken,
                readings = readings,
                recorder = recorder,
                busy = turn.phase != Phase.Idle,
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
                onRedo = { scope.launch { pipeline.redo(at, it) } },
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

/**
 * Which take of this turn is on screen, when there is more than one.
 *
 * Plain numbers and nothing else. There is deliberately no comparison drawn between them --
 * no arrow, no better or worse: the doc rules out showing a trend, and two readings sitting
 * where they can each be looked at is not a trend. What tells the learner whether it went
 * better is the marks themselves, read one attempt at a time.
 */
@Composable
private fun Attempts(count: Int, shown: Int, onShow: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { rank ->
            TextButton(onClick = { onShow(rank) }) {
                Text(
                    "${rank + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (rank == shown) FontWeight.Bold else FontWeight.Normal,
                    color = if (rank == shown) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
 * The small circle is the big one, smaller, and it holds the same way. Saying a sentence
 * again is not a new turn of conversation: it never reaches the language model, and what
 * comes back is the same sentence measured again.
 */
@Composable
private fun Redo(
    recorder: TurnRecorder,
    busy: Boolean,
    side: Side,
    speed: Float,
    onSide: (Side) -> Unit,
    onSpeed: (Float) -> Unit,
    onHear: () -> Unit,
    onSaid: (java.io.File) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val capture by recorder.state.collectAsState()
    var mine by rememberSaveable { mutableStateOf(false) }
    val recording = mine && capture.recording

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
        Surface(
            shape = CircleShape,
            color = if (recording) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(34.dp)
                .pointerInput(busy) {
                    detectTapGestures(onPress = {
                        if (busy) return@detectTapGestures
                        mine = true
                        recorder.hold(scope)
                        tryAwaitRelease()
                        recorder.release()
                        scope.launch {
                            recorder.finish()?.let { onSaid(it) }
                            mine = false
                        }
                    })
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(color = Color.White, radius = size.minDimension * 0.22f)
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
    /** Every reading of this turn, oldest first. Each one is addressed by its place in the run. */
    readings: List<Pair<Int, Utterance>>,
    recorder: TurnRecorder,
    busy: Boolean,
    side: Side,
    speed: Float,
    onSide: (Side) -> Unit,
    onSpeed: (Float) -> Unit,
    onHear: (Int) -> Unit,
    onHearSpan: (Int, Int, Int) -> Unit,
    onHearSound: (Int, AnalysedSound, Side) -> Unit,
    onRedo: (java.io.File) -> Unit,
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
        // The last by default: saying it again is done to improve on the one before, so the
        // newest is the one being looked at. The earlier ones stay reachable rather than
        // being overwritten -- three takes of one sentence against one model is material,
        // and it is only material if all three survive.
        val scope = rememberCoroutineScope()
        var shown by rememberSaveable(readings.size) { mutableStateOf(readings.size - 1) }
        val reading = readings.getOrNull(shown)
        val where = reading?.first
        val marking = reading?.second?.marking
        val sounds = reading?.second?.sounds

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
        if (readings.size > 1) Attempts(readings.size, shown) { shown = it }
        // The redo controls stay: saying it again is exactly the answer to a reading that
        // slid, and taking them away would leave no way out of it.
        if (where != null) {
            Redo(recorder, busy, side, speed, onSide, onSpeed, { onHear(where) }, onRedo)
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
        if (spoken.faulty) {
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
