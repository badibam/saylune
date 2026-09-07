package app.speakup.analysis

import android.content.Context
import app.speakup.BuildConfig
import app.speakup.R
import app.speakup.embedded.AcousticMatrix
import app.speakup.embedded.Added
import app.speakup.embedded.Affinity
import app.speakup.embedded.Alphabet
import app.speakup.embedded.Frames
import app.speakup.embedded.Grid
import app.speakup.embedded.Join
import app.speakup.embedded.inWhole
import app.speakup.embedded.Overlap
import app.speakup.embedded.Pitch
import app.speakup.embedded.Segment
import app.speakup.embedded.Sound
import app.speakup.embedded.Stress
import app.speakup.embedded.Syllables
import app.speakup.embedded.Readout
import app.speakup.debug.Trace
import app.speakup.judged.Kept
import app.speakup.marking.Syllable
import app.speakup.marking.TurnMarking
import app.speakup.embedded.Marks
import app.speakup.ui.NOISE_BAND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest
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
        val probe: Stress.Probe,
        /** What produced every reading this engine gives. See [stamp]. */
        val version: String,
    )

    override suspend fun readiness(): Readiness = lock.withLock {
        engine?.let { return Readiness.On(it.version) }
        refused?.let { return it }
        // Settled once for the conversation and cached either way: the doc is explicit that
        // this is not a per-turn question, and a turn stays analysable as long as the
        // conversation started with its model loaded.
        val outcome = runCatching { load() }
        outcome.getOrNull()?.let {
            engine = it
            Trace.add("analysis: ready", "weights" to weights()?.name,
                      "sounds" to it.alphabet.size.toString(), "version" to it.version)
            return Readiness.On(it.version)
        }
        val failure = outcome.exceptionOrNull()
        val off = Readiness.Off(R.string.analysis_engine_refused, failure?.message)
        Trace.fail("analysis: off for the session", "why" to failure?.message)
        refused = off
        return off
    }

    override suspend fun examine(said: File, model: File, text: String, kept: Kept): Analysed =
        withContext(Dispatchers.Default) {
            val engine = engine ?: error("examine before readiness said On")
            // The two are handed in separately and have to be the same string: every offset
            // that comes back is an offset into one of them, and a mismatch would slide every
            // mark by however much the two texts differ, silently.
            require(kept.whole == text) {
                "the kept stretches are of another turn: \"${kept.whole}\" against \"$text\""
            }

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
            // The model said the kept words alone, so it is to **those** that its sounds are
            // joined -- and the offsets they come back with are carried to the whole turn at
            // once, here and nowhere later. Everything downstream indexes into the displayed
            // string: the marks, the syllables, and above all `Added.found`, which sets this
            // join against the learner's own and can only do so in one frame of reference.
            val sounds = Join.joined(
                symbols = grid,
                text = kept.text,
                affinity = engine.affinity,
            ).inWhole(kept)

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


            // Read off the frames rather than declared: a candidate model that halves its
            // last stride doubles the resolution, and a duration is only worth reading if
            // it came from the pass that produced it.
            val step = modelReading.seconds / modelReading.frames

            // Where each of the learner's own sounds sits in his recording, widened as every
            // other span of the app is: the network is peaky, and a raw run of frames is not
            // something anyone can listen to. It is a **place** and never a duration -- what a
            // free decoding says is which sound wins frame by frame -- and it is what lets a
            // sound the learner added be played back at all.
            val freelyAt = Overlap
                .widened(freely.map { it.start..it.stop - 1 })
                .map { ms(it, step) }
            val added = Added.found(said = theirs, model = sounds, saidAt = freelyAt)
            Trace.add(
                "analysis: added sounds",
                "stretches" to added.size.toString(),
                "of their sounds placed" to
                    "${theirs.count { it.spots.isNotEmpty() || it.borrowed.isNotEmpty() }}" +
                        " / ${theirs.size}",
            )

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

            // The melody, syllable by syllable, and the stress of each, by the frozen
            // probe -- both read on the model's syllables, which the montage in service
            // already locates in the learner's recording.
            val melody = melody(
                sounds, reading.gaps, segments, model, said,
                modelReading.hidden, saidReading.hidden, engine.probe, step,
            )

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
                recorded = (saidReading.seconds * 1000).toInt(),
                rendered = (modelReading.seconds * 1000).toInt(),
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
                // Each one with the word it landed on, which the join beside it already
                // says: it is the only reading that covers the hesitations, so it is what
                // places a filler or an abandoned start in the recording.
                freely = freely.mapIndexed { index, segment ->
                    Heard(
                        symbol = engine.alphabet[segment.symbol],
                        at = ms(segment.start..segment.stop - 1, step),
                        word = theirs.getOrNull(index)?.wordAt,
                    )
                },
            )
        }

    private fun ms(span: IntRange, step: Float): IntRange =
        (span.first * step * 1000).toInt()..((span.last + 1) * step * 1000).toInt()

    /**
     * Bricks 10 and 7: one pitch and one stress flag per syllable, on either side.
     *
     * The learner is read on the **model's** syllables. The model is the source of truth and
     * the montage in service already says where each of its sounds sits in the learner's
     * recording, so both bricks cost no montage of their own -- they read positions and
     * never labels, which is the one thing the forced alignment gives away for free.
     *
     * A syllable no sound of which was compared is left out rather than guessed at: its
     * place in the learner's audio is precisely what is missing.
     */
    private fun melody(
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        segments: List<Segment>,
        model: File,
        said: File,
        modelHidden: FloatArray,
        saidHidden: FloatArray,
        probe: Stress.Probe,
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
        val flags = Stress.flags(kept, sounds, segments, spanOf, modelHidden,
                                 saidHidden, probe)
        val out = kept.indices.mapNotNull { i ->
            // A model syllable nothing voiced has no contour to be departed from, so it is
            // no more a mark than a silence is: it drops out of both lines at once.
            modelLine[i]?.let {
                Syllable(
                    start = kept[i].spots.min(),
                    end = kept[i].spots.max() + 1,
                    modelPitch = it,
                    learnerPitch = saidLine[i],
                    modelStressed = flags[i].first,
                    learnerStressed = flags[i].second,
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
            "stress marked on the model" to
                out.count { it.modelStressed }.toString(),
            "stress marked on the take" to
                out.count { it.learnerStressed }.toString(),
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
        val probeFile = File(weights.parentFile, PROBE)
        if (!probeFile.isFile) throw IllegalStateException(
            "$PROBE is missing beside $weights -- bench/probe.py --json writes it"
        )

        val alphabet = Alphabet.read(vocab)
        val probe = Stress.Probe.read(probeFile)
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
        return Engine(AcousticMatrix(weights, THREADS), alphabet, affinity, probe,
                      version = stamp(weights, vocab, probeFile))
    }

    /**
     * What produced a reading, in one line: the weights, the alphabet, and the app.
     *
     * The three are what a number depends on. The weights and the alphabet decide what the
     * matrix says; the app version stands for the affinity tables it ships and for the join
     * that reads them, which move the marks as surely as the weights do.
     *
     * **The weights are identified by their size and the digest of their first bytes, not by
     * their whole content.** A full digest of 359 MB is the prohibitive case the wisdom names,
     * and this is the compromise it prescribes, stated rather than hidden: two files would
     * have to share a size *and* a first 64 kB to be confused, which no pair of trained
     * weights does by accident. The alphabet is small, so it is hashed whole.
     *
     * Never the file's name or its path. A name is what a person typed when they pushed the
     * file, and it changes without the bytes changing -- the wisdom is explicit that identity
     * is the content.
     */
    private fun stamp(weights: File, vocab: File, probeFile: File): String {
        val head = ByteArray(HEAD_BYTES)
        val read = weights.inputStream().use { it.read(head) }.coerceAtLeast(0)
        return listOf(
            "w:${weights.length()}-${digest(head.copyOf(read))}",
            "a:${digest(vocab.readBytes())}",
            // Small enough to hash whole, and it decides the stress marks as
            // surely as the weights decide the sounds.
            "p:${digest(probeFile.readBytes())}",
            "app:${BuildConfig.VERSION_NAME}",
        ).joinToString("/")
    }

    private fun digest(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }.take(16)

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

        /** Enough of the weights to tell two trained files apart, beside their size. */
        const val HEAD_BYTES = 64 * 1024

        const val VOCAB = "vocab.json"
        const val PROBE = "probe.json"
        const val LETTERS = "affinity.json"
        const val GROUPS = "affinity-groups.json"
        const val REDUCTIONS = "affinity-reductions.json"


        /** Stated rather than left to the machine: the reading has to be reproducible. */
        const val THREADS = 4
    }
}
