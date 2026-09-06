package app.speakup.conversation

import android.content.Context
import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Recognition
import app.speakup.chain.Word
import app.speakup.activity.Activity
import app.speakup.capture.Playback
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Analysis
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.judged.Judgement
import app.speakup.marking.TurnMarking
import app.speakup.providers.ChosenSynthesis
import app.speakup.providers.words
import app.speakup.store.ArchiveDao
import app.speakup.store.Recordings
import app.speakup.store.activity
import app.speakup.store.row
import app.speakup.store.utterance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID

/** Where a turn has got to. The screen shows it; nothing else depends on it. */
enum class Phase { Idle, Hearing, Thinking, Speaking }

/** Whose recording a manual gesture plays. */
enum class Side { Model, Learner }

/** Who said it. A human model is a third case, and it changes nothing about the shape. */
enum class Speaker { Learner, Ai }

/**
 * One thing said: a text, a speaker, the audio, the analysis.
 *
 * **Everything said in the app is one of these** -- the answer of the model as much as the
 * learner's sentence (`docs/design/activity-model.md`). Most carry no analysis, and the
 * answers carry none at all; keeping one table for all of it is what gives the thread of the
 * conversation for free, as a plain ordered run.
 *
 * [repeats] points at the utterance this one says again. That is the whole of saying a
 * sentence again on the spot: another utterance in the same conversation, tied to the one it
 * replays, with no second notion needed for it.
 */
data class Utterance(
    val speaker: Speaker,
    val text: String,
    /**
     * The activity this belongs to, and **always exactly one**.
     *
     * One parent and not several is what keeps every request on the matter simple, whichever
     * activity the utterance came from.
     */
    val activity: String,
    /** The recording it was said in. The answers of the model have none. */
    val said: File? = null,
    /**
     * What was marked and what was measured, or null when nothing read this.
     *
     * Null is not "nothing to report": a turn the gate held back and a turn read clean must
     * not be drawn alike. [faulty] tells the two apart.
     */
    val marking: TurnMarking? = null,
    val sounds: List<AnalysedSound> = emptyList(),
    /**
     * The synthesis of this very text, kept to be heard again.
     *
     * One render serves three times, exactly as the doc says: a yardstick for the measure, a
     * model to hear, and a model to hear again at every retry. An utterance that repeats
     * another has none of its own -- the text is the same, so the model is the same file, and
     * [modelOf] reads it through [repeats] rather than keeping a second name for it.
     */
    val model: File? = null,
    /**
     * What the language model marked on this turn, or null when nothing judged it.
     *
     * It replaces the single `faulty` boolean, which was a verdict over the whole turn: that
     * flattened the fact that a passage can carry several faults and left the portion
     * concerned with nowhere to be marked. One notch per group of words settles both.
     */
    val judged: Judgement? = null,
    /** The folder this take was written to on disk, or null when nothing was kept. */
    val take: String? = null,
    /**
     * The utterance this says again, **by its identity**. Null when it says something new.
     *
     * Its identity and not its place in the run: a place is only true for as long as nothing
     * is inserted before it, and it stops being true the moment the run is written down and
     * read back. An identity survives both.
     */
    val repeats: String? = null,
    val id: String = UUID.randomUUID().toString(),
    /** When it was said. Its order in the run is the run's; this is the wall clock. */
    val at: Long = System.currentTimeMillis(),
    /**
     * The engine that produced [marking], or null when nothing read this.
     *
     * Everything stored carries the version of what produced it: a reading redone with
     * different weights does not give the same numbers, and two eras of measurement that add
     * up in silence make a comparison that is wrong with nothing to say so.
     */
    val engine: String? = null,
)

