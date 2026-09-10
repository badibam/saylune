package app.saylune.capture

/**
 * What counts as silence in a stretch of samples.
 *
 * **This is a reading of the recording and never a measure of the learner.** Nothing scored
 * reads it: the fluency sheets take their silences from where the two recordings put each
 * word, through the network, which is the reading that knows a word boundary from a stop
 * closure.
 *
 * **And it has one client left**, since the trimming of empty stretches was given up
 * (2026-09-10): the clock that sends a turn by itself at the third capture position. That
 * clock has to decide while somebody is speaking, with no words yet to go by, which is the
 * one place a level is the only thing there is to read.
 */
object Silence {

    /**
     * Under this, in the fraction of full scale a root-mean-square gives, the samples are
     * taken for silence.
     *
     * **Set by hand, and it is an open question rather than a number owed to the bench**
     * (`../../../../../../TODO.md`). An absolute bar cannot hold across devices: the app asks
     * for the microphone source with no automatic gain, so the same voice arrives at levels
     * that differ by more than the whole margin this bar leaves. That is what broke the
     * trimming, and the trimming is gone; what is left to decide is what this clock reads
     * instead, on the position of capture that has never yet been used.
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
