package app.saylune.providers

import androidx.annotation.StringRes
import app.saylune.R
import app.saylune.chain.ChainFailure
import app.saylune.keys.Secret
import app.saylune.keys.SecretStore
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
    /**
     * The reasoning levels this provider accepts on the conversation link, most sparing
     * first. Empty where it has no notion of one, in which case nothing is sent.
     */
    val efforts: List<Effort> = emptyList(),
) {
    Replicate(
        id = "replicate",
        label = "Replicate",
        needs = listOf(Secret.ReplicateApiKey, Secret.ReplicateEndpoint),
        does = setOf(Task.Recognition, Task.Conversation, Task.Synthesis),
        models = mapOf(
            // A second route to a language model, for two reasons the measurements gave.
            // One key fewer at the BYOK wall, since this one already carries two links; and
            // `reasoning_effort`, the lever the latency points at -- the thinking is what
            // the learner waits for. Named with their namespace, which is what tells
            // `openai/gpt-5-mini` reached this way from a `gpt-5-mini` reached direct.
            Task.Conversation to listOf("openai/gpt-5-mini", "openai/gpt-5-nano"),
            // whisperx first because it is the one that gives word spans. whisper is kept
            // beside it precisely because it does not: the app has to hold up without them,
            // and the seam's own contract allows a word with no bounds.
            Task.Recognition to listOf(
                "victor-upmeet/whisperx", "openai/whisper", "elevenlabs/scribe-v2",
            ),
            // The same voices reached by two routes, and they are not the same offer: the
            // direct one serves raw PCM and lists the account's whole voice library, this
            // one returns a compressed file and eight preset names from its schema. Both
            // are kept because the difference is real and the choice is the user's.
            Task.Synthesis to listOf(
                "resemble-ai/chatterbox-turbo", "resemble-ai/chatterbox",
                "elevenlabs/flash-v2.5", "elevenlabs/turbo-v2.5", "elevenlabs/v3",
            ),
        ),
        // Empty, and that is a statement about what is known rather than about the models:
        // these are OpenAI's, whose `reasoning_effort` Replicate publishes in each model's
        // schema, and nothing here has read it. The call sends `low` outright meanwhile, so
        // no default of anyone's is being paid for silently -- what is missing is the
        // choice, not the value (`../../../../../../TODO.md`).
    ),
    Inworld(
        id = "inworld",
        label = "Inworld",
        needs = listOf(Secret.InworldApiKey, Secret.InworldEndpoint),
        does = setOf(Task.Recognition, Task.Conversation, Task.Synthesis),
        // The second provider to carry all three links, and the only one whose three are its
        // own rather than other people's hosted side by side. One key at the BYOK wall
        // instead of three is the whole reason it is offered.
        models = mapOf(
            // Their router fronts hundreds of models behind one OpenAI-shaped endpoint, and
            // this list holds the one their own documentation names. **It is provisional and
            // unmeasured**, like Replicate's beside it: what belongs here is what has been
            // put through the judge's bench, and nothing has yet.
            Task.Conversation to listOf("openai/gpt-4o-mini"),
            // Whisper large-v3 through Groq is the accuracy ceiling the bench wants named
            // (`../../../../../../TODO.md`, chantier 2), and reaching it on this key means
            // the ceiling and the candidate are one account apart.
            Task.Recognition to listOf("groq/whisper-large-v3", "inworld/inworld-stt-1"),
            Task.Synthesis to listOf("inworld-tts-2"),
        ),
        // Empty, and a statement about what is known rather than about the models: nothing
        // has read whether the router forwards a reasoning level to a model that takes one.
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
    ElevenLabs(
        id = "elevenlabs",
        label = "ElevenLabs",
        needs = listOf(Secret.ElevenLabsApiKey),
        does = setOf(Task.Recognition, Task.Synthesis),
        // Billed by the character, which is why it is worth offering: a turn spends the
        // answer plus `intended`, and saying a sentence again spends nothing.
        models = mapOf(
            Task.Recognition to listOf("scribe_v1"),
            // Flash first: it is the one whose first sound arrives soonest, and the
            // synthesis is the link measured costing the most before anything is heard
            // (`../../../../../../TODO.md`).
            Task.Synthesis to listOf(
                "eleven_flash_v2_5", "eleven_turbo_v2_5", "eleven_multilingual_v2",
            ),
        ),
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
        // What the OpenAI-shaped endpoint takes: `thinking` on or off, and `reasoning_effort`
        // over low, high and max. Read 2026-09-06 on
        // https://api-docs.deepseek.com/guides/thinking_mode, which also states that thinking
        // is **on by default at high** -- so the absence of these fields is itself a level,
        // and the most expensive one.
        efforts = listOf(Effort.None, Effort.Low, Effort.High, Effort.Max),
    ),
    OpenAI(
        id = "openai",
        label = "OpenAI",
        needs = listOf(Secret.OpenaiApiKey, Secret.OpenaiEndpoint),
        does = setOf(Task.Conversation),
        // The same two models Replicate offers, on purpose: the point of this route is that
        // one model be reachable both ways, so what the detour costs stops being a deduction
        // (`../../../../../../TODO.md`).
        models = mapOf(Task.Conversation to listOf("gpt-5-nano", "gpt-5-mini")),
        // **Not what these models accept, but what can be vouched for.** OpenAI publishes no
        // capability on `/v1/models` -- id, created and owner, nothing else -- and its
        // reasoning guide says only that "some models support only a subset of these values,
        // so check the relevant model page", which does not list them either. The full
        // vocabulary is none, minimal, low, medium, high, xhigh and max; these three are the
        // ones the whole gpt-5 family has carried since it shipped. Read 2026-09-06 on
        // https://developers.openai.com/api/docs/guides/reasoning.
        //
        // The floor costs something real: `none` would be the fastest level and this route
        // exists for latency. It goes back on the menu the day a model page lists its own.
        efforts = listOf(Effort.Low, Effort.Medium, Effort.High),
    ),
    ;

    /**
     * Whether the user has filled in what this provider needs.
     *
     * An overridable entry does not count: it is a host with a default behind it, so leaving
     * it empty is the ordinary case and not an omission.
     */
    fun ready(values: Map<Secret, String>): Boolean =
        needs.filterNot { it.overridable }.all { !values[it].isNullOrBlank() }

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
        ElevenLabs -> elevenVoices(store)
        Inworld -> inworldVoices(store)
        Deepseek, OpenAI -> throw ChainFailure("$label has no voices")
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

    /**
     * ElevenLabs lists what this key can reach, which is what makes the fetch the probe:
     * a list that comes back is a key that works, and it is what *this* key unlocks rather
     * than what the plan advertises.
     */
    /**
     * Inworld's voice library, which is also the probe: a list that comes back is a key that
     * works, and the list that came back is what *this* key really unlocks.
     *
     * **The path is inferred and not read**, and it is the one thing here that a real key has
     * to confirm. Their published example clones a voice at `voices/v1/voices:clone`, which is
     * the collection plus a verb in the style their whole API follows, so the collection
     * itself is where a listing lives. Their reference page for it sits behind a login. If it
     * is wrong the screen will say the key failed when the key is fine, which is the worst
     * shape a wrong guess can take here -- so it is named rather than left to be discovered.
     */
    private suspend fun inworldVoices(store: SecretStore): List<VoiceOption> {
        val values = store.values().first()
        val answer = withContext(Dispatchers.IO) {
            Http.get(
                "${InworldApi.base(values)}/voices/v1/voices",
                InworldApi.basic(InworldApi.key(values)),
            ).decodeToString()
        }
        val listed = org.json.JSONObject(answer).optJSONArray("voices")
            ?: throw ChainFailure("Inworld listed no voices")
        return (0 until listed.length()).mapNotNull { at ->
            val voice = listed.optJSONObject(at) ?: return@mapNotNull null
            // Their own examples name a voice by a bare given name -- `Dennis`, `Olivia` --
            // so the id is what is spoken and the display name may not even differ.
            val id = voice.optString("voiceId").ifBlank { voice.optString("name") }
            if (id.isBlank()) return@mapNotNull null
            // The accent is the one thing anyone asks of a voice here, so it goes in front
            // when it is known, exactly as it does for ElevenLabs.
            val accent = voice.optString("accent").ifBlank { voice.optString("languageCode") }
            val named = voice.optString("displayName").ifBlank { id }
            VoiceOption(
                id = id,
                label = if (accent.isBlank()) named else "$accent \u00b7 $named",
            )
        }.sortedBy { it.label }
    }

    private suspend fun elevenVoices(store: SecretStore): List<VoiceOption> {
        val key = store.values().first()[Secret.ElevenLabsApiKey]
            ?: throw ChainFailure("no ElevenLabs key has been entered")
        val answer = withContext(Dispatchers.IO) {
            Http.get("${ElevenLabsSynthesis.BASE}/voices", mapOf("xi-api-key" to key))
                .decodeToString()
        }
        val listed = org.json.JSONObject(answer).optJSONArray("voices")
            ?: throw ChainFailure("ElevenLabs listed no voices")
        return (0 until listed.length()).mapNotNull { at ->
            val voice = listed.optJSONObject(at) ?: return@mapNotNull null
            val id = voice.optString("voice_id")
            if (id.isBlank()) return@mapNotNull null
            val labels = voice.optJSONObject("labels")
            // The accent is the one thing anyone asks of a voice here, and unlike Azure's
            // ids it is not written into the name -- so it goes in front when it is known.
            val accent = labels?.optString("accent").orEmpty()
            val named = voice.optString("name").ifBlank { id }
            VoiceOption(
                id = id,
                label = (if (accent.isBlank()) named else "$accent · $named") +
                    (labels?.optString("gender").orEmpty()
                        .let { if (it.isBlank()) "" else " ($it)" }),
            )
        }.sortedBy { it.label }
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
