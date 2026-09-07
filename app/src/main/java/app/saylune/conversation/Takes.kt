package app.saylune.conversation

import android.content.Context
import app.saylune.analysis.Analysed
import app.saylune.chain.Word
import app.saylune.debug.Trace
import app.saylune.fluency.Fluency
import app.saylune.analysis.timed
import app.saylune.judged.Marked
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Every turn kept on disk, audio included, as material for the bench.
 *
 * **This is a deliberate divergence from `docs/reference.md`, and only the debug build makes
 * it.** The doc no longer purges the learner's voice by default and leaves the purge to be
 * redefined, but it has never asked for this either: a second copy of every take, written
 * beside the buffer that already holds it. What justifies it is that the measures the
 * project still owes -- the false-alarm rate on a spontaneous turn, the two benches of
 * chantier 2 -- can only be made on real turns, and a turn not kept is a turn measured
 * never.
 *
 * So the rule stands for the release, which keeps none of this, and this is an instrument
 * rather than a behaviour of the app. It is written down as temporary
 * (`../../../../../../TODO.md`); when the benches have their material it goes.
 *
 * A turn is kept whether or not it was analysed. A turn the grammatical gate held back is
 * not analysed, and it is exactly the material the fidelity bench is short of: a real
 * learner fault the recognition could not have guessed.
 *
 * **Every take names the turn it belongs to and its rank among that turn's attempts.** Saying
 * a sentence again writes another folder, and without those two fields the only way back to
 * which turn it repeated was the identical `text` and the order of the timestamps -- so two
 * turns carrying the same sentence were indistinguishable, and a session of retries was a
 * pile to reassemble by hand. A learner saying one sentence three times against one model is
 * material no bench has; it is only material if the three stay tied together.
 */
object Takes {

    /** Written by the app and read by `adb pull`, which is the direction that works. */
    private const val DIR = "takes"

