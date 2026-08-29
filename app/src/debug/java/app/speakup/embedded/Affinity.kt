package app.speakup.embedded

import org.json.JSONObject
import java.io.File

/**
 * Which sounds a letter takes part in writing, weighted 0 to 3.
 *
 * The only outside file in the whole chain besides the weights, and the distinction that
 * lets it in is the one the project rests on (`docs/reference.md`): a norm that says what is
 * *correct* is refused; a table that says where to *paint* is not. It is never asked how a
 * word is said. It answers whether `s` takes part in /ʃ/, which is the whole of what decides
 * here -- and without it the mark lands wrong nearly two times in five.
 *
 * Two tables, not one. The per-letter table cannot say that `sh` writes /ʃ/ between them; it
 * can only say `s` and `h` each take part in it, which leaves `sch` free to hand its `c` to
 * the /s/. The group table keeps the letters that belong -- `kn` on /n/, `mb` on /m/, `wr`
 * on /ɹ/, `dge` on /ʤ/ -- and it only earns its place once a letter is allowed to hold
 * nothing at all.
 */
class Affinity(
    private val letters: Map<String, Map<String, Int>>,
    private val groups: Map<String, Map<String, Int>>,
) {

    /** What a run of letters is paid for each sound, or null when the table has no such run. */
    fun weights(run: String): Map<String, Int>? =
        if (run.length > 1) groups[run] else letters[run]

    /** What one letter is paid for one sound. */
    fun paid(letter: Char, symbol: String): Int =
        letters[letter.lowercase()]?.get(symbol) ?: 0

    /** Whether the table can write this character at all. */
    fun writes(character: Char): Boolean = letter(character) in letters

    private fun letter(character: Char) = character.lowercase()

    /**
     * The characters of [text] that are spoken and that the table cannot write.
     *
     * Punctuation is not among them: a comma is silent, and a silent character asks nothing
     * of the join. A digit does -- `25` is two characters and four syllables.
     */
    fun unspellable(text: String): List<Char> =
        text.filter { it.isLetterOrDigit() && !writes(it) }.toSortedSet().toList()

    /**
     * The sounds the table names that this alphabet does not render.
     *
     * A table written for another alphabet does not fail: every lookup misses, the affinity
     * falls to zero everywhere, and the join returns whatever order alone can do, with
     * nothing to say so. So it is checked rather than trusted.
     */
    fun unknownTo(alphabet: Alphabet): List<String> {
        val rendered = (0 until alphabet.size).map { alphabet[it] }.toSet()
        return (letters.values + groups.values)
            .flatMap { it.keys }
            .toSortedSet()
            .filterNot { it in rendered }
    }

    companion object {
        fun read(letters: File, groups: File) =
            Affinity(table(letters), table(groups))

        private fun table(file: File): Map<String, Map<String, Int>> {
            val json = JSONObject(file.readText())
            return json.keys().asSequence().associateWith { run ->
                val row = json.getJSONObject(run)
                row.keys().asSequence().associateWith { row.getInt(it) }
            }
        }
    }
}
