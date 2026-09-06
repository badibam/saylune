package app.speakup.fluency

/**
 * The four fluency sheets that are calculated rather than judged.
 *
 * **Nothing is counted twice**, and that is the property the whole file is arranged around:
 * [continuity] owns all the silence **between** words, [longestSilence] owns the block,
 * [rate] reads only the time the mouth is articulating, and the stumbling column owns the
 * hesitations. Each of the four is written against what it alone sees.
 *
 * It stays free of Android and of the analysis so it can be proved in plain JVM: what it
 * takes is where the two recordings put each word, which the seam works out, and never audio.
 */
object Fluency {

    /**
     * **A pause is a silence between two words lasting at least this.**
     *
     * Two conditions, and each does its own work. The word boundary rules out what happens
     * *inside* a word -- a closure, a hold, a stammer held on are not pauses. The threshold
     * rules out the micro-blanks that fall between words: the /t/ of *to stop* shuts the
     * mouth for 50 to 150 ms in the blank before *stop*, and with no threshold a sentence
     * full of /p t k/ would look less continuous than one full of vowels -- **for its text,
     * not for its speaker**, and this sheet being read in the absolute, that bias would
     * cancel nowhere.
     *
     * It is shared with [rate], which excludes exactly what continuity counts.
     *
     * *Wanted above the longest closure of a stop, and not measured against one yet.*
     */
    const val PAUSE_MS = 200

    /**
     * **A grace of one second at both edges, never inside**, and on [continuity] alone.
     *
     * Without it the ordinary time to react and to press -- the same for everyone, whether
     * one said a word or twenty -- would weigh far more on a short turn than on a long one:
     * two seconds of reflex on *"Yes, I did"* is 50%, the same two seconds before a
     * twenty-second answer is almost nothing, for identical behaviour. It is not measured; it
     * is a judgement, set at the height of an ordinary reaction.
     *
     * It is **not** applied to [longestSilence], and that is what makes the doc's other
     * sentence hold -- *a silence of 6 s stays a silence of 6 s for whoever is looking for a
     * big blank*. The argument above is about a **share** being distorted on a short turn,
     * and a longest has no such distortion to correct: subtracting a second from it would
     * quietly move the very number a condition reads.
     */
    const val GRACE_MS = 1000

    /**
     * **The share of the turn spent in silence, minus the model's on the same sentence**, in
     * points of percentage. It can be negative.
     *
     * Comparing to the model does real work: what is left in his is not nothing, it is the
     * **legitimate prosodic pauses** at the comma and at the end of a clause. The model stops
     * there, and the learner is allowed to as well.
     *
     * The gap is written as a **difference and not a ratio**, for the reason that already
     * ruled out a ratio in melody: the model's silent share can fall to one or two percent on
     * a short sentence, and a ratio turns hypersensitive there -- the same learner at 20%
     * would give a ratio of 10 or of 40 depending on the denominator.
     *
     * Keeping quieter than the model is no fluency fault, and **nothing is clipped for that**:
     * a negative gap falls above the A bound, in the same band as zero.
     *
     * Null on a turn with no kept word: there is no model to compare against, and absent is
     * not nil.
     */
    fun continuity(turn: Turn): Float? {
        if (turn.kept.isEmpty()) return null
        val said = silentShare(turn.spoken.map { it.said }, turn.recorded, grace = true)
        val model = silentShare(turn.kept.mapNotNull { it.model }, turn.rendered, grace = false)
        return (said - model) * 100f
    }

    /**
     * **The longest single silence, in seconds.**
     *
     * One element, its own length, and the series reads in seconds -- *"from three seconds it
     * is a C"*. Taking the silences as elements and averaging them would not do: a turn with
     * no silence at all would have no element, so no sheet, and the most fluent turn possible
     * would go unscored. A share of the turn spent in a blank would not do either, being
     * diluted by the turn's length -- the same 5 s blank is 50% of a 10 s turn and 17% of a
     * 30 s one, for identical behaviour. The worst silence depends on nothing but itself.
     *
     * What it ignores, and it is accepted: two blanks of 5 s are worth one. Accumulation is
     * what continuity carries.
     */
    fun longestSilence(turn: Turn): Float =
        silences(turn.spoken.map { it.said }, turn.recorded, grace = false)
            .maxOrNull()?.let { it / 1000f } ?: 0f

