package app.speakup.providers

import android.content.Context
import app.speakup.chain.Voice
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
 * No size ceiling and no eviction yet, deliberately -- the doc asks for both
 * (`../../../../../../TODO.md`, chantier 0).
 */
internal object Renders {

    fun file(context: Context, text: String, voice: Voice, model: String): File {
        val dir = File(context.cacheDir, "renders").apply { mkdirs() }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("${voice.provider}/$model/${voice.id}/$text".toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(32)
        return File(dir, "$digest.wav")
    }
}
