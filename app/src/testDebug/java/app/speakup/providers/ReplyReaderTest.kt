package app.speakup.providers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The enriched contract, read back and checked at the seam.
 *
 * Nothing here falls back to a plausible value: a field the contract requires and the model
 * left out is the model breaking its contract, which is a failure like any other.
 */
class ReplyReaderTest {

    private fun answer(
        intended: String = "I go there yesterday",
        spans: String = "[]",
        stumbling: String = "[]",
        following: String = "\"precise\"",
        difficulty: String = "\"medium\"",
        extra: String = "",
    ) = """
        {"intended": "$intended", "spans": $spans, "stumbling": $stumbling,
         "following": $following, "spoken": "Ah, yesterday!", "difficulty": $difficulty$extra}
    """.trimIndent()

    @Test fun `a clean answer reads back whole`() {
        val reply = ReplyReader.read(answer(), "i go there yesterday")
        assertEquals("Ah, yesterday!", reply.spoken)
        assertEquals("I go there yesterday", reply.judged.intended)
        assertEquals("precise", reply.judged.following)
        assertEquals("medium", reply.judged.difficulty)
        // Absent is the ordinary answer for all three, and means "there is none".
        assertNull(reply.echo)
        assertNull(reply.choice)
        assertNull(reply.title)
    }

    @Test fun `a span unfolds onto the words it names`() {
        val reply = ReplyReader.read(
            answer(spans = """[{"from":0,"to":4,"correctness":"malformed","relevance":"ok"}]"""),
            "i go there yesterday",
        )
        assertEquals(listOf("malformed", "malformed", "ok", "ok"),
                     reply.judged.words().correctness.map { it.notch })
        // The two scales cover the same words, so one span carries both notches.
        assertTrue(reply.judged.words().relevance.all { it.notch == "ok" })
    }

    @Test fun `the kept words are what the stumbling leaves alone`() {
        val reply = ReplyReader.read(
            answer(intended = "It was um nice",
                   stumbling = """[{"from":7,"to":9,"notch":"filler"}]"""),
            "it was um nice",
        )
        val words = reply.judged.words()
        assertEquals(listOf("kept", "kept", "filler", "kept"),
                     words.stumbling.map { it.notch })
        assertEquals(listOf(0..1, 3..5, 10..13), words.kept)
    }

    @Test fun `intended falls back to the transcript, and nothing else does`() {
        // The one documented fallback: the analysis then measures against exactly what was
        // heard, which is harmless.
        val reply = ReplyReader.read(answer(intended = ""), "i go there yesterday")
        assertEquals("i go there yesterday", reply.judged.intended)

        // A missing notch is the contract broken, and it says so.
        assertTrue(runCatching {
            ReplyReader.read(answer(following = "\"\""), "x")
        }.isFailure)
        assertTrue(runCatching {
            ReplyReader.read(answer(difficulty = "\"\""), "x")
        }.isFailure)
    }

    @Test fun `a notch the catalogue does not declare is a failure`() {
        assertTrue(runCatching {
            ReplyReader.read(answer(following = "\"bof\""), "x")
        }.isFailure)
        assertTrue(runCatching {
            ReplyReader.read(answer(difficulty = "\"impossible\""), "x")
        }.isFailure)
    }

    @Test fun `bounds that miss a word boundary are a failure at the seam`() {
        // Read downstream, a mark sliding inside a word would be indistinguishable from a
        // mark the judge meant. The place to say so is the seam that read it.
        assertTrue(runCatching {
            ReplyReader.read(
                answer(spans = """[{"from":1,"to":4,"correctness":"malformed","relevance":"ok"}]"""),
                "i go there yesterday",
            )
        }.isFailure)
    }

    @Test fun `an echo comes back only when something was marked`() {
        val reply = ReplyReader.read(
            answer(spans = """[{"from":0,"to":4,"correctness":"malformed","relevance":"ok"}]""",
                   extra = ""","echo": "Ah, you went there yesterday!""""),
            "i go there yesterday",
        )
        assertEquals("Ah, you went there yesterday!", reply.echo)
    }
}
