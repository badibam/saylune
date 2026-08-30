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
 */
class AddedTest {

    private val text = "I'm trying to learn English."

    /** Only [Overlap.Gap.rank] is read here -- the rest is what the mark never consults. */
    private fun gap(rank: Int) = Overlap.Gap(
        value = 0f, symbol = "", rank = rank, at = 0 until 0, span = 0 until 0,
        model = emptyList(), said = emptyList(),
    )

    /** A sound of the learner's join: what it took of the text, and the word it sat in. */
    private fun sound(symbol: String, spots: List<Int>, word: IntRange?) =
        Sound(symbol, word?.let { text.substring(it) }, word, "", spots, emptyList())

    /**
     * The learner said `how` inside the sentence. The join paid the `g` of `trying` for the
     * `aʊ` of `how`, so the `ŋ` was left holding the `n` alone and the loose `h` fell between
     * the two letters of `ng` -- inside a word, and written before the sound that spells it.
     */
    @Test
    fun `a loose stretch anchors to the end of the word, not inside it`() {
        val trying = 4..9
        val said = listOf(
            sound("aɪ", listOf(0), 0..2),
            sound("m", listOf(2), 0..2),
            sound("t", listOf(4), trying),
            sound("ɹ", listOf(5), trying),
            sound("eɪ", listOf(6), trying),
            sound("i", listOf(7), trying),
            sound("ŋ", listOf(8), trying),
            sound("h", emptyList(), trying),
            sound("aʊ", listOf(9), trying),
        )
        // The model's own join, where `ŋ` spells the whole of `ng`.
        val model = listOf(
            sound("t", listOf(4), trying),
            sound("ɹ", listOf(5), trying),
            sound("aɪ", listOf(6, 7), trying),
            sound("ŋ", listOf(8, 9), trying),
        )
        val gaps = model.indices.map { gap(it) }

        val added = Added.found(said = said, sounds = model, gaps = gaps)
        assertEquals(1, added.size)
        assertEquals("h", added[0].symbol)
        assertEquals("the seam sits after `trying`, not inside `ng`", 9, added[0].after)
        assertEquals("it follows the sound that spells `ng`", 3, added[0].afterSound)
    }

    /** A whole word said in addition is one thing that happened, not one mark per sound. */
    @Test
    fun `neighbouring loose sounds make a single mark`() {
        val trying = 4..9
        val said = listOf(
            sound("ŋ", listOf(8, 9), trying),
            sound("h", emptyList(), null),
            sound("aʊ", emptyList(), null),
            sound("t", listOf(11), 11..12),
        )
        val model = listOf(sound("ŋ", listOf(8, 9), trying))
        val added = Added.found(said, model, listOf(gap(0)))
        assertEquals(1, added.size)
        assertEquals("h aʊ", added[0].symbol)
        assertEquals(9, added[0].after)
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
