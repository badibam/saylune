package app.saylune.notes

import app.saylune.sheets.Branch
import app.saylune.sheets.POSITIONS
import app.saylune.sheets.Reading
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets
import app.saylune.sheets.Unit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What is proved here are the properties, not the numbers. Every series and every column in
 * the catalogue is a first pass the calibration bench takes over, so a test that pinned one
 * would only say the bench had not run yet.
 */
class NotesTest {

    private val sheet = { path: String -> Sheets.of(path) as Sheet }
    private val scored = Sheets.all.filter { it.scored }

    // ── The scale ───────────────────────────────────────────────────────────────────────

    @Test
    fun `the letters cut the scale into equal fifths`() {
        assertEquals(Letter.E, Note(0.00f).letter)
        assertEquals(Letter.E, Note(0.19f).letter)
        assertEquals(Letter.D, Note(0.20f).letter)
        assertEquals(Letter.C, Note(0.40f).letter)
        assertEquals(Letter.B, Note(0.60f).letter)
        assertEquals(Letter.A, Note(0.80f).letter)
        assertEquals(Letter.A, Note(1.00f).letter)
    }

    /** The bar is not a number of its own: it is where B starts, and that is why it never moves. */
    @Test
    fun `the bar is where B starts, and A or B clears it`() {
        assertEquals(Letter.B.floor, BAR)
        assertTrue(Note(BAR).passes)
        assertTrue(Note(0.99f).passes)
        assertTrue(!Note(BAR - 0.01f).passes)
    }

    @Test
    fun `the modifier is the quarter of the band`() {
        assertEquals(Modifier.Minus, Note(0.61f).modifier)
        assertEquals(Modifier.None, Note(0.70f).modifier)
        assertEquals(Modifier.Plus, Note(0.79f).modifier)
    }

    // ── The window ──────────────────────────────────────────────────────────────────────

    /**
     * The claim the whole sensitivity mechanism rests on: *severe means the same thing
     * everywhere*. What was a B at normal is a C at severe, on the sounds as on the silence as
     * on the rate -- and that is checked on every scored sheet rather than on a chosen one.
     */
    @Test
    fun `one notch of sensitivity is worth exactly one letter`() {
        scored.forEach { sheet ->
            (0 until POSITIONS - 1).forEach { position ->
                val lenient = sheet.windowAt(position)
                val stricter = sheet.windowAt(position + 1)
                // The bounds of the stricter window are the lenient one's, slid by one rank:
                // so a figure sitting on a bound falls one letter lower.
                stricter.bounds.dropLast(1).forEachIndexed { rank, bound ->
                    val above = lenient.noteOf(bound).letter
                    val below = stricter.noteOf(bound).letter
                    assertEquals(
                        "${sheet.name}, position $position, bound $rank",
                        1,
                        above.ordinal - below.ordinal,
                    )
                }
            }
        }
    }

    @Test
    fun `a figure asked back from a note reads as that note again`() {
        scored.forEach { sheet ->
            (0 until POSITIONS).forEach { position ->
                val window = sheet.windowAt(position)
                listOf(0.05f, 0.20f, 0.35f, 0.60f, 0.80f, 0.95f).forEach { note ->
                    assertEquals(
                        "${sheet.name} at position $position, note $note",
                        note,
                        window.noteOf(window.figureOf(Note(note))).value,
                        1e-3f,
                    )
                }
            }
        }
    }

    @Test
    fun `every bound of a window lands on a letter's floor`() {
        scored.forEach { sheet ->
            (0 until POSITIONS).forEach { position ->
                val window = sheet.windowAt(position)
                window.bounds.forEachIndexed { rank, bound ->
                    assertEquals(
                        "${sheet.name}, position $position, bound $rank",
                        Letter.entries[rank + 1].floor,
                        window.noteOf(bound).value,
                        1e-4f,
                    )
                }
            }
        }
    }

