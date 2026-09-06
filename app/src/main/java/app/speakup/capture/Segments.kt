package app.speakup.capture

import java.io.File
import java.io.RandomAccessFile

/**
 * A turn as a list of stretches: a length of silence, or some audio.
 *
 * **The silence is never stored as samples.** It costs its length and nothing else, where
 * keeping it would take a twenty-minute conversation into tens of megabytes. What is kept is
 * enough to put the original back: a stretch of speech knows where it sat, so the recording
 * is rebuilt by writing each one at its own offset with silence between.
 *
 * **Every stretch of speech keeps a margin of real audio at both ends.** What identifies a
 * stop lives in the transition into the sound that follows, and cutting flush with the speech
 * would damage the measure -- so the margin is not a comfort, it is what makes the trimming
 * safe to do at all.
 *
 * **Nothing measured reads this.** Every measure reads the rebuilt recording (settled
 * 2026-09-06): the sound analysis rests on continuous frames and on the rule that nothing is
 * done to one of the two recordings alone -- the model's render carries its own silences and
 * nobody strips those -- and the fluency sheets cannot read a list of stretches anyway, a
 * pause being a silence *between two words*, which only the placing of the words can say. So
 * this is what saves disk and what keeps silence off the network, and the list of measures
 * that read it is empty.
 */
object Segments {

    /**
     * How long a stretch has to stay under [Silence.LEVEL] before it is dropped rather than
     * kept as samples.
     *
     * **Safe at the order of half a second, and well outside the domain of the phonemes** --
     * the longest closure of a stop is a fraction of it -- which is why this one is not owed
     * to the bench the way the level is.
     */
    const val RUN_MS = 500

    /** How much real audio is kept either side of a stretch of speech. */
    const val MARGIN_MS = 120

    /** How long a slice of samples is read and judged at a time. */
    const val FRAME_MS = 20

    /**
     * One stretch of the turn: silence for [speech] false, audio otherwise.
     *
     * [at] and [length] are in milliseconds from the start of the turn. A silent stretch has
     * no bytes at all, which is the whole saving.
     */
    data class Stretch(val at: Int, val length: Int, val speech: Boolean)

    /**
     * Cut [pcm] into stretches, silence runs of at least [RUN_MS] dropped.
     *
     * A file with no silence long enough comes back as one stretch of speech covering all of
     * it, which is the ordinary case of somebody answering straight away.
     */
    fun cut(pcm: File): List<Stretch> {
        val frame = bytesOf(FRAME_MS)
        val total = pcm.length().toInt()
        if (total <= 0) return emptyList()

        // Which frames hold speech, read once. A boolean per twenty milliseconds is a few
        // hundred bytes on the longest turn the ceiling allows.
        val loud = BooleanArray((total + frame - 1) / frame)
        val buffer = ByteArray(frame)
        pcm.inputStream().buffered().use { input ->
            var at = 0
            while (at < loud.size) {
                val read = input.read(buffer)
                if (read <= 0) break
                loud[at] = Silence.level(buffer, read) >= Silence.LEVEL
                at++
            }
        }

        val runFrames = maxOf(1, RUN_MS / FRAME_MS)
        val marginFrames = MARGIN_MS / FRAME_MS

        // **The speech is found first, margins and all, and the silence is what is left
        // between.** Cutting the silences out first and giving the speech its margin
        // afterwards puts the two in competition for the same frames, and the margin is what
        // loses -- which is the one thing here that must not be given up, since it is what
        // makes the trimming safe for the measure.
        val speech = mutableListOf<IntRange>()
        var frameAt = 0
        while (frameAt < loud.size) {
            if (!loud[frameAt]) { frameAt++; continue }
            var end = frameAt
            var quiet = 0
            // A run of quiet shorter than the threshold is inside the speech: it is a stop
            // closure or a breath, and cutting there would make a sentence full of /p t k/
            // into a dozen stretches, each seam a sound compared out of its context.
            while (end < loud.size) {
                quiet = if (loud[end]) 0 else quiet + 1
                if (quiet >= runFrames) break
                end++
            }
            val last = end - quiet
            speech += maxOf(0, frameAt - marginFrames)..minOf(loud.size - 1, last + marginFrames)
            frameAt = end
        }

        // Margins can push two stretches into one another; joined, they stay one stretch.
        val joined = mutableListOf<IntRange>()
        speech.forEach { span ->
            val last = joined.lastOrNull()
            if (last != null && span.first <= last.last + 1) {
                joined[joined.lastIndex] = last.first..maxOf(last.last, span.last)
            } else joined += span
        }

        val out = mutableListOf<Stretch>()
        var atFrame = 0
        joined.forEach { span ->
            if (span.first > atFrame) {
                out += Stretch(msOf(atFrame * frame), msOf((span.first - atFrame) * frame), false)
            }
            out += Stretch(msOf(span.first * frame), msOf((span.last + 1 - span.first) * frame), true)
            atFrame = span.last + 1
        }
        if (atFrame < loud.size) {
            out += Stretch(msOf(atFrame * frame), msOf((loud.size - atFrame) * frame), false)
        }
        return out.filter { it.length > 0 }
    }

    /**
     * Write only the stretches of speech of [pcm] into [into], silence dropped.
     *
     * This is what does not go to the network, and what a turn is kept as. The stretches say
     * where each one sat, so [rebuild] puts the original back without them.
     */
    fun keep(pcm: File, into: File, stretches: List<Stretch>) {
        RandomAccessFile(pcm, "r").use { source ->
            into.outputStream().buffered().use { out ->
                stretches.filter { it.speech }.forEach { stretch ->
                    source.seek(bytesOf(stretch.at).toLong())
                    val buffer = ByteArray(bytesOf(stretch.length))
                    val read = source.read(buffer)
                    if (read > 0) out.write(buffer, 0, read)
                }
            }
        }
    }

    /**
     * Put the recording back at [into]: each stretch of speech at its own offset, silence
     * written as zeroes between them.
     *
     * **Every measure reads this and never the stretches.** The zeroes are not what was in
     * the room, and that is the one thing to know about them: a room floor is not silence
     * and the analysis reads spreads rather than levels, so what is restored is the shape of
     * the turn in time, which is what every measure of it is anchored to.
     */
    fun rebuild(kept: File, into: File, stretches: List<Stretch>) {
        kept.inputStream().buffered().use { input ->
            into.outputStream().buffered().use { out ->
                var written = 0
                stretches.forEach { stretch ->
                    val start = bytesOf(stretch.at)
                    if (start > written) {
                        out.write(ByteArray(start - written))
                        written = start
                    }
                    val length = bytesOf(stretch.length)
                    if (stretch.speech) {
                        val buffer = ByteArray(length)
                        var got = 0
                        while (got < length) {
                            val read = input.read(buffer, got, length - got)
                            if (read <= 0) break
                            got += read
                        }
                        out.write(buffer, 0, got)
                        written += got
                    } else {
                        out.write(ByteArray(length))
                        written += length
                    }
                }
            }
        }
    }

    private fun bytesOf(ms: Int): Int =
        ms * (WavFile.SAMPLE_RATE * WavFile.CHANNELS * WavFile.BITS / 8) / 1000

    private fun msOf(bytes: Int): Int =
        bytes * 1000 / (WavFile.SAMPLE_RATE * WavFile.CHANNELS * WavFile.BITS / 8)
}
