package app.saylune.notes

import app.saylune.analysis.Analysed
import app.saylune.fluency.Fluency
import app.saylune.fluency.Turn
import app.saylune.judged.Judgement
import app.saylune.judged.Word
import app.saylune.providers.words
import app.saylune.sheets.Reading
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets

/**
 * What one attempt makes of every sheet: a figure, or the fact that it has none.
 *
 * **A sheet that did not measure is absent, and it never counts as a zero.** That is not a
 * detail of this file, it is what the flat aggregation rests on: a cascade of means silently
 * redistributes weights nobody set as soon as a sheet is missing, and one is missing all the
 * time -- the lexical stress is not wired into the sum, a passage whose words' gate closed has
 * no sound at all, a turn with no kept word has nothing to compare a rate against.
 *
 * **The figure is the mean of the elements**, and a proportion is the mean of a 0/1, so there
 * is one recipe and not two. Where a sheet has a **single element** there is nothing to average
 * and the figure keeps its raw unit, the series reading it directly.
 */
object Sheeting {

    /**
     * Every sheet of the tree, read off what this attempt produced.
     *
     * [judged] is null on an attempt nothing judged -- a repeat, which never reaches the
     * language model. [analysed] is null wherever the sound analysis did not run: the words'
     * gate closed, or a word is unsayable. Each absence takes its sheets out of the sum rather
     * than filling them with a zero.
     */
    fun of(judged: Judgement?, analysed: Analysed?, timed: Turn?): List<Measured> =
        Sheets.all.filter { Sheets.scoredPathOf(it) != null }.map { sheet ->
            Measured(sheet, figure(sheet, judged, analysed, timed))
        }

    private fun figure(
        sheet: Sheet, judged: Judgement?, analysed: Analysed?, timed: Turn?,
    ): Float? = when (Sheets.scoredPathOf(sheet)) {
        "pronunciation/intelligibility" -> analysed?.let { sounds(it, sheet) }
        "pronunciation/proximity" -> analysed?.let { sounds(it, sheet) }
        "pronunciation/melody" -> analysed?.let { melody(it) }
        "pronunciation/lexical-stress" -> analysed?.let { stress(it) }
        "understanding/uptake" -> judged?.let { notch(sheet, it.following) }
        "correctness/correctness" -> judged?.let { column(sheet, it.words().correctness) }
        "relevance/relevance" -> judged?.let { column(sheet, it.words().relevance) }
        "fluency/continuity" -> timed?.let { Fluency.continuity(it) }
        "fluency/longest-silence" -> timed?.let { Fluency.longestSilence(it) }
        "fluency/pace" -> timed?.let { Fluency.rate(it) }
        "fluency/stumbling" -> judged?.let { column(sheet, it.words().stumbling) }
        else -> null
    }

    /**
     * The sounds, read through whichever ramp this sheet declares.
     *
     * **Two sheets on the same elements, told apart by their ramp alone**, and the inversion
     * test is what separates them: a thick accent that stays perfectly understandable beats an
     * accent-free learner with two sounds outright wrong on one reading and loses on the other,
     * so no setting of a single sheet renders both verdicts.
     */
    private fun sounds(analysed: Analysed, sheet: Sheet): Float? {
        val points = analysed.marking.phonemes.map { it.points }
        if (points.isEmpty()) return null
        return when (val reading = sheet.reading) {
            is Reading.Cliff -> points.count { it < reading.at }.toFloat() / points.size
            is Reading.Slope -> points
                .map { 1f - ((it - reading.from) / (reading.to - reading.from)).coerceIn(0f, 1f) }
                .average().toFloat()
            else -> null
        }
    }

    /**
     * The melody: the mean gap in semitones, on the syllables where both sides were voiced.
     *
     * A syllable nothing voiced on the learner's side has no contour to be departed from, so it
     * drops out rather than being counted as a gap of zero -- which would read as a perfect
     * match on a syllable nobody said.
     */
    private fun melody(analysed: Analysed): Float? {
        val gaps = analysed.marking.syllables.mapNotNull { syllable ->
            syllable.learnerPitch?.let { kotlin.math.abs(syllable.modelPitch - it) }
        }
        return if (gaps.isEmpty()) null else gaps.average().toFloat()
    }

    /**
     * The lexical stress: the share of eligible words the beat landed right on.
     *
     * The denominator is **the words the model stresses clearly**, at the probe margin an ear
     * validated -- not "the words": a monosyllable has no choice of stress, and a function word
     * has no clear beat even in the model.
     */
    private fun stress(analysed: Analysed): Float? {
        val eligible = analysed.marking.syllables.filter { it.modelStressed }
        if (eligible.isEmpty()) return null
        return eligible.count { it.learnerStressed }.toFloat() / eligible.size
    }

    /**
     * A judged marking, unfolded onto its words and averaged.
     *
     * **One sheet and not one per notch**, because the notches share the same words: they are
     * slices of one cake and not independent measures. Separate sheets would force each to
     * render a figure even when nothing was marked in its colour, and a rare notch would sit at
     * its extreme value on nearly every passage, moving the whole scale of the node.
     */
    private fun column(sheet: Sheet, words: List<Word>): Float? {
        if (words.isEmpty()) return null
        val column = sheet.reading as? Reading.Column ?: return null
        return words.map { word -> column.notches.first { it.name == word.notch }.value }
            .average().toFloat()
    }

    /** A sheet whose single element is the passage: the notch's own value, nothing averaged. */
    private fun notch(sheet: Sheet, name: String): Float? =
        (sheet.reading as? Reading.Column)?.notches?.firstOrNull { it.name == name }?.value
}
