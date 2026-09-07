package app.saylune.analysis

import android.content.Context

/**
 * The debug build carries the engine; the release one does not, and says so.
 *
 * Two implementations rather than a flag, so that the day the engine is chosen the release
 * stand-in is deleted and this moves to `main`. Nothing that calls [Analysis] changes.
 */
object Analyses {
    fun onDevice(context: Context): Analysis = EmbeddedAnalysis(context)
}
