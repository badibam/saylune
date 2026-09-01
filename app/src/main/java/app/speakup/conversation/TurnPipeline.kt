package app.speakup.conversation

import android.content.Context
import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Recognition
import app.speakup.chain.Word
import app.speakup.capture.Playback
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Analysis
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.marking.TurnMarking
import app.speakup.providers.ChosenSynthesis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

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
     * The grammatical gate's verdict on this utterance.
     *
     * Kept because the gate has to be legible. A turn marked here carries no sound marks, and
     * that is not an absence of findings: grammar is a door in front of the sound analysis,
     * and behind a closed door nothing was measured.
     */
    val faulty: Boolean = false,
    /** The folder this take was written to on disk, or null when nothing was kept. */
    val take: String? = null,
    /** The utterance this says again, by its place in the run. Null when it says something new. */
    val repeats: Int? = null,
)

data class ConversationState(
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
     * Every reading of the utterance at [at], oldest first: its own, then each one that says
     * it again, **each with its own place in the run**.
     *
     * The place travels with the reading because every listening gesture is addressed by it:
     * a reading whose audio came from one take and whose times came from another plays a
     * different part of the sentence, and handing back a bare list is how that happened.
     *
     * Derived and never stored. It is a walk of the run, and a walk of the run cannot fall
     * out of step with the run -- which is exactly what a list kept beside it used to do.
     * Only the readings that carry a marking are here: one that carries none was never read,
     * and offering it as a numbered attempt would promise something to look at.
     */
    fun readings(at: Int): List<Pair<Int, Utterance>> =
        utterances.withIndex()
            .filter { (index, spoken) ->
                (index == at || spoken.repeats == at) && spoken.marking != null
            }
            .map { (index, spoken) -> index to spoken }

    /**
     * The model to imitate for the utterance at [at], read through what it repeats.
     *
     * A repeat says the same text, so it is the same render; asking the utterance it repeats
     * is what keeps one file under one text instead of a copy per attempt.
     */
    fun modelOf(at: Int): File? = utterances.getOrNull(at)?.let { spoken ->
        spoken.model ?: spoken.repeats?.let { utterances.getOrNull(it)?.model }
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
) {
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    /**
     * Settle whether the marks are on, once, before the first turn.
     *
     * At the start of the conversation and not on the first turn: the doc settles this once
     * for the conversation, and an app that only discovers it has no engine after someone
     * has spoken has told them too late.
     */
    suspend fun prepare() {
        if (_state.value.analysis == null) {
            _state.value = _state.value.copy(analysis = analysis.readiness())
        }
    }

    /** Run [audio] through the chain, or run again what a previous failure left pending. */
    suspend fun submit(audio: File? = null) {
        val turn = audio ?: _state.value.pending ?: return
        Trace.turn()
        Trace.add(
            if (audio == null) "turn: sending again what was kept" else "turn: a new recording",
            "file" to turn.path,
            "bytes" to turn.length().toString(),
        )
        _state.value = _state.value.copy(phase = Phase.Hearing, failure = null, pending = turn)
        try {
            val heard = recognition.transcribe(turn)
            if (heard.isEmpty()) {
                Trace.add("turn: nothing was said, dropped")
                // Holding the button by accident is not a failure and must not read as one.
                _state.value = _state.value.copy(phase = Phase.Idle, pending = null)
                return
            }

            _state.value = _state.value.copy(phase = Phase.Thinking)
            val reply = conversation.reply(_state.value.history(), heard)

            _state.value = _state.value.copy(
                utterances = _state.value.utterances +
                    Utterance(
                        speaker = Speaker.Learner,
                        text = reply.intended,
                        said = turn,
                        faulty = reply.faulty,
                    ) +
                    Utterance(speaker = Speaker.Ai, text = reply.spoken),
                phase = Phase.Speaking,
                pending = null,
            )

            Playback.play(synthesis.speak(reply.spoken, synthesis.voice())) {
                // The number the doc puts on the chain, and the only one the learner feels.
                Trace.add("turn: first sound")
            }
            _state.value = _state.value.copy(phase = Phase.Idle)
            Trace.add("turn: said, and done")

            // The learner's turn sits two before the end: it was appended with the answer.
            val at = _state.value.utterances.size - 2
            if (reply.faulty) {
                // The gate: grammar is a door in front of the sound analysis. One does not
                // work the pronunciation of a sentence about to be rewritten, so on a
                // faulty turn the analysis is not hidden -- it is not computed.
                Trace.add("turn: grammar closes the gate, no sound analysis", "said" to reply.intended)
                // Kept even so, and especially so: a turn the gate held back is a real
                // learner fault the recognition could not have guessed, which is what the
                // fidelity bench is short of.
                keep(at, Takes.keep(context, turn, null, heard, reply.intended, true, null,
                                    turn = turnOf(at), attempt = attemptOf(at)))
            } else {
                examine(at = at, said = turn, heard = heard, text = reply.intended)
            }
        } catch (failure: ChainFailure) {
            Trace.fail("turn: a link gave way, the recording is kept", "why" to failure.message)
            _state.value = _state.value.copy(
                phase = Phase.Idle,
                failure = failure.message,
                pending = turn,
            )
        }
    }

    /**
     * What the learner did differently from the model, on the turn just spoken.
     *
     * The model is synthesised from `intended` in the very voice that just answered, which
     * is the default the doc sets: the model to imitate is the voice already being heard,
     * and the accent setting governs both because there is no third thing to align.
     */
    private suspend fun examine(at: Int, said: File, heard: List<Word>, text: String) {
        val readiness = _state.value.analysis
            ?: analysis.readiness().also { _state.value = _state.value.copy(analysis = it) }
        if (readiness !is Readiness.On) {
            keep(at, Takes.keep(context, said, null, heard, text, false, null,
                                turn = turnOf(at), attempt = attemptOf(at)))
            return
        }
        try {
            val model = synthesis.speak(text, synthesis.voice())
            val analysed = analysis.examine(said, model, text)
            update(at) {
                it.copy(marking = analysed.marking, sounds = analysed.sounds, model = model)
            }
            keep(at, Takes.keep(context, said, model, heard, text, false, analysed,
                                turn = turnOf(at), attempt = attemptOf(at)))
        } catch (failure: ChainFailure) {
            // The model has to be synthesised, so this branch depends on the network the
            // analysis itself does not. The turn stands either way: it was answered and
            // said, and only its marks are missing.
            Trace.fail("analysis: no model to measure against", "why" to failure.message)
        }
    }

    /**
     * Say the model of the utterance at [at] again.
     *
     * The remedy for a sound fault is to hear the model and say it again, not to be given a
     * written instruction about the tongue (`docs/reference.md`). This is the hearing half.
     */
    suspend fun hear(at: Int) {
        val wav = chosen(at) ?: return
        _state.value = _state.value.copy(phase = Phase.Speaking)
        Playback.play(wav, _state.value.speed)
        _state.value = _state.value.copy(phase = Phase.Idle)
    }

    /**
     * The recording the selector points at, for the utterance at [at]: its model, or itself.
     *
     * [at] is the reading being looked at and not the turn it belongs to, which is what keeps
     * the audio and the times on the same take: a turn holds several readings, and reading
     * one recording's clock onto another's audio played a different part of the sentence.
     */
    private fun chosen(at: Int): File? = when (_state.value.side) {
        Side.Model -> _state.value.modelOf(at)
        Side.Learner -> _state.value.utterances.getOrNull(at)?.said
    }

    fun side(side: Side) { _state.value = _state.value.copy(side = side) }

    fun speed(speed: Float) { _state.value = _state.value.copy(speed = speed) }

    /**
     * One stretch of the utterance at [at], on whichever side the selector points at.
     *
     * Used by a tap on a word: the word's bounds in each recording are read off the sounds
     * it covers, so nothing new is computed and the two sides stay in step by construction.
     */
    suspend fun hear(at: Int, fromMs: Int, toMs: Int) {
        val wav = chosen(at) ?: return
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
    suspend fun hear(at: Int, sound: AnalysedSound, side: Side) {
        val wav = (if (side == Side.Model) _state.value.modelOf(at)
                   else _state.value.utterances.getOrNull(at)?.said) ?: return
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
    suspend fun redo(at: Int, audio: File) {
        val spoken = _state.value.utterances.getOrNull(at) ?: return
        val model = _state.value.modelOf(at) ?: return
        // Its own clock: this is pipe B alone, and timing it from the conversation turn it
        // repeats would add every second of that turn to a chain that never ran here.
        Trace.turn("— redo —")
        Trace.add("redo: same sentence, same model", "text" to spoken.text)
        try {
            val analysed = analysis.examine(audio, model, spoken.text)
            val stamp = Takes.keep(context, audio, model, emptyList(), spoken.text, false,
                                   analysed, redo = true,
                                   turn = turnOf(at), attempt = attemptOf(at))
            _state.value = _state.value.copy(
                utterances = _state.value.utterances + Utterance(
                    speaker = Speaker.Learner,
                    text = spoken.text,
                    said = audio,
                    marking = analysed.marking,
                    sounds = analysed.sounds,
                    take = stamp,
                    repeats = at,
                ),
            )
        } catch (failure: ChainFailure) {
            Trace.fail("redo: could not be measured", "why" to failure.message)
        }
    }

    /** Replace the utterance at [at] with what [change] makes of it. */
    private fun update(at: Int, change: (Utterance) -> Utterance) {
        val run = _state.value.utterances
        if (at !in run.indices) return
        _state.value = _state.value.copy(
            utterances = run.mapIndexed { index, spoken ->
                if (index == at) change(spoken) else spoken
            },
        )
    }

    /**
     * The turn's name on disk: the take of the utterance the run starts from.
     *
     * A turn is named by its first take, so every later take can say which turn it repeats.
     * Null while that first take has not been written.
     */
    private fun turnOf(at: Int): String? = _state.value.utterances.getOrNull(at)?.take

    /** Which take of this turn is about to be written, the first being 1. */
    private fun attemptOf(at: Int): Int {
        val run = _state.value.utterances
        val written = (listOfNotNull(run.getOrNull(at)) + run.filter { it.repeats == at })
            .count { it.take != null }
        return written + 1
    }

    /** Remember a take that was written, so the next one can name the turn and its rank. */
    private fun keep(at: Int, stamp: String?) {
        if (stamp == null) return
        update(at) { it.copy(take = stamp) }
    }
}