    /**
     * Keeping quieter than the model is no fluency fault. It falls above the A bound, in the
     * same band as zero, and nothing had to be clipped for that.
     */
    @Test
    fun `a negative continuity gap lands in A without clipping`() {
        val window = sheet("fluency/continuity").windowAt(2)
        // The figure itself is never clipped on the way in: a negative gap is read as it
        // stands, and it lands in the same band as zero.
        assertEquals(Letter.A, window.noteOf(-8f).letter)
        assertEquals(Letter.A, window.noteOf(0f).letter)
        assertTrue(window.noteOf(0f).value >= window.noteOf(window.bounds.last()).value)
    }

    // ── The detector ────────────────────────────────────────────────────────────────────

    /**
     * The detector is what is proved here, not the series: a series is material and the bench
     * moves it. So an edge is bent on purpose and the detector has to see it.
     */
    @Test
    fun `a bound outside what a share can be makes a letter unreachable`() {
        val impossible = sheet("correctness/correctness").copy(
            // A share never passes 1, so nothing can reach A above this bound.
            series = listOf(0.70f, 0.80f, 0.87f, 0.92f, 0.95f, 0.97f, 0.985f, 1.30f),
        )
        assertEquals(setOf(Letter.A), impossible.windowAt(POSITIONS - 1).unreachable())
    }

    /**
     * A sheet whose figure runs continuously has no excuse: four bounds inside the range it
     * can produce leave all five letters open, and a bound outside it is a writing mistake.
     */
    @Test
    fun `every continuous series leaves all five letters reachable`() {
        scored.filter { it.unit != Unit.NotchValue }.forEach { sheet ->
            (0 until POSITIONS).forEach { position ->
                assertEquals(
                    "${sheet.name} at position $position",
                    emptySet<Letter>(),
                    sheet.windowAt(position).unreachable(),
                )
            }
        }
    }

    /**
     * A sheet whose single element carries a notch can only ever show as many figures as it
     * has notches, so a middle letter can fall out of reach and that is the coarseness the doc
     * accepts -- the precision **is** the notch's. **Both ends must stay open**: a top out of
     * reach would punish everyone for an occasion nobody gave them, and a bottom out of reach
     * would make the worst answer readable as passable.
     */
    @Test
    fun `a notch sheet keeps both ends of the scale in reach`() {
        scored.filter { it.unit == Unit.NotchValue }.forEach { sheet ->
            (0 until POSITIONS).forEach { position ->
                val out = sheet.windowAt(position).unreachable()
                assertTrue("${sheet.name} at position $position: $out", Letter.A !in out)
                assertTrue("${sheet.name} at position $position: $out", Letter.E !in out)
            }
        }
    }

    /**
     * What the reach's series is **for**, stated as the property and not as its numbers: the
     * sensitivity says what an ordinary sentence is worth, and `plain` walks the whole scale
     * A to E as the position climbs -- one notch of sensitivity, one letter, read on the
     * notch a learner produces all day.
     */
    @Test
    fun `the reach position says what a plain sentence is worth`() {
        val reach = sheet("relevance/reach")
        val plain = reach.notch("plain")
        val letters = (0 until POSITIONS).map { reach.windowAt(it).noteOf(plain).letter }
        assertEquals(listOf(Letter.A, Letter.B, Letter.C, Letter.D, Letter.E), letters)
    }

    /**
     * **The top of the reach is never out of reach**, at any position, and that is why `built`
     * sits just above `extended` rather than far ahead: not every turn admits a subordinate
     * clause, and requiring one for an A would punish everyone for an occasion nobody gave.
     * `extended`, which every turn admits, gives ground a letter at a time instead.
     */
    @Test
    fun `the reach never demands a construction the turn may not admit`() {
        val reach = sheet("relevance/reach")
        val built = reach.notch("built")
        val extended = reach.notch("extended")
        (0 until POSITIONS).forEach { position ->
            val window = reach.windowAt(position)
            assertEquals("at position $position", Letter.A, window.noteOf(built).letter)
            assertTrue("at position $position",
                       window.noteOf(extended).value >= window.noteOf(reach.notch("plain")).value)
        }
    }