    /**
     * **The speed gap to the model, in percent, on the kept words alone.**
     *
     * ```
     * v     = the learner's speaking time / the model's
     * gap   = (the greater of v and 1/v) - 1
     * ```
     *
     * Compared to the model rather than in the absolute, which puts rate back under the
     * project's general rule -- *the model is the truth, departing from it is marked* -- that
     * it had no reason to escape. The text being the same on both sides there is **nothing to
     * count**, neither words nor syllables: the unit cancels, and the question of how many
     * words a second a good learner speaks, which has no answer independent of the text,
     * disappears.
     *
     * **Symmetric by construction**: twice as slow and twice as fast both give 100%.
     *
     * **Speaking time excludes the silences on both sides**, so silence is never counted
     * twice. Which means that detaching one's words instead of running them together -- the
     * French speaker's fault -- shows up in [continuity], as small pauses, which is fairer:
     * detaching words is literally manufacturing them. What rate alone sees is **articulating
     * slowly inside the words**: stretched vowels, over-pronounced consonants, and held
     * closures too, which make a silence but lengthen the word, a pause being a silence
     * *between* two words.
     *
     * **And it reads only the kept words.** The learner's raw speaking time holds the *ums*
     * and the abandoned starts, which the model never says: a hesitant turn would have an
     * inflated time and a large speed gap, and would be marked down on rate **on top of** its
     * own sheet.
     *
     * Null on a turn with no kept word: it would divide by zero.
     */
    fun rate(turn: Turn): Float? {
        val said = turn.kept.sumOf { it.said.length }.toFloat()
        val model = turn.kept.mapNotNull { it.model }.sumOf { it.length }.toFloat()
        if (said <= 0f || model <= 0f) return null
        val v = said / model
        return (maxOf(v, 1f / v) - 1f) * 100f
    }

    /** The silences this turn actually held, longest first -- what a condition reads, raw. */
    fun blanks(turn: Turn): List<Float> =
        silences(turn.spoken.map { it.said }, turn.recorded, grace = false)
            .map { it / 1000f }
            .sortedDescending()

    /**
     * The share of [whole] that is silence, counting only the blanks between words.
     *
     * The edges count: the one before the first word and the one after the last. **Handing
     * the floor back is an act** -- in a real conversation one signals having finished, and
     * being slow to do so is a genuine fault rather than noise to strip out.
     */
    private fun silentShare(spans: List<Span>, whole: Int, grace: Boolean): Float {
        if (whole <= 0) return 0f
        return silences(spans, whole, grace).sum().toFloat() / whole
    }

    /** Every silence of at least [PAUSE_MS], the two edges included. */
    private fun silences(spans: List<Span>, whole: Int, grace: Boolean): List<Int> {
        if (spans.isEmpty()) return emptyList()
        val ordered = spans.sortedBy { it.from }
        val out = mutableListOf<Int>()
        val lead = ordered.first().from - if (grace) GRACE_MS else 0
        val tail = (whole - ordered.last().to) - if (grace) GRACE_MS else 0
        if (lead >= PAUSE_MS) out += lead
        ordered.zipWithNext().forEach { (before, after) ->
            val blank = after.from - before.to
            if (blank >= PAUSE_MS) out += blank
        }
        if (tail >= PAUSE_MS) out += tail
        return out
    }
}

/** A stretch of time, in milliseconds from the start of its own recording. */
data class Span(val from: Int, val to: Int) {
    init { require(to >= from) { "a span that ends before it starts: $from..$to" } }
    val length: Int get() = to - from
}

/**
 * One word the mouth said, and where the two recordings put it.
 *
 * [model] is null on a word the model never said: **the model's voice says only the kept
 * words.** Making it say *"It was, like, um, I went to the…"* is out of the question -- the
 * model is what is given to imitate, and the whole montage rests on it being the truth.
 *
 * [notch] is the stumbling scale, `retenu` / `abandonne` / `remplissage`, judged and never
 * computed off a word list: *um* has no other use, but *I mean*, *like*, *well* are all real
 * words, and only the judge sees the use.
 */
data class Word(val said: Span, val model: Span?, val notch: String)

/**
 * A turn, as the fluency sheets read it.
 *
 * [spoken] is everything the mouth said, hesitations included: **the learner is aligned on
 * all of it**, because without the hesitations in the text those bits of audio have no letter
 * facing them and become added sounds -- so hesitating would cost a pronunciation mark.
 *
 * [recorded] and [rendered] are the two whole recordings, which is what a share is taken of.
 */
data class Turn(val spoken: List<Word>, val recorded: Int, val rendered: Int) {

    /** The words that are part of the sentence, which are the ones the model rendered. */
    val kept: List<Word> get() = spoken.filter { it.notch == KEPT }

    companion object {
        const val KEPT = "retenu"
    }
}
