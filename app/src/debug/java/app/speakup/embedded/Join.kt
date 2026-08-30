package app.speakup.embedded

/**
 * One sound of the model's grid, and everything a mark needs of it.
 *
 * [spots] is what a mark is actually drawn on: offsets into the text. [borrowed] holds a
 * neighbour's letter when this sound holds none of its own. Both empty means the gutter --
 * a sound English writes with nothing, like the schwa of `doesn't`.
 */
data class Sound(
    val symbol: String,
    val word: String?,
    /**
     * The whole word in the text, silent letters and all -- not just the letters this sound
     * took. A verdict about the word is painted over every character of it, and the letters
     * a sound holds are by construction the ones that carry a sound.
     */
    val wordAt: IntRange?,
    val letters: String,
    val spots: List<Int>,
    val borrowed: List<Int>,
)

/**
 * Every sound the voice produced, and the letters spoken inside it.
 *
 * This is exigence 2 of the analysis -- anchor the measures to the text -- and it is
 * computed entirely on the device, from order and spelling alone. Nothing is asked of the
 * synthesis provider but a wav, which is what leaves every engine a candidate, the free ones
 * included.
 *
 * Words first: the sound sequence is partitioned between the words of the text, then each
 * word's letters are matched inside its own group. That is what keeps a letter from reaching
 * across a word boundary, and it asks the audio for nothing.
 */
object Join {

    /**
     * How many letters a single sound may be given at once. Four covers the longest
     * graphemes English writes -- `ough`, `eigh` -- and anything longer is two graphemes.
     */
    const val LONGEST = 4

    /**
     * What it costs to step over a sound and leave it with no letter at all.
     *
     * Without it the objective pays for letters and never for sounds, so nothing stops a
     * vowel from swallowing its neighbour's letters. A sound with no letter is a sound no
     * mark can be drawn on, which is exactly the defect to price. Read off the answer key,
     * where the value sits on a plateau broad enough not to be a knife edge.
     */
    const val HUNGER = 1.0

    private const val NONE = -1

    /** A word of the text: where it sits, and the offsets of the letters the table writes. */
    private class Spoken(val text: String, val at: IntRange, val positions: List<Int>)

    fun joined(symbols: List<String>, text: String, affinity: Affinity): List<Sound> {
        require(symbols.isNotEmpty()) { "free decoding produced no sound" }

        val words = spoken(text, affinity)
        val cuts = partition(words.map { word -> word.positions.map { text[it] } }, symbols, affinity)

        val covered = Array(symbols.size) { StringBuilder() }
        val spots = Array(symbols.size) { mutableListOf<Int>() }
        val held = arrayOfNulls<Spoken>(symbols.size)

        words.forEachIndexed { rank, word ->
            val start = cuts[rank]
            val stop = cuts[rank + 1]
            for (index in start until stop) held[index] = word
            val characters = word.positions.map { text[it] }
            val chosen = inner(characters, symbols.subList(start, stop), affinity).chosen
            word.positions.forEachIndexed { at, position ->
                val sound = chosen[at]
                if (sound != NONE) {
                    covered[start + sound].append(text[position])
                    spots[start + sound].add(position)
                }
            }
        }

        val borrowed = lent(symbols, held, covered, spots, text, affinity)
        return ordered(symbols.indices.map { index ->
            Sound(
                symbol = symbols[index],
                word = held[index]?.text,
                wordAt = held[index]?.at,
                letters = covered[index].toString(),
                spots = spots[index].toList(),
                borrowed = borrowed[index],
            )
        })
    }

    /**
     * The sounds, once it is established that they walk the text forward.
     *
     * Checked rather than assumed, and checked here rather than trusted downstream. Every
     * consumer reads the order off this list: `readoutRows` lays its lines on the text in it,
     * and [Added] takes a mark's place in the phrase from the word the sound before it sat
     * in. A single sound claiming a letter behind its predecessor puts one of those in the
     * wrong place, silently and only sometimes -- the kind of defect a person finds reading a
     * screen months later, never anything in here.
     *
     * Nothing legitimate produces it: [partition] hands out the words in text order and
     * [inner] never walks back inside a word. So a failure is a defect in this file, and it
     * says so rather than returning a list that reads plausibly.
     */
    private fun ordered(sounds: List<Sound>): List<Sound> {
        var behind = -1
        for (sound in sounds) {
            if (sound.spots.isEmpty()) continue
            check(sound.spots.min() > behind) {
                "the join walks back: /${sound.symbol}/ takes ${sound.spots} after letter $behind"
            }
            behind = sound.spots.max()
        }
        return sounds
    }

