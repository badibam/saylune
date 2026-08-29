package app.speakup.embedded

import org.json.JSONObject
import java.io.File

/**
 * What each column of the matrix stands for, read from the model's own vocabulary.
 *
 * From the vocabulary file rather than from the network: the table has to be the same on
 * the bench and on the device, and a column read as another sound would not fail, it would
 * measure the wrong thing.
 */
class Alphabet(private val table: List<String>) {

    val size: Int get() = table.size

    operator fun get(index: Int): String = table[index]

    /** The column meaning "nothing is being pronounced here". */
    val blank: Int = PAD.firstNotNullOfOrNull { name ->
        table.indexOf(name).takeIf { it >= 0 }
    } ?: throw IllegalArgumentException("no blank symbol among $PAD in this vocabulary")

    /** The columns that stand for a sound: silence and notation excluded. */
    val spoken: IntArray = table.indices
        .filter { it != blank && table[it] !in NOT_A_SOUND }
        .toIntArray()

    /** Everything the grid must not turn into a sound, blank included. */
    val notSounds: Set<Int> = (table.indices - spoken.toSet()).toSet()

    companion object {
        /**
         * A frame classifier names silence outright and keeps a padding token beside it
         * that means nothing acoustic, so the explicit silence is tried first.
         */
        private val PAD = listOf("[SIL]", "<pad>", "[PAD]")

        /**
         * Not sounds: a word separator, and the several names annotators give to silence.
         * Letting any of them into the grid would set the comparison to work on emptiness,
         * and make the alignment thread through a boundary as if it had been spoken.
         */
        private val NOT_A_SOUND = setOf("|", "h#", "pau", "epi", " ")

        /** `vocab.json` maps a token to its column; the table is that, turned around. */
        fun read(vocab: File): Alphabet {
            val json = JSONObject(vocab.readText())
            val table = arrayOfNulls<String>(json.length())
            for (token in json.keys()) {
                val index = json.getInt(token)
                require(index in table.indices) { "$token sits at $index, outside the table" }
                table[index] = token
            }
            return Alphabet(table.mapIndexed { index, name ->
                name ?: throw IllegalArgumentException("column $index has no symbol")
            })
        }
    }
}
