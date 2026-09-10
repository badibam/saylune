package app.saylune.analysis

import android.content.Context
import app.saylune.BuildConfig
import app.saylune.R
import app.saylune.embedded.AcousticMatrix
import app.saylune.embedded.AcousticPass
import app.saylune.embedded.Layer
import app.saylune.keys.Secret
import app.saylune.keys.SecretStore
import app.saylune.providers.Provider
import app.saylune.providers.Task
import kotlinx.coroutines.flow.first
import app.saylune.embedded.Added
import app.saylune.embedded.Affinity
import app.saylune.embedded.Alphabet
import app.saylune.embedded.Frames
import app.saylune.embedded.Grid
import app.saylune.embedded.Join
import app.saylune.embedded.inWhole
import app.saylune.embedded.Overlap
import app.saylune.embedded.Pitch
import app.saylune.embedded.Segment
import app.saylune.embedded.Sound
import app.saylune.embedded.Stress
import app.saylune.embedded.Syllables
import app.saylune.embedded.Readout
import app.saylune.debug.Trace
import app.saylune.judged.Kept
import app.saylune.marking.Syllable
import app.saylune.marking.TurnMarking
import app.saylune.embedded.Marks
import app.saylune.ui.NOISE_BAND
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
class EmbeddedAnalysis(
    private val context: Context,
    /**
     * Where the choice of pass is read, or null to stay on this device without asking.
     *
     * Read when the engine is built and not at every turn: the doc settles this once for a
     * conversation, and a reading computed here is not interchangeable with one computed
     * elsewhere -- switching mid-thread would put two eras of measurement in one fil
     * (`docs/design/remote-analysis.md`).
     */
    private val store: SecretStore? = null,
) : Analysis {

    private val lock = Mutex()
    private var engine: Engine? = null
    private var refused: Readiness.Off? = null

    /**
     * The set on disk the refusal was given against ([Weights.onDisk]).
     *
     * **A refusal lapses when the files change, and that is the whole of it.** Cached with no
     * such condition it outlived the very gesture that answers it: the app opened without the
     * model, said no once, and went on saying no for the life of the process -- so someone who
     * then brought the three files by hand saw the settings screen declare them verified while
     * the conversation kept the marks off, and only killing the app made it true. Read off the
     * files rather than counted from the gestures, so a piece pushed over the cable lifts it
     * exactly like one the picker brought.
     */
    private var refusedOver: String? = null

    private class Engine(
        /** Which pass this engine was built for, so a change of mind rebuilds it. */
        val chosen: String,
        val matrix: AcousticPass,
        val alphabet: Alphabet,
        val affinity: Affinity,
        val probe: Stress.Probe,
        /** What produced every reading this engine gives. See [stamp]. */
        val version: String,
    )

    override suspend fun readiness(): Readiness = lock.withLock {
        val (chosen, remote) = choice()
        engine?.let {
            if (it.chosen == chosen) return Readiness.On(it.version)
            // A different answer to "where does the pass run" is a different engine, and
            // the old one holds a session worth hundreds of megabytes.
            it.matrix.close()
            engine = null
            refused = null
        }
        val disk = "${Weights.onDisk(context)}|$chosen"
        refused?.let { if (refusedOver == disk) return it }
        // Settled once for the conversation and cached either way: the doc is explicit that
        // this is not a per-turn question, and a turn stays analysable as long as the
        // conversation started with its model loaded.
        val outcome = runCatching { load(chosen, remote) }
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
        refusedOver = disk
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
                modelReading.layer, saidReading.layer, engine.probe, step,
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
        modelLayer: Layer,
        saidLayer: Layer,
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
        val flags = Stress.flags(kept, sounds, segments, spanOf, modelLayer,
                                 saidLayer, probe)
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

    /**
     * Where the pass runs, and what identifies that choice.
     *
     * Nothing chosen means this device, which is not a default papering over a missing
     * setting: it is the app's own answer, it needs no key and no network, and it is what
     * `docs/reference.md` describes as the ordinary case.
     */
    private suspend fun choice(): Pair<String, RemoteMatrix?> {
        val values = store?.values()?.first() ?: return HERE to null
        // The same question the settings screen asks before it draws the menu: the local
        // pass is offered only where its weights are, exactly as a provider is offered only
        // where its key is.
        val weights = weights() != null
        if (Task.Analysis.chosen(values, weights) != Provider.AnalysisServer) {
            return HERE to null
        }
        val endpoint = values[Secret.AnalysisEndpoint].orEmpty()
        val token = values[Secret.AnalysisToken].orEmpty()
        if (endpoint.isBlank() || token.isBlank()) return HERE to null
        val remote = RemoteMatrix(endpoint, token)
        return remote.version to remote
    }

    private fun load(chosen: String, remote: RemoteMatrix?): Engine {
        // The reason names the directory and what is in it. "Nothing found" without saying
        // where it looked is the kind of message that costs an hour.
        val home = home()
        // The network is what the local pass needs, and only it: a remote pass has nothing
        // to load here, so demanding 358 MB of it would shut the door this link exists to
        // open -- the device that cannot hold the model is the one the server is for.
        val weights = weights()
        if (remote == null && weights == null) throw IllegalStateException(
            "no .onnx in $home (it holds: " +
                (home.list()?.joinToString(", ")?.ifEmpty { "nothing" }
                    ?: if (home.isDirectory) "unreadable" else "no such directory") + ")"
        )
        // Named from the directory and not from the network file, which a remote pass does
        // not have: these two are needed either way -- the alphabet cuts the grid, and the
        // probe's bias closes the stress score whichever machine projected it.
        val vocab = File(home, VOCAB)
        if (!vocab.isFile) throw IllegalStateException("$VOCAB is missing in $home")
        val probeFile = File(home, PROBE)
        if (!probeFile.isFile) throw IllegalStateException(
            "$PROBE is missing in $home -- bench/probe.py --json writes it"
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
        val here = stamp(weights, vocab, probeFile)
        // The remote pass is not loaded here -- there is nothing to load -- and it costs
        // neither the session nor the memory the local one holds.
        val pass = remote
            ?: AcousticMatrix(requireNotNull(weights), THREADS, here)
        return Engine(chosen, pass, alphabet, affinity, probe,
                      version = if (remote == null) here else "${remote.version}|$here")
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
    private fun stamp(weights: File?, vocab: File, probeFile: File): String {
        // No network here means the pass ran elsewhere, and what ran it is named by
        // the caller instead: what must never happen is two eras sharing one stamp.
        val network = weights?.let {
            val head = ByteArray(HEAD_BYTES)
            val read = it.inputStream().use { file -> file.read(head) }.coerceAtLeast(0)
            "w:${it.length()}-${digest(head.copyOf(read))}"
        } ?: "w:elsewhere"
        return listOf(
            network,
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
     * Where the weights sit, with `vocab.json` and `probe.json` beside them.
     *
     * The app's own directory, now that the app fetches them itself ([Weights]). It was
     * `/data/local/tmp` for as long as the shell had to push them, and that detour was
     * measured rather than chosen: an app cannot list a subdirectory of its own external
     * storage whose contents the shell created -- `list()` returns null while `isDirectory`
     * is true -- whereas SELinux lets it read `/data/local/tmp` and never write there.
     * Nothing in the analysis moved when this changed, which is what the seam was for.
     */
    private fun home(): File = Weights.home(context)

    /** The network [Weights] names, and never whatever `.onnx` happens to be lying there. */
    private fun weights(): File? =
        File(home(), Weights.NETWORK.name).takeIf { it.isFile }

    private companion object {
        /** Enough of the weights to tell two trained files apart, beside their size. */
        const val HEAD_BYTES = 64 * 1024

        const val VOCAB = "vocab.json"
        const val PROBE = "probe.json"
        const val LETTERS = "affinity.json"
        const val GROUPS = "affinity-groups.json"
        const val REDUCTIONS = "affinity-reductions.json"


        /** Stated rather than left to the machine: the reading has to be reproducible. */
        const val THREADS = 4

        /** What the choice of this device is called, where a remote one is called by its address. */
        const val HERE = "on-device"
    }
}
