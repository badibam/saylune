package app.speakup.providers

import app.speakup.chain.ChainFailure
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The little HTTP the three remote links need, on the platform's own client.
 *
 * No library: three POSTs do not earn a dependency, and every dependency is one more thing
 * to rebuild offline for a reproducible build.
 *
 * Nothing here ever puts a header into a message. A failure carries the status and the
 * provider's own words, never what was sent -- the keys live behind the Keystore precisely
 * so they never end up somewhere readable, and an exception message is readable.
 */
internal object Http {

    fun post(
        url: String,
        headers: Map<String, String>,
        contentType: String,
        body: ByteArray,
    ): ByteArray {
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
                throw ChainFailure("$url answered $status: $said")
            }
            return connection.inputStream.use { it.readBytes() }
        } catch (e: IOException) {
            throw ChainFailure("$url could not be reached", e)
        } finally {
            connection.disconnect()
        }
    }
}
