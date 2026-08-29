package app.speakup.debug

import android.os.SystemClock
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
 * Nothing is recorded in a release build: [on] is the build type, and R8 drops the dead
 * branch. What is recorded never includes a header -- the keys live behind the Keystore so
 * that they never reach anywhere readable, and a screen is readable.
 */
object Trace {

    /** Enough to hold several turns, bounded so a long session cannot eat the heap. */
    private const val KEPT = 200

    /** A body longer than this is cut; the brick says so rather than lying about it. */
    const val BODY_CEILING = 20_000

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

    fun add(name: String, vararg bricks: Pair<String, String?>) = record(name, false, bricks)

    /** Same, but the step is the one that gave way. */
    fun fail(name: String, vararg bricks: Pair<String, String?>) = record(name, true, bricks)

    fun clear() {
        origin = SystemClock.elapsedRealtime()
        _steps.value = emptyList()
    }

    /** A body to record, cut to [BODY_CEILING] and saying so when it was. */
    fun cut(body: String): String =
        if (body.length <= BODY_CEILING) body
        else body.take(BODY_CEILING) + "\n\n[cut here — ${body.length} chars in all]"

    private fun record(name: String, failed: Boolean, bricks: Array<out Pair<String, String?>>) {
        if (!on) return
        val step = Step(
            name = name,
            atMs = SystemClock.elapsedRealtime() - origin,
            failed = failed,
            bricks = bricks.mapNotNull { (label, body) -> body?.let { Brick(label, it) } },
        )
        _steps.update { (it + step).takeLast(KEPT) }
    }
}

/** One thing that happened, in the order it happened, with how long into the turn. */
data class Step(
    val name: String,
    val atMs: Long,
    val bricks: List<Brick>,
    val failed: Boolean = false,
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
