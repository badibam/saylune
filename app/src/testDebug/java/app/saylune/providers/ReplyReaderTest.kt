package app.saylune.providers

import app.saylune.activity.Answers
import app.saylune.activity.Question
import app.saylune.activity.Rung
import app.saylune.activity.Text
import app.saylune.chain.ChainFailure
import app.saylune.rules.Trigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the one who speaks sends back, read at the seam.
 *
 * Nothing here falls back to a plausible value: a field the contract requires and the model
 * left out is the model breaking its contract, which is a failure like any other. The one
 * exception is `intended`, and it is tested below.
 */
class ReplyReaderTest {

    private fun answer(intended: String = "I go there yesterday", extra: String = "") = """
        {"intended": "$intended", "spoken": "Ah, yesterday!"$extra}
    """.trimIndent()

    @Test fun `a turn nobody prompted comes back with nothing written out`() {
        val reply = ReplyReader.read("""{"spoken": "Ah, there you are."}""", "", provoked = true)
        assertEquals("Ah, there you are.", reply.spoken)
        // Not "nothing was said" -- there was nothing to write out, nobody having spoken.
        assertNull(reply.intended)
        assertNull(reply.echo)
    }

    @Test fun `a reply with nothing to say is the contract broken`() {
        assertTrue(runCatching {
            ReplyReader.read("""{"intended": "I go there yesterday"}""", "x")
        }.exceptionOrNull() is ChainFailure)
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

    // ── What comes back ─────────────────────────────────────────────────────────────────

    @Test fun `a clean answer reads back whole`() {
        val reply = ReplyReader.read(answer(), "i go there yesterday")
        assertEquals("Ah, yesterday!", reply.spoken)
        assertEquals("I go there yesterday", reply.intended)
        // Absent is the ordinary answer, and means "there is none".
        assertNull(reply.echo)
    }

    @Test fun `intended falls back to the transcript, and it is the only thing that does`() {
        // The one documented fallback: the analysis then measures against exactly what was
        // heard, which is harmless.
        val reply = ReplyReader.read(answer(intended = ""), "i go there yesterday")
        assertEquals("i go there yesterday", reply.intended)
    }

    /**
     * The echo is the opening of the reply, and the one who speaks decides there is one.
     *
     * It used to be tied to a span it had marked a moment earlier; with the marking gone to
     * the other call, what makes it appear is the slip itself. What keeps the two from
     * drifting apart is on the judge's side, which is shown this line.
     */
    @Test fun `an echo comes back as the opening of the reply`() {
        val reply = ReplyReader.read(
            answer(extra = ""","echo": "Ah, you went there yesterday!""""),
            "i go there yesterday",
        )
        assertEquals("Ah, you went there yesterday!", reply.echo)
        assertEquals("Ah, yesterday!", reply.spoken)
    }
}
