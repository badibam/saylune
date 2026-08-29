package app.speakup.capture

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The header that turns the raw samples of a capture into a file anything can read.
 *
 * Written once, at the end, and this is what makes the capture safe to append to. The
 * wisdom asks a durable file to be written atomically, and a recording cannot be: it grows
 * while someone speaks. The exemption it grants is for a partial write that cannot be
 * mistaken for a whole one -- so the samples accumulate in a `.pcm`, which no reader
 * accepts, and the `.wav` appears complete or not at all.
 */
object WavFile {

    const val SAMPLE_RATE = 16_000
    const val CHANNELS = 1
    const val BITS = 16

    /**
     * Wrap [pcm] as a wav at [target], via a temporary that is renamed -- so a reader either
     * finds a whole turn or finds nothing.
     */
    fun wrap(pcm: File, target: File) {
        val part = File(target.parentFile, target.name + ".part")
        RandomAccessFile(part, "rw").use { out ->
            out.setLength(0)
            out.write(header(pcm.length().toInt()))
            pcm.inputStream().use { it.copyTo(java.io.FileOutputStream(out.fd)) }
        }
        check(part.renameTo(target)) { "could not put the finished turn at ${target.path}" }
    }

    private fun header(dataBytes: Int): ByteArray {
        val byteRate = SAMPLE_RATE * CHANNELS * BITS / 8
        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(36 + dataBytes)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1)                                   // PCM, uncompressed
            putShort(CHANNELS.toShort())
            putInt(SAMPLE_RATE)
            putInt(byteRate)
            putShort((CHANNELS * BITS / 8).toShort())     // block align
            putShort(BITS.toShort())
            put("data".toByteArray())
            putInt(dataBytes)
        }.array()
    }
}
