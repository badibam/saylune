package app.speakup.embedded

import org.json.JSONObject
import java.io.File
import kotlin.math.exp

/**
 * Brick 7 on the device: which syllable carries the stress, read by the frozen probe.
 *
 * The probe is a linear read of layer 19 of the acoustic model: the mean of the hidden
 * frames over each nucleus span, standardised, dotted with a frozen weight, softmaxed over
 * the word. It was fitted on TIMIT and judged by an ear (`bench/probe.py`, `bench/hear.py`);
 * what lives here is the same arithmetic, held against the bench by `PortTest`.
 */
object Stress {

    /**
     * The eligibility bar, on the model side. A word whose elected syllable does not lead
     * the runner-up by this much receives no mark at all: a word the model does not stress
     * clearly cannot be got wrong. Set by measure, not by taste -- between 0.80 and 0.94
     * the sweep renders the same marks, and 0.95 would throw away a word an ear heard as
     * plainly stressed (`bench/probe.py` BAR, `docs/analysis.md`).
     */
    const val BAR = 0.90f

    /**
     * The frozen probe: the moments and the weight, exactly as `probe.py --json` writes
     * them beside the exported graph. They belong to the weights they were fitted on, which
     * is why they live beside the onnx rather than among the assets -- pushing one without
     * the other would read stress off a model that never produced the states it was
     * trained on.
     */
    data class Probe(
        val layer: Int,
        val mean: FloatArray,
        val deviation: FloatArray,
        val weight: FloatArray,
        val bias: Float,
    ) {
        companion object {
            fun read(file: File): Probe {
                val held = JSONObject(file.readText())
                val width = held.getJSONArray("mean").length()
                fun column(name: String) = FloatArray(width) { index ->
                    held.getJSONArray(name).getDouble(index).toFloat()
                }
                return Probe(
                    layer = held.getInt("layer"),
                    mean = column("mean"),
                    deviation = column("deviation"),
                    weight = column("weight"),
                    bias = held.getDouble("bias").toFloat(),
                )
            }
        }
    }

    /**
     * The probe's part of the stress on each nucleus span, softmaxed over the word.
     *
     * One span is an (first, last) pair of frames, `last` exclusive, as the bench hands
     * them out. The mean of the layer over the span, standardised with the frozen moments,
     * dotted with the frozen weight, plus the bias: the raw score. The softmax turns the
     * scores of one word into parts summing to one -- what "the elected syllable" and the
     * margin are read off. Null when a span runs past the layer, so the caller drops the
     * word rather than reading a short syllable: the same contract `probe.parts` holds.
     */
    fun parts(hidden: FloatArray, places: List<IntRange>, probe: Probe): FloatArray? {
        require(hidden.size % probe.mean.size == 0) {
            "the hidden layer does not match the probe: ${hidden.size} frames "
            "over ${probe.mean.size} columns leaves a remainder"
        }
        val width = probe.mean.size
        val frames = hidden.size / width
        val scores = FloatArray(places.size)
        for ((index, span) in places.withIndex()) {
            var last = span.last
            if (last <= span.first) last = span.first + 1
            if (last > frames) return null
            // Accumulated in double and returned in float, for the same reason
            // `Frames.mean` gives: a running float sum drifts enough to move a
            // mark, and two implementations have to agree about the same
            // numbers, not about nearly the same ones.
            val mean = DoubleArray(width)
            for (frame in span.first until last) {
                val base = frame * width
                for (column in 0 until width) mean[column] += hidden[base + column]
            }
            val count = (last - span.first).toDouble()
            var dot = 0.0
            for (column in 0 until width) {
                val standardised = (mean[column] / count - probe.mean[column]) /
                    probe.deviation[column]
                dot += standardised * probe.weight[column]
            }
            scores[index] = (dot + probe.bias).toFloat()
        }
        // Softmax shifted by the largest score before exponentiating, as in the
        // bench: the parts are the same either way, and the shift keeps the
        // exponentials off the machine's ceiling.
        var total = 0.0
        val parts = FloatArray(scores.size)
        var best = scores[0]
        for (index in 1 until scores.size) if (scores[index] > best) best = scores[index]
        for (index in scores.indices) {
            val odds = exp((scores[index] - best).toDouble())
            parts[index] = odds.toFloat()
            total += odds
        }
        for (index in parts.indices) parts[index] = (parts[index] / total).toFloat()
        return parts
    }

    /**
     * Which syllable carries the stress, on each side, for every syllable handed in.
     *
     * The caller hands the cuts of the whole utterance and the two hidden layers; the
     * words are regrouped here, as the bench regroups them (`bench/turn.py`): consecutive
     * cuts sharing a word are that word, read on the **nucleus** alone -- the model's own
     * segment on its side, and where that same sound sits in the take on the other, which
     * [spanOf] already knows from the aligned montage.
     *
     * A word is eligible when the model's elected syllable leads the runner-up by
     * [BAR] -- a word the model does not stress clearly cannot be got wrong -- and a word
     * of one syllable or a function word has no stress choice to be wrong about. The
     * returned list lines up with [cuts]; every ineligible syllable is (false, false).
     */
    fun flags(
        cuts: List<Syllables.Cut>,
        sounds: List<Sound>,
        segments: List<Segment>,
        spanOf: Map<Int, Pair<IntRange, IntRange>>,
        modelHidden: FloatArray,
        saidHidden: FloatArray,
        probe: Probe,
    ): List<Pair<Boolean, Boolean>> {
        val out = MutableList(cuts.size) { false to false }
        var at = 0
        while (at < cuts.size) {
            val word = cuts[at].word
                ?.trim(' ', '.', ',', '!', '?', '\'')
                ?.lowercase()
            var end = at + 1
            while (end < cuts.size && cuts[end].word == cuts[at].word) end++
            if (word == null || word in Syllables.FUNCTION || end - at < 2) {
                at = end
                continue
            }
            val modelPlaces = mutableListOf<IntRange>()
            val saidPlaces = mutableListOf<IntRange>()
            var broken = false
            for (index in at until end) {
                val cut = cuts[index]
                val symbols = sounds
                    .subList(cut.sounds.first, cut.sounds.last + 1)
                    .map { it.symbol }
                val heads = Syllables.nuclei(symbols)
                val span = heads.firstOrNull()?.let { spanOf[cut.sounds.first + it] }
                if (span == null) {
                    broken = true
                    break
                }
                modelPlaces.add(
                    segments[cut.sounds.first + heads[0]].start until
                        segments[cut.sounds.first + heads[0]].stop
                )
                saidPlaces.add(span.second)
            }
            if (!broken) {
                val theirs = parts(modelHidden, modelPlaces, probe)
                val mine = parts(saidHidden, saidPlaces, probe)
                if (theirs != null && mine != null && margin(theirs) >= BAR) {
                    val modelAt = elected(theirs)
                    val saidAt = elected(mine)
                    for (index in at until end) {
                        out[index] = (index - at == modelAt) to (index - at == saidAt)
                    }
                }
            }
            at = end
        }
        return out
    }

    /** The syllable the probe elected: the index of the largest part. */
    fun elected(parts: FloatArray): Int {
        var best = 0
        for (index in 1 until parts.size) {
            if (parts[index] > parts[best]) best = index
        }
        return best
    }

    /** The elected syllable's lead over the runner-up, which the bar reads. */
    fun margin(parts: FloatArray): Float {
        var best = -1f
        var second = -1f
        for (part in parts) {
            if (part > best) {
                second = best
                best = part
            } else if (part > second) {
                second = part
            }
        }
        return best - second
    }
}
