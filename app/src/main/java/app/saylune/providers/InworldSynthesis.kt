package app.saylune.providers

import android.content.Context
import android.util.Base64
import app.saylune.capture.WavFile
import app.saylune.chain.ChainFailure
import app.saylune.chain.Synthesis
import app.saylune.chain.Voice
import app.saylune.debug.Trace
import app.saylune.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * Inworld's synthesis, asked for raw 16 kHz samples.
 *
 * **That last part is what makes it a candidate for more than talking.** Their audio config
 * takes a sample rate from a list that holds 16000 and an encoding that is uncompressed, so
 * a render arrives in the very format the acoustic model consumes on both sides. A provider
 * that could only serve mp3 would be usable for a character and never as the standard the
 * measure is taken against, the analysis reading a transcoded file as a different mouth.
 * Being *askable* for it is not being qualified as a standard: no voice anywhere is, and
 * the test that would say so is still to be written (`docs/reference.md`).
 *
 * The cache is [Renders], shared with the other synthesis implementations, and the model is
 * part of what identifies a render -- two models do not render one voice the same way.
 */
class InworldSynthesis(
    private val context: Context,
    private val store: SecretStore,
    private val model: String,
) : Synthesis {

    override suspend fun speak(text: String, voice: Voice): File = withContext(Dispatchers.IO) {
        val cached = Renders.file(context, text, voice, model = model)
        if (cached.isFile && cached.length() > 0) return@withContext cached

        val values = store.values().first()

        Trace.add(
            "synthesis: asking inworld/$model",
            "voice" to voice.id,
            "chars" to text.length.toString(),
        )
        val body = JSONObject()
            .put("text", text)
            .put("voiceId", voice.id)
            .put("modelId", model)
            .put(
                "audioConfig", JSONObject()
                    .put("audioEncoding", "LINEAR16")
                    .put("sampleRateHertz", WavFile.SAMPLE_RATE),
            )
            .toString()
            .toByteArray()

        val answer = Http.post(
            url = "${InworldApi.base(values)}/tts/v1/voice",
            headers = InworldApi.basic(InworldApi.key(values)),
            contentType = "application/json",
            body = body,
        )

        val encoded = JSONObject(answer.decodeToString()).optString("audioContent")
        if (encoded.isBlank()) throw ChainFailure("Inworld returned no audio")
        val bytes = Base64.decode(encoded, Base64.DEFAULT)

        // **Their two uncompressed encodings differ by a header and their doc does not say
        // which this one carries** -- `LINEAR16` is described as linear PCM, `PCM` as the
        // same "with no WAV header". So the bytes are asked rather than assumed: a file that
        // already begins with `RIFF` is written through, and one that does not gets the
        // header the app writes for every other raw render.
        if (bytes.size >= 4 && bytes.decodeToString(0, 4) == "RIFF") {
            cached.writeBytes(bytes)
        } else {
            val raw = File(cached.parentFile, cached.name + ".pcm")
            raw.writeBytes(bytes)
            WavFile.wrap(raw, cached)
            raw.delete()
        }
        cached
    }
}
