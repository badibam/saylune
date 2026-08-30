package app.speakup.providers

import androidx.annotation.StringRes
import app.speakup.R
import app.speakup.chain.ChainFailure
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * One link of the chain, seen from the settings screen.
 *
 * The three are named after what they do and not after who does it, because who does it is
 * exactly what the user chooses here. Each names the three store entries that answer, for
 * this task: by whom, with which model, and -- for the voice -- in which voice.
 */
enum class Task(
    @StringRes val label: Int,
    val provider: Secret,
    val model: Secret,
    val voice: Secret?,
) {
    Recognition(R.string.task_recognition, Secret.RecognitionProvider, Secret.RecognitionModel, null),
    Conversation(R.string.task_conversation, Secret.ConversationProvider, Secret.ConversationModel, null),
    Synthesis(R.string.task_synthesis, Secret.SynthesisProvider, Secret.SynthesisModel, Secret.SynthesisVoice),
    ;

    /**
     * The providers offered for this task: those that can do it and whose keys are filled.
     *
     * A local filter with no call in it, which is what lets the screen draw its menus before
     * the network has said anything. It answers "is this offered", never "does this work" --
     * the second is the probe's business, and a key that is present but refused stays in the
     * list with its task turned off and saying why (`docs/reference.md`).
     */
    fun offered(values: Map<Secret, String>): List<Provider> =
        Provider.entries.filter { this in it.does && it.ready(values) }

    /** Who is doing this task, or nothing when the choice has not been made or has lapsed. */
    fun chosen(values: Map<Secret, String>): Provider? =
        values[provider]?.let { id -> Provider.entries.firstOrNull { it.id == id } }
            ?.takeIf { this in it.does && it.ready(values) }
}

/**
 * A provider, and what it takes to use it.
 *
 * The two ways of being agnostic are opposite, and this is the one the doc keeps: each
 * provider declares what it can do, and the app turns bricks on and off accordingly --
 * rather than exposing only what all of them share, which would waste whatever the best one
 * has. What follows from it is that the set of working features depends on who is chosen,
 * and an unavailable option must carry its reason on screen.
 *
 * [models] is a short curated list per task, not a fetch. Replicate hosts tens of thousands
 * of models and publishes no "what can run this task" endpoint, so a list of every model is
 * neither obtainable nor useful; what is worth offering is what has been looked at. It is a
 * provisional constant and says so, like the model name in [DeepseekConversation]. What
 * genuinely is fetched is the step after -- the voices of the chosen synthesis model -- and
 * that fetch is the probe.
 */
