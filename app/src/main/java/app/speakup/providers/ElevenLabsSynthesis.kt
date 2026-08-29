package app.speakup.providers

import android.content.Context
import app.speakup.capture.WavFile
import app.speakup.chain.ChainFailure
import app.speakup.chain.Synthesis
import app.speakup.chain.Voice
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * ElevenLabs. Two of its voices were already read by the bench, which is why it is wired in;
 * no voice is qualified as a yardstick yet (`docs/reference.md`, "L'accent").
 *
 * It asks for `pcm_16000` rather than mp3, so the render arrives in the very format the
 * acoustic model consumes -- one less transform, and above all the same format on both
 * sides, which is the entry contract the measure rests on. Only the wav header has to be
 * put back on.
 *
 * The cache lives here because one render serves three times: a yardstick for the measure, a
 * model to hear, and a model to hear again at every retry. It has no size ceiling and no
 * eviction yet, deliberately (`../../../../../../TODO.md`, chantier 0).
 */
class ElevenLabsSynthesis(
    private val context: Context,
    private val store: SecretStore,
) : Synthesis {

    override suspend fun speak(text: String, voice: Voice): File = withContext(Dispatchers.IO) {
        val cached = cacheFile(text, voice)
        if (cached.isFile && cached.length() > 0) return@withContext cached

        val key = store.values().first()[Secret.ElevenlabsKey]
            ?: throw ChainFailure("no ElevenLabs key has been entered")

        val body = JSONObject()
            .put("text", text)
            .put("model_id", MODEL)
            .toString()

        val pcm = Http.post(
            url = "https://api.elevenlabs.io/v1/text-to-speech/${voice.id}" +
                "?output_format=pcm_${WavFile.SAMPLE_RATE}",
            headers = mapOf("xi-api-key" to key, "Accept" to "audio/pcm"),
            contentType = "application/json",
            body = body.toByteArray(),
        )

        val raw = File(cached.parentFile, cached.name + ".pcm")
        raw.writeBytes(pcm)
        WavFile.wrap(raw, cached)
        raw.delete()
        cached
    }

    /** Keyed by what was said and by whom, exactly as `docs/reference.md` asks. */
    private fun cacheFile(text: String, voice: Voice): File {
        val dir = File(context.cacheDir, "renders").apply { mkdirs() }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("${voice.provider}/${voice.id}/$text".toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
        return File(dir, "$digest.wav")
    }

    private companion object {
        /** Provisional: which model reads best as a yardstick is what the voice test decides. */
        const val MODEL = "eleven_multilingual_v2"
    }
}
