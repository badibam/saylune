package app.saylune.embedded

import android.app.Activity
import android.os.Bundle
import android.os.Debug
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

/**
 * The proof of concept, driven from the bench rather than by hand.
 *
 * It is started by `bench/phone.py`, reads whatever the bench pushed beside the
 * weights, and writes its matrices back for the same script to pull and compare.
 * No screen, no button: a measuring instrument whose numbers depend on someone
 * tapping at the right moment is not one.
 *
 *     adb shell am start -n app.saylune.debug/app.saylune.embedded.ProbeActivity
 */
class ProbeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val threads = intent.getIntExtra("threads", 4)
        val root = intent.getStringExtra("root") ?: DEFAULT_ROOT
        // Off the main thread: a pass takes seconds, and Android kills an
        // activity that stops answering -- which would look like a model too
        // heavy for the device rather than a probe written badly.
        thread { run(File(root), threads) }
    }

    private fun run(root: File, threads: Int) {
        // Found rather than named: past a size the exporter leaves the tensors
        // in a sidecar the graph looks up by its own file name, so the weights
        // have to keep the name they were exported under.
        val weights = root.listFiles { file -> file.name.endsWith(".onnx") }
            ?.firstOrNull() ?: File(root, "absent.onnx")
        val takes = File(root, "audio").listFiles()?.sortedBy { it.name }.orEmpty()
        // Read from one place, written to another, and not by taste: SELinux
        // lets an app read /data/local/tmp and never write there. What comes
        // back therefore leaves by the app's own external directory, which the
        // shell may read even though it may not usefully write it.
        val dumps = File(getExternalFilesDir(null), "matrices").apply { mkdirs() }

        say("lu depuis ${root.absolutePath}")
        say("écrit dans ${dumps.absolutePath}")
        if (!weights.isFile || takes.isEmpty()) {
            say("FIN rien à lire — poids ${weights.isFile}, ${takes.size} fichiers")
            return
        }
        say("poids ${weights.length() / 1_000_000} Mo, ${takes.size} fichiers, "
            + "$threads fils, ${Runtime.getRuntime().availableProcessors()} cœurs")

        val before = residentKilobytes()
        val engine = try {
            AcousticMatrix(weights, threads)
        } catch (failure: Throwable) {
            say("FIN chargement impossible : $failure")
            return
        }
        val loaded = residentKilobytes()
        say("chargé en ${engine.loadMillis} ms, "
            + "empreinte ${before / 1024} → ${loaded / 1024} Mo "
            + "(+${(loaded - before) / 1024})")

        var peak = loaded
        for (wav in takes) {
            try {
                val reading = engine.read(wav)
                write(File(dumps, wav.name.removeSuffix(".wav") + ".mat"), reading)
                val held = residentKilobytes()
                peak = maxOf(peak, held)
                say("%-30s %5.2f s  %6d ms  x%.2f  %4d trames  %5d Mo".format(
                    wav.name, reading.seconds, reading.millis,
                    reading.millis / 1000f / reading.seconds,
                    reading.frames, held / 1024))
                if (wav === takes.first()) {
                    // Twice on the same file, because determinism is a
                    // qualification criterion and not an assumption: comparing
                    // a learner to a model measures the engine's own noise
                    // otherwise.
                    val again = engine.read(wav)
                    val steady = again.values.contentEquals(reading.values)
                    say("relu à l'identique : $steady, ${again.millis} ms")
                }
            } catch (failure: Throwable) {
                say("${wav.name} ÉCHEC $failure")
            }
        }
        engine.close()
        say("empreinte maximale ${peak / 1024} Mo")
        say("FIN")
    }

    /**
     * What the process actually holds in RAM, weights included.
     *
     * The heap says nothing useful here: the weights live in native memory
     * mapped by the runtime, and a Java-side figure would report a model of
     * several hundred megabytes as costing nothing.
     */
    private fun residentKilobytes(): Long {
        val info = Debug.MemoryInfo()
        Debug.getMemoryInfo(info)
        return info.totalPss.toLong()
    }

    /**
     * The matrix, raw: two 32-bit counts then the values, row by row.
     *
     * A format with nothing in it to get wrong, because what is being tested is
     * the network and not a serialiser. `bench/phone.py` holds the other half.
     */
    private fun write(destination: File, reading: PassReading) {
        val buffer = ByteBuffer.allocate(8 + reading.values.size * 4)
            .order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(reading.frames)
        buffer.putInt(reading.symbols)
        for (value in reading.values) buffer.putFloat(value)
        destination.writeBytes(buffer.array())
    }

    private fun say(line: String) = Log.i(TAG, line)

    companion object {
        const val TAG = "saylune.probe"

        /**
         * Not the app's own external directory, which is where this belongs and
         * where it cannot live: since Android 11 a directory pushed into
         * `Android/data` belongs to the shell user and the app is refused entry
         * to it. `/data/local/tmp` is the path both sides can reach -- the bench
         * writes it, the app opens it by name -- and it is a debug arrangement,
         * not a design.
         */
        const val DEFAULT_ROOT = "/data/local/tmp/saylune-probe"
    }
}
