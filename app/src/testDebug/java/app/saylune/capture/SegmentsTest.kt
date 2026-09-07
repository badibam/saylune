package app.saylune.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * A turn cut into stretches, and put back.
 *
 * What is proved here is the shape and not the level: the level is a hand-set number the
 * bench will move, and a test that pinned it would fail the day the bench does its job. The
 * shape is what stays true whatever the number becomes -- silence costs its length and no
 * samples, speech keeps a margin, and the rebuilt turn is as long as the one that went in.
 */
class SegmentsTest {

    @get:Rule val folder = TemporaryFolder()

    private val bytesPerMs = WavFile.SAMPLE_RATE * WavFile.CHANNELS * WavFile.BITS / 8 / 1000

    /** Samples where each pair of milliseconds is loud or not, as [plan] says. */
    private fun pcm(vararg plan: Pair<Int, Boolean>): File {
        val file = folder.newFile("turn-${plan.hashCode()}.pcm")
        file.outputStream().buffered().use { out ->
            plan.forEach { (ms, loud) ->
                val amplitude = if (loud) (Short.MAX_VALUE * 0.4f).toInt() else 0
                var at = 0
                var up = true
                val bytes = ByteArray(ms * bytesPerMs)
                while (at + 1 < bytes.size) {
                    val sample = if (up) amplitude else -amplitude
                    bytes[at] = (sample and 0xFF).toByte()
                    bytes[at + 1] = ((sample shr 8) and 0xFF).toByte()
                    up = !up
                    at += 2
                }
                out.write(bytes)
            }
        }
        return file
    }

    /** Somebody who answers straight away: nothing to drop, so one stretch over all of it. */
    @Test
    fun `a turn with no long silence is one stretch of speech`() {
        val cut = Segments.cut(pcm(1000 to true))
        assertEquals(1, cut.size)
        assertTrue(cut.first().speech)
    }

    /** A blank long enough is a stretch of its own, and it carries no samples. */
    @Test
    fun `a long silence becomes a stretch that holds no audio`() {
        val cut = Segments.cut(pcm(600 to true, 1500 to false, 600 to true))
        assertEquals(listOf(true, false, true), cut.map { it.speech })
        val kept = folder.newFile("kept.pcm")
        Segments.keep(pcm(600 to true, 1500 to false, 600 to true), kept, cut)
        // What is written is the two stretches of speech and nothing between them.
        val speech = cut.filter { it.speech }.sumOf { it.length }
        assertEquals(speech.toLong() * bytesPerMs, kept.length())
    }

    /**
     * **A stop closure is not a silence.** A blank shorter than the run sits inside the
     * speech: cutting there would make a sentence full of /p t k/ into a dozen stretches, and
     * every one of those seams is a sound compared out of its context.
     */
    @Test
    fun `a short blank stays inside the speech`() {
        val cut = Segments.cut(pcm(400 to true, 120 to false, 400 to true))
        assertEquals(1, cut.size)
        assertTrue(cut.first().speech)
    }

    /**
     * **Every stretch of speech keeps a margin of real audio.** What identifies a stop lives
     * in the transition into the sound that follows, so cutting flush with the speech would
     * damage the very measure the trimming exists to leave alone.
     */
    @Test
    fun `speech keeps real audio either side of it`() {
        val cut = Segments.cut(pcm(1000 to false, 400 to true, 1000 to false))
        val speech = cut.first { it.speech }
        assertTrue("starts at ${speech.at}", speech.at < 1000)
        assertTrue("runs ${speech.length} ms", speech.length > 400)
    }

    /**
     * The property every measure rests on: the rebuilt recording is the turn again, silence
     * included, so nothing downstream can tell it apart from what was recorded by its shape
     * in time -- which is what every mark is anchored to.
     */
    @Test
    fun `the rebuilt turn is as long as the one that went in`() {
        val source = pcm(500 to true, 2000 to false, 500 to true)
        val cut = Segments.cut(source)
        val kept = folder.newFile("kept2.pcm")
        val back = folder.newFile("back.pcm")
        Segments.keep(source, kept, cut)
        Segments.rebuild(kept, back, cut)
        val whole = cut.last().let { it.at + it.length }
        assertEquals(whole.toLong() * bytesPerMs, back.length())
        assertTrue("kept ${kept.length()} of ${source.length()}", kept.length() < source.length())
    }

    /** Stretches never overlap and never run backwards, which is what makes them rebuildable. */
    @Test
    fun `the stretches tile the turn in order`() {
        val cut = Segments.cut(pcm(300 to true, 900 to false, 300 to true, 900 to false, 300 to true))
        cut.zipWithNext().forEach { (before, after) ->
            assertTrue("$before then $after", after.at >= before.at + before.length)
        }
    }

    /** Nothing recorded is no stretches, rather than one stretch of nothing. */
    @Test
    fun `an empty recording has no stretches`() {
        assertEquals(emptyList<Segments.Stretch>(), Segments.cut(folder.newFile("empty.pcm")))
    }
}
