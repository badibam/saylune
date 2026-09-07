package app.speakup.marking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one file two writers fill, held to its shape.
 *
 * `bench/turn.py` writes it from the bench's own takes and the app writes it beside every turn
 * it analysed, so that a turn recorded on the phone is read on the prototype screen exactly like
 * one of the bench's. Nothing kept the two in step: the reader came to require a channel the
 * phone's files did not carry, and the only way to find out was to push one and watch the screen
 * refuse it. **A real file of each kind is in the repo now**, and a field that moves on either
 * side fails here instead.
 *
 * The fixtures are kept for their **shape** and not their numbers -- what is asserted is that the
 * phrase, the marks and the channels come through, never what any of them is worth.
 */
class TurnFileTest {

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "no $name" }
            .use { it.readBytes().decodeToString() }

    /** A turn the bench measured: every channel it fills, read as the screen reads it. */
    @Test
    fun `a turn written by the bench reads`() {
        val turn = TurnFile.parse(fixture("turn-from-bench.json"))
        assertEquals("I think you are right", turn.marking.text)
        assertEquals("01-sink", turn.take)
        assertEquals("think", turn.model)
        assertTrue(turn.marking.phonemes.isNotEmpty())
        assertTrue(turn.marking.words.isNotEmpty())
        assertTrue(turn.marking.added.isNotEmpty())
        // Every mark falls inside the phrase it is laid on, which is the only thing that makes
        // a channel drawable at all.
        turn.marking.phonemes.forEach {
            assertTrue(it.start >= 0 && it.end <= turn.marking.text.length)
        }
    }

    /**
     * A turn the phone kept, pushed back.
     *
     * This one is older than the melody channel, so it carries no syllables at all -- which is
     * the ordinary case for a file kept for months, and what the contract calls an absent
     * channel rather than a broken file.
     */
    @Test
    fun `a turn kept by the app reads, and an absent channel is an empty one`() {
        val turn = TurnFile.parse(fixture("turn-from-phone.json"))
        assertEquals("I'm trying to learn English.", turn.marking.text)
        assertTrue(turn.marking.phonemes.isNotEmpty())
        assertEquals(emptyList<Syllable>(), turn.marking.syllables)
    }

    /** What is not optional: without the phrase and its marks there is no turn to draw. */
    @Test
    fun `a file with no phrase or no phonemes fails rather than drawing nothing`() {
        assertThrows(Exception::class.java) { TurnFile.parse("""{"phonemes": []}""") }
        assertThrows(Exception::class.java) { TurnFile.parse("""{"text": "hi"}""") }
    }
}
