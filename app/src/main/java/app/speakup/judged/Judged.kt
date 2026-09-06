package app.speakup.judged

/**
 * What the language model marks on the learner's turn, and how the app reads it.
 *
 * **Three markings come from the model** -- the language spans, from which the app draws
 * correctness and relevance; the stumbling, from which it draws filler and restarts; and the
 * following of what was said (`activity-model.md`). Everything else is calculated: a spread
 * gap, a silence, a rate, a ratio.
 *
 * **The judge returns a notch, never a percentage.** "72% following" is checkable by nobody,
 * and the fineness comes from counting, as it does for the sounds: a passage is finely scored
 * by aggregating many coarse judgements. The cost is owned: a three-word passage carrying one
 * marked group has a very coarse note. That is a true coarseness, not a false precision.
 *
 * Everything here indexes [Judgement.intended], which is the string the screen shows and the
 * one every other mark of the project already indexes into.
 */
data class Judgement(
    /**
     * The learner's own turn, written out, hesitations included.
     *
     * It is a repair of the **transcript** and never of the grammar: a wrong tense, a missing
     * article, a clumsy turn must survive here exactly as they were said, the whole of the
     * words' gate being downstream of it.
     */
    val intended: String,
    val spans: List<Span>,
    val stumbling: List<Marked>,
    /** One notch for the whole passage: what the answer proves it took in. */
    val following: String,
    /** What the model says of the turn it just wrote. Not a measure of the learner. */
    val difficulty: String,
)

/**
 * One group of words, by **character bounds in `intended`**, carrying one notch per scale.
 *
 * Bounds and not the faulty substring, and that is a decision worth finding again: a substring
 * is ambiguous the moment it appears twice in the turn, and every mark in the project already
 * indexes into the displayed text. The check that goes with it fails outright when the bounds
 * do not land on word boundaries.
 *
 * [from] is inclusive, [to] is exclusive.
 */
data class Span(
    val from: Int,
    val to: Int,
    /** `ne-se-dit-pas`, `mal-forme` or `ok`. **Absolute**: no instruction moves them. */
    val correctness: String,
    /** `a-cote`, `flat`, `ok` or `apt`. **Situational**, and an instruction shapes them. */
    val relevance: String,
)

/** One stretch of `intended` at one notch. [from] inclusive, [to] exclusive. */
data class Marked(val from: Int, val to: Int, val notch: String)

/** One word of `intended`, and the notch it ends up carrying on one scale. */
data class Word(val at: IntRange, val notch: String)

/**
 * The words of [text], as the ranges a mark can land on.
 *
 * A word is a run of non-whitespace. Punctuation travels with the word it touches, which is
 * what makes a bound falling after a comma land on a word boundary rather than inside one.
 */
fun words(text: String): List<IntRange> {
    val out = mutableListOf<IntRange>()
    var at = 0
    while (at < text.length) {
        while (at < text.length && text[at].isWhitespace()) at++
        if (at >= text.length) break
        val start = at
        while (at < text.length && !text[at].isWhitespace()) at++
        out += start until at
    }
    return out
}

/**
 * Every word of [text] with the notch it carries, once [spans] are unfolded onto it.
 *
 * Three rules, and all three come from the doc.
 *
 * **A word carries at most one notch per scale.** Two overlapping spans do not count twice: a
 * word takes the **worst** notch it carries, worst being first in [precedence].
 *
 * **A word carrying nothing takes [fallback]**, which is a notch like any other and never an
 * absence -- `ok` for the two language scales, `kept` for the stumbling.
 *
 * **The bounds must land on word boundaries.** They come from a model, so they can be wrong,
 * and a bound falling inside a word would slide the mark onto letters the judge never named.
 * It fails outright rather than rounding: rounding would put a mark somewhere plausible and
 * nothing downstream could tell it apart from a mark the judge meant.
 */
fun unfold(
    text: String,
    spans: List<Marked>,
    precedence: List<String>,
    fallback: String,
): List<Word> {
    val words = words(text)
    spans.forEach { span ->
        require(span.notch in precedence) {
            "'${span.notch}' is not one of $precedence"
        }
        require(words.any { it.first == span.from } && words.any { it.last + 1 == span.to }) {
            "a span of ${span.from}..${span.to} does not land on word boundaries of \"$text\""
        }
    }
    return words.map { word ->
        val carried = spans
            .filter { it.from <= word.first && word.last < it.to }
            .map { it.notch }
        Word(word, carried.minByOrNull(precedence::indexOf) ?: fallback)
    }
}
