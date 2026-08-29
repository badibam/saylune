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
     */
    suspend fun reply(history: List<Exchange>, heard: List<Word>): Reply
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
 * The gate itself is not in this type yet, and that is a placeholder, not a design: v1 owes
 * a severity cran and a verdict here (`../../../../../../TODO.md`).
 */
data class Reply(
    val spoken: String,
    val intended: String,
)
