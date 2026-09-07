package app.speakup.marking

import app.speakup.analysis.AnalysedSound

/**
 * A silence between two words, and how long it was, in notches.
 *
 * [after] is the offset of the last character of the word it follows, and -1 for the silence
 * before the first word -- the one that sits in the blank column bordering the turn.
 */
data class Pause(val after: Int, val notches: Int)

/**
 * The pauses of a turn, read off where each sound was said.
 *
 * **A pause is a silence between two words**, and the word boundary does its own work: what
 * happens inside a word -- an occlusion, a hold, a stutter held -- is not a pause, and the
 * threshold throws out the micro-blanks that fall between words, the /t/ of *to stop* closing
 * the mouth for 50 to 150 ms in the blank before *stop*.
 *
 * **Three notches, at 0.2 / 0.5 / 3 seconds**, one point per notch. The first begins at the
 * threshold itself, so every pause counted carries at least one point. They serve two sheets
 * that do not look at the same range: the cut at 0.5 s separates a detachment, which continuity
 * counts, from a real pause, and the cut at 3 s keeps a three-second block distinct from a
 * five-second one, which is the whole trade of the longest silence. Past three points the image
 * saturates, the same renunciation the ramp makes and at the same place.
 *
 * **The closing silence is one of them**, on the same footing as the opening one: it is the
 * stretch between the last sound and the end of the recording, and it is counted in the same
 * notches. What it says is a real thing about a turn -- handing the floor back is an act, and
 * being slow to do it is what the fluency sheets already count at both edges (`Fluency`). It
 * needs [recorded], which the analysis pass reads on the same clock as the sounds; a turn
 * recorded before that figure was kept has none, and then the turn simply has no closing pause
 * rather than one of nothing.
 */
fun pausesOf(text: String, sounds: List<AnalysedSound>, recorded: Int? = null): List<Pause> {
    if (sounds.isEmpty()) return emptyList()
    val spoken = app.speakup.judged.words(text).mapNotNull { word ->
        val inside = sounds.filter { sound -> sound.at.any { it in word } }
        if (inside.isEmpty()) null
        else word to (inside.minOf { it.saidMs.first }..inside.maxOf { it.saidMs.last })
    }
    if (spoken.isEmpty()) return emptyList()
    val out = mutableListOf<Pause>()
    notchesOf(spoken.first().second.first).takeIf { it > 0 }?.let { out += Pause(-1, it) }
    spoken.zipWithNext { before, after ->
        val silence = after.second.first - before.second.last
        notchesOf(silence).takeIf { it > 0 }
            ?.let { out += Pause(before.first.last, it) }
    }
    recorded?.let { end ->
        val last = spoken.last()
        notchesOf(end - last.second.last).takeIf { it > 0 }
            ?.let { out += Pause(last.first.last, it) }
    }
    return out
}

/** How many points a silence of [ms] carries: none below the threshold, then one per notch. */
fun notchesOf(ms: Int): Int = NOTCHES.count { ms >= it }

/** The three notches, in milliseconds. Material for the calibration bench, like every value. */
private val NOTCHES = intArrayOf(200, 500, 3_000)
