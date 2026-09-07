package app.saylune.conversation

import app.saylune.analysis.AnalysedSound
import app.saylune.judged.Word
import app.saylune.notes.Letter
import app.saylune.notes.Measured
import app.saylune.notes.Passage as Scored
import app.saylune.notes.Weights
import app.saylune.notes.noteOver
import app.saylune.rules.Facts
import app.saylune.rules.Rule
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets
import kotlin.random.Random

/**
 * What the rules read of the sitting, at one moment.
 *
 * **Read afresh at each wave** -- the engine asks it again every time, because what the wave
 * before made true is what opens the next. Nothing is cached here for that reason: every answer
 * is worked out from the material handed in, which does not change inside a moment.
 *
 * **A condition never reads anything but the tree.** A fact carried by the turn -- its capture
 * position, how it ended -- is not readable as such: it takes the sheet that reads it, and that
 * is what lets a challenge be written against the catalogue rather than against the code.
 */
class World(
    /** How many passages have closed. */
    override val passage: Int,
    /** What this attempt made of each sheet, or empty outside the two reading moments. */
    private val measured: List<Measured> = emptyList(),
    /** What the note is weighed by. Null where nothing has been measured yet. */
    private val scored: Scored? = null,
    /** What the sitting looks at. Null on a sitting whose definition declared no tree. */
    private val weights: Weights? = null,
    private val sensitivity: (Sheet) -> Int,
    /** What the elements of a sheet are made of, on this attempt. */
    private val material: Material = Material(),
    /** The two clocks, while recording. Empty at the other two moments, where none runs. */
    private val clocks: Map<app.saylune.rules.Trigger.Clock.Which, Int> = emptyMap(),
    private val draw: Random = Random.Default,
) : Facts {

    override fun clock(which: app.saylune.rules.Trigger.Clock.Which): Int = clocks[which] ?: 0

    /**
     * Whether a node says what the trigger says it says.
     *
     * The three readings live together because what each one means is the sheet's business:
     * a **note** is a letter, a **figure** is in the sheet's own unit, an **element** is a fact
     * about one of the things it reads.
     */
    override fun node(of: app.saylune.rules.Trigger.Node): Boolean =
        when (of.reads) {
            app.saylune.rules.Trigger.Node.Reads.Note -> note(of.path)?.let {
                // **In both directions**: fallen to D, or reached A. Reading only the failing
                // half is what made a reward unwritable, when everything else for one was
                // already there -- a patch moves a lever both ways, an ending carries *passed*.
                it.letter == Letter.valueOf(of.value)
            } ?: false
            app.saylune.rules.Trigger.Node.Reads.Figure -> figure(of.path)?.let {
                it >= of.value.toFloat()
            } ?: false
            // **No denominator, and that is deliberate**: this is the one place in the project
            // where an absolute fact is read. *At least one word off target* fires the same on
            // a passage of three words and on one of thirty; whoever wants the proportion reads
            // the figure.
            app.saylune.rules.Trigger.Node.Reads.Element -> material.any(of.path, of.value)
        }

    /**
     * What the model answered to a piece of free prose.
     *
     * **It has no channel yet**: the contract carries neither the yes/no field nor the menu
     * key, so a rule written on this kind of trigger never fires. False rather than a guess --
     * a trigger that fired on nothing read would put a scene's beat where nobody asked for one
     * (`../../../../../../TODO.md`).
     */
    override fun judged(prose: String): Boolean = false

    /**
     * Which pack a draw or the model took.
     *
     * A draw really draws, and what it drew goes to the journal: without it the effective state
     * of a sitting no longer recomputes, and the sitting stops comparing to itself. The model
     * has no channel, so it falls to the first pack, as above.
     */
    override fun chose(rule: Rule): Int = when (rule.decider) {
        app.saylune.rules.Decider.Chance -> draw.nextInt(rule.choice.size)
        else -> 0
    }

    /** The note of a node, over the sheets under it. Null where nothing under it measured. */
    private fun note(path: String): app.saylune.notes.Note? {
        val weights = weights ?: return null
        val scored = scored ?: return null
        val mine = measured.filter { Sheets.pathOf(it.sheet).let { at ->
            at == path || at.startsWith("$path/")
        } }
        if (mine.isEmpty()) return null
        return noteOver(listOf(scored.copy(measured = mine)), weights, sensitivity)
    }

    /** A sheet's own figure, in its own unit. Null on a branch, which has none. */
    private fun figure(path: String): Float? =
        measured.firstOrNull { Sheets.pathOf(it.sheet) == path }?.figure
}

/**
 * What the elements of a sheet are made of, on the attempt being read.
 *
 * Handed in rather than derived here, because it is the very material the sheets were computed
 * from: deriving it a second time would be a second arithmetic to hold in step with the first.
 */
data class Material(
    /** The three markings unfolded onto the words, or empty where nothing judged the turn. */
    val correctness: List<Word> = emptyList(),
    val relevance: List<Word> = emptyList(),
    val stumbling: List<Word> = emptyList(),
    /** The sounds, or empty where the analysis did not run. */
    val sounds: List<AnalysedSound> = emptyList(),
    /** Every silence of the turn, in seconds. */
    val blanks: List<Float> = emptyList(),
) {

    /**
     * Whether at least one element of the sheet at [path] says [value].
     *
     * **What the value means follows from the nature of the element**, and there is nothing more
     * to declare, the sheet saying it already: an element carrying a **notch** leaves nothing to
     * choose -- *at least one word `not-said`*, with no threshold -- and one carrying a
     * **quantity** takes one -- *a silence of more than five seconds*.
     *
     * A path whose elements nothing here holds **fails outright**. A challenge is written
     * against the catalogue, so naming a sheet whose elements have no reader is a writing
     * mistake, and answering *no* to it would hide the mistake behind a rule that never fires.
     */
    fun any(path: String, value: String): Boolean = when (path) {
        "correctness/correctness" -> correctness.any { it.notch == value }
        "relevance/relevance" -> relevance.any { it.notch == value }
        "fluency/stumbling" -> stumbling.any { it.notch == value }
        // The sounds are the elements of both pronunciation sheets, and the quantity is the
        // gap in points -- the same number the ramp on screen saturates at.
        "pronunciation/intelligibility", "pronunciation/proximity" ->
            sounds.any { it.points >= value.toFloat() }
        "fluency/longest-silence" -> blanks.any { it >= value.toFloat() }
        else -> error("$path: a condition on elements nothing reads")
    }
}
