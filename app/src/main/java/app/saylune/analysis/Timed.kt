package app.saylune.analysis

import app.saylune.fluency.Span
import app.saylune.fluency.Turn
import app.saylune.fluency.Word
import app.saylune.judged.Kept
import app.saylune.judged.Marked
import app.saylune.judged.unfold

/**
 * The turn as the fluency sheets read it: every word the mouth said, and where each of the
 * two recordings put it.
 *
 * **The two halves come from two places, and neither can give the other.** The seam works out
 * *where* a word sits -- that is a reading of audio and nothing else does it -- and the judge
 * says *what* it is, `kept` / `abandoned` / `filler`, which is a judgement of use and
 * not of sound: *um* has no other employment, but *I mean*, *like*, *well* are real words. So
 * this is composed here, above both, rather than inside either.
 *
 * The two sides are read off **two different montages, and that is the point.** The model's
 * bounds come from its own grid, which holds the kept words alone. The learner's come from
 * his **own free decoding**, which covers everything he said -- and it has to, twice over: it
 * is the only reading that reaches a hesitation at all, and the forced alignment cannot be
 * used for this even where it exists, since the audio of a hesitation has no slot in a grid
 * that never held it and gets swallowed into whichever kept word sits next to it. Reading a
 * word's length there would put the stumble's seconds inside a word the learner said cleanly,
 * which is the double count `rate` is written to avoid.
 */
fun Analysed.timed(stumbling: List<Marked>): Turn = Turn(
    // Unfolded here and against [marking]'s own text, which is the string every offset in
    // this object indexes into: the words this walks and the words the marks sit on are then
    // the same words by construction. A turn nothing judged unfolds to every word `kept`,
    // which is the reading a judge that said nothing leaves -- the same one [Kept.of] takes.
    spoken = unfold(marking.text, stumbling, Kept.STUMBLING, Kept.KEPT).mapNotNull { word ->
        // A word no sound of the learner's decoding landed on drops out rather than being
        // given a made-up stretch. Its audio then reads as silence, which is what the
        // recording actually offers when nothing can say where the word is in it -- and a
        // sheet is better short and honest than complete and invented.
        val said = span(freely.filter { it.word == word.at }.map { it.at })
            ?: return@mapNotNull null
        Word(
            said = said,
            // Null on a word the model never said, which is every hesitation: `rate` and
            // `continuity` then have nothing to compare it against, and absent is not nil.
            model = span(
                sounds.filter { !it.at.isEmpty() && it.at.first in word.at }.map { it.modelMs },
            ),
            notch = word.notch,
        )
    },
    recorded = recorded,
    rendered = rendered,
)

/** The stretch [of] covers end to end, or nothing at all when it is empty. */
private fun span(of: List<IntRange>): Span? =
    if (of.isEmpty()) null else Span(of.minOf { it.first }, of.maxOf { it.last })