    fun keep(
        context: Context,
        said: File,
        model: File?,
        heard: List<Word>,
        intended: String,
        faulty: Boolean,
        analysed: Analysed?,
        /** What the judge marked of the stumbling, which says which words the model said. */
        stumbling: List<Marked> = emptyList(),
        /** A second take of the same sentence, measured against the same model. */
        redo: Boolean = false,
        /** The turn this repeats, or null when this take is the turn's first. */
        turn: String? = null,
        /** Its rank among that turn's takes, the first being 1. */
        attempt: Int = 1,
    ): String? {
        if (!Trace.on) return null
        return runCatching {
            // Saying a sentence again lands inside the same second easily, and two takes
            // in one folder is one take lost with nothing to say so.
            val home = File(context.getExternalFilesDir(null), DIR)
            val clock = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date())
            var stamp = clock
            var again = 2
            while (File(home, stamp).exists()) stamp = "$clock-${again++}"
            val into = File(home, stamp).apply { mkdirs() }

            link(said, File(into, "said.wav"))
            model?.copyTo(File(into, "model.wav"), overwrite = true)

            // Read once and used twice, by the trace and by the file: it is a walk of the
            // marks and the sounds, and two walks that could disagree would be two figures.
            val timed = analysed?.timed(stumbling)

            val kept = JSONObject()
                .put("at", stamp)
                // Its own stamp when it is the first: a turn is named by its first take, so
                // the name exists before there is anything to compare it to.
                .put("turn", turn ?: stamp)
                .put("attempt", attempt)
                .put("text", intended)
                .put("heard", heard.joinToString(" ") { it.text })
                .put("faulty", faulty)
                .put("analysed", analysed != null)
                .put("redo", redo)
                // **Where a timing survives.** Every step is timed against the start of the
                // turn and goes to logcat, and on this phone logcat keeps 81 seconds: some
                // system service floods a 256 kB ring, so a turn is erased before it can be
                // read. Written here, a latency stays attached to the take it belongs to and
                // is still there a week later, which is what comparing two ways of running
                // the chain needs. Names and moments only -- see `Trace.ofTurn`.
                .put("steps", JSONArray().apply {
                    Trace.ofTurn().forEach {
                        put(JSONObject().put("name", it.name).put("atMs", it.atMs)
                            .apply { if (it.failed) put("failed", true) })
                    }
                })
            if (analysed != null && timed != null) {
                // `phonemes` keeps the shape `bench/turn.py` writes, so the bench reads a
                // turn from the phone the way it reads one of its own.
                kept.put("phonemes", JSONArray().apply {
                    analysed.marking.phonemes.forEach {
                        put(JSONObject().put("start", it.start).put("end", it.end)
                            .put("points", it.points))
                    }
                })
                // The melody, which no kept take carried until now: a contour that looks
                // wrong on screen could not be checked against anything afterwards, because
                // the only place these numbers went was a logcat this phone keeps for 81
                // seconds. `learnerPitch` is null where nothing of the syllable was voiced,
                // which is not the same as flat and must not be written as a number.
                kept.put("syllables", JSONArray().apply {
                    analysed.marking.syllables.forEach {
                        put(JSONObject().put("start", it.start).put("end", it.end)
                            .put("modelPitch", it.modelPitch)
                            .put("learnerPitch", it.learnerPitch ?: JSONObject.NULL)
                            .put("modelStressed", it.modelStressed)
                            .put("learnerStressed", it.learnerStressed))
                    }
                })
                kept.put("gutters", JSONArray().apply {
                    analysed.marking.gutters.forEach {
                        put(JSONObject().put("symbol", it.symbol).put("after", it.after)
                            .put("points", it.points))
                    }
                })
                kept.put("words", JSONArray().apply {
                    analysed.marking.words.forEach {
                        put(JSONObject().put("start", it.start).put("end", it.end))
                    }
                })
                kept.put("added", JSONArray().apply {
                    analysed.added.forEach {
                        put(JSONObject().put("symbol", it.symbol)
                            .put("after", it.after))
                    }
                })
                // What the network heard of the learner, in order. No mark reads it; a
                // take read back does, and without it a take cannot be told apart from a
                // take read back wrongly.
                kept.put("freely", JSONArray().apply {
                    analysed.freely.forEach {
                        put(JSONObject().put("symbol", it.symbol)
                            .put("at", JSONArray(listOf(it.at.first, it.at.last)))
                            // The word it landed on, which is what places a hesitation in
                            // the recording -- the model's grid never held one.
                            .put("word", it.word?.let { w ->
                                JSONArray(listOf(w.first, w.last))
                            } ?: JSONObject.NULL))
                    }
                })
                kept.put("dropped", analysed.dropped)
                // **The four fluency sheets, and the word times they are read off.** They go
                // here because this is where the calibration bench finds its matter: their
                // series -- what counts as a C in seconds, in points of percentage -- are
                // written on real takes and nowhere else, and a figure that only ever reached
                // a logcat this phone keeps for 81 seconds could not be one of them. `model`
                // is null on a word the model never said, which is every hesitation.
                kept.put("fluency", JSONObject()
                    .put("recorded", timed.recorded)
                    .put("rendered", timed.rendered)
                    .put("continuity", Fluency.continuity(timed) ?: JSONObject.NULL)
                    .put("longestSilence", Fluency.longestSilence(timed))
                    .put("rate", Fluency.rate(timed) ?: JSONObject.NULL)
                    .put("blanks", JSONArray(Fluency.blanks(timed)))
                    .put("words", JSONArray().apply {
                        timed.spoken.forEach { word ->
                            put(JSONObject()
                                .put("notch", word.notch)
                                .put("said", JSONArray(listOf(word.said.from, word.said.to)))
                                .put("model", word.model?.let {
                                    JSONArray(listOf(it.from, it.to))
                                } ?: JSONObject.NULL))
                        }
                    }))
                kept.put("sounds", JSONArray().apply {
                    analysed.sounds.forEach { sound ->
                        put(JSONObject()
                            .put("symbol", sound.symbol)
                            .put("points", sound.points)
                            .put("letters", sound.letters)
                            .put("borrowed", sound.borrowed)
                            .put("modelMs", JSONArray(listOf(sound.modelMs.first, sound.modelMs.last)))
                            .put("saidMs", JSONArray(listOf(sound.saidMs.first, sound.saidMs.last)))
                            .put("model", shares(sound.model))
                            .put("said", shares(sound.said)))
                    }
                })
            }
            // Written aside then renamed: a turn interrupted mid-write would otherwise leave
            // a truncated JSON beside two good wavs, and the bench would read it as a turn.
            val part = File(into, "turn.json.part")
            part.writeText(kept.toString(2) + "\n", Charsets.UTF_8)
            part.renameTo(File(into, "turn.json"))

            timed?.let { turn ->
                Trace.add(
                    "fluency",
                    "words said / model's" to
                        "${turn.spoken.size} / ${turn.spoken.count { word -> word.model != null }}",
                    "continuity" to Fluency.continuity(turn)?.let { v -> "%.1f pts".format(v) },
                    "longest silence" to "%.2f s".format(Fluency.longestSilence(turn)),
                    "rate" to Fluency.rate(turn)?.let { v -> "%.1f %%".format(v) },
                )
            }
            Trace.add("take kept", "where" to into.path,
                      "turn" to (turn ?: stamp), "attempt" to attempt.toString())
            stamp
        }.onFailure {
            // Keeping a take is bench material, never the turn. It failing must not cost the
            // learner the conversation -- but it says so rather than passing for a turn that
            // was kept.
            Trace.fail("take not kept", "why" to it.message)
        }.getOrNull()
    }

    /**
     * Put the take's audio at [into] without keeping a second copy of it where that works.
     *
     * The measure of 2026-09-01 found the learner's wav on the phone twice, 26 MB against
     * 29 MB, the same audio: this folder is on external storage so that `adb pull` can reach
     * it, while the recording lives in internal storage where the store names it. A hard link
     * is the same bytes under two names and removes the duplicate outright.
     *
     * It only works when the two are one filesystem, which nothing here can promise, so a
     * copy stands behind it -- and which of the two happened is said rather than swallowed,
     * because the difference is tens of megabytes and it is invisible from the outside.
     */
    private fun link(said: File, into: File) {
        into.delete()
        val linked = runCatching {
            java.nio.file.Files.createLink(into.toPath(), said.toPath())
        }.isSuccess
        if (!linked) said.copyTo(into, overwrite = true)
        Trace.add("take audio", "how" to if (linked) "linked" else "copied")
    }

    private fun shares(shares: List<app.saylune.analysis.Share>) = JSONArray().apply {
        shares.forEach { put(JSONObject().put("symbol", it.symbol).put("part", it.part)) }
    }
}
