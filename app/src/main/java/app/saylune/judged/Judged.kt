package app.saylune.judged

/**
 * What the language model marks on the learner's turn, and how the app reads it.
 *
 * **Three markings come from the model** -- the language spans, from which the app draws
 * correctness and relevance; the stumbling, from which it draws filler and restarts; and the
 * following of what was said (`activity.md`). Everything else is calculated: a spread
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
 * A marking the app cannot read, and which of the two ways it is unreadable.
 *
 * The two are told apart because they say different things to whoever reads the failure: a
 * notch outside the catalogue is a contract broken on a name, a bound off the words is a
 * contract broken on arithmetic. One message for both sent every such failure to the wrong
 * place to look.
 */
class Unreadable(val kind: Kind, message: String) : IllegalArgumentException(message) {
    enum class Kind { Notch, Bounds }
}

/**
 * The nearest word boundary [at] can be moved to without changing which letters are covered.
 *
 * Null when there is none, which is what fails the marking.
 *
 * [starts] picks the side: a span's `from` settles on the first character of a word, its `to`
 * one past the last. Only boundaries reachable across characters that are **neither letter
 * nor digit** are candidates, so the move can add or drop punctuation and whitespace and
 * nothing else. A run without letters holds no word, so no word can be gained or lost.
 */
private fun snap(text: String, words: List<IntRange>, at: Int, starts: Boolean): Int? =
    if (at !in 0..text.length) null
    else words
        .map { if (starts) it.first else it.last + 1 }
        .filter { edge ->
            text.substring(minOf(edge, at), maxOf(edge, at)).none { it.isLetterOrDigit() }
        }
        .minByOrNull { kotlin.math.abs(it - at) }

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
 * **The bounds must name whole words, and a bound is settled onto them when nothing but
 * punctuation and whitespace separates it from one.** They come from a model, so they can be
 * wrong, and a bound falling inside a word would slide the mark onto letters the judge never
 * named: that fails outright rather than rounding, because rounding would put a mark
 * somewhere plausible and nothing downstream could tell it apart from a mark the judge meant.
 *
 * Settling is not that rounding, and the difference is the whole reason it is allowed. A word
 * here is a run of non-whitespace, so punctuation travels with the word it touches -- which
 * makes two honest readings of "on a word boundary" wrong: stopping before the full stop of
 * `domain?`, and running on to the first letter of the next word. Both were measured coming
 * back from the model, in the same sitting, one each way (2026-09-07). Neither names a
 * different set of words: what [snap] may cross carries no letter and no digit, so the mark
 * lands on exactly the words the judge picked, and a bound that would gain or lose one letter
 * still fails.
 */
fun unfold(
    text: String,
    spans: List<Marked>,
    precedence: List<String>,
    fallback: String,
): List<Word> {
    val words = words(text)
    val settled = spans.map { span ->
        if (span.notch !in precedence) {
            throw Unreadable(Unreadable.Kind.Notch, "'${span.notch}' is not one of $precedence")
        }
        val from = snap(text, words, span.from, starts = true)
        val to = snap(text, words, span.to, starts = false)
        if (from == null || to == null || from >= to) {
            throw Unreadable(
                Unreadable.Kind.Bounds,
                "a span of ${span.from}..${span.to} does not name whole words of \"$text\"",
            )
        }
        span.copy(from = from, to = to)
    }
    return words.map { word ->
        val carried = settled
            .filter { it.from <= word.first && word.last < it.to }
            .map { it.notch }
        Word(word, carried.minByOrNull(precedence::indexOf) ?: fallback)
    }
}
