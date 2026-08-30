package app.speakup.marking

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The one property a readout laid out on the text owes: nothing of the text is missing.
 *
 * A soft rule -- "show the silent letters too" -- lets a character disappear without anyone
 * noticing, which is exactly the defect this exists against: a reader who cannot find a line
 * in the phrase cannot use the line.
 */
class ReadoutRowsTest {

    /** The payload is the range itself: what is under test is the laying out, not a sound. */
    private fun rows(text: String, owned: List<IntRange>) = readoutRows(text, owned) { it }

    private fun written(text: String, owned: List<IntRange>) =
        rows(text, owned).joinToString("") { row ->
            if (row.at.isEmpty()) "" else text.substring(row.at.first, row.at.last + 1)
        }

    @Test
    fun `every character of the text appears exactly once`() {
        val text = "He doesn't know"
        // `H`, `e`, `d`, `oe`, `s`, then `kn`, `ow`: the `n't` carries no sound at all.
        val owned = listOf(0..0, 1..1, 3..3, 4..5, 6..6, 11..12, 13..14)
        assertEquals(text, written(text, owned))
    }

    @Test
    fun `a sound owning nothing hides no text and adds none`() {
        val text = "He doesn't know"
        val owned = listOf(0..0, 1..1, 3..3, 4..5, 6..6, IntRange.EMPTY, 11..12, 13..14)
        assertEquals(text, written(text, owned))
        // It still gets a line, in the order it fell between the sounds.
        val letterless = rows(text, owned).map { it.at.isEmpty() && it.of != null }
        assertEquals(1, letterless.count { it })
        assertEquals(6, letterless.indexOfFirst { it })
    }

    @Test
    fun `text before the first sound and after the last is not dropped`() {
        val text = "  hi!"
        assertEquals(text, written(text, listOf(2..2, 3..3)))
    }

    @Test
    fun `a text no sound claims at all is still written out`() {
        val text = "mm"
        assertEquals(text, written(text, listOf(IntRange.EMPTY)))
        assertEquals(2, rows(text, listOf(IntRange.EMPTY)).size)
    }
}
