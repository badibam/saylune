package app.speakup.embedded

import app.speakup.marking.AddedSound

/**
 * The sounds the learner made that belong to no word of the text.
 *
 * Everything else walks the grid decoded from the **model alone**, the learner forced onto
 * it, so there are exactly as many slots as the model has sounds and a sound the learner
 * adds has none. What finds them is not a second comparison against the model but the very
 * reading the model already gets: [Join] takes a sequence of sounds and a text and says
 * which letters each sound writes. Pointed at the learner's own decoding, it places the
 * learner.
 *
 * The letters then hold both readings at once, which is what lets them face each other
 * **without a clock**: the model's contracted `ɝ` covers `ou're`, and the learner who does
 * not contract puts a `ʊ` on the `o` and a `ɹ` on the `r`, inside the same word. So a fuller
 * reading of a word stays in the word, where the sound marks already speak for it. A sound
 * no letter of any word can write, at its place in the order, belongs to no word -- and that
 * is the whole detection. No duration is read here, by design: measured, free decoding gives
 * labels and peak positions and no durations at all, and forced alignment has no slot for an
 * added sound, so neither of them can say how long added matter lasted.
 *
 * Two things it is known to get wrong, both accepted rather than unnoticed. A substitution
 * no letter of its own word can write falls **out** of the word and reads as added matter --
 * `have` said with an `f` for its `v`, `I sink` for `I think`. The word carries a mark either
 * way and the readout names the sound, so the reader can see what happened. And a sound the
 * learner produced that English writes with no letter at all -- the learner's side of the
 * gutter -- falls out too, since it has no letter to land on.
 */
object Added {

    /**
     * [said] is the learner's own sounds put on the letters, [sounds] the model's, and
     * [gaps] the sounds that were compared -- the last only so a mark can name the readout
     * line it follows.
     */
    fun found(
        said: List<Sound>,
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
    ): List<AddedSound> {
        val marks = mutableListOf<AddedSound>()
        val run = mutableListOf<String>()
        var opened = -1
        // The end of the last word the learner actually landed in. A borrowed letter belongs
        // to the neighbour that took it, so it does not move this -- the same rule the
        // gutters already keep on the model's side, in `Marks.drawn`.
        var boundary = -1
        for (sound in said) {
            if (sound.spots.isEmpty() && sound.borrowed.isEmpty()) {
                if (run.isEmpty()) opened = boundary
                run.add(sound.symbol)
                continue
            }
            if (run.isNotEmpty()) {
                marks.add(mark(sounds, gaps, opened, run))
                run.clear()
            }
            if (sound.spots.isNotEmpty()) sound.wordAt?.let { boundary = it.last }
        }
        if (run.isNotEmpty()) marks.add(mark(sounds, gaps, opened, run))
        return marks
    }

    /**
     * One stretch of loose sounds, placed on the text and on the readout.
     *
     * A run of neighbouring loose sounds is **one** mark and not one each: a whole word said
     * in addition is a burst of them at a single word boundary, and it is one thing that
     * happened. Its place is the seam just after [after], the way a gutter's is -- the mark
     * belongs between two letters, and a fault that is found must not vanish for want of
     * somewhere to paint it.
     *
     * [after] is the **end of a word**, never a letter inside one. Measured on a real turn:
     * `I'm trying how to learn english` against `I'm trying to learn English.` -- the `aʊ` of
     * `how` was paid for by the `g` of `trying`, which left the `ŋ` holding the `n` alone, so
     * the loose `h` anchored on the last letter claimed landed between the `n` and the `g`
     * and was written before the `ŋ`. Anchoring on that letter is not wrong there, it is
     * incoherent: a stretch that belongs to no word cannot sit inside one, and the reader
     * sees a mark placed before a letter that was said before it.
     *
     * The readout line is the last compared sound whose own letters end at or before the
     * seam, and -1 before the first of them. Sounds holding no letter are stepped over
     * rather than counted: they say nothing about where in the text they sit, so a mark
     * lands before them.
     */
    private fun mark(
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        after: Int,
        run: List<String>,
    ): AddedSound {
        var line = -1
        gaps.forEachIndexed { index, gap ->
            val spots = sounds[gap.rank].spots
            if (spots.isNotEmpty() && spots.max() <= after) line = index
        }
        return AddedSound(symbol = run.joinToString(" "), after = after, afterSound = line)
    }
}
