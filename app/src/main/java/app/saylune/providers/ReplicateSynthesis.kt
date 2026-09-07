package app.saylune.providers

import android.content.Context
import app.saylune.capture.Rendered
import app.saylune.chain.ChainFailure
import app.saylune.chain.Synthesis
import app.saylune.chain.Voice
import app.saylune.debug.Trace
import app.saylune.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Chatterbox on Replicate. The same key as the recognition, which is the whole point of
 * bringing Replicate in: one account instead of two, so the BYOK wall the doc names gets
 * lower by one provider.
 *
 * Two things it does that Azure does not have to.
 *
 * **The seed is pinned.** Chatterbox samples, so the same sentence comes back a little
 * different -- and different in *duration* -- at every call. That is fine for a voice to
 * listen to and fatal for a yardstick: the model is what the take is measured against, so a
 * model that moves makes the measured gap move with it. Pinning the seed is what makes the
 * cache honest too, since a cached render must be the one a fresh call would have produced.
 *
 * **The render is converted.** Chatterbox writes floating-point samples at its own rate, and
 * the acoustic model consumes 16 kHz mono 16-bit on both sides. [Rendered] does that, and
 * does it band-limited, because a careless conversion would bias one of the two recordings
 * and not the other.
 *
 * Two things chatterbox does that the app cannot switch off, written down because both touch
 * rules the project holds elsewhere (`docs/providers.md`).
 *
 * The turbo normalises its loudness to -27 LUFS, and Replicate exposes no input to stop it.
 * That is a treatment on the model's side and on no other, which the doc forbids as a rule.
 * Whether it moves the measure is **not measured**: the comparison reads distributions of
 * acoustic similarity rather than levels, so it may well cost nothing -- and "may well" is
 * not a result. Nothing here is concluded from it.
 *
 * And it normalises the punctuation of the text before saying it -- ellipses, dashes, curly
 * quotes, a full stop added when none was there. Letters are what the marks are anchored to,
 * and none of that touches a letter; but the app hands the model a sentence and gets back a
 * render of a slightly different one, which is worth knowing when a contour looks wrong.
 */
class ReplicateSynthesis(
    private val context: Context,
    private val store: SecretStore,
    private val model: String,
) : Synthesis {

    private val client = ReplicateClient(store)

    override suspend fun speak(text: String, voice: Voice): File {
        val cached = Renders.file(context, text, voice, model)
        if (cached.isFile && cached.length() > 0) return cached

        Trace.add(
            "synthesis: asking replicate/$model",
            "voice" to voice.id,
            "chars" to text.length.toString(),
        )
        val input = JSONObject().put(textFieldOf(model), text)
        // A seed only where there is one to set. The chatterbox models sample and must be
        // pinned or every render of the same sentence is a different yardstick; the
        // ElevenLabs models expose no such input, and sending one they do not declare is
        // asking to be refused.
        if (!model.startsWith("elevenlabs/")) input.put("seed", SEED)
        if (voice.id.isNotBlank()) input.put("voice", voice.id)

        val audio = client.fetch(model, input) { output ->
            // A model leaves either the url itself or a list holding it; both are ordinary,
            // and which one is the model's business rather than a shape to be surprised by.
            when (output) {
                is String -> output
                is JSONArray -> output.optString(0).takeIf { it.isNotBlank() }
                else -> null
            }
        }

        return withContext(Dispatchers.IO) {
            val raw = File(cached.parentFile, cached.name + ".rendered")
            raw.writeBytes(audio)
            try {
                Rendered.at16kMono(raw, cached)
            } catch (e: IllegalArgumentException) {
                // Loud, and with the render thrown away: a half-written model file would be
                // measured against, and every sound of the take would come back wrong.
                Trace.fail("synthesis: the render could not be read", "why" to e.message)
                cached.delete()
                throw ChainFailure("$model rendered audio this cannot read: ${e.message}", e)
            } finally {
                raw.delete()
            }
            cached
        }
    }

    /**
     * Models disagree on what the text field is called -- `text` for the chatterbox turbo,
     * `prompt` for the other one and for all the ElevenLabs models -- and nothing about a
     * schema makes one of them wrong.
     * Read off the model name rather than fetched: it is one word of difference, and a fetch
     * to learn it would cost a round trip before every synthesis.
     */
    private fun textFieldOf(model: String): String =
        if (model.endsWith("chatterbox") || model.startsWith("elevenlabs/")) "prompt" else "text"

    private companion object {
        /**
         * Any fixed number would do; what matters is that it never changes, since changing
         * it changes every voice the app has ever measured against.
         */
        const val SEED = 20260830
    }
}
