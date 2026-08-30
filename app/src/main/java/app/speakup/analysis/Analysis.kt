package app.speakup.analysis

import androidx.annotation.StringRes
import app.speakup.marking.AddedSound
import app.speakup.marking.TurnMarking
import java.io.File

/**
 * The analysis: two recordings of the same sentence in, marks on its text out.
 *
 * The shape of this seam follows what the app actually has, which is what makes the whole
 * montage unusual (`docs/reference.md`). Whoever holds only a learner's audio and a text
 * needs a dictionary to give itself a norm; this app holds **two recordings of the same
 * utterance**, because it synthesises the model anyway -- that is the one it is heard. So
 * the model is the norm, and nothing outside these two files judges anything.
 *
 * What is deliberately absent from the signature, and each absence is a decision:
 *
 * - **No weights, no engine, no path to either.** Which acoustic model the app ships with is
 *   not decided, and a seam naming one would have to be rewritten the day it is.
 * - **No provider and no key.** The analysis runs on the device and has no key at all. It is
 *   the one link that never leaves.
 * - **No accent, no dialect, no lexicon.** The accent setting picks the voice that made
 *   [model], and there is no third thing to align: the model is the only norm.
 * - **No severity, no threshold.** Marking is an exigence read off these numbers by whoever
 *   displays them, not a decision taken in here. The cursor moves the bar, never the measure.
 *
 * The caller synthesises [model] and keeps [said]; this only reads them.
 */
interface Analysis {

    /**
     * Whether marks can be produced at all -- settled **once for the session**, never per
     * turn (`docs/reference.md`, "La panne").
     *
     * The analysis depends on no network, which removes the quota, the expired key and the
     * provider outage in one go. What is left is of another kind and does not come and go
     * mid-session: the weights are not downloaded, or the device will not load them. A turn
     * stays analysable as long as the session started with its model loaded.
     */
    suspend fun readiness(): Readiness

    /**
     * What [said] does differently from [model], anchored to [text].
     *
     * [text] is the string the marks index into, and it must be the very one displayed:
     * every offset in the result is an offset into it, so showing a different string would
     * slide every mark.
     *
     * A sound that could not be compared yields no mark rather than a made-up one -- but
     * silence is never an issue in itself (`docs/reference.md`), so a reading that had to
     * drop something says so through [Analysed.dropped] instead of quietly shortening.
     */
    suspend fun examine(said: File, model: File, text: String): Analysed
}

/**
 * Whether the marks are on, and when they are not, why.
 *
 * An option that is off **carries its reason**, here as everywhere: without it the app looks
 * broken rather than unequipped, and it is the app that gets blamed.
 */
sealed interface Readiness {
    data object On : Readiness

    /**
     * [reason] is a string resource and not a sentence, because the reason is the app's own
     * words and not a provider's -- the app owes them in the reader's language. [detail] is
     * what the device or the engine said, when there is such a thing, and stays as it came.
     */
    data class Off(@StringRes val reason: Int, val detail: String? = null) : Readiness
}

/**
 * One analysed turn.
 *
 * [marking] is what the screen draws. [gutters] are the sounds no letter can carry -- a
 * sound can hold no letter at all, measured at 1.4% of the bench's sounds, and a fault found
 * there is a fault that must not vanish for want of somewhere to paint it. [dropped] counts
 * the sounds of the model's grid that could not be compared, so a short reading is
 * distinguishable from a clean one.
 */
data class Analysed(
    val marking: TurnMarking,
    val gutters: List<Gutter>,
    val dropped: Int,
    /**
     * What was found, sound by sound -- of which [marking] is the *drawn* view.
     *
     * Not a debug extra. The parenthesis needs exactly this to make the model and the
     * learner's own take heard at the same place, which the doc says falls out of the same
     * calculation; and a spread is the only way to see what a gap is made of, since two
     * spreads can share a peak and mean different things.
     */
    val sounds: List<AnalysedSound>,
    /**
     * Sounds the learner made that the model did not -- the one thing the grid cannot hold,
     * since it has exactly as many slots as the model has sounds.
     */
    val added: List<AddedSound>,
    /**
     * Whether the alignment slid, in which case **nothing here is worth showing**.
     *
     * The forced alignment must visit every sound of the model in order, so when the learner
     * says something substantially different it has slack, and it spends the slack putting
     * each sound wherever that sound fits best. The comparison then finds an agreement it
     * manufactured itself: measured on a take saying `I said you are, hum, right` against
     * `I think you're right.`, the /θ/ of `think` scored a perfect zero on audio that was
     * /s/.
     *
     * This is exigence 1 of the analysis (`docs/reference.md`) -- see when it could not place
     * the sounds -- and the doc names its signature outright: absurd durations. An aberration
     * nobody sees passes for a fault of the learner's, which is the worst thing this can do.
     */
    val slid: Boolean,
)

/**
 * One sound of the model's grid, and what the two recordings did with it.
 *
 * [model] and [said] are the heaviest shares of each spread, biggest first, and they do not
 * sum to one -- what they leave out is the tail. The top symbol of [said] is a hint and
 * never a verdict: naming the sound produced is the least reliable thing an acoustic machine
 * renders, and nothing in the mark depends on it.
 */
data class AnalysedSound(
    val symbol: String,
    val points: Float,
    /** The letters it covers, or a neighbour's when it holds none, or empty for the gutter. */
    val letters: String,
    val borrowed: Boolean,
    /**
     * The characters it **owns**, as offsets into the text; empty when it owns none, its
     * place in the phrase then coming from its rank among the sounds rather than from here.
     *
     * A borrowed letter is not owned: it belongs to the neighbour that took it, and counting
     * it here would show it twice in a readout laid out on the text.
     */
    val at: IntRange,
    val model: List<Share>,
    val said: List<Share>,
    /** Where each side says it, in milliseconds -- the way back to audio to listen to. */
    val modelMs: IntRange,
    val saidMs: IntRange,
)

/** One sound's share of a spread. */
data class Share(val symbol: String, val part: Float)

/**
 * A sound with no letters of its own, sitting after the character at [after].
 *
 * [after] is -1 when the sound precedes every letter of the turn. It is the position of the
 * last letter any sound claimed, not of this one: a gutter has no letters, so its own spots
 * say nothing, and reading them put every gutter at the front of the sentence.
 */
data class Gutter(val symbol: String, val after: Int, val points: Float)
