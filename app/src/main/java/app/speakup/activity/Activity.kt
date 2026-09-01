package app.speakup.activity

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
    val format: Format,
    /**
     * What the activity is about, as free text.
     *
     * Free text on purpose, plus structured pointers only where they come for nothing -- the
     * sounds, which the analysis already returns coded. Telling two activities that bear on
     * the same thing apart is reading work, which the language model does; making it schema
     * work would cost a closed vocabulary and buy less than it costs.
     */
    val matter: String = "",
    /** How it was set. Null while nothing sets it. */
    val settings: Settings? = null,
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
    companion object {

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
            format = Format.Conversation,
            status = Status.Running,
            createdAt = now,
            startedAt = now,
            by = Prescriber.Learner,
        )
    }
}

/**
 * What kind of activity it is.
 *
 * The conversation is the only one named, and that is not a placeholder: the design leaves
 * the catalogue of activities and how each one runs to be specified, and says the separate
 * activities stay in the model ready to be filled rather than built.
 */
enum class Format { Conversation }

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
    val says: String,
)

/**
 * What an activity was set to, aptitude by aptitude, fixed for the whole of its run.
 *
 * **The form of the settings depends on the activity**; this is the conversation's. They bend
 * the content through the prompt, and they can bend the shape too: pronunciation at zero puts
 * the sound analysis out rather than computing it to show nothing of it.
 *
 * The levels are empty here and the step that gives them values is still ahead. What a level
 * is worth is not settled either: of the five aptitudes only two have a written mechanism --
 * the severity cran of wording, in three notches, and the marking threshold of elocution.
 * They are held as a number wide enough for both rather than as a shape that would have to be
 * guessed now.
 */
data class Settings(val levels: Map<Aptitude, Float> = emptyMap())

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
    Wording,
    /** Finding one's words fast enough, carrying on, not stopping in the middle. */
    Fluency,
    /**
     * The exact word, the register, the shade.
     *
     * The only one whose failure is invisible: nothing signals that what was just said is a
     * poor version of the idea.
     */
    Range,
}
