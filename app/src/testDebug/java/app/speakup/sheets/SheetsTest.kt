package app.speakup.sheets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the sheet tree has to hold, whatever the numbers in it turn out to be.
 *
 * Every case is a **property**. The series and the columns are a first pass that the
 * calibration bench takes over sheet by sheet, and none of that work should move a line here.
 */
class SheetsTest {

    private val scored get() = Sheets.all.filter { it.scored }

    @Test fun `a sheet with one element keeps its raw unit and has nothing to average`() {
        val silence = Sheets.of("fluidite/plus-long-silence") as Sheet
        assertEquals(Elements.Whole, silence.elements)
        assertEquals(Reading.Raw, silence.reading)
        assertEquals(Unit.Seconds, silence.unit)
        // A ramp only brings heterogeneous elements onto a common scale before averaging
        // them. With no mean, it has no object.
        assertTrue(Sheets.all.none { it.elements == Elements.Whole && it.reading is Reading.Slope })
    }

    @Test fun `the denominator is what the sheet reads, never the whole passage`() {
        // Each of these reads a different population of the same turn, and that is the whole
        // point: the weight picks the columns, the denominator follows the weight.
        assertEquals(Elements.KeptWords, (Sheets.of("correction/correction") as Sheet).elements)
        assertEquals(Elements.SpokenWords,
                     (Sheets.of("fluidite/remplissage-reprises") as Sheet).elements)
        assertEquals(Elements.StressedWords,
                     (Sheets.of("elocution/accent-lexical") as Sheet).elements)
        // Two sheets on one judged marking, and they do not read the same words.
        val spans = scored.filter { it.from == Marking.LanguageSpans }
        assertEquals(2, spans.size)
        assertTrue(spans.all { it.elements == Elements.KeptWords })
    }

    @Test fun `a binary sheet has neither sensitivity nor weight`() {
        val interrupted = Sheets.of("tour-interrompu") as Sheet
        assertEquals(Reading.Either, interrupted.reading)
        // No series means no sensitivity window to pick, and it is outside the tree, so
        // there is no node for a weight to sit on. Two fields with no object rather than
        // two declared inert.
        assertNull(interrupted.series)
        assertTrue(!interrupted.scored)
        assertTrue(Sheets.unscored.any { it === interrupted })
    }

    @Test fun `a counted sheet never gives a note`() {
        // A count climbs at every attempt: weighed in, the note could never cross the bar
        // again and the learner would burn attempts with no way out.
        val counts = Sheets.unscored.filter { it.unit == Unit.Times }
        assertEquals(3, counts.size)
        assertTrue(counts.none { it.scored })
    }

    @Test fun `the value of an element is a quality between 0 and 1, the top being good`() {
        scored.forEach { sheet ->
            when (val reading = sheet.reading) {
                is Reading.Column -> reading.notches.forEach {
                    assertTrue("${sheet.name}/${it.name}", it.value in 0f..1f)
                }
                // A cliff and a slope both land on 0..1 by construction; what matters is
                // that neither declares a direction, the good end being the top already.
                is Reading.Cliff, is Reading.Slope, Reading.Either -> assertNull(sheet.direction)
                // And a direction is declared exactly where the figure keeps a raw unit.
                Reading.Raw -> assertNotNull(sheet.name, sheet.direction)
            }
        }
    }

    @Test fun `a direction is declared exactly where the figure keeps a raw unit`() {
        Sheets.all.forEach { sheet ->
            assertEquals(sheet.name, sheet.unit.raw && sheet.reading == Reading.Raw,
                         sheet.direction != null)
        }
    }

    @Test fun `every series climbs towards its good end and sizes its windows`() {
        scored.forEach { sheet ->
            val series = sheet.series!!
            assertEquals(sheet.name, SERIES_LENGTH, series.size)
            // Windows of four consecutive bounds, one per position, none left over.
            assertEquals(sheet.name, POSITIONS, series.size - 3)
        }
    }

    @Test fun `an unmarked element takes a notch and never an absence`() {
        // A word carrying nothing is `ok`, which is a notch like any other. Where the judge
        // marks every element, there is nothing to fall back to.
        val correction = (Sheets.of("correction/correction") as Sheet).reading as Reading.Column
        assertEquals("ok", correction.fallback)
        val stumbling =
            (Sheets.of("fluidite/remplissage-reprises") as Sheet).reading as Reading.Column
        assertNull(stumbling.fallback)
    }

    @Test fun `correctness has no notch above ok, relevance does`() {
        // There is no norm for a difficult construction brought off right, so it has no
        // notch; an ambitious sentence that lands is marked on the relevance side, where
        // the criterion is situational by construction.
        val correction = (Sheets.of("correction/correction") as Sheet).reading as Reading.Column
        assertEquals(1.00f, correction.notches.first { it.name == "ok" }.value, 0f)
        val relevance = (Sheets.of("pertinence/pertinence") as Sheet).reading as Reading.Column
        assertTrue(relevance.notches.first { it.name == "juste" }.value >
                   relevance.notches.first { it.name == "ok" }.value)
    }

    @Test fun `only relevance among the correctness pair reads an instruction`() {
        // Correctness is an absolute judgement against a norm the app fixes, identical for
        // every activity: no instruction moves it. Relevance carries everything a challenge
        // wants to demand.
        assertEquals(Marking.LanguageSpans, (Sheets.of("correction/correction") as Sheet).from)
        assertEquals(Marking.LanguageSpans, (Sheets.of("pertinence/pertinence") as Sheet).from)
    }

    @Test fun `a path nobody declared fails outright`() {
        assertTrue(runCatching { Sheets.of("elocution/rythme") }.isFailure)
        assertTrue(runCatching { Sheets.of("melodie") }.isFailure)
        assertNotNull(Sheets.of("elocution/melodie"))
    }

    @Test fun `every node is reachable by the path it is addressed with`() {
        Sheets.all.forEach { assertEquals(it, Sheets.of(Sheets.pathOf(it))) }
        assertEquals(11, scored.size)
        assertEquals(4, Sheets.unscored.size)
    }
}
