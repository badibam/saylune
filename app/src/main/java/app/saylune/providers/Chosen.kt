package app.saylune.providers

import android.content.Context
import app.saylune.chain.ChainFailure
import app.saylune.chain.Conversation
import app.saylune.chain.Exchange
import app.saylune.chain.Present
import app.saylune.chain.Recognition
import app.saylune.chain.Reply
import app.saylune.chain.Verdict
import app.saylune.chain.Synthesis
import app.saylune.chain.Voice
import app.saylune.chain.Word
import app.saylune.chain.Scene
import app.saylune.keys.Secret
import app.saylune.keys.SecretStore
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

    override suspend fun reply(
        history: List<Exchange>, heard: List<Word>, scene: Scene, present: Present,
    ): Reply {
        val values = store.values().first()
        val (provider, model) = pick(Task.Conversation, values)
        // **Laid on the scene here and not carried down from the pipeline.** What the learner
        // wants steered around is a setting, and this is where the settings already are: the
        // pipeline would have had to hold the store to pass it, and every provider below would
        // have had to take one more argument to hand it on unchanged.
        return conversationBy(store, provider, model,
                              effortFor(provider, Task.Conversation, values))
            .reply(
                history,
                heard,
                scene.copy(avoid = values[Secret.Avoid].orEmpty()),
                present,
            )
    }

    /**
     * The judge, picked on **its own link**.
     *
     * **Nothing falls back to the conversation's choice.** A judgement setting left empty is a
     * link that is not set, and it fails saying so, exactly as the other three do -- the same
     * rule that keeps a missing recognition provider from quietly becoming somebody else's.
     * What it costs is that an install carrying the earlier settings has one more row to fill
     * before it can talk, and the screen is where that is said.
     *
     * Nothing of the scene travels: `judge` takes the situation and nothing else, so the
     * staging and what the learner asked to be steered around have no way through.
     */
    override suspend fun judge(
        history: List<Exchange>, said: String, answered: String, situation: String,
        present: Present,
    ): Verdict {
        val values = store.values().first()
        val (provider, model) = pick(Task.Judging, values)
        return conversationBy(store, provider, model, effortFor(provider, Task.Judging, values))
            .judge(history, said, answered, situation, present)
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
        // The analysis providers do one link and it is not this one. Reached only if a
        // stored choice outlived the list it came from, which is what `Task.chosen`
        // already guards -- so this says so rather than falling through to anyone.
        Provider.OnDevice, Provider.AnalysisServer ->
            throw ChainFailure("$provider does not transcribe")
        Provider.Inworld -> InworldRecognition(store, model)
        Provider.Deepseek, Provider.OpenAI ->
            throw ChainFailure("${provider.label} does not transcribe")
    }

internal fun conversationBy(
    store: SecretStore,
    provider: Provider,
    model: String,
    /** Null only where the provider has no notion of one, and then nothing is sent. */
    effort: Effort?,
): Conversation = when (provider) {
    Provider.Deepseek -> DeepseekConversation(
        store, model,
        effort ?: throw ChainFailure("DeepSeek was given no reasoning level"),
    )
    Provider.OpenAI -> OpenaiConversation(store, model, effort)
    Provider.Replicate -> ReplicateConversation(ReplicateClient(store), model)
    Provider.Inworld -> InworldConversation(store, model)
    // Named one by one and never closed with an `else`. The two seams beside this one are
    // exhaustive, and that is what made the compiler ask about OpenAI when it was added;
    // here an `else` answered for it, and a provider offered on screen threw at the moment
    // the learner had already spoken.
    Provider.Azure, Provider.ElevenLabs, Provider.OnDevice, Provider.AnalysisServer ->
        throw ChainFailure("${provider.label} does not hold a conversation")
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
    Provider.OnDevice, Provider.AnalysisServer ->
        throw ChainFailure("$provider does not speak")
    Provider.Inworld -> InworldSynthesis(context, store, model)
    Provider.Deepseek, Provider.OpenAI ->
        throw ChainFailure("${provider.label} does not speak")
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
