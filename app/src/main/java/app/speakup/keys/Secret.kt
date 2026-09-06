package app.speakup.keys

import androidx.annotation.StringRes
import app.speakup.R

/**
 * One thing the user settles so a remote link can work.
 *
 * Not all of these are secrets. A region, an endpoint, the provider a task is given, the
 * model and the voice it uses are configuration -- and the endpoint is configurable on
 * purpose: depending on a protocol rather than on one host is the difference the wisdom
 * asks for. They share the store anyway, because two mechanisms for "what the user settled"
 * would be two places to get wrong for no gain, and encrypting a region costs nothing.
 *
 * [id] is the storage key and never changes: renaming an entry would silently lose whatever
 * the user had already entered under the old name.
 *
 * **Nothing here says whether the app can run.** It used to: one flag marked an entry
 * required, and a single `allPresent` decided the whole chain was ready. That reading dies
 * the moment a task has two possible providers -- the Azure key is indispensable to someone
 * whose recognition is Azure and pointless to someone whose recognition is Replicate.
 * Readiness is asked per task now, of `providers.Task`.
 */
enum class Secret(
    val id: String,
    @StringRes val label: Int,
    /** False for what is configuration rather than a credential, and safe to read back. */
    val masked: Boolean = true,
    /**
     * True for an override that has a default behind it, so leaving it blank is the ordinary
     * case and never an omission.
     *
     * It lives on the entry rather than in a set beside whoever reads it, because a set is a
     * second place to remember: a provider added with an endpoint and left out of that set is
     * a provider that never becomes offerable, and nothing says why.
     */
    val overridable: Boolean = false,
) {
    ReplicateApiKey("replicate.api.key", R.string.secret_replicate_key),
    ReplicateEndpoint("replicate.endpoint", R.string.secret_replicate_endpoint,
                      masked = false, overridable = true),
    AzureSpeechKey("azure.speech.key", R.string.secret_azure_key),
    AzureSpeechRegion("azure.speech.region", R.string.secret_azure_region, masked = false),
    ElevenLabsApiKey("elevenlabs.api.key", R.string.secret_elevenlabs_key),
    DeepseekApiKey("deepseek.api.key", R.string.secret_deepseek_key),
    DeepseekEndpoint("deepseek.endpoint", R.string.secret_deepseek_endpoint,
                     masked = false, overridable = true),
    OpenaiApiKey("openai.api.key", R.string.secret_openai_key),
    OpenaiEndpoint("openai.endpoint", R.string.secret_openai_endpoint,
                   masked = false, overridable = true),

    /**
     * Which provider does each task, and with what.
     *
     * Configuration, not credentials, so they are not masked -- the user has to be able to
     * read back the model they chose. They are written by the settings screen's selectors
     * and read by the switching implementations in `providers`.
     */
    RecognitionProvider("task.stt.provider", R.string.task_recognition, masked = false),
    RecognitionModel("task.stt.model", R.string.setting_model, masked = false),
    ConversationProvider("task.llm.provider", R.string.task_conversation, masked = false),
    ConversationModel("task.llm.model", R.string.setting_model, masked = false),
    ConversationEffort("task.llm.effort", R.string.setting_effort, masked = false),
    SparePalette("view.palette.spare", R.string.setting_palette, masked = false),
    SynthesisProvider("task.tts.provider", R.string.task_synthesis, masked = false),
    SynthesisModel("task.tts.model", R.string.setting_model, masked = false),

    /**
     * The voice, with no provider in its name.
     *
     * It replaces an `azure.voice` that carried one, and the rename is deliberate rather
     * than careless: what it holds has genuinely changed meaning. It used to be an Azure
     * voice id typed by hand; it is now whichever voice the chosen synthesis provider
     * offers, picked from a list. Keeping the old key would have handed an Azure name to a
     * Replicate model. The cost is one setting to make again, once, and it is now chosen
     * from a menu instead of remembered.
     */
    SynthesisVoice("task.tts.voice", R.string.setting_voice, masked = false),

    /**
     * How many megabytes of rendered models to keep, as digits.
     *
     * Configuration and not a credential. The doc asks for a ceiling with least-recently-used
     * eviction and gives no number, so the number is the user's: a cache is only ever a cache,
     * regenerable at the price of one call, and what it is worth to hold depends on the phone
     * it is held on. Blank or unreadable means the default, which is the one place in this
     * store where an absent value legitimately stands for something.
     */
    RenderCacheCap("cache.renders.cap", R.string.setting_cache_cap, masked = false),
    ;
}
