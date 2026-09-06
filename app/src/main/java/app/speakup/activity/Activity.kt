package app.speakup.activity

import app.speakup.levers.Positions
import app.speakup.notes.Weights
import app.speakup.rules.Instructing
import app.speakup.rules.Rule
import java.util.UUID

/**
 * One piece of work: what it is, what it is about, how it was set, where it got to, when, and
 * how it went.
 *
 * **An activity is one object** (`docs/design/activity-model.md`). There is nothing separate
 * for its scope and nothing separate for its run: the two were only ever told apart by an
 * indirection that nothing had a use for.
 *
 * **The conversation is one of these**, in the same table and with the same fields, and every
 * field means something for it: its matter is what is being talked about, its settings are
 * the ones set for the sitting, its status says whether it is still open, its outcome adds up
 * like any other. What sets it apart is how it behaves, never its shape -- its thread is just
 * the ordered run of its utterances, so no field carries it.
 *
 * *Naming*: this shares a name with `android.app.Activity`, which one debug screen imports.
 * The word is the design's and is kept, since renaming the central idea to dodge a collision
 * with a class that never meets it costs more at every reading than it saves once.
 */
data class Activity(
    /**
     * What the activity is about, as free text.
     *
     * Free text on purpose, plus structured pointers only where they come for nothing -- the
     * sounds, which the analysis already returns coded. Telling two activities that bear on
     * the same thing apart is reading work, which the language model does; making it schema
     * work would cost a closed vocabulary and buy less than it costs.
     */
    val matter: String = "",
    /**
     * What opens the sitting: the situation, and the staging only the character sees.
     *
     * **Not to be confused with [matter]**, which says what it turned out to be about and
     * which the AI may write after the fact: an instruction *push him onto the past, he
     * avoids it* can give a conversation whose matter ends up being *his move*. Confusing
     * them would let a title written by the AI overwrite the brief.
     *
     * Null in a free conversation until the learner writes one -- the one activity whose
     * brief comes from him rather than from a file.
     */
    val brief: Brief? = null,
    /**
     * Where every lever of this sitting sits, and **that is the whole of what it stores of
     * its settings** (`docs/design/activity-model.md`).
     *
     * The test is that every entry answers the same question: *where is this parameter right
     * now*. A ramp does not answer it, it says how things will change; an origin does not
     * either, it says where the settings came from. Those have fields of their own, below.
     *
     * **Always on the line, a definition or no definition.** A definition is a template
     * applied at creation and not a dependency kept afterwards: the alternative -- a pointer
     * for sittings from a block, values for the rest -- would make one field sometimes a
     * pointer and sometimes values, and force every reader to settle which before reading.
     * The duplication cannot drift, the definition being fixed.
     */
    val settings: Positions = Positions(),
    /**
     * What this sitting looks at, fixed when it was written and moved by nothing.
     *
     * **The weights are constant for the whole sitting, and no patch moves one.** Otherwise
     * the closing note would be a mean of measures taken under different rules, unreadable
     * for the learner as for a ranking -- which is the argument served everywhere here: a note
     * is never read without the combination that produced it, so there has to be one.
     *
     * Null while nothing weighs anything, which is a free conversation until its definition
     * is written (step 14).
     */
    val weights: Weights? = null,
    /**
     * The instructions in force at the start, by judged marking.
     *
     * **Only a judged marking takes them** -- free text entering the criterion the judge
     * reads, and never a sheet: two sheets are read from one pass of the judge over the spans,
     * and there is nothing for a per-sheet instruction to attach to. On the spans an
     * instruction touches **relevance alone**; the correctness notches are absolute.
     */
    val instructions: List<Instructing> = emptyList(),
    /**
     * What may change during the sitting, and when.
     *
     * They come from the definition and are copied onto the line with everything else: a
     * sitting whose settings a rule moved cannot recompute its own state without them.
     */
    val rules: List<Rule> = emptyList(),
    /**
     * What a draw or the AI chose, in order.
     *
     * **Without it a sitting whose settings a draw or the AI moved no longer recomputes**, so
     * it no longer compares to itself three weeks later. It is the project's criterion applied
     * to the two deciders nothing reproduces: store what depends on something that will not be
     * found again. Keeping a random seed would cost less and would only replay right if the
     * code had not moved.
     */
    val journal: List<Chosen> = emptyList(),
    /**
     * Which definition this came from, and at which version. Null for a free conversation.
     *
     * **It only ever groups** -- opening the next level, filing a score -- and is never
     * consulted to know how the sitting was set: that is on the line. And it names the
     * **version** as well, without which an update that fixes a scene makes two runs that
     * believe themselves the same incomparable.
     */
    val origin: Origin? = null,
    /**
     * Which rules engine produced [journal].
     *
     * **Everything stored carries the version of what produced it**, applied to the run
     * itself. Replaying this journal under a semantics that has changed -- a release later --
     * gives a different state, and a sitting stops comparing to itself. **What is done about
     * it is the same answer as for the cache eviction: a change of engine takes the
     * resumption away**, the sitting staying readable without being able to carry on. Replaying
     * a journal under a semantics that moved would give another state without saying so, which
     * is worse than stopping.
     */
    val engine: Int = ENGINE,
    val status: Status,
    val createdAt: Long,
    /** When it stopped being a suggestion. Null while it has not started. */
    val startedAt: Long? = null,
    /** When it reached [Status.Finished] or [Status.Abandoned]. Null while it is open. */
    val endedAt: Long? = null,
    val outcome: Outcome? = null,
    /** What filled this in. */
    val by: Prescriber,
    val id: String = UUID.randomUUID().toString(),
) {

    /**
     * Whether this sitting can be carried on, or only read.
     *
     * **A change of rules engine takes the resumption away**, and that is the same answer the
     * project gives for the cache eviction: the sitting stays readable without being able to
     * go on. Its effective state is recomputed from the settings, the rules and the journal,
     * and replaying that journal under a semantics that has moved gives a different state
     * **without saying so**, which is worse than stopping.
     *
     * A journal with nothing in it replays under any semantics: there is nothing to reread,
     * so nothing can be reread differently.
     */
    val resumable: Boolean get() = journal.isEmpty() || engine == ENGINE

    companion object {

        /**
         * The rules engine this build interprets by.
         *
         * Bumped when the **meaning** of a rule changes, never when a rule is added: what it
         * protects is a stored journal being replayable, and a journal only stops replaying
         * when what it says comes to mean something else.
         */
        const val ENGINE = 1

        /**
         * A conversation, open, at one instant.
         *
         * The clock is read once and both stamps take it. Read twice, they differ by however
         * long the two lines took, and a conversation that started before it was created is
         * a fact nobody meant to record.
         *
         * Prescribed by the learner: opening the app is the learner asking for it, and the
         * design makes the learner the way new matter gets in. Running from the moment it
         * exists -- a conversation is never a suggestion waiting to be taken up.
         */
        fun conversation(now: Long = System.currentTimeMillis()) = Activity(
            status = Status.Running,
            createdAt = now,
            startedAt = now,
            by = Prescriber.Learner,
        )
    }
}

