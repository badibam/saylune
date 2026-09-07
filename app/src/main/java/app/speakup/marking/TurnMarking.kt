package app.speakup.marking

/**
 * What the analysis found on one turn, anchored to the exact string that was sent to the
 * engine. Every index below is an offset into [TurnMarking.text]; displaying a different
 * string than the one submitted would slide every mark.
 */
data class TurnMarking(
    val text: String,
    val syllables: List<Syllable>,
    val phonemes: List<PhonemeDeviation>,
    val words: List<WordFault>,
    val added: List<AddedSound>,
    val gutters: List<Gutter>,
) {
    /** Pitch bounds across both contours, with a little air so neither hugs the band edge. */
    fun pitchBounds(): ClosedFloatingPointRange<Float> {
        val values = syllables.flatMap { listOfNotNull(it.modelPitch, it.learnerPitch) }
        // A semitone and a half of air, and the band is never narrower than a fifth: a turn
        // said almost evenly would otherwise be stretched over the whole height and read as
        // dramatic, which is the one thing it is not.
        val lo = minOf(values.minOrNull() ?: 0f, -3.5f) - 1.5f
        val hi = maxOf(values.maxOrNull() ?: 0f, 3.5f) + 1.5f
        return lo..hi
    }

    fun modelContour(): Contour =
        Contour(syllables.map { it.center to it.modelPitch })

    fun learnerContour(): Contour =
        Contour(syllables.mapNotNull { s -> s.learnerPitch?.let { s.center to it } })

    /** False across a syllable whose pitch the harmonic-lock filter threw away. */
    fun learnerPitchKnownAt(offset: Int): Boolean =
        syllables.none { offset in it.start until it.end && it.learnerPitch == null }

    companion object {
        /**
         * A turn read on the word channel alone: its text, and nothing measured on the sound.
         *
         * **This is what a refused turn carries.** The sound analysis does not run on a phrase
         * about to be reworded, or on one holding a word that does not exist -- but the turn
         * was read, by the judge, and what the judge marked is the reason it is being sent
         * back. Empty lists here are the truth about it and not a placeholder: nothing
         * measured the sounds, so there is nothing to draw on them.
         */
        fun wordsOnly(text: String) = TurnMarking(
            text = text,
            syllables = emptyList(),
            phonemes = emptyList(),
            words = emptyList(),
            added = emptyList(),
            gutters = emptyList(),
        )
    }
}

/**
 * One syllable. Stress is binary by nature: it sits on the right syllable or it does not.
 * Both stress flags come from the engine reading the model and the learner — never from a
 * dictionary, which the engine contradicts on 41% of polysyllabic words.
 *
 * **The two pitches are in semitones, each side centred on its own middle** — not hertz. A
 * synthetic voice and a learner do not share a register, so a raw pitch would put the two
 * contours on separate parts of the band and call every man imitating a woman wrong at every
 * syllable. Neither side is scaled: a learner speaking flat where the model swings is a
 * fault to show, and dividing by the spread would erase it before the two are compared.
 *
 * [learnerPitch] is null where nothing of the syllable was voiced, which is **not** the same
 * as flat and must not be drawn alike.
 */
data class Syllable(
    val start: Int,
    val end: Int,
    val modelPitch: Float,
    val learnerPitch: Float?,
    val modelStressed: Boolean,
    val learnerStressed: Boolean,
) {
    val center: Float get() = (start + end) / 2f

    val stressStrayed: Boolean get() = learnerStressed && !modelStressed
    val stressMissing: Boolean get() = modelStressed && !learnerStressed
}

/** How far below the model one phoneme fell, in engine points, over the letters it covers. */
data class PhonemeDeviation(val start: Int, val end: Int, val points: Float)

/**
 * A word not one sound of which came through: `[start, end)` covers the whole of it.
 *
 * Binary, and deliberately so. The per-sound ramp grades because a sound can be a little
 * off; a word every sound of which is at fault is not a little off, and the reader needs to
 * see one thing rather than five. It is also the only mark that reaches a word's **silent
 * letters**, which carry no sound and so can never be tinted by the ramp -- without it, a
 * word swapped for another lights up in patches and reads as a pronunciation slip.
 *
 * No threshold of its own: a word is at fault when every one of its compared sounds is over
 * the same bar the sounds already answer to. One bar, no second number to tune.
 */
