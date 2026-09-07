package app.saylune.capture

/**
 * What counts as silence in a stretch of samples.
 *
 * **This is a reading of the recording and never a measure of the learner.** Nothing scored
 * reads it: the fluency sheets take their silences from where the two recordings put each
 * word, through the network, which is the reading that knows a word boundary from a stop
 * closure. What this one does is govern two things the microphone owes an answer to right
 * now, while somebody is speaking -- the clock that sends a turn at the third capture
 * position, and the empty stretches that are not kept as samples.
 *
 * So the two definitions of silence in the app do not have to agree, and they are not two
 * sources that drift: one is a live reading of a level and the other is a reading of speech,
 * and neither is ever asked what the other says.
 */
object Silence {

    /**
     * Under this, in the fraction of full scale a root-mean-square gives, the samples are
     * taken for silence.
     *
     * **Set by hand, and owed to the calibration bench** (`../../../../../../TODO.md`). The
     * bench has no hesitant spontaneous turns to read it off, and it is the one number here
     * that wants them: a room's floor, a breath and a held vowel are what separate a value
     * that works from one that clips speech.
     *
     * What bounds the cost of being wrong is where it is used. Too low, silence is kept and
     * it costs bytes; too high, a breath is cut, and the margin of real audio each stretch of
     * speech keeps is written for exactly that. The clock at the third position is the case
     * that bites, and it bites the same way a threshold set a little short would.
     */
    const val LEVEL = 0.012f

    /**
     * The root mean square of [count] bytes of 16-bit little-endian PCM in [buffer], as a
     * fraction of full scale.
     */
    fun level(buffer: ByteArray, count: Int): Float {
        if (count < 2) return 0f
        var sum = 0.0
        var at = 0
        while (at + 1 < count) {
            val sample = ((buffer[at + 1].toInt() shl 8) or (buffer[at].toInt() and 0xFF)).toShort()
            sum += sample.toDouble() * sample.toDouble()
            at += 2
        }
        val samples = count / 2
        return (Math.sqrt(sum / samples) / Short.MAX_VALUE).toFloat()
    }
}
