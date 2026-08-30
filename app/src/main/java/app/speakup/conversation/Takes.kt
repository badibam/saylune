package app.speakup.conversation

import android.content.Context
import app.speakup.analysis.Analysed
import app.speakup.chain.Word
import app.speakup.debug.Trace
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
 * it.** The doc decides that the learner's voice is purged when the session closes and that
 * no store of voice sleeps on the device, for a reason that still holds: past its own turn
 * nothing consumes it, and the independence of turns forbids a later use. What the doc did
 * not weigh is that the measures the project still owes -- the false-alarm rate on a
 * spontaneous turn, the shape of the marking, the two benches of chantier 2 -- can only be
 * made on real turns, and a turn not kept is a turn measured never.
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

            said.copyTo(File(into, "said.wav"), overwrite = true)
            model?.copyTo(File(into, "model.wav"), overwrite = true)

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
            if (analysed != null) {
                // `phonemes` keeps the shape `bench/turn.py` writes, so the bench reads a
                // turn from the phone the way it reads one of its own.
                kept.put("phonemes", JSONArray().apply {
                    analysed.marking.phonemes.forEach {
                        put(JSONObject().put("start", it.start).put("end", it.end)
                            .put("points", it.points))
                    }
                })
                kept.put("gutters", JSONArray().apply {
                    analysed.gutters.forEach {
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
                            .put("at", it.at ?: JSONObject.NULL)
                            .put("after", it.after)
                            .put("afterSound", it.afterSound))
                    }
                })
                // What the network heard of the learner, in order. No mark reads it; a
                // take read back does, and without it a take cannot be told apart from a
                // take read back wrongly.
                kept.put("freely", JSONArray().apply {
                    analysed.freely.forEach {
                        put(JSONObject().put("symbol", it.symbol)
                            .put("at", JSONArray(listOf(it.at.first, it.at.last))))
                    }
                })
                kept.put("dropped", analysed.dropped)
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

    private fun shares(shares: List<app.speakup.analysis.Share>) = JSONArray().apply {
        shares.forEach { put(JSONObject().put("symbol", it.symbol).put("part", it.part)) }
    }
}
