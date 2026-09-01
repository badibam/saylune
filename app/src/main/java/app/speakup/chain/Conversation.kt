package app.speakup.chain

/**
 * The language model: it answers, and it decides what the learner meant.
 *
 * Two jobs in one call, and that is the point of the chain being STT -> LLM -> TTS rather
 * than a voice-to-voice API: the reference text the analysis needs travels inside the call
 * the app makes anyway, where voice-to-voice would cost an extra round trip per turn just
 * to obtain it (`docs/reference.md`).
 *
 * The provider is not chosen. DeepSeek measured the chain's latency -- 2.6 s to first
 * sound -- and measuring is all it has done.
 */
interface Conversation {

    /**
     * Answer [heard] in the context of [history]. Throws [ChainFailure]; the caller retries
     * from the kept audio file rather than asking for the sentence again.
     *
     * [titled] is what the conversation is called so far, or null while it is unnamed. It is
     * sent every turn and comes back only sometimes -- see [Reply.title].
     */
    suspend fun reply(history: List<Exchange>, heard: List<Word>, titled: String?): Reply
}

/** One past turn, as the model should remember it. */
data class Exchange(val fromLearner: Boolean, val text: String)

/**
 * What comes back: something to say, and the text the analysis will be measured against.
 *
 * [intended] is the normalisation the recognition is forbidden to do -- the words the
 * learner meant, punctuated by the intention the model is answering, because the synthesised
 * model's contour depends on that punctuation. It is a repair of the *transcript*, never of
 * the learner's grammar: a turn whose grammar is wrong keeps its wrong grammar here, since
 * the whole of the grammatical gate is downstream of it.
 *
 * [faulty] is the grammatical gate's verdict, and it is left by the model on its own
 * judgement -- no severity is sent to it yet, so what it marks is what it judges. The gate
 * decides whether the sound analysis runs at all: on a turn that is going to be rewritten,
 * the doc is explicit that the analysis is not hidden, it is not computed. It works with
 * [intended] rather than against it -- the fault stays written there exactly as it was said,
 * and this says it is there.
 *
 * What is still owed here is the severity cran, which turns the verdict from "is it wrong"
 * into "is it wrong at the cran set for this conversation", and the span of the fault, without
 * which the discreet mark the doc asks for has nowhere to sit
 * (`../../../../../../TODO.md`).
 */
data class Reply(
    val spoken: String,
    val intended: String,
    val faulty: Boolean,
    /**
     * A new name for the conversation, or **null to leave the one it has**.
     *
     * Null is the ordinary answer and the field is ordinarily absent. The model is given the
     * current title every turn and asked to send one back only when there is a reason -- the
     * conversation has none yet, or what is being talked about has drifted far enough that
     * the old name no longer describes it. A title rewritten every turn is a title nobody can
     * recognise in a list, which is the one thing it exists for.
     */
    val title: String? = null,
)