    private fun Sheet.notch(name: String): Float =
        (reading as Reading.Column).notches.first { it.name == name }.value

    // ── The two sound sheets ────────────────────────────────────────────────────────────

    /**
     * The inversion test, for what it really proves: **a single sheet cannot render both
     * verdicts, whatever its series.** A series is monotone -- it rescales, it never reorders
     * -- so if the two readings put two learners in the opposite order, no setting of one
     * sheet reaches both.
     *
     * A: a thick accent, perfectly understandable -- fifty sounds all some twenty points out,
     * none past the gross-miss line. B: a clean accent and two sounds completely wrong.
     */
    @Test
    fun `no single sheet renders both sound verdicts`() {
        val thickAccent = List(50) { 20f }
        val twoWrong = List(48) { 3f } + listOf(95f, 95f)

        val intelligibility = sheet("pronunciation/intelligibility")
        val proximity = sheet("pronunciation/proximity")

        val understood = { sounds: List<Float> -> sounds.count { it < 30f } / sounds.size.toFloat() }
        val close = { sounds: List<Float> -> sounds.map { 1f - it / 100f }.average().toFloat() }

        assertTrue(understood(thickAccent) > understood(twoWrong))
        assertTrue(close(thickAccent) < close(twoWrong))

        // And it survives the passage into notes, at every sensitivity, which is the part that
        // matters: the order has to still be opposite once the series has had its say.
        (0 until POSITIONS).forEach { position ->
            val a = intelligibility.windowAt(position)
            val p = proximity.windowAt(position)
            assertTrue(
                "position $position",
                a.noteOf(understood(thickAccent)).value > a.noteOf(understood(twoWrong)).value,
            )
            assertTrue(
                "position $position",
                p.noteOf(close(thickAccent)).value < p.noteOf(close(twoWrong)).value,
            )
        }
    }

    // ── The flat aggregation ────────────────────────────────────────────────────────────

    private val sounds = sheet("pronunciation/intelligibility")
    private val melody = sheet("pronunciation/melody")
    private val correctness = sheet("correctness/correctness")

    /** Elocution 2 (sounds 1, melody 1) against correctness 1, and nothing else counts. */
    private val weights = Weights(
        buildMap {
            Sheets.all.forEach { put(Sheets.pathOf(it), 0f) }
            listOf("pronunciation", "understanding", "correctness", "relevance", "fluency")
                .forEach { put(it, 0f) }
            put("pronunciation", 2f)
            put("correctness", 1f)
            put(Sheets.pathOf(sounds), 1f)
            put(Sheets.pathOf(melody), 1f)
            put(Sheets.pathOf(correctness), 1f)
        }
    )

    /**
     * The doc's worked case, which is a claim of design and not an arithmetic to rediscover:
     * cascade 60 against flat 65. What the cascade does is hand correctness two thirds of the
     * sitting and elocution one third, the exact reverse of the 2:1 asked for.
     */
    @Test
    fun `flat is not the cascade, and the gap is not rounding`() {
        val first = Passage(
            keptWords = 1,
            difficulty = null,
            measured = listOf(direct(sounds, 0.40f), direct(melody, 0.80f), direct(correctness, 0.90f)),
        )
        // The word gate closed, so no sound was measured at all.
        val second = Passage(
            keptWords = 1,
            difficulty = null,
            measured = listOf(
                Measured(sounds, null),
                Measured(melody, null),
                direct(correctness, 0.50f),
            ),
        )
        // Pronunciation takes two thirds of the tree and correctness one, so its two sheets
        // carry a third each: (40 + 80 + 90 + 50) / 4, the ratio the author asked for applied
        // once.
        val flat = noteOver(listOf(first, second), weights, ::identityPosition)!!
        assertEquals(0.65f, flat.value, 1e-3f)

        // The cascade: passage 1 averages elocution to 60, then (2x60 + 90) / 3 = 70; passage
        // 2 renormalises over what is left and is worth 50; the sitting is 60. What the gap is
        // made of is that correctness ended up carrying two thirds of it.
        assertNotEquals(0.60f, flat.value, 1e-3f)
    }

