package app.saylune.providers

import android.content.Context
import app.saylune.chain.Voice
import app.saylune.chain.Word
import app.saylune.chain.Scene
import app.saylune.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * How long each link takes, model by model, on the same two sentences every time.
 *
 * The turns kept during real use time the chain as it was actually used, which is what the
 * learner feels -- and which mixes provider, sentence length, network weather and one
 * sample each. Nothing in it separates a model that is slow from an afternoon that was.
 * This does the other half: one sentence short, one long, every model whose key is filled,
 * repeated, on demand.
 *
 * **It times a link and never a turn.** The number the doc cares about -- time to first
 * sound -- stays what a kept turn measures. What comes out of here compares candidates.
 *
 * Three things that would make it lie, and what is done about each:
 *
 * - **The render cache** indexes by text, voice and model, so a second run would return in
 *   no time at all and the test would congratulate itself. The cached file is deleted
 *   before the call is timed.
 * - **The recognition needs audio**, and comparing models means they must hear the *same*
 *   audio. It is synthesised once, kept beside the results, and reused by every later run
 *   -- otherwise two runs are not comparable either.
 * - **One call measures nothing** where the defect is variance: the recognition was seen
 *   spending 0.9 s and 10.9 s on turns of the same length. Hence repeats.
 *
 * Results are appended, never replaced. A provider that gets slower is only visible against
 * what it used to do.
 */
internal object LatencyTest {

    /** One timed call: what was asked of whom, and what it cost. */
    data class Trial(
        val at: String,
        val note: String,
        val link: String,
        val route: String,
        val model: String,
        /** The reasoning level asked for, or null on a link that has no notion of one. */
        val effort: Effort?,
        val size: String,
        val chars: Int,
        val ms: Long?,
        val why: String?,
    ) {
        val label: String get() = "$route/$model" + (effort?.let { "@${it.id}" } ?: "")
    }

    /** The two sentences, fixed forever: a number that moves is not a measurement. */
    const val SHORT = "This is a short one."
    const val LONG = "I would still like to speak with an Australian accent, " +
        "because the way people talk there has always fascinated me."

    /** What a run would cost, so the button can say it before spending anything. */
    suspend fun planned(store: SecretStore, repeats: Int): Int {
        val values = store.values().first()
        return Task.entries.sumOf { task ->
            task.offered(values).sumOf { provider ->
                models(provider, task).size * efforts(provider, task).size
            }
        } * 2 * repeats
    }

    private fun models(provider: Provider, task: Task): List<String> =
        provider.models[task].orEmpty().ifEmpty { listOf("") }

    /**
     * The reasoning levels to sweep, or a single null where there is no such notion.
     *
     * It belongs here rather than in the settings because it is a candidate like a model is:
     * what the turns of 2026-09-06 showed is that the level moves this link by more than the
     * choice of model does, and comparing two models at whatever level each happened to be on
     * compares two things at once.
     */
    private fun efforts(provider: Provider, task: Task): List<Effort?> =
        if (task.effort != null && provider.efforts.isNotEmpty()) provider.efforts
        else listOf(null)

    /**
     * Run every offered model twice per repeat, and write what happened.
     *
     * [note] names the run. Two sweeps of the same model are not two samples of the same
     * thing when one was on a home wifi and the other on a phone network at a station: the
     * numbers differ by the place, and nothing in the file would say so.
     *
     * [say] reports progress; a sweep is minutes long and a frozen screen would read as a
     * crash. Nothing here throws: a link that fails is a result -- it says which model
     * refused and why, which is exactly what one wants to know before choosing it.
     */
    suspend fun run(
        context: Context,
        store: SecretStore,
        repeats: Int,
        note: String,
        say: (String) -> Unit,
    ): List<Trial> {
        val values = store.values().first()
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date())
        val out = mutableListOf<Trial>()

        val heard = audio(context, store, say)

