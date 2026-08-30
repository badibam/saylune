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
     * [said] is the learner's own sounds put on the letters, [model] the model's own -- read
     * only for what it too left unwritten.
     *
     * A mark comes out with **one** position, the character it sits just after. Where each
     * view draws it is that view's business, and both read the same number: carrying a
     * readout line beside it made two coordinates for one place, and they drifted.
     */
    fun found(said: List<Sound>, model: List<Sound>): List<AddedSound> {
        // What the model itself left unwritten, word by word. This is the one place the
        // channel would otherwise read a single recording, and the one that consumes the
        // **label** -- the least reliable thing the network renders. A reduced `I'm` comes
        // back as `ɑ n`, which neither `i` nor `m` can write, so a learner saying exactly
        // what the model said raised a mark. Cancelling against the model does not ask the
        // label to be right, only to be the same on both sides, which is the argument the
        // whole measure rests on.
        val unwritten = mutableMapOf<IntRange, Int>()
        for (sound in model) {
            if (sound.spots.isEmpty() && sound.borrowed.isEmpty() && sound.wordAt != null) {
                unwritten[sound.wordAt] = (unwritten[sound.wordAt] ?: 0) + 1
            }
        }

        val marks = mutableListOf<AddedSound>()
        val run = mutableListOf<String>()
        // The last letter the learner claimed, and the word it was in. A borrowed letter
        // belongs to the neighbour that took it, so neither moves for one -- the same rule
        // the gutters already keep on the model's side, in `Marks.drawn`.
        var claimed = -1
        var word: IntRange? = null

        fun close(next: Sound?) {
            // Between two words the run sat between them, so the mark goes to the end of the
            // word before it -- which is what keeps a wedge from splitting `ng` in two. Inside
            // one word it sat inside that word, and the mark stays on the last letter
            // claimed: pushing it to the word's end would put it after letters said before
            // it. The second case is real, not hypothetical -- `Join.trimmed` empties a sound
            // after the walk when its letters turn out to be worth nothing on it, and one
            // that can borrow none from a neighbour is then loose in the middle of a word.
            val after =
                if (word != null && next?.wordAt != word) word!!.last else claimed
            val ahead = next?.spots?.minOrNull()
            // The order, stated rather than hoped for: a mark never reaches back past the
            // letter said before it, and never past the letter said after it.
            check(after >= claimed) {
                "the mark ${run.joinToString(" ")} walks back: $after before letter $claimed"
            }
            check(ahead == null || after < ahead) {
                "the mark ${run.joinToString(" ")} passes letter $ahead, which was said after it"
            }
            marks.add(AddedSound(symbol = run.joinToString(" "), after = after))
            run.clear()
        }

        for (sound in said) {
            if (sound.spots.isEmpty() && sound.borrowed.isEmpty()) {
                val spare = sound.wordAt?.let { unwritten[it] } ?: 0
                if (spare > 0) {
                    unwritten[sound.wordAt!!] = spare - 1
                    continue
                }
                run.add(sound.symbol)
                continue
            }
            if (run.isNotEmpty()) close(sound)
            if (sound.spots.isNotEmpty()) {
                claimed = sound.spots.max()
                word = sound.wordAt
            }
        }
        if (run.isNotEmpty()) close(null)
        return marks
    }

}
