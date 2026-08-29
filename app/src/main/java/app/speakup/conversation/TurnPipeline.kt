package app.speakup.conversation

import android.content.Context
import android.media.MediaPlayer
import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Recognition
import app.speakup.chain.Synthesis
import app.speakup.chain.Voice
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
)

/**
 * One turn through the three links: what was heard, what to answer, and the voice answering.
 *
 * The chain is bloquante but repairable by construction. Nothing here throws a turn away:
 * the recorded file stays in [ConversationState.pending] until a run of the chain succeeds,
 * so a failure costs a button and never a spoken sentence.
 *
 * Nothing accumulates between turns beyond the conversation itself. The analysis is not
 * wired in yet -- that is step 4 -- and when it is, it will read `intended` and the very
 * file this pipeline kept.
 */
class TurnPipeline(
    private val context: Context,
    private val store: SecretStore,
    private val recognition: Recognition,
    private val conversation: Conversation,
    private val synthesis: Synthesis,
) {
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    /** Run [audio] through the chain, or run again what a previous failure left pending. */
    suspend fun submit(audio: File? = null) {
        val turn = audio ?: _state.value.pending ?: return
        _state.value = _state.value.copy(phase = Phase.Hearing, failure = null, pending = turn)
        try {
            val heard = recognition.transcribe(turn)
            if (heard.isEmpty()) {
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
        } catch (failure: ChainFailure) {
            _state.value = _state.value.copy(
                phase = Phase.Idle,
                failure = failure.message,
                pending = turn,
            )
        }
    }

    private suspend fun voice(): Voice {
        val id = store.values().first()[Secret.ElevenlabsVoiceId]
            ?: throw ChainFailure("no ElevenLabs voice has been chosen")
        return Voice(provider = "elevenlabs", id = id)
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
