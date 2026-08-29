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
import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest

/**
 * Azure Speech, the long-form REST endpoint. Not chosen either -- but it is already the
 * recognition's provider, so it costs no second key, and its voices are the ones the bench
 * has already rendered: `en-US-JennyNeural` is the `azure-us-jenny` the labelled set is read
 * against. The model the app makes heard and the model the bench measures can be the same
 * voice, which no other provider offered.
 *
 * It asks for `raw-16khz-16bit-mono-pcm`, so the render arrives in the very format the
 * acoustic model consumes on both sides -- the entry contract the measure rests on. Only the
 * wav header has to be put back on.
 *
 * The cache lives here because one render serves three times: a yardstick for the measure, a
 * model to hear, and a model to hear again at every retry. It has no size ceiling and no
 * eviction yet, deliberately (`../../../../../../TODO.md`, chantier 0).
 */
class AzureSynthesis(
    private val context: Context,
    private val store: SecretStore,
) : Synthesis {

    override suspend fun speak(text: String, voice: Voice): File = withContext(Dispatchers.IO) {
        val cached = cacheFile(text, voice)
        if (cached.isFile && cached.length() > 0) return@withContext cached

        val secrets = store.values().first()
        val key = secrets[Secret.AzureSpeechKey]
            ?: throw ChainFailure("no Azure Speech key has been entered")
        val region = secrets[Secret.AzureSpeechRegion]
            ?: throw ChainFailure("no Azure Speech region has been entered")

        val pcm = Http.post(
            url = "https://${URLEncoder.encode(region, "UTF-8")}.tts.speech.microsoft.com" +
                "/cognitiveservices/v1",
            headers = mapOf(
                "Ocp-Apim-Subscription-Key" to key,
                "X-Microsoft-OutputFormat" to "raw-${WavFile.SAMPLE_RATE / 1000}khz-16bit-mono-pcm",
            ),
            contentType = "application/ssml+xml",
            body = ssml(text, voice).toByteArray(),
        )

        val raw = File(cached.parentFile, cached.name + ".pcm")
        raw.writeBytes(pcm)
        WavFile.wrap(raw, cached)
        raw.delete()
        cached
    }

    /**
     * The request is SSML, so the text is markup and has to be escaped -- an apostrophe in
     * a learner's sentence is common and an ampersand would otherwise break the document.
     *
     * The language is read off the voice rather than asked for: `en-US-JennyNeural` says
     * `en-US`, and a second setting that could disagree with the first is a setting that
     * eventually will.
     */
    private fun ssml(text: String, voice: Voice): String {
        val language = voice.id.split('-').take(2).joinToString("-")
        return "<speak version='1.0' xml:lang='$language'>" +
            "<voice name='${voice.id}'>${escaped(text)}</voice></speak>"
    }

    private fun escaped(text: String) = text
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;")

    /** Keyed by what was said and by whom, exactly as `docs/reference.md` asks. */
    private fun cacheFile(text: String, voice: Voice): File {
        val dir = File(context.cacheDir, "renders").apply { mkdirs() }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("${voice.provider}/${voice.id}/$text".toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
        return File(dir, "$digest.wav")
    }
}