data class WordFault(val start: Int, val end: Int)

/**
 * A stretch the learner said that belongs to no word of the text.
 *
 * **Binary, and that is structural rather than a choice.** Every other mark is born of two
 * spreads compared; added matter has no model side at all, so there is nothing to compare
 * and no degree to report. The mass the network puts on the added symbol exists, but it is a
 * reading of one recording, which is the one thing the measure never does.
 *
 * **It never sits on a letter, and that too is structural.** A sound that lands on a letter
 * belongs to the word that letter spells, and the sound marks already speak for it; what is
 * left over is exactly what no letter could write. So the mark belongs **between two
 * letters**, just after [after], the way a gutter's does: a fault that is found must not
 * vanish for want of somewhere to paint it.
 *
 * [symbol] holds every sound of one stretch, space-separated -- a whole word said in
 * addition is a burst of loose sounds at one word boundary, and it is one thing that
 * happened rather than four.
 *
 * [after] is the seam it sits in: the offset of the character it comes just after, -1 before
 * the first of them.
 *
 * **One position and no second one.** It used to carry the readout line it followed as well,
 * which is derivable from [after] and drifted from it: the line was counted over the sounds
 * that hold letters, while the readout draws a row for every sound, so a mark drawn after
 * `I'm` under the phrase was written at the very top of the table. Both views place it from
 * this one number now -- immediately before the first sound that claims a character past it.
 */
data class AddedSound(
    val symbol: String,
    val after: Int,
)

/**
 * A sound of the model that no letter of the text can carry, sitting after the character at
 * [after].
 *
 * English writes some sounds with nothing -- the schwa of `doesn't`, the `ɑ n` a reduced
 * `I'm` comes back as -- and a word whose sounds are all of that kind has no letter any mark
 * could be drawn on. The gap is measured all the same, so it goes in the seam between the
 * letters its neighbours claimed rather than onto one of them, which would accuse a letter
 * that was said correctly.
 *
 * It carries [points] where [AddedSound] carries none, and that is the difference between
 * the two: a gutter has both recordings to compare, an added sound has only one. So it is
 * drawn on the same ramp as every other sound mark, and like them it is silent below the
 * band -- a letter under the bar keeps the neutral ink, which is to say no mark appears, and
 * a gutter has no glyph to keep, so its equivalent is nothing at all. A clean turn carries
 * none. Every gutter is still **carried** here rather than filtered on the way in: the bar
 * belongs to the screen, and an analysis that dropped them could not be asked later what it
 * had found.
 *
 * [after] is -1 when the sound precedes every letter of the turn. It is the position of the
 * last letter any sound claimed, not of this one: a gutter has no letters, so its own spots
 * say nothing, and reading them put every gutter at the front of the sentence.
 */
data class Gutter(val symbol: String, val after: Int, val points: Float)

/**
 * The pitch contour, sampled in character coordinates. Control points sit at syllable
 * centres and the curve smooths between them, so the line neither breaks at every syllable
 * nor claims a resolution the engine does not have: it reports one pitch per syllable.
 */
class Contour(private val points: List<Pair<Float, Float>>) {

    val isEmpty: Boolean get() = points.isEmpty()

    fun at(x: Float): Float {
        if (points.isEmpty()) return 0f
        if (x <= points.first().first) return points.first().second
        if (x >= points.last().first) return points.last().second
        for (i in 0 until points.size - 1) {
            val (x0, v0) = points[i]
            val (x1, v1) = points[i + 1]
            if (x <= x1) {
                val t = (x - x0) / (x1 - x0)
                return v0 + (v1 - v0) * smoothStep(t)
            }
        }
        return points.last().second
    }

    private fun smoothStep(t: Float) = t * t * (3f - 2f * t)
}
