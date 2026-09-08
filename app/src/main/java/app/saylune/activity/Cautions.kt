package app.saylune.activity

import androidx.annotation.StringRes
import app.saylune.R

/**
 * What a scene may warn about, as a **closed list of keys**.
 *
 * **Called a caution here and `triggers` in the file**, and the two words are on purpose: the
 * rule engine already owns `Trigger`, which is when a rule fires, and one word standing for two
 * unrelated things inside one package costs more than two words do. The file keeps the word a
 * reader knows.
 *
 * ## Why a list and not free prose
 *
 * Free prose per file would give thirteen wordings for the same thing, none of them
 * translatable once and none comparable. A key is written once here, translated once in the
 * strings, and read the same on every tile.
 *
 * **What lets a key onto this list is one test**: an author must be able to answer yes or no
 * about their own scene without supposing anything about who will play it. `mature` and
 * `sensitive` fail it -- they ask the author to guess at the reader -- and that is what keeps
 * the list from growing into a checklist nobody fills honestly.
 *
 * ## Two families, and they are not the same kind of thing
 *
 * [Kind.Manner] is **what the character does to you**, and [Kind.Subject] is **what the scene
 * is about**. Standard content-warning vocabularies are all of the second kind, because they
 * were written for texts; this app is somebody answering back, and the first family is where
 * every shipped tile actually lives -- a character who holds against your memory, one who
 * refuses you, one with a queue behind you. Borrowing a ready-made taxonomy would have missed
 * exactly the half that is used.
 *
 * ## What a declaration promises, and what it does not
 *
 * **It describes the frame, never the sitting.** Nine of the shipped scenes are built on a
 * hole the learner types themselves -- *a forgotten thing one could be blamed for*, *what you
 * are bringing back*, *what you saw happen* -- and the model improvises on top of it. So a
 * ticked box says what the scene is set up to do; what fills it comes from the learner. The
 * setting that reaches the model is the part that actually adapts, and this list is a sign at
 * the door.
 *
 * **And nothing filters on it.** Matching the learner's own prose against these keys is a
 * judgement the app cannot make, and a tile removed in silence is exactly what
 * `docs/reference.md` refuses everywhere else -- what is unavailable carries its reason. The
 * list is shown, on demand, and never used to hide anything.
 */
enum class Caution(val key: String, val kind: Kind, @StringRes val says: Int) {

    /** A reproach, a falling-out, somebody angry with you. */
    Conflict("conflict", Kind.Manner, R.string.trigger_conflict),

    /** Cold, dismissive, or refusing what you ask for. */
    Hostility("hostility", Kind.Manner, R.string.trigger_hostility),

    /** Lying, or holding out against your own account of what happened. */
    Deception("deception", Kind.Manner, R.string.trigger_deception),

    /** A clock, a queue, somebody waiting on you. */
    Pressure("pressure", Kind.Manner, R.string.trigger_pressure),

    /** Being questioned, assessed, or recorded. */
    Scrutiny("scrutiny", Kind.Manner, R.string.trigger_scrutiny),

    /** Asking after your life, your feelings, your past. */
    Intimacy("intimacy", Kind.Manner, R.string.trigger_intimacy),

    /** Loss, death, serious illness. */
    Grief("grief", Kind.Subject, R.string.trigger_grief),

    /** An act of violence, witnessed or told. */
    Violence("violence", Kind.Subject, R.string.trigger_violence),

    /** Bodies, injury, being cared for. */
    Body("body", Kind.Subject, R.string.trigger_body),

    /** Being singled out for what one is. */
    Discrimination("discrimination", Kind.Subject, R.string.trigger_discrimination),

    /** Drink, drugs, dependence. */
    Substances("substances", Kind.Subject, R.string.trigger_substances),

    /** Alone, stranded, at night. */
    Isolation("isolation", Kind.Subject, R.string.trigger_isolation);

    /** Which of the two families this belongs to. */
    enum class Kind { Manner, Subject }

    companion object {

        /**
         * The trigger [key] names, and **an unknown one fails outright**.
         *
         * A definition is content the app ships, so a typo is a file that says nothing where
         * it meant to say something -- and silently dropping it would be a warning nobody
         * gets, which is the one failure mode this whole list exists against.
         */
        fun of(key: String): Caution = entries.firstOrNull { it.key == key }
            ?: error("'$key' is not a trigger this build knows")
    }
}
