package app.speakup.providers

import android.content.Context
import app.speakup.capture.WavFile
import app.speakup.chain.ChainFailure
import app.speakup.chain.Synthesis
import app.speakup.chain.Voice
import app.speakup.debug.Trace
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * ElevenLabs, billed by the character -- which is the reason it is worth having here.
 *
 * A turn spends its characters twice and no more: the answer to say, and `intended` to
 * measure against. Saying a sentence again costs nothing at all, since a redo reuses the
 * model already rendered for the turn. What does spend is a parenthesis, where every new
 * sentence is a new text.
 *
 * Asked for `pcm_16000`, so the render arrives in the format the acoustic model consumes on
 * both sides and only the wav header has to be put back on -- the same entry contract Azure
 * is asked for. Not every plan serves raw PCM; a plan that refuses it fails here rather than
 * quietly handing back an mp3 the analysis would have to transcode, which is a difference
 * the measure would carry without saying so.
 *
 * The cache is [Renders], shared with the other synthesis implementations, and the model is
 * part of what identifies a render: `eleven_flash_v2_5` and `eleven_multilingual_v2` do not
 * render the same voice, exactly as chatterbox and chatterbox-turbo do not.
 */
class ElevenLabsSynthesis(
    private val context: Context,
    private val store: SecretStore,
    private val model: String,
) : Synthesis {

    override suspend fun speak(text: String, voice: Voice): File = withContext(Dispatchers.IO) {
        val cached = Renders.file(context, text, voice, model = model)
        if (cached.isFile && cached.length() > 0) return@withContext cached

        val key = store.values().first()[Secret.ElevenLabsApiKey]
            ?: throw ChainFailure("no ElevenLabs key has been entered")

        Trace.add(
            "synthesis: asking elevenlabs/$model",
            "voice" to voice.id,
            "chars" to text.length.toString(),
        )
        val body = JSONObject()
            .put("text", text)
            .put("model_id", model)
            .toString()
            .toByteArray()

        val pcm = Http.post(
            url = "$BASE/text-to-speech/${voice.id}" +
                "?output_format=pcm_${WavFile.SAMPLE_RATE}",
            headers = mapOf("xi-api-key" to key, "Accept" to "audio/pcm"),
            contentType = "application/json",
            body = body,
        )

        val raw = File(cached.parentFile, cached.name + ".pcm")
        raw.writeBytes(pcm)
        WavFile.wrap(raw, cached)
        raw.delete()
        cached
    }

    internal companion object {
        const val BASE = "https://api.elevenlabs.io/v1"
    }
}
