package app.saylune.notes

import app.saylune.sheets.Direction
import app.saylune.sheets.POSITIONS
import app.saylune.sheets.Reading
import app.saylune.sheets.Sheet
import app.saylune.sheets.Unit

/**
 * A sensitivity position is a **window of four consecutive bounds** in the sheet's series.
 *
 * What the app reads to turn a figure into a note is a table of four bounds, in the sheet's
 * own unit, read as they stand. **Nothing is computed between sheets**: every bound in play is
 * a value written by hand. A single transformation -- "severe is the bounds halved" -- was
 * tried and does not hold: the same gesture soundly tightens gross misses and breaks melody,
 * where two semitones become one, that is, under the machine's own noise band.
 *
 * The four bounds are what the five letters are cut at, so **one notch of sensitivity is worth
 * exactly one letter**, on the sounds as on the silence as on the rate: moving the window by
 * one slides every bound to its neighbour, and what was the C/D bound becomes the D/E one.
 * That is unitless by construction, where "halved" was not -- nothing is computed across two
 * units, one moves one rank down a list.
 */
class Window(private val sheet: Sheet, val position: Int) {

    init {
        require(position in 0 until POSITIONS) { "no sensitivity position $position" }
        requireNotNull(sheet.series) { "${sheet.name}: no series, so no sensitivity" }
    }

    /** The four bounds, most lenient first, in the sheet's unit. */
    val bounds: List<Float> = sheet.series!!.subList(position, position + LETTERS_CUT)

    /**
     * The note this figure earns.
     *
     * The bounds are where the letter bands are cut, so a bound lands on a band edge: the most
     * lenient on E/D, then D/C, C/B, and the strictest on B/A. Between two bounds the reading
     * is straight, so the quarters of a band -- the modifier -- are the quarters of the
     * interval between two written values.
     *
     * **Past either end it keeps climbing rather than clipping**, at the slope of the last
     * band it has: a continuity gap that is negative -- keeping quieter than the model -- lands
     * above the A bound and stays in A, and a figure far past the strictest bound still says
     * more than one that just cleared it.
     */
    fun noteOf(figure: Float): Note {
        val t = towardsGood(figure)
        val edges = bounds.map(::towardsGood)
        // Which of the three written bands it sits in. Outside them the nearest band's own
        // slope carries on, which is what makes it climb past either end instead of clipping.
        val band = when {
            t < edges[1] -> 0
            t < edges[2] -> 1
            else -> 2
        }
        val within = (t - edges[band]) / (edges[band + 1] - edges[band])
        return Note((BAND * (band + 1) + within * BAND).coerceIn(0f, 1f))
    }

    /**
     * The figure that earns exactly this note: the reading run backwards.
     *
     * What the custom screen and the calibration bench both want to ask -- *what figure is a
     * B here?* -- and the one honest way to ask it, since the bounds are written in the
     * sheet's own unit and nothing else translates between two units.
     */
    fun figureOf(note: Note): Float {
        val band = ((note.value / BAND).toInt() - 1).coerceIn(0, 2)
        val within = (note.value - BAND * (band + 1)) / BAND
        val edges = bounds.map(::towardsGood)
        return towardsGood(edges[band] + within * (edges[band + 1] - edges[band]))
    }

    /** A coordinate that climbs towards the sheet's good end, whichever way its unit runs. */
    private fun towardsGood(figure: Float): Float =
        if (sheet.direction == Direction.LowIsGood) -figure else figure

    /**
     * The letters no figure this sheet can produce would ever reach, under this window.
     *
     * **An impossible edge shows while writing the series** rather than at run time on a
     * learner who cannot understand why A is out of reach. What it needs is the range the
     * figure can actually take, which the sheet declares: a share never leaves 0..1, and a
     * sheet whose single element carries a notch can only ever land on one of those notches.
     */
    fun unreachable(): Set<Letter> = Letter.entries.toSet() - reachable()

    private fun reachable(): Set<Letter> = when (val figures = figures(sheet)) {
        is Figures.Values -> figures.of.map { noteOf(it).letter }.toSet()
        is Figures.Range -> {
            val low = noteOf(figures.from).letter
            val high = noteOf(figures.to).letter
            // The reading is monotone, so everything between the two ends is reached too.
            Letter.entries.filter { it >= minOf(low, high) && it <= maxOf(low, high) }.toSet()
        }
    }

    companion object {
        /** Four bounds cut five bands. */
        const val LETTERS_CUT = 4
    }
}

/** What figures a sheet can hand out at all -- an interval, or a handful of values. */
private sealed interface Figures {
    data class Range(val from: Float, val to: Float) : Figures
    data class Values(val of: List<Float>) : Figures
}

private fun figures(sheet: Sheet): Figures = when (sheet.unit) {
    // The mean of qualities each between 0 and 1, so it is one too.
    Unit.Share -> Figures.Range(0f, 1f)
    // One element carrying a notch: the figure is that notch's value and nothing between.
    Unit.NotchValue -> Figures.Values((sheet.reading as Reading.Column).notches.map { it.value })
    // A distance, a length, a speed gap: never negative, and nothing bounds the far end.
    Unit.Semitones, Unit.Seconds, Unit.Percent ->
        Figures.Range(0f, Float.POSITIVE_INFINITY)
    // A difference of two shares, so it runs the whole way in both directions.
    Unit.PercentPoints -> Figures.Range(-100f, 100f)
    Unit.Times -> error("${sheet.name}: a count gives no note")
}

/** The window this sheet reads at [position]. */
fun Sheet.windowAt(position: Int): Window = Window(this, position)
