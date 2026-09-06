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
        assertEquals(At("brouille"), Positions().of("tour-ia.affichage"))
        assertEquals(Count(null), Positions().of("reformulations-permises"))
    }

    @Test fun `a move stops at the bound rather than failing`() {
        val (at, moved) = Positions().by("tour-ia.affichage", 9)
        assertEquals(At("rien"), at.of("tour-ia.affichage"))
        assertEquals(Direction.Harder, moved?.direction)
    }

    @Test fun `a move that shifted nothing announces nothing`() {
        val top = Positions().by("tour-ia.affichage", 9).first
        val (again, moved) = top.by("tour-ia.affichage", 1)
        assertNull("announcing 'this tightens' with nothing changed is a lie", moved)
        assertEquals(top, again)
    }

    @Test fun `direction reads the same in both forms`() {
        // Stepped: the declaration orders it, easiest first.
        val words = Positions().by("avance.mots", 1).second
        assertEquals(Direction.Harder, words?.direction)
        assertEquals(Direction.Easier, Positions().by("echo", -1).second?.direction)

        // Numeric: arithmetic, plus the end the lever names as hard.
        val three = Positions().at("vies.restantes", Count(3)).first
        assertEquals(Direction.Harder, three.by("vies.restantes", 1).second?.direction)
        assertEquals(Direction.Easier, three.by("vies.restantes", -1).second?.direction)
    }

    @Test fun `no maximum sits above every number`() {
        val free = Positions()
        assertEquals(Count(null), free.of("redites-permises"))
        val counted = free.at("redites-permises", Count(2))
        assertEquals(Direction.Harder, counted.second?.direction)
        assertEquals(Direction.Easier, counted.first.at("redites-permises", Count(null)).second?.direction)
    }

    @Test fun `a move off no maximum fails instead of inventing a neighbour`() {
        val thrown = runCatching { Positions().by("redites-permises", -1) }.exceptionOrNull()
        assertTrue("'no maximum' minus one notch is not a number", thrown != null)
    }

    @Test fun `a lever with no object keeps its value and says why`() {
        val plain = Positions()
        assertTrue("capture is on the finger, so nothing sends on its own",
                   !plain.live("seuil-silence"))
        // The value stays on the line, so it is there if capture goes back up.
        assertEquals(Count(5), plain.of("seuil-silence"))
        val armed = plain.at("capture", At("armee-et-silence")).first
        assertTrue(armed.live("seuil-silence"))
        assertEquals(Count(5), armed.of("seuil-silence"))
    }

    @Test fun `every declared default is one of the lever's own positions`() {
        // The constructors check it; this is what makes the catalogue load at all, and it
        // fails here rather than on a device.
        assertEquals(Levers.all.size, Levers.all.map { it.key }.toSet().size)
        Levers.all.forEach { lever -> Positions().of(lever.key) }
    }
}
