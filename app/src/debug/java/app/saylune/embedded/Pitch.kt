package app.saylune.embedded

import app.saylune.capture.WavFile
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * The melody: one pitch per syllable, on either side, in semitones.
 *
 * The third scale of the sound. `docs/analysis.md` had specified it as a slope over the
 * final voiced region -- one number for the whole sentence -- and that number cannot be
 * painted: `docs/reference.md` holds that the three scales anchor to the same characters of
 * the displayed text, and a terminal slope sits on no letters. Worse, a fault anywhere but
 * at the end would be invisible.
 *
 * So a pitch per syllable, and the mark where the two contours part. The syllables are the
 * ones [Syllables] already cuts and each knows its letters, so the melody anchors for free
 * and marks like the other two scales.
 *
 * **It asks for no montage of its own.** It reads positions and never labels, and the
 * montage in service already says where each sound of the model sits in the learner's
 * recording. The model counts the syllables and the learner is read against that count,
 * which is the rule everywhere else here.
 */
object Pitch {

    /** The band a human voice speaks in. Outside it, a peak is not a period. */
    private const val F0_MIN = 70.0f
    private const val F0_MAX = 400.0f

    /**
     * Below this the autocorrelation peak is noise rather than a period. Naming a pitch
     * there would invent the value the brick exists to read.
     */
    private const val VOICED = 0.30f

    private const val STEP_MS = 10
    private const val WINDOW_MS = 40

    /**
     * How far from the utterance's own middle a syllable may sit before it is read as a
     * harmonic lock rather than a pitch. Doubling is what a lock does, so the band is an
     * octave (`docs/qualification.md` names the artefact and says its label is then false).
     */
    private const val OCTAVE_BAND = 2.0f

    /** One reading of one recording: hertz every [STEP_MS], zero where nothing was voiced. */
    class Track(private val hz: FloatArray) {

        /**
         * The pitch over a stretch, in hertz, or zero when nothing in it was voiced.
         *
         * The median and not the mean, over the voiced frames alone: a syllable is a fifth
         * of a second with a consonant at each end, and an unvoiced frame counted as zero
         * would drag any average to the floor.
         */
        fun over(fromMs: Int, toMs: Int): Float {
            val first = (fromMs / STEP_MS).coerceIn(0, hz.size)
            val last = (toMs / STEP_MS + 1).coerceIn(first, hz.size)
            val voiced = (first until last).map { hz[it] }.filter { it > 0f }.sorted()
            return if (voiced.isEmpty()) 0f else voiced[voiced.size / 2]
        }
    }

    fun track(wav: java.io.File): Track {
        val samples = AcousticMatrix.samples(wav)
        val rate = WavFile.SAMPLE_RATE
        val hop = rate * STEP_MS / 1000
        val size = rate * WINDOW_MS / 1000
        val low = (rate / F0_MAX).roundToInt()
        val high = (rate / F0_MIN).roundToInt()
        val window = FloatArray(size) { 0.5f - 0.5f * cos(2.0 * Math.PI * it / (size - 1)).toFloat() }

        val out = ArrayList<Float>(samples.size / hop + 1)
        var start = 0
        while (start + size <= samples.size) {
            var mean = 0f
            for (i in 0 until size) mean += samples[start + i]
            mean /= size
            val frame = FloatArray(size) { (samples[start + it] - mean) * window[it] }

            var energy = 0f
            for (value in frame) energy += value * value
            if (energy <= 0f || high >= size) {
                out.add(0f)
                start += hop
                continue
            }
            var best = 0f
            var bestLag = 0
            for (lag in low..minOf(high, size - 1)) {
                var sum = 0f
                for (i in 0 until size - lag) sum += frame[i] * frame[i + lag]
                val strength = sum / energy
                if (strength > best) {
                    best = strength
                    bestLag = lag
                }
            }
            out.add(if (best >= VOICED && bestLag > 0) rate.toFloat() / bestLag else 0f)
            start += hop
        }
        return Track(out.toFloatArray())
    }

    /**
     * A contour in semitones, each side centred on its own middle.
     *
     * **Centred, never scaled**, and the difference decides the brick. A synthetic voice and
     * a learner do not share a register, so comparing hertz would call every man imitating a
     * woman wrong at every syllable -- hence the centring. But dividing by the spread would
     * make the two contours the same size before comparing them, and a learner speaking flat
     * where the model swings is exactly the fault this exists to catch. Centring removes the
     * register; scaling would remove the fault.
     *
     * A harmonic lock is folded back by octaves rather than dropped: an unreadable syllable
     * would be silence, and silence is never an issue here.
     *
     * Returns null for a syllable nothing voiced -- which is not the same as flat, and the
     * screen must not draw the two alike.
     */
    fun semitones(hz: List<Float>): List<Float?> {
        val voiced = hz.filter { it > 0f }.sorted()
        if (voiced.isEmpty()) return hz.map { null }
        val middle = voiced[voiced.size / 2]
        val folded = hz.map { value ->
            var here = value
            while (here > 0f && here > middle * OCTAVE_BAND) here /= 2f
            while (here > 0f && here < middle / OCTAVE_BAND) here *= 2f
            here
        }
        val kept = folded.filter { it > 0f }.sorted()
        val centre = kept[kept.size / 2]
        return folded.map { if (it <= 0f) null else 12f * (ln(it / centre) / ln(2f)) }
    }
}
