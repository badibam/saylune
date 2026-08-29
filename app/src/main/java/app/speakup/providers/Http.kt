package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.debug.Trace
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The little HTTP the three remote links need, on the platform's own client.
 *
 * No library: three POSTs do not earn a dependency, and every dependency is one more thing
 * to rebuild offline for a reproducible build.
 *
 * Nothing here ever puts a header into a message, nor into the trace. A failure carries the
 * status and the provider's own words, never what was sent -- the keys live behind the
 * Keystore precisely so they never end up somewhere readable, and both an exception message
 * and a debug panel are readable.
 */
internal object Http {

    fun post(
        url: String,
        headers: Map<String, String>,
        contentType: String,
        body: ByteArray,
    ): ByteArray {
        // Guarded rather than left to Trace: reading a whole body back out as text is
        // not free, and a release must not pay for an instrument it does not carry.
        if (Trace.on) Trace.add("POST $url", "sent" to sent(contentType, body))
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 60_000
            setRequestProperty("Content-Type", contentType)
            headers.forEach { (name, value) -> setRequestProperty(name, value) }
        }
        try {
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            if (status !in 200..299) {
                val said = connection.errorStream?.readBytes()?.decodeToString().orEmpty().take(400)
                Trace.fail("$status from $url", "said" to said)
                throw ChainFailure("$url answered $status: $said")
            }
            val answer = connection.inputStream.use { it.readBytes() }
            if (Trace.on) {
                Trace.add("$status from $url", "back" to sent(connection.contentType.orEmpty(), answer))
            }
            return answer
        } catch (e: IOException) {
            Trace.fail("unreachable: $url", "why" to e.message)
            throw ChainFailure("$url could not be reached", e)
        } finally {
            connection.disconnect()
        }
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
}