data class ConversationState(
    /** The conversation itself, which is an activity like any other. */
    val activity: Activity = Activity.conversation(),
    /**
     * Everything said, oldest first. The thread is this run and nothing else carries it.
     *
     * A single ordered table rather than a list of exchanges with marks and takes and models
     * in maps beside it: those maps were four sources keyed by one index, and every one of
     * them could drift from the others by a single append in the wrong order.
     */
    val utterances: List<Utterance> = emptyList(),
    val phase: Phase = Phase.Idle,
    /** The provider's own words when a link gave way. Cleared by the next attempt. */
    val failure: String? = null,
    /** Kept so a failed send is retried without saying the sentence again. */
    val pending: File? = null,
    /**
     * Which recording a manual gesture plays: the voice being imitated, or one's own.
     *
     * It governs the play button and a tap on a word, and nothing else. A tap on a symbol
     * in the readout names its side outright -- the model's column and the learner's are
     * side by side there -- so a selector would only be able to contradict what the reader
     * just pointed at.
     */
    val side: Side = Side.Model,
    /**
     * How fast anything played by hand is played. Never applied to the answer being spoken:
     * that is the conversation, and slowing it would be the app deciding how the
     * conversation goes rather than the learner deciding what to examine.
     */
    val speed: Float = 1f,
    /** Whether the marks are on at all, settled once for the conversation. Null until asked. */
    val analysis: Readiness? = null,
) {

    /**
     * Every reading of the utterance [of], oldest first: its own, then each one that says it
     * again.
     *
     * Each reading is addressed by its own identity, which every listening gesture then
     * carries: a reading whose audio came from one take and whose times came from another
     * plays a different part of the sentence.
     *
     * Derived and never stored. It is a walk of the run, and a walk of the run cannot fall
     * out of step with the run -- which is exactly what a list kept beside it used to do.
     * Only the readings that carry a marking are here: one that carries none was never read,
     * so there is nothing of it to look at.
     */
    fun readings(of: String): List<Utterance> {
        val root = utterances.firstOrNull { it.id == of } ?: return emptyList()
        return utterances.filter {
            (it.id == root.id || it.repeats == root.id) && it.marking != null
        }
    }

    /**
     * The model to imitate for the utterance [of], read through what it repeats.
     *
     * A repeat says the same text, so it is the same render; asking the utterance it repeats
     * is what keeps one file under one text instead of a copy per attempt.
     */
    fun modelOf(of: String): File? = utterances.firstOrNull { it.id == of }?.let { spoken ->
        spoken.model ?: spoken.repeats?.let { id -> utterances.firstOrNull { it.id == id }?.model }
    }

    /**
     * The run as the language model should remember it.
     *
     * Repeats are left out. Saying a sentence again never reaches the language model -- it is
     * pipe B alone, on a text already settled -- so putting it in the history would tell the
     * model a turn was taken that it never answered.
     */
    fun history(): List<Exchange> = utterances
        .filter { it.repeats == null }
        .map { Exchange(fromLearner = it.speaker == Speaker.Learner, text = it.text) }
}

/**
 * One turn through the three links: what was heard, what to answer, and the voice answering.
 *
 * The chain is bloquante but repairable by construction. Nothing here throws a turn away:
 * the recorded file stays in [ConversationState.pending] until a run of the chain succeeds,
 * so a failure costs a button and never a spoken sentence.
 *
 * The analysis reads `intended` and the very file this kept, and it runs **after** the answer
 * has been said: the two pipes are independent, and the conversation is never made to wait on
 * the measure.
 */
