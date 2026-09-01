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
     * Whether marks can be produced at all -- settled **once for the conversation**, never per
     * turn (`docs/reference.md`, "La panne").
     *
     * The analysis depends on no network, which removes the quota, the expired key and the
     * provider outage in one go. What is left is of another kind and does not come and go
     * midway: the weights are not downloaded, or the device will not load them. A turn
     * stays analysable as long as the conversation started with its model loaded.
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

    /**
     * [version] names the engine that will produce every reading of this conversation.
     *
     * **Everything stored carries the version of what produced it** (`activity-model.md`).
     * An analysis redone with a different model does not return the same numbers, and
     * without this stamp two eras of measurement add up in silence and the comparison is
     * wrong with nothing to say so. It is settled here rather than per reading because it
     * cannot change under a running conversation: the engine is loaded once and kept.
     */
    data class On(val version: String) : Readiness

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
 * [marking] is what the screen draws, the gutters among the rest of it -- a sound can hold
 * no letter at all, measured at 1.4% of the bench's sounds, and a fault found there must not
 * vanish for want of somewhere to paint it. [dropped] counts the sounds of the model's grid
 * that could not be compared, so a short reading is distinguishable from a clean one.
 */
data class Analysed(
    val marking: TurnMarking,
    val dropped: Int,
    /**
     * What was found, sound by sound -- of which [marking] is the *drawn* view.
     *
     Not a debug extra. Saying a sentence again needs exactly this to make the model and
     * the learner's own take heard at the same place, which the doc says falls out of the same
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
     * The learner's own free decoding, with times -- what the network heard, in order.
     *
     * Not used by any mark: the label is the least reliable thing the network renders, and
     * nothing here depends on it. It is kept because a take read back without it cannot be
     * told apart from a take read back wrongly -- twice in one session a kept take was read
     * as one fault when it was another, for want of this line.
     */
    val freely: List<Heard>,
)

/** One sound of the learner's free decoding, and when it was said. */
data class Heard(val symbol: String, val at: IntRange)

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


