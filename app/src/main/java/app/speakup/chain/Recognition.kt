package app.speakup.chain

import java.io.File

/**
 * What the learner's mouth said, as words.
 *
 * The shape of this seam is an acted decision, not an implementation detail
 * (`docs/reference.md`). A file goes in, words with their spans come out: no key in the
 * signature, no network failure in the contract, no latency assumed. Recognition is the one
 * link of the chain that can stop being remote -- sherpa-onnx runs a recognition model
 * through the ONNX Runtime the app already carries for the analysis -- and a seam that
 * presumes the remote is paid back in a rewrite the day the local one wins.
 *
 * The division of labour with the language model is a rule of the project and it runs
 * through this type: the recognition transcribes the mouth, the language model decides the
 * intention. So an implementation is judged on verbatim fidelity, and one that repairs
 * grammar is disqualified however good its prose -- it erases the learning signal before
 * anything has judged it. Normalising a *sound* toward a plausible word is the wanted
 * behaviour and a different thing: "I sink" coming back as `think` was measured, and it
 * was right.
 *
 * Punctuation is not asked of this link and is discarded if offered. It belongs to the
 * language model, which punctuates by the intention it answers.
 */
interface Recognition {

    /**
     * The words of [audio], in order. Throws [ChainFailure] rather than returning an empty
     * transcript for a turn it could not read: silence and failure must not arrive looking
     * alike, since one of them is a legitimate turn.
     */
    suspend fun transcribe(audio: File): List<Word>
}

/**
 * One word and where it sits in the recording.
 *
 * The spans are here because the analysis anchors everything to the text, and because a
 * turn too long to analyse in one pass is cut somewhere -- both want to know where a word
 * begins. An implementation that cannot give spans reports the word alone; the analysis
 * partitions sounds between words on its own, at 95% on the bench.
 */
data class Word(
    val text: String,
    val startMs: Int? = null,
    val endMs: Int? = null,
)
