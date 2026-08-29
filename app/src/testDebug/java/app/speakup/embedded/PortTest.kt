package app.speakup.embedded

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The Kotlin arithmetic against the Python it was ported from, on one frozen turn.
 *
 * The two exist side by side now, and two implementations that agreed about different
 * numbers would be worse than one that failed outright -- the same argument `concord.py`
 * makes about the network pass, which is why this does not repeat it: the matrices are read
 * from the fixture rather than recomputed, so a difference here can only come from the port.
 *
 * Every frozen turn under `src/testDebug/resources/fixture` is run, so covering a case the
 * port gets wrong is a matter of freezing one more pair rather than of editing this:
 *
 *     cd bench && ACOUSTIC_MODEL=timit-ipa python3 fixture.py 02-dont-know --model doesnt-know
 */
class PortTest {

    /**
     * Half of the last digit the fixture keeps. `turn.py` rounds points to two decimals on
     * its way to JSON and the port does not round at all, so this is the finest agreement
     * the fixture can express -- not a slack granted to the port. Two orders of magnitude
     * below anything that could move a mark, the ramp starting at 5 points.
     */
    private val tolerance = 0.005

    @Test
    fun `the marks match what the bench writes`() {
        val frozen = File("src/testDebug/resources/fixture").listFiles()
            ?.filter { it.isDirectory }?.sortedBy { it.name }.orEmpty()
        assertTrue("no frozen turn -- generate one with bench/fixture.py", frozen.isNotEmpty())
        frozen.forEach { check(it) }
    }

    private fun check(turn: File) {
        val expected = JSONObject(resource(turn, "expected.json").readText())
        val alphabet = Alphabet.read(resource(turn, "vocab.json"))
        val affinity = Affinity.read(asset("affinity.json"), asset("affinity-groups.json"))
        assertEquals("the table names sounds this model does not render",
                     emptyList<String>(), affinity.unknownTo(alphabet))

        val model = frames(turn, "model.matrix")
        val said = frames(turn, "said.matrix")

        val reading = Overlap.sounds(model, said, alphabet)
        val segments = Grid.decode(model, alphabet)
        val sounds = Join.joined(segments.map { alphabet[it.symbol] },
                                 expected.getString("text"), affinity)
        val drawn = Marks.drawn(reading.gaps, sounds)

        val wanted = expected.getJSONArray("phonemes")
        assertEquals("${turn.name}: number of marks", wanted.length(), drawn.phonemes.size)
        for (index in 0 until wanted.length()) {
            val row = wanted.getJSONObject(index)
            val got = drawn.phonemes[index]
            assertEquals("${turn.name}: mark $index start", row.getInt("start"), got.start)
            assertEquals("${turn.name}: mark $index end", row.getInt("end"), got.end)
            assertEquals("${turn.name}: mark $index points",
                         row.getDouble("points"), got.points.toDouble(), tolerance)
        }

        val gutters = expected.getJSONArray("gutters")
        assertEquals("${turn.name}: number of gutters", gutters.length(), drawn.gutters.size)
        for (index in 0 until gutters.length()) {
            val row = gutters.getJSONObject(index)
            val got = drawn.gutters[index]
            assertEquals("${turn.name}: gutter $index symbol", row.getString("symbol"), got.symbol)
            assertEquals("${turn.name}: gutter $index after", row.getInt("after"), got.after)
            assertEquals("${turn.name}: gutter $index points",
                         row.getDouble("points"), got.points.toDouble(), tolerance)
        }
    }

    /** Shape then rows, little-endian, exactly as `bench/fixture.py` writes them. */
    private fun frames(turn: File, name: String): Frames {
        val bytes = ByteBuffer.wrap(resource(turn, name).readBytes()).order(ByteOrder.LITTLE_ENDIAN)
        val count = bytes.int
        val width = bytes.int
        val values = FloatArray(count * width)
        for (index in values.indices) values[index] = bytes.float
        return Frames(count, width, values)
    }

    private fun resource(turn: File, name: String): File {
        val file = File(turn, name)
        assertTrue("$file is missing -- regenerate it with bench/fixture.py", file.isFile)
        return file
    }

    /** The very tables the debug build ships, read where they live rather than copied. */
    private fun asset(name: String) = File("src/debug/assets", name)
}
