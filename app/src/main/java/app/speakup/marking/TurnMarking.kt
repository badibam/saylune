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
) {
    /** Pitch bounds across both contours, with a little air so neither hugs the band edge. */
    fun pitchBounds(): ClosedFloatingPointRange<Float> {
        val values = syllables.flatMap { listOfNotNull(it.modelHz, it.learnerHz) }
        val lo = (values.minOrNull() ?: 0f) - 10f
        val hi = (values.maxOrNull() ?: 1f) + 10f
        return lo..hi
    }

    fun modelContour(): Contour =
        Contour(syllables.map { it.center to it.modelHz })

    fun learnerContour(): Contour =
        Contour(syllables.mapNotNull { s -> s.learnerHz?.let { s.center to it } })

    /** False across a syllable whose pitch the harmonic-lock filter threw away. */
    fun learnerPitchKnownAt(offset: Int): Boolean =
        syllables.none { offset in it.start until it.end && it.learnerHz == null }
}

/**
 * One syllable. Stress is binary by nature: it sits on the right syllable or it does not.
 * Both stress flags come from the engine reading the model and the learner — never from a
 * dictionary, which the engine contradicts on 41% of polysyllabic words.
 */
data class Syllable(
    val start: Int,
    val end: Int,
    val modelHz: Float,
    val learnerHz: Float?,
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
