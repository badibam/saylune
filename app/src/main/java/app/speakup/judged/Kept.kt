package app.speakup.judged

/**
 * The stretches of the turn the model was made to say, and the way back from its text to the
 * whole one.
 *
 * **The model's voice says only the kept words.** Making it say *"It was, like, um, I went to
 * the…"* is out of the question: the model is what is given to imitate, and the whole montage
 * rests on it being the truth. **The learner, though, is aligned on everything he said** --
 * without the hesitations in the text those bits of audio have no letter facing them and
 * become added sounds, so hesitating would cost a pronunciation mark.
 *
 * So the two sides no longer carry the same text, one containing the other, and something has
 * to carry the offsets across. That is this: the model's sounds are joined to [text], and
 * every offset they come back with is [inWhole] away from the string the marks index into.
 *
 * **Price accepted**: at the seams, a sound or two is compared out of its context. The kept
 * *I* that follows an abandoned *the* does not have in front of it what the model has in
 * front of his, and a sound is coloured by the one before it. It does not spread -- a fault
 * does not contaminate the rest of the sentence -- and saying the sentence again cancels it,
 * a sentence said in one breath having no seam at all. One more reason to have a stumbling
 * turn said again.
 */
class Kept(
    /** The whole turn as the screen shows it, hesitations included. */
    val whole: String,
    val ranges: List<IntRange>,
) {

    init {
        ranges.zipWithNext().forEach { (before, after) ->
            require(before.last < after.first) {
                "kept stretches that overlap or run backwards: $before then $after"
            }
        }
        ranges.forEach {
            require(it.first >= 0 && it.last < whole.length) {
                "a kept stretch of $it outside a text of ${whole.length}"
            }
        }
    }

    /**
     * What the model is made to say: the kept stretches, one space between them.
     *
     * A space and not the text that lay between them, which is exactly the filler and the
     * abandoned starts being left out; and not nothing either, or two kept words either side
     * of a hesitation would run into one.
     */
    val text: String = ranges.joinToString(" ") { whole.substring(it.first, it.last + 1) }

    /** Whether the model says the whole turn, which is the ordinary case of a clean one. */
    val entire: Boolean = ranges.size == 1 && ranges.first().first == 0 &&
        ranges.first().last == whole.length - 1

    /**
     * Where [at], an offset into [text], sits in the whole turn.
     *
     * An offset landing on one of the separators has no letter of its own in the whole turn:
     * it belongs to the gap the hesitation left, so it answers with the start of the stretch
     * that follows -- the nearest real letter in the direction the reading runs.
     */
    fun inWhole(at: Int): Int {
        require(at in 0..text.length) { "an offset of $at outside a model text of ${text.length}" }
        var seen = 0
        ranges.forEach { range ->
            val length = range.last + 1 - range.first
            if (at < seen + length) return range.first + (at - seen)
            seen += length
            // The separator that follows this stretch.
            if (at == seen) return range.last + 1
            seen++
        }
        return whole.length
    }

    /** [span] read in the whole turn, both ends carried across. */
    fun inWhole(span: IntRange): IntRange = inWhole(span.first)..inWhole(span.last)

    companion object {

        /**
         * The kept stretches of [text], read off the stumbling the judge marked.
         *
         * Consecutive kept words are one stretch and not several: what the model has to say is
         * a sentence, and cutting it at every space would put a seam between every word.
         */
        fun of(text: String, stumbling: List<Marked>): Kept {
            val notched = unfold(text, stumbling, STUMBLING, KEPT)
            val ranges = mutableListOf<IntRange>()
            notched.filter { it.notch == KEPT }.forEach { word ->
                val last = ranges.lastOrNull()
                // Run them together when nothing but blanks separates them.
                if (last != null && text.substring(last.last + 1, word.at.first).isBlank()) {
                    ranges[ranges.lastIndex] = last.first..word.at.last
                } else {
                    ranges += word.at
                }
            }
            return Kept(text, ranges)
        }

        /** Worst first, which is what makes a word carry at most one of them. */
        val STUMBLING = listOf("abandonne", "remplissage", "retenu")

        const val KEPT = "retenu"
    }
}
