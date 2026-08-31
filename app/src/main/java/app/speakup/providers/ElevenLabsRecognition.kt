package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.chain.Recognition
import app.speakup.chain.Word
import app.speakup.debug.Trace
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * ElevenLabs Scribe. It gives every word its bounds, which whisperx does and Azure does not.
 *
 * Those bounds enrich and never condition: the app partitions sounds between words by order
 * and spelling whatever a provider offers, and a brick that required them would go dark the
 * day recognition becomes local, which is where this link is headed.
 *
 * **Punctuation is thrown away here.** Scribe punctuates, and punctuation belongs to the
 * language model, which puts it there by the intention it is answering -- the synthesised
 * model's contour depends on that choice, so letting two links both decide it would make the
 * contour depend on which recognition was chosen. What is kept is the word as it was said.
 */
class ElevenLabsRecognition(
    private val store: SecretStore,
    private val model: String,
) : Recognition {

    override suspend fun transcribe(audio: File): List<Word> = withContext(Dispatchers.IO) {
        val key = store.values().first()[Secret.ElevenLabsApiKey]
            ?: throw ChainFailure("no ElevenLabs key has been entered")

        Trace.add(
            "recognition: sending the turn to elevenlabs/$model",
            "file" to audio.name,
            "bytes" to audio.length().toString(),
        )

        val boundary = "----speakup${System.nanoTime()}"
        val answer = Http.post(
            url = "${ElevenLabsSynthesis.BASE}/speech-to-text",
            headers = mapOf("xi-api-key" to key, "Accept" to "application/json"),
            contentType = "multipart/form-data; boundary=$boundary",
            body = multipart(boundary, model, audio),
        )

        val json = JSONObject(answer.decodeToString())
        val listed = json.optJSONArray("words")
        val whole = json.optString("text")
        Trace.add(
            "recognition: read the mouth",
            "text" to whole,
            "spans" to (listed?.length() ?: 0).toString(),
        )
        // No transcript at all is a broken link; an empty one is a turn with nothing said,
        // and the caller has to be able to tell them apart.
        if (listed == null && whole.isBlank() && !json.has("text")) {
            throw ChainFailure("ElevenLabs returned no transcript")
        }

        if (listed == null) {
            return@withContext whole.split(' ').mapNotNull { bare(it)?.let(::Word) }
        }
        (0 until listed.length()).mapNotNull { at ->
            val entry = listed.optJSONObject(at) ?: return@mapNotNull null
            // `spacing` and audio events are not words and must not become any.
            if (entry.optString("type", "word") != "word") return@mapNotNull null
            val text = bare(entry.optString("text")) ?: return@mapNotNull null
            Word(
                text = text,
                startMs = seconds(entry, "start"),
                endMs = seconds(entry, "end"),
            )
        }
    }

    private fun seconds(entry: JSONObject, name: String): Int? =
        if (entry.has(name) && !entry.isNull(name)) (entry.optDouble(name) * 1000).toInt()
        else null

    /** The word without the punctuation the language model is the one to decide. */
    private fun bare(text: String): String? =
        text.filter { it.isLetterOrDigit() || it == '\'' }.trim('\'').ifBlank { null }

    private fun multipart(boundary: String, model: String, audio: File): ByteArray {
        val out = ByteArrayOutputStream()
        fun field(name: String, value: String) {
            out.write(
                ("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n" +
                    "$value\r\n").toByteArray()
            )
        }
        field("model_id", model)
        out.write(
            ("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; " +
                "filename=\"${audio.name}\"\r\nContent-Type: audio/wav\r\n\r\n").toByteArray()
        )
        out.write(audio.readBytes())
        out.write("\r\n--$boundary--\r\n".toByteArray())
        return out.toByteArray()
    }
}
