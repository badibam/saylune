package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.debug.Trace
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The little HTTP the three remote links need, on the platform's own client.
 *
 * No library: a handful of requests do not earn a dependency, and every dependency is one
 * more thing to rebuild offline for a reproducible build.
 *
 * Nothing here ever puts a header into a message, nor into the trace. A failure carries the
 * status and the provider's own words, never what was sent -- the keys live behind the
 * Keystore precisely so they never end up somewhere readable, and both an exception message
 * and a debug panel are readable.
 *
 * **A 429 is answered by coming back when the server said to.** That is not a workaround for
 * a throttle, it is the protocol: the server names the delay, in a header or in its own body,
 * and the only correct client is one that waits it out. Measured on the device, a turn spends
 * two syntheses -- the answer to say, then the model of `intended` to measure against -- and
 * on a Replicate account under five dollars of credit, whose burst is one, the second was
 * refused five seconds after the first. The turn was spoken and carried **no marks at all**.
 *
 * The waiting is bounded, and it is traced: a request that takes ten seconds because it was
 * told to wait five must not look like a slow network.
 */
internal object Http {

    /**
     * A GET, for the two things a POST cannot do: ask a provider what it offers, and fetch
     * what a prediction left at a URL.
     *
     * Same rule as [post] on headers -- none of them ever reaches a message or the trace.
     */
    fun get(url: String, headers: Map<String, String> = emptyMap()): ByteArray =
        send("GET", url, headers, contentType = null, body = null)

    fun post(
        url: String,
        headers: Map<String, String>,
        contentType: String,
        body: ByteArray,
    ): ByteArray {
        // Guarded rather than left to Trace: reading a whole body back out as text is
        // not free, and a release must not pay for an instrument it does not carry.
        if (Trace.on) Trace.add("POST $url", "sent" to sent(contentType, body))
        return send("POST", url, headers, contentType, body)
    }

    private fun send(
        method: String,
        url: String,
        headers: Map<String, String>,
        contentType: String?,
        body: ByteArray?,
    ): ByteArray {
        var attempt = 0
        var waited = 0L
        while (true) {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 60_000
                contentType?.let { setRequestProperty("Content-Type", it) }
                headers.forEach { (name, value) -> setRequestProperty(name, value) }
                if (body != null) doOutput = true
            }
            try {
                if (body != null) connection.outputStream.use { it.write(body) }
                val status = connection.responseCode
                if (status in 200..299) {
                    val answer = connection.inputStream.use { it.readBytes() }
                    if (Trace.on) {
                        Trace.add(
                            "$status from $url",
                            "back" to sent(connection.contentType.orEmpty(), answer),
                        )
                    }
                    return answer
                }

                val said = connection.errorStream?.readBytes()?.decodeToString().orEmpty().take(400)
                val pause = if (status == THROTTLED) waitFor(connection, said) else null
                if (pause == null || attempt >= TRIES || waited + pause > WAITING_CEILING_MS) {
                    Trace.fail("$status from $url", "said" to said)
                    throw ChainFailure("$url answered $status: $said")
                }
                Trace.add(
                    "$status from $url -- waiting as asked",
                    "seconds" to (pause / 1000.0).toString(),
                    "said" to said,
                )
                attempt++
                waited += pause
                connection.disconnect()
                Thread.sleep(pause)
                continue
            } catch (e: IOException) {
                Trace.fail("unreachable: $url", "why" to e.message)
                throw ChainFailure("$url could not be reached", e)
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * How long the server asked to be left alone, in milliseconds, or null if it did not say.
     *
     * The standard header comes first; Replicate also puts the number in its body, and a
     * server that throttles without naming a delay gets [BLIND_WAIT] -- a documented default
     * at the contract, not a silence over something missing. Anything longer than the ceiling
     * is refused by the caller rather than slept through: a turn that stalls for a minute has
     * failed, whatever the reason.
     */
    private fun waitFor(connection: HttpURLConnection, said: String): Long {
        connection.getHeaderField("Retry-After")?.toDoubleOrNull()?.let {
            return (it * 1000).toLong().coerceAtLeast(0)
        }
        val stated = runCatching { JSONObject(said).optDouble("retry_after") }.getOrNull()
        if (stated != null && !stated.isNaN() && stated > 0) return (stated * 1000).toLong()
        return BLIND_WAIT
    }

    /**
     * What a body is worth showing as. Text is written out; audio is not -- a wav dumped as
     * mojibake tells nothing, its size tells something.
     */
    private fun sent(contentType: String, body: ByteArray): String =
        if (contentType.startsWith("application/json") || contentType.startsWith("text/")) {
            Trace.cut(body.decodeToString())
        } else {
            "${body.size} bytes of $contentType"
        }

    private const val THROTTLED = 429
    private const val TRIES = 3
    private const val BLIND_WAIT = 5_000L

    /** Past this, waiting is worse than failing: the caller keeps the recording either way. */
    private const val WAITING_CEILING_MS = 30_000L
}
