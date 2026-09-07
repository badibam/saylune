package app.saylune.ui

import app.saylune.judged.Word
import app.saylune.judged.words
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the marked turn's layout has to hold, proved without a device.
 *
 * This is what moving the layout out of the drawing bought: wrapping, where a group falls, and
 * where the curve breaks are arithmetic on columns, so they are properties one can state --
 * and the values in them are material, never what a test asserts.
 */
class TurnLayoutTest {

    private val text = "I think we should book a table"

    private fun tokens(vararg aside: String) = tokensOf(
        text,
        words(text).map { at ->
            Word(at, if (text.substring(at.first, at.last + 1) in aside) "filler" else "kept")
        },
    )

    // ── The tokens ──────────────────────────────────────────────────────────────────────

    /**
     * **What is set aside is written between brackets**, which are real characters in their own
     * cell -- that is what tells them from the painted enclosure of relevance.
     */
    @Test
    fun `a set-aside fragment is bracketed and a kept word is not`() {
        val laid = tokens("we")
        assertEquals("we", text.substring(laid[2].at.first, laid[2].at.last + 1))
        assertEquals("[we]", laid[2].text)
        assertTrue(laid[2].aside)
        assertEquals("think", laid[1].text)
        assertFalse(laid[1].aside)
    }

    // ── The wrapping ────────────────────────────────────────────────────────────────────

    /** No line runs past the columns it was given, one blank between two words included. */
    @Test
    fun `a line never runs past its columns`() {
        (6..40).forEach { columns ->
            wrap(tokens(), columns).forEach { line ->
                val end = line.last().after
                // A word wider than the line goes on a line of its own and overflows rather
                // than being cut: cutting it would put half a mark on each line.
                if (line.size > 1) assertTrue("$columns: $end", end <= columns)
            }
        }
    }

    /** Every word is laid out once, in the order it was said. */
    @Test
    fun `wrapping loses no word and reorders none`() {
        val all = wrap(tokens("we"), 12).flatten().map { it.token.at }
        assertEquals(words(text), all)
    }

    // ── The groups ──────────────────────────────────────────────────────────────────────

    /**
     * **A group cut by a line break opens on one side and closes on the other**, rather than
     * doubling into two complete enclosures.
     */
    @Test
    fun `a group split by a line break opens on one line and closes on the next`() {
        val span = words(text)[3].first..words(text)[5].last   // should book a
        val lines = wrap(tokens(), 20)
        val reaches = lines.mapNotNull { reachOf(it, span.first, span.last + 1) }
        assertEquals(2, reaches.size)
        assertTrue(reaches.first().opens)
        assertFalse(reaches.first().closes)
        assertFalse(reaches.last().opens)
        assertTrue(reaches.last().closes)
    }

    /**
     * **A group carries its identity, not its label.** Two neighbouring spans deserving the same
     * notch stay two groups, so they get two enclosures and not one running across both.
     */
    @Test
    fun `two neighbouring groups stay two`() {
        val line = wrap(tokens(), 60).single()
        val first = reachOf(line, words(text)[1].first, words(text)[1].last + 1)!!
        val second = reachOf(line, words(text)[2].first, words(text)[2].last + 1)!!
        assertTrue(first.opens && first.closes)
        assertTrue(second.opens && second.closes)
        assertTrue(second.column > first.column + first.width)
    }

    /** A group that falls on no part of a line reaches nothing there. */
    @Test
    fun `a group off the line reaches nothing`() {
        val lines = wrap(tokens(), 12)
        val last = words(text).last()
        assertNull(reachOf(lines.first(), last.first, last.last + 1))
    }

    // ── The melody's stretches ──────────────────────────────────────────────────────────

    /**
     * **The curve breaks only at a set-aside fragment**, where the comparison does not exist.
     * A word gap is a space, which is a character like any other, so it stays in one piece
     * across it -- and so does a pause, which takes no column of its own.
     */
    @Test
    fun `the melody is one stretch across a word gap and breaks at a fragment`() {
        val whole = wrap(tokens(), 60).single()
        assertEquals(1, voiced(whole).size)
        assertEquals(whole.last().after, voiced(whole).single().width)

        val broken = wrap(tokens("we"), 60).single()
        val stretches = voiced(broken)
        assertEquals(2, stretches.size)
        assertEquals(0, stretches.first().column)
        assertEquals(broken[1].after, stretches.first().width)
        assertEquals(broken[3].column, stretches.last().column)
    }
}
