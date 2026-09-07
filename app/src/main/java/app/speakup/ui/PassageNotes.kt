package app.speakup.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.speakup.R
import app.speakup.conversation.Utterance
import app.speakup.providers.words
import app.speakup.sheets.Branch
import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets
import app.speakup.ui.theme.Speakup
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The passage's notes: **one screen with two doors**, pushed between two passages or opened on
 * demand from any passage of the thread (`ui.md`). One content to design, one reading to
 * learn.
 *
 * It carries, **per aptitude, every sheet**: its raw measure, and its letter where the mode
 * uses notes. **What the access layer cuts is the letters, never the measures** -- so in a free
 * conversation the screen exists and carries *2.3 semitones*, *47 sounds out of 50*, *22%
 * slower*, facts about what was just said that depend on no setting and that one wants even
 * with nothing at stake. Free is the only door written, so no letter is drawn here yet; the
 * column is what a mode with notes turns on (`../../../../../../TODO.md`).
 *
 * **A row with no measure says *not measured***, and it is drawn rather than dropped: the list
 * of sheets is fixed, so a missing row could not be told from a sheet nobody drew. A passage
 * whose words' gate closed shows four empty pronunciation rows, which is the truth about it.
 *
 * **No trend.** An arrow saying *"+1 since the last passage"* would compare two readings taken
 * under rules that have no reason to be the same, and a note is never read without the
 * combination that produced it. It shows one take, the passage's last, like the thread.
 *
 * Its `OK` is the action bar's, which is why it stays visible whatever the scroll: sixteen rows
 * of content do not fit a short screen, and a button in the flow would scroll away with them.
 */
@Composable
fun PassageNotes(
    /** The attempt being read, which is the passage's last -- what one knows how to say now. */
    attempt: Utterance,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    val colors = markingColors()
    if (attempt.measured.isEmpty()) {
        Text(
            stringResource(R.string.passage_notes_none),
            modifier = modifier.padding(grid.cell),
            style = type.text,
            color = palette.dim.srgb,
        )
        return
    }
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = grid.cell),
    ) {
        Sheets.tree.children.filterIsInstance<Branch>().forEach { aptitude ->
            Text(
                stringResource(nameOfAptitude(aptitude.name)),
                modifier = Modifier.padding(top = grid.cell),
                style = type.text,
                color = palette.ink.srgb,
                maxLines = 1,
            )
            aptitude.children.filterIsInstance<Sheet>().forEach { sheet ->
                SheetRow(Sheets.pathOf(sheet), sheet, attempt, colors)
            }
        }
    }
}

/**
 * One sheet's line: **the name on the left, the measure aligned right**.
 *
 * The measure runs leftwards for as far as the name allows, which is what makes *between the
 * lines* -- sixteen columns -- fit on a row whose name takes seven, and so what saves having to
 * invent short names for the six notches of the following.
 */
