package app.saylune.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The live reading of the level, which governs the clock and what is kept as samples.
 *
 * It is deliberately not what any sheet reads: the fluency silences come from where the two
 * recordings put each word, which is the reading that knows a word boundary from a stop
 * closure. So the two definitions of silence never have to agree, and nothing here is a
 * second source that could drift from a measure.
 */
class SilenceTest {

    /** [count] bytes of 16-bit little-endian PCM at a constant amplitude. */
    private fun tone(count: Int, amplitude: Int): ByteArray {
        val out = ByteArray(count)
        var at = 0
        var up = true
        while (at + 1 < count) {
            val sample = if (up) amplitude else -amplitude
            out[at] = (sample and 0xFF).toByte()
            out[at + 1] = ((sample shr 8) and 0xFF).toByte()
            up = !up
            at += 2
        }
        return out
    }

    @Test
    fun `a flat zero reads as no level at all`() {
        assertEquals(0f, Silence.level(ByteArray(320), 320), 0.0001f)
    }

    @Test
    fun `full scale reads as one`() {
        assertEquals(1f, Silence.level(tone(320, Short.MAX_VALUE.toInt()), 320), 0.001f)
    }

    /**
     * The threshold has to sit between a quiet room and a quiet voice, and this says which
     * side of it each falls on. The two amplitudes are not measured -- they stand for the
     * order of magnitude, which is all a hand-set value can claim.
     */
    @Test
    fun `a room floor is silence and a quiet voice is not`() {
        val floor = Silence.level(tone(320, (Short.MAX_VALUE * 0.002f).toInt()), 320)
        val quiet = Silence.level(tone(320, (Short.MAX_VALUE * 0.05f).toInt()), 320)
        assertTrue("$floor should be under ${Silence.LEVEL}", floor < Silence.LEVEL)
        assertTrue("$quiet should be over ${Silence.LEVEL}", quiet > Silence.LEVEL)
    }

    /** A count shorter than one sample has nothing to average, and says so rather than dividing. */
    @Test
    fun `nothing to read reads as nothing`() {
        assertEquals(0f, Silence.level(ByteArray(2), 1), 0.0001f)
    }

}
