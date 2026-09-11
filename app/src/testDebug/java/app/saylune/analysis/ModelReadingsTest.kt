package app.saylune.analysis

import app.saylune.embedded.Layer
import app.saylune.embedded.PassReading
import app.saylune.embedded.Stress
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files
import kotlin.random.Random

class ModelReadingsTest {

    private val width = 8
    private val probe = Random(3).let { random ->
        Stress.Probe(19, FloatArray(width) { random.nextFloat() },
                     FloatArray(width) { 0.5f + random.nextFloat() },
                     FloatArray(width) { random.nextFloat() - 0.5f }, -1.5f)
    }

    private fun reading(frames: Int) = Random(5).let { random ->
        PassReading(frames, 3, FloatArray(frames * 3) { random.nextFloat() },
                    Layer.Whole(FloatArray(frames * width) { random.nextFloat() }, width),
                    120, frames * 0.02f)
    }

    @Test
    fun `a kept reading comes back as it was handed out, without a second pass`() {
        val kept = Files.createTempDirectory("readings").toFile().resolve("m.reading")
        var passes = 0
        val first = ModelReadings.of(kept, probe) { passes++; reading(40) }
        val second = ModelReadings.of(kept, probe) { passes++; reading(40) }
        assertEquals(1, passes)
        assertArrayEquals(first.values, second.values, 0f)
        assertArrayEquals((first.layer as Layer.Folded).perFrame,
                          (second.layer as Layer.Folded).perFrame, 0f)
        assertEquals(first.seconds, second.seconds, 0f)
    }

    @Test
    fun `folding the layer leaves the stress where it was`() {
        val whole = reading(60).layer as Layer.Whole
        val places = listOf(3..9, 9..20, 20..31, 40..44)
        val before = Stress.parts(whole, places, probe)!!
        val after = Stress.parts(Stress.fold(whole, probe), places, probe)!!
        assertArrayEquals(before, after, 1e-5f)
    }
}
