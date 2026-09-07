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

    /** What opens an episode. [ofTurn] finds it by its dash, whatever it is named. */
    private const val TURN = "— turn —"

    /** logcat drops a line past about 4 kB, so a long body goes out in pieces. */
    private const val LINE = 3_500

    val on: Boolean get() = BuildConfig.DEBUG

    private val _steps = MutableStateFlow<List<Step>>(emptyList())
    val steps: StateFlow<List<Step>> = _steps.asStateFlow()

    @Volatile
    private var origin = SystemClock.elapsedRealtime()

    /**
     * A new turn begins: the clock the steps are timed against restarts here.
     *
     * [named] so that a saying-again, which is pipe B alone and has its own short chain, is
     * timed from its own start rather than from the conversation turn it repeats.
     */
    fun turn(named: String = TURN) {
        if (!on) return
        origin = SystemClock.elapsedRealtime()
        add(named)
    }

    /**
     * The steps of the episode under way -- everything since the last [turn].
     *
     * Read by [app.speakup.conversation.Takes] so a kept turn carries its own timings.
     * **The bodies are left behind**: they hold whole request payloads, an audio data-URI
     * among them, and what a latency is read from is the name and the moment.
     */
    fun ofTurn(): List<Step> {
        val steps = _steps.value
        val from = steps.indexOfLast { it.name.startsWith("—") }
        return if (from < 0) steps else steps.drop(from + 1)
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

    /**
     * How many passages' prompts are kept. Bounded like [KEPT] and for a sharper reason:
     * every prompt holds the whole history, so keeping all of them grows as the square of
     * the sitting.
     */
    private const val PROMPTS = 40

    private val prompts = LinkedHashMap<String, String>()

    /** The last body handed to a language model, waiting for a passage to be named. */
    @Volatile
    private var pending: String? = null

    /**
     * What a conversation provider is about to send, whole, kept until [askedFor] names the
     * passage it is for.
     *
     * **What is kept is what was sent, and nothing is rebuilt.** The prompt is a function of
     * the sitting -- the brief, the cast, the run, the levers -- so it could be made again
     * from what the base holds, and that would be a different thing: a rebuilt prompt says
     * what *would* go out now, and reading it as what did go out is the mix of the measured
     * and the deduced the project refuses. The price is that this lives in memory alone -- a
     * reopened sitting, and everything before the last start of the process, has none.
     */
    fun asking(body: String) {
        if (on) pending = body
    }

    /**
     * Called once the turn is written, naming the passage that call was for.
     *
     * **The passage and not the attempt.** A rewording makes a call of its own and replaces
     * the passage's prompt with the newer one, which is the one that produced what stands;
     * a repeat makes no call at all, and keyed by attempt it would open on nothing.
     */
    @Synchronized
    fun askedFor(passage: String) {
        val body = pending ?: return
        pending = null
        prompts.remove(passage)
        prompts[passage] = body
        while (prompts.size > PROMPTS) prompts.remove(prompts.keys.first())
    }

    /** What went out for [passage], or null where this process never sent it. */
    @Synchronized
    fun asked(passage: String): String? = prompts[passage]

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
