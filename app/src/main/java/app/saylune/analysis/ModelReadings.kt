package app.saylune.analysis

import app.saylune.embedded.Layer
import app.saylune.embedded.PassReading
import app.saylune.embedded.Stress
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The reading of a model, kept beside its render so a retake does not pay the pass again.
 *
 * A model is read once per sentence and then at every retake of it, and on a remote pass
 * each of those sent the same audio up again (`docs/design/remote-analysis.md`). The take is
 * never kept: it is new every time.
 *
 * **Stored folded, whichever pass produced it.** A local reading carries layer 19 whole,
 * a thousand times the matrix; folded it weighs what the matrix does. And **what was stored
 * is what is handed back, the first time too**, so every turn measured against this model
 * reads the same numbers rather than the first one reading others.
 */
internal object ModelReadings {

    fun of(kept: File, probe: Stress.Probe, read: () -> PassReading): PassReading {
        if (kept.isFile) {
            // A hit is a use, for the same eviction that sorts the renders.
            kept.setLastModified(System.currentTimeMillis())
            return parsed(kept.readBytes(), kept)
        }
        val fresh = read()
        val folded = when (val layer = fresh.layer) {
            is Layer.Whole -> Stress.fold(layer, probe)
            is Layer.Folded -> layer
        }
        val reading = PassReading(fresh.frames, fresh.symbols, fresh.values, folded,
                                  fresh.millis, fresh.seconds)
        // Written aside and renamed: a reading cut short would be taken for a whole one.
        val part = File(kept.parentFile, kept.name + ".part")
        part.writeBytes(written(reading))
        if (!part.renameTo(kept)) {
            part.delete()
            throw IllegalStateException("could not keep the model reading as ${kept.name}")
        }
        return reading
    }

    private fun written(reading: PassReading): ByteArray {
        val perFrame = (reading.layer as Layer.Folded).perFrame
        val buffer = ByteBuffer.allocate(HEAD + reading.values.size * 4 + perFrame.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(MAGIC).putShort(VERSION.toShort()).putInt(reading.frames)
            .putShort(reading.symbols.toShort()).putFloat(reading.seconds)
        reading.values.forEach { buffer.putFloat(it) }
        perFrame.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    /** Refused rather than guessed at: a reading at the wrong offsets is marks in the wrong place. */
    private fun parsed(bytes: ByteArray, kept: File): PassReading {
        check(bytes.size >= HEAD) { "${kept.name} is ${bytes.size} bytes, too short to be a reading" }
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val magic = ByteArray(4).also { buffer.get(it) }
        check(magic.contentEquals(MAGIC)) { "${kept.name} is not a kept reading" }
        val version = buffer.short.toInt()
        check(version == VERSION) { "${kept.name} is in format $version, this build knows $VERSION" }
        val frames = buffer.int
        val symbols = buffer.short.toInt()
        val seconds = buffer.float
        val due = HEAD + frames * symbols * 4 + frames * 4
        check(bytes.size == due) { "${kept.name} holds ${bytes.size} bytes where $due were due" }
        val values = FloatArray(frames * symbols) { buffer.float }
        val perFrame = FloatArray(frames) { buffer.float }
        // Nothing was computed to hand this back.
        return PassReading(frames, symbols, values, Layer.Folded(perFrame), 0, seconds)
    }

    private val MAGIC = "SAYR".toByteArray()
    private const val VERSION = 1
    // magic 4, version 2, frames 4, symbols 2, seconds 4
    private const val HEAD = 16
}
