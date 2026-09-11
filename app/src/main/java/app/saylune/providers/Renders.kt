package app.saylune.providers

import android.content.Context
import app.saylune.chain.Voice
import app.saylune.debug.Trace
import java.io.File
import java.security.MessageDigest

/**
 * Where a synthesised model is kept, so that one render serves three times: a yardstick for
 * the measure, a model to hear, and a model to hear again at every retry.
 *
 * Shared by the synthesis implementations rather than owned by one, because the cache is a
 * property of the app and not of a provider -- and because the key has to hold the provider
 * anyway. Two providers saying the same sentence produce two different models, and the take
 * is measured against exactly one of them.
 *
 * The model name is in the key beside the voice: `chatterbox` and `chatterbox-turbo` offer
 * the same voice names and do not render the same voice.
 *
 * **Under a size ceiling, oldest use evicted first**, which is what the doc asks: this is
 * only ever a cache, regenerable at the price of one call, and the ceiling is the user's to
 * set because what it is worth to hold depends on the phone holding it.
 */
internal object Renders {

    /** No ceiling was set, so this one. Roughly five thousand sentences, measured at 96 kB. */
    const val DEFAULT_CAP_MB = 500

    fun file(context: Context, text: String, voice: Voice, model: String): File {
        val dir = home(context)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("${voice.provider}/$model/${voice.id}/$text".toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
        val kept = File(dir, "$digest.wav")
        // A hit is a use, and the eviction sorts on this. Without the touch the date would
        // say when the sentence was first rendered, so the one asked for every day would go
        // out ahead of one nobody has wanted since.
        if (kept.isFile) kept.setLastModified(System.currentTimeMillis())
        return kept
    }

    /**
     * Where the acoustic reading of [wav] is kept, for the engine that [version] names.
     *
     * Beside the renders, so the ceiling counts it and [prune] evicts it like one. Named by
     * the bytes of the audio and not by its path: a sentence rendered again under the same
     * key can come back as different samples, and a reading of the old ones would then face
     * a model that says the same words another way.
     */
    fun reading(context: Context, wav: File, version: String): File {
        val audio = MessageDigest.getInstance("SHA-256").digest(wav.readBytes())
            .joinToString("") { "%02x".format(it) }.take(32)
        val engine = MessageDigest.getInstance("SHA-256").digest(version.toByteArray())
            .joinToString("") { "%02x".format(it) }.take(16)
        return File(home(context), "$audio-$engine.reading")
    }

    private fun home(context: Context) = File(context.cacheDir, "renders").apply { mkdirs() }

    /**
     * Bring the cache back under [capMb] megabytes, least recently **asked for** first.
     *
     * Asked for and not written: [file] touches a render whenever it hands one back, so a
     * sentence that keeps coming up survives however old it is. What this does not count is
     * a model replayed from an utterance, which reaches the file by the path stored with the
     * turn and never comes through here -- so a much-replayed model of a sentence never said
     * again can still be evicted, and is then re-rendered at the price of one call.
     *
     * Run after a render rather than before: pruning to make room for a file that then fails
     * to arrive would throw away sentences to hold nothing.
     */
    fun prune(context: Context, capMb: Int) {
        val cap = capMb.toLong() * 1024 * 1024
        val files = home(context).listFiles()?.toMutableList() ?: return
        var held = files.sumOf { it.length() }
        if (held <= cap) return
        // Oldest touch first, so what goes is what has been left alone longest.
        files.sortBy { it.lastModified() }
        var gone = 0
        for (file in files) {
            if (held <= cap) break
            val size = file.length()
            if (file.delete()) { held -= size; gone++ }
        }
        Trace.add("renders: pruned", "removed" to gone.toString(),
                  "held" to (held / (1024 * 1024)).toString() + " MB of " + capMb)
    }
}
