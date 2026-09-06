package app.speakup.chain

import app.speakup.judged.Judgement

/**
 * The language model: it answers, it decides what the learner meant, and it marks.
 *
 * **One call does every job** -- playing the character, rebuilding `intended`, marking the
 * spans, judging the following, rating the difficulty of its own turn, and picking from the
 * menu when a rule offers one (`activity-model.md`). Price is not what settles it: a second
 * call would cost nothing in latency, the judgement only serving to show marks while the
 * answer is synthesised and played, and nothing in money either. What settles it is that a
 * well-structured prompt holds its boundaries -- so the boundaries are watched case by case
 * rather than walled off in advance against a wolf nobody has seen.
 *
 * **The order of the returned fields is the partition that stays free.** The model writes its
 * answer in sequence and each field written conditions the next, so `intended` drafted before
 * the character's voice is taken is worth more than the other way round.
 *
 * *The cost in latency is not bounded.* The 0,16 s on file was measured to decide about
 * pipelining the synthesis, on the contract of that day -- one reply, `intended`, one boolean.
 * This one adds three markings, the difficulty, the echo and the menu choice, of which the
 * spans weigh about half the history again; in output, sequential by nature, that is latency
 * straight onto the link that is already the project's first defect. To be re-measured now
 * that it exists, before anything is stacked on it (`../../../../../../TODO.md`).
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
 * What comes back: something to say, what the learner meant, and what was marked on it.
 *
 * `faulty` is gone, absorbed by the marking. It was a boolean over the whole turn, which
 * flattened the fact that a passage can carry several faults and made it impossible to mark
 * the portion concerned; one notch per group of words settles both.
 */
data class Reply(
    /** What the learner meant, and everything marked on it. */
    val judged: Judgement,
    /** The reply, in English, as it is to be said aloud. */
    val spoken: String,
    /**
     * A short line that picks the slip up, or null when nothing was marked.
     *
     * The call returns **the continuation and this together**, and the app plays one of the
     * two: so nothing is ever retracted, and it is literally "the model plays, the app
     * decides" -- the model supplies the matter of both outcomes without settling which.
     * It is only produced when something was marked, which makes it free on a clean passage.
     */
    val echo: String?,
    /**
     * The key the model picked out of the menu a rule offered, or null when none was offered.
     *
     * Free prose on the way out, listed keys on the way back: the app declares what is
     * available, the model **picks a key** and never invents one. Choosing from a list is what
     * models do best; calibrating a fresh constraint is not.
     */
    val choice: String?,
    /**
     * A new name for the conversation, or **null to leave the one it has**.
     *
     * Null is the ordinary answer and the field is ordinarily absent. A title rewritten every
     * turn is a title nobody can recognise in a list, which is the one thing it exists for.
     */
    val title: String? = null,
)
