package app.speakup.conversation

import app.speakup.levers.Count
import app.speakup.levers.Levers
import app.speakup.levers.Positions

/**
 * An utterance and all of its attempts. **The unit the note is read on.**
 *
 * The word is new because *turn* already names a turn of speech -- one recording, one
 * utterance, one capture position -- and confusing the two would be paid for at the first
 * commit. It is also why *the turn does not pass* means nothing here: what passes or does not
 * is a passage.
 *
 * **Derived from the run and never stored.** What ties an attempt to what it repeats is
 * already on the utterance, so a passage is a walk of the run -- and a walk of the run cannot
 * fall out of step with the run, which is exactly what a table beside it would do.
 */
data class Passage(
    /** The utterance that opened it, which is the one every attempt points back at. */
    val opener: Utterance,
    /** Every attempt at it, in order, the opener first. */
    val attempts: List<Utterance>,
) {

    /**
     * **The note of the passage is the last attempt's**, and that for every sheet without
     * exception.
     *
     * The other three readings are worse. The **first** makes saying it again pointless. The
     * **best** lets stubbornness reach A. The **mean of the attempts** lowers the note at every
     * try, which punishes exactly the gesture the app exists to provoke. A consequence to tell
     * the learner rather than let them find out: one attempt too many, after a good one, can
     * bring the note down.
     *
     * A proposal used to sit here -- that fluency and relevance read the **first**, a repeated
     * sentence no longer being spontaneous speech. The argument is right and the price is
     * accepted: two reading rules instead of one, on a doc that keeps saying a note is not read
     * without its combination, cost more than they earn. And nothing is frozen by the choice:
     * every attempt stays under the passage, so which one a sheet reads is a **calculation**
     * and never a storage.
     */
    val last: Utterance get() = attempts.last()

    /** How many times the words were changed. */
    val rewordings: Int get() = attempts.count { it.attempt == Attempt.Rewording }

    /** How many times the same words were said again. */
    val repeats: Int get() = attempts.count { it.attempt == Attempt.Repeat }

    /**
     * Whether another attempt of [kind] is allowed.
     *
     * **Two counters, one per kind of repair, and they do not steal from each other**: a
     * challenge aiming only at pronunciation keeps its repeats intact whatever happens on the
     * words' side. Both are levers that may be zero, and either may be *no maximum*.
     */
    fun spare(kind: Attempt, settings: Positions): Boolean {
        val allowed = (settings.of(kind.lever) as? Count)?.n ?: return true
        return when (kind) {
            Attempt.Rewording -> rewordings
            Attempt.Repeat -> repeats
        } < allowed
    }

    companion object {

        /**
         * Every passage of [run], oldest first.
         *
         * An utterance of the learner that repeats nothing opens one; the ones that point back
         * at it are its attempts. The AI's turns belong to no passage: what is scored is what
         * the learner said.
         */
        fun of(run: List<Utterance>): List<Passage> = run
            .filter { it.speaker.isLearner && it.repeats == null }
            .map { opener ->
                Passage(opener, listOf(opener) + run.filter { it.repeats == opener.id })
            }
    }
}

/**
 * Which of the two repairs an utterance is, or null when it opens its passage.
 *
 * **They are not two names for one gesture.** A **rewording** gives a sentence that is not the
 * same, so the exchange is remade on it; a **repeat** says the same words, so there is nothing
 * new to answer and it stays what it was -- an exercise, which improves the passage's note and
 * gives the sound analysis a turn it would never have had on a malformed one.
 */
enum class Attempt(val lever: String) {
    Rewording("rewordings-allowed"),
    Repeat("retakes-allowed"),
}

/**
 * Where a passage stands. **Four, and there is no fifth.**
 *
 * *Failed* is not one of them: that is a word about an activity's outcome, beside *passed* and
 * *the note decides*, and using it here mixes two planes. What is said of a passage is
 * **unrepaired**, and that is not a stored state either -- it follows from the note of the node
 * read and the count of attempts, both already there.
 */
sealed interface Standing {

    /** Nothing to redo. The big button is available and closing it repairs it. */
    object Open : Standing

    /** The words have to change. What repairs it is a rewording. */
    object ToReword : Standing

    /** The words do not change and the way of saying them is taken again. A repeat repairs it. */
    object ToSayAgain : Standing

    /**
     * Closed, one of two ways.
     *
     * **Repaired**, the last attempt clearing the bar, or **unrepaired**, the attempts having
     * run out before it did. Which is why *"waits" does not guarantee the repair, it guarantees
     * the attempts get spent*: a passage to reword can close without ever having been reworded.
     */
    data class Closed(val repaired: Boolean) : Standing
}
