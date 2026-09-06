package app.speakup.fluency

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FluencyTest {

    private fun kept(from: Int, to: Int, model: Span?) = Word(Span(from, to), model, "retenu")
    private fun filler(from: Int, to: Int) = Word(Span(from, to), null, "remplissage")

    /** Three words said cleanly, the model saying the same three at the same speed. */
    private fun even(): Turn = Turn(
        spoken = listOf(
            kept(1000, 1400, Span(0, 400)),
            kept(1400, 1800, Span(400, 800)),
            kept(1800, 2200, Span(800, 1200)),
        ),
        recorded = 3200,
        rendered = 1200,
    )

    // ── Nothing is counted twice ────────────────────────────────────────────────────────

    /**
     * The property the four sheets are arranged around. A silence inside a word is not a
     * pause and continuity does not see it -- but it lengthens the word, so rate does.
     */
    @Test
    fun `a silence inside a word is the rate's and never the continuity's`() {
        val tight = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400, 800, Span(400, 800))),
            recorded = 800, rendered = 800,
        )
        // The same two words, the second held twice as long from the inside: no blank opens
        // between them, so the silence share does not move and the speed gap does.
        val held = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400, 1200, Span(400, 800))),
            recorded = 1200, rendered = 800,
        )
        assertEquals(continuityOf(tight), continuityOf(held), 1e-3f)
        assertTrue(Fluency.rate(held)!! > Fluency.rate(tight)!!)
    }

    /** And the reverse: a blank opened between two words is continuity's and not rate's. */
    @Test
    fun `a blank between two words is the continuity's and never the rate's`() {
        val tight = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400, 800, Span(400, 800))),
            recorded = 800, rendered = 800,
        )
        val gaping = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(1400, 1800, Span(400, 800))),
            recorded = 1800, rendered = 800,
        )
        assertTrue(continuityOf(gaping) > continuityOf(tight))
        assertEquals(Fluency.rate(tight)!!, Fluency.rate(gaping)!!, 1e-3f)
    }

    /** A hesitation lengthens the mouth's work and must not reach the rate either. */
    @Test
    fun `filler never reaches the rate`() {
        val clean = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400, 800, Span(400, 800))),
            recorded = 800, rendered = 800,
        )
        val hesitant = Turn(
            spoken = listOf(
                kept(0, 400, Span(0, 400)),
                filler(400, 1200),
                kept(1200, 1600, Span(400, 800)),
            ),
            recorded = 1600, rendered = 800,
        )
        assertEquals(Fluency.rate(clean)!!, Fluency.rate(hesitant)!!, 1e-3f)
    }

    // ── The threshold ───────────────────────────────────────────────────────────────────

    /**
     * The micro-blank a stop closure leaves between two words is not a pause. Without the
     * threshold a sentence full of /p t k/ would look less continuous than one full of
     * vowels -- for its text, not for its speaker.
     */
    @Test
    fun `a blank under the threshold is not a pause`() {
        val closure = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400 + 150, 950, Span(400, 800))),
            recorded = 950, rendered = 800,
        )
        val tight = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400, 800, Span(400, 800))),
            recorded = 950, rendered = 800,
        )
        assertEquals(continuityOf(tight), continuityOf(closure), 1e-3f)
    }

    // ── The grace ───────────────────────────────────────────────────────────────────────

    /**
     * **Exactly one second is forgiven at each edge**, which is the whole of what the grace
     * does. Two seconds of reflex weigh 50% on a short turn and almost nothing on a long one
     * for identical behaviour; forgiving a second takes the same amount off both, which is
     * far more of the short one's figure than of the long one's.
     *
     * It does not make them equal, and it is not meant to: a short turn that spent most of
     * itself waiting really was less continuous.
     */
    @Test
    fun `the grace forgives one second at each edge and no more`() {
        fun reflex(words: Int): Turn {
            val said = (0 until words).map {
                kept(2000 + it * 400, 2400 + it * 400, Span(it * 400, 400 + it * 400))
            }
            return Turn(said, recorded = 2000 + words * 400, rendered = words * 400)
        }
        listOf(2, 20).forEach { words ->
            val turn = reflex(words)
            // Two seconds of lead-in, one of them forgiven, over the whole recording.
            val forgiven = (2000 - Fluency.GRACE_MS).toFloat() / turn.recorded * 100f
            assertEquals("$words words", forgiven, Fluency.continuity(turn)!!, 1e-3f)
        }
    }

    /** Inside the turn nothing is forgiven: a blank between two words counts whole. */
    @Test
    fun `the grace never reaches inside the turn`() {
        val turn = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(2400, 2800, Span(400, 800))),
            recorded = 2800, rendered = 800,
        )
        assertEquals(2000f / 2800f * 100f, Fluency.continuity(turn)!!, 1e-3f)
    }

    /**
     * **And it does not touch what a condition reads.** A silence of six seconds stays a
     * silence of six seconds for whoever is looking for a big blank -- which is why the grace
     * lives on continuity alone and not on the longest, whose whole argument (a share
     * distorted on a short turn) has no purchase on a maximum.
     */
    @Test
    fun `the grace does not shorten what a condition reads`() {
        val blocked = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(6400, 6800, Span(400, 800))),
            recorded = 6800, rendered = 800,
        )
        assertEquals(6.0f, Fluency.longestSilence(blocked), 1e-3f)
        assertEquals(6.0f, Fluency.blanks(blocked).first(), 1e-3f)
    }

    // ── Continuity ──────────────────────────────────────────────────────────────────────

    /**
     * Keeping quieter than the model is no fluency fault, and nothing is clipped for it: the
     * figure simply goes negative, which lands above the A bound.
     */
    @Test
    fun `keeping quieter than the model gives a negative gap and is not clipped`() {
        // The model pauses at a comma; the learner does not.
        val turn = Turn(
            spoken = listOf(kept(0, 400, Span(0, 400)), kept(400, 800, Span(1000, 1400))),
            recorded = 800, rendered = 1400,
        )
        assertTrue(Fluency.continuity(turn)!! < 0f)
    }

    @Test
    fun `the final silence counts, because handing the floor back is an act`() {
        val prompt = Turn(listOf(kept(0, 400, Span(0, 400))), recorded = 400, rendered = 400)
        val trailing = Turn(listOf(kept(0, 400, Span(0, 400))), recorded = 3400, rendered = 400)
        assertTrue(continuityOf(trailing) > continuityOf(prompt))
    }

    // ── Rate ────────────────────────────────────────────────────────────────────────────

    /** Twice as slow and twice as fast are the same figure, by construction. */
    @Test
    fun `the rate is symmetric`() {
        val slow = Turn(listOf(kept(0, 800, Span(0, 400))), recorded = 800, rendered = 400)
        val fast = Turn(listOf(kept(0, 400, Span(0, 800))), recorded = 400, rendered = 800)
        assertEquals(100f, Fluency.rate(slow)!!, 1e-3f)
        assertEquals(100f, Fluency.rate(fast)!!, 1e-3f)
    }

    @Test
    fun `speaking at the model's speed is no gap at all`() {
        assertEquals(0f, Fluency.rate(even())!!, 1e-3f)
    }

    // ── A turn with no kept word ────────────────────────────────────────────────────────

    /**
     * **Absent, and never nil.** An absent sheet leaves the sum; a sheet worth zero drags it
     * down for something that was never measured.
     */
    @Test
    fun `continuity and rate are absent on a turn with no kept word`() {
        val nothing = Turn(
            spoken = listOf(filler(0, 400), filler(900, 1300)),
            recorded = 1300, rendered = 0,
        )
        assertNull(Fluency.continuity(nothing))
        assertNull(Fluency.rate(nothing))
    }

    /** The two that still have everything they need on that same turn. */
    @Test
    fun `the longest silence still measures a turn with no kept word`() {
        val nothing = Turn(
            spoken = listOf(filler(0, 400), filler(900, 1300)),
            recorded = 1300, rendered = 0,
        )
        assertEquals(0.5f, Fluency.longestSilence(nothing), 1e-3f)
    }

    private fun continuityOf(turn: Turn): Float =
        Fluency.continuity(turn) ?: error("no continuity on a turn that has kept words")
}