    /** The words of [text], each with the offsets of its writable letters. */
    private fun spoken(text: String, affinity: Affinity): List<Spoken> {
        val words = mutableListOf<Spoken>()
        Regex("\\S+").findAll(text).forEach { match ->
            val positions = match.range.filter { affinity.writes(text[it]) }
            if (positions.isNotEmpty()) words.add(Spoken(match.value, match.range, positions))
        }
        return words
    }

    private class Matched(val total: Double, val chosen: IntArray)

    /**
     * A word's letters onto a group of sounds, the assignment never going back.
     *
     * Letters are taken one to [LONGEST] at a time: a group the table names is read as the
     * single grapheme it is and paid for every letter it carries, so spelling `ch` as one
     * /k/ outweighs spelling `c` and `h` apart. A group too small to hold the letters is not
     * refused -- several letters sharing a sound is the ordinary case, and a letter split
     * across two would colour neither.
     */
    private fun inner(
        characters: List<Char>,
        symbols: List<String>,
        affinity: Affinity,
    ): Matched {
        val count = characters.size
        if (symbols.isEmpty()) return Matched(0.0, IntArray(count) { NONE })

        val best = Array(count + 1) { DoubleArray(symbols.size) { Double.NEGATIVE_INFINITY } }
        val backLength = Array(count + 1) { IntArray(symbols.size) }
        val backFrom = Array(count + 1) { IntArray(symbols.size) { NONE } }

        for (index in 0 until count) {
            if (index > 0 && best[index].all { it == Double.NEGATIVE_INFINITY }) continue

            // Stepping from one sound to a later one costs a hunger per sound skipped, so
            // the best predecessor maximises `best[sound] + HUNGER * sound` -- a running
            // maximum, the skipped stretch being the same for all of them.
            val reachedScore = DoubleArray(symbols.size)
            val reachedFrom = IntArray(symbols.size)
            var running = Double.NEGATIVE_INFINITY
            var argmax = 0
            for (sound in symbols.indices) {
                if (index > 0) {
                    if (sound > 0) {
                        val stepped = running - HUNGER * (sound - 1)
                        if (best[index][sound] > stepped) {
                            reachedScore[sound] = best[index][sound]
                            reachedFrom[sound] = sound
                        } else {
                            reachedScore[sound] = stepped
                            reachedFrom[sound] = argmax
                        }
                    } else {
                        reachedScore[0] = best[index][0]
                        reachedFrom[0] = 0
                    }
                    val candidate = best[index][sound] + HUNGER * sound
                    if (candidate > running) {
                        running = candidate
                        argmax = sound
                    }
                } else {
                    reachedScore[sound] = -HUNGER * sound
                    reachedFrom[sound] = NONE
                }
            }

            for (length in 1..minOf(LONGEST, count - index)) {
                val run = characters.subList(index, index + length)
                    .joinToString("").lowercase()
                val weights = affinity.weights(run) ?: continue
                for (sound in symbols.indices) {
                    val score = reachedScore[sound] +
                        length * (weights[symbols[sound]] ?: 0) / 3.0
                    if (score > best[index + length][sound]) {
                        best[index + length][sound] = score
                        backLength[index + length][sound] = length
                        backFrom[index + length][sound] = reachedFrom[sound]
                    }
                }
            }
        }

        var last = 0
        var total = Double.NEGATIVE_INFINITY
        for (sound in symbols.indices) {
            val value = best[count][sound] - HUNGER * (symbols.size - sound - 1)
            if (value > total) {
                total = value
                last = sound
            }
        }
        if (total == Double.NEGATIVE_INFINITY) {
            // Not one letter of the word is in the table: it takes no sound rather than
            // taking them all for nothing.
            return Matched(0.0, IntArray(count) { NONE })
        }

        val chosen = IntArray(count) { NONE }
        val worth = BooleanArray(count)
        var index = count
        while (index > 0) {
            val length = backLength[index][last]
            val run = characters.subList(index - length, index).joinToString("").lowercase()
            val weights = affinity.weights(run)
            for (position in index - length until index) {
                chosen[position] = last
                worth[position] = (weights?.get(symbols[last]) ?: 0) != 0
            }
            val from = backFrom[index][last]
            index -= length
            last = if (from == NONE) 0 else from
        }
        return Matched(total, trimmed(chosen, worth))
    }

