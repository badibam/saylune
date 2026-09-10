package app.saylune.chain

import app.saylune.capture.Ending
import app.saylune.judged.Judgement
import app.saylune.activity.Brief
import app.saylune.activity.Character
import app.saylune.activity.Question
import app.saylune.levers.Positions
import app.saylune.rules.Instructing

/**
 * The language model: it answers, it decides what the learner meant, and it marks.
 *
 * **One call does every job** -- playing the character, rebuilding `intended`, marking the
 * spans, judging the following, rating the difficulty of its own turn, and picking from the
 * menu when a rule offers one (`activity.md`). Price is not what settles it: a second
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
     * played: who is speaking, and what is being played.
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
    /**
     * The instructions standing on the judged markings, in the words a definition wrote.
     *
     * **A instruction has three readers, which is what tells it from every other effect**: the
     * learner, who has to read it to follow it; the character, who has to know it to play with
     * it; and the judge, who marks against it. This is the character's copy.
     */
    val instructions: List<Instructing> = emptyList(),
    /**
     * What a rule has just told the model, in an author's words.
     *
     * **This is the front door, and it is the only way the state reaches the model.** A lost
     * life, a failed passage, a threshold that just got shorter reach it only if a rule decided
     * to say so. That is what gives an author control over what their character knows: with no
     * rule the fault changes nothing in the scene; with one, the receptionist sighs and doubts
     * the booking was ever made.
     */
    val said: List<String> = emptyList(),
    /**
     * Whether this turn has **no learner turn in front of it**.
     *
     * A rule can make the character speak of its own accord -- the alarm going off, the
     * passer-by, the last word of a scene that is ending -- and then there is nothing to
     * transcribe, nothing to mark and nothing to answer. It is declared here rather than read
     * off an empty transcript: an emptiness is indistinguishable from a recording nobody spoke
     * into, and the two want opposite things.
     *
     * **It falls only at the opening or at a passage's close**, never while somebody is
     * recording: nothing cuts off a person who is still speaking.
     */
    val provoked: Boolean = false,
    /**
     * The questions the app is putting on this turn, whose answers are **required fields**.
     *
     * **The app serves them and the model answers on the spot.** That is the inversion the
     * mechanism rests on: not a menu of empty fields the model fills when it likes, but a
     * question put at the moment its author wrote, so that the presence of an answer is
     * checkable like any other field of the contract.
     *
     * They ride on the first call after the moment that put them, exactly as a rule's message
     * does: a question due at the end of an attempt is one the call for that attempt has
     * already gone without, and the next call is the earliest there is.
     */
    val asking: List<Question> = emptyList(),
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
 * **The name is not here**, because the model has no use for one: what a sitting is called is
 * a label on a tile, decided by the file it came from, and the brief already says everything
 * about the scene that the character has to know.
 */
data class Scene(
    val brief: Brief? = null,
    val cast: List<Character> = emptyList(),
    /**
     * What the learner asked the conversation to stay away from, in their own words.
     *
     * **It is not the activity's and it sits here anyway**, which is worth saying rather than
     * leaving to be noticed: it is a fact about the person, identical in every scene they
     * open. What puts it in part 2 is that part 2 is the last stable thing before the history
     * -- it does not change inside a sitting, so it belongs in the head of the instruction that
     * a provider's cache can keep, and not in part 4, which is rebuilt and resent every turn.
     *
     * Blank is the ordinary case and nothing at all is written then.
     */
    val avoid: String = "",
)

/**
 * One past turn, as the model should remember it.
 *
 * [established] is what the model settled on that very turn, and it travels with it rather
 * than in a section of its own. **The fact is laid where it was established**, which carries
 * its date for nothing -- a fact settled at the twelfth passage and put in a heading would
 * read as a given of the start, and the model would play somebody who had always known it --
 * and which does not break the cached prefix, the history being stable at the head and growing
 * at the tail where a heading would grow in the middle.
 */
data class Exchange(
    val fromLearner: Boolean,
    val text: String,
    val established: Map<String, String> = emptyMap(),
)

/**
 * What comes back: something to say, what the learner meant, and what was marked on it.
 *
 * `faulty` is gone, absorbed by the marking. It was a boolean over the whole turn, which
 * flattened the fact that a passage can carry several faults and made it impossible to mark
 * the portion concerned; one notch per group of words settles both.
 */
data class Reply(
    /**
     * What the learner meant, and everything marked on it. **Null on a provoked turn.**
     *
     * Null is not *nothing was marked* -- that is a judgement with no spans in it. It is a
     * turn with **nothing to judge**: nobody spoke into it, so there is no sentence to write
     * out, no group of words to mark, and no answer whose uptake could be read.
     */
    val judged: Judgement?,
    /**
     * What carries the conversation forward, in English, as it is to be said aloud.
     *
     * **It never picks the slip up**: that is [echo]'s work, and [echo] is said just before it.
     */
    val spoken: String,
    /**
     * The short line that picks the slip up, or null when nothing was marked.
     *
     * **It is not a competing reply, it is the opening of one.** The call returns this and
     * [spoken] as one utterance cut in two, and the app composes them -- so nothing is ever
     * retracted, and the founding gesture of the project, *"Ah, you're twenty-five! And where
     * do you work?"*, is the two of them joined.
     *
     * The app hears it **alone** in one case only: the passage is to reword and the
     * conversation waits. It is only produced when something was marked, which makes it free
     * on a clean passage.
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
     * What the model settled, by question key. Empty where the turn put none.
     *
     * **It comes before `spoken` in the object the model writes**, and that is not a detail of
     * order: the contract says each field written conditions the next, so the character speaks
     * knowing what has just been established and never the other way round.
     */
    val established: Map<String, String> = emptyMap(),
)