class TurnPipeline(
    private val context: Context,
    private val recognition: Recognition,
    private val conversation: Conversation,
    private val synthesis: ChosenSynthesis,
    private val analysis: Analysis,
    private val archive: ArchiveDao,
) {
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    /**
     * One writer at a time, over every entry that changes the run.
     *
     * Each of those entries reads the state, works across a suspension -- a recognition, a
     * call, a playback -- and writes back what it read plus its own change. Two of them
     * running at once therefore lose one of the two writes, and nothing in the types says so.
     *
     * The screen does keep them apart today, by refusing every gesture while `phase` is not
     * `Idle`. That is a convention of one screen, reread from scratch at every screen added,
     * and it reads `busy` out of the very state that concurrent writes would rewrite. The
     * lock puts the property in the structure instead.
     *
     * Not reentrant: what is under it -- [write], [update], [name], [keep], [examine] -- is
     * private and never takes it. The two listening gestures that only read a snapshot stay
     * outside it on purpose (see [hear]).
     */
    private val writing = Mutex()

    /**
     * Pick the conversation back up, and settle whether the marks are on -- once, before the
     * first turn.
     *
     * **Carrying on is continuing an activity that did not finish**, and for the conversation
     * that is a policy on its status rather than a structure of its own: an unfinished one is
     * reopened as it stands, utterances and all. A fresh one is only started when there is
     * none to carry on.
     *
     * Readiness is asked every time and never read back from the store. It is a fact about
     * the device and not about the conversation -- the weights can have gone since -- and
     * asking it here rather than on the first turn is what keeps an app from discovering it
     * has no engine after somebody has already spoken.
     */
    suspend fun prepare() = writing.withLock {
        if (!opened) {
            opened = true
            archive.latest()?.let { openLocked(it.activity().id) } ?: beginLocked()
            // Once, here, and here only: at startup nothing is in flight and nothing has
            // been recorded, so a file the store does not name is one nothing will name.
            runCatching { Recordings.sweepOrphans(context, archive.recordings().toSet()) }
                .onFailure { Trace.fail("recordings: not swept", "why" to it.message) }
        }
        if (_state.value.analysis == null) {
            val readiness = analysis.readiness()
            _state.update { it.copy(analysis = readiness) }
        }
    }

    /** So a second call does not open a second conversation while the first is loading. */
    private var opened = false

    /**
     * Open the conversation [id], utterances and all.
     *
     * Nothing has to have been closed for this: an activity that did not finish is one to
     * carry on with, and every conversation but the one being had is in that state.
     */
    suspend fun open(id: String) = writing.withLock { openLocked(id) }

    private suspend fun openLocked(id: String) {
        val row = archive.activity(id) ?: return
        val activity = row.activity()
        opened = true
        val run = archive.utterances(activity.id).map { it.utterance() }
        _state.update {
            it.copy(
                activity = activity,
                utterances = run,
                // The run is another conversation's now, so anything that pointed into the
                // old one has to go: a retry of a recording from the conversation just left
                // would send it into this one.
                pending = null,
                failure = null,
            )
        }
        Trace.add("conversation: opened", "activity" to activity.id,
                  "titled" to activity.matter.ifBlank { null },
                  "utterances" to _state.value.utterances.size.toString())
    }

    /**
     * Start a fresh conversation.
     *
     * The one being left is not marked finished. Nothing prevents starting another, and
     * calling the old one finished would be the app deciding it is over on no evidence -- it
     * stays there to be carried on with.
     */
    suspend fun begin() = writing.withLock { beginLocked() }

    private suspend fun beginLocked() {
        val fresh = Activity.conversation()
        opened = true
        archive.put(fresh.row())
        _state.update {
            it.copy(
                activity = fresh,
                utterances = emptyList(),
                pending = null,
                failure = null,
            )
        }
        Trace.add("conversation: begun", "activity" to fresh.id)
    }

    /** Every conversation, most recent first, for the list to draw. */
    fun conversations() = archive.conversations()

    /** Run [audio] through the chain, or run again what a previous failure left pending. */
    suspend fun submit(audio: File? = null) = writing.withLock { submitLocked(audio) }

    private suspend fun submitLocked(audio: File?) {
        val turn = audio ?: _state.value.pending ?: return
        Trace.turn()
        Trace.add(
            if (audio == null) "turn: sending again what was kept" else "turn: a new recording",
            "file" to turn.path,
            "bytes" to turn.length().toString(),
        )
        _state.update { it.copy(phase = Phase.Hearing, failure = null, pending = turn) }
        try {
            val heard = recognition.transcribe(turn)
            if (heard.isEmpty()) {
                Trace.add("turn: nothing was said, dropped")
                // Holding the button by accident is not a failure and must not read as one.
                _state.update { it.copy(phase = Phase.Idle, pending = null) }
                return
            }

            _state.update { it.copy(phase = Phase.Thinking) }
            val reply = conversation.reply(
                _state.value.history(), heard, titled = _state.value.activity.matter.ifBlank { null },
            )

            // The two utterances are held by identity from here on. Their place in the run is
            // where they happen to sit, and it is not what addresses them: everything that
            // follows -- the take, the marks, the analysis -- names them by their id.
            val said = Utterance(
                speaker = Speaker.Learner,
                activity = _state.value.activity.id,
                text = reply.judged.intended,
                said = turn,
                judged = reply.judged,
            )
            val answer = Utterance(
                speaker = Speaker.Ai,
                activity = _state.value.activity.id,
                text = reply.spoken,
            )
            _state.update {
                it.copy(
                    utterances = it.utterances + said + answer,
                    phase = Phase.Speaking,
                    pending = null,
                )
            }

            write(said.id)
            write(answer.id)

            Playback.play(synthesis.speak(reply.spoken, synthesis.voice())) {
                // The number the doc puts on the chain, and the only one the learner feels.
                Trace.add("turn: first sound")
            }
            _state.update { it.copy(phase = Phase.Idle) }
            Trace.add("turn: said, and done")

            // A new name arrives only when there is a reason for one; any other turn leaves
            // the conversation called what it was called.
            reply.title?.let { name(it) }

            // What cuts the sound analysis today is **an absence of ground**, and nothing
            // else: a phrase that does not exist in the language cannot be synthesised, and
            // making the model say a non-phrase would give a non-phrase to imitate. The
            // words' gate, which reads the correctness note at the A-B bar, arrives with the
            // notes -- until then a malformed turn is marked and still measured.
            val groundless = reply.judged.words().correctness.any { it.notch == "ne-se-dit-pas" }
            if (groundless) {
                Trace.add("turn: no ground for a model, no sound analysis",
                          "said" to reply.judged.intended)
                // Kept even so, and especially so: a turn like this is a real learner fault
                // the recognition could not have guessed, which is what the fidelity bench
                // is short of.
                keep(said.id, Takes.keep(context, turn, null, heard, reply.judged.intended, true,
                                         null, turn = turnOf(said.id),
                                         attempt = attemptOf(said.id)))
            } else {
                examine(of = said.id, said = turn, heard = heard, text = reply.judged.intended)
            }
        } catch (failure: ChainFailure) {
            Trace.fail("turn: a link gave way, the recording is kept", "why" to failure.message)
            _state.update {
                it.copy(
                    phase = Phase.Idle,
                    failure = failure.message,
                    pending = turn,
                )
            }
        }
    }

    /**
     * What the learner did differently from the model, on the turn just spoken.
     *
     * The model is synthesised from `intended` in the very voice that just answered, which
     * is the default the doc sets: the model to imitate is the voice already being heard,
     * and the accent setting governs both because there is no third thing to align.
     */
    private suspend fun examine(of: String, said: File, heard: List<Word>, text: String) {
        val readiness = _state.value.analysis
            ?: analysis.readiness().also { ready -> _state.update { it.copy(analysis = ready) } }
        if (readiness !is Readiness.On) {
            keep(of, Takes.keep(context, said, null, heard, text, false, null,
                                turn = turnOf(of), attempt = attemptOf(of)))
            return
        }
        try {
            val model = synthesis.speak(text, synthesis.voice())
            val analysed = analysis.examine(said, model, text)
            update(of) {
                it.copy(marking = analysed.marking, sounds = analysed.sounds, model = model,
                        engine = readiness.version)
            }
            write(of)
            keep(of, Takes.keep(context, said, model, heard, text, false, analysed,
                                turn = turnOf(of), attempt = attemptOf(of)))
        } catch (failure: ChainFailure) {
            // The model has to be synthesised, so this branch depends on the network the
            // analysis itself does not. The turn stands either way: it was answered and
            // said, and only its marks are missing.
            Trace.fail("analysis: no model to measure against", "why" to failure.message)
        }
    }

    /**
     * Say the model of the utterance [of] again.
     *
     * The remedy for a sound fault is to hear the model and say it again, not to be given a
     * written instruction about the tongue (`docs/reference.md`). This is the hearing half.
     *
     * Under the lock, because it is the app speaking and everything waits for it: letting a
     * turn start while a model is being played would put two voices on one speaker.
     */
    suspend fun hear(of: String) = writing.withLock {
        val wav = chosen(of) ?: return@withLock
        _state.update { it.copy(phase = Phase.Speaking) }
        Playback.play(wav, _state.value.speed)
        _state.update { it.copy(phase = Phase.Idle) }
    }

    /**
     * The recording the selector points at, for the utterance [of]: its model, or itself.
     *
     * [of] is the reading being looked at and not the turn it belongs to, which is what keeps
     * the audio and the times on the same take: a turn holds several readings, and reading
     * one recording's clock onto another's audio played a different part of the sentence.
     */
    private fun chosen(of: String): File? = when (_state.value.side) {
        Side.Model -> _state.value.modelOf(of)
        Side.Learner -> _state.value.utterances.firstOrNull { it.id == of }?.said
    }

    fun side(side: Side) { _state.update { it.copy(side = side) } }

    fun speed(speed: Float) { _state.update { it.copy(speed = speed) } }

    /**
     * One stretch of the utterance [of], on whichever side the selector points at.
     *
     * Used by a tap on a word: the word's bounds in each recording are read off the sounds
     * it covers, so nothing new is computed and the two sides stay in step by construction.
     *
     * Outside the lock, and that is the point: it changes nothing, it reads one snapshot, and
     * it lasts a fraction of a second. Putting it behind the writer would make a tap on a
     * word wait out the turn that is running, which is the freeze this gesture exists to
     * avoid.
     */
    suspend fun hear(of: String, fromMs: Int, toMs: Int) {
        val wav = chosen(of) ?: return
        Playback.play(wav, fromMs, toMs, _state.value.speed, Playback.WORD_MARGIN_MS)
    }

    /**
     * Say one sound of the utterance at [at] -- the same remedy, one notch finer.
     *
     * The doc calls the isolated sound a level of listening: the objection to it was about
     * **production**, a sound got right alone still being got wrong in the word, and it does
     * not carry over to listening, which asks nothing of the mouth. It costs nothing to offer
     * -- the model is synthesised anyway and the analysis already says where in it each sound
     * sits.
     *
     * It is the model's own voice and not a specimen from elsewhere, which is what keeps it
     * inside the rule that nothing outside the two recordings is ever consulted. It says
     * where, in the recording being imitated, this mark is about.
     *
     * The phase is left alone. Hearing the whole model is the app speaking and everything
     * waits for it; a tap on a symbol is a fraction of a second, and freezing the screen for
     * it would be a worse lie than the wait it prevents.
     */
    suspend fun hear(of: String, sound: AnalysedSound, side: Side) {
        val wav = (if (side == Side.Model) _state.value.modelOf(of)
                   else _state.value.utterances.firstOrNull { it.id == of }?.said) ?: return
        val span = if (side == Side.Model) sound.modelMs else sound.saidMs
        Playback.play(wav, span.first, span.last, _state.value.speed)
    }

    /**
     * Measure [audio] against the utterance at [at] again -- the saying-again half.
     *
     * **It does not go through the conversation.** No recognition, no language model, no
     * reply: this is pipe B alone, on a sentence whose text is already settled. That is what
     * makes it cheap and what makes it honest -- the new take is scored against a reference
     * text known in advance, which is the one thing free conversation cannot offer.
     *
     * It **appends an utterance** pointing at the one it repeats, rather than adding to a
     * list held beside the turn. Nothing of the earlier reading enters this one -- the same
     * fault must produce the same mark at any moment, so a second take is read exactly like a
     * first -- and the earlier reading stays where it was rather than being overwritten.
     */
    suspend fun redo(of: String, audio: File) = writing.withLock { redoLocked(of, audio) }

    private suspend fun redoLocked(of: String, audio: File) {
        val spoken = _state.value.utterances.firstOrNull { it.id == of } ?: return
        val model = _state.value.modelOf(of) ?: return
        // Its own clock: this is pipe B alone, and timing it from the conversation turn it
        // repeats would add every second of that turn to a chain that never ran here.
        Trace.turn("— redo —")
        Trace.add("redo: same sentence, same model", "text" to spoken.text)
        try {
            val analysed = analysis.examine(audio, model, spoken.text)
            val stamp = Takes.keep(context, audio, model, emptyList(), spoken.text, false,
                                   analysed, redo = true,
                                   turn = turnOf(of), attempt = attemptOf(of))
            val again = Utterance(
                speaker = Speaker.Learner,
                activity = spoken.activity,
                text = spoken.text,
                said = audio,
                marking = analysed.marking,
                sounds = analysed.sounds,
                take = stamp,
                engine = (_state.value.analysis as? Readiness.On)?.version,
                repeats = spoken.id,
            )
            _state.update { it.copy(utterances = it.utterances + again) }
            write(again.id)
        } catch (failure: ChainFailure) {
            Trace.fail("redo: could not be measured", "why" to failure.message)
        }
    }

    /**
     * Call the conversation [title], and keep it called that.
     *
     * The title is the activity's **matter**, not a field of its own: the design says the
     * matter of a conversation is what is being talked about, which is exactly what a title
     * says in a few words. Two fields for one idea would be two things to keep in step.
     *
     * Under the writer and never taking it itself: it is called from inside a turn, which
     * already holds it.
     */
    private suspend fun name(title: String) {
        val named = _state.value.activity.copy(matter = title)
        _state.update { it.copy(activity = named) }
        runCatching { archive.update(named.row()) }.onFailure {
            Trace.fail("archive: the new title was not kept", "why" to it.message)
        }
    }

    /**
     * Put the utterance [of] in the store, as it now stands.
     *
     * Called again whenever it changes rather than only once, because it changes after it
     * appears: a turn is said before it is read, and its marks arrive a few seconds later.
     * The row is replaced, so writing twice costs a write and never a duplicate.
     *
     * The rank is looked up here rather than passed in. It is the utterance's place in the
     * run, which the row keeps because SQL will not give the insertion order back -- but it
     * is not what addresses anything, so nobody outside this line has to carry it.
     */
    private suspend fun write(of: String) {
        val rank = _state.value.utterances.indexOfFirst { it.id == of }
        if (rank < 0) return
        val spoken = _state.value.utterances[rank]
        runCatching { archive.put(spoken.row(rank)) }.onFailure {
            // Keeping the trace is not the turn. A store that will not write must not cost
            // the learner the conversation -- but it says so rather than passing for kept.
            Trace.fail("archive: not written", "why" to it.message)
        }
    }

    /** Replace the utterance [of] with what [change] makes of it. */
    private fun update(of: String, change: (Utterance) -> Utterance) {
        _state.update { state ->
            state.copy(
                utterances = state.utterances.map { spoken ->
                    if (spoken.id == of) change(spoken) else spoken
                },
            )
        }
    }

    /**
     * The turn's name on disk: the take of the utterance the run starts from.
     *
     * A turn is named by its first take, so every later take can say which turn it repeats.
     * Null while that first take has not been written.
     */
    private fun turnOf(of: String): String? =
        _state.value.utterances.firstOrNull { it.id == of }?.take

    /** Which take of this turn is about to be written, the first being 1. */
    private fun attemptOf(of: String): Int {
        val run = _state.value.utterances
        val root = run.firstOrNull { it.id == of } ?: return 1
        val written = (listOf(root) + run.filter { it.repeats == root.id })
            .count { it.take != null }
        return written + 1
    }

    /** Remember a take that was written, so the next one can name the turn and its rank. */
    private suspend fun keep(of: String, stamp: String?) {
        if (stamp == null) return
        update(of) { it.copy(take = stamp) }
        write(of)
    }
}
