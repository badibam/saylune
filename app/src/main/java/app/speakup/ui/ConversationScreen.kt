package app.speakup.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.speakup.R
import app.speakup.capture.TurnRecorder
import app.speakup.conversation.Attempt
import app.speakup.conversation.Closing
import app.speakup.conversation.Speaker
import app.speakup.conversation.Standing
import app.speakup.conversation.Utterance
import app.speakup.providers.words
import app.speakup.conversation.Phase
import androidx.compose.runtime.saveable.rememberSaveable
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Readiness
import app.speakup.capture.Capture
import app.speakup.capture.CaptureState
import app.speakup.capture.Ending
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Positions
import app.speakup.levers.Levers
import app.speakup.conversation.ConversationState
import app.speakup.conversation.TurnPipeline
import app.speakup.capture.Playback
import app.speakup.capture.Reference
import app.speakup.conversation.Side
import app.speakup.marking.TurnMarking
import app.speakup.ui.theme.Speakup
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
    /**
     * Whose recording is running: null for a new turn, the identity of the passage being said
     * again otherwise.
     *
     * **Held above this screen**, because the scaffold's status line is what names what is
     * running -- *a turn*, *a repeat*, running or paused -- and that name is what makes the
     * two shared buttons at the bottom safe. A fact two objects read belongs above both.
     */
    repeating: String?,
    onRepeating: (String?) -> Unit,
    /** Which marks the conversation menu has left on. */
    channels: Channels,
    /** Open the summary of the passage whose last attempt is this one. */
    onNotes: (String) -> Unit,
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

    // The sitting's capture, read in one place: which position, what the two clocks are set
    // to, and whether a take may be thrown away. Every button below reads these rather than
    // asking the catalogue itself, so no two of them can disagree about the position.
    val position = (turn.positions.of(Levers.CAPTURE.key) as? At)?.name ?: Levers.BY_HAND
    val arms = position != Levers.BY_HAND
    val settings = Capture(
        ceilingMs = seconds(turn.positions, Levers.TURN_LENGTH.key, TurnRecorder.CEILING_MS),
        // Null wherever the silence does not send, which is the first two positions: there
        // the learner is the only one who sends. Not zero -- zero would send at once.
        sendsAfterMs =
            if (position == Levers.ARMED_AND_SENDING)
                seconds(turn.positions, Levers.SILENCE_THRESHOLD.key, 5_000)
            else null,
    )

    // **`SEND` exists at all three positions**, and what changes from one to the next is what
    // *arms* the mic, never what sends. So one lambda, called by the button and by the clock
    // alike, and the take goes wherever `repeating` says.
    val send: () -> Unit = {
        scope.launch {
            val said = repeating
            // Read before `send` clears the state: the two facts belong to the take, and
            // the take is about to stop existing as a recording in progress.
            val ending = capture.ending
            // **Which repair this is comes from the passage's standing, never from the
            // gesture**: the button is the same one either way. A rewording gives fresh words,
            // so the exchange is remade on them; a repeat says the same ones and relaunches
            // nothing -- it is pipe B alone, on a text already settled.
            val rewording = turn.standing() == Standing.ToReword
            recorder.send()?.let { take ->
                when {
                    said == null -> pipeline.submit(take, position, ending)
                    rewording -> pipeline.reword(said, take, position, ending)
                    else -> pipeline.redo(said, take, position, ending)
                }
            }
            onRepeating(null)
        }
    }

    // **A clock closed the turn**: it is truncated and sent as it stands, never cut into two
    // turns. Sending is the same gesture the hand would have made, so it is the same lambda.
    LaunchedEffect(capture.ending) { if (capture.ending != null) send() }

    // **The mic never arms before the AI has finished answering**, and it never arms on its
    // own while a passage waits for a repair -- which is what recreates the press a passage
    // closes on. The second half has nothing to read yet: the passage's four states arrive
    // with step 13, and until then nothing ever waits, so this reads false and is written
    // down as owed (`../../../../../../TODO.md`).
    val repairWaits = false
    LaunchedEffect(arms, busyOf(turn.phase), repairWaits, turn.utterances.size) {
        if (!arms || repairWaits || turn.phase != Phase.Idle) return@LaunchedEffect
        if (capture.recording || capture.hasAudio) return@LaunchedEffect
        // The preparation: the time between the end of the AI's answer and the mic being
        // armed. It lives outside the turn, so it touches no measure.
        val wait = seconds(turn.positions, Levers.PREPARATION.key, 0)
        if (wait > 0) kotlinx.coroutines.delay(wait.toLong())
        onRepeating(null)
        recorder.open(scope, settings)
    }

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
        // The passage still open, which is the last one. **The small button lives only on it**,
        // the attempts stopping at the close -- the passages above keep what listens, reads
        // back and opens their measures, never what retakes.
        //
        // **And it disappears where there is nothing left to spend**: a budget run out, or a
        // lever set to zero, is a button that is not there rather than one that is greyed.
        // Greying is for what is momentarily impossible; absence is for what has no object.
        val open = turn.open()
        val standing = turn.standing()
        val retaking = when (standing) {
            Standing.ToReword -> Attempt.Rewording
            Standing.ToSayAgain -> Attempt.Repeat
            else -> null
        }
        val retakes = open != null &&
            (retaking == null || open.spare(retaking, turn.positions))
        // What the AI's turn shows, which is a lever's position and never a preference: the
        // scrambled text by default, the ear being the main channel and a legible text
        // preempting the listening.
        val display = Display.of(turn.positions)
        turn.utterances.forEach { spoken ->
            // An utterance that says another again is not drawn where it sits in the run: it
            // is one of the readings grouped under the one it repeats, which is where the
            // learner is looking. The run keeps the order; the screen keeps the grouping.
            if (spoken.repeats != null) return@forEach
            if (!spoken.speaker.isLearner) {
                Heard(spoken.text, shortName(turn, spoken.speaker), display, channels)
                return@forEach
            }
            val readings = turn.readings(spoken.id)
            Said(
                spoken = spoken,
                speaker = shortName(turn, spoken.speaker),
                channels = channels,
                readings = readings,
                open = spoken.id == open?.opener?.id && retakes,
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
                    onRepeating(spoken.id)
                    recorder.open(scope, settings)
                },
                onNotes = onNotes,
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
                .clickable(enabled = !busy && !recordingSomething && turn.closes()) {
                    // **It closes the previous passage and opens mine**, which is exactly what
                    // happens: a turn of speech and not a next page.
                    scope.launch {
                        pipeline.close()
                        onRepeating(null)
                        recorder.open(scope, settings)
                    }
                },
        ) {}

        // **The two countdowns, visible at all times** -- the turn's time and the silence
        // running now. Two times running out, shown the same way. The silence one only shows
        // where a silence sends, there being no countdown otherwise.
        if (capture.recording) {
            Text(
                if (settings.sendsAfterMs != null) stringResource(
                    R.string.capture_clocks,
                    seconds(capture.elapsedMs), seconds(settings.ceilingMs),
                    seconds(capture.silenceMs), seconds(settings.sendsAfterMs),
                ) else stringResource(
                    R.string.capture_clock,
                    seconds(capture.elapsedMs), seconds(settings.ceilingMs),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        // With the sitting's clocks, like every other opening: carrying on
                        // resumes the same turn, and a turn does not change how long it may
                        // run because the thumb stopped it once.
                        if (capture.recording) recorder.pause()
                        else recorder.open(scope, settings)
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
                // **Throwing a take away is a lever**, `jeter-la-prise`. Offered freely it
                // walks around the attempt counters -- a challenge granting one attempt could
                // be restarted ten times -- so a challenge has to be able to close it. It has
                // **no object at the third position**, where a clock sends too: the silence
                // one hesitates through is what sends the take, so the button would be a race
                // against the pendulum, lost by whoever thinks.
                val mayDiscard = turn.positions.live(Levers.DISCARD_TAKE.key) &&
                    (turn.positions.of(Levers.DISCARD_TAKE.key) as? At)?.name == "allowed"
                if (mayDiscard) {
                    OutlinedButton(onClick = { recorder.discard(); onRepeating(null) }) {
                        Text(stringResource(R.string.capture_redo))
                    }
                }
                Button(onClick = send) {
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

@Composable
private fun Said(
    spoken: Utterance,
    /** The short name of whoever said it, which the line that names the turn carries. */
    speaker: String,
    channels: Channels,
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
    onNotes: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // The last, and only the last: saying it again is done to improve on the one before,
        // so the newest is what the learner knows how to say now.
        val shown = readings.lastOrNull()
        TurnLabel(
            name = speaker,
            following = shown?.judged?.following,
            // The distance and the side, which are two facts and are kept as two: the figure
            // is symmetric so that twice as slow and twice as fast weigh the same in the note,
            // and the chevrons are the one place the side is read. An attempt that measured
            // neither leaves the slot empty, which says *not measured*.
            pace = shown?.let { attempt ->
                attempt.measured[PACE]?.let { far ->
                    attempt.slower?.let { if (it) -far else far }
                }
            },
            channels = channels,
        )
        // The earlier takes stay in the base for the measures and for a bench, and no screen
        // shows them.
        val scope = rememberCoroutineScope()
        val reading = shown
        val where = reading?.id
        val marking = reading?.marking
        val sounds = reading?.sounds

        if (marking != null) {
            MarkedTurn(
                marking,
                reading.judged,
                channels = channels,
                sounds = sounds.orEmpty(),
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
        } else Text(
            spoken.text,
            style = Speakup.type.text,
            color = Speakup.palette.ink.srgb,
        )
        // Every passage that carries a recording gets the row -- listening back is what a
        // measured turn is for. What the row holds depends on whether the passage is open.
        if (where != null) {
            val context = LocalContext.current
            var reading by rememberSaveable { mutableStateOf(false) }
            Commands(
                open = open,
                busy = busy,
                mine = mine,
                running = running,
                side = side,
                speed = speed,
                sounds = !sounds.isNullOrEmpty(),
                onSide = onSide,
                onSpeed = onSpeed,
                onHear = { onHear(where) },
                onNotes = { onNotes(where) },
                onRead = { reading = !reading },
                onOpen = onOpenRepeat,
            )
            // **The readout is a piece of the app now** and no longer something the trace
            // gated: it is what the magnifier of the row opens, one notch below the marks --
            // the inventory sound by sound, where the row above plays the whole phrase.
            if (reading && sounds != null) {
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
        // **A turn a clock closed says so, on itself**: it is truncated and sent as it
        // stands, and someone who does not know that reads a sentence that stops mid-word as
        // the app having lost half of it.
        spoken.ending?.let { ending ->
            Text(
                stringResource(
                    when (ending) {
                        Ending.ByLength -> R.string.capture_ended_length
                        Ending.BySilence -> R.string.capture_ended_silence
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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

/**
 * What the narrator says about the turn right now.
 *
 * **It names what is running**, and that is what makes the two shared buttons at the bottom
 * safe: not *recording* but *a turn*, *a repeat*, running or paused. Without the name, `PAUSE`
 * and `SEND` would be two buttons whose effect depends on something the screen never said.
 *
 * **Nothing ever waits without a visible reason.** The words' gate names the aptitudes in
 * cause -- all of them, never the worst -- where the sound's gate names nothing, being wired
 * to elocution and fluency alone and so saying a constant.
 *
 * It lives here rather than in the scaffold because everything it reads is the conversation's,
 * and the scaffold's second line takes a string from whatever screen is up.
 */
@Composable
fun turnStatus(turn: ConversationState, capture: CaptureState, repeating: String?): String = when {
    turn.phase == Phase.Hearing -> stringResource(R.string.phase_hearing)
    turn.phase == Phase.Thinking -> stringResource(R.string.phase_thinking)
    turn.phase == Phase.Speaking -> stringResource(R.string.phase_speaking)
    turn.phase == Phase.Measuring -> stringResource(R.string.phase_measuring)
    capture.recording && repeating != null ->
        stringResource(R.string.capture_repeat_running, seconds(capture.elapsedMs))
    capture.recording ->
        stringResource(R.string.capture_turn_running, seconds(capture.elapsedMs))
    capture.hasAudio && repeating != null ->
        stringResource(R.string.capture_repeat_paused, seconds(capture.elapsedMs))
    capture.hasAudio ->
        stringResource(R.string.capture_turn_paused, seconds(capture.elapsedMs))
    turn.standing() == Standing.ToReword -> stringResource(
        R.string.passage_reword,
        (turn.wordsGate as? Closing.Aptitudes)?.names?.joinToString(", ")
            ?: stringResource(R.string.passage_no_matter),
    )
    turn.standing() == Standing.ToSayAgain -> stringResource(R.string.passage_say_again)
    else -> stringResource(R.string.capture_press)
}

/** The pace's path in the sheet tree, which is how a figure is addressed on an utterance. */
private const val PACE = "fluency/pace"

private fun seconds(ms: Int): String = "%.1f s".format(ms / 1000f)

/** Whether the chain holds the screen. Named so an effect can key on it. */
private fun busyOf(phase: Phase): Boolean = phase != Phase.Idle

/**
 * A numeric lever read in milliseconds, or [fallback] when it carries no number.
 *
 * A lever with no number is one whose position is *no maximum* -- the shape the catalogue
 * gives to a ceiling that does not exist. The clocks want a number either way, so the caller
 * says what standing for *no limit* means to it.
 */
private fun seconds(positions: Positions, key: String, fallback: Int): Int =
    (positions.of(key) as? Count)?.n?.times(1000) ?: fallback


/**
 * The short name of [who], as the line that names a turn carries it.
 *
 * **A definition declares one per character**, because the app truncates in any case and the
 * author is better placed than the truncation to choose what survives. The learner is not in
 * the cast -- his key is reserved -- so he keeps the app's own word for himself.
 */
@Composable
private fun shortName(turn: ConversationState, who: Speaker): String = when {
    who.isLearner -> stringResource(R.string.speaker_learner)
    else -> turn.activity.cast.firstOrNull { it.key == who.key }
        ?.short?.inLanguage(java.util.Locale.getDefault().language)
        ?: stringResource(R.string.speaker_ai)
}
