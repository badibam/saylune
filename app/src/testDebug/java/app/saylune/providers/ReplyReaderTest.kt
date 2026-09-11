package app.saylune.providers

import app.saylune.chain.Asked
import app.saylune.chain.ChainFailure
import app.saylune.chain.Said
import app.saylune.scene.Kind
import app.saylune.scene.Reach
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
        {"intended": "$intended", "said": [$SPEECH]$extra}
    """.trimIndent()

    /** One ordinary speech, which is what nearly every turn is. */
    private val SPEECH = """{"kind": "speech", "who": "saylune", "text": "Ah, yesterday!"}"""

    @Test fun `a turn nobody prompted comes back with nothing written out`() {
        val reply = ReplyReader.read(
            """{"said": [{"kind": "speech", "who": "saylune", "text": "Ah, there you are."}]}""",
            "", provoked = true,
        )
        assertEquals("Ah, there you are.", reply.said.single().text)
        // Not "nothing was said" -- there was nothing to write out, nobody having spoken.
        assertNull(reply.intended)
        assertNull(reply.echo)
    }

    @Test fun `a reply with nothing to say is the contract broken`() {
        assertTrue(runCatching {
            ReplyReader.read("""{"intended": "I go there yesterday"}""", "x")
        }.exceptionOrNull() is ChainFailure)
    }

    // ── The cases the app asks the leader for ───────────────────────────────────────────

    private fun asked(
        key: String = "mood",
        kind: Kind = Kind.Words,
        reach: Reach = Reach.Said,
    ) = Asked(key, "How his talk went", kind, reach)

    private val yesNo = Kind.Choice(listOf("yes", "no"))

    @Test fun `what the leader wrote comes back under the key it was asked for`() {
        val reply = ReplyReader.read(
            answer(extra = ""","established": {"mood": "His room was half empty."}"""),
            "i go there yesterday",
            asking = listOf(asked()),
        )
        assertEquals(mapOf("mood" to "His room was half empty."), reply.established)
    }

    /**
     * **The presence of an answer is checked and its content is not**, save that the case must
     * be able to hold it: the app chose the moment, so a missing answer is the contract broken
     * and not a model that had nothing to say.
     */
    @Test fun `a case asked for and not written is the contract broken`() {
        assertTrue(runCatching {
            ReplyReader.read(answer(), "x", asking = listOf(asked()))
        }.exceptionOrNull() is ChainFailure)
    }

    /** A list is checked by membership, on the values that went out. */
    @Test fun `a value the case cannot hold is refused`() {
        assertTrue(runCatching {
            ReplyReader.read(
                answer(extra = ""","established": {"safe": "maybe"}"""),
                "x", asking = listOf(asked("safe", yesNo)),
            )
        }.exceptionOrNull() is ChainFailure)
    }

    /** A number outside its bounds is one the case cannot hold either. */
    @Test fun `a number outside the bounds of its case is refused`() {
        assertTrue(runCatching {
            ReplyReader.read(
                answer(extra = ""","established": {"glasses": "14"}"""),
                "x", asking = listOf(asked("glasses", Kind.Number(0.0, 10.0), Reach.Deduce)),
            )
        }.exceptionOrNull() is ChainFailure)
        assertEquals(
            mapOf("glasses" to "4"),
            ReplyReader.read(
                answer(extra = ""","established": {"glasses": "4"}"""),
                "x", asking = listOf(asked("glasses", Kind.Number(0.0, 10.0), Reach.Deduce)),
            ).established,
        )
    }

    /**
     * *It does not know* is a value at the first step of the reach and at that one alone --
     * said rather than left out, a missing field being indistinguishable from a model that
     * forgot -- and it leaves the case as it was.
     */
    @Test fun `it does not know is an answer at the first step and nowhere else`() {
        val said = ""","established": {"safe": "${Asked.DONT_KNOW}"}"""
        assertEquals(
            mapOf("safe" to Asked.DONT_KNOW),
            ReplyReader.read(answer(extra = said), "x",
                             asking = listOf(asked("safe", yesNo))).established,
        )
        assertTrue(runCatching {
            ReplyReader.read(answer(extra = said), "x",
                             asking = listOf(asked("safe", yesNo, Reach.Invent)))
        }.exceptionOrNull() is ChainFailure)
    }

    // ── What comes back ─────────────────────────────────────────────────────────────────

    @Test fun `a clean answer reads back whole`() {
        val reply = ReplyReader.read(answer(), "i go there yesterday")
        assertEquals("Ah, yesterday!", reply.said.single().text)
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
     * The echo is the opening of the reply, and the one who speaks decides there is one. What
     * keeps the two from drifting apart is on the judge's side, which is shown this line.
     */
    @Test fun `an echo comes back as the opening of the reply`() {
        val reply = ReplyReader.read(
            answer(extra = ""","echo": "Ah, you went there yesterday!""""),
            "i go there yesterday",
        )
        assertEquals("Ah, you went there yesterday!", reply.echo)
        assertEquals("Ah, yesterday!", reply.said.single().text)
    }

    // ── A turn is a run of utterances ───────────────────────────────────────────────────

    private fun run(vararg said: String) =
        ReplyReader.read("""{"intended": "x", "said": [${said.joinToString(",")}]}""", "x")

    private fun one(kind: String, who: String, text: String) =
        """{"kind": "$kind", "who": "$who", "text": "$text"}"""

    /**
     * **No order is imposed.** A stage direction may open the turn, two characters may speak
     * in it, and each utterance keeps its own kind and its own speaker.
     */
    @Test fun `a turn carries several utterances, each with its kind and its speaker`() {
        val said = run(
            one("stage", "narrator", "The train pulls in."),
            one("speech", "clerk", "Ticket, please."),
            one("speech", "porter", "Let them through."),
        ).said
        assertEquals(3, said.size)
        assertEquals(listOf(false, true, true), said.map { it.isSpeech })
        assertEquals(listOf("narrator", "clerk", "porter"), said.map { it.who })
    }

    /**
     * **The ceiling refuses rather than trims.** A run cut short is a scene missing its last
     * lines, said as though it were whole, with nothing on screen to say so; a refusal keeps
     * the recording and offers the button that sends it again.
     */
    @Test fun `more utterances than the ceiling allows is the contract broken`() {
        val many = (0..Said.CEILING).map { one("speech", "saylune", "line $it") }
        assertTrue(
            runCatching { run(*many.toTypedArray()) }.exceptionOrNull() is ChainFailure,
        )
        // And exactly the ceiling is fine, or the test above would prove nothing.
        assertEquals(Said.CEILING, run(*many.dropLast(1).toTypedArray()).said.size)
    }

    /**
     * A kind the contract does not name is refused rather than guessed: reading it as speech
     * would put a narrator's line in a character's mouth, which is the confusion the two
     * kinds exist to prevent.
     */
    @Test fun `an utterance of no known kind is the contract broken`() {
        assertTrue(runCatching {
            run(one("aside", "saylune", "Hm."))
        }.exceptionOrNull() is ChainFailure)
    }

    @Test fun `an utterance with no words is the contract broken`() {
        assertTrue(runCatching {
            run(one("speech", "saylune", ""))
        }.exceptionOrNull() is ChainFailure)
    }

    @Test fun `an utterance nobody said is the contract broken`() {
        assertTrue(runCatching {
            run(one("speech", "", "Hm."))
        }.exceptionOrNull() is ChainFailure)
    }
}
