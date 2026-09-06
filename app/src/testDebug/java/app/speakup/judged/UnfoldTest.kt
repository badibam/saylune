package app.speakup.judged

import app.speakup.sheets.Sheets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unfolding a judged marking onto the words it covers.
 *
 * The judge marks by **group of words**, once for both scales; the app spreads each group over
 * its words and counts. The fineness does not get lost, it changes place: it comes from the
 * counting -- how many words at which notch, over what length of sentence.
 */
class UnfoldTest {

    private val correctness = Sheets.columnOf("correctness/correctness")
    private val relevance = Sheets.columnOf("relevance/relevance")
    private val stumbling = Sheets.columnOf("fluency/stumbling")

    private fun unfoldWith(text: String, spans: List<Marked>, column: app.speakup.sheets.Reading.Column) =
        unfold(text, spans, column.notches.map { it.name }, column.fallback!!)

    @Test fun `a span unfolds into the words it covers`() {
        val text = "I go there yesterday"
        val words = unfoldWith(text, listOf(Marked(0, 4, "malformed")), correctness)
        assertEquals(listOf("malformed", "malformed", "ok", "ok"), words.map { it.notch })
        assertEquals(0..0, words[0].at)
        assertEquals(2..3, words[1].at)
    }

    @Test fun `a word carrying nothing is ok, not an absence`() {
        val words = unfoldWith("It was nice", emptyList(), correctness)
        assertEquals(listOf("ok", "ok", "ok"), words.map { it.notch })
    }

    @Test fun `two overlapping spans give a word one notch, the worst it carries`() {
        val text = "I go there yesterday"
        val words = unfoldWith(
            text,
            listOf(Marked(0, 10, "malformed"), Marked(2, 4, "not-said")),
            correctness,
        )
        // `ne se dit pas` before `mal formé` before `ok`, and `go` counts once.
        assertEquals(listOf("malformed", "not-said", "malformed", "ok"), words.map { it.notch })
    }

    @Test fun `on relevance the precedence runs from a-cote down to juste`() {
        val text = "crack on with it"
        val words = unfoldWith(
            text,
            listOf(Marked(0, 8, "apt"), Marked(0, 16, "off-target")),
            relevance,
        )
        // Idiomatic but out of tone: the exclusion files it under `à côté`, which is the
        // right verdict. The price is owned -- a group that is both vague and too familiar
        // shows as `à côté` and never as flatness.
        assertTrue(words.all { it.notch == "off-target" })
    }

    @Test fun `an unmarked spoken word is kept`() {
        val text = "It was like um I went"
        val words = unfoldWith(text, listOf(Marked(7, 14, "filler")), stumbling)
        assertEquals(
            listOf("kept", "kept", "filler", "filler", "kept", "kept"),
            words.map { it.notch },
        )
    }

    @Test fun `bounds that miss a word boundary fail outright`() {
        val text = "I go there yesterday"
        // Rounding would put the mark somewhere plausible, and nothing downstream could tell
        // it apart from a mark the judge meant.
        assertTrue(runCatching {
            unfoldWith(text, listOf(Marked(1, 4, "malformed")), correctness)
        }.isFailure)
        assertTrue(runCatching {
            unfoldWith(text, listOf(Marked(0, 3, "malformed")), correctness)
        }.isFailure)
    }

    @Test fun `a notch the catalogue does not declare fails outright`() {
        assertTrue(runCatching {
            unfoldWith("I go there", listOf(Marked(0, 4, "bancal")), correctness)
        }.isFailure)
    }

    @Test fun `punctuation travels with the word it touches`() {
        val text = "Yeah, I went there last summer."
        assertEquals(listOf("Yeah,", "I", "went", "there", "last", "summer."),
                     words(text).map { text.substring(it) })
        val marked = unfoldWith(text, listOf(Marked(0, 5, "flat")), relevance)
        assertEquals("flat", marked.first().notch)
    }

    @Test fun `the figure is the mean of the words the sheet reads`() {
        // *Yeah, I went there last summer. It was nice* -- nine words, nothing off, one
        // flat: (8 x 0,90 + 0,50) / 9 = 0,86, which is the doc's own worked case.
        val text = "Yeah, I went there last summer. It was nice"
        val words = unfoldWith(text, listOf(Marked(39, 43, "flat")), relevance)
        val value = words.map { word -> relevance.notches.first { it.name == word.notch }.value }
        assertEquals(0.86f, value.average().toFloat(), 0.005f)
    }
}
