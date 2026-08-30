package app.speakup.embedded

import app.speakup.marking.AddedSound

/**
 * The sounds the learner made that the model did not.
 *
 * Everything else in the analysis walks the grid decoded from the **model alone**, the
 * learner forced onto it, so there are exactly as many slots as the model has sounds. A
 * sound the learner adds is therefore not badly scored -- it is not seen. Its only trace is
 * the time it steals inside a neighbour's span, which inflates that neighbour, so the mark
 * lands on a letter beside the one at fault.
 *
 * The way in costs no second pass: free decoding is the argmax of a matrix already computed
 * for the forced alignment, and lining the two symbol sequences up by edit distance drops
 * the insertions out. What it does cost is the **label**, which nothing else here consumes
 * -- naming the sound produced is the least reliable thing the network renders, and the doc
 * keeps it out of every comparison. It is admitted for one job only: choosing where to
 * *paint*, never deciding whether something is wrong. That is the class of the affinity
 * table, and it is the same licence.
 *
 * Measured before being written (`../../../../../../../TODO.md`): 1.3 insertions per take on
 * the labelled set, so the channel does not flood; four of twenty-two find a letter, all in
 * one word. It did **not** separate the labelled inserted syllable from its two controls.
 * Written on that footing, deliberately.
 */
object Added {

    /**
     * Every insertion, anchored where it can be.
     *
     * [model] and [said] are the two freely decoded symbol sequences; [sounds] is the join,
     * one entry per sound of the model's grid; [gaps] says which of those were compared, so
     * an insertion can name the readout line it follows.
     */
    fun found(
        model: List<String>,
        said: List<String>,
        sounds: List<Sound>,
        gaps: List<Overlap.Gap>,
        text: String,
        affinity: Affinity,
    ): List<AddedSound> {
        val claimed = sounds.flatMap { it.spots }.toSet()
        return lined(model, said).map { (rank, symbol) ->
            AddedSound(
                symbol = symbol,
                at = carrier(text, sounds, claimed, rank, symbol, affinity),
                after = sounds.take(rank + 1).flatMap { it.spots }.maxOrNull() ?: -1,
                afterSound = gaps.indexOfLast { it.rank <= rank },
            )
        }
    }

    /**
     * The learner's sounds against the model's, as an edit-distance path.
     *
     * Returns each sound of the learner that faces nothing on the model's side, as the rank
     * of the model sound it comes after -- -1 before the first. Substitutions and omissions
     * are left out on purpose: those are what the forced alignment already measures, and the
     * insertion is the only one with no slot to be measured in.
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
     * Only inside a word, and only a letter **no sound claimed** -- which is exactly the set
     * of silent letters, since every letter that carries a sound was taken by it. Across a
     * word boundary there is nothing to offer, and the mark goes between the two words.
     *
     * And only a letter lying **between the two sounds it fell between**, which is the rule
     * `Join.lent` already keeps for a borrowed letter: a sound marks where it is written,
     * not wherever in the word the table pays best. Measured: without the bound, a schwa
     * added in the middle of `comfortable` lit a letter at its far end.
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