    /**
     * What a weight means, and it is the whole reason the shares are normalised: the same
     * weight everywhere makes every aptitude count the same, whatever number of sheets each
     * one happens to have under it. Left absolute, pronunciation and fluency would have taken
     * eight elevenths of the note for having four sheets each.
     */
    @Test
    fun `the same weight everywhere gives every aptitude the same say`() {
        val even = Weights(Sheets.tree.children.flatMap { branch ->
            listOf(Sheets.pathOf(branch) to 1f) +
                (branch as Branch).children.map { Sheets.pathOf(it) to 1f }
        }.toMap())
        Sheets.tree.children.forEach { branch ->
            val share = (branch as Branch).children
                .filterIsInstance<Sheet>()
                .map { even.of(it) }
                .sum()
            assertEquals(branch.name, 1f / Sheets.tree.children.size, share, 1e-5f)
        }
    }

    /** All at 0 under a branch is nothing counting, never a division by zero. */
    @Test
    fun `a branch whose children are all at zero weighs nothing`() {
        val none = Weights(Sheets.tree.children.flatMap { branch ->
            listOf(Sheets.pathOf(branch) to 1f) +
                (branch as Branch).children.map { Sheets.pathOf(it) to 0f }
        }.toMap())
        assertEquals(0f, none.of(correctness), 0f)
    }

    @Test
    fun `an absent sheet leaves the sum and never counts as a zero`() {
        val withMelody = Passage(
            keptWords = 1, difficulty = null,
            measured = listOf(direct(sounds, 0.40f), direct(melody, 0.80f)),
        )
        val without = Passage(
            keptWords = 1, difficulty = null,
            measured = listOf(direct(sounds, 0.40f), Measured(melody, null)),
        )
        assertEquals(0.60f, noteOver(listOf(withMelody), weights, ::identityPosition)!!.value, 1e-4f)
        assertEquals(0.40f, noteOver(listOf(without), weights, ::identityPosition)!!.value, 1e-4f)
    }

    @Test
    fun `a passage that measured nothing has no note at all`() {
        val nothing = Passage(1, null, listOf(Measured(sounds, null), Measured(melody, null)))
        assertNull(noteOver(listOf(nothing), weights, ::identityPosition))
    }

    /** Following weighs on the difficulty of the AI turn, not on the length of the answer. */
    @Test
    fun `following weighs on the difficulty of the turn it answered`() {
        val following = sheet("understanding/uptake")
        val weights = Weights(
            buildMap {
                Sheets.all.forEach { put(Sheets.pathOf(it), 0f) }
                listOf("pronunciation", "understanding", "correctness", "relevance", "fluency")
                    .forEach { put(it, 0f) }
                put("understanding", 1f)
                put("correctness", 1f)
                put(Sheets.pathOf(following), 1f)
                put(Sheets.pathOf(correctness), 1f)
            }
        )
        // Twenty kept words against an easy turn: correctness carries twenty, following one.
        val easy = Passage(20, 0.05f, listOf(direct(following, 0.50f), direct(correctness, 1.00f)))
        val hard = Passage(20, 20f, listOf(direct(following, 0.50f), direct(correctness, 1.00f)))
        assertTrue(
            noteOver(listOf(hard), weights, ::identityPosition)!!.value <
                noteOver(listOf(easy), weights, ::identityPosition)!!.value
        )
    }

    /**
     * The figure this sheet has to show to be worth [note], so the worked case below reads in
     * the doc's own numbers -- sounds 40, melody 80, correctness 90 -- whatever unit each of
     * the three actually measures in.
     */
    private fun direct(sheet: Sheet, note: Float) =
        Measured(sheet, sheet.windowAt(identityPosition(sheet)).figureOf(Note(note)))

    private fun identityPosition(sheet: Sheet) = POSITIONS / 2
}
