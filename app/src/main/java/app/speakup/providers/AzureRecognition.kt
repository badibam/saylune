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
import java.io.File
import java.net.URLEncoder

/**
 * Azure Speech, short-audio REST. Wired in because it was already configured, not chosen --
 * recognition's destination is the device (`docs/reference.md`).
 *
 * It reads the **lexical** transcript, not the display one. Display applies punctuation and
 * inverse text normalisation; both belong to the language model here, which has the context
 * and an instruction, where this link has neither. Taking the lexical form is this seam's
 * whole rule made concrete: transcribe the mouth, decide nothing.
 *
 * No word spans: the short-audio endpoint does not give them, and [Word] allows their
 * absence because the analysis partitions sounds between words on its own, at 95%.
 */
class AzureRecognition(private val store: SecretStore) : Recognition {

    override suspend fun transcribe(audio: File): List<Word> = withContext(Dispatchers.IO) {
        val secrets = store.values().first()
        val key = secrets[Secret.AzureSpeechKey]
            ?: throw ChainFailure("no Azure Speech key has been entered")
        val region = secrets[Secret.AzureSpeechRegion]
            ?: throw ChainFailure("no Azure Speech region has been entered")

        Trace.add(
            "recognition: sending the turn",
            "file" to audio.name,
            "bytes" to audio.length().toString(),
        )

        val url = "https://${URLEncoder.encode(region, "UTF-8")}.stt.speech.microsoft.com" +
            "/speech/recognition/conversation/cognitiveservices/v1" +
            "?language=en-US&format=detailed&profanity=raw"

        val answer = Http.post(
            url = url,
            headers = mapOf("Ocp-Apim-Subscription-Key" to key, "Accept" to "application/json"),
            contentType = "audio/wav; codecs=audio/pcm; samplerate=16000",
            body = audio.readBytes(),
        )

        val json = JSONObject(answer.decodeToString())
        when (val status = json.optString("RecognitionStatus")) {
            "Success" -> Unit
            // A turn with nothing in it is a legitimate outcome of holding the button by
            // accident, and the caller must be able to tell it from a link that broke.
            "NoMatch", "InitialSilenceTimeout" -> {
                Trace.add("recognition: heard nothing", "status" to status)
                return@withContext emptyList()
            }
            else -> {
                Trace.fail("recognition: refused", "status" to status)
                throw ChainFailure("Azure returned status $status")
            }
        }

        val best = json.optJSONArray("NBest")?.optJSONObject(0)
            ?: throw ChainFailure("Azure returned no transcript")
        val lexical = best.optString("Lexical")
        Trace.add(
            "recognition: read the mouth",
            "lexical" to lexical,
            "display (not used)" to best.optString("Display"),
        )
        lexical
            .split(' ')
            .filter { it.isNotBlank() }
            .map { Word(it) }
    }
}
