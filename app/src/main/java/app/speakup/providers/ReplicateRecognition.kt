package app.speakup.providers

import android.util.Base64
import app.speakup.chain.ChainFailure
import app.speakup.chain.Recognition
import app.speakup.chain.Word
import app.speakup.debug.Trace
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Whisper on Replicate, behind the same key as the synthesis.
 *
 * Two models are offered and they differ in one thing that matters to the seam: whisperx,
 * asked to align its output, gives every word its bounds in the recording; plain whisper
 * gives the sentence and its segments only. [Word] allows a word with no bounds, so both are
 * legitimate here -- and the app never requires them: the analysis partitions sounds between
 * words by order and spelling on its own, measured at 95% on the twenty sentences held back
 * (`docs/analysis.md`). Bounds enrich, they do not condition. They could not, or the app
 * would break the day the recognition becomes local, which is where the doc sends it.
 *
 * **Punctuation is stripped, and the case with it.** Whisper writes prose -- capitals, full
 * stops, an apostrophe where it hears one -- and this link is not allowed to decide any of
 * it: the recognition transcribes the mouth, the language model decides the intention, and
 * it punctuates by the intention it is answering. Leaving whisper's punctuation in would
 * hand the language model a sentence already interpreted by something with no context. What
 * is *not* stripped is a word whisper normalised toward a plausible spelling: that is the
 * wanted behaviour, and "I sink" coming back as `think` was measured and was right.
 *
 * The audio goes up as a data URI in the request body. There is a file API beside it, but it
 * is one more round trip per turn for a recording of a few tens of kilobytes.
 */
class ReplicateRecognition(
    private val store: SecretStore,
    private val model: String,
) : Recognition {

    private val client = ReplicateClient(store)

    override suspend fun transcribe(audio: File): List<Word> {
        Trace.add(
            "recognition: sending the turn to $model",
            "route" to "replicate",
            "file" to audio.name,
            "bytes" to audio.length().toString(),
        )
        val uri = withContext(Dispatchers.IO) {
            "data:audio/wav;base64," + Base64.encodeToString(audio.readBytes(), Base64.NO_WRAP)
        }

        val input = JSONObject().put("language", "en")
        val prediction = if (alignable) {
            client.predict(model, input.put("audio_file", uri).put("align_output", true))
        } else {
            client.predict(model, input.put("audio", uri))
        }

        val output = prediction.opt("output") as? JSONObject
            ?: throw ChainFailure("$model returned no transcript")
        val words = if (alignable) aligned(output) else plain(output)
        Trace.add(
            "recognition: read the mouth",
            "words" to words.joinToString(" ") { it.text },
            "spans" to if (words.any { it.startMs != null }) "yes" else "no",
        )
        return words
    }

    /** whisperx: the words are already separate, and each carries where it was said. */
    private fun aligned(output: JSONObject): List<Word> {
        val segments = output.optJSONArray("segments") ?: JSONArray()
        return (0 until segments.length()).flatMap { at ->
            val said = segments.optJSONObject(at)?.optJSONArray("words") ?: JSONArray()
            (0 until said.length()).mapNotNull { index ->
                val word = said.optJSONObject(index) ?: return@mapNotNull null
                val text = bare(word.optString("word"))
                if (text.isEmpty()) return@mapNotNull null
                Word(
                    text = text,
                    // Absent on a word the aligner could not place, which happens and is not
                    // a failure: the word was heard, only its bounds are unknown.
                    startMs = word.optDouble("start").takeIf { !it.isNaN() }?.times(1000)?.toInt(),
                    endMs = word.optDouble("end").takeIf { !it.isNaN() }?.times(1000)?.toInt(),
                )
            }
        }
    }

    /** whisper: one sentence, cut on spaces, with no bounds to offer. */
    private fun plain(output: JSONObject): List<Word> =
        output.optString("transcription")
            .split(' ', '\n')
            .map { bare(it) }
            .filter { it.isNotEmpty() }
            .map { Word(it) }

    /**
     * A word as the mouth said it: lower case, and without the marks whisper added.
     *
     * The apostrophe stays. It is not punctuation here but part of the spelling -- `you're`
     * is one word and two sounds, and dropping the mark would make it `youre`, which the
     * analysis would then have to anchor letters against.
     */
    private fun bare(word: String): String =
        word.trim().lowercase().trim { it !in "'" && !it.isLetterOrDigit() }

    /** Only whisperx aligns, and only when asked; the rest give the sentence. */
    private val alignable: Boolean get() = model.endsWith("whisperx")
}
