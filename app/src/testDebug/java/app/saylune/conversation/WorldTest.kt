package app.saylune.conversation

import app.saylune.judged.Word
import app.saylune.notes.Measured
import app.saylune.notes.Passage as Scored
import app.saylune.notes.Weights
import app.saylune.rules.Trigger
import app.saylune.sheets.Sheets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the rules read of a sitting.
 *
 * What is proved is the **shape of each reading**, never a number: the series and the columns
 * of the catalogue are a first pass the calibration bench takes over, so a test pinning one
 * would only say the bench had not run yet. The values here are material.
 */
class WorldTest {

    /** Every node weighed the same, which is what an author means by writing 1 across a tree. */
    private val weights = Weights(Sheets.all.mapNotNull { Sheets.scoredPathOf(it) }
        .flatMap { path -> path.split("/").runningReduce { a, b -> "$a/$b" } }
        .distinct().associateWith { 1f })

    private fun word(notch: String) = Word(0..0, notch)

    private fun sheetAt(path: String) = Sheets.of(path) as app.saylune.sheets.Sheet

    private fun world(
        measured: List<Measured> = emptyList(),
        material: Material = Material(),
        passage: Int = 0,
        clocks: Map<Trigger.Clock.Which, Int> = emptyMap(),
    ) = World(
        passage = passage,
        measured = measured,
        scored = Scored(keptWords = 10, difficulty = null, measured = measured),
        weights = weights,
        sensitivity = { 2 },
        material = material,
        clocks = clocks,
    )

    // ── The elements ────────────────────────────────────────────────────────────────────

    /**
     * **An element carrying a notch leaves nothing to choose**: at least one word at it, and no
     * threshold. What the value means follows from the nature of the element, which the sheet
     * says already.
     */
    @Test
    fun `a notch element reads at least one word at that notch`() {
        val marked = Material(correctness = listOf(word("ok"), word("malformed")))
        assertTrue(marked.any("correctness/correctness", "malformed"))
        assertFalse(marked.any("correctness/correctness", "not-said"))
    }

    /** **An element carrying a quantity takes a threshold**: a silence of more than so long. */
    @Test
    fun `a quantity element reads a threshold`() {
        val heard = Material(blanks = listOf(0.4f, 5.2f, 1.1f))
        assertTrue(heard.any("fluency/longest-silence", "5"))
        assertFalse(heard.any("fluency/longest-silence", "6"))
    }

    /**
     * **No denominator, and that is deliberate.** This is the one place in the project where an
     * absolute fact is read: one word off target fires the same on a passage of three words and
     * on one of thirty. Whoever wants the proportion reads the figure.
     */
    @Test
    fun `an element reading has no denominator`() {
        val short = Material(relevance = listOf(word("off-target"), word("ok")))
        val long = Material(relevance = List(28) { word("ok") } + word("off-target"))
        assertTrue(short.any("relevance/relevance", "off-target"))
        assertTrue(long.any("relevance/relevance", "off-target"))
    }

    /**
     * **A path whose elements nothing reads fails outright.** A challenge is written against the
     * catalogue, so naming a sheet with no element reader is a writing mistake -- and answering
     * *no* would hide it behind a rule that never fires.
     */
    @Test
    fun `an element reading nothing holds fails rather than answering no`() {
        assertThrows(IllegalStateException::class.java) {
            Material().any("fluency/pace", "20")
        }
    }

    // ── The three readings of a node ────────────────────────────────────────────────────

    /** A sheet's own figure, in its own unit, read straight off what the attempt measured. */
    @Test
    fun `a figure reading compares in the sheet's own unit`() {
        val at = world(listOf(Measured(sheetAt("fluency/longest-silence"), 3.2f)))
        assertTrue(at.node(node("fluency/longest-silence", Trigger.Node.Reads.Figure, "3")))
        assertFalse(at.node(node("fluency/longest-silence", Trigger.Node.Reads.Figure, "4")))
    }

    /**
     * **A note is read in both directions**: fallen to D, or reached A. Reading only the failing
     * half is what made a reward unwritable, when everything else for one was already there.
     */
    @Test
    fun `a note reading names a letter and reads both halves`() {
        val top = world(listOf(Measured(sheetAt("correctness/correctness"), 1f)))
        assertTrue(top.node(node("correctness", Trigger.Node.Reads.Note, "A")))
        val bottom = world(listOf(Measured(sheetAt("correctness/correctness"), 0f)))
        assertTrue(bottom.node(node("correctness", Trigger.Node.Reads.Note, "E")))
        assertFalse(bottom.node(node("correctness", Trigger.Node.Reads.Note, "A")))
    }

    /**
     * **A node with nothing measured under it has no note**, and a trigger on it does not fire:
     * an absent sheet leaves the sum, it never counts as a zero.
     */
    @Test
    fun `a node nothing measured says nothing`() {
        assertFalse(world().node(node("pronunciation", Trigger.Node.Reads.Note, "E")))
    }

    // ── The clocks ──────────────────────────────────────────────────────────────────────

    /** The two clocks, which are the whole of what a rule of the recording moment can read. */
    @Test
    fun `the clocks answer what the recorder has run`() {
        val at = world(clocks = mapOf(Trigger.Clock.Which.Silence to 4_200))
        assertEquals(4_200, at.clock(Trigger.Clock.Which.Silence))
        // Not a clock that is running: zero, which is where a turn that has not started is.
        assertEquals(0, at.clock(Trigger.Clock.Which.TurnLength))
    }

    private fun node(path: String, reads: Trigger.Node.Reads, value: String) =
        Trigger.Node(path, reads, value, app.saylune.rules.Moment.EndOfAttempt)
}
