package app.saylune.activity

import app.saylune.levers.Positions
import app.saylune.notes.Weights
import app.saylune.scene.Role
import app.saylune.scene.SceneFile
import java.util.UUID

/**
 * One piece of work: what it is, how it was set, where it got to, when, and how it went.
 *
 * **An activity is one object** (`docs/activity.md`), and **the conversation is one of these**,
 * in the same table and with the same fields. What it is called is its scene's business and
 * not a field here -- nothing in the app is named by the model. Its thread is the ordered run
 * of its utterances, so no field carries it.
 *
 * *Naming*: this shares a name with `android.app.Activity`, which one debug screen imports.
 * The word is the design's and is kept.
 */
data class Activity(
    /**
     * Where every lever of this sitting started, and **that is the whole of what it stores of
     * its settings**. Where they stand now is the scene's state, recomputed.
     */
    val settings: Positions = Positions(),
    /**
     * What this sitting looks at, fixed when it opened and moved by nothing: a closing note
     * that were a mean of measures taken under different weights would be unreadable.
     */
    val weights: Weights? = null,
    /**
     * Who speaks in this sitting besides the learner, with the gender each one settled.
     *
     * Copied onto the line: an utterance names its speaker by key, so a sitting that had to
     * ask a release for its cast could not be read without it.
     */
    val cast: List<Role> = emptyList(),
    /**
     * The scene file as it shipped, copied when the sitting opened.
     *
     * **A scene is a template applied at creation and never a dependency kept afterwards**, so
     * a release that changes or retires the file rewrites nothing about how this sitting is
     * played. Null on a sitting opened under the engine before this one: it stays readable and
     * does not carry on.
     */
    val scene: String? = null,
    /**
     * What was written into a case and is not found again: what the learner filled at the
     * launch, what chance drew. What the leader wrote lives on the turn that wrote it.
     */
    val journal: List<Written> = emptyList(),
    /**
     * Which scene this came from, and at which version. It only ever groups -- opening the
     * next level, filing a score -- and is never consulted to know how the sitting was set.
     */
    val origin: Origin? = null,
    /**
     * Which engine of scenes this sitting was played under. **A change of engine takes the
     * resumption away**: replaying under a semantics that moved would give another state
     * without saying so, which is worse than stopping.
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

    /** Whether this sitting can be carried on, or only read. */
    val resumable: Boolean get() = scene != null && engine == ENGINE

    /** What the learner typed into the holes at the launch, by case. */
    val filled: Map<String, String>
        get() = journal.filter { it.by == Writer.Learner }.associate { it.case to it.value }

    companion object {

        /** The engine of scenes this build plays by. Bumped when what a scene means changes. */
        const val ENGINE = 3

        /**
         * A sitting opened from [definition], which is **the only way one is made**.
         *
         * [answers] are what the learner typed into its holes, by case; a blank one is left out,
         * the case staying empty. [gender] is what he chose for the main character where the
         * file left it open, null being *no matter*, which draws.
         */
        fun from(
            definition: SceneFile,
            answers: Map<String, String> = emptyMap(),
            gender: String? = null,
            by: Prescriber = Prescriber.Learner,
            now: Long = System.currentTimeMillis(),
        ) = Activity(
            settings = definition.levers,
            weights = definition.weights,
            cast = definition.cast.map { if (it.main) settled(it, gender) else it },
            scene = definition.source,
            journal = answers.filterValues { it.isNotBlank() }
                .map { (case, text) -> Written(case, text.trim(), Writer.Learner, 0) },
            origin = Origin(definition.id, definition.version),
            status = Status.Running,
            createdAt = now,
            startedAt = now,
            by = by,
        )

        /**
         * The main character with its gender settled -- the file's, else the one chosen, else
         * one drawn -- and told to it in its description, which the learner never reads.
         */
        private fun settled(role: Role, chosen: String?): Role {
            val gender = role.gender ?: chosen ?: GENDERS.random()
            return role.copy(gender = gender, about = "${role.about} You are a $gender.")
        }

        /** What a gender can be, which is what the generic voices come in. */
        val GENDERS = listOf("man", "woman")
    }
}

/**
 * One value written into a case that nothing gives back: what the learner filled, or what
 * chance drew, and at which passage. **The value and nothing else**: what the events made of it
 * is recomputed, and storing it too would keep one thing twice.
 */
data class Written(val case: String, val value: String, val by: Writer, val passage: Int)

enum class Writer { Learner, Chance }

/**
 * Where an activity has got to. A suggestion is an activity that never started, and
 * [Dismissed] is a gesture that counts: without it a refused suggestion comes back.
 */
enum class Status { Suggested, Accepted, Dismissed, Running, Finished, Abandoned }

/** What fills an activity in. */
enum class Prescriber {
    /** The conversation, out of what has just happened in it. */
    Conversation,
    /** The learner, by choice or by a free instruction. */
    Learner,
    /** Progression, for what is due, never got right, or about to be forgotten. */
    Progression,
}

/**
 * How an activity went: an outcome, what settled it, and when.
 *
 * **Stored and never recomputed**, because it rests on a judgement nothing reproduces
 * identically.
 */
data class Outcome(
    /**
     * The issue that actually fell: passed, or failed. *By note* survives here only where
     * nothing measured and there was no note to resolve it with, which says so truthfully.
     */
    val verdict: String,
    /** What settled it -- an event, or the note. */
    val judge: String,
    val at: Long,
    /** A number, where the sitting produces one. Null everywhere else. */
    val score: Int? = null,
)

/** Which scene a sitting came from, and at which version. */
data class Origin(val definition: String, val version: String)

/**
 * The five aptitudes. They are independent: one can be intelligible and slow, correct and
 * poor, fluent and wrong.
 */
enum class Aptitude { Elocution, Understanding, Correctness, Fluency, Relevance }
