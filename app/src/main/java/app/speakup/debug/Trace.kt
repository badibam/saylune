package app.speakup.debug

import android.os.SystemClock
import android.util.Log
import app.speakup.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * What each step of a turn actually sent and got back, so a link that misbehaves can be
 * looked at instead of guessed at.
 *
 * A global rather than a parameter, and that is the whole reason it exists as an object: the
 * three seams of the chain are deliberately free of anything that is not their job -- no key
 * in the signature, no network failure in the contract (`docs/reference.md`). A trace
 * parameter threaded through `transcribe`, `reply` and `speak` would be exactly that kind of
 * dirt, and it would outlive the debugging it was added for.
 *
 * Every step also goes to logcat under [TAG], so a turn can be read from the desk with
 * `adb logcat -s speakup` instead of being transcribed off the phone. Same content, same
 * omissions -- the screen and the log say exactly the same thing.
 *
 * Nothing is recorded in a release build: [on] is the build type, and R8 drops the dead
 * branch. What is recorded never includes a header -- the keys live behind the Keystore so
 * that they never reach anywhere readable, and a screen is readable.
 */
object Trace {

    /** Enough to hold several turns, bounded so a long session cannot eat the heap. */
    private const val KEPT = 200

    /** A body longer than this is cut; the brick says so rather than lying about it. */
    const val BODY_CEILING = 20_000

    private const val TAG = "speakup"

    /** logcat drops a line past about 4 kB, so a long body goes out in pieces. */
    private const val LINE = 3_500

    val on: Boolean get() = BuildConfig.DEBUG

    private val _steps = MutableStateFlow<List<Step>>(emptyList())
    val steps: StateFlow<List<Step>> = _steps.asStateFlow()

    @Volatile
    private var origin = SystemClock.elapsedRealtime()

    /** A new turn begins: the clock the steps are timed against restarts here. */
    fun turn() {
        if (!on) return
        origin = SystemClock.elapsedRealtime()
        add("— turn —")
    }

    fun add(name: String, vararg bricks: Pair<String, String?>) = record(name, false, bricks, true)

    /**
     * To logcat and not to the screen, for what is too wide to be read on a phone.
     *
     * A monospace table wide enough to be complete is wider than any phone, and shown in a
     * dialog it wraps into nonsense -- which is a worse way to say something than not saying
     * it. In the log the width costs nothing. What the screen needs instead is laid out
     * rather than written out (`../ui/AnalysisReadout.kt`).
     */
    fun wide(name: String, vararg bricks: Pair<String, String?>) = record(name, false, bricks, false)

    /** Same, but the step is the one that gave way. */
    fun fail(name: String, vararg bricks: Pair<String, String?>) = record(name, true, bricks, true)

    fun clear() {
        origin = SystemClock.elapsedRealtime()
        _steps.value = emptyList()
    }

    /** A body to record, cut to [BODY_CEILING] and saying so when it was. */
    fun cut(body: String): String =
        if (body.length <= BODY_CEILING) body
        else body.take(BODY_CEILING) + "\n\n[cut here — ${body.length} chars in all]"

    private fun record(
        name: String,
        failed: Boolean,
        bricks: Array<out Pair<String, String?>>,
        onScreen: Boolean,
    ) {
        if (!on) return
        val step = Step(
            onScreen = onScreen,
            name = name,
            atMs = SystemClock.elapsedRealtime() - origin,
            failed = failed,
            bricks = bricks.mapNotNull { (label, body) -> body?.let { Brick(label, it) } },
        )
        _steps.update { (it + step).takeLast(KEPT) }
        log(step)
    }

    private fun log(step: Step) {
        Log.d(TAG, "%s  +%.1fs%s".format(step.name, step.atMs / 1000f, if (step.failed) "  FAILED" else ""))
        step.bricks.forEach { brick ->
            if (brick.body.length <= LINE) {
                Log.d(TAG, "    ${brick.label}: ${brick.body}")
            } else {
                Log.d(TAG, "    ${brick.label}: (${brick.body.length} chars)")
                brick.body.chunked(LINE).forEachIndexed { i, piece -> Log.d(TAG, "      [$i] $piece") }
            }
        }
    }
}

/** One thing that happened, in the order it happened, with how long into the turn. */
data class Step(
    val name: String,
    val atMs: Long,
    val bricks: List<Brick>,
    val failed: Boolean = false,
    /** False for a step the log carries and the panel does not: see [Trace.wide]. */
    val onScreen: Boolean = true,
)

/**
 * One named piece of a step.
 *
 * A short body is shown as it is; a long one is shown as its name and opens on a tap. The
 * split is [long], and it is about the screen alone -- nothing upstream cares.
 */
data class Brick(val label: String, val body: String) {
    val long: Boolean get() = body.length > 60 || '\n' in body
}
