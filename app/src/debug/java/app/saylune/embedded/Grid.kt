package app.saylune.embedded

/**
 * The matrix as rows and columns: one row per frame, one column per sound, each row
 * summing to one.
 *
 * The softmax lives inside the exported graph, so what the device gets out is already the
 * spread and not logits -- the same contract the bench holds itself to (`bench/matrix.py`).
 */
class Frames(val count: Int, val width: Int, private val values: FloatArray) {

    init {
        require(values.size == count * width) { "$count x $width does not fill ${values.size}" }
    }

    operator fun get(frame: Int, symbol: Int): Float = values[frame * width + symbol]

    /** The column this frame leans to, which is all free decoding ever asks. */
    fun loudest(frame: Int): Int {
        var best = 0
        val base = frame * width
        for (symbol in 1 until width) if (values[base + symbol] > values[base + best]) best = symbol
        return best
    }

    /**
     * The mean of every column over `[from, until)`.
     *
     * Accumulated in double and returned in float: a running float sum over a few hundred
     * frames drifts enough to move a mark, and the same arithmetic exists in Python beside
     * this one -- two implementations have to agree about the same numbers, not about
     * nearly the same ones.
     */
    fun mean(from: Int, until: Int): FloatArray {
        val total = DoubleArray(width)
        for (frame in from until until) {
            val base = frame * width
            for (symbol in 0 until width) total[symbol] += values[base + symbol]
        }
        val span = (until - from).toDouble()
        return FloatArray(width) { (total[it] / span).toFloat() }
    }

    companion object {
        fun of(reading: AcousticMatrix.Reading) =
            Frames(reading.frames, reading.symbols, reading.values)
    }
}

/** Where the model puts one sound: which column, and the frames it runs over. */
data class Segment(val symbol: Int, val start: Int, val stop: Int)

/**
 * The two readings of a matrix the comparison rests on.
 *
 * They are not symmetric and that is the point. The model is decoded **freely** -- these are
 * the sounds it really produced, not the ones the spelling says it should have -- and the
 * learner is **forced** onto that same sequence. So a row of one faces the row of the other
 * at the moment they mean the same thing, and the correspondence passes through a shared
 * symbolic landmark rather than through any resemblance of the two signals. That is what
 * keeps the difference of voice out of the measure.
 */
object Grid {

    /**
     * Free decoding: the sounds this voice actually produced, as frame spans.
     *
     * The label may be wrong without harm, since it never enters the comparison. Only the
     * spans matter, because they are what tells two matrices which rows face which.
     */
    fun decode(frames: Frames, alphabet: Alphabet): List<Segment> {
        val ignored = alphabet.notSounds
        val best = IntArray(frames.count) { frames.loudest(it) }
        val segments = mutableListOf<Segment>()
        var start = 0
        for (frame in 1..best.size) {
            if (frame == best.size || best[frame] != best[start]) {
                if (best[start] !in ignored) segments.add(Segment(best[start], start, frame))
                start = frame
            }
        }
        return segments
    }

    /**
     * Forced alignment: where the learner says each sound of the model's grid.
     *
     * The CTC trellis, with a blank threaded between every pair of sounds. A sound the path
     * never rests on comes back null -- it is dropped rather than given an invented span,
     * because a mark cannot be placed on what was not compared.
     */
    fun align(frames: Frames, ids: IntArray, blank: Int): Array<IntRange?> {
        if (ids.isEmpty()) return emptyArray()

        val extended = IntArray(2 * ids.size + 1) { blank }
        for (position in ids.indices) extended[2 * position + 1] = ids[position]

        val width = extended.size
        val length = frames.count
        require(length >= width) { "$length frames cannot spell ${ids.size} sounds" }

        val log = { frame: Int, symbol: Int ->
            Math.log(Math.max(frames[frame, symbol].toDouble(), 1e-12))
        }

        val score = Array(length) { DoubleArray(width) { Double.NEGATIVE_INFINITY } }
        val back = Array(length) { ByteArray(width) }
        score[0][0] = log(0, extended[0])
        if (width > 1) score[0][1] = log(0, extended[1])

        for (frame in 1 until length) {
            for (step in 0 until width) {
                var best = score[frame - 1][step]
                var origin = 0
                if (step > 0 && score[frame - 1][step - 1] > best) {
                    best = score[frame - 1][step - 1]
                    origin = 1
                }
                // A blank may never be skipped, and neither may a repeat: two identical
                // sounds in a row need the blank between them or they collapse into one.
                val skippable = step > 1 && extended[step] != blank &&
                    extended[step] != extended[step - 2]
                if (skippable && score[frame - 1][step - 2] > best) {
                    best = score[frame - 1][step - 2]
                    origin = 2
                }
                score[frame][step] = best + log(frame, extended[step])
                back[frame][step] = origin.toByte()
            }
        }

        var step = width - 1
        if (width >= 2 && score[length - 1][width - 2] > score[length - 1][width - 1]) {
            step = width - 2
        }
        val path = IntArray(length)
        for (frame in length - 1 downTo 0) {
            path[frame] = step
            step -= back[frame][step]
        }

        return Array(ids.size) { position ->
            val wanted = 2 * position + 1
            val first = path.indexOfFirst { it == wanted }
            if (first < 0) null else first..path.indexOfLast { it == wanted }
        }
    }
}
