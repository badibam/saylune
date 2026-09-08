package app.saylune.providers

import app.saylune.activity.Answers
import app.saylune.activity.Question
import app.saylune.activity.Rung
import app.saylune.activity.Text
import app.saylune.chain.ChainFailure
import app.saylune.chain.Reply
import app.saylune.rules.Trigger
import app.saylune.judged.Judgement
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
        reach: String = "\"extended\"",
        difficulty: String = "\"medium\"",
        extra: String = "",
    ) = """
        {"intended": "$intended", "spans": $spans, "stumbling": $stumbling,
         "following": $following, "reach": $reach, "spoken": "Ah, yesterday!",
         "difficulty": $difficulty$extra}
    """.trimIndent()

    /**
     * What was judged, on a path where there is a judgement.
     *
     * It is null only on a provoked turn, which has nothing to judge; every case below sends
     * a learner turn in, so a null here would be the reader breaking its own contract.
     */
    private val Reply.marked: Judgement get() = judged ?: error("nothing was judged")

    @Test fun `a turn nobody prompted comes back with nothing judged`() {
        val reply = ReplyReader.read("""{"spoken": "Ah, there you are."}""", "", provoked = true)
        assertEquals("Ah, there you are.", reply.spoken)
        // Not "nothing was marked" -- there was nothing to mark, nobody having spoken.
        assertNull(reply.judged)
        assertNull(reply.echo)
    }

    // ── The questions the app puts ──────────────────────────────────────────────────────

    private fun question(
        key: String = "mood",
        answers: Answers = Answers.Free,
        rung: Rung = Rung.FromTheTalk,
    ) = Question(key, "How did his talk go?", answers,
                 moments = listOf(Trigger.Opening), rung = rung)

    private val yesNo = Answers.OneOf(listOf(
        Text(mapOf("en" to "yes")), Text(mapOf("en" to "no")),
    ))

    @Test fun `an answer to a question the app put comes back under its key`() {
        val reply = ReplyReader.read(
            answer(extra = ""","established": {"mood": "His room was half empty."}"""),
            "i go there yesterday",
            asking = listOf(question()),
        )
        assertEquals(mapOf("mood" to "His room was half empty."), reply.established)
    }

    /**
     * **The presence of an answer is checked and its content is not.** That is the whole of
     * what serving the question buys: the app chose the moment, so a missing answer is the
     * contract broken and not a model that had nothing to say.
     */
    @Test fun `a question put and not answered is the contract broken`() {
        assertTrue(runCatching {
            ReplyReader.read(answer(), "x", asking = listOf(question()))
        }.exceptionOrNull() is ChainFailure)
    }

    /** A closed shape is checked by membership, on the English that went out. */
    @Test fun `an answer outside the options offered is refused`() {
        assertTrue(runCatching {
            ReplyReader.read(
                answer(extra = ""","established": {"safe": "maybe"}"""),
                "x", asking = listOf(question("safe", yesNo)),
            )
        }.exceptionOrNull() is ChainFailure)
    }

    /**
     * *It does not know* is a member at the first rung and at that one alone -- said rather
     * than left out, a missing field being indistinguishable from a model that forgot.
     */
    @Test fun `it does not know is an option at the first rung and nowhere else`() {
        val said = ""","established": {"safe": "${Question.DONT_KNOW}"}"""
        assertEquals(
            mapOf("safe" to Question.DONT_KNOW),
            ReplyReader.read(answer(extra = said), "x",
                             asking = listOf(question("safe", yesNo))).established,
        )
        assertTrue(runCatching {
            ReplyReader.read(answer(extra = said), "x",
                             asking = listOf(question("safe", yesNo, Rung.MayInvent)))
        }.exceptionOrNull() is ChainFailure)
    }

    @Test fun `a clean answer reads back whole`() {
        val reply = ReplyReader.read(answer(), "i go there yesterday")
        assertEquals("Ah, yesterday!", reply.spoken)
        assertEquals("I go there yesterday", reply.marked.intended)
        assertEquals("precise", reply.marked.following)
        assertEquals("medium", reply.marked.difficulty)
        // Absent is the ordinary answer for both, and means "there is none".
        assertNull(reply.echo)
        assertNull(reply.choice)
    }

    @Test fun `a span unfolds onto the words it names`() {
        val reply = ReplyReader.read(
            answer(spans = """[{"from":0,"to":4,"correctness":"malformed","relevance":"ok"}]"""),
            "i go there yesterday",
        )
        assertEquals(listOf("malformed", "malformed", "ok", "ok"),
                     reply.marked.words().correctness.map { it.notch })
        // The two scales cover the same words, so one span carries both notches.
        assertTrue(reply.marked.words().relevance.all { it.notch == "ok" })
    }

    @Test fun `the kept words are what the stumbling leaves alone`() {
        val reply = ReplyReader.read(
            answer(intended = "It was um nice",
                   stumbling = """[{"from":7,"to":9,"notch":"filler"}]"""),
            "it was um nice",
        )
        val words = reply.marked.words()
        assertEquals(listOf("kept", "kept", "filler", "kept"),
                     words.stumbling.map { it.notch })
        assertEquals(listOf(0..1, 3..5, 10..13), words.kept)
    }

    @Test fun `intended falls back to the transcript, and nothing else does`() {
        // The one documented fallback: the analysis then measures against exactly what was
        // heard, which is harmless.
        val reply = ReplyReader.read(answer(intended = ""), "i go there yesterday")
        assertEquals("i go there yesterday", reply.marked.intended)

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

    @Test fun `bounds that cut a word in half are a failure at the seam`() {
        // Read downstream, a mark sliding inside a word would be indistinguishable from a
        // mark the judge meant. The place to say so is the seam that read it.
        assertTrue(runCatching {
            ReplyReader.read(
                answer(spans = """[{"from":0,"to":3,"correctness":"malformed","relevance":"ok"}]"""),
                "i go there yesterday",
            )
        }.isFailure)
    }

    @Test fun `a bound beside the word it names is settled onto it`() {
        // Measured on the device: the model stops before the full stop, or runs on to the
        // next word. Neither names a different set of words, so the seam settles the bound
        // instead of throwing the whole turn away over one character.
        val reply = ReplyReader.read(
            answer(intended = "I go there yesterday.",
                   spans = """[{"from":0,"to":5,"correctness":"malformed","relevance":"ok"}]"""),
            "i go there yesterday",
        )
        assertEquals(listOf("malformed", "malformed", "ok", "ok"),
                     reply.marked.words().correctness.map { it.notch })
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
