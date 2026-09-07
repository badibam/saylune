package app.saylune.notes

/**
 * A note is a number from 0 to 1; the letter is how it is shown.
 *
 * **One scale, A-E**, for everything that is scored at any level, so there is never more than
 * one to remember. The letters cut the scale into **equal fifths**, which is what puts the
 * A/B bar at 0,60 by construction rather than by measurement.
 *
 * Averaging letters was tried and does not hold: two passages at 8% and 1% of missed sounds
 * both fall in A and would render the same note, and a tenth of a percent more would swing a
 * passage's note by half a notch -- flat shelves separated by cliffs. It is also what makes
 * this doc's own sentence hold, that **it is the sensitivity that makes sheets comparable**:
 * turning "8% of sounds missed" and "3 semitones out" into one yardstick takes better than
 * five values.
 *
 * **Numbers and notches are what is kept in the store, never letters**, so the bounds can move
 * without spoiling old sittings.
 */
@JvmInline
value class Note(val value: Float) {
    init { require(value in 0f..1f) { "a note outside 0..1: $value" } }

    val letter: Letter get() = Letter.of(value)

    /**
     * The quarter of its band, which is **display only**: nothing stores it, and a condition
     * names a letter and never a `B+`. Whoever wants finer reads the sheet's figure, which is
     * made for that.
     */
    val modifier: Modifier
        get() = when (((value - letter.floor) / BAND).coerceIn(0f, 0.999f)) {
            in 0f..0.25f -> Modifier.Minus
            in 0.75f..1f -> Modifier.Plus
            else -> Modifier.None
        }

    /** Does it clear the one bar the project has, the same one everywhere. */
    val passes: Boolean get() = value >= BAR

    override fun toString(): String = "${letter.name}${modifier.sign}"
}

enum class Letter(val floor: Float) {
    E(0.00f), D(0.20f), C(0.40f), B(0.60f), A(0.80f);

    companion object {
        fun of(value: Float): Letter = entries.last { value >= it.floor }
    }
}

enum class Modifier(val sign: String) { Minus("-"), None(""), Plus("+") }

/** Five letters over the 0..1 scale. */
const val BAND = 0.20f

/**
 * **A single bar, the same everywhere: A or B is fine.**
 *
 * On a passage it decides whether it has to be said again; on an activity, whether it is a
 * pass and whether the next level opens. The learner learns the rule once and it holds
 * everywhere. **And it does not get set.** If severity and the bar both moved, two knobs
 * would do the same thing and nothing would say which made a sitting hard. What is set is the
 * severity; what has to be reached never moves.
 */
val BAR = Letter.B.floor
