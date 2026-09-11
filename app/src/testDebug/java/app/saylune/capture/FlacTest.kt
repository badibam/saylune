package app.saylune.capture

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * The encoder on the cases that break encoders, written out for the reference decoder.
 *
 * What proves the files is not here: a decoder written by the same hand would share its
 * misreadings. Each case lands in `build/flac/` beside its raw samples, and `bench/flac.py`
 * decodes them with libFLAC and compares sample for sample.
 */
class FlacTest {

    private val cases: Map<String, ShortArray> = Random(11).let { random ->
        mapOf(
            "speech-like" to ShortArray(16_000) {
                (8000 * sin(2 * PI * 220 * it / 16_000) + random.nextInt(-300, 300)).toInt().toShort()
            },
            "silence" to ShortArray(20_000),
            "three" to shortArrayOf(1, -2, 3),
            "one-block" to ShortArray(4096) { (it * 7 % 2000 - 1000).toShort() },
            "one-block-and-one" to ShortArray(4097) { (it * 7 % 2000 - 1000).toShort() },
            "noise" to ShortArray(10_000) { random.nextInt(-32768, 32768).toShort() },
            "edges" to ShortArray(6000) { if (it % 2 == 0) Short.MAX_VALUE else Short.MIN_VALUE },
            "long" to ShortArray(16_000 * 31) {
                (12000 * sin(2 * PI * 150 * it / 16_000) * sin(2 * PI * 0.5 * it / 16_000))
                    .toInt().toShort()
            },
        )
    }

    @Test
    fun `every case is written with the marker, and speech comes out smaller`() {
        val home = File("build/flac").apply { mkdirs() }
        for ((name, samples) in cases) {
            val flac = Flac.encode(samples)
            assertArrayEquals("fLaC".toByteArray(), flac.copyOf(4))
            File(home, "$name.flac").writeBytes(flac)
            val raw = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            samples.forEach { raw.putShort(it) }
            File(home, "$name.raw").writeBytes(raw.array())
            assertArrayEquals(flac, Flac.encodePcm(raw.array()))
        }
        val speech = cases.getValue("speech-like")
        assertTrue(Flac.encode(speech).size < speech.size * 2)
    }

    /**
     * Real audio, when `bench/flac.py --stage` has put some there: the takes and renders of
     * the test set, as raw samples. Absent, there is nothing to do -- they are not frozen
     * into the repository, being the bench's and not the app's.
     */
    @Test
    fun `whatever real audio is staged is written for the reference decoder`() {
        val staged = File("build/flac-sources").listFiles { file -> file.extension == "raw" }
            ?: return
        val home = File("build/flac").apply { mkdirs() }
        for (raw in staged) {
            val bytes = raw.readBytes()
            File(home, "${raw.nameWithoutExtension}.flac").writeBytes(Flac.encodePcm(bytes))
            raw.copyTo(File(home, raw.name), overwrite = true)
        }
    }
}
