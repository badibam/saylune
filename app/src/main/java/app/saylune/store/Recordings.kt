package app.saylune.store

import android.content.Context
import app.saylune.debug.Trace
import java.io.File

/**
 * The recordings on disk, and the ones nothing points at any more.
 *
 * **This is not the purge.** The purge is about audio that *is* still named by a turn, and
 * the design deliberately leaves it undecided -- it used to hang off the closing of a session
 * and there is no session now. This is the other thing entirely: files no utterance names,
 * which are not kept audio but litter.
 *
 * They come from two turns that end without an utterance. A turn the recognition heard
 * nothing in is dropped, on purpose, since holding the button by accident must not read as a
 * failure. And a turn whose chain gave way is held for a retry that may never come -- the
 * recording is kept precisely so a failed send costs a button rather than a spoken sentence,
 * and if the app is closed instead, nothing will ever ask for it again.
 */
internal object Recordings {

    /**
     * Delete every recording no utterance names.
     *
     * **At startup and nowhere else**, which is what makes it safe without a single guess:
     * nothing is in flight, nothing has been recorded yet, and no retry is pending, so a file
     * the store does not name is a file nothing will ever name. Run mid-session it would have
     * to guess at how new is too new to touch, and it would eventually guess wrong about the
     * turn somebody was in the middle of.
     */
    fun sweepOrphans(context: Context, named: Set<String>) {
        val home = File(context.filesDir, "turns")
        val files = home.listFiles() ?: return
        var gone = 0
        var freed = 0L
        files.forEach { file ->
            if (file.path in named) return@forEach
            val size = file.length()
            if (file.delete()) { gone++; freed += size }
        }
        if (gone > 0) {
            Trace.add("recordings: swept what nothing names",
                      "files" to gone.toString(), "kB" to (freed / 1024).toString())
        }
    }
}
