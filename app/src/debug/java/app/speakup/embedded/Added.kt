package app.speakup.embedded

import app.speakup.marking.AddedSound

/**
 * The sounds the learner made that the model did not.
 *
 * Everything else walks the grid decoded from the **model alone**, the learner forced onto
 * it, so there are exactly as many slots as the model has sounds and a sound the learner adds
 * has none. Two signals find them, and they are kept apart because each is blind where the
 * other sees.
 *
 * **A swelling finds the long ones.** The alignment has to cover every frame of the learner's
 * audio -- it may not drop any -- so material with no model sound to belong to is swallowed
 * by the sounds either side of it, half each. A filler of half a second makes its two
 * neighbours five times the length of the take's other sounds, and the seam between them is
 * where it was said. Measured on three real takes of one sentence with a `hmm` in it: every
 * sound between 80 and 240 ms except the two straddling the filler, at 505/565, 626/666 and
 * 767/726. This reads the spans of the alignment the whole app already trusts, and consults
 * no label.
 *
 * **The edit distance finds the short ones.** A silent letter voiced adds sixty milliseconds
 * and swells nothing, so the swelling cannot see it at all; lining the two symbol sequences
 * up does. But it is flat -- it knows nothing of words -- and it pays for its own arithmetic:
 * measured, it called the `u` and the `r` of a correctly said `you're` insertions, to save
 * itself a substitution elsewhere. So only what a **silent letter can carry** is kept from
 * it: no letter, no mark, and the swelling is left to speak for the rest.
 */
object Added {

    /**
     * How many times the take's own median sound one sound may last before something extra
     * was said inside it.
     *
     * Against the take's median and not against the model, which is what makes it hold for a
     * slow speaker: speaking slowly moves the median with it, while swallowing a filler moves
     * two sounds and leaves the others alone. A line drawn between observations and not a
     * measured threshold -- on four real takes the swollen sounds sat at 4.0 to 4.7 times the
     * median and the largest ordinary one at 1.8. It wants a bench, and there is none yet.
     */
    const val SWELL = 3

    /**
     * [said] is the learner's free decode with its frame spans; [spans] the learner span of
     * each compared sound, widened, in the same order as [gaps]; [sounds] the join, one entry
     * per sound of the model's grid.
     */
    fun found(
        said: List<Segment>,
        spans: List<IntRange>,
        model: List<String>,
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        alphabet: Alphabet,
        text: String,
        affinity: Affinity,
    ): List<AddedSound> {
        val claimed = sounds.flatMap { it.spots }.toSet()
        val carried = lined(model, said.map { alphabet[it.symbol] }).mapNotNull { (rank, symbol) ->
            carrier(text, sounds, claimed, rank, symbol, affinity)?.let { letter ->
                AddedSound(
                    symbol = symbol,
                    at = letter,
                    after = letter,
                    afterSound = gaps.indexOfLast { it.rank <= rank },
                )
            }
        }
        return (carried + swollen(said, spans, sounds, gaps, alphabet)).sortedBy { it.after }
    }

    /**
     * The seams where the alignment had to swallow something, one mark each.
     *
     * A run of neighbouring swollen sounds is one insertion, not one per sound: the two that
     * shared a filler are two halves of the same event. The mark goes in the middle of the
     * run, which for the ordinary run of two is the seam between them.
     */
    private fun swollen(
        said: List<Segment>,
        spans: List<IntRange>,
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        alphabet: Alphabet,
    ): List<AddedSound> {
        if (spans.isEmpty()) return emptyList()
        val lengths = spans.map { it.last + 1 - it.first }
        val median = lengths.sorted()[lengths.size / 2]
        if (median <= 0) return emptyList()

        val marks = mutableListOf<AddedSound>()
        var index = 0
        while (index < lengths.size) {
            if (lengths[index] <= SWELL * median) {
                index++
                continue
            }
            var stop = index
            while (stop + 1 < lengths.size && lengths[stop + 1] > SWELL * median) stop++
            val seam = index + (stop - index) / 2
            // Silence swells a neighbour exactly as a sound does, and a pause is not a fault
            // of pronunciation. Seen at once on a frozen turn: half a second between `sheep`
            // and `is`, both neighbours swollen, and the learner's own decoding naming
            // nothing in the gap. So the swelling says where to look and the free decode says
            // whether there is anything there -- no sound named, no mark.
            val voiced = heard(said, spans, seam, sounds, gaps, alphabet)
            if (voiced.isEmpty()) {
                index = stop + 1
                continue
            }
            marks.add(
                AddedSound(
                    symbol = voiced,
                    // No letter: the material sits between two sounds, so it sits between
                    // their letters. A letter of its own is what the edit distance's half
                    // finds, and only when the spelling offers a silent one.
                    at = null,
                    after = sounds.take(gaps[seam].rank + 1).flatMap { it.spots }.maxOrNull()
                        ?: -1,
                    afterSound = seam,
                )
            )
            index = stop + 1
        }
        return marks
    }

