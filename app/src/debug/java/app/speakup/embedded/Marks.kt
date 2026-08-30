package app.speakup.embedded

import app.speakup.analysis.Gutter
import app.speakup.marking.PhonemeDeviation
import app.speakup.marking.WordFault

/**
 * The gaps and the letters, turned into what the screen draws.
 *
 * Its own object rather than a step inside the analysis, so a test can hold this exact
 * assembly against what the bench writes. The engine is checked elsewhere; what has never
 * been checked is the arithmetic on top of it, and that is only worth checking if the thing
 * under test is the thing that runs.
 */
object Marks {

    /**
     * A gap is a divergence between two spreads, in [0, 1]; the ramp of `MarkingColors.kt`
     * reads points, ignores anything under 5 and saturates at 30. A hundred is the plainest
     * transform there is, and it is deliberately not tuned to make the picture pretty: the
     * labelled set puts controls at 0.003 and faults above 0.93, so this saturates every
     * fault, and that is a fact to look at rather than to hide.
     */
    const val POINTS = 100f

    class Drawn(
        val phonemes: List<PhonemeDeviation>,
        val gutters: List<Gutter>,
        val words: List<WordFault>,
    )

    fun drawn(gaps: List<Overlap.Gap>, sounds: List<Sound>, band: Float): Drawn {
        val phonemes = mutableListOf<PhonemeDeviation>()
        val gutters = mutableListOf<Gutter>()
        for (gap in gaps) {
            val sound = sounds[gap.rank]
            val points = gap.value * POINTS
            // A sound that holds no letter borrows a neighbour's; one that can borrow none
            // marks the gutter, which no character range can express.
            val where = sound.spots.ifEmpty { sound.borrowed }
            if (where.isEmpty()) {
                gutters.add(Gutter(sound.symbol, sound.spots.maxOrNull() ?: 0, points))
            } else {
                phonemes.add(PhonemeDeviation(where.min(), where.max() + 1, points))
            }
        }
        return Drawn(phonemes, gutters, faulty(gaps, sounds, band))
    }

    /**
     * The words no sound of which came through.
     *
     * Only sounds that were **compared** count: a dropped one says nothing either way, and
     * treating it as clean would clear a word on the strength of a reading that never
     * happened. A word all of whose sounds were dropped has no verdict at all.
     */
    private fun faulty(gaps: List<Overlap.Gap>, sounds: List<Sound>, band: Float): List<WordFault> =
        gaps.groupBy { sounds[it.rank].wordAt }
            .mapNotNull { (word, its) ->
                if (word == null || its.any { it.value * POINTS <= band }) null
                else WordFault(word.first, word.last + 1)
            }
            .sortedBy { it.start }
}
