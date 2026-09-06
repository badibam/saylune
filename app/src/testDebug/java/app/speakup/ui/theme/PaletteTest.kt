package app.speakup.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The palette's values are the bench's and are judged by looking; what is proved here is the
 * two rules they have to satisfy, which are not judged by looking at all.
 */
class PaletteTest {

    private val registers = mapOf("night" to NightPlum, "pale" to PalePlum)

    /**
     * **A marked letter must be far enough from a bare one**, and the distance is measured on
     * the **first notch** -- the hardest case, the one that grazes the neutral ink. It may be
     * paid in lightness, in chroma, or in both: measuring lightness alone called a perfectly
     * legible pale palette a failure.
     *
     * The floor is the bench's, and the two registers pay it differently: at night mostly in
     * lightness, so a marked letter lights up; in pale mostly in chroma, the neutral ink being
     * nearly colourless, so a marked letter takes on colour.
     */
    @Test
    fun `the first notch stands off the neutral ink in both registers`() {
        registers.forEach { (name, register) ->
            val palette = Palette(register)
            val apart = palette.ramp.first() apart palette.ink
            assertTrue("$name: the first notch is $apart from the ink", apart > FLOOR)
        }
    }

    /** And it has to stand off the ground too, which is plain legibility rather than marking. */
    @Test
    fun `the first notch stands off the ground in both registers`() {
        registers.forEach { (name, register) ->
            val palette = Palette(register)
            val apart = palette.ramp.first() apart palette.ground
            assertTrue("$name: the first notch is $apart from the ground", apart > FLOOR)
        }
    }

    /**
     * Every entry has to exist in sRGB once the chroma has been reduced -- which is what says
     * the clipping ran at all. Per-channel clipping would shift the hue as well as fade it,
     * and that is what made the red end of the ramp come out pink.
     */
    @Test
    fun `every entry survives into sRGB`() {
        registers.forEach { (name, register) ->
            entriesOf(Palette(register)).forEach { (what, colour) ->
                val srgb = colour.srgb
                listOf("red" to srgb.red, "green" to srgb.green, "blue" to srgb.blue)
                    .forEach { (channel, value) ->
                        assertTrue("$name/$what: $channel is $value", value in 0f..1f)
                    }
            }
        }
    }

    /** Amber to red, at one lightness and one chroma, so the ramp reads as one family. */
    @Test
    fun `the ramp turns from amber to red at a constant lightness`() {
        registers.forEach { (name, register) ->
            val ramp = Palette(register).ramp
            assertEquals(Palette.RAMP_NOTCHES, ramp.size)
            assertEquals("$name", Palette.RAMP_START_HUE, ramp.first().h, 1e-3f)
            assertEquals("$name", register.rampEndHue, ramp.last().h, 1e-3f)
            ramp.forEach {
                assertEquals("$name", register.rampLightness, it.l, 1e-4f)
                assertEquals("$name", register.rampChroma, it.c, 1e-4f)
            }
            // Strictly turning, so no two notches are the same colour.
            ramp.zipWithNext().forEach { (a, b) -> assertTrue("$name", b.h < a.h) }
        }
    }

    /**
     * A halo is the stroke's own colour mixed toward the ground, and it stays **opaque**: an
     * alpha halo would let the learner's red show through the model's blue at the very moment
     * the covering has to be total.
     */
    @Test
    fun `a halo sits between its stroke and the ground`() {
        registers.forEach { (name, register) ->
            val palette = Palette(register)
            val stroke = palette.melodyLearner
            val halo = palette.halo(stroke, 0.50f)
            assertTrue("$name", (halo apart stroke) > 0f)
            assertTrue("$name", (halo apart palette.ground) < (stroke apart palette.ground))
        }
    }

    /** Mixing takes the short way round the circle: amber toward plum never passes through green. */
    @Test
    fun `a mix never travels the long way round the hue circle`() {
        val amber = Lch(0.78f, 0.13f, 20f)
        val plum = Lch(0.20f, 0.045f, 300f)
        assertEquals(340f, amber.mix(plum, 0.5f).h, 1e-3f)
    }

    /** Which notch of the ramp a gap falls on: nothing under the noise band, saturated over the line. */
    @Test
    fun `the notches split the band between noise and saturation`() {
        assertEquals(0, app.speakup.ui.notchOf(0f))
        assertEquals(0, app.speakup.ui.notchOf(app.speakup.ui.NOISE_BAND))
        assertEquals(1, app.speakup.ui.notchOf(app.speakup.ui.NOISE_BAND + 0.1f))
        assertEquals(Palette.RAMP_NOTCHES, app.speakup.ui.notchOf(app.speakup.ui.SATURATES))
        assertEquals(Palette.RAMP_NOTCHES, app.speakup.ui.notchOf(200f))
    }

    private fun entriesOf(palette: Palette): List<Pair<String, Lch>> = listOf(
        "ground" to palette.ground,
        "panel" to palette.panel,
        "frameLight" to palette.frameLight,
        "frameDark" to palette.frameDark,
        "ink" to palette.ink,
        "dim" to palette.dim,
        "green" to palette.green,
        "accent" to palette.accent,
        "melodyModel" to palette.melodyModel,
        "melodyLearner" to palette.melodyLearner,
    ) + palette.ramp.mapIndexed { notch, colour -> "ramp$notch" to colour }

    companion object {
        /** The bench's bar: under it the first notch passes for neutral ink. */
        const val FLOOR = 0.10f
    }
}
