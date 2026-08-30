package app.speakup.embedded

import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Renders every frozen turn the way the debug panel shows it, into `build/readout.txt`.
 *
 * Not an assertion about the numbers -- `PortTest` does that. This exists because a readout
 * is only worth having if it can be read, and the only way to know is to look at one. It
 * already earned its place: the first render showed every duration at 0.02 s, which is how
 * the peaky spans came to be widened for display.
 */
class ReadoutTest {

    @Test
    fun `every frozen turn renders`() {
        val frozen = File("src/testDebug/resources/fixture").listFiles()
            ?.filter { it.isDirectory }?.sortedBy { it.name }.orEmpty()
        assertTrue("no frozen turn -- generate one with bench/fixture.py", frozen.isNotEmpty())

        val affinity = Affinity.read(File("src/debug/assets/affinity.json"),
                                     File("src/debug/assets/affinity-groups.json"))
        val out = StringBuilder()
        frozen.forEach { turn ->
            val expected = JSONObject(File(turn, "expected.json").readText())
            val alphabet = Alphabet.read(File(turn, "vocab.json"))
            val model = frames(turn, "model.matrix")
            val said = frames(turn, "said.matrix")
            val reading = Overlap.sounds(model, said, alphabet)
            val segments = Grid.decode(model, alphabet)
            val text = expected.getString("text")
            val sounds = Join.joined(segments.map { alphabet[it.symbol] }, text, affinity)
            val drawn = Marks.drawn(reading.gaps, sounds, 5f)
            // The fixture does not carry the stride, and the readout only needs it to turn
            // frames into seconds. This model's is 20 ms.
            val step = 0.02f
            out.append("=== ").append(turn.name).append(" ===\n")
                .append(Readout.table(text, reading.gaps, sounds, drawn.phonemes,
                                      Added.found(segments.map { alphabet[it.symbol] },
                                                  Grid.decode(said, alphabet)
                                                      .map { alphabet[it.symbol] },
                                                  sounds, reading.gaps, text, affinity),
                                      reading.grid, reading.dropped, step, 5f))
                .append('\n')
                .append(Readout.spreads(reading.gaps, sounds, step))
                .append('\n')
        }
        File("build").mkdirs()
        File("build/readout.txt").writeText(out.toString())
    }

    private fun frames(turn: File, name: String): Frames {
        val bytes = ByteBuffer.wrap(File(turn, name).readBytes()).order(ByteOrder.LITTLE_ENDIAN)
        val count = bytes.int
        val width = bytes.int
        return Frames(count, width, FloatArray(count * width) { bytes.float })
    }
}
