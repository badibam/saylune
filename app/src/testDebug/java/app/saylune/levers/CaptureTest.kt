package app.saylune.levers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the three capture positions actually govern, read off the catalogue.
 *
 * These are properties of the declaration and not of the numbers in it: the bounds are
 * material the calibration bench will move, and a test that pinned one would fail the day
 * the bench does its job. What is pinned is what stays true whatever the numbers become.
 */
class CaptureTest {

    private fun at(vararg pairs: Pair<String, String>) =
        Positions(pairs.associate { (key, name) -> key to At(name) })

    /**
     * **Zero puts the lever out when that is true, otherwise two levers.** The silence
     * threshold has no value meaning *no threshold* -- zero would send the turn at once --
     * and what separates the second position from the third is not x but the fact that the
     * turn goes on its own. So: two levers, and the threshold has no object at the first two.
     */
    @Test
    fun `the silence threshold is only read where a silence sends`() {
        assertFalse(at("capture" to Levers.BY_HAND).live(Levers.SILENCE_THRESHOLD.key))
        assertFalse(at("capture" to Levers.ARMED).live(Levers.SILENCE_THRESHOLD.key))
        assertTrue(at("capture" to Levers.ARMED_AND_SENDING).live(Levers.SILENCE_THRESHOLD.key))
    }

    /** The preparation is the wait before the mic arms, so it has no object where no mic arms. */
    @Test
    fun `the preparation is only read where the mic arms on its own`() {
        assertFalse(at("capture" to Levers.BY_HAND).live(Levers.PREPARATION.key))
        assertTrue(at("capture" to Levers.ARMED).live(Levers.PREPARATION.key))
        assertTrue(at("capture" to Levers.ARMED_AND_SENDING).live(Levers.PREPARATION.key))
    }

    /**
     * And throwing a take away has no object at the **third**, for the opposite reason: it is
     * the one position where the learner is not alone in sending, so the silence one hesitates
     * through is what sends the take, and the button would be a race against the pendulum.
     */
    @Test
    fun `throwing a take away has no object where a clock also sends`() {
        assertTrue(at("capture" to Levers.BY_HAND).live(Levers.DISCARD_TAKE.key))
        assertTrue(at("capture" to Levers.ARMED).live(Levers.DISCARD_TAKE.key))
        assertFalse(at("capture" to Levers.ARMED_AND_SENDING).live(Levers.DISCARD_TAKE.key))
    }

    /**
     * **No object does not mean absent.** The value stays on the line and is simply not read,
     * so it is still there if capture goes back up to the third position.
     */
    @Test
    fun `a lever with no object keeps the value it was given`() {
        val settings = Positions(mapOf(
            "capture" to At(Levers.BY_HAND),
            Levers.SILENCE_THRESHOLD.key to Count(7),
        ))
        assertFalse(settings.live(Levers.SILENCE_THRESHOLD.key))
        assertEquals(Count(7), settings.of(Levers.SILENCE_THRESHOLD.key))
    }

    /**
     * The turn's length holds at all three positions: it is the one clock that is not about
     * who opens the mic. A challenge asking for an answer in five seconds and the technical
     * ceiling of thirty are the same variable set differently.
     */
    @Test
    fun `the turn's length is read wherever the turn is`() {
        listOf(Levers.BY_HAND, Levers.ARMED, Levers.ARMED_AND_SENDING).forEach {
            assertTrue(at("capture" to it).live(Levers.TURN_LENGTH.key))
        }
    }

    /**
     * What a free conversation does when nobody has asked for anything: the mic is opened by
     * hand, so no clock sends, and a take may be thrown away.
     */
    @Test
    fun `the defaults are a conversation nobody has set anything for`() {
        val free = Positions()
        assertEquals(At(Levers.BY_HAND), free.of(Levers.CAPTURE.key))
        assertFalse(free.live(Levers.SILENCE_THRESHOLD.key))
        assertEquals(At("allowed"), free.of(Levers.DISCARD_TAKE.key))
    }
}
