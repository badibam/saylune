package app.speakup.capture

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A wav a provider rendered, brought to the format the acoustic model consumes.
 *
 * Azure hands back `raw-16khz-16bit-mono-pcm` and needs none of this. Replicate hands back
 * whatever the model renders -- chatterbox writes floating-point samples at its own rate --
 * so something has to sit between the download and the analysis.
 *
 * **This is not a treatment, and the difference decides how carefully it is written.** The
 * doc forbids anything done to one of the two recordings and not the other: a gain, a
 * denoiser, a normalisation applied to the take and not to the model would make part of the
 * measured gap manufactured. What the doc *requires* of both sides is 16 kHz, mono, PCM 16
 * bits -- the entry contract. Reaching that contract is not a treatment, it is the condition
 * of being measured at all.
 *
 * But getting there badly would be one. Dropping samples to go from 24 kHz to 16 kHz folds
 * everything above 8 kHz back into the band as aliasing, on the model's side only -- exactly
 * the asymmetry the rule exists to forbid, arriving by the back door of a format conversion.
 * So the band is cut before the rate is, with a windowed-sinc filter, and the cost is a few
 * lines here rather than a bias no one would see.
 */
object Rendered {

    /**
     * Read [source], whatever it holds, and write [target] as the app's own 16 kHz mono wav.
     *
     * Throws on anything it cannot read rather than writing silence: a model file that is
     * quietly empty would be measured against, and every sound of the take would look wrong.
     */
    fun at16kMono(source: File, target: File) {
        val bytes = source.readBytes()
        val wav = read(bytes)
        val mono = toMono(wav)
        val resampled = resample(mono, wav.sampleRate, WavFile.SAMPLE_RATE)

        val pcm = File(target.parentFile, target.name + ".pcm")
        ByteBuffer.allocate(resampled.size * 2).order(ByteOrder.LITTLE_ENDIAN).let { out ->
            resampled.forEach { sample ->
                out.putShort((sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort())
            }
            pcm.writeBytes(out.array())
        }
        WavFile.wrap(pcm, target)
        pcm.delete()
    }

    /** Samples in -1..1, and how they were laid out. */
    private class Pcm(val samples: FloatArray, val channels: Int, val sampleRate: Int)

    /**
     * Walk the chunks rather than assume the layout.
     *
     * A wav is `fmt ` and `data` somewhere among chunks whose order and number nobody
     * promises -- renderers put a `LIST` in the middle, and the 44-byte header the app
     * writes itself is only the simplest possible case.
     */
    private fun read(bytes: ByteArray): Pcm {
        require(bytes.size > 12 && bytes.tag(0) == "RIFF" && bytes.tag(8) == "WAVE") {
            "the render is not a wav"
        }
        var format = 0
        var channels = 0
        var sampleRate = 0
        var bits = 0
        var at = 12
        while (at + 8 <= bytes.size) {
            val id = bytes.tag(at)
            val size = bytes.int(at + 4)
            val body = at + 8
            when (id) {
                "fmt " -> {
                    format = bytes.short(body)
                    channels = bytes.short(body + 2)
                    sampleRate = bytes.int(body + 4)
                    bits = bytes.short(body + 14)
                }
                "data" -> {
                    require(channels > 0 && sampleRate > 0) { "the render has no format chunk" }
                    val end = minOf(body + size, bytes.size)
                    return Pcm(decode(bytes, body, end, format, bits), channels, sampleRate)
                }
            }
            at = body + size + (size and 1)
        }
        throw IllegalArgumentException("the render has no data chunk")
    }

    /**
     * The two encodings that turn up: integer PCM and IEEE floats.
     *
     * Chatterbox renders floats, which is why this is here at all -- the `wave` reader of
     * the bench refuses them too, and a reader that only knew integers would have failed on
     * the first synthesis rather than on some rare model.
     */
    private fun decode(bytes: ByteArray, from: Int, to: Int, format: Int, bits: Int): FloatArray {
        val buffer = ByteBuffer.wrap(bytes, from, to - from).order(ByteOrder.LITTLE_ENDIAN)
        return when {
            format == FORMAT_FLOAT && bits == 32 ->
                FloatArray((to - from) / 4) { buffer.getFloat() }
            format == FORMAT_PCM && bits == 16 ->
                FloatArray((to - from) / 2) { buffer.getShort() / SHORT_SCALE }
            format == FORMAT_PCM && bits == 8 ->
                FloatArray(to - from) { (buffer.get().toInt() and 0xFF).minus(128) / 128f }
            else -> throw IllegalArgumentException(
                "the render is in an encoding nothing here reads: format $format, $bits bits"
            )
        }
    }

    /** Channels are averaged, never picked: dropping one would drop half of what was said. */
    private fun toMono(wav: Pcm): FloatArray {
        if (wav.channels == 1) return wav.samples
        val frames = wav.samples.size / wav.channels
        return FloatArray(frames) { frame ->
            var sum = 0f
            for (channel in 0 until wav.channels) sum += wav.samples[frame * wav.channels + channel]
            sum / wav.channels
        }
    }

    /**
     * From [from] hertz to [to], band-limited first when that means going down.
     *
     * The filter is a sinc windowed by a Hamming window, cut at half the target rate: it is
     * the plain textbook answer, it is thirty lines, and it removes the one way this
     * conversion could quietly bias the measure. Going up needs no filter -- interpolation
     * invents nothing above the band that was already there.
     */
    private fun resample(samples: FloatArray, from: Int, to: Int): FloatArray {
        if (from == to || samples.isEmpty()) return samples
        val band = if (from > to) lowPass(samples, cutoff = 0.5 * to / from) else samples
        val ratio = to.toDouble() / from
        val length = (band.size * ratio).toInt().coerceAtLeast(1)
        return FloatArray(length) { at ->
            val source = at / ratio
            val left = source.toInt()
            val right = (left + 1).coerceAtMost(band.size - 1)
            val fraction = (source - left).toFloat()
            band[left] * (1 - fraction) + band[right] * fraction
        }
    }

    /** [cutoff] is a fraction of the sampling rate, so 0.5 is everything and 0.33 is a third. */
    private fun lowPass(samples: FloatArray, cutoff: Double): FloatArray {
        val half = TAPS / 2
        val kernel = DoubleArray(TAPS) { tap ->
            val offset = (tap - half).toDouble()
            val sinc = if (offset == 0.0) 2 * cutoff
            else Math.sin(2 * Math.PI * cutoff * offset) / (Math.PI * offset)
            // Hamming, so the stopband does not ring back into what it was meant to remove.
            sinc * (0.54 - 0.46 * Math.cos(2 * Math.PI * tap / (TAPS - 1)))
        }
        val weight = kernel.sum()
        return FloatArray(samples.size) { at ->
            var sum = 0.0
            for (tap in 0 until TAPS) {
                val from = at + tap - half
                if (from in samples.indices) sum += samples[from] * kernel[tap]
            }
            (sum / weight).toFloat()
        }
    }

    private fun ByteArray.tag(at: Int) = String(this, at, 4, Charsets.US_ASCII)

    private fun ByteArray.int(at: Int) =
        ByteBuffer.wrap(this, at, 4).order(ByteOrder.LITTLE_ENDIAN).int

    private fun ByteArray.short(at: Int) =
        ByteBuffer.wrap(this, at, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0xFFFF

    private const val FORMAT_PCM = 1
    private const val FORMAT_FLOAT = 3
    private const val SHORT_SCALE = 32768f

    /** Odd, so the kernel has a middle tap and the filter delays nothing by half a sample. */
    private const val TAPS = 65
}
