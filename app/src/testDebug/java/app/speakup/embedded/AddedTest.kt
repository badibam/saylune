package app.speakup.embedded

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Where a mark for added matter lands, held on the turn that found the rule wrong.
 *
 * Built by hand rather than read off a matrix: the rule is about the arithmetic above the
 * join, and a fixture would drag the weights in to test something they have nothing to do
 * with. The numbers below are the ones a real turn produced -- `I'm trying how to learn
 * english` said against `I'm trying to learn English.`, pulled off the device.
 *
 * The two cases below are the two halves of the rule, and they pull in opposite directions:
 * between two words the mark goes forward to the word's end, inside one word it stays put.
 * Getting either one alone right is easy; a change that breaks the other is the whole risk.
 */
class AddedTest {

    private val text = "I'm trying to learn English."
    private val trying = 4..9
    private val to = 11..12

    /** Only [Overlap.Gap.rank] is read here -- the rest is what the mark never consults. */
    private fun gap(rank: Int) = Overlap.Gap(
        value = 0f, symbol = "", rank = rank, at = 0 until 0, span = 0 until 0,
        model = emptyList(), said = emptyList(),
    )

    /** A sound of a join: what it took of the text, and the word it sat in. */
    private fun sound(symbol: String, spots: List<Int>, word: IntRange?) =
        Sound(symbol, word?.let { text.substring(it) }, word, "", spots, emptyList())

    /**
     * The learner said `how` in the middle of the sentence, so both its sounds are loose and
     * the mark belongs after the whole of `trying` -- not between the `n` and the `g`, which
     * would draw it before the sound that spells `ng`.
     */
    @Test
    fun `a stretch between two words anchors at the end of the word before it`() {
        val said = listOf(
            sound("ŋ", listOf(8, 9), trying),
            sound("h", emptyList(), trying),
            sound("aʊ", emptyList(), to),
            sound("t", listOf(11), to),
        )
        val model = listOf(sound("ŋ", listOf(8, 9), trying))

        val added = Added.found(said = said, sounds = model, gaps = listOf(gap(0)))
        assertEquals(1, added.size)
        // One mark for the whole of it: an added word is one thing that happened.
        assertEquals("h aʊ", added[0].symbol)
        assertEquals("the seam sits after `trying`, not inside `ng`", 9, added[0].after)
        assertEquals("it follows the sound that spells `ng`", 0, added[0].afterSound)
    }

    /**
     * A sound loose in the **middle** of a word stays on the last letter claimed.
     *
     * `Join.trimmed` empties a sound after the walk when the letters it was given turn out to
     * be worth nothing on it, and one that can borrow none from a neighbour is loose inside
     * its word -- measured on four of the bench's 95 renders. Pushing that mark to the end of
     * the word would put it after letters that were said before it.
     */
    @Test
    fun `a stretch inside a word stays on the last letter claimed`() {
        val said = listOf(
            sound("t", listOf(4), trying),
            sound("ɹ", listOf(5), trying),
            sound("ə", emptyList(), trying),
            sound("ŋ", listOf(8, 9), trying),
        )
        val model = listOf(sound("ɹ", listOf(5), trying))

        val added = Added.found(said = said, sounds = model, gaps = listOf(gap(0)))
        assertEquals(1, added.size)
        assertEquals("it may not pass the letters said after it", 5, added[0].after)
    }

    /** Before every letter of the turn there is no word to end: the seam is the very start. */
    @Test
    fun `a stretch before the first placed sound anchors before the text`() {
        val said = listOf(
            sound("b", emptyList(), null),
            sound("aɪ", listOf(0), 0..2),
        )
        val added = Added.found(said, emptyList(), emptyList())
        assertEquals(1, added.size)
        assertEquals(-1, added[0].after)
        assertEquals(-1, added[0].afterSound)
    }
}