    /**
     * What the learner's own decoding says in the seam, or empty when it says nothing new.
     *
     * The window runs from the middle of the swollen sound to the middle of the next, which
     * is the stretch neither of them needed. Anything the free decode puts there that is not
     * one of the two neighbours is the material that was swallowed. A hint like every label
     * here, and the mark does not depend on it: the swelling already said there was something.
     */
    private fun heard(
        said: List<Segment>,
        spans: List<IntRange>,
        seam: Int,
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        alphabet: Alphabet,
    ): String {
        val from = (spans[seam].first + spans[seam].last) / 2
        val to = spans.getOrNull(seam + 1)?.let { (it.first + it.last) / 2 } ?: spans[seam].last
        val neighbours = listOfNotNull(
            sounds.getOrNull(gaps[seam].rank)?.symbol,
            gaps.getOrNull(seam + 1)?.let { sounds.getOrNull(it.rank)?.symbol },
        )
        return said
            .filter { (it.start + it.stop) / 2 in from..to }
            .map { alphabet[it.symbol] }
            .filter { it !in neighbours }
            .joinToString(" ")
    }

    /**
     * The learner's sounds against the model's, as an edit-distance path.
     *
     * Returns each sound of the learner that faces nothing on the model's side, as the rank
     * of the model sound it comes after -- -1 before the first. Substitutions and omissions
     * are left out on purpose: those are what the forced alignment already measures.
     */
    fun lined(model: List<String>, said: List<String>): List<Pair<Int, String>> {
        val rows = model.size + 1
        val columns = said.size + 1
        val cost = Array(rows) { row -> IntArray(columns) { column ->
            if (row == 0) column else if (column == 0) row else 0
        } }
        for (row in 1 until rows) {
            for (column in 1 until columns) {
                val same = model[row - 1] == said[column - 1]
                cost[row][column] = minOf(
                    cost[row - 1][column - 1] + if (same) 0 else 1,
                    cost[row - 1][column] + 1,
                    cost[row][column - 1] + 1,
                )
            }
        }

        val added = mutableListOf<Pair<Int, String>>()
        var row = model.size
        var column = said.size
        while (row > 0 || column > 0) {
            if (row > 0 && column > 0) {
                val same = model[row - 1] == said[column - 1]
                if (cost[row][column] == cost[row - 1][column - 1] + if (same) 0 else 1) {
                    row--
                    column--
                    continue
                }
            }
            if (column > 0 && cost[row][column] == cost[row][column - 1] + 1) {
                added.add(row - 1 to said[column - 1])
                column--
                continue
            }
            row--
        }
        return added.reversed()
    }

    /**
     * The letter an insertion lights, or null when none can.
     *
     * Only inside a word, and only a letter **no sound claimed** -- exactly the set of silent
     * letters, since every letter that carries a sound was taken by it. And only one lying
     * between the two sounds it fell between, which is the rule `Join.lent` already keeps for
     * a borrowed letter: measured, without that bound a schwa added in the middle of
     * `comfortable` lit a letter at its far end.
     */
    private fun carrier(
        text: String,
        sounds: List<Sound>,
        claimed: Set<Int>,
        rank: Int,
        symbol: String,
        affinity: Affinity,
    ): Int? {
        val left = sounds.getOrNull(rank)?.wordAt ?: return null
        if (sounds.getOrNull(rank + 1)?.wordAt != left) return null
        val low = sounds[rank].spots.maxOrNull()?.let { it + 1 } ?: left.first
        val high = sounds[rank + 1].spots.minOrNull() ?: (left.last + 1)
        var best: Int? = null
        var weight = 0
        for (position in maxOf(low, left.first) until minOf(high, left.last + 1)) {
            if (position in claimed || !text[position].isLetter()) continue
            val paid = affinity.paid(text[position], symbol)
            if (paid > weight) {
                best = position
                weight = paid
            }
        }
        return best
    }
}
