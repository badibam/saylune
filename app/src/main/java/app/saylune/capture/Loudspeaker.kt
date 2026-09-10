package app.saylune.capture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The one speaker of the app, and the rule for who gets it.
 *
 * A conversation screen offers a great many ways to make a sound at once: the AI's answer,
 * the triangle on any answer above it, the whole phrase of any turn, a word, a sound, a
 * sound the learner added, the recorded legend of a symbol -- for every passage on screen,
 * all of them live at the same time. Nothing held them apart, so two taps played two things
 * over each other, and a tap during the answer put two voices on one speaker.
 *
 * **The rule has two ranks and no queue.** What the app says of its own accord -- the answer,
 * the echo of a repair, the opening -- takes the speaker from anything a hand started. What a
 * hand starts takes it from another hand, since every one of these gestures means *let me
 * hear this instead of what I am hearing*. And a hand never cuts off the app: the tap is
 * dropped rather than held, holding it being a sound that arrives later for no reason anyone
 * could see.
 *
 * Nothing waits its turn. A gesture that lost the speaker is over, not postponed.
 */
object Loudspeaker {

    /** Who is asking. The default is [Hand] everywhere, which is the rank that yields. */
    enum class By { App, Hand }

    private val lock = Mutex()
    private var held: Held? = null
    private val stage = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private class Held(val by: By, val job: Job)

    /**
     * Play [sound] with the speaker to itself, and come back when it has finished -- or at
     * once, when the rule says this asker does not get it.
     *
     * [sound] must give up on cancellation: this is what stops the sound when something else
     * takes the speaker, and both readings in [Playback] are written for it.
     */
    suspend fun take(by: By, sound: suspend () -> Unit) {
        val job = lock.withLock {
            val standing = held?.takeIf { it.job.isActive }
            if (standing != null) {
                if (by == By.Hand && standing.by == By.App) return
                standing.job.cancel()
                // Waited out under the lock, so the player has released the device before the
                // next one asks it for it. Cancelling is quick by construction -- what it
                // interrupts is a wait, never a piece of work.
                standing.job.join()
            }
            stage.launch { sound() }.also { held = Held(by, it) }
        }
        try {
            job.join()
        } finally {
            // The caller was cancelled -- the screen left, the turn dropped. The sound goes
            // with it: it was being played *for* that caller.
            if (!job.isCompleted) job.cancel()
        }
    }
}
