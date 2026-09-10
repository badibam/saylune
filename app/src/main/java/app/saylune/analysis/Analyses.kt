package app.saylune.analysis

import android.content.Context
import app.saylune.keys.SecretStore

/**
 * The debug build carries the engine; the release one does not, and says so.
 *
 * Two implementations rather than a flag, so that the day the engine is chosen the release
 * stand-in is deleted and this moves to `main`. Nothing that calls [Analysis] changes.
 */
object Analyses {
    fun onDevice(context: Context): Analysis = EmbeddedAnalysis(context)

    /**
     * The analysis the user settled on, which is this device unless they said otherwise.
     *
     * The store is handed in rather than read here, for the reason the other three links are
     * built the same way: what is chosen is read when it is used, so a change in the settings
     * takes hold without restarting anything.
     */
    fun chosen(context: Context, store: SecretStore): Analysis =
        EmbeddedAnalysis(context, store)
}
