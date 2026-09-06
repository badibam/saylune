package app.speakup.judged

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeptTest {

    // "It was, like, um, I went to the— I was going to the store"
    private val stumbled = "It was like um I went to the I was going to the store"

    private fun marked(from: Int, to: Int, notch: String) = Marked(from, to, notch)

    /** A clean turn: the model says all of it, and nothing has to be carried across. */
    @Test
    fun `a turn with no hesitation keeps its whole text and its own offsets`() {
        val text = "I am twenty five years old"
        val kept = Kept.of(text, emptyList())
        assertTrue(kept.entire)
        assertEquals(text, kept.text)
        text.indices.forEach { assertEquals(it, kept.inWhole(it)) }
    }

    /** The model is given a sentence, not a list of words: kept neighbours are one stretch. */
    @Test
    fun `consecutive kept words make one stretch and not several`() {
        val text = "I am twenty five years old"
        assertEquals(1, Kept.of(text, emptyList()).ranges.size)
    }

    @Test
    fun `the model is never made to say the filler`() {
        val text = "I am [um] twenty five"
        val kept = Kept.of(text, listOf(marked(5, 9, "filler")))
        assertEquals("I am twenty five", kept.text)
    }

    @Test
    fun `an abandoned start is left out too`() {
        val kept = Kept.of(stumbled, listOf(
            marked(7, 11, "filler"),   // like
            marked(12, 14, "filler"),  // um
            marked(15, 28, "abandoned"),    // I went to the
        ))
        assertEquals("It was I was going to the store", kept.text)
    }

    /**
     * The property the whole class exists for: every mark the model's sounds come back with
     * has to land on the letter it belongs to in the string the screen shows.
     */
    @Test
    fun `every offset of the model text lands on its own letter in the whole turn`() {
        val kept = Kept.of(stumbled, listOf(
            marked(7, 11, "filler"),
            marked(12, 14, "filler"),
            marked(15, 28, "abandoned"),
        ))
        kept.text.indices.forEach { at ->
            val there = kept.inWhole(at)
            if (!kept.text[at].isWhitespace()) {
                assertEquals(
                    "offset $at ('${kept.text[at]}')",
                    kept.text[at],
                    stumbled[there],
                )
            }
        }
    }

    /** A span carries both its ends, so a mark on a word stays on that word. */
    @Test
    fun `a span of the model text keeps its word in the whole turn`() {
        val kept = Kept.of(stumbled, listOf(
            marked(7, 11, "filler"),
            marked(12, 14, "filler"),
            marked(15, 28, "abandoned"),
        ))
        val store = kept.text.indexOf("store")
        val carried = kept.inWhole(store..(store + 4))
        assertEquals("store", stumbled.substring(carried.first, carried.last + 1))
    }

    /** Offsets never run backwards, whatever the turn is made of. */
    @Test
    fun `carrying an offset across is monotone`() {
        val kept = Kept.of(stumbled, listOf(
            marked(7, 11, "filler"),
            marked(12, 14, "filler"),
            marked(15, 28, "abandoned"),
        ))
        val carried = kept.text.indices.map { kept.inWhole(it) }
        carried.zipWithNext().forEach { (a, b) -> assertTrue("$a then $b", b >= a) }
    }

    /** A turn made entirely of hesitation has nothing to synthesise, and says so plainly. */
    @Test
    fun `a turn with nothing kept leaves the model nothing to say`() {
        val text = "um like um"
        val kept = Kept.of(text, listOf(
            marked(0, 2, "filler"),
            marked(3, 7, "filler"),
            marked(8, 10, "filler"),
        ))
        assertTrue(kept.ranges.isEmpty())
        assertEquals("", kept.text)
    }
}
