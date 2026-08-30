package app.speakup.capture

import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Say a wav, and come back when it has finished saying it.
 *
 * Suspending until the end is the whole point: the conversation's phases would otherwise
 * mean nothing -- "answering" would end while the answer was still being spoken.
 *
 * An error resumes rather than throws. A model that will not play is a disappointment and
 * not a broken turn, and the caller has nothing useful to do about it that it would not do
 * anyway.
 */
object Playback {

    suspend fun play(wav: File) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val player = MediaPlayer()
            player.setOnCompletionListener {
                it.release()
                if (continuation.isActive) continuation.resume(Unit)
            }
            player.setOnErrorListener { p, _, _ ->
                p.release()
                if (continuation.isActive) continuation.resume(Unit)
                true
            }
            continuation.invokeOnCancellation { runCatching { player.release() } }
            player.setDataSource(wav.path)
            player.prepare()
            player.start()
        }
    }
}
