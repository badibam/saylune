package app.saylune.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The palette's values are the bench's and are judged by looking; what is proved here is the
 * two rules they have to satisfy, which are not judged by looking at all.
 */
class PaletteTest {

    private val registers = mapOf(
        "night" to NightPlum,
        "pale" to PalePlum,
        "night spare" to NightPlumSpare,
        "pale spare" to PalePlumSpare,
    )

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

    /** Amber to red at one chroma, so the ramp reads as one family whatever else it spends. */
    @Test
    fun `the ramp turns from amber to red`() {
        registers.forEach { (name, register) ->
            val ramp = Palette(register).ramp
            assertEquals(Palette.RAMP_NOTCHES, ramp.size)
            assertEquals("$name", Palette.RAMP_START_HUE, ramp.first().h, 1e-3f)
            assertEquals("$name", register.rampEndHue, ramp.last().h, 1e-3f)
            ramp.forEach { assertEquals("$name", register.rampChroma, it.c, 1e-4f) }
            // Strictly turning, so no two notches are the same colour.
            ramp.zipWithNext().forEach { (a, b) -> assertTrue("$name", b.h < a.h) }
        }
    }

    /**
     * The plain registers hold their ramp at one lightness, so it reads as one family. The
     * spare spends lightness instead, climbing away from the ground as the alarm rises, so
     * that four notches from amber to red have a second axis to separate on for an eye that
     * does not read that arc.
     */
    @Test
    fun `the spare separates its notches by lightness and the plain ones do not`() {
        registers.forEach { (name, register) ->
            val ramp = Palette(register).ramp
            if (register.rampLightnessSpread == 0f) {
                ramp.forEach { assertEquals(name, register.rampLightness, it.l, 1e-4f) }
            } else {
                val away = if (register.groundLightness < 0.5f) 1f else -1f
                ramp.zipWithNext().forEach { (a, b) ->
                    assertTrue("$name: ${a.l} then ${b.l}", (b.l - a.l) * away > 0f)
                }
            }
        }
    }

    /**
     * **The worst case in the doc**: *juste* and *à côté* are the same shape and two opposite
     * verdicts, told apart by colour alone. The stress target and the stray have the same
     * defect. Here that only checks they are far apart at all -- what a red-green eye actually
     * makes of them is not simulated anywhere and is what the bench still owes.
     */
    @Test
    fun `the good end and the alarm are never near each other`() {
        registers.forEach { (name, register) ->
            val palette = Palette(register)
            assertTrue("$name", (palette.green apart palette.ramp.last()) > 0.15f)
            assertTrue("$name", (palette.accent apart palette.ramp.last()) > 0.15f)
        }
    }

    /**
     * In the spare the good end takes the blue, so the melody contour gives it up: it keeps
     * its place by being nearly colourless where the good end is saturated. They are not far
     * apart in OKLab and do not need to be -- one is a curve in the band above the line, the
     * other a bracket around words, and the form is what says which channel. What would be a
     * collision is two marks of the same shape, and there is none here.
     */
    @Test
    fun `the spare gives the melody contour up to keep its good end coloured`() {
        listOf("night spare" to NightPlumSpare, "pale spare" to PalePlumSpare)
            .forEach { (name, register) ->
                val palette = Palette(register)
                assertTrue("$name", palette.green.c > palette.melodyModel.c * 3f)
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
        assertEquals(0, app.saylune.ui.notchOf(0f))
        assertEquals(0, app.saylune.ui.notchOf(app.saylune.ui.NOISE_BAND))
        assertEquals(1, app.saylune.ui.notchOf(app.saylune.ui.NOISE_BAND + 0.1f))
        assertEquals(Palette.RAMP_NOTCHES, app.saylune.ui.notchOf(app.saylune.ui.SATURATES))
        assertEquals(Palette.RAMP_NOTCHES, app.saylune.ui.notchOf(200f))
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
