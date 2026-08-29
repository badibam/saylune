package app.speakup.embedded

/**
 * How far the learner sits from the model, sound by sound.
 *
 * Two things are never done here, and both are the measure's whole honesty.
 *
 * Nothing is ever compared to a note. Each side yields a **spread** -- at that instant, the
 * resemblance to every sound of English -- and the two spreads are compared entire.
 * `R 0.90 / W 0.10` and `R 0.90 / ER 0.10` share a peak and do not say the same thing.
 *
 * And nothing is done to one audio that is not done to the other. The machine's own bias
 * cancels because it is the same on both sides; a gain, a denoiser, a normalisation applied
 * to the take and not to the model would make part of the measured gap manufactured.
 */
object Overlap {

    /**
     * A sound whose frames are mostly silence has nothing to compare: the spread would be
     * read off the gaps rather than off the speech.
     */
    const val EMPTY_MASS = 0.9f

    /** Where the model puts a sound, and how far the other recording sits from it. */
    data class Gap(
        val value: Float,
        val symbol: String,
        /** Which sound of the model's grid this is: the list is shorter than the grid. */
        val rank: Int,
        /** Where the model says it. */
        val at: IntRange,
        /** Where the *other* recording says it -- the only way back to audio to listen to. */
        val span: IntRange,
        /**
         * The heaviest shares of each spread, which is what the measure actually compares.
         *
         * Kept so the two can be *looked at* rather than taken on the word of one number:
         * `R .90 / W .10` and `R .90 / ER .10` share a peak and do not say the same thing,
         * and no divergence written out alone will ever show that.
         *
         * The topmost symbol of each side reads like a verdict and is not one. Naming the
         * sound produced is the least reliable thing an acoustic machine renders, and the
         * app depends on none of it: the mark is born of the gap between the two whole
         * shapes (`docs/reference.md`).
         */
        val model: List<Share>,
        val said: List<Share>,
    )

    /** One sound's share of a spread. */
    data class Share(val symbol: String, val part: Float)

    /** How many shares of each side are kept: enough to read the shape, not the whole table. */
    const val SHARES = 4

    /** The mean spread over a span, silence dropped and the rest renormalised. */
    private fun spread(frames: Frames, from: Int, until: Int, alphabet: Alphabet): Spread? {
        val mean = frames.mean(from, until)
        val empty = mean[alphabet.blank]
        val speech = FloatArray(alphabet.spoken.size) { mean[alphabet.spoken[it]] }
        val total = speech.sum()
        if (total <= 0f) return null
        for (index in speech.indices) speech[index] /= total
        return Spread(speech, empty)
    }

    private class Spread(val over: FloatArray, val empty: Float)

    /** Jensen-Shannon between two spreads, in bits: 0 identical, 1 disjoint. */
    fun divergence(first: FloatArray, second: FloatArray): Float {
        var sum = 0.0
        for (index in first.indices) {
            val middle = 0.5 * (first[index] + second[index])
            if (middle <= 0.0) continue
            if (first[index] > 0f) sum += 0.5 * first[index] * log2(first[index] / middle)
            if (second[index] > 0f) sum += 0.5 * second[index] * log2(second[index] / middle)
        }
        return sum.toFloat()
    }

    private fun log2(value: Double) = Math.log(value) / LN2

    private val LN2 = Math.log(2.0)

    /**
     * Every sound of the model's grid, and how far the other voice sits from it.
     *
     * A sound the alignment could not place, or whose span holds no speech on either side,
     * is left out -- with [Reading.dropped] counting them, because a reading quietly short
     * of what it measured cannot be told from a clean one.
     */
    fun sounds(model: Frames, said: Frames, alphabet: Alphabet): Reading {
        val segments = Grid.decode(model, alphabet)
        val ids = IntArray(segments.size) { segments[it].symbol }
        val spans = Grid.align(said, ids, alphabet.blank)

        val gaps = mutableListOf<Gap>()
        var dropped = 0
        for (rank in segments.indices) {
            val span = spans[rank]
            if (span == null) { dropped++; continue }
            val here = spread(model, segments[rank].start, segments[rank].stop, alphabet)
            val there = spread(said, span.first, span.last + 1, alphabet)
            if (here == null || there == null) { dropped++; continue }
            if (here.empty > EMPTY_MASS || there.empty > EMPTY_MASS) { dropped++; continue }
            gaps.add(
                Gap(
                    value = divergence(here.over, there.over),
                    symbol = alphabet[segments[rank].symbol],
                    rank = rank,
                    at = segments[rank].start until segments[rank].stop,
                    span = span,
                    model = shares(here.over, alphabet),
                    said = shares(there.over, alphabet),
                )
            )
        }
        return Reading(gaps, segments.size, dropped)
    }

    /**
     * Peaks turned into a covering of the time, the gaps split down the middle.
     *
     * The network is peaky: it says "here" on one or two frames and leaves the rest to the
     * blank, so a sound arrives as an instant where what is wanted is a stretch -- an
     * extract to replay, a duration to find absurd. Giving every peak the silence up to
     * half way to its neighbour turns the grid into a covering.
     *
     * **Nothing in the measure reads these bounds.** They are the sound's own geometry, for
     * replaying and for looking at; the spreads are still read off the raw spans, and
     * widening them would change what is compared.
     */
    fun widened(spans: List<IntRange>): List<IntRange> = spans.mapIndexed { at, span ->
        val low = if (at > 0) (spans[at - 1].last + 1 + span.first) / 2 else span.first
        val high = if (at < spans.size - 1) (span.last + spans[at + 1].first) / 2 else span.last
        low..maxOf(low, high)
    }

    /** The heaviest [SHARES] of a spread, biggest first. */
    private fun shares(over: FloatArray, alphabet: Alphabet): List<Share> =
        over.indices
            .sortedByDescending { over[it] }
            .take(SHARES)
            .map { Share(alphabet[alphabet.spoken[it]], over[it]) }

    /** The gaps, and enough to know the grid they were read off. */
    data class Reading(val gaps: List<Gap>, val grid: Int, val dropped: Int)
}
