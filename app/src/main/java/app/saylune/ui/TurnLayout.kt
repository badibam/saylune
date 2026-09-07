package app.saylune.ui

import app.saylune.judged.Word

/**
 * The marked turn, laid out on the grid, with no screen anywhere in it.
 *
 * **The grid is horizontal** (`ui.md`), so laying a turn out is arithmetic on columns
 * rather than text measurement: in a pixel font at a whole scale every advance is the same
 * whole number of pixels, so a word starts at its column times the cell and a mark covers a
 * whole number of cells. That is what took a class of rounding out of the drawing, and it is
 * why this file has no `Density`, no `TextMeasurer` and no `Canvas` -- it can be proved
 * without a device.
 */

/**
 * One word as the screen draws it.
 *
 * [text] is what is drawn and [at] is where it sits in the turn's text, and the two differ for
 * a fragment set aside from the sentence: it is written **in dimmed ink between brackets**,
 * `[um]`, and the brackets are real characters in their own cell at letter height -- which is
 * what tells them from the painted enclosure of relevance, which overflows in height.
 */
data class Token(
    val text: String,
    val at: IntRange,
    /** Set aside from the sentence: filler, or a piece of an abandoned start. */
    val aside: Boolean,
) {
    /** How many cells it takes. */
    val width: Int get() = text.length
}

/** A token and the column it starts at on its line. */
data class Placed(val token: Token, val column: Int) {
    val after: Int get() = column + token.width
}

/**
 * The words of [text], each carrying whether the judge set it aside.
 *
 * The notches come from the stumbling, whose fallback is `kept`: what is not kept is filler or
 * a piece of an abandoned start, and the screen draws both the same way, since what it says is
 * the one thing they share -- this is not in the sentence.
 */
fun tokensOf(text: String, stumbling: List<Word>): List<Token> = stumbling.map { word ->
    val said = text.substring(word.at.first, word.at.last + 1)
    val aside = word.notch != KEPT
    Token(if (aside) "[$said]" else said, word.at, aside)
}

/**
 * The tokens wrapped into lines of at most [columns] cells, one blank between two words.
 *
 * A word longer than the line goes on a line of its own and overflows rather than being cut:
 * the marks are anchored to its characters, and cutting a word would put half of a mark on
 * each line with nothing saying they are one.
 */
fun wrap(tokens: List<Token>, columns: Int): List<List<Placed>> {
    if (columns <= 0) return emptyList()
    val lines = mutableListOf<List<Placed>>()
    var line = mutableListOf<Placed>()
    var column = 0
    tokens.forEach { token ->
        val needs = if (line.isEmpty()) token.width else token.width + 1
        if (line.isNotEmpty() && column + needs > columns) {
            lines += line
            line = mutableListOf()
            column = 0
        }
        if (line.isNotEmpty()) column += 1
        line += Placed(token, column)
        column += token.width
    }
    if (line.isNotEmpty()) lines += line
    return lines
}

/**
 * What a span of the turn's text covers on one line: its columns, and whether the group's own
 * ends are on this line.
 *
 * **A group carries its identity, not its label** (`ui.md`). Two neighbouring groups can
 * deserve the same label without being the same group, so a run of like-labelled words is not
 * a group; the span is. And **a group cut by a line break opens on one side and closes on the
 * other** rather than doubling into two complete enclosures, which is what [opens] and
 * [closes] say.
 */
data class Reach(val column: Int, val width: Int, val opens: Boolean, val closes: Boolean)

/** Where the span `[from, to)` of the turn's text falls on [line], or null when it does not. */
fun reachOf(line: List<Placed>, from: Int, to: Int): Reach? {
    val inside = line.filter { it.token.at.first < to && it.token.at.last + 1 > from }
    if (inside.isEmpty()) return null
    val first = inside.first()
    val last = inside.last()
    return Reach(
        column = first.column,
        width = last.after - first.column,
        // The group's own end is on this line when no earlier word of it was left behind.
        opens = first.token.at.first <= from,
        closes = last.token.at.last + 1 >= to,
    )
}

/**
 * The stretches of one line the melody covers: the runs of words that are in the sentence.
 *
 * **The curve breaks only at a set-aside fragment**, where the comparison does not exist; at a
 * pause it stays in one piece, the points displacing nothing.
 */
fun voiced(line: List<Placed>): List<Reach> {
    val out = mutableListOf<Reach>()
    var run = mutableListOf<Placed>()
    fun close() {
        if (run.isEmpty()) return
        out += Reach(run.first().column, run.last().after - run.first().column, true, true)
        run = mutableListOf()
    }
    line.forEach { placed ->
        if (placed.token.aside) close() else run += placed
    }
    close()
    return out
}

/** What the stumbling calls a word that is in the sentence. */
private const val KEPT = "kept"
