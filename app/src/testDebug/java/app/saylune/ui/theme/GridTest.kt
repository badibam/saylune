package app.saylune.ui.theme

import androidx.compose.ui.unit.Density
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What is proved here is the grid's two hard properties, not the factor of the phone in hand:
 * the values move with the device, the properties do not.
 */
class GridTest {

    private val densities = generateSequence(0.75f) { it + 0.05f }.takeWhile { it <= 4.01f }

    /** A capital in dp, which is the yardstick the scale is chosen against. */
    private fun capDp(scale: Int, density: Float) = Grid.CAP * scale / density

    @Test
    fun `the scale is always a whole number of at least one`() {
        densities.forEach { d ->
            val scale = gridFor(Density(d)).scale
            assertTrue("density $d gave scale $scale", scale >= 1)
        }
    }

    @Test
    fun `no other whole factor puts a capital nearer ten dp`() {
        densities.forEach { d ->
            val chosen = gridFor(Density(d)).scale
            val ours = abs(capDp(chosen, d) - 10f)
            (1..8).forEach { other ->
                val theirs = abs(capDp(other, d) - 10f)
                assertTrue(
                    "at density $d, scale $other lands ${theirs} from 10 dp against ours $ours",
                    theirs >= ours - 1e-4f,
                )
            }
        }
    }

    @Test
    fun `what is painted lands on whole screen pixels`() {
        densities.forEach { d ->
            val grid = gridFor(Density(d))
            listOf(1, 2, 5, 11, 22).forEach { drawn ->
                val painted = grid.painted(drawn)
                assertEquals(
                    "density $d, $drawn drawing pixels",
                    (drawn * grid.scale).toFloat(),
                    painted,
                    0f,
                )
            }
        }
    }

    /**
     * A layout length has to come back as the pixels it was made from: a cell that rounds to
     * one pixel less than it was built from would put the marking off its characters, which is
     * the one thing the grid exists to hold.
     */
    @Test
    fun `a drawn length converts back to the pixels it was made of`() {
        densities.forEach { d ->
            val density = Density(d)
            val grid = gridFor(density)
            listOf(1, 2, 5, 11, 22).forEach { drawn ->
                val back = with(density) { grid.drawn(drawn).roundToPx() }
                assertEquals("density $d, $drawn drawing pixels", drawn * grid.scale, back)
            }
        }
    }
}