@Composable
private fun SheetRow(path: String, sheet: Sheet, attempt: Utterance, colors: MarkingColors) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    Row(Modifier.fillMaxWidth().padding(start = grid.cell)) {
        Text(
            stringResource(nameOfSheet(path)),
            style = type.text,
            color = palette.dim.srgb,
            maxLines = 1,
        )
        val slices = slicesOf(path, attempt, colors)
        if (slices != null) {
            NotchBar(slices, Modifier.weight(1f))
            return@Row
        }
        Text(
            measureOf(path, sheet, attempt).orEmpty(),
            modifier = Modifier.weight(1f),
            style = type.text,
            // Not measured is the empty string and no sign of its own, exactly as on the line
            // that names a turn: the slot is fixed, so its emptiness is what carries.
            color = palette.ink.srgb,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The bar of one column sheet, paved with the font's gauge blocks.
 *
 * **The three column sheets show the count per notch and not the sheet's figure**, which is a
 * mean between 0 and 1 that says nothing to anyone. It is the same data read otherwise, not a
 * second source.
 *
 * **The empty part of a bar is a colour and not a shape**: the gauge painted in the dimmed ink,
 * with no hollow to draw. And the bar **reuses the marking's ramp** -- the red that underlined
 * *not said* in the turn counts its words here -- so the summary and the marked turn speak the
 * same language of colour, with nothing to learn in going from one to the other.
 *
 * A vertical column is not possible and that is why this is horizontal: the gauge paves because
 * it takes all eleven columns of the cell, gutter included, and there is no vertical equivalent
 * -- the box is fourteen rows and the line step fifteen, so an empty row always separates two
 * stacked cells and a column would break into stumps.
 */
@Composable
private fun NotchBar(slices: List<Slice>, modifier: Modifier = Modifier) {
    val type = Speakup.type
    Row(modifier, horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
        cells(slices).forEach { (colour, cells) ->
            if (cells > 0) {
                Text(
                    Glyphs.GAUGE_FULL.toString().repeat(cells),
                    style = type.text,
                    color = colour,
                    maxLines = 1,
                )
            }
        }
    }
}

/** One notch's share of a bar: what it is painted with, and how many words carry it. */
private data class Slice(val colour: Color, val count: Int)

/**
 * The slices spread over [BAR_CELLS] whole cells, by largest remainder.
 *
 * Whole cells and not quarters, because two colours meeting inside one cell cannot be drawn:
 * a partial block is left-aligned in its cell, so the next colour would start a cell further on
 * and leave a gap in a bar the doc wants paved. The resolution is coarse and honest -- a bar of
 * five says fifths, and whoever wants finer reads the marks on the turn itself.
 */
private fun cells(slices: List<Slice>): List<Pair<Color, Int>> {
    val total = slices.sumOf { it.count }
    if (total == 0) return emptyList()
    val exact = slices.map { it.count.toFloat() * BAR_CELLS / total }
    val floors = exact.map { it.toInt() }.toMutableList()
    // The cells rounding left over go to the largest remainders, so the bar is always exactly
    // BAR_CELLS wide and no notch with words in it can round away to nothing unfairly.
    var left = BAR_CELLS - floors.sum()
    exact.indices.sortedByDescending { exact[it] - floors[it] }.forEach { index ->
        if (left > 0) { floors[index]++; left-- }
    }
    return slices.indices.map { slices[it].colour to floors[it] }
}

/**
 * The notch counts of the three column sheets, or null on a sheet that shows a measure.
 *
 * The notches are listed **worst first**, so a bar reads from the alarm towards the calm and
 * two bars of different sheets are compared at a glance.
 */
@Composable
private fun slicesOf(path: String, attempt: Utterance, colors: MarkingColors): List<Slice>? {
    val marked = attempt.judged?.words() ?: return null
    fun count(words: List<app.speakup.judged.Word>, notch: String) =
        words.count { it.notch == notch }
    return when (path) {
        CORRECTNESS -> marked.correctness.let {
            listOf(
                Slice(colors.ramp.last(), count(it, "not-said")),
                Slice(colors.ramp[1], count(it, "malformed")),
                Slice(colors.dim, count(it, "ok")),
            )
        }
        RELEVANCE -> marked.relevance.let {
            listOf(
                Slice(colors.ramp.last(), count(it, "off-target")),
                Slice(colors.ramp[1], count(it, "flat")),
                Slice(colors.dim, count(it, "ok")),
                Slice(colors.apt, count(it, "apt")),
            )
        }
        STUMBLING -> marked.stumbling.let {
            listOf(
                Slice(colors.ramp.last(), count(it, "abandoned")),
                Slice(colors.ramp[1], count(it, "filler")),
                Slice(colors.dim, count(it, "kept")),
            )
        }
        else -> null
    }
}

/**
 * What one sheet's raw measure reads as, or null where nothing measured it.
 *
 * **Three rules behind the units.** A **fraction rather than a percentage** where the sheet
 * counts elements: `47/50` says what `94%` hides, that the turn held fifty sounds -- on a turn
 * of three, the percentage would be a lie of precision. A **sign wherever the measure is a
 * gap**, `+` for more than the model and `-` for less, so `-22 %` reads *22% slower*. And **the
 * unit names the thing measured and never the arithmetic**: continuity's figure is a share of
 * the turn minus the model's share of the same sentence, so it reads *+16% silence* -- `+16
 * pts` named the unit of a subtraction and said nothing about speech. The precision's "points"
 * are an internal unit and are written with no unit at all.
 */
@Composable
private fun measureOf(path: String, sheet: Sheet, attempt: Utterance): String? {
    val figure = attempt.measured[path]
    return when (path) {
        // The two that count elements. The counts come off the reading itself rather than off
        // the figure, a share alone having lost the denominator that is the point of a fraction.
        INTELLIGIBILITY -> attempt.sounds.takeIf { it.isNotEmpty() && figure != null }?.let {
            stringResource(R.string.measure_fraction, it.count { s -> s.points < SATURATES },
                           it.size)
        }
        STRESS -> attempt.marking?.syllables?.filter { it.modelStressed }
            ?.takeIf { it.isNotEmpty() && figure != null }?.let {
                stringResource(R.string.measure_fraction, it.count { s -> s.learnerStressed },
                               it.size)
            }
        PROXIMITY -> figure?.let {
            stringResource(R.string.measure_plain, (it * PER_CENT).roundToInt())
        }
        MELODY -> figure?.let { stringResource(R.string.measure_semitones, one(it)) }
        // The notch's own words, which is what the following renders: it is a judgement over
        // the whole passage and has no figure to print.
        UPTAKE -> attempt.judged?.following?.let { stringResource(nameOfNotch(it)) }
        // **In percent of silence and not in "points".** The figure is the share of the turn
        // spent silent minus the model's share on the same sentence, so it is already a
        // percentage of the turn; "+16 pts" named the unit of a subtraction and said nothing
        // about speech. "+16% silence" says what it is: a sixth of the turn quiet that the
        // model did not spend quiet.
        CONTINUITY -> figure?.let { stringResource(R.string.measure_silence, signed(it)) }
        LONGEST_SILENCE -> figure?.let { stringResource(R.string.measure_seconds, one(it)) }
        // The distance and the side, which are two facts: the figure is symmetric by
        // construction, so the sign comes from the column that says which way it fell.
        PACE -> figure?.let { far ->
            attempt.slower?.let { stringResource(R.string.measure_percent, signed(if (it) -far else far)) }
        }
        else -> null
    }
}

private fun one(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

private fun signed(value: Float): String =
    (if (value < 0f) "-" else "+") + abs(value).roundToInt()


/** How wide a notch bar is, in cells. Five columns of the twenty-eight the worst screen gives. */
private const val BAR_CELLS = 5

private const val PER_CENT = 100
