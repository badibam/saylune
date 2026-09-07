package app.saylune.providers

import app.saylune.chain.ChainFailure
import app.saylune.debug.Trace
import app.saylune.keys.Secret
import app.saylune.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * The life of one Replicate prediction: create it, wait for it, fetch what it left.
 *
 * Shared by the recognition and the synthesis the way [Http] is shared by the three remote
 * links -- one key serves two tasks there, so the code that spends it is written once. It
 * sits **below** the seams of `chain/`: neither seam knows this type exists.
 *
 * Replicate does not answer with the result. It answers with a prediction that finishes
 * later and leaves its output at a URL, so a call here is two or three round trips. The
 * `Prefer: wait` header holds the first one open until the model is done, which collapses
 * the poll into nothing on a fast model and still works on a slow one.
 *
 * **A poor account is throttled**: under five dollars of credit Replicate allows six
 * predictions a minute, burst of one. A conversation turn spends one per link and lives
 * inside that; anything that fires several calls close together does not, and has to space
 * them itself.
 */
internal class ReplicateClient(private val store: SecretStore) {

    /**
     * Run [model] on [input] and give back the bytes it produced.
     *
     * [output] pulls the URL out of the prediction, because where it sits is the model's
     * business and not this type's: a synthesis leaves one string, a transcription leaves a
     * whole object. A model whose answer is the JSON itself is served by [predict].
     */
    suspend fun fetch(model: String, input: JSONObject, output: (Any?) -> String?): ByteArray {
        val url = output(predict(model, input).opt("output"))
            ?: throw ChainFailure("$model left no file behind")
        return withContext(Dispatchers.IO) { Http.get(url) }
    }

    /** Run [model] on [input] and give back the finished prediction. */
    suspend fun predict(model: String, input: JSONObject): JSONObject {
        val token = token()
        val body = JSONObject()
            .put("version", version(model, token))
            .put("input", input)
            .toString()
        var prediction = withContext(Dispatchers.IO) {
            JSONObject(
                Http.post(
                    url = "${base()}/predictions",
                    headers = mapOf(
                        "Authorization" to "Token $token",
                        // Held as long as the API allows, so a model that finishes quickly
                        // costs one round trip instead of three.
                        "Prefer" to "wait=60",
                    ),
                    contentType = "application/json",
                    body = body.toByteArray(),
                ).decodeToString()
            )
        }
        var waited = 0L
        while (prediction.optString("status") !in FINISHED) {
            if (waited > WAIT_CEILING_MS) {
                throw ChainFailure("$model was still running after ${WAIT_CEILING_MS / 1000} s")
            }
            delay(POLL_MS)
            waited += POLL_MS
            val id = prediction.optString("id")
            prediction = withContext(Dispatchers.IO) {
                JSONObject(
                    Http.get("${base()}/predictions/$id", mapOf("Authorization" to "Token $token"))
                        .decodeToString()
                )
            }
        }
        val status = prediction.optString("status")
        if (status != "succeeded") {
            val why = prediction.opt("error")?.toString().orEmpty().take(300)
            Trace.fail("replicate: $model did not finish", "status" to status, "why" to why)
            throw ChainFailure("$model $status: $why")
        }
        return prediction
    }

    /**
     * What [model] accepts, as its published input schema.
     *
     * This is where the settings screen gets its lists -- a synthesis model's voices are an
     * enumeration in its own schema, so nothing has to be maintained beside the model. It is
     * also the probe the doc asks for: a key that brings a schema back is a key that works,
     * and what comes back is what that key really unlocks.
     */
    suspend fun schema(model: String): JSONObject =
        describe(model, token()).optJSONObject("latest_version")
            ?.optJSONObject("openapi_schema")
            ?.optJSONObject("components")
            ?.optJSONObject("schemas")
            ?: throw ChainFailure("$model publishes no input schema")

    /**
     * The version hash of [model], resolved rather than assumed.
     *
     * Addressing a model by `owner/name` works only for the handful Replicate runs itself: a
     * community model answers 404 there, and `victor-upmeet/whisperx` -- the one that gives
     * word spans -- is one. Resolving the version first is the single form that works for
     * both, so there is one code path and nothing is guessed from the name.
     *
     * Held for the life of the process and not stored: it saves the round trip inside a
     * session without freezing a hash the user never chose. **So the version is not pinned**,
     * and a model republished under the app renders a different voice from the one the
     * yardstick was calibrated on (`../../../../../../TODO.md`).
     */
    private suspend fun version(model: String, token: String): String =
        versions[model] ?: (describe(model, token).optJSONObject("latest_version")?.optString("id")
            ?.takeIf { it.isNotBlank() }
            ?: throw ChainFailure("$model has no published version"))
            .also { versions[model] = it }

    private suspend fun describe(model: String, token: String): JSONObject =
        withContext(Dispatchers.IO) {
            JSONObject(
                Http.get("${base()}/models/$model", mapOf("Authorization" to "Token $token"))
                    .decodeToString()
            )
        }

    private suspend fun token(): String = store.values().first()[Secret.ReplicateApiKey]
        ?: throw ChainFailure("no Replicate key has been entered")

    /** The default host, or whatever the user put in its place. */
    private suspend fun base(): String =
        (store.values().first()[Secret.ReplicateEndpoint] ?: DEFAULT_BASE).trimEnd('/')

    companion object {
        const val DEFAULT_BASE = "https://api.replicate.com/v1"

        private val versions = ConcurrentHashMap<String, String>()
        private val FINISHED = setOf("succeeded", "failed", "canceled")
        private const val POLL_MS = 500L
        private const val WAIT_CEILING_MS = 180_000L

        /**
         * The values a model's schema allows for [field], in the order it publishes them.
         *
         * Replicate writes an enumerated input as a reference to a schema of the field's own
         * name, so the enumeration is looked up beside the property as well as inside it. An
         * empty list is a real answer and not a failure: a model whose voice is free-form has
         * no list to offer, and the screen shows a field instead of a menu.
         */
        fun choicesFor(schemas: JSONObject, field: String): List<String> {
            val enumerated = schemas.optJSONObject(field)?.optJSONArray("enum")
                ?: schemas.optJSONObject("Input")?.optJSONObject("properties")
                    ?.optJSONObject(field)?.optJSONArray("enum")
                ?: return emptyList()
            return enumerated.strings()
        }

        private fun JSONArray.strings(): List<String> =
            (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }
    }
}
