package app.speakup.chain

import app.speakup.capture.Ending
import app.speakup.judged.Judgement
import app.speakup.activity.Brief
import app.speakup.activity.Character
import app.speakup.levers.Positions

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
     * [scene] is what this activity is, frozen at launch: who is speaking and what is being
     * played. Its name is sent every turn and comes back only while there is none -- see
     * [Reply.about].
     */
    suspend fun reply(
        history: List<Exchange>, heard: List<Word>, scene: Scene, present: Present = Present(),
    ): Reply
}

/**
 * What governs the turn being answered, rebuilt every time.
 *
 * [positions] are the sitting's levers, of which only the **requested** ones reach the model
 * -- the ones it holds, which exist as nothing but an instruction. What the app does itself
 * has nothing to say here.
 *
 * [ending] is how the recording stopped, and the app knows it rather than leaving the model to
 * guess from a transcript: faced with *"I went to the"*, rebuilding *"I went to the market"*
 * would have the model's voice say a word nobody said, and every mark on the turn would land
 * beside its sound.
 */
data class Present(
    val positions: Positions = Positions(),
    val ending: Ending? = null,
    /**
     * Which passage of the sitting is being spoken, counting from one.
     *
     * **A passage and not a turn**: it holds every attempt at one thing the learner set out
     * to say, and the model sees only the last of them anyway, so counting recordings would
     * count something nobody shows it.
     *
     * **Sent every turn, and it is a fact rather than a verdict.** The front-door rule keeps
     * the state away from the model because a receptionist who lets slip *"you have one
     * chance left"* breaks his own fiction -- but that guards **judgements about the
     * learner**. Where the scene stands is the shape of the scene, an author's business, and
     * a character plays it. What it means is left to the brief: the permanent context says
     * the number carries no instruction of its own, so in a free conversation, which has no
     * brief and no ending, it means nothing. **Not proved**: that the model leaves it alone
     * when nothing asks it to is a bench check, not a guarantee.
     *
     * One by default, which is what a sitting with nothing said yet is on.
     */
    val passage: Int = 1,
)

/**
 * What this activity is, frozen at launch: part 2 of the instruction.
 *
 * **This is where the persona lives, and it had to leave the permanent context.** *Warm and
 * curious* is a trait of character, and written into the part every activity shares it would
 * have governed the hostile bouncer and the bored receptionist too. What is permanent is what
 * the app is and what it returns; who is speaking comes from the definition, where an author
 * can write someone else.
 *
 * [titled] is the name this sitting goes by: a definition's own for a scene, and what the
 * model called it for a conversation that had no name. Once set it does not move.
 */
data class Scene(
    val titled: String? = null,
    val brief: Brief? = null,
    val cast: List<Character> = emptyList(),
)

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
     * What the conversation is about, **and only ever for one that has no name yet**.
     *
     * It fills the activity's `matter` and not a title of its own, which is why it is not
     * called one: a definition's title is a declared short name for the status line, and this
     * is the subject of a conversation nobody named. Null is the ordinary answer and the field
     * is ordinarily absent.
     *
     * **What holds a declared name still is the code and not this instruction.** The prompt
     * asks for nothing once there is a name, which saves the tokens; the pipeline ignores one
     * that comes anyway, which is what makes it true.
     */
    val about: String? = null,
)
