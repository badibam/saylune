package app.saylune.keys

import androidx.annotation.StringRes
import app.saylune.R

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
    InworldApiKey("inworld.api.key", R.string.secret_inworld_key),
    InworldEndpoint("inworld.endpoint", R.string.secret_inworld_endpoint,
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
    /**
     * The judge's own three, and they are its own because the two calls are two choices.
     *
     * The one who speaks may be picked for its voice or its speed; the one who judges is the
     * careful one, and the same key can open both -- so what identifies each is its provider,
     * its model and what it spends, per link (`../../../../../docs/design/premium-chain.md`).
     */
    JudgementProvider("task.judge.provider", R.string.task_judgement, masked = false),
    JudgementModel("task.judge.model", R.string.setting_model, masked = false),
    JudgementEffort("task.judge.effort", R.string.setting_effort, masked = false),
    SparePalette("view.palette.spare", R.string.setting_palette, masked = false),

    /**
     * The four things the app itself is set by, as against the links it calls out to.
     *
     * They share this store with the keys because there is one mechanism for *what the user
     * settled* and a second one would be a second place to get wrong. What separates them is
     * the screen: the keys are an installation, visited three times in a life; these are
     * revisited, so they have a door of their own on the title screen.
     *
     * **Blank means the default everywhere here**, and the default of a look is what the
     * device says: the register follows the system's dark mode until someone says otherwise,
     * and the language follows the system's locale. Storing "system" as a value would be the
     * same state written twice.
     */
    TextWeight("view.text.weight", R.string.setting_weight, masked = false),
    TextScale("view.text.scale", R.string.setting_size, masked = false),
    Register("view.register", R.string.setting_register, masked = false),
    Language("view.language", R.string.setting_language, masked = false),

    /**
     * Which marks the conversation menu has turned **off**, comma separated.
     *
     * The channels that are off and not the ones that are on: nothing stored is then every
     * mark shown, which is what the app does before anybody has asked for anything, and a
     * channel added later is on by default rather than silently missing.
     */
    HiddenMarks("view.marks.hidden", R.string.display_marks, masked = false),

    /**
     * Whether the passage's notes are pushed between two passages.
     *
     * **What the mode decides is the letters; what the learner sets is whether the screen is
     * pushed** (`ui.md`). Stored as the refusal rather than the wish, like the marks:
     * nothing stored is pushed, which is the default, and it stays the default for anyone who
     * has never opened the setting.
     */
    NotesUnpushed("view.notes.unpushed", R.string.notes_pushed, masked = false),

    /**
     * Whether the word marks come off a passage the words' gate has let through.
     *
     * Stored as the wish and not as the refusal, unlike its two neighbours: **keeping them is
     * the default** (`ui.md`) -- a passage that passes still carries marks, the gate reading
     * a note and not the absence of one -- so nothing stored has to mean *kept*, and the
     * learner who wants the line quieter once he can no longer act on it says so.
     */
    MarksDropped("view.marks.dropped", R.string.display_drop_words, masked = false),
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

    /**
     * What the learner would rather the conversation stayed away from, in their own words.
     *
     * **Prose and not a list of boxes**, on purpose and unlike the ones a scene declares: what
     * a person needs kept away is theirs, it is not drawn from a vocabulary somebody else
     * wrote, and the model reads prose better than it reads a taxonomy. It is read at every
     * call and sits in the stable head of the instruction, since it does not change inside a
     * sitting.
     *
     * Blank is the default and nothing at all is sent then -- not an empty section, not a
     * sentence saying there is nothing.
     */
    Avoid("talk.avoid", R.string.setting_avoid, masked = false),

    /**
     * Whether a scene's declared warnings are shown before it is started.
     *
     * **Off by default, and that is the whole design**: whoever has nothing to keep away from
     * is being handed a list of what a scene is about, which is a spoiler nobody asked for.
     * Stored as the wish rather than the refusal, so nothing stored means not shown.
     */
    ShowTriggers("talk.triggers.shown", R.string.setting_show_triggers, masked = false),
    ;
}
