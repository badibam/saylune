package app.saylune.marking

/**
 * One line of a readout: a stretch of the turn's text, and the sound that claims it.
 *
 * [of] is null for a stretch **no sound claims** -- silent letters, spaces, punctuation.
 * Those lines carry no verdict and exist for one reason: without them the readout is a list
 * of sounds rather than the sentence, and there is no way to find where in the turn a line
 * sits. A reader has to be able to read the phrase down the column.
 */
data class ReadoutRow<T>(val at: IntRange, val of: T?)

/**
 * Every sound in turn, with the text nobody claimed filled in between them.
 *
 * The invariant, which `ReadoutRowsTest` holds it to: **concatenating the stretches of every
 * row reproduces the text, character for character.** A soft rule would let a character go
 * missing without anyone noticing; this one fails a test.
 *
 * A sound that owns no letters -- the gutter, or one borrowing a neighbour's -- keeps its
 * place in the order and claims nothing, so it neither hides text nor duplicates it. Its
 * range is empty and sits where the sound does.
 *
 * Sounds arrive in grid order, which is text order: the join walks the words in order and
 * never goes back inside one, so no sorting is needed and none is done -- sorting here would
 * quietly paper over a join that had gone out of order.
 *
 * **A stretch nobody claims is cut at the space**, so the blank between two words is always a
 * row of its own. Left whole, a stretch like `"e "` -- a silent letter and the space after it --
 * drew one row of letters and swallowed the break, so the next word's first sound came directly
 * under it and the phrase read as one long word down the column. The screen draws a blank row
 * for a blank stretch, and it can only do that if the blank is a stretch.
 */
fun <T> readoutRows(text: String, sounds: List<T>, at: (T) -> IntRange): List<ReadoutRow<T>> {
    val rows = mutableListOf<ReadoutRow<T>>()
    var cursor = 0
    for (sound in sounds) {
        val span = at(sound)
        if (span.isEmpty()) {
            rows.add(ReadoutRow(cursor until cursor, sound))
            continue
        }
        if (span.first > cursor) rows.unclaimed(text, cursor, span.first)
        rows.add(ReadoutRow(span.first..span.last, sound))
        cursor = span.last + 1
    }
    if (cursor < text.length) rows.unclaimed(text, cursor, text.length)
    return rows
}

/** The text between two sounds, cut into its runs of blank and of ink. */
private fun <T> MutableList<ReadoutRow<T>>.unclaimed(text: String, from: Int, to: Int) {
    var start = from
    while (start < to) {
        val blank = text[start].isWhitespace()
        var end = start
        while (end < to && text[end].isWhitespace() == blank) end++
        add(ReadoutRow(start until end, null))
        start = end
    }
}
