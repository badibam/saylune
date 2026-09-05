package app.speakup.embedded

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Brick 2 of the embedded pipeline, on the device: one audio file in, the
 * spread over every sound every 20 ms out.
 *
 * It exists to answer what no desktop measure can -- how much memory it takes
 * to hold the weights, and how long a pass costs on a real turn of speech. The
 * numbers themselves are checked elsewhere: the matrices this writes are read
 * back by `bench/concord.py` against the ones the bench computed, because a
 * pipeline that agreed on the phone about different numbers would be worse than
 * one that failed outright.
 *
 * Debug-only on purpose. Which engine the app ships with is not decided, and a
 * release that carried a native dependency would be presuming the answer.
 */
class AcousticMatrix(weights: File, threads: Int) {

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    /** How long loading the weights took, which is half of what the device is asked. */
    val loadMillis: Long

    init {
        val options = OrtSession.SessionOptions()
        // CPU only, and a stated number of threads rather than whatever the
        // machine happens to offer: the desktop reading this is held against was
        // deterministic, and a GPU provider is not always.
        options.setIntraOpNumThreads(threads)
        val started = System.nanoTime()
        session = environment.createSession(weights.absolutePath, options)
        loadMillis = (System.nanoTime() - started) / 1_000_000
    }

    /**
     * The matrix of one file, the hidden layer the stress probe reads, and what the pass
     * cost. The graph renders both in one pass (`bench/export.py`), so nothing is computed
     * twice and the layer rows line up with the matrix rows, frame for frame.
     */
    class Reading(val frames: Int, val symbols: Int, val values: FloatArray,
                  val hidden: FloatArray, val millis: Long, val seconds: Float)

    fun read(wav: File): Reading {
        val audio = samples(wav)
        val prepared = prepared(audio)
        val started = System.nanoTime()
        val input = OnnxTensor.createTensor(
            environment, FloatBuffer.wrap(prepared),
            longArrayOf(1, prepared.size.toLong()))
        input.use { tensor ->
            session.run(mapOf("input_values" to tensor)).use { result ->
                // Fails by name rather than by index: a weights file exported before the
                // probe's layer existed would otherwise fail one array lookup later with
                // nothing to say what is missing.
                require(result.size() >= 2) {
                    "the weights render no hidden layer -- re-export them (bench/export.py)"
                }
                @Suppress("UNCHECKED_CAST")
                val batch = result[0].value as Array<Array<FloatArray>>
                val rows = batch[0]
                @Suppress("UNCHECKED_CAST")
                val hiddenRows = (result[1].value as Array<Array<FloatArray>>)[0]
                val millis = (System.nanoTime() - started) / 1_000_000
                val symbols = rows[0].size
                val values = FloatArray(rows.size * symbols)
                val width = hiddenRows[0].size
                val hidden = FloatArray(rows.size * width)
                for (frame in rows.indices) {
                    rows[frame].copyInto(values, frame * symbols)
                    hiddenRows[frame].copyInto(hidden, frame * width)
                }
                return Reading(rows.size, symbols, values, hidden, millis,
                               audio.size / SAMPLE_RATE.toFloat())
            }
        }
    }

    fun close() = session.close()

    companion object {
        const val SAMPLE_RATE = 16000

        /**
         * The waveform as the network expects it: zero mean, unit variance.
         *
         * The one step of the pipeline that lives on both sides. `bench/matrix.py`
         * writes it out in numpy for exactly this reason -- so that what has to be
         * rewritten here is visible there, rather than hidden inside a library the
         * phone does not have.
         */
        fun prepared(audio: FloatArray): FloatArray {
            var sum = 0.0
            for (value in audio) sum += value
            val mean = sum / audio.size
            var square = 0.0
            for (value in audio) square += (value - mean) * (value - mean)
            val deviation = Math.sqrt(square / audio.size + 1e-7)
            return FloatArray(audio.size) { ((audio[it] - mean) / deviation).toFloat() }
        }

        /**
         * The samples of a 16 kHz mono PCM wav, as the bench renders them.
         *
         * The chunks are walked rather than assumed at a fixed offset: a header
         * carrying anything before `data` would otherwise be read as sound, and
         * the failure would look like a bad pronunciation.
         */
        fun samples(wav: File): FloatArray {
            val bytes = ByteBuffer.wrap(wav.readBytes()).order(ByteOrder.LITTLE_ENDIAN)
            require(bytes.int == 0x46464952) { "${wav.name} n'est pas un RIFF" }
            bytes.int
            require(bytes.int == 0x45564157) { "${wav.name} n'est pas un WAVE" }
            var channels = 0
            var rate = 0
            var bits = 0
            while (bytes.remaining() >= 8) {
                val id = bytes.int
                val size = bytes.int
                val next = bytes.position() + size + (size and 1)
                when (id) {
                    0x20746d66 -> {  // "fmt "
                        bytes.short
                        channels = bytes.short.toInt()
                        rate = bytes.int
                        bytes.int
                        bytes.short
                        bits = bytes.short.toInt()
                    }
                    0x61746164 -> {  // "data"
                        require(channels == 1 && bits == 16) {
                            "${wav.name} est en $channels canaux sur $bits bits"
                        }
                        require(rate == SAMPLE_RATE) {
                            "${wav.name} est à $rate Hz, attendu $SAMPLE_RATE"
                        }
                        val audio = FloatArray(size / 2)
                        for (index in audio.indices) {
                            audio[index] = bytes.short / 32768f
                        }
                        return audio
                    }
                }
                bytes.position(next)
            }
            throw IllegalArgumentException("${wav.name} n'a pas de bloc data")
        }
    }
}
