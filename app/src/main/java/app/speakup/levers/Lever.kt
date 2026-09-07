package app.speakup.levers

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * What a lever is, and what one has to declare to be one.
 *
 * **A lever is a setting of the sitting, and it measures nothing** (`activity.md`). What
 * makes something a lever is two things together: **declared, closed positions**, and **a hard
 * end**. Without the positions there is no phrase to show and no value to check; without the
 * hard end a notification cannot say *this tightens* or *this eases*.
 *
 * The test turns two things away that reflex would file here. **A voice is not one**: between
 * two voices there is no harder and no easier, and its positions come from a provider's
 * catalogue rather than from a declaration. **A register is not one either**: familiar,
 * plain, formal is ordered, and no end of it is the hard one -- it is the middle that is
 * easy. Those live elsewhere, and the place they live is said where one would have gone
 * looking for the lever.
 *
 * Nothing here says which aptitude a lever belongs to. Grouping by aptitude is presentation,
 * for the custom screen and for the doc; the sections below are a reading convenience and not
 * a field.
 */
sealed interface Lever {

    /** How a definition, a patch and the store all name it. */
    val key: String

    /** Who makes it true. */
    val held: Held

    /** What has to hold for it to be read at all, or null when it always is. */
    val needs: Requirement?
}

/**
 * Who makes a lever true.
 *
 * Three things follow from this, and none is a detail. A condition **cannot read** a lever the
 * model holds, there being no fact to read. The bench **cannot test one**. And its
 * notification **announces something that may not happen** -- *"it will speak denser"*, and it
 * speaks the same. It is shown anyway: saying nothing would leave the learner with nothing to
 * understand what was attempted, and it tells the truth to whoever writes a challenge --
 * raising complexity is a request, not a guarantee.
 */
enum class Held {
    /** The app does it, so it is true. Lives, the silence threshold, capture, the turn's length. */
    App,
    /** The app writes it into the prompt and nothing checks it was honoured. */
    Model,
}

/** Which end of a numeric lever is the hard one. Its order is arithmetic and not settable. */
enum class HardSide { Low, High }

/**
 * When a lever has no object: [key] must sit on one of [positions], or this one is not read.
 *
 * **No object does not mean absent.** The value stays on the line, it is not read, and it is
 * there if capture goes back up to the third position. It shows, greyed, carrying [because] --
 * the project's rule for any option that is off.
 *
 * [because] says why in the learner's language, and it is the app's own words rather than a
 * provider's, so it is a resource and not a sentence.
 */
data class Requirement(
    val key: String,
    val positions: Set<String>,
    @StringRes val because: Int,
)

/**
 * One step of a stepped lever: how it is stored, what it says, and what it asks for.
 *
 * [tells] is the instruction written into the prompt when the lever is [Held.Model] -- the
 * *requested* levers exist as nothing else. It stays in code and in English: it is addressed
 * to the model and never to the learner, so it is not a resource and is never translated.
 *
 * Null where there is nothing to write: on every [Held.App] lever, which the app simply does,
 * and on a model-held position whose channel does not exist yet.
 */
data class Step(val name: String, @StringRes val says: Int, val tells: String? = null)

/**
 * A lever with a closed list of named positions, each with its phrase written in advance.
 *
 * **The steps are declared easiest first and hardest last**, which is why the hard end is not
 * a field here: it is the last one. Aids read backwards from intuition for that reason --
 * for [Levers.ECHO] and [Levers.EXPLANATION] it is the absence that is the hard notch, and
 * they are still declared easy to hard like everyone else.
 */
data class Stepped(
    override val key: String,
    val steps: List<Step>,
    /** What the app does when nobody has asked for anything. */
    val fallback: String,
    override val held: Held,
    override val needs: Requirement? = null,
) : Lever {

    init {
        require(steps.isNotEmpty()) { "$key: a stepped lever with no step" }
        require(steps.any { it.name == fallback }) { "$key: default '$fallback' is not a step" }
    }

    fun rank(name: String): Int {
        val at = steps.indexOfFirst { it.name == name }
        require(at >= 0) { "$key: '$name' is not one of its positions" }
        return at
    }
}

/**
 * A lever with a unit, a floor, a ceiling and a step, plus a phrase with a hole in it.
 *
 * **Not a degraded form of [Stepped].** Forcing numbers into named steps costs straight away:
 * "lives: few / normal / many" forbids a challenge from asking for four, and lies to a screen
 * where the learner sees three hearts and not the word *normal*. Nothing is lost in exchange
 * -- a number is ordered on its own, so direction is worked out the same way in both forms,
 * and the menu sent to the model does not change, since it picks between ready-made patches
 * and never a value.
 *
 * [max] is null for **no maximum**, which listens and attempts take willingly. A very large
 * number in its place would lie to the screen exactly as the named step did.
 */
data class Numeric(
    override val key: String,
    /** The phrase with a hole. A plural because "1 life" and "3 lives" are not one string. */
    @PluralsRes val says: Int,
    val min: Int,
    val max: Int?,
    val step: Int,
    val fallback: Count,
    val hard: HardSide,
    override val held: Held,
    /** What zero says when it means something of its own -- *from memory*, *no replay*. */
    @StringRes val saysZero: Int? = null,
    /** What the sentinel says. Required exactly when [max] is null. */
    @StringRes val saysUnbounded: Int? = null,
    override val needs: Requirement? = null,
) : Lever {

    init {
        require(min <= (max ?: Int.MAX_VALUE)) { "$key: floor above ceiling" }
        require(step > 0) { "$key: a step of $step" }
        require((max == null) == (saysUnbounded != null)) {
            "$key: no maximum and no phrase for it, or a phrase for a maximum that exists"
        }
        require(holds(fallback)) { "$key: default $fallback is out of its own bounds" }
    }

    fun holds(count: Count): Boolean = when (count.n) {
        null -> max == null
        else -> count.n >= min && count.n <= (max ?: Int.MAX_VALUE)
    }

    /** [count] brought back inside the bounds. A move stops at the bound; it does not fail. */
    fun clamp(count: Count): Count = when {
        count.n == null -> if (max == null) count else Count(max)
        else -> Count(count.n.coerceIn(min, max ?: count.n))
    }
}

/**
 * Where a lever sits.
 *
 * Stored by name and never by rank, for the reason the store already gives about enums: a rank
 * is a promise never to reorder a list, which nobody remembers making, and inserting a
 * position would silently reread every stored sitting as the position after it.
 */
sealed interface Position {

    /** A named step of a [Stepped] lever. */
    data class At(val name: String) : Position

    /** A number, or **no maximum** when [n] is null. */
    data class Count(val n: Int?) : Position
}

typealias At = Position.At
typealias Count = Position.Count

/** Which way a change went, for the notification to announce. */
enum class Direction { Harder, Easier }

/**
 * A change that actually happened: which lever, from where to where, and which way.
 *
 * **What moved nothing returns null rather than one of these**, because announcing *this
 * tightens* with nothing changed is a lie. And a move that stopped at the bound is not an
 * error -- a ramp reaches its top notch by design.
 */
data class Move(
    val lever: Lever,
    val from: Position,
    val to: Position,
    val direction: Direction,
)
