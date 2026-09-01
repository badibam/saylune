package app.speakup.providers

import android.content.Context
import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Recognition
import app.speakup.chain.Reply
import app.speakup.chain.Synthesis
import app.speakup.chain.Voice
import app.speakup.chain.Word
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * The link the user chose, resolved at the moment it is used.
 *
 * These are implementations of the three seams, not a layer above them: nothing is created
 * over `chain/`, and the pipeline still holds one Recognition, one Conversation and one
 * Synthesis exactly as before. What changes is that who answers is read from the store on
 * each call instead of being fixed when the app started -- so changing a provider in the
 * settings takes effect on the next turn, with nothing to restart and no state to invalidate.
 *
 * Resolving late costs one read of a DataStore per turn, against the several seconds the
 * call itself takes. Resolving early would cost a rebuild of the pipeline on every settings
 * change, which is the kind of wiring that goes wrong quietly.
 *
 * A task whose provider is not set, or whose provider lost the key it needs, throws
 * [ChainFailure] like any other link that cannot do its job -- and the turn's recording is
 * kept, so the user fills the setting in and sends the same recording again.
 */
class ChosenRecognition(private val store: SecretStore) : Recognition {

    override suspend fun transcribe(audio: File): List<Word> {
        val values = store.values().first()
        val (provider, model) = pick(Task.Recognition, values)
        return recognitionBy(store, provider, model).transcribe(audio)
    }
}

class ChosenConversation(private val store: SecretStore) : Conversation {

    override suspend fun reply(history: List<Exchange>, heard: List<Word>, titled: String?): Reply {
        val values = store.values().first()
        val (provider, model) = pick(Task.Conversation, values)
        return conversationBy(store, provider, model).reply(history, heard, titled)
    }
}

class ChosenSynthesis(
    private val context: Context,
    private val store: SecretStore,
) : Synthesis {

    override suspend fun speak(text: String, voice: Voice): File {
        val values = store.values().first()
        val (provider, model) = pick(Task.Synthesis, values)
        val wav = synthesisBy(context, store, provider, model).speak(text, voice)
        // After the render and not before: pruning to make room for a file that then fails
        // to arrive would throw sentences away to hold nothing. Here rather than in each
        // provider, because the ceiling belongs to the app and not to whoever rendered.
        Renders.prune(context, cap(values))
        return wav
    }

    /**
     * The ceiling the user set, in megabytes, or the default.
     *
     * Blank and unreadable are the same answer, and this is the one setting where an absent
     * value legitimately stands for something: a ceiling nobody chose is still a ceiling, and
     * a cache with none is what the doc says not to have. A number at or below zero would
     * empty the cache after every render, which is not a choice anyone means to make, so it
     * reads as unset.
     */
    private fun cap(values: Map<Secret, String>): Int =
        values[Secret.RenderCacheCap]?.trim()?.toIntOrNull()?.takeIf { it > 0 }
            ?: Renders.DEFAULT_CAP_MB

    /**
     * The voice in force, with the provider that will actually say it.
     *
     * Read here rather than by the caller because the two travel together: a voice name
     * belongs to one provider, and handing an Azure name to a Replicate model would render
     * something nobody chose. The accent setting the doc describes is this pair.
     */
    suspend fun voice(): Voice {
        val values = store.values().first()
        val (provider, _) = pick(Task.Synthesis, values)
        val id = values[Secret.SynthesisVoice]
            ?: throw ChainFailure("no voice has been chosen for ${provider.label}")
        return Voice(provider = provider.id, id = id)
    }
}

/**
 * Who implements a link, named outright rather than read from the settings.
 *
 * The switching classes above and the latency test both need this, and a second copy of the
 * mapping beside a second caller would be a second source that drifts. What the settings say
 * is decided in [pick]; what a provider *is* is decided here, once.
 */
internal fun recognitionBy(store: SecretStore, provider: Provider, model: String): Recognition =
    when (provider) {
        Provider.Replicate -> ReplicateRecognition(store, model)
        Provider.Azure -> AzureRecognition(store)
        Provider.ElevenLabs -> ElevenLabsRecognition(store, model)
        Provider.Deepseek -> throw ChainFailure("DeepSeek does not transcribe")
    }

internal fun conversationBy(store: SecretStore, provider: Provider, model: String): Conversation =
    when (provider) {
        Provider.Deepseek -> DeepseekConversation(store, model)
        Provider.Replicate -> ReplicateConversation(ReplicateClient(store), model)
        else -> throw ChainFailure("${provider.label} does not hold a conversation")
    }

internal fun synthesisBy(
    context: Context,
    store: SecretStore,
    provider: Provider,
    model: String,
): Synthesis = when (provider) {
    Provider.Replicate -> ReplicateSynthesis(context, store, model)
    Provider.Azure -> AzureSynthesis(context, store)
    Provider.ElevenLabs -> ElevenLabsSynthesis(context, store, model)
    Provider.Deepseek -> throw ChainFailure("DeepSeek does not speak")
}

/**
 * Who does [task] and with what, or a failure that names the setting that is missing.
 *
 * No default provider. Picking one for the user would send their key and their voice to a
 * service they never named, and would do it silently -- the settings screen exists to be
 * answered.
 */
private fun pick(task: Task, values: Map<Secret, String>): Pair<Provider, String> {
    val provider = task.chosen(values)
        ?: throw ChainFailure("no provider is set for this link, or its key is missing")
    return provider to modelFor(task, provider, values)
}
