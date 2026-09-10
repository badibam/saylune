package app.saylune.providers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the one who judges sends back, checked against the `intended` it was given.
 *
 * **The turn it marks is handed to it**, so nothing here falls back to a transcript: the
 * string every offset counts into came out of the other call, and a bound that misses its
 * words is a broken answer whether or not it looks plausible.
 */
class VerdictReaderTest {

    private fun marked(
        spans: String = "[]",
        stumbling: String = "[]",
        following: String = "\"precise\"",
        reach: String = "\"extended\"",
        difficulty: String = "\"medium\"",
        extra: String = "",
    ) = """
        {"spans": $spans, "stumbling": $stumbling, "following": $following,
         "reach": $reach, "difficulty": $difficulty$extra}
    """.trimIndent()

    @Test fun `a clean verdict reads back whole`() {
        val verdict = VerdictReader.read(marked(), "I go there yesterday")
        assertEquals("I go there yesterday", verdict.judgement.intended)
        assertEquals("precise", verdict.judgement.following)
        assertEquals("extended", verdict.judgement.reach)
        assertEquals("medium", verdict.judgement.difficulty)
        // No menu was offered, so no key was picked.
        assertNull(verdict.choice)
    }

    @Test fun `a span unfolds onto the words it names`() {
        val verdict = VerdictReader.read(
            marked(spans = """[{"from":0,"to":4,"correctness":"malformed","relevance":"ok"}]"""),
            "I go there yesterday",
        )
        assertEquals(listOf("malformed", "malformed", "ok", "ok"),
                     verdict.judgement.words().correctness.map { it.notch })
        // The two scales cover the same words, so one span carries both notches.
        assertTrue(verdict.judgement.words().relevance.all { it.notch == "ok" })
    }

    @Test fun `the kept words are what the stumbling leaves alone`() {
        val verdict = VerdictReader.read(
            marked(stumbling = """[{"from":7,"to":9,"notch":"filler"}]"""),
            "It was um nice",
        )
        val words = verdict.judgement.words()
        assertEquals(listOf("kept", "kept", "filler", "kept"), words.stumbling.map { it.notch })
        assertEquals(listOf(0..1, 3..5, 10..13), words.kept)
    }

    @Test fun `a notch left out is the contract broken`() {
        assertTrue(runCatching {
            VerdictReader.read(marked(following = "\"\""), "I go there yesterday")
        }.isFailure)
        assertTrue(runCatching {
            VerdictReader.read(marked(difficulty = "\"\""), "I go there yesterday")
        }.isFailure)
    }

    @Test fun `a notch the catalogue does not declare is a failure`() {
        assertTrue(runCatching {
            VerdictReader.read(marked(following = "\"bof\""), "I go there yesterday")
        }.isFailure)
        assertTrue(runCatching {
            VerdictReader.read(marked(difficulty = "\"impossible\""), "I go there yesterday")
        }.isFailure)
    }

    @Test fun `bounds that cut a word in half are a failure at the seam`() {
        // Read downstream, a mark sliding inside a word would be indistinguishable from a
        // mark the judge meant. The place to say so is the seam that read it.
        assertTrue(runCatching {
            VerdictReader.read(
                marked(spans = """[{"from":0,"to":3,"correctness":"malformed","relevance":"ok"}]"""),
                "I go there yesterday",
            )
        }.isFailure)
    }

    @Test fun `a bound beside the word it names is settled onto it`() {
        // Measured on the device: the model stops before the full stop, or runs on to the
        // next word. Neither names a different set of words, so the seam settles the bound
        // instead of throwing the whole turn away over one character.
        val verdict = VerdictReader.read(
            marked(spans = """[{"from":0,"to":5,"correctness":"malformed","relevance":"ok"}]"""),
            "I go there yesterday.",
        )
        assertEquals(listOf("malformed", "malformed", "ok", "ok"),
                     verdict.judgement.words().correctness.map { it.notch })
    }
}
