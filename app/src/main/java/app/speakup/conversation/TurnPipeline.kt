package app.speakup.conversation

import android.content.Context
import android.media.MediaPlayer
import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Recognition
import app.speakup.chain.Synthesis
import app.speakup.chain.Voice
import app.speakup.chain.Word
import app.speakup.analysis.Analysed
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Analysis
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.marking.TurnMarking
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/** Where a turn has got to. The screen shows it; nothing else depends on it. */
enum class Phase { Idle, Hearing, Thinking, Speaking }

data class ConversationState(
    val exchanges: List<Exchange> = emptyList(),
    val phase: Phase = Phase.Idle,
    /** The provider's own words when a link gave way. Cleared by the next attempt. */
    val failure: String? = null,
    /** Kept so a failed send is retried without saying the sentence again. */
    val pending: File? = null,
    /**
     * Marks by the position of the learner's turn in [exchanges].
     *
     * Keyed by index because the list only ever grows, and kept beside [exchanges] rather
     * than inside them: an [Exchange] is what the language model is told, and marks are no
     * business of the language model.
     *
     * A turn absent from here has not been analysed -- which is not the same as a turn
     * with nothing to report, and the two must not be drawn alike.
     */
    val marking: Map<Int, TurnMarking> = emptyMap(),
    /** What the analysis found under those marks, for the readout. Same keys as [marking]. */
    val sounds: Map<Int, List<AnalysedSound>> = emptyMap(),
    /**
     * The synthesised model of each analysed turn, kept to be heard again.
     *
     * One render serves three times, exactly as the doc says: a yardstick for the measure, a
     * model to hear, and a model to hear again at every retry.
     */
    val models: Map<Int, File> = emptyMap(),
    /** Whether the marks are on at all, settled once for the session. Null until asked. */
    val analysis: Readiness? = null,
    /**
     * The learner's turns the model judged grammatically wrong, by their position in
     * [exchanges].
     *
     * Kept because the gate has to be legible. A turn in here carries no sound marks and
     * that is not an absence of findings: grammar is a door in front of the sound analysis,
     * and behind a closed door nothing was measured.
     */
    val faulty: Set<Int> = emptySet(),
)

/**
 * One turn through the three links: what was heard, what to answer, and the voice answering.
 *
 * The chain is bloquante but repairable by construction. Nothing here throws a turn away:
 * the recorded file stays in [ConversationState.pending] until a run of the chain succeeds,
 * so a failure costs a button and never a spoken sentence.
 *
 * Nothing accumulates between turns beyond the conversation itself. The analysis reads
 * `intended` and the very file this kept, and it runs **after** the answer has been said:
 * the two pipes are independent, and the conversation is never made to wait on the measure.
 */
