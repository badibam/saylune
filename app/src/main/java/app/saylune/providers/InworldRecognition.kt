package app.saylune.providers

import android.util.Base64
import app.saylune.chain.ChainFailure
import app.saylune.chain.Recognition
import app.saylune.chain.Word
import app.saylune.debug.Trace
import app.saylune.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Inworld's synchronous transcription: the whole turn goes up, the whole transcript comes
 * back.
 *
 * The streaming socket beside it is deliberately not used. It exists to decide where a turn
 * ends, and that decision is the app's -- nothing cuts the speech of somebody still speaking
 * (`docs/reference.md`), and a link that ends turns for us would take the invariant away
 * from where it is enforced.
 *
 * **It gives every word its bounds**, which whisperx and Scribe do and Azure does not. They
 * enrich and never condition: the app partitions sounds between words by order and spelling
 * whatever a provider offers.
 *
 * **Punctuation is thrown away here**, as with every other recognition. Punctuation belongs
 * to the language model, which puts it there by the intention it is answering, and the
 * synthesised model's contour depends on that choice -- letting two links both decide it
 * would make the contour depend on which recognition was chosen.
 */
class InworldRecognition(
    private val store: SecretStore,
    private val model: String,
) : Recognition {

    override suspend fun transcribe(audio: File): List<Word> = withContext(Dispatchers.IO) {
        val values = store.values().first()

        Trace.add(
            "recognition: sending the turn to inworld/$model",
            "file" to audio.name,
            "bytes" to audio.length().toString(),
        )

        val body = JSONObject()
            .put(
                "transcribeConfig", JSONObject()
                    .put("modelId", model)
                    // The app hands up a wav it wrote itself, so the format is known -- but
                    // asking them to read the header costs nothing and survives the day the
                    // capture writes something else.
                    .put("audioEncoding", "AUTO_DETECT")
                    .put("language", "en-US"),
            )
            .put(
                "audioData",
                JSONObject().put(
                    "content",
                    Base64.encodeToString(audio.readBytes(), Base64.NO_WRAP),
                ),
            )
            .toString()
            .toByteArray()

        val answer = Http.post(
            url = "${InworldApi.base(values)}/stt/v1/transcribe",
            headers = InworldApi.basic(InworldApi.key(values)),
            contentType = "application/json",
            body = body,
        )

        val json = JSONObject(answer.decodeToString())
        val transcription = json.optJSONObject("transcription")
        val whole = transcription?.optString("transcript").orEmpty()
        val listed = transcription?.optJSONArray("wordTimestamps")
        Trace.add(
            "recognition: read the mouth",
            "text" to whole,
            "spans" to (listed?.length() ?: 0).toString(),
        )
        // No transcription object at all is a broken link; an empty transcript is a turn
        // with nothing said, and the caller has to be able to tell them apart.
        if (transcription == null) throw ChainFailure("Inworld returned no transcript")

        if (listed == null || listed.length() == 0) {
            return@withContext whole.split(' ').mapNotNull { bare(it)?.let(::Word) }
        }
        (0 until listed.length()).mapNotNull { at ->
            val entry = listed.optJSONObject(at) ?: return@mapNotNull null
            val text = bare(entry.optString("word")) ?: return@mapNotNull null
            Word(
                text = text,
                startMs = millis(entry, "startTimeMs"),
                endMs = millis(entry, "endTimeMs"),
            )
        }
    }

    private fun millis(entry: JSONObject, name: String): Int? =
        if (entry.has(name) && !entry.isNull(name)) entry.optDouble(name).toInt() else null

    /** The word without the punctuation the language model is the one to decide. */
    private fun bare(text: String): String? =
        text.filter { it.isLetterOrDigit() || it == '\'' }.trim('\'').ifBlank { null }
}
