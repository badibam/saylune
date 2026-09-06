package app.speakup.embedded

import app.speakup.judged.Kept
import app.speakup.judged.Marked
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The marks stay on their letters when the turn stumbles.
 *
 * The model says the kept words alone, so its grid is joined to a text the screen never
 * shows; the learner is joined to everything he said. Two frames of reference, and the
 * carrying is what puts the first back into the second. Get it wrong -- or do it late, after
 * something has already set the two joins against each other -- and every mark of the turn
 * slides by however many characters the hesitations take up, silently: the letters would
 * still be coloured, just not the ones that were read.
 *
 * The sounds are typed out rather than decoded, as in [ReducedTest]: this tests the join and
 * the carrying, not the engine, so it needs no weights and runs on any machine.
 */
class CarriedTest {

    private val affinity = Affinity.read(
        File("src/debug/assets/affinity.json"),
        File("src/debug/assets/affinity-groups.json"),
        File("src/debug/assets/affinity-reductions.json"),
    )

    /** "It was, like, um, I went to the— I was going to the store", as the screen shows it. */
    private val stumbled = "It was like um I went to the I was going to the store"

    private val hesitations = listOf(
        Marked(7, 11, "filler"),   // like
        Marked(12, 14, "filler"),  // um
        Marked(15, 28, "abandoned"),    // I went to the
    )

    /** What the model was made to say: "It was I was going to the store". */
    private val sounds = listOf(
        "ɪ", "t", "w", "ʌ", "z",                       // It was
        "aɪ", "w", "ʌ", "z",                           // I was
        "g", "oʊ", "ɪ", "ŋ",                           // going
        "t", "ə", "ð", "ə",                            // to the
        "s", "t", "ɔ", "ɹ",                            // store
    )

    private fun joined(kept: Kept) =
        Join.joined(sounds, kept.text, affinity).inWhole(kept)

    /**
     * The property the whole carrying exists for, said as the screen would read it: whatever
     * a sound was given, the characters it claims in the whole turn spell it.
     */
    @Test
    fun `every sound claims the letters it was read on, in the string the screen shows`() {
        val kept = Kept.of(stumbled, hesitations)
        joined(kept).filter { it.spots.isNotEmpty() }.forEach { sound ->
            assertEquals(
                "${sound.symbol} claims ${sound.spots}",
                sound.letters,
                sound.spots.sorted().map { stumbled[it] }.joinToString(""),
            )
        }
    }

    /** And no sound of the model's ever lands inside a hesitation, which it never said. */
    @Test
    fun `nothing the model said lands on a word it was never given`() {
        val kept = Kept.of(stumbled, hesitations)
        val hesitated = hesitations.flatMap { it.from until it.to }.toSet()
        joined(kept).forEach { sound ->
            (sound.spots + sound.borrowed).forEach {
                assertEquals("${sound.symbol} at $it", false, it in hesitated)
            }
        }
    }

    /**
     * A word verdict is painted over the whole word, so the word a sound belongs to has to
     * come across as a word of the displayed string and not as a range that straddles two.
     */
    @Test
    fun `the word a sound belongs to is a word of the whole turn`() {
        val kept = Kept.of(stumbled, hesitations)
        val words = app.speakup.judged.words(stumbled).toSet()
        joined(kept).mapNotNull { it.wordAt }.distinct().forEach {
            assertEquals("$it is no word of the turn", true, it in words)
        }
    }

    /** A clean turn carries nothing, and the join comes back exactly as it went in. */
    @Test
    fun `a turn with no hesitation is joined and carried to the same thing`() {
        val clean = "It was I was going to the store"
        val kept = Kept.of(clean, emptyList())
        assertEquals(Join.joined(sounds, clean, affinity), joined(kept))
    }
}
