package app.saylune.conversation

import app.saylune.levers.At
import app.saylune.levers.Positions
import app.saylune.notes.Measured
import app.saylune.notes.Passage as Scored
import app.saylune.notes.Weights
import app.saylune.notes.noteOver
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets

/**
 * The two gates, and what cuts the sound analysis.
 *
 * **A closed gate is a failure and an open one lets through.** Closing declares the passage to
 * be done again -- reworded for the words, said again for the sound. Opening declares nothing.
 * The sense reads backwards one time in two, so it is written here: **what closes is what does
 * not pass.**
 *
 * **There is one gate per kind of repair, and they are read at two different moments.** The
 * **words' gate** is read when the call returns and says the passage is *to reword*; the
 * **sound's gate** is read when the analysis has finished and says it is *to say again*.
 *
 * **What files each aptitude on one side or the other is already settled: do the words
 * change.** An activity chooses **which aptitudes it sends back**, never which side that falls
 * on -- the side is a fact about the sentence, and reopening that choice would make it possible
 * to have a sentence said again that is about to be rewritten, which is what the gate exists to
 * prevent.
 */
object Gates {

    /** The aptitudes whose failure changes the words. */
    val WORDS = listOf("correctness", "relevance", "understanding")

    /** The aptitudes whose failure leaves the sentence as it is. */
    val SOUND = listOf("pronunciation", "fluency")

    /**
     * The notch of `not-said`, whose presence is **an absence of ground and not a
     * decision**: a phrase that does not exist in the language cannot be synthesised, and
     * making the model say a non-phrase would give a non-phrase to imitate.
     */
    const val UNSAYABLE = "ne-se-dit-pas"

    /**
     * Why the words' gate closes, or null where it lets through.
     *
     * When it closes on notes, it names **every** aptitude that says so
     * and never the worst: several may say it at once, and they are not competing criteria --
     * register and grammar are two ways for the words to change. Naming only one would be an
     * election, which the project does nowhere.
     *
     * **Two facts close it without reading any note.** A **truncated turn**: there is no
     * complete sentence, and letting it through would mean having a fragment said again with no
     * right to finish it. And a **turn with no kept word**: there is no sentence at all,
     * correctness and relevance have no elements, so there is not even a note to read. Both are
     * an **absence of matter on the words' side**, so neither is negotiable, and neither is
     * ever a verdict of correctness -- none of it is marked as a fault of language.
     */
    fun words(
        measured: List<Measured>,
        passage: Scored,
        settings: Positions,
        weights: Weights,
        sensitivity: (Sheet) -> Int,
        truncated: Boolean,
        keptWords: Int,
    ): Closing? {
        if (truncated) return Closing.Truncated
        if (keptWords == 0) return Closing.NothingKept
        val said = WORDS.filter {
            sendsBack(it, settings) && under(it, measured, passage, weights, sensitivity)
        }
        return if (said.isEmpty()) null else Closing.Aptitudes(said)
    }

    /**
     * Why the sound's gate closes, or null where it lets through.
     *
     * Read at the end of the analysis, so only ever on a passage whose words' gate let through:
     * a sentence about to be rewritten has no sound analysis at all.
     */
    fun sound(
        measured: List<Measured>,
        passage: Scored,
        settings: Positions,
        weights: Weights,
        sensitivity: (Sheet) -> Int,
    ): Closing? {
        val said = SOUND.filter {
            sendsBack(it, settings) && under(it, measured, passage, weights, sensitivity)
        }
        return if (said.isEmpty()) null else Closing.Aptitudes(said)
    }

    /**
     * Whether the sound analysis runs at all.
     *
     * **Not a third gate: a consequence of the first two, plus one case.** The words' gate
     * having closed is the original reason -- one does not work on the pronunciation of a
     * sentence about to be rewritten, it is going to disappear -- and it carries an ordering
     * that falls out on its own: the words' gate is read **before** the analysis runs, so when
     * it closes the sound's gate never gets the chance to speak.
     *
     * The second is [UNSAYABLE], and it is not a decision but **an absence of ground**. Where
     * the words' gate is settable -- an activity chooses which aptitudes it sends back -- this
     * one is not negotiable.
     *
     * **`malformed` is not in this case**, and putting it there was tried: one grammar fault in
     * a thirty-word turn would kill the whole sound analysis, and a learner makes one nearly
     * every turn, so pronunciation would almost never be measured. It closes by the
     * **correctness note**, like everything else.
     */
    fun soundAnalysisRuns(wordsClosed: Boolean, unsayable: Boolean): Boolean =
        !wordsClosed && !unsayable

    /**
     * Whether [aptitude] sends the passage back at all.
     *
     * **Only one is at *yes* in a free conversation**: correctness.
     */
    private fun sendsBack(aptitude: String, settings: Positions): Boolean =
        (settings.of("$aptitude.sends-back") as? At)?.name == "yes"

    /**
     * Whether the note of [aptitude] falls under the bar.
     *
     * **Both gates read a note at the A-B bar**, and never a sheet a definition names. Nothing
     * is lost by that: a challenge aiming only at melody puts a weight on melody and zero on
     * the rest of its branch, and *elocution under the bar* **becomes** *melody under the bar*.
     * Writing the same aiming twice, once in the tree and once in the gate, is what that avoids.
     *
     * A node with nothing measured under it has no note, and **no note lets through**: a gate
     * is a failure to declare, and there is nothing here to declare a failure about.
     */
    private fun under(
        aptitude: String,
        measured: List<Measured>,
        passage: Scored,
        weights: Weights,
        sensitivity: (Sheet) -> Int,
    ): Boolean {
        val mine = measured.filter { Sheets.pathOf(it.sheet).startsWith("$aptitude/") }
        if (mine.isEmpty()) return false
        val note = noteOver(listOf(passage.copy(measured = mine)), weights, sensitivity)
        return note != null && !note.passes
    }

}

/**
 * Why a gate closed, or null where it let through.
 *
 * Three cases and not a list of names with a blank in it: the two facts close the words' gate
 * **without reading any note**, so there is no aptitude to hand back, and a screen given an
 * empty name would have to invent one. What the learner is owed differs too -- the words' gate
 * **names the aptitudes in cause**, and an absence of matter names none because none of them
 * said anything.
 */
sealed interface Closing {

    /** One or more aptitudes fell under the bar. **All of them, and never the worst.** */
    data class Aptitudes(val names: List<String>) : Closing

    /**
     * The turn was cut off by a clock. There is no complete sentence, and letting it through
     * would mean having a fragment said again with no right to finish it.
     */
    object Truncated : Closing

    /**
     * Not one word of the turn was kept. There is no sentence at all, so correctness and
     * relevance have no elements and there is not even a note to read.
     */
    object NothingKept : Closing
}
