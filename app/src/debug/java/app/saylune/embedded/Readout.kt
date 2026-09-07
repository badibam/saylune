package app.saylune.embedded

import app.saylune.marking.AddedSound
import app.saylune.marking.PhonemeDeviation
import app.saylune.marking.readoutRows
import java.util.Locale

/**
 * The analysis of one turn, written out to be read.
 *
 * Two levels, because one number per sound is not enough to trust and forty-two are not
 * readable. The table gives every sound a line; the spreads give each one the shapes the
 * measure actually compared -- `R .90 / W .10` and `R .90 / ER .10` share a peak and do not
 * say the same thing, and no divergence quoted alone will ever show that.
 *
 * The table holds every character of the turn, sound or no sound: a stretch no sound claims
 * gets a line with a dash, so the phrase can be read down the column and a line can be
 * placed in it. A list of sounds alone gives no way to tell where in the turn one sits.
 *
 * A stretch the learner said that belongs to no word gets a line in its place, dashed on the
 * model's side because nothing was there. No points and no duration: it has one side only.
 *
 * The durations are the widened spans, not the raw ones: the network is peaky, so every raw
 * span is a frame or two and a column of `0.02` everywhere says nothing -- least of all the
 * absurd duration that is the only way a degenerate alignment gives itself away. Nothing in
 * the measure reads them.
 *
 * The `model -> you` column reads like a verdict and is not one, which the heading says
 * outright. Naming the sound produced is the least reliable thing an acoustic machine
 * renders and the app depends on none of it: the mark is born of the gap between two whole
 * shapes (`docs/reference.md`).
 */
object Readout {

    private const val BAR = 12

    /** One line per sound: what each side leans to, the gap, the letters, the durations. */
    fun table(
        text: String,
        gaps: List<Overlap.Gap>,
        sounds: List<Sound>,
        phonemes: List<PhonemeDeviation>,
        added: List<AddedSound>,
        grid: Int,
        dropped: Int,
        secondsPerFrame: Float,
        band: Float,
    ): String {
        val out = StringBuilder()
        val at = Overlap.widened(gaps.map { it.at })
        val span = Overlap.widened(gaps.map { it.span })
        out.append(text).append('\n')
        out.append(" #  model -> you    pts   letters   dur m/y\n")
        out.append("  (the two peaks are a hint, never the verdict)\n")
        out.append("-".repeat(46)).append('\n')
        val pending = added.sortedBy { it.after }.toMutableList()
        readoutRows(text, gaps.indices.toList()) { index ->
            sounds[gaps[index].rank].spots.let { spots ->
                if (spots.isEmpty()) 0 until 0 else spots.min()..spots.max()
            }
        }.forEach { row ->
            val index = row.of
            if (index == null) {
                // Columns of the sound line, all dashed but the letters: nothing was read
                // here, and a blank would read as a zero. Quoted, because a stretch of
                // text nobody claimed is often nothing but a space, and an unquoted space
                // is an empty line that looks like a bug.
                out.append("    %-5s   %-5s %5s  %-9s\n".format(
                    Locale.ROOT, "-", "-", "-",
                    "\"" + text.substring(row.at.first, row.at.last + 1) + "\""))
                return@forEach
            }
            val gap = gaps[index]
            val sound = sounds[gap.rank]
            // Before the first sound claiming a character past it, which is the rule the
            // screen draws by. A sound holding none is transparent to the test and keeps its
            // own place, so the table shows it without moving anything.
            sound.spots.maxOrNull()?.let { claims ->
                while (pending.isNotEmpty() && pending.first().after < claims) {
                    inserted(out, pending.removeAt(0))
                }
            }
            val heard = gap.said.firstOrNull()?.symbol ?: "?"
            val letters = sound.letters.ifEmpty {
                if (sound.borrowed.isEmpty()) "(gutter)"
                else "(" + sound.borrowed.joinToString("") { text[it].toString() } + ")"
            }
            out.append(
                "%2d  %-5s-> %-5s %5.1f  %-9s %.2f/%.2f%s\n".format(
                    Locale.ROOT,
                    gap.rank,
                    gap.symbol,
                    heard,
                    gap.value * Marks.POINTS,
                    letters,
                    (at[index].last + 1 - at[index].first) * secondsPerFrame,
                    (span[index].last + 1 - span[index].first) * secondsPerFrame,
                    if (gap.value * Marks.POINTS > band) "  <<" else "",
                )
            )
        }
        pending.forEach { inserted(out, it) }
        out.append("-".repeat(46)).append('\n')
        val painted = phonemes.count { it.points > band }
        out.append(
            "$grid sounds in the grid, ${gaps.size} compared, $dropped dropped\n" +
                "$painted of ${phonemes.size} over $band points  << marks one\n"
        )
        return out.toString()
    }

    /**
     * One added stretch, written in the seam it belongs to.
     *
     * Dashes where the model's columns would be, because there was nothing there -- and no
     * points, since added matter has no second side to be compared to. The letters column
     * says `seam`: the stretch belongs to no word, so it sits between two letters and there
     * is no letter of its own to quote.
     */
    private fun inserted(out: StringBuilder, one: AddedSound) {
        out.append("    %-5s   %-5s %5s  %-9s\n".format(
            Locale.ROOT, "-", one.symbol, "add", "^ seam"))
    }

    /** For each sound, the two spreads side by side -- what was really compared. */
    fun spreads(gaps: List<Overlap.Gap>, sounds: List<Sound>, secondsPerFrame: Float): String {
        val out = StringBuilder()
        val at = Overlap.widened(gaps.map { it.at })
        val span = Overlap.widened(gaps.map { it.span })
        gaps.forEachIndexed { index, gap ->
            val sound = sounds[gap.rank]
            out.append(
                "%s  %.1f pts  \"%s\"  model %.2f-%.2fs  you %.2f-%.2fs\n".format(
                    Locale.ROOT,
                    gap.symbol,
                    gap.value * Marks.POINTS,
                    sound.letters,
                    at[index].first * secondsPerFrame,
                    (at[index].last + 1) * secondsPerFrame,
                    span[index].first * secondsPerFrame,
                    (span[index].last + 1) * secondsPerFrame,
                )
            )
            out.append("    model                 you\n")
            for (rank in 0 until Overlap.SHARES) {
                out.append("    ")
                out.append(share(gap.model.getOrNull(rank)))
                out.append("  ")
                out.append(share(gap.said.getOrNull(rank)))
                out.append('\n')
            }
            out.append("    rest            %.2f  rest            %.2f\n\n".format(
                Locale.ROOT, rest(gap.model), rest(gap.said)))
        }
        return out.toString()
    }

    private fun share(share: Overlap.Share?): String =
        if (share == null) " ".repeat(22)
        else "%-4s%-12s%.2f".format(
            Locale.ROOT, share.symbol, "#".repeat((share.part * BAR).toInt()), share.part)

    /** What the kept shares leave out, so the column is honest about being a top few. */
    private fun rest(shares: List<Overlap.Share>) =
        (1f - shares.sumOf { it.part.toDouble() }).coerceAtLeast(0.0)
}
