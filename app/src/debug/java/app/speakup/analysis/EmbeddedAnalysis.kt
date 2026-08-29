package app.speakup.analysis

import android.content.Context
import app.speakup.R
import app.speakup.embedded.AcousticMatrix
import app.speakup.embedded.Affinity
import app.speakup.embedded.Alphabet
import app.speakup.embedded.Frames
import app.speakup.embedded.Grid
import app.speakup.embedded.Join
import app.speakup.embedded.Marks
import app.speakup.embedded.Overlap
import app.speakup.debug.Trace
import app.speakup.marking.TurnMarking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The analysis on the device: the acoustic model, the grid, the comparison, the anchoring.
 *
 * It composes four bricks that each stay replaceable, because none of them is settled. Which
 * acoustic model the app ships with is open, so [AcousticMatrix] is behind a file rather than
 * a name; the arithmetic above it reads only a matrix, so it survives a change of engine.
 *
 * Everything here consults exactly two outside files, and their difference carries the whole
 * principle (`docs/reference.md`): the weights of a free acoustic model, and a table that
 * says where to *paint* -- never a norm that says what is *correct*. No pronunciation
 * dictionary, no dialect lexicon, no reference of rightness. What decides whether a take is
 * at fault is the gap to the model, and nothing else.
 */
class EmbeddedAnalysis(private val context: Context) : Analysis {

    private val lock = Mutex()
    private var engine: Engine? = null
    private var refused: Readiness.Off? = null

    private class Engine(
        val matrix: AcousticMatrix,
        val alphabet: Alphabet,
        val affinity: Affinity,
    )

    override suspend fun readiness(): Readiness = lock.withLock {
        engine?.let { return Readiness.On }
        refused?.let { return it }
        // Settled once for the session and cached either way: the doc is explicit that this
        // is not a per-turn question, and a turn stays analysable as long as the session
        // started with its model loaded.
        val outcome = runCatching { load() }
        outcome.getOrNull()?.let {
            engine = it
            Trace.add("analysis: ready", "weights" to weights()?.name, "sounds" to it.alphabet.size.toString())
            return Readiness.On
        }
        val failure = outcome.exceptionOrNull()
        val off = Readiness.Off(R.string.analysis_engine_refused, failure?.message)
        Trace.fail("analysis: off for the session", "why" to failure?.message)
        refused = off
        return off
    }

    override suspend fun examine(said: File, model: File, text: String): Analysed =
        withContext(Dispatchers.Default) {
            val engine = engine ?: error("examine before readiness said On")

            // Both readings walk the same grid, decoded once from the model, so the two
            // face each other sound for sound.
            val modelFrames = Frames.of(engine.matrix.read(model))
            val saidFrames = Frames.of(engine.matrix.read(said))
            val reading = Overlap.sounds(modelFrames, saidFrames, engine.alphabet)

            val segments = Grid.decode(modelFrames, engine.alphabet)
            val sounds = Join.joined(
                symbols = segments.map { engine.alphabet[it.symbol] },
                text = text,
                affinity = engine.affinity,
            )

            val drawn = Marks.drawn(reading.gaps, sounds)

            Trace.add(
                "analysis: examined",
                "sounds in the grid" to reading.grid.toString(),
                "compared" to reading.gaps.size.toString(),
                "dropped" to reading.dropped.toString(),
                "marks" to drawn.phonemes.size.toString(),
                "gutters" to drawn.gutters.size.toString(),
            )

            // Stress and melody stay empty, and that is not an omission to fill in later
            // with something plausible: a syllable the screen draws carries which side is
            // stressed and each side's pitch, all of them bricks 7 and 10. Inventing them
            // would put a false accent on the screen.
            Analysed(
                marking = TurnMarking(text = text, syllables = emptyList(), phonemes = drawn.phonemes),
                gutters = drawn.gutters,
                dropped = reading.dropped,
            )
        }

    private fun load(): Engine {
        val weights = weights() ?: throw IllegalStateException("no .onnx beside the app")
        val vocab = File(weights.parentFile, VOCAB)
        if (!vocab.isFile) throw IllegalStateException("${VOCAB} is missing beside the weights")

        val alphabet = Alphabet.read(vocab)
        val affinity = context.assets.let { assets ->
            val letters = File(context.cacheDir, LETTERS).also { copy(LETTERS, it) }
            val groups = File(context.cacheDir, GROUPS).also { copy(GROUPS, it) }
            Affinity.read(letters, groups)
        }
        val unknown = affinity.unknownTo(alphabet)
        if (unknown.isNotEmpty()) {
            // A table written for another alphabet does not fail on its own: every lookup
            // misses and the join quietly returns what order alone can do.
            throw IllegalStateException(
                "the affinity table names sounds this model does not render: " +
                    unknown.joinToString(" ")
            )
        }
        return Engine(AcousticMatrix(weights, THREADS), alphabet, affinity)
    }

    private fun copy(name: String, into: File) {
        if (into.isFile && into.length() > 0) return
        context.assets.open(name).use { source -> into.outputStream().use { source.copyTo(it) } }
    }

    /**
     * Where the weights sit: a directory of its own, with `vocab.json` beside them.
     *
     * Pushed with `adb push` for now. The 359 MB opt-in download the doc calls for is not
     * written, and this stands in its place (`../../../../../../../TODO.md`) -- but the
     * directory is the analysis's own rather than borrowed from the probe, so the day the
     * download exists it fills this and nothing else moves.
     */
    private fun home(): File? =
        context.getExternalFilesDir(null)?.let { File(it, "analysis") }

    private fun weights(): File? =
        home()?.listFiles { file -> file.name.endsWith(".onnx") }?.firstOrNull()

    private companion object {
        const val VOCAB = "vocab.json"
        const val LETTERS = "affinity.json"
        const val GROUPS = "affinity-groups.json"


        /** Stated rather than left to the machine: the reading has to be reproducible. */
        const val THREADS = 4
    }
}