enum class Provider(
    val id: String,
    val label: String,
    val needs: List<Secret>,
    val does: Set<Task>,
    val models: Map<Task, List<String>>,
) {
    Replicate(
        id = "replicate",
        label = "Replicate",
        needs = listOf(Secret.ReplicateApiKey, Secret.ReplicateEndpoint),
        does = setOf(Task.Recognition, Task.Synthesis),
        models = mapOf(
            // whisperx first because it is the one that gives word spans. whisper is kept
            // beside it precisely because it does not: the app has to hold up without them,
            // and the seam's own contract allows a word with no bounds.
            Task.Recognition to listOf("victor-upmeet/whisperx", "openai/whisper"),
            Task.Synthesis to listOf("resemble-ai/chatterbox-turbo", "resemble-ai/chatterbox"),
        ),
    ),
    Azure(
        id = "azure",
        label = "Azure Speech",
        needs = listOf(Secret.AzureSpeechKey, Secret.AzureSpeechRegion),
        does = setOf(Task.Recognition, Task.Synthesis),
        // Azure exposes one recognition and one synthesis behind the region, so there is no
        // model to pick: the region is the endpoint and the voice is the whole choice.
        models = emptyMap(),
    ),
    Deepseek(
        id = "deepseek",
        label = "DeepSeek",
        needs = listOf(Secret.DeepseekApiKey, Secret.DeepseekEndpoint),
        does = setOf(Task.Conversation),
        // Named outright, never by an alias: `deepseek-chat` was measured being served by
        // `deepseek-v4-flash`, which the trace showed and nothing else would have. A measure
        // is worth what the model behind it is known to be.
        models = mapOf(Task.Conversation to listOf("deepseek-v4-pro", "deepseek-v4-flash")),
    ),
    ;

    /**
     * Whether the user has filled in what this provider needs.
     *
     * An endpoint does not count: it is an override with a default behind it, so leaving it
     * empty is the ordinary case and not an omission.
     */
    fun ready(values: Map<Secret, String>): Boolean =
        needs.filterNot { it in ENDPOINTS }.all { !values[it].isNullOrBlank() }

    /**
     * The voices this provider offers for [model], asked of the provider itself.
     *
     * This is the doc's probe made concrete, and the reason nothing here is cached: a
     * cached catalogue dies in silence, and what a provider advertises is not what a
     * particular key unlocks. A list that comes back is a key that works.
     *
     * **What each voice is labelled with is whatever the provider publishes, and no more.**
     * Azure publishes the locale, so its voices say `en-GB` or `en-US` outright. Chatterbox
     * publishes an enumeration of twenty first names and nothing else -- no locale, no
     * gender, not a word of description -- so its voices carry their name alone. Inventing a
     * name-to-accent table here would be putting a norm on screen that nobody publishes, and
     * the screen would state it with the same confidence as Azure's real one. The accent of
     * `Abigail` is knowable by listening, which is what the preview button is for.
     */
    suspend fun voices(store: SecretStore, model: String): List<VoiceOption> = when (this) {
        Replicate -> ReplicateClient.choicesFor(ReplicateClient(store).schema(model), "voice")
            .map { VoiceOption(id = it, label = it) }
        Azure -> azureVoices(store)
        Deepseek -> throw ChainFailure("DeepSeek has no voices")
    }

    /** Azure publishes the whole voice list for a region, so it is read whole and filtered. */
    private suspend fun azureVoices(store: SecretStore): List<VoiceOption> {
        val values = store.values().first()
        val key = values[Secret.AzureSpeechKey]
            ?: throw ChainFailure("no Azure Speech key has been entered")
        val region = values[Secret.AzureSpeechRegion]
            ?: throw ChainFailure("no Azure Speech region has been entered")
        val answer = withContext(Dispatchers.IO) {
            Http.get(
                "https://$region.tts.speech.microsoft.com/cognitiveservices/voices/list",
                mapOf("Ocp-Apim-Subscription-Key" to key),
            ).decodeToString()
        }
        val listed = org.json.JSONArray(answer)
        return (0 until listed.length()).mapNotNull { at ->
            val voice = listed.optJSONObject(at) ?: return@mapNotNull null
            val locale = voice.optString("Locale")
            val id = voice.optString("ShortName")
            if (!locale.startsWith("en-") || id.isBlank()) return@mapNotNull null
            // The id already holds the locale, but buried in `en-GB-SoniaNeural`. Said in
            // front, it answers the only question anyone asks of a voice list.
            val named = voice.optString("DisplayName").ifBlank { id }
            val gender = voice.optString("Gender")
            VoiceOption(
                id = id,
                label = "$locale · $named" + if (gender.isBlank()) "" else " ($gender)",
            )
        }.sortedBy { it.label }
    }

    private companion object {
        /** Overrides with a default behind them, so blank is the ordinary case. */
        val ENDPOINTS = setOf(Secret.ReplicateEndpoint, Secret.DeepseekEndpoint)
    }
}

/**
 * A voice to offer: what to store, and what to show for it.
 *
 * The two are separate because they come from different places. [id] is the provider's own
 * name for the voice and is all the app ever sends back; [label] is whatever that provider
 * saw fit to publish about it, which is a great deal at Azure and nothing at all at
 * chatterbox.
 */
data class VoiceOption(val id: String, val label: String)

/**
 * What [Task.model] holds, or the first model the provider offers when nothing was chosen.
 *
 * A documented default at the contract, not a silence over a missing setting: the doc makes
 * the model a per-task parameter with a provisional constant behind it, and the screen shows
 * which one is in force. A provider with no model list -- Azure -- gets an empty string,
 * which its implementations neither read nor need.
 */
fun modelFor(task: Task, provider: Provider, values: Map<Secret, String>): String =
    values[task.model]?.takeIf { it in provider.models[task].orEmpty() }
        ?: provider.models[task]?.firstOrNull().orEmpty()