class TurnPipeline(
    private val context: Context,
    private val store: SecretStore,
    private val recognition: Recognition,
    private val conversation: Conversation,
    private val synthesis: Synthesis,
    private val analysis: Analysis,
) {
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    /**
     * Settle whether the marks are on, once, before the first turn.
     *
     * At the start of the session and not on the first turn: the doc makes this a
     * session-level question, and an app that only discovers it has no engine after someone
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
            val reply = conversation.reply(_state.value.exchanges, heard)

            _state.value = _state.value.copy(
                exchanges = _state.value.exchanges +
                    Exchange(fromLearner = true, text = reply.intended) +
                    Exchange(fromLearner = false, text = reply.spoken),
                phase = Phase.Speaking,
                pending = null,
            )

            play(synthesis.speak(reply.spoken, voice()))
            _state.value = _state.value.copy(phase = Phase.Idle)
            Trace.add("turn: said, and done")

            // The learner's turn sits two before the end: it was appended with the answer.
            val at = _state.value.exchanges.size - 2
            if (reply.faulty) {
                // The gate: grammar is a door in front of the sound analysis. One does not
                // work the pronunciation of a sentence about to be rewritten, so on a
                // faulty turn the analysis is not hidden -- it is not computed.
                _state.value = _state.value.copy(faulty = _state.value.faulty + at)
                Trace.add("turn: grammar closes the gate, no sound analysis", "said" to reply.intended)
                // Kept even so, and especially so: a turn the gate held back is a real
                // learner fault the recognition could not have guessed, which is what the
                // fidelity bench is short of.
                Takes.keep(context, turn, null, heard, reply.intended, true, null)
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
     *
     * **The grammatical gate is not here yet, and its absence shows.** The doc is explicit
     * that on a turn marked as faulty the sound analysis does not run at all -- not hidden,
     * not computed -- because the sentence is about to be rewritten. Nothing carries the
     * verdict yet (`../../../../../../TODO.md`), so this analyses every turn, and marks will
     * appear on turns the gate will later hold back.
     */
    private suspend fun examine(at: Int, said: File, heard: List<Word>, text: String) {
        val readiness = _state.value.analysis
            ?: analysis.readiness().also { _state.value = _state.value.copy(analysis = it) }
        if (readiness !is Readiness.On) {
            Takes.keep(context, said, null, heard, text, false, null)
            return
        }
        try {
            val model = synthesis.speak(text, voice())
            val analysed = analysis.examine(said, model, text)
            _state.value = _state.value.copy(
                marking = _state.value.marking + (at to analysed.marking),
                sounds = _state.value.sounds + (at to analysed.sounds),
                models = _state.value.models + (at to model),
            )
            Takes.keep(context, said, model, heard, text, false, analysed)
        } catch (failure: ChainFailure) {
            // The model has to be synthesised, so this branch depends on the network the
            // analysis itself does not. The turn stands either way: it was answered and
            // said, and only its marks are missing.
            Trace.fail("analysis: no model to measure against", "why" to failure.message)
        }
    }

    /**
     * Say the model of the turn at [at] again.
     *
     * The remedy for a sound fault is to hear the model and say it again, not to be given a
     * written instruction about the tongue (`docs/reference.md`). This is the hearing half.
     */
    suspend fun hear(at: Int) {
        val model = _state.value.models[at] ?: return
        _state.value = _state.value.copy(phase = Phase.Speaking)
        play(model)
        _state.value = _state.value.copy(phase = Phase.Idle)
    }

    /**
     * Measure [audio] against the turn at [at] again -- the saying-again half.
     *
     * **It does not go through the conversation.** No recognition, no language model, no
     * reply: this is pipe B alone, on a sentence whose text is already settled. That is what
     * makes it cheap and what makes it honest -- the new take is scored against a reference
     * text known in advance, which is the one thing free conversation cannot offer.
     *
     * The marks of the turn are replaced rather than added to. The same fault must produce
     * the same mark at any moment, so a second take is read exactly like a first, and
     * nothing of the previous reading survives to weigh on it.
     */
    suspend fun redo(at: Int, audio: File) {
        val model = _state.value.models[at] ?: return
        val text = _state.value.exchanges.getOrNull(at)?.text ?: return
        Trace.add("redo: same sentence, same model", "text" to text)
        try {
            val analysed = analysis.examine(audio, model, text)
            _state.value = _state.value.copy(
                marking = _state.value.marking + (at to analysed.marking),
                sounds = _state.value.sounds + (at to analysed.sounds),
            )
            Takes.keep(context, audio, model, emptyList(), text, false, analysed, redo = true)
        } catch (failure: ChainFailure) {
            Trace.fail("redo: could not be measured", "why" to failure.message)
        }
    }

    private suspend fun voice(): Voice {
        val id = store.values().first()[Secret.AzureVoice]
            ?: throw ChainFailure("no voice has been chosen")
        return Voice(provider = "azure", id = id)
    }

    /** Suspends until the reply has finished being said, so the phases mean what they say. */
    private suspend fun play(wav: File) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val player = MediaPlayer()
            player.setOnCompletionListener {
                it.release()
                if (continuation.isActive) continuation.resume(Unit)
            }
            player.setOnErrorListener { p, _, _ ->
                p.release()
                if (continuation.isActive) continuation.resume(Unit)
                true
            }
            continuation.invokeOnCancellation { runCatching { player.release() } }
            player.setDataSource(wav.path)
            player.prepare()
            player.start()
        }
    }
}
