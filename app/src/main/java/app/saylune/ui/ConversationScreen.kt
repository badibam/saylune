package app.saylune.ui

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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
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
import androidx.core.content.ContextCompat
import app.saylune.R
import app.saylune.capture.TurnRecorder
import app.saylune.conversation.Attempt
import app.saylune.conversation.Closing
import app.saylune.conversation.Speaker
import app.saylune.conversation.Standing
import app.saylune.conversation.Utterance
import app.saylune.conversation.Phase
import app.saylune.debug.Trace
import androidx.compose.runtime.saveable.rememberSaveable
import app.saylune.analysis.AnalysedSound
import app.saylune.analysis.Readiness
import app.saylune.capture.Capture
import app.saylune.capture.CaptureState
import app.saylune.capture.Ending
import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Positions
import app.saylune.levers.Levers
import app.saylune.conversation.ConversationState
import app.saylune.conversation.TurnPipeline
import app.saylune.capture.Playback
import app.saylune.capture.Reference
import app.saylune.conversation.Side
import app.saylune.marking.AddedSound
import app.saylune.marking.TurnMarking
import app.saylune.ui.theme.Saylune
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
    /**
     * Whether the word marks come off a passage the words' gate has let through.
     *
     * **A drawing and never a measure**: the marks are stored, the note is what it was, and
     * turning the setting off shows them again. What it settles is how long a mark stays on
     * screen -- they are kept while the gate is holding the passage back, which is the moment
     * they say what to change, and they go once there is nothing left to do about them.
     */
    dropWordMarks: Boolean,
    /** Open the summary of the passage whose last attempt is this one. */
    onNotes: (String) -> Unit,
    /** Open what went out to the model for this passage, named by the utterance that opened it. */
    onPrompt: (String) -> Unit,
    /**
     * Show the summary of the passage that has just closed, or null where the learner has
     * turned that off.
     *
     * **The same screen through the other door**: pushed rather than asked for, which is the
     * learner's setting and not the mode's -- what the mode decides is the letters. It is
     * called at the **send** of the next take, not at the close: see `closed` below.
     */
    onClosed: ((String) -> Unit)?,
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
    val settings = Capture.of(turn.positions)

    // The passage that closed when this turn was opened, waiting for the take to go.
    //
    // **The summary is pushed at the send and not at the close**, though the close is where the
    // note becomes final. Pushed at the close it covered the screen at the instant the mic
    // opened -- one is about to speak, and what is put in front of one is a page about the
    // sentence before. At the send, the wait for the answer has just begun and there is nothing
    // else to do with those seconds, which is the argument that made it pushed at all.
    var closed by remember { mutableStateOf<String?>(null) }

    // **`SEND` exists at all three positions**, and what changes from one to the next is what
    // *arms* the mic, never what sends. So one lambda, called by the button and by the clock
    // alike, and the take goes wherever `repeating` says.
    val send: () -> Unit = {
        // **The pipeline's scope and not this screen's.** Sending a take pushes the passage's
        // notes over this screen, so launched here the turn would be cancelled by the very
        // gesture that started it -- measured, and it left the phase on *hearing* for good.
        pipeline.turns.launch {
            val said = repeating
            // Read before `send` clears the state: the two facts belong to the take, and
            // the take is about to stop existing as a recording in progress.
            val ending = capture.ending
            // **Which repair this is comes from the passage's standing, never from the
            // gesture**: the button is the same one either way. A rewording gives fresh words,
            // so the exchange is remade on them; a repeat says the same ones and relaunches
            // nothing -- it is pipe B alone, on a text already settled.
            val rewording = turn.standing() == Standing.ToReword
            val sent = recorder.send()
            // **The retake is over the instant its take leaves the recorder**, not when its
            // measure comes back. Cleared after the measure, it wiped a retake the learner
            // opened in between -- tapped as the marks landed -- and that take went out as a
            // passage of its own.
            onRepeating(null)
            sent?.let { take ->
                // The passage that closed goes up now, before the chain is launched: what it
                // reports is settled, and the seconds it fills are the ones about to start.
                // A retake closes nothing, so it pushes nothing.
                if (said == null) closed?.let { onClosed?.invoke(it) }
                closed = null
                when {
                    said == null -> pipeline.submit(take, position, ending)
                    rewording -> pipeline.reword(said, take, position, ending)
                    else -> pipeline.redo(said, take, position, ending)
                }
            }
        }
    }

    // **Asked again on the way in, not only when the sitting opened.** The model is brought
    // from another screen, so the one moment the answer can have changed is the return from
    // it -- and a line still saying the marks are off, over a screen that has just declared
    // the files verified, is read as the app being broken. It costs nothing when nothing
    // moved: an engine already up says so at once, and a refusal is remembered against the
    // files it was given over.
    LaunchedEffect(Unit) { pipeline.recheckAnalysis() }

    // **A clock closed the turn**: it is truncated and sent as it stands, never cut into two
    // turns. Sending is the same gesture the hand would have made, so it is the same lambda.
    LaunchedEffect(capture.ending) { if (capture.ending != null) send() }

    // **The recording moment, driven from here** -- this is what watches the recorder, and the
    // two clocks are all a rule of that moment can read. Once a second and not on every frame:
    // a rule fires at most once per moment anyway, and a clock trigger names whole seconds.
    LaunchedEffect(capture.recording, capture.elapsedMs / 1000, capture.silenceMs / 1000) {
        if (capture.recording) pipeline.ticking(capture.elapsedMs, capture.silenceMs)
    }

    // **The mic arms on the character having finished, and on nothing else.**
    //
    // It used to arm on whatever made the screen compose, which is not a moment in the
    // conversation: walking in on a thread left yesterday opened the mic over somebody reading
    // it back, and the clocks ran on the room -- five seconds of quiet at the third position,
    // the turn's whole length at the second -- so a turn nobody said went out, was paid for and
    // was answered. What it follows is [ConversationState.opening], which is written at the
    // instant the app stops speaking and derived from nothing: a screen appearing, a crash and
    // a resume all find it empty, which is the truth about them.
    //
    // **And it is the big button's own condition besides.** Arming stands in for that press at
    // the two automatic positions, so anything that greys the button has to stop the arming:
    // the sitting over, a passage waiting for a repair in *waits*, and a turn nobody read
    // holding the conversation. Written twice, the two would drift, and the mic would open on a
    // turn the engine then refuses.
    //
    // **Keyed only on what it does not change itself.** Its own close moves the phase and can
    // write a fresh event, so keyed on those it was cancelled by its own gesture before the
    // mic opened. What it waits for is read off the flows instead. A fresh event -- a turn the
    // close provoked has ended -- restarts it, and the second close is spent at once, a
    // passage closing once.
    // **What the notice shows, and never while the app is speaking**: a receipt comes after the
    // audio, whichever moment laid it -- the end of an attempt fires while the voice still
    // plays. Its reading time is worked out here, where its lines are, and handed to the arming
    // with the notices it was worked out for.
    val showing = if (turn.phase == Phase.Speaking) emptyList() else turn.notices
    val lines = noticeLines(showing)
    val reading by rememberUpdatedState(showing to readingMs(lines))
    var noticeUntil by remember { mutableStateOf<Long?>(null) }
    val noticeLeft by produceState<Int?>(null, noticeUntil) {
        val until = noticeUntil
        while (until != null) {
            value = ((until - System.currentTimeMillis() + 999) / 1000).toInt().coerceAtLeast(0)
            kotlinx.coroutines.delay(200)
        }
        value = null
    }

    val opening = turn.opening
    LaunchedEffect(arms, opening) {
        if (!arms || opening == null) return@LaunchedEffect
        // The app has finished everything it had to say and measure, and the passage may be
        // left. A repeat in *waits* is what makes it leavable, so this can wait a while.
        pipeline.state.first { it.phase == Phase.Idle && it.closes() }
        recorder.state.first { !it.recording && !it.hasAudio }
        // **The passage closes as the countdown starts, not when it ends.** The countdown says
        // *you speak in three seconds*, so there is nothing left to do on the sentence before:
        // no retake, and what the close sets off -- a patch, a scripted or provoked turn -- is
        // said and shown before the learner's time starts rather than over it.
        //
        // In the pipeline's scope and not this screen's: closing a passage can send the
        // character off to speak, and that is a turn like any other, which has no business
        // dying because somebody stepped out to read their notes.
        pipeline.turns.launch { pipeline.close() }.join()
        pipeline.state.first { it.phase == Phase.Idle }
        if (pipeline.state.value.over) return@LaunchedEffect
        // **The receipts, on a clock that shows itself**, and the preparation only after them:
        // reading is not thinking, so the one does not eat into the other. The finger takes a
        // notice down sooner, and the preparation starts then.
        val standing = pipeline.state.value.notices
        if (standing.isNotEmpty()) {
            val ms = snapshotFlow { reading }.first { it.first == standing }.second
            noticeUntil = System.currentTimeMillis() + ms
            try {
                withTimeoutOrNull(ms) { pipeline.state.first { it.notices.isEmpty() } }
            } finally {
                noticeUntil = null
            }
            pipeline.shown()
        }
        // The preparation: the time between the end of the AI's answer and the mic being
        // armed. It lives outside the turn, so it touches no measure. Read after the close,
        // which may have moved it.
        val positions = pipeline.state.value.positions
        val wait = (positions.of(Levers.PREPARATION.key) as? Count)?.n?.times(1000) ?: 0
        // **Shown, in the status line**: a wait nobody can see is a mic that opens out of
        // nowhere. Taken down however the wait ends, the finger's press included.
        if (wait > 0) {
            pipeline.preparing(System.currentTimeMillis() + wait)
            try {
                kotlinx.coroutines.delay(wait.toLong())
            } finally {
                pipeline.preparing(null)
            }
        }
        pipeline.spend(opening)
        onRepeating(null)
        recorder.open(scope, Capture.of(positions))
    }

    val grid = Saylune.grid
    // **The thread scrolls, the bottom does not.** What the bottom commands is whatever
    // records, so it has to be reachable while the thread is anywhere.
    //
    // **And a cell separates the two.** Left touching, the last line of the thread reads as a
    // caption of the buttons under it -- the scaffold gives the same air above the content,
    // and this is its other end.
    val thread = rememberScrollState()
    Column(modifier) {
      Column(
        modifier = Modifier
            .weight(1f)
            // Outside the scroll and not inside it: inside, the gap would be a last empty line
            // of the thread and would scroll away with it, leaving the text against the buttons
            // exactly when the thread is long enough for it to matter.
            //
            // **The cell at the sides is the one every other screen already pays.** The
            // settings, the situation, the themes, the buttons, the commands, the notes and
            // the title all lay it; the thread was the only surface flush to the glass. What
            // that cost is measured: a relevance bracket opening on the first letter of a line
            // is drawn four pixels to the left of its cell (`MarkedTurn.kt`), so it fell off
            // the screen entirely -- and a curved edge takes the rest.
            .padding(bottom = grid.cell, start = grid.cell, end = grid.cell)
            .verticalScroll(thread),
        verticalArrangement = Arrangement.spacedBy(grid.cell),
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
        // **And nothing at all is said while the sitting holds.** A turn nobody read holds
        // it, and its own passage has no marking and no model to hear -- so the small button
        // would open the recorder on a gesture the engine drops without a word.
        val retakes = open != null && !turn.holding &&
            (retaking == null || open.spare(retaking, turn.positions))
        // What the AI's turn shows, which is a lever's position and never a preference: the
        // scrambled text by default, the ear being the main channel and a legible text
        // preempting the listening.
        val display = Display.of(turn.positions)
        // What the run holds minus what it has superseded: an attempt is drawn under the
        // utterance it repeats rather than where it sits, and a reply a rewording replaced
        // goes with the sentence it answered. The run keeps the order; the screen keeps the
        // grouping.
        turn.thread().forEach { spoken ->
            if (!spoken.speaker.isLearner) {
                Heard(
                    spoken.text, shortName(turn, spoken.speaker), display, channels,
                    // The lever, read per utterance: a replay spent on one answer is not spent
                    // on the next, and an answer with none left simply has no triangle.
                    onReplay = if (pipeline.replaysLeft(spoken.id) != 0) {
                        { scope.launch { pipeline.replay(spoken.id) } }
                    } else null,
                    stage = spoken.kind == app.saylune.chain.Said.Kind.StageDirection,
                )
                return@forEach
            }
            val readings = turn.readings(spoken.id)
            Said(
                spoken = spoken,
                speaker = shortName(turn, spoken.speaker),
                channels = if (
                    dropWordMarks &&
                    !(spoken.id == open?.opener?.id && standing == Standing.ToReword)
                ) channels.without(Channel.Squiggle, Channel.Brackets) else channels,
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
                onHearAdded = { where, added -> scope.launch { pipeline.hear(where, added) } },
                // The small button only opens; the bottom is what pauses and sends, and it
                // is `repeating` that says where the take goes when it does.
                onOpenRepeat = {
                    // **A retake taken by hand spends the arming the answer left pending.**
                    // The learner has taken the floor that arming was to give them; left
                    // standing, it only waited for the mic to come free, then closed the
                    // passage behind their back and opened the mic on a new turn -- the next
                    // sentence went out as a passage of its own. It comes back with the next
                    // answer, which a rewording has and a repeat does not.
                    turn.opening?.let { pipeline.spend(it) }
                    onRepeating(spoken.id)
                    recorder.open(scope, settings)
                },
                onNotes = onNotes,
                // The passage and not the attempt: what was sent is kept under the utterance
                // that opened it, a rewording replacing it and a repeat sending nothing.
                onPrompt = if (Trace.on) ({ onPrompt(spoken.id) }) else null,
                thread = thread,
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

        // **What gave way settles what the line says, and the button stays one.** The
        // learner has one question -- it did not work, do it again -- and which call is
        // missing is the app's to know. What differs is what is owed meanwhile: a chain that
        // gave way before the character answered leaves a recording to send and nothing else
        // stopped; a judgement that gave way after it holds the conversation, and the line has
        // to say so or the greyed button below reads as a bug in the app.
        turn.failure?.let { said ->
            Text(
                stringResource(
                    if (turn.holding) R.string.turn_unread else R.string.turn_failed, said,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = { pipeline.turns.launch { pipeline.retry() } }) {
                Text(stringResource(R.string.turn_retry))
            }
        }

        DebugPanel()
      }

      // **A receipt, and it goes when it has been read.** It sits between the thread and the
      // buttons rather than over them: what it says is why the buttons under it have just
      // changed, and covering them would hide the very thing it is explaining.
      RuleNotice(showing, onSeen = pipeline::shown, left = noticeLeft)

      val busy = turn.phase != Phase.Idle
      val recordingSomething = capture.recording || capture.hasAudio
      Buttons(
          myTurn = stringResource(R.string.capture_my_turn),
          send = stringResource(R.string.capture_send),
          // `closes` is false once the sitting is over, and while a turn nobody read holds
          // it, so nothing more opens in either case.
          mayOpen = !busy && !recordingSomething && turn.closes(),
          // **The pause exists at the first capture position and nowhere else**, the one
          // position that has a pause at all. Greyed at the other two rather than gone: what
          // has no object for the whole sitting still keeps its place in a row whose shape
          // must not move under the thumb.
          pauses = position == Levers.BY_HAND,
          mayPause = capture.recording || (capture.hasAudio && !busy),
          recording = capture.recording,
          // Greyed where nothing records: there is no take to send.
          maySend = capture.hasAudio && !busy,
          // **Throwing a take away is a lever.** Offered freely it walks around the attempt
          // counters -- a challenge granting one attempt could be restarted ten times -- so a
          // challenge has to be able to close it. It has **no object at the third position**,
          // where a clock sends too: the silence one hesitates through is what sends the take,
          // so the button would be a race against the pendulum, lost by whoever thinks.
          mayDiscard = turn.positions.live(Levers.DISCARD_TAKE.key) &&
              (turn.positions.of(Levers.DISCARD_TAKE.key) as? At)?.name == "allowed",
          onOpen = {
              // **It closes the previous passage and opens mine**, which is exactly what
              // happens: a turn of speech and not a next page.
              // The pipeline's scope: closing a passage runs the rules of the moment, and
              // can play a held continuation and a turn the scene provoked.
              pipeline.turns.launch {
                  // Read before the close, which is what makes the passage stop being open,
                  // and held until the take goes rather than shown here.
                  closed = turn.open()?.last?.takeIf { it.measured.isNotEmpty() }?.id
                  // The gesture spends the event, so an arming still counting down drops.
                  pipeline.opens()
                  onRepeating(null)
                  // The recorder keeps the screen's scope: its clocks are the screen's
                  // business and stopping them on the way out is what one wants.
                  recorder.open(scope, settings)
              }
          },
          onPause = {
              // With the sitting's clocks, like every other opening: carrying on resumes the
              // same turn, and a turn does not change how long it may run because the thumb
              // stopped it once.
              if (capture.recording) recorder.pause() else recorder.open(scope, settings)
          },
          onSend = send,
          onDiscard = { recorder.discard(); onRepeating(null) },
      )
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
    onHearAdded: (String, AddedSound) -> Unit,
    onOpenRepeat: () -> Unit,
    onNotes: (String) -> Unit,
    /** Open what went out for this passage, or null in a build that traces nothing. */
    onPrompt: (() -> Unit)?,
    /** The thread's own scroll, which the readout brings an opened row to the top of. */
    thread: ScrollState,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // The last, and only the last: saying it again is done to improve on the one before,
        // so the newest is what the learner knows how to say now.
        val shown = readings.lastOrNull()
        // **The judgement is read through the take it repeats.** Nothing judges a repeat -- it
        // is pipe B alone, on a text already settled -- so a repeat carries none of its own,
        // and reading only its own left the word marks off the very take one had just improved.
        // The words are the same words by construction, so it is the same judgement of them,
        // and it is read rather than copied: copied onto the repeat it would say that these
        // spans were measured on this take, which they were not.
        val judged = shown?.judged ?: spoken.judged
        // The words on screen are the standing attempt's, and the opener's only until one has
        // been made: everything drawn here is measured against them -- the marks, a tap that
        // plays a word, the inventory sound by sound -- so reading them anywhere else would
        // put a reading of one sentence on top of another.
        val text = shown?.text ?: spoken.text
        TurnLabel(
            name = speaker,
            following = judged?.following,
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
            onDebug = onPrompt,
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
                judged,
                channels = channels,
                sounds = sounds.orEmpty(),
                recorded = reading?.recorded,
                modifier = Modifier.fillMaxWidth(),
                // A tap anywhere in a word plays that word, on whichever side the selector
                // points at. Its bounds are read off the sounds it covers rather than
                // measured again, so the two recordings stay in step by construction.
                onTapCharacter = { offset ->
                    spanOfWord(text, offset)?.let { word ->
                        heard(sounds.orEmpty(), word, side)?.let { (from, to) ->
                            // The take being looked at, never the turn's first: its times
                            // are the ones just read off it.
                            where?.let { onHearSpan(it, from, to) }
                        }
                    }
                },
            )
        } else Text(
            // The attempt that stands, and the opener only until one has been made: a
            // rewording is on screen from the moment the call returns, seconds before it is
            // read, and drawing the opener there left the learner reading the sentence he had
            // just replaced while its analysis ran.
            text,
            style = Saylune.type.text,
            color = Saylune.palette.ink.srgb,
        )
        // Every passage that carries a reading gets the row -- listening back is what a
        // measured turn is for. What the row holds depends on whether the passage is open.
        if (where != null && marking != null) {
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
                    text, sounds, marking.added, thread,
                    onHearSound = { sound, which -> onHearSound(where, sound, which) },
                    // The pre-recorded set, played whole: a symbol on its own is already
                    // one sound and there is nothing in it to cut.
                    onHearAdded = { added -> onHearAdded(where, added) },
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
        (shown ?: spoken).ending?.let { ending ->
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
    // **Before everything, because it outranks everything**: a sitting that is over takes no
    // take, closes no passage and arms no microphone, so a line about what to press next
    // would name a gesture the screen refuses.
    turn.over -> stringResource(R.string.sitting_over)
    turn.phase == Phase.Hearing -> stringResource(R.string.phase_hearing)
    turn.phase == Phase.Thinking -> stringResource(R.string.phase_thinking)
    turn.phase == Phase.Speaking -> stringResource(R.string.phase_speaking)
    turn.phase == Phase.Measuring -> stringResource(R.string.phase_measuring)
    turn.armsAt != null -> armsIn(turn.armsAt)
    capture.recording && repeating != null ->
        stringResource(R.string.capture_repeat_running, clocks(turn, capture))
    capture.recording ->
        stringResource(R.string.capture_turn_running, clocks(turn, capture))
    capture.hasAudio && repeating != null ->
        stringResource(R.string.capture_repeat_paused, clocks(turn, capture))
    capture.hasAudio ->
        stringResource(R.string.capture_turn_paused, clocks(turn, capture))
    // **It names every aptitude in cause and never the worst**: several may say so at once,
    // and they are not competing criteria -- register and grammar are two ways for the words
    // to change. Naming one would be an election, which the project does nowhere. In the
    // learner's own words, which is what the aptitude names are for: `correctness` is a key.
    turn.standing() == Standing.ToReword -> stringResource(
        R.string.passage_reword,
        (turn.wordsGate as? Closing.Aptitudes)?.names
            ?.map { stringResource(nameOfAptitude(it)) }
            ?.joinToString(", ")
            ?: stringResource(R.string.passage_no_matter),
    )
    turn.standing() == Standing.ToSayAgain -> stringResource(R.string.passage_say_again)
    else -> stringResource(R.string.capture_press)
}

/**
 * **The two countdowns, and they are visible at all times** -- the time the turn has run and
 * the silence running now. Two times running out, shown the same way.
 *
 * They live in the status line, which is the one place that is always full and up to date, and
 * they are read off `Capture.of` like everything else that wants a clock, so no two readers can
 * disagree about when a turn ends. The silence one shows only where a silence sends, there
 * being no countdown otherwise -- at the first two positions the learner is the only one who
 * sends, so nothing is running out.
 *
 * **Where they belong exactly is not settled** (`ui.md` leaves it open, with the recording
 * symbol): the status line is where they are until it is.
 */
@Composable
private fun clocks(turn: ConversationState, capture: CaptureState): String {
    val settings = Capture.of(turn.positions)
    return settings.sendsAfterMs?.let {
        stringResource(
            R.string.capture_clocks,
            seconds(capture.elapsedMs), seconds(settings.ceilingMs),
            seconds(capture.silenceMs), seconds(it),
        )
    } ?: stringResource(
        R.string.capture_clock, seconds(capture.elapsedMs), seconds(settings.ceilingMs),
    )
}

private fun seconds(ms: Int): String = "%.1f s".format(ms / 1000f)

/**
 * The preparation counting down to [at], in whole seconds.
 *
 * **Where the countdowns belong is not settled**, like the capture clocks above: the status
 * line is where this one is until the question is answered for all of them at once.
 */
@Composable
private fun armsIn(at: Long): String {
    val left by produceState(secondsUntil(at), at) {
        while (true) {
            value = secondsUntil(at)
            kotlinx.coroutines.delay(200)
        }
    }
    return stringResource(R.string.capture_arms_in, left)
}

private fun secondsUntil(at: Long): Int =
    ((at - System.currentTimeMillis() + 999) / 1000).toInt().coerceAtLeast(0)

/** Whether the chain holds the screen. Named so an effect can key on it. */
private fun busyOf(phase: Phase): Boolean = phase != Phase.Idle

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
