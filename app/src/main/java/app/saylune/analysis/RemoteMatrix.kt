package app.saylune.analysis

import app.saylune.chain.ChainFailure
import app.saylune.embedded.AcousticPass
import app.saylune.embedded.Layer
import app.saylune.embedded.PassReading
import app.saylune.providers.Http
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The acoustic pass, asked of a machine that is not this one.
 *
 * What travels is a waveform and what comes back is a matrix. No text, no reference, no
 * threshold, no verdict: the seam is [AcousticPass] precisely so that a deported pass is a
 * computation and never a judgement, and every brick above it -- grid, join, alignment,
 * overlap, marks, notes -- stays on the device unchanged.
 *
 * **The layer comes back folded.** The stress probe is a linear read of layer 19, which at
 * 1024 columns a frame is a thousand times the matrix; the far end applies the projection and
 * sends one scalar per frame, and [Layer.Folded] finishes the same arithmetic here. The
 * return is about 26 KB for a six second turn instead of 1.27 MB.
 *
 * **Two machines running these weights do not agree to the last digit**, measured: on a
 * twenty-six second turn the medians sit at 6.7e-07 apart, and twelve frames in thirteen
 * hundred elect a different sound where two were nearly tied. That is tolerable for exactly
 * one reason -- both audios of a turn go through the same pass, so the machine's own bias is
 * the same twice and cancels (`docs/reference.md`) -- and intolerable to mix: a reading from
 * here and a reading from there are two eras of measurement, which is what [version] is for.
 */
class RemoteMatrix(
    private val endpoint: String,
    private val token: String,
) : AcousticPass {

    override val version: String = "remote:${endpoint.trimEnd('/')}"

    override fun read(wav: File): PassReading {
        val answer = Http.post(
            url = "${endpoint.trimEnd('/')}/matrix",
            headers = mapOf("Authorization" to "Bearer $token"),
            contentType = "application/octet-stream",
            body = wav.readBytes(),
        )
        return parsed(answer)
    }

    /** Nothing to release: the session lives at the far end and outlives any one turn. */
    override fun close() = Unit

    private fun parsed(answer: ByteArray): PassReading {
        if (answer.size < HEAD) throw ChainFailure(
            "the analysis server answered ${answer.size} bytes, too short to be a reading"
        )
        val head = ByteBuffer.wrap(answer, 0, HEAD).order(ByteOrder.LITTLE_ENDIAN)
        val magic = ByteArray(4).also { head.get(it) }
        if (!magic.contentEquals(MAGIC)) throw ChainFailure(
            "the analysis server did not answer a reading"
        )
        // Refused rather than read on a guess: a version this build does not know may have
        // moved a field, and a matrix read at the wrong offsets is marks in the wrong place
        // -- which is worse than no marks, because nothing says it happened.
        val version = head.short.toInt()
        if (version != VERSION) throw ChainFailure(
            "the analysis server speaks reading format $version, this build knows $VERSION"
        )
        val frames = head.int
        val symbols = head.short.toInt()
        val seconds = head.float
        val millis = head.int.toLong()

        val expected = HEAD + frames * symbols * 2 + frames * 4
        if (answer.size != expected) throw ChainFailure(
            "the analysis server announced $frames x $symbols but sent ${answer.size} " +
                "bytes where $expected were due"
        )
        val body = ByteBuffer.wrap(answer, HEAD, answer.size - HEAD)
            .order(ByteOrder.LITTLE_ENDIAN)
        val values = FloatArray(frames * symbols)
        val halves = body.asShortBuffer()
        for (at in values.indices) values[at] = half(halves.get(at))
        body.position(HEAD + frames * symbols * 2)
        val folded = FloatArray(frames)
        body.asFloatBuffer().get(folded)
        return PassReading(frames, symbols, values, Layer.Folded(folded), millis, seconds)
    }

    private companion object {
        val MAGIC = "SAYM".toByteArray()
        const val VERSION = 1
        // magic 4, version 2, frames 4, symbols 2, seconds 4, millis 4
        const val HEAD = 20

        /**
         * A 16-bit float widened, by hand.
         *
         * `Float.fromBits` is the platform's, but half precision only arrived at API 34 and
         * the app goes lower. Sixteen bits is what the matrix travels in because it is read
         * as a spread and never to the last digit: measured, the rounding moves a cell by at
         * most 2.4e-04 and never changes which sound a frame elects.
         */
        fun half(bits: Short): Float {
            val held = bits.toInt() and 0xFFFF
            val sign = held ushr 15
            val exponent = (held ushr 10) and 0x1F
            val fraction = held and 0x3FF
            val widened = when {
                exponent == 0 && fraction == 0 -> sign shl 31
                exponent == 0 -> {
                    // Subnormal: normalise it by hand, since the wider format has room.
                    var mantissa = fraction
                    var power = -1
                    do {
                        mantissa = mantissa shl 1
                        power++
                    } while (mantissa and 0x400 == 0)
                    (sign shl 31) or ((127 - 15 - power) shl 23) or
                        ((mantissa and 0x3FF) shl 13)
                }
                exponent == 0x1F -> (sign shl 31) or (0xFF shl 23) or (fraction shl 13)
                else -> (sign shl 31) or ((exponent - 15 + 127) shl 23) or (fraction shl 13)
            }
            return Float.fromBits(widened)
        }
    }
}
