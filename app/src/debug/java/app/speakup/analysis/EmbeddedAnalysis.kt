package app.speakup.analysis

import android.content.Context
import app.speakup.R
import app.speakup.embedded.AcousticMatrix
import app.speakup.embedded.Added
import app.speakup.embedded.Affinity
import app.speakup.embedded.Alphabet
import app.speakup.embedded.Frames
import app.speakup.embedded.Grid
import app.speakup.embedded.Join
import app.speakup.embedded.Overlap
import app.speakup.embedded.Pitch
import app.speakup.embedded.Sound
import app.speakup.embedded.Syllables
import app.speakup.embedded.Readout
import app.speakup.debug.Trace
import app.speakup.marking.Syllable
import app.speakup.marking.TurnMarking
import app.speakup.embedded.Marks
import app.speakup.ui.NOISE_BAND
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

            // Bounded on both sides, or its cost is read as the synthesis call before it:
            // the first step it wrote came after two network passes and every join.
            Trace.add("analysis: begun")

            // Both readings walk the same grid, decoded once from the model, so the two
            // face each other sound for sound.
            val modelReading = engine.matrix.read(model)
            val saidReading = engine.matrix.read(said)
            val modelFrames = Frames.of(modelReading)
            val saidFrames = Frames.of(saidReading)
            val reading = Overlap.sounds(modelFrames, saidFrames, engine.alphabet)

            val segments = Grid.decode(modelFrames, engine.alphabet)
            val grid = segments.map { engine.alphabet[it.symbol] }
            val sounds = Join.joined(
                symbols = grid,
                text = text,
                affinity = engine.affinity,
            )

            val drawn = Marks.drawn(reading.gaps, sounds, NOISE_BAND)

            // Widened, and raw for what was measured: the network is peaky, a raw span is a
            // frame or two, and an extract of one frame is not something anyone can listen
            // to. Nothing in the measure reads these -- the screen and the ear do.
            val modelAt = Overlap.widened(reading.gaps.map { it.at })
            val saidAt = Overlap.widened(reading.gaps.map { it.span })

            // The one thing the grid cannot hold: the grid is the model's, so a sound the
            // learner added has no slot in it. What finds them is this same join, pointed at
            // the learner's own decoding instead of the model's -- the letters then hold
            // both readings, and what lands on none of them belongs to no word.
            val freely = Grid.decode(saidFrames, engine.alphabet)
            val theirs = if (freely.isEmpty()) emptyList() else Join.joined(
                symbols = freely.map { engine.alphabet[it.symbol] },
                text = text,
                affinity = engine.affinity,
            )
            val added = Added.found(said = theirs, model = sounds)
            Trace.add(
                "analysis: added sounds",
                "stretches" to added.size.toString(),
                "of their sounds placed" to
                    "${theirs.count { it.spots.isNotEmpty() || it.borrowed.isNotEmpty() }}" +
                        " / ${theirs.size}",
            )


            // Read off the frames rather than declared: a candidate model that halves its
            // last stride doubles the resolution, and a duration is only worth reading if
            // it came from the pass that produced it.
            val step = modelReading.seconds / modelReading.frames

            val points = drawn.phonemes.map { it.points }.sorted()
            Trace.add(
                "analysis: examined",
                "painted / entries" to
                    "${points.count { it > NOISE_BAND }} / ${points.size} over $NOISE_BAND points",
                "points low / median / high" to points.takeIf { it.isNotEmpty() }?.let {
                    "%.1f / %.1f / %.1f".format(it.first(), it[it.size / 2], it.last())
                },
            )
            // To the log alone. On the screen these two wrapped into nonsense; what the
            // screen shows is laid out instead, under the turn itself.
            Trace.wide(
                "analysis: the whole table, times included",
                "sounds" to Readout.table(
                    text, reading.gaps, sounds, drawn.phonemes, added,
                    reading.grid, reading.dropped, step, NOISE_BAND,
                ),
                "spreads" to Readout.spreads(reading.gaps, sounds, step),
            )

            // The melody, syllable by syllable. Stress stays false on both sides, and that
            // is not an omission to fill in later with something plausible: every way of
            // reading it has been measured and none holds -- naming the strong syllable on
            // each side costs 24.9 % false alarm, and the three profile channels see 3, 14
            // and 6 faults of 70 at 5 % (`../../../../../../TODO.md`). Inventing a stress
            // would put a false accent on the screen; leaving it false leaves the two
            // rulers unlit, which is what "not measured" looks like.
            val melody = melody(text, sounds, reading.gaps, model, said, step)

            Analysed(
                marking = TurnMarking(
                    text = text,
                    syllables = melody,
                    phonemes = drawn.phonemes,
                    words = drawn.words,
                    added = added,
                    gutters = drawn.gutters,
                ),
                dropped = reading.dropped,
                sounds = reading.gaps.mapIndexed { index, gap ->
                    val sound = sounds[gap.rank]
                    AnalysedSound(
                        symbol = gap.symbol,
                        points = gap.value * Marks.POINTS,
                        letters = sound.letters.ifEmpty {
                            sound.borrowed.joinToString("") { text[it].toString() }
                        },
                        borrowed = sound.letters.isEmpty() && sound.borrowed.isNotEmpty(),
                        // What it owns, never what it borrowed: a borrowed letter belongs to
                        // the neighbour that took it, and claiming it here would show it
                        // twice in a readout laid out on the text.
                        at = sound.spots.minOrNull()?.let { it..sound.spots.max() }
                            ?: (0 until 0),
                        model = gap.model.map { Share(it.symbol, it.part) },
                        said = gap.said.map { Share(it.symbol, it.part) },
                        modelMs = ms(modelAt[index], step),
                        saidMs = ms(saidAt[index], step),
                    )
                },
                added = added,
                freely = freely.map {
                    Heard(engine.alphabet[it.symbol], ms(it.start..it.stop - 1, step))
                },
            )
        }

    private fun ms(span: IntRange, step: Float): IntRange =
        (span.first * step * 1000).toInt()..((span.last + 1) * step * 1000).toInt()

    /**
     * Brick 10: one pitch per syllable, on either side, anchored to the letters.
     *
     * The learner is read on the **model's** syllables. The model is the source of truth and
     * the montage in service already says where each of its sounds sits in the learner's
     * recording, so the melody costs no montage of its own -- it reads positions and never
     * labels, which is the one thing the forced alignment gives away for free.
     *
     * A syllable no sound of which was compared is left out rather than guessed at: its
     * place in the learner's audio is precisely what is missing.
     */
    private fun melody(
        text: String,
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        model: File,
        said: File,
        step: Float,
    ): List<Syllable> {
        val cuts = Syllables.cut(sounds).filter { it.spots.isNotEmpty() }
        if (cuts.isEmpty()) return emptyList()
        val spanOf = gaps.associate { it.rank to (it.at to it.span) }

        val kept = mutableListOf<Syllables.Cut>()
        val mine = mutableListOf<Float>()
        val theirs = mutableListOf<Float>()
        val modelTrack = Pitch.track(model)
        val saidTrack = Pitch.track(said)
        for (cut in cuts) {
            val held = cut.sounds.mapNotNull { spanOf[it] }
            if (held.isEmpty()) continue
            val here = ms(held.minOf { it.first.first }..held.maxOf { it.first.last }, step)
            val there = ms(held.minOf { it.second.first }..held.maxOf { it.second.last }, step)
            kept.add(cut)
            mine.add(modelTrack.over(here.first, here.last))
            theirs.add(saidTrack.over(there.first, there.last))
        }
        if (kept.isEmpty()) return emptyList()

        val modelLine = Pitch.semitones(mine)
        val saidLine = Pitch.semitones(theirs)
        val out = kept.indices.mapNotNull { i ->
            // A model syllable nothing voiced has no contour to be departed from, so it is
            // no more a mark than a silence is: it drops out of both lines at once.
            modelLine[i]?.let {
                Syllable(
                    start = kept[i].spots.min(),
                    end = kept[i].spots.max() + 1,
                    modelPitch = it,
                    learnerPitch = saidLine[i],
                    modelStressed = false,
                    learnerStressed = false,
                )
            }
        }
        Trace.add(
            "analysis: melody",
            "syllables" to "${out.size} / ${cuts.size} cut",
            "unvoiced in the take" to out.count { it.learnerPitch == null }.toString(),
            "worst gap in semitones" to out.mapNotNull { s ->
                s.learnerPitch?.let { kotlin.math.abs(s.modelPitch - it) }
            }.maxOrNull()?.let { "%.1f".format(it) },
        )
        return out
    }

    private fun load(): Engine {
        // The reason names the directory and what is in it. "Nothing found" without saying
        // where it looked is the kind of message that costs an hour.
        val home = home()
        val weights = weights() ?: throw IllegalStateException(
            "no .onnx in $home (it holds: " +
                (home.list()?.joinToString(", ")?.ifEmpty { "nothing" }
                    ?: if (home.isDirectory) "unreadable" else "no such directory") + ")"
        )
        val vocab = File(weights.parentFile, VOCAB)
        if (!vocab.isFile) throw IllegalStateException("$VOCAB is missing beside $weights")

        val alphabet = Alphabet.read(vocab)
        val affinity = context.assets.let { assets ->
            val letters = File(context.cacheDir, LETTERS).also { copy(LETTERS, it) }
            val groups = File(context.cacheDir, GROUPS).also { copy(GROUPS, it) }
            val reductions = File(context.cacheDir, REDUCTIONS).also { copy(REDUCTIONS, it) }
            Affinity.read(letters, groups, reductions)
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
     * Where the weights sit, with `vocab.json` beside them.
     *
     * Not the app's own external directory, and that is measured rather than assumed: the
     * app cannot list a subdirectory of it whose contents the shell created -- `list()`
     * returns null while `isDirectory` is true. `/data/local/tmp` is the other way round,
     * which is the arrangement `ProbeActivity` already runs on: SELinux lets an app read
     * there and never write, and the shell may write there.
     *
     * That asymmetry is the whole reason this is a provisional. The doc calls for a 359 MB
     * opt-in download, and an app writing its own storage would read it back with no
     * question of permission at all. The download is not written
     * (`../../../../../../../TODO.md`); when it is, this returns the app's own directory
     * and nothing else in the analysis moves.
     */
    private fun home(): File = File(HOME)

    private fun weights(): File? =
        home().listFiles { file -> file.name.endsWith(".onnx") }?.firstOrNull()

    private companion object {
        /** Where `adb push` can write and the app can read. Provisional, see [home]. */
        const val HOME = "/data/local/tmp/speakup-analysis"

        const val VOCAB = "vocab.json"
        const val LETTERS = "affinity.json"
        const val GROUPS = "affinity-groups.json"
        const val REDUCTIONS = "affinity-reductions.json"


        /** Stated rather than left to the machine: the reading has to be reproducible. */
        const val THREADS = 4
    }
}
