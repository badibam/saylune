package app.saylune.embedded

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The guard on the reductions table: fill an empty sound, never widen a spelt one.
 *
 * `PortTest` holds the whole port against frozen turns of the bench, and it cannot reach
 * this: no phrase of the bench carries a `'m`, and `fixture.py` freezes a bench take against
 * a bench phrase, so the case the reductions table was written for cannot be frozen at all.
 * Rather than leave the newest rule as the one nothing runs, the two sentences are built
 * here from the sound sequence the network actually rendered for them.
 *
 * The sounds are typed out rather than decoded, which is the point: this tests the join and
 * not the engine, so it needs no weights and runs on any machine.
 */
class ReducedTest {

    private val affinity = Affinity.read(
        File("src/debug/assets/affinity.json"),
        File("src/debug/assets/affinity-groups.json"),
        File("src/debug/assets/affinity-reductions.json"),
    )

    private fun letters(text: String, symbols: List<String>) =
        Join.joined(symbols, text, affinity).map { it.symbol to it.letters }

    /**
     * A contraction said at speed, as the model renders it on the device: the vowel reduced
     * and the `m` assimilated to the `t` that follows, so the word produces `ɑ n` and not one
     * of `I`, `'`, `m` writes either. Before the reductions the whole word came back blank --
     * red as a word verdict, and unmarkable letter by letter.
     */
    @Test
    fun `a reduction fills a word no letter of which could be marked`() {
        val read = letters(
            "I'm trying to learn English.",
            listOf("ɑ", "n", "t", "ɹ", "aɪ", "ŋ", "t", "ə", "l", "ɝ", "n",
                   "ɪ", "ŋ", "g", "l", "ɪ", "ʃ"),
        )
        assertEquals("ɑ" to "I", read[0])
        assertEquals("n" to "'m", read[1])
    }

    /**
     * And the other half of the rule, which is what it costs to get the first half wrong.
     * `t → n` is a real reduction -- `want to` said `wɑnə` -- but `went` spells its own /n/
     * and the `t` beside it is silent, the following /t/ belonging to `to`. Unguarded, the
     * reduction made that `t` worth something on the /n/, the trim no longer dropped it, and
     * the mark for `went` spread over `nt`. Measured on the hand annotation, this and the
     * same fault in `important` were two of the four sounds an unguarded table cost.
     */
    @Test
    fun `a reduction never widens a sound the spelling already pays`() {
        val read = letters(
            "Yesterday I went to the market",
            listOf("j", "ɛ", "s", "t", "ɝ", "ɾ", "eɪ", "aɪ", "w", "ɛ", "n",
                   "t", "ɪ", "ð", "ə", "m", "ɑ", "ɹ", "k", "ɪ"),
        )
        assertEquals("n" to "n", read[10])
        assertEquals(
            listOf("Y", "e", "s", "t", "er", "d", "ay", "I", "w", "e", "n",
                   "t", "o", "th", "e", "m", "a", "r", "k", "e"),
            read.map { it.second },
        )
    }
}
