package app.speakup.capture

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.PlaybackParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.resume
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * Say a wav, and come back when it has finished saying it.
 *
 * Suspending until the end is the whole point: the conversation's phases would otherwise
 * mean nothing -- "answering" would end while the answer was still being spoken.
 *
 * An error resumes rather than throws. A model that will not play is a disappointment and
 * not a broken turn, and the caller has nothing useful to do about it that it would not do
 * anyway.
 */
object Playback {

    suspend fun play(wav: File, speed: Float = 1f) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val player = MediaPlayer()
            player.setOnCompletionListener {
                it.release()
                if (continuation.isActive) continuation.resume(Unit)
            }
            player.setOnErrorListener { p, _, _ ->
                p.release()
                if (continuation.isActive) continuation.resume(Unit)
                true
            }
            continuation.invokeOnCancellation { runCatching { player.release() } }
            player.setDataSource(wav.path)
            player.prepare()
            // Slower without dropping the pitch, which is the only slowing worth
            // having: resampling would take the formants down with the rate and turn
            // `ɪ` into a vowel nobody said. This stretches time and leaves pitch alone.
            if (speed != 1f) player.playbackParams = PlaybackParams().setSpeed(speed)
            player.start()
        }
    }

    /**
     * One stretch of a wav, from [fromMs] to [toMs] -- a single sound, made audible.
     *
     * Not `MediaPlayer.seekTo`: a sound lasts 20 to 150 ms and seeking is accurate to a frame
     * of the container, which is the same order of magnitude. The samples are cut instead,
     * which is exact, and both wavs the app holds are 16 kHz mono PCM by contract -- the
     * capture writes it and [Rendered] brings every provider's answer to it.
     *
     * **Cut at the exact bounds it would not be a sound but a click.** A plosive is a closure
     * and a release, and most of what tells /t/ from /k/ is in the formants of whatever
     * follows; a razor cut at the mark's own edges throws that away and hands back a tick.
     * So a margin is played on each side, and the ends are faded rather than started at full
     * amplitude, which would add a click of its own to a sound that had none. It is heard in
     * its place rather than sliced out of it, which is the honest thing anyway: this says
     * where the gap is, and nothing here is a specimen of the sound in isolation.
     */
    suspend fun play(
        wav: File,
        fromMs: Int,
        toMs: Int,
        speed: Float = 1f,
        marginMs: Int = SOUND_MARGIN_MS,
    ) =
        withContext(Dispatchers.IO) {
            val samples = pcm(wav) ?: return@withContext
            val rate = WavFile.SAMPLE_RATE
            val margin = marginMs * rate / 1000
            val start = ((fromMs * rate / 1000) - margin).coerceIn(0, samples.size)
            val stop = ((toMs * rate / 1000) + margin).coerceIn(start, samples.size)
            if (stop - start < 2) return@withContext
            faded(samples.copyOfRange(start, stop), rate, speed)
        }

    /** The samples of a 16-bit mono wav, or null when the file is not one. */
    private fun pcm(wav: File): ShortArray? {
        val bytes = runCatching { wav.readBytes() }.getOrNull() ?: return null
        // The `data` chunk, found rather than assumed at 44: every wav the app holds is
        // written by `WavFile`, but a file that grew a chunk would otherwise be read as
        // noise with nothing to say so.
        var at = 12
        while (at + 8 <= bytes.size) {
            val id = String(bytes, at, 4, Charsets.US_ASCII)
            val size = ByteBuffer.wrap(bytes, at + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (id == "data") {
                val length = size.coerceAtMost(bytes.size - at - 8) / 2
                if (length <= 0) return null
                val out = ShortArray(length)
                ByteBuffer.wrap(bytes, at + 8, length * 2)
                    .order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(out)
                return out
            }
            at += 8 + size + (size and 1)
        }
        return null
    }

    /** The stretch played once, its ends ramped so neither edge is a click of its own. */
    private fun faded(slice: ShortArray, rate: Int, speed: Float) {
        val ramp = (FADE_MS * rate / 1000).coerceAtMost(slice.size / 2)
        for (index in 0 until ramp) {
            val gain = (1.0 - cos(Math.PI * index / ramp)) / 2.0
            slice[index] = (slice[index] * gain).roundToInt().toShort()
            val mirror = slice.size - 1 - index
            slice[mirror] = (slice[mirror] * gain).roundToInt().toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(rate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(slice.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        runCatching {
            track.write(slice, 0, slice.size)
            // Time stretched, pitch left alone -- see the note in the whole-file `play`.
            if (speed != 1f) track.playbackParams = PlaybackParams().setSpeed(speed)
            track.play()
            // MODE_STATIC plays the whole buffer once; the wait is its own length over the
            // speed, which is known exactly here and costs no listener.
            Thread.sleep((slice.size * 1000L / rate / speed).toLong() + 60)
        }
        runCatching { track.stop() }
        track.release()
    }

    /**
     * How much of the neighbours is played either side of **one sound**. A stop's release
     * and the formant transition that identifies it both sit outside the mark's own bounds,
     * so cutting at them hands back a tick rather than a consonant.
     */
    const val SOUND_MARGIN_MS = 45

    /**
     * And either side of **a word**, where the same generosity would be a fault: what sits
     * outside a word is other words, and 45 ms of them is a syllable of the neighbour played
     * as if it belonged. Barely more than the fade, which is what keeps the edge from
     * clicking -- enough not to cut inside the first sound's onset, not enough to say
     * anything about what comes before it.
     */
    const val WORD_MARGIN_MS = 12

    /** Long enough that the edge is not a click, short enough not to eat a short sound. */
    private const val FADE_MS = 8
}
