package app.speakup.analysis

import app.speakup.fluency.Fluency
import app.speakup.judged.Marked
import app.speakup.marking.TurnMarking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Where the two recordings put each word, read off one analysed turn.
 *
 * The turns here are built by hand rather than analysed: this is the composition and not the
 * engine, so it needs no weights. What it stands on is the one property the two montages were
 * chosen for -- the learner's own decoding covers everything he said, the model's grid covers
 * only what he kept, and a stumble's seconds must never end up inside a word he said cleanly.
 */
class TimedTest {

    /** "I am um twenty five", the `um` a filler the model was never given. */
    private val text = "I am um twenty five"

    private val filler = listOf(Marked(5, 7, "filler"))

    private fun word(at: IntRange) = at

    private fun heard(word: IntRange?, from: Int, to: Int) =
        Heard(symbol = "x", at = from..to, word = word)

    private fun sound(at: IntRange, from: Int, to: Int) = AnalysedSound(
        symbol = "x", points = 0f, letters = "", borrowed = false, at = at,
        model = emptyList(), said = emptyList(), modelMs = from..to, saidMs = from..to,
    )

    /**
     * The learner said the filler; the model never did. Both sides are read where they are
     * actually written -- the said times off his own decoding, the model's off its own grid.
     */
    private fun analysed() = Analysed(
        marking = TurnMarking(text = text, syllables = emptyList(), phonemes = emptyList(),
                              words = emptyList(), added = emptyList(), gutters = emptyList()),
        dropped = 0,
        recorded = 2600,
        rendered = 1400,
        sounds = listOf(
            sound(word(0..0), 0, 200),        // I
            sound(word(2..3), 200, 500),      // am
            sound(word(8..13), 500, 1000),    // twenty
            sound(word(15..18), 1000, 1400),  // five
        ),
        added = emptyList(),
        freely = listOf(
            heard(word(0..0), 100, 300),
            heard(word(2..3), 300, 600),
            heard(word(5..6), 600, 1400),     // um, eight hundred milliseconds of it
            heard(word(8..13), 1500, 2000),
            heard(word(15..18), 2000, 2400),
        ),
    )

    @Test
    fun `every word the mouth said is there, and only the kept ones face a model`() {
        val turn = analysed().timed(filler)
        assertEquals(listOf("kept", "kept", "filler", "kept", "kept"),
                     turn.spoken.map { it.notch })
        assertEquals(4, turn.kept.size)
        assertNull(turn.spoken.first { it.notch == "filler" }.model)
    }

    /**
     * The property the whole choice of montage rests on. The filler lasts 800 ms; if its
     * audio were read off the forced alignment it would be swallowed by whichever kept word
     * sits beside it, and that word would look slow on a sheet that exists to say so.
     */
    @Test
    fun `a stumble's seconds land in the stumble and in no word said cleanly`() {
        val turn = analysed().timed(filler)
        val um = turn.spoken.first { it.notch == "filler" }
        assertEquals(800, um.said.length)
        turn.kept.forEach {
            assertTrue("${it.said} overlaps the filler", it.said.to <= um.said.from ||
                it.said.from >= um.said.to)
        }
    }

    /**
     * And the sheet that exists to be unmoved by it: the speaking time of the kept words is
     * what it would have been without the stumble, so the rate reads the mouth and not the
     * hesitation.
     */
    @Test
    fun `the rate reads the kept words alone`() {
        val turn = analysed().timed(filler)
        val said = turn.kept.sumOf { it.said.length }
        val model = turn.kept.mapNotNull { it.model }.sumOf { it.length }
        assertEquals(1400, said)
        assertEquals(1400, model)
        assertEquals(0f, Fluency.rate(turn)!!, 0.001f)
    }

    /**
     * A hesitation is speech and never a blank. Were its audio missing from the turn, the
     * eight hundred milliseconds it lasted would read as a silence, and the learner would be
     * marked for a pause he did not take -- on top of the sheet that already counts the
     * stumble. The one blank here is the two hundred milliseconds after the last word, which
     * counts on purpose: handing the floor back is an act.
     */
    @Test
    fun `a hesitation is speech and no blank opens where it was`() {
        val turn = analysed().timed(filler)
        assertEquals(listOf(0.2f), Fluency.blanks(turn))
    }

    /** A turn nothing judged has every word kept, which is what a silent judge leaves. */
    @Test
    fun `a turn no judge marked keeps all of its words`() {
        val turn = analysed().timed(emptyList())
        assertEquals(5, turn.kept.size)
    }
}