    /**
     * Each sound's letters cut back to the ones it is paid for, at the ends.
     *
     * A letter worth nothing on the sound it landed on was posted there by the order of the
     * walk and by nothing else, so where it sits says nothing: the silent `t` of `listen` is
     * worth zero on every sound that word offers. It holds nothing instead. What saves the
     * silent letters that do belong is the group table.
     *
     * Only at the ends: a letter worth nothing in the middle of a run is held inside a
     * spelling, and dropping it would leave the sound two letters with a hole between them
     * -- `take` painted as `a`..`e`.
     */
    private fun trimmed(chosen: IntArray, worth: BooleanArray): IntArray {
        val kept = chosen.copyOf()
        for (sound in chosen.toSortedSet()) {
            if (sound == NONE) continue
            val run = chosen.indices.filter { chosen[it] == sound }
            var low = 0
            var high = run.size - 1
            while (low <= high && !worth[run[low]]) {
                kept[run[low]] = NONE
                low++
            }
            while (high > low && !worth[run[high]]) {
                kept[run[high]] = NONE
                high--
            }
        }
        return kept
    }

    /**
     * Where each word's sounds start and stop, the cut chosen by spelling.
     *
     * A word may be given no sound at all -- an elided function word is real, and forcing
     * one on it would steal it from a neighbour.
     */
    private fun partition(
        words: List<List<Char>>,
        symbols: List<String>,
        affinity: Affinity,
    ): IntArray {
        val count = symbols.size
        val cost = Array(words.size) { rank ->
            Array(count + 1) { start ->
                DoubleArray(count + 1) { stop ->
                    if (stop < start) 0.0
                    else inner(words[rank], symbols.subList(start, stop), affinity).total
                }
            }
        }

        val best = Array(words.size + 1) { DoubleArray(count + 1) { Double.NEGATIVE_INFINITY } }
        val back = Array(words.size + 1) { IntArray(count + 1) }
        best[0][0] = 0.0
        for (rank in words.indices) {
            for (start in 0..count) {
                if (best[rank][start] == Double.NEGATIVE_INFINITY) continue
                for (stop in start..count) {
                    val score = best[rank][start] + cost[rank][start][stop]
                    if (score > best[rank + 1][stop]) {
                        best[rank + 1][stop] = score
                        back[rank + 1][stop] = start
                    }
                }
            }
        }

        val cuts = IntArray(words.size + 1)
        cuts[words.size] = count
        for (rank in words.size downTo 1) cuts[rank - 1] = back[rank][cuts[rank]]
        return cuts
    }

    /**
     * For a sound holding no letter, the letter it would light anyway.
     *
     * A letter lands on one sound and no more, which is what keeps the match honest -- but
     * that is a rule of the match, not of the screen. `x` writes the /k/ of `boxes` and then
     * its /s/; one of the two gets the letter, and the other would have nothing to colour
     * although the very same `x` is where it is written. So the sound borrows rather than
     * holds: two sounds may light one letter, neither owns it.
     *
     * Only a sound that touches it can lend, and only inside the same word -- a letter
     * fetched from across the word would mark somewhere the sound is not.
     */
    private fun lent(
        symbols: List<String>,
        held: Array<Spoken?>,
        covered: Array<StringBuilder>,
        spots: Array<MutableList<Int>>,
        text: String,
        affinity: Affinity,
    ): List<List<Int>> = symbols.indices.map { index ->
        val word = held[index]
        if (covered[index].isNotBlank() || word == null) return@map emptyList()
        var best = NONE
        var score = 0
        for (other in listOf(index - 1, index + 1)) {
            if (other !in symbols.indices) continue
            // Identity, not spelling: `that that` is two words, and a letter must not be
            // lent across the boundary between them.
            if (held[other] !== word || covered[other].isBlank()) continue
            for (position in spots[other]) {
                val weight = affinity.paid(text[position], symbols[index])
                if (weight > score) {
                    best = position
                    score = weight
                }
            }
        }
        if (best == NONE) emptyList() else listOf(best)
    }
}
