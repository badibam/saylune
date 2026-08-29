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
    ) {
        if (!Trace.on) return
        runCatching {
            val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date())
            val into = File(context.getExternalFilesDir(null), "$DIR/$stamp").apply { mkdirs() }

            said.copyTo(File(into, "said.wav"), overwrite = true)
            model?.copyTo(File(into, "model.wav"), overwrite = true)

            val turn = JSONObject()
                .put("at", stamp)
                .put("text", intended)
                .put("heard", heard.joinToString(" ") { it.text })
                .put("faulty", faulty)
                .put("analysed", analysed != null)
            if (analysed != null) {
                // `phonemes` keeps the shape `bench/turn.py` writes, so the bench reads a
                // turn from the phone the way it reads one of its own.
                turn.put("phonemes", JSONArray().apply {
                    analysed.marking.phonemes.forEach {
                        put(JSONObject().put("start", it.start).put("end", it.end)
                            .put("points", it.points))
                    }
                })
                turn.put("gutters", JSONArray().apply {
                    analysed.gutters.forEach {
                        put(JSONObject().put("symbol", it.symbol).put("after", it.after)
                            .put("points", it.points))
                    }
                })
                turn.put("dropped", analysed.dropped)
                turn.put("sounds", JSONArray().apply {
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
            part.writeText(turn.toString(2) + "\n", Charsets.UTF_8)
            part.renameTo(File(into, "turn.json"))

            Trace.add("take kept", "where" to into.path)
        }.onFailure {
            // Keeping a take is bench material, never the turn. It failing must not cost the
            // learner the conversation -- but it says so rather than passing for a turn that
            // was kept.
            Trace.fail("take not kept", "why" to it.message)
        }
    }

    private fun shares(shares: List<app.speakup.analysis.Share>) = JSONArray().apply {
        shares.forEach { put(JSONObject().put("symbol", it.symbol).put("part", it.part)) }
    }
}
