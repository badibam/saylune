package app.speakup.notes

import app.speakup.sheets.Branch
import app.speakup.sheets.Node
import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets

/**
 * The weights a definition lays on the tree, by path.
 *
 * **The weights are constant for the whole sitting, and no patch moves one.** What is aimed at
 * is decided when the challenge is written and does not move again -- otherwise the closing
 * note would be a mean of measures taken under different rules, unreadable for the learner as
 * for a ranking.
 *
 * A challenge that scores nothing but lexical stress is a weight of 1 and weights of 0, not a
 * mechanism of its own: whatever is scored, the information is already there. At 0 a node does
 * not count -- and it is **not switched off**: it still computes, and a condition still reads
 * it.
 *
 * A path nobody declared a weight for fails outright. Silently taking it for 1 would put a
 * sheet in a challenge's note that its author never named, and taking it for 0 would drop one
 * they thought they had asked for; neither is distinguishable from what they meant.
 */
class Weights(private val byPath: Map<String, Float>) {

    init {
        require(byPath.values.all { it >= 0f }) { "a negative weight" }
    }

    /**
     * How much this sheet weighs in the sitting's note: **the weights multiply going down**.
     *
     * Elocution 2 over melody 1 means melody carries 2 x 1 against a correctness of 1 x 1, and
     * the ratio the author asked for is the one that comes out -- which is exactly what the
     * flat sum below protects and a cascade of means does not.
     */
    fun of(sheet: Sheet): Float {
        var weight = 1f
        var node: Node = Sheets.tree
        Sheets.pathOf(sheet).split("/").forEach { step ->
            node = (node as Branch).children.first { it.name == step }
            weight *= byPath[Sheets.pathOf(node)]
                ?: error("${Sheets.pathOf(node)}: no weight. The tree is the list of them.")
        }
        return weight
    }
}

/**
 * One sheet's figure on one passage, or the fact that it has none.
 *
 * A sheet that did not measure is **absent**, and it never counts as a zero: the fluency of a
 * turn with no kept word has nothing to divide by, and a passage whose word gate closed has no
 * sound at all.
 */
data class Measured(val sheet: Sheet, val figure: Float?)

/**
 * What one passage hands the aggregation.
 *
 * [keptWords] is the length every sheet weighs by. It is counted in **kept words** rather than
 * in sounds: a passage whose gate cut the analysis has no sounds and still has words.
 * Proportional to the length is a little arbitrary -- a melodic contour is a contour whether it
 * is long or short -- but it is uniform, and holding one over a long sentence is genuinely
 * more work.
 *
 * [difficulty] is what the model said of the turn it had just written, and it is the weight of
 * `suivi` alone: that sheet does not measure a length of the learner's, it measures how much
 * of the matter sent his way came back. Null on a passage with no AI turn in front of it,
 * where following is absent rather than nil.
 */
data class Passage(
    val keptWords: Int,
    val difficulty: Float?,
    val measured: List<Measured>,
)

/**
 * The note of a sitting: **computed once, flat, over the sheets actually present.**
 *
 * The letters of an aptitude and of a passage are the same formula restricted to a sub-tree --
 * readings, not steps of the calculation.
 *
 * The reason is that a cascade of means silently redistributes weights nobody set, as soon as
 * a sheet is missing -- and one is missing all the time. With elocution 2 (sounds 1, melody 1)
 * and correctness 1, over two passages the second of which had its gate closed -- passage 1:
 * sounds 40, melody 80, correctness 90; passage 2: correctness 50 -- a cascade makes passage 1
 * worth 70, passage 2 worth 50 since its mean renormalises over what is left, and the sitting
 * 60. Flat, (40 + 80 + 90 + 50) / 4 = 65. The gap is not rounding: in the cascade, correctness
 * ended up carrying two thirds of the sitting and elocution one third, the exact reverse of
 * the 2:1 asked for.
 *
 * **Between sheets the mean decides alone**: no floor capping the note the moment a counted
 * sheet drops under the bar. A good sheet can mask a bad one, and that is accepted -- in a
 * challenge few sheets count, and each one's sensitivity says how easy it is to hold.
 */
fun noteOver(passages: List<Passage>, weights: Weights, sensitivity: (Sheet) -> Int): Note? {
    var total = 0f
    var carried = 0f
    passages.forEach { passage ->
        passage.measured.forEach { measured ->
            val figure = measured.figure ?: return@forEach
            val length = lengthOf(measured.sheet, passage) ?: return@forEach
            val weight = weights.of(measured.sheet) * length
            if (weight <= 0f) return@forEach
            val sheet = measured.sheet
            total += weight * sheet.windowAt(sensitivity(sheet)).noteOf(figure).value
            carried += weight
        }
    }
    return if (carried > 0f) Note(total / carried) else null
}

/** What this sheet weighs by on this passage: the length, or the difficulty for `suivi`. */
private fun lengthOf(sheet: Sheet, passage: Passage): Float? =
    if (sheet === Sheets.of(FOLLOWING)) passage.difficulty else passage.keptWords.toFloat()

private const val FOLLOWING = "comprehension/suivi"
