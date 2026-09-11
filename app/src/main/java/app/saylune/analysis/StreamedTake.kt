package app.saylune.analysis

import app.saylune.capture.Flac
import app.saylune.capture.Following
import app.saylune.capture.WavFile
import app.saylune.chain.ChainFailure
import app.saylune.debug.Trace
import app.saylune.embedded.PassReading
import app.saylune.providers.Http
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A take sent to the analysis server while it is said, a piece a second.
 *
 * The uplink sat idle for as long as someone spoke, then carried the whole take once they
 * stopped (`docs/design/remote-analysis.md`). Here each piece names where it starts in the
 * take and the server answers how much it holds, so a piece lost to the network is sent
 * again from there and a pause holds nothing open. On close only the last second is left to
 * send, and the server runs the pass at once -- while the language model and the synthesis
 * are still working, so the take's pass leaves the critical path along with its upload.
 *
 * **What the server read is proved to be the file**: the close carries the length and the
 * digest of the samples, and a server holding anything else refuses rather than reads.
 *
 * Every request runs on one thread of its own, so pieces and the close are never reordered.
 */
internal class StreamedTake(
    private val pcm: File,
    private val base: String,
    token: String,
    private val parse: (ByteArray) -> PassReading,
    private val landed: (File, CompletableFuture<PassReading>) -> Unit,
) : Following {

    private val id = UUID.randomUUID().toString()
    private val headers = mapOf("Authorization" to "Bearer $token")
    private val worker = Executors.newSingleThreadScheduledExecutor()
    private val ticking = worker.scheduleWithFixedDelay(
        { piece() }, PIECE_MS, PIECE_MS, TimeUnit.MILLISECONDS,
    )

    /** How many bytes the far end holds, as it last said. Touched on [worker] only. */
    private var held = 0L

    override fun closed(wav: File) {
        ticking.cancel(false)
        val reading = CompletableFuture<PassReading>()
        landed(wav, reading)
        worker.execute {
            try {
                val bytes = wav.readBytes()
                val samples = bytes.copyOfRange(WavFile.HEADER_BYTES, bytes.size)
                if (held < samples.size) {
                    held = sent(held, samples.copyOfRange(held.toInt(), samples.size))
                }
                if (held != samples.size.toLong()) throw ChainFailure(
                    "the analysis server holds $held bytes of a take of ${samples.size}"
                )
                val answer = Http.post(
                    url = "$base/take/$id/end?bytes=${samples.size}&sha256=${digest(samples)}",
                    headers = headers,
                    contentType = "application/octet-stream",
                    body = ByteArray(0),
                )
                reading.complete(parse(answer))
            } catch (failure: Exception) {
                reading.completeExceptionally(failure)
            }
        }
        worker.shutdown()
    }

    override fun dropped() {
        ticking.cancel(false)
        worker.execute {
            // The server forgets an unfinished take on its own after a while; this only says
            // so sooner, and a failure costs nothing but that.
            runCatching {
                Http.post("$base/take/$id/drop", headers, "application/octet-stream", ByteArray(0))
            }.onFailure { Trace.fail("analysis: a dropped take was not said", "why" to it.message) }
        }
        worker.shutdown()
    }

    private fun piece() {
        try {
            // Whole samples only: half of one would shift every sample after it.
            val length = pcm.length() and 1L.inv()
            if (length <= held) return
            val bytes = RandomAccessFile(pcm, "r").use { file ->
                file.seek(held)
                ByteArray((length - held).toInt()).also { file.readFully(it) }
            }
            held = sent(held, bytes)
        } catch (failure: Exception) {
            // Sent again from where the server says it stands, at the next tick or at the close.
            Trace.fail("analysis: a piece did not go", "why" to failure.message)
        }
    }

    /**
     * Send the samples [bytes] as starting at [at], and hand back how much the server now
     * holds. They travel in FLAC, each piece a file of its own; [at] and the answer count
     * bytes of the samples, never of what travelled.
     */
    private fun sent(at: Long, bytes: ByteArray): Long {
        val answer = Http.post("$base/take/$id?at=$at", headers, "audio/flac",
                               Flac.encodePcm(bytes))
        return JSONObject(answer.decodeToString()).getLong("held")
    }

    private fun digest(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private companion object {
        const val PIECE_MS = 1_000L
    }
}