/**
 * Where an activity has got to.
 *
 * A suggestion is an activity that never started, and [Dismissed] is a gesture that counts:
 * without it a refused suggestion comes back.
 */
enum class Status { Suggested, Accepted, Dismissed, Running, Finished, Abandoned }

/**
 * What fills an activity in -- its matter and its settings.
 *
 * A fourth may come, the wider context suggesting of its own accord. It has nothing to
 * prepare: adding a prescriber is only one more way of filling the same fields.
 */
enum class Prescriber {
    /** The conversation, out of what has just happened in it. */
    Conversation,
    /** The learner, by choice or by a free instruction. This is how new matter gets in. */
    Learner,
    /** Progression, for what is due, never got right, or about to be forgotten. */
    Progression,
}

/**
 * How an activity went: an outcome, who judged, when, and free text.
 *
 * **Stored and never recomputed**, because it rests on a judgement -- of an AI or of a person
 * -- that nothing reproduces identically. Knowing who judged and when is what makes two
 * outcomes separated in time comparable; without that attribution, any aggregate mixes judges
 * without saying so. This is the smallest form, and it will grow.
 */
data class Outcome(
    val verdict: String,
    val judge: String,
    val at: Long,
    /**
     * The answers to the questions the definition declared, and nothing else.
     *
     * A challenge wanting a closing comment declares the question -- *what worked, what stuck?*
     * -- and an activity that declares none has no text. A mechanism rather than a field to
     * put anything in.
     */
    val says: String,
    /**
     * A number, where the sitting produces one. Null everywhere else.
     *
     * **The arcade neither passes nor fails**, it returns a score: its ending is zero lives,
     * and what counts is the number the outcome carries. Filing that in the free text would
     * make it unusable.
     */
    val score: Int? = null,
)

/**
 * What opens a sitting, in the two halves it cuts into and no more.
 *
 * [situation] is true for everybody and is readable on the pre-game screen too. [staging]
 * addresses the character alone and is **never shown**, on pain of sabotaging itself. Going
 * finer would encroach on the instructions, which are already a field of their own with their
 * own readers and their own lives.
 *
 * The price of the single call is here, and it is said once: the staging sits in the judge's
 * context, where it has no business, and what holds it at arm's length is a sentence saying
 * the judge's criterion is the instruction. That is checked at a bench; it is not proved.
 */
data class Brief(val situation: String, val staging: String = "")

/**
 * Which definition a sitting came from, and at which version.
 *
 * A file shipped with the app **inherits the release's version for nothing**, where a row
 * created at runtime has no natural one and would need one stamped on it and a migration
 * written for it.
 */
data class Origin(val definition: String, val version: String)

/**
 * One choice a draw or the AI made, kept so the sitting recomputes.
 *
 * What does not come back on its own is the **choice**, and that alone: which rule fired, at
 * which passage, and which of its packs was taken. The effects follow from the rule and the
 * choice, so storing them too would be a second source that could drift from the first.
 */
data class Chosen(val rule: String, val passage: Int, val pack: Int, val at: Long)

/**
 * The five aptitudes. They are independent: one can be intelligible and slow, correct and
 * poor, fluent and wrong.
 */
enum class Aptitude {
    /** The sounds, the rhythm, the stress of words, the melody. What decides being understood. */
    Elocution,
    /** Following someone at their speed, with their reductions, without a text. */
    Understanding,
    /** The well-formed sentence, and the rule applied while speaking rather than known. */
    Correctness,
    /** Finding one's words fast enough, carrying on, not stopping in the middle. */
    Fluency,
    /**
     * The exact word, the register, the shade.
     *
     * The only one whose failure is invisible: nothing signals that what was just said is a
     * poor version of the idea.
     *
     * It is the one that carries an activity's instruction, so everything a challenge wants
     * to demand -- a register, an imposed length, a forbidden word -- lands here.
     */
    Relevance,
}
