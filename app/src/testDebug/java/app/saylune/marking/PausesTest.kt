package app.saylune.marking

import app.saylune.analysis.AnalysedSound
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The silences of a turn, read off where its sounds were said.
 *
 * What is held here is the **shape** -- where a pause is anchored and how many notches it
 * carries -- and never the thresholds, which are the calibration bench's material and move
 * with it (`../../../../../../TODO.md`).
 */
class PausesTest {

    /** A sound is its letters and its two spans; only the said one is read here. */
    private fun sound(at: IntRange, from: Int, to: Int) = AnalysedSound(
        symbol = "s",
        points = 0f,
        letters = "",
        borrowed = false,
        at = at,
        modelMs = 0..0,
        saidMs = from..to,
        model = emptyList(),
        said = emptyList(),
    )

    // "one two": the first word runs to 500 ms, the second from 1200 to 1600.
    private val text = "one two"
    private val sounds = listOf(sound(0..2, 100, 500), sound(4..6, 1_200, 1_600))

    @Test
    fun `the silence between two words is anchored on the word before it`() {
        val pauses = pausesOf(text, sounds)
        assertEquals(listOf(Pause(after = 2, notches = 2)), pauses)
    }

    /**
     * The closing silence: the stretch between the last sound and the end of the recording,
     * anchored on the last word like any other pause.
     */
    @Test
    fun `the closing silence is drawn when the recording says where it ended`() {
        assertEquals(
            listOf(Pause(after = 2, notches = 2), Pause(after = 6, notches = 1)),
            pausesOf(text, sounds, recorded = 1_900),
        )
    }

    /** A turn that stopped on the last word has no closing silence rather than one of nothing. */
    @Test
    fun `a recording that ends on the last sound closes no silence`() {
        assertEquals(listOf(Pause(after = 2, notches = 2)), pausesOf(text, sounds, recorded = 1_650))
    }

    /** A turn kept before the figure existed carries none, and nothing is invented for it. */
    @Test
    fun `without the recording's length there is no closing silence`() {
        assertEquals(1, pausesOf(text, sounds, recorded = null).size)
    }
}