        for (round in 1..repeats) {
            for (task in Task.entries) {
                for (provider in task.offered(values)) {
                    for (model in models(provider, task)) {
                        val voice = if (task == Task.Synthesis) {
                            firstVoice(store, provider, model)
                        } else null
                        for (effort in efforts(provider, task)) {
                        for ((size, text) in listOf("court" to SHORT, "long" to LONG)) {
                            val at = effort?.let { " @${it.id}" }.orEmpty()
                            say("$round/$repeats · ${provider.id}/$model$at · $size")
                            val trial = timed(
                                context, store, stamp, note, task, provider, model, effort,
                                voice, size, text, heard,
                            )
                            out += trial
                            // Written as it happens, not at the end. A sweep is minutes
                            // long and lives in the screen's scope: leaving the settings
                            // cancels it, and a run that only writes when it finishes
                            // leaves nothing behind at all -- which is what happened the
                            // first time this was used.
                            keep(context, trial)
                        }
                        }
                    }
                }
            }
        }
        return out
    }

    private suspend fun timed(
        context: Context,
        store: SecretStore,
        stamp: String,
        note: String,
        task: Task,
        provider: Provider,
        model: String,
        effort: Effort?,
        voice: Voice?,
        size: String,
        text: String,
        heard: Samples,
    ): Trial {
        var why: String? = null
        val began = System.nanoTime()
        try {
            when (task) {
                // Not a remote call to time beside the other three: the acoustic pass is
                // measured on its own, on the machine that runs it (`bench/cost.py`), and
                // what a round trip adds is read off real turns rather than a sample.
                Task.Analysis -> error("the analysis link is not timed here")
                Task.Recognition -> {
                    val file = heard.files[size]
                        ?: error("no $size sample: " + (heard.why ?: "not rendered"))
                    recognitionBy(store, provider, model).transcribe(file)
                }
                Task.Conversation -> conversationBy(store, provider, model, effort).reply(
                    emptyList(),
                    text.trim('.').split(' ').map { Word(it.lowercase()) },
                    // Unnamed, as a first turn is. The bench times one call and holds
                    // nothing between them, so there is no conversation here to name.
                    scene = Scene(),
                )
                // The same sentence, marked instead of answered. The reply it is shown is one
                // short line and the situation is empty: a longer context would be timing a
                // different call, and what is wanted here is the two links side by side.
                Task.Judging -> conversationBy(store, provider, model, effort).judge(
                    emptyList(),
                    said = text.trim('.'),
                    answered = "Right, I see.",
                    situation = "",
                )
                Task.Synthesis -> {
                    val speaking = voice ?: error("no voice was listed for $model")
                    // Or the cache would hand back the last run's file and call it speed.
                    Renders.file(context, text, speaking, model).delete()
                    synthesisBy(context, store, provider, model).speak(text, speaking)
                }
            }
        } catch (failure: Throwable) {
            why = failure.message?.take(160) ?: failure.javaClass.simpleName
        }
        val ms = (System.nanoTime() - began) / 1_000_000
        return Trial(
            at = stamp,
            note = note,
            link = task.name.lowercase(),
            route = provider.id,
            model = model.ifBlank { "—" },
            effort = effort,
            size = size,
            chars = text.length,
            ms = if (why == null) ms else null,
            why = why,
        )
    }

    /** The first voice the model lists, which is all a stopwatch needs of one. */
    private suspend fun firstVoice(
        store: SecretStore,
        provider: Provider,
        model: String,
    ): Voice? = runCatching {
        provider.voices(store, model).firstOrNull()
            ?.let { Voice(provider = provider.id, id = it.id) }
    }.getOrNull()

    /** The samples every recognition hears, and why they are missing when they are. */
    private class Samples(val files: Map<String, File>, val why: String?)

    /**
     * The two sentences said aloud, once, and kept.
     *
     * Every recognition model must hear the same thing or the comparison is between
     * recordings. Kept beside the results rather than regenerated, so runs weeks apart stay
     * comparable -- and rendered by whichever synthesis is set, because which voice it is
     * does not matter as long as it never changes.
     */
    private suspend fun audio(
        context: Context,
        store: SecretStore,
        say: (String) -> Unit,
    ): Samples {
        val home = File(context.getExternalFilesDir(null), "latency").apply { mkdirs() }
        val out = mutableMapOf<String, File>()
        for ((size, text) in listOf("court" to SHORT, "long" to LONG)) {
            val kept = File(home, "heard-$size.wav")
            if (!kept.isFile || kept.length() == 0L) {
                say("rendu de l'échantillon $size")
                runCatching {
                    val synthesis = ChosenSynthesis(context, store)
                    synthesis.speak(text, synthesis.voice()).copyTo(kept, overwrite = true)
                }.onFailure {
                    // Carried out rather than swallowed. Without it a whole column reads
                    // "no sample was rendered" and the sweep does not say what refused --
                    // which is what a run of 2026-09-01 left behind for the recognition.
                    return Samples(out, it.message?.take(160) ?: it.javaClass.simpleName)
                }
            }
            out[size] = kept
        }
        return Samples(out, null)
    }

    /**
     * The name the last run was given, to be offered again.
     *
     * Typed afresh every time, the same place becomes "maison", "Maison" and "chez moi",
     * and three names for one place group into three. Offering the last one back makes
     * repeating a measure the cheap gesture and renaming it the deliberate one.
     */
    suspend fun lastNote(context: Context): String = withContext(Dispatchers.IO) {
        val file = File(File(context.getExternalFilesDir(null), "latency"), "latency.jsonl")
        if (!file.isFile) return@withContext ""
        runCatching {
            file.readLines().lastOrNull { it.isNotBlank() }
                ?.let { JSONObject(it).optString("note") }
                .orEmpty()
        }.getOrDefault("")
    }

    /** Appended as one JSON object per line: history by construction, nothing to migrate. */
    private suspend fun keep(context: Context, trial: Trial) = withContext(Dispatchers.IO) {
        val home = File(context.getExternalFilesDir(null), "latency").apply { mkdirs() }
        File(home, "latency.jsonl").appendText(
            JSONObject()
                .put("at", trial.at)
                .put("note", trial.note)
                .put("link", trial.link)
                .put("route", trial.route)
                .put("model", trial.model)
                .put("effort", trial.effort?.id ?: JSONObject.NULL)
                .put("size", trial.size)
                .put("chars", trial.chars)
                .put("ms", trial.ms ?: JSONObject.NULL)
                .put("why", trial.why ?: JSONObject.NULL)
                .toString() + "\n"
        )
    }
}
