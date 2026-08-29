package app.speakup.analysis

import android.content.Context
import app.speakup.R
import java.io.File

/**
 * A release carries no analysis, and says so rather than looking broken.
 *
 * The engine is a bet that is not settled: which acoustic model the app ships with is open
 * (`../../../../../../../TODO.md`), and the ONNX Runtime is held to the debug build so that
 * no release carries a native dependency on a guess -- nor owes F-Droid an answer about a
 * prebuilt library before the measure that would justify it exists.
 *
 * This is the whole reason the seam has two implementations rather than a flag: the day the
 * engine is chosen, this file is deleted and the debug one moves to `main`. Nothing that
 * calls [Analysis] changes.
 */
object Analyses {
    fun onDevice(context: Context): Analysis = Unshipped
}

private object Unshipped : Analysis {

    override suspend fun readiness(): Readiness = Readiness.Off(R.string.analysis_unshipped)

    override suspend fun examine(said: File, model: File, text: String): Analysed =
        // Unreachable through the app, which asks readiness first and puts the marks out
        // end to end when it is Off. Loud rather than empty all the same: an empty marking
        // reads exactly like a turn with nothing to report.
        throw IllegalStateException("no analysis is shipped in this build")
}
