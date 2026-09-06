package app.speakup.levers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the lever catalogue has to hold, whatever the values in it turn out to be.
 *
 * Every case here is a **property**: the numbers it uses are material and never what it
 * asserts. Tuning a bound or a default is the calibration bench's work, before publication,
 * and none of it should move a line of this file.
 */
class LeversTest {

    @Test fun `a key nobody declared fails outright`() {
        val thrown = runCatching { Positions().of("seuil-de-silence") }.exceptionOrNull()
        assertTrue("an undeclared key must not quietly take a default", thrown != null)
    }

    @Test fun `a declared key nobody set takes its declared default`() {
        assertEquals(At("scrambled"), Positions().of("ai-turn.display"))
        assertEquals(Count(null), Positions().of("rewordings-allowed"))
    }

    @Test fun `a move stops at the bound rather than failing`() {
        val (at, moved) = Positions().by("ai-turn.display", 9)
        assertEquals(At("nothing"), at.of("ai-turn.display"))
        assertEquals(Direction.Harder, moved?.direction)
    }

    @Test fun `a move that shifted nothing announces nothing`() {
        val top = Positions().by("ai-turn.display", 9).first
        val (again, moved) = top.by("ai-turn.display", 1)
        assertNull("announcing 'this tightens' with nothing changed is a lie", moved)
        assertEquals(top, again)
    }

    @Test fun `direction reads the same in both forms`() {
        // Stepped: the declaration orders it, easiest first.
        val words = Positions().by("advance.words", 1).second
        assertEquals(Direction.Harder, words?.direction)
        assertEquals(Direction.Easier, Positions().by("echo", -1).second?.direction)

        // Numeric: arithmetic, plus the end the lever names as hard.
        val three = Positions().at("lives.left", Count(3)).first
        assertEquals(Direction.Harder, three.by("lives.left", 1).second?.direction)
        assertEquals(Direction.Easier, three.by("lives.left", -1).second?.direction)
    }

    @Test fun `no maximum sits above every number`() {
        val free = Positions()
        assertEquals(Count(null), free.of("retakes-allowed"))
        val counted = free.at("retakes-allowed", Count(2))
        assertEquals(Direction.Harder, counted.second?.direction)
        assertEquals(Direction.Easier, counted.first.at("retakes-allowed", Count(null)).second?.direction)
    }

    @Test fun `a move off no maximum fails instead of inventing a neighbour`() {
        val thrown = runCatching { Positions().by("retakes-allowed", -1) }.exceptionOrNull()
        assertTrue("'no maximum' minus one notch is not a number", thrown != null)
    }

    @Test fun `a lever with no object keeps its value and says why`() {
        val plain = Positions()
        assertTrue("capture is on the finger, so nothing sends on its own",
                   !plain.live("silence-threshold"))
        // The value stays on the line, so it is there if capture goes back up.
        assertEquals(Count(5), plain.of("silence-threshold"))
        val armed = plain.at("capture", At("armed-and-sending")).first
        assertTrue(armed.live("silence-threshold"))
        assertEquals(Count(5), armed.of("silence-threshold"))
    }

    @Test fun `every declared default is one of the lever's own positions`() {
        // The constructors check it; this is what makes the catalogue load at all, and it
        // fails here rather than on a device.
        assertEquals(Levers.all.size, Levers.all.map { it.key }.toSet().size)
        Levers.all.forEach { lever -> Positions().of(lever.key) }
    }
}
