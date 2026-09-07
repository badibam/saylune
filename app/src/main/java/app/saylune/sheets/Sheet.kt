package app.saylune.sheets

/**
 * What a sheet is, and what one has to declare to be one.
 *
 * **A challenge is written against a list, never against the code** (`activity.md`).
 * Without this, laying a condition -- the sheet *longest silence*, five seconds -- means
 * knowing that sheet exists, what it is called, and that its number is in seconds and not a
 * percentage. That is to say: reading the calculation.
 *
 * Four things not to confuse, and the confusion is easy. A **sheet** is a measure -- what the
 * app observes of what the learner did, and nobody sets it. A **lever** is a setting of the
 * sitting, which measures nothing. A **sensitivity** is a lever attached to a sheet, which
 * does not touch the measure but moves its A-E bounds. A **weight** is attached to a sheet
 * too and is **not** a lever, its direction depending on the learner; it lives in the weights
 * tree a definition writes.
 *
 * Nothing here carries a display name. What reads these on a screen -- the result screen, the
 * custom screen -- is outside the current perimeter, and this file stays free of Android so it
 * is provable in plain JVM.
 */
sealed interface Node {

    /** Its own name, unique among its siblings. Its address is [Sheets.pathOf]. */
    val name: String
}

/**
 * A node with children and no measure of its own.
 *
 * **What is scored is a tree, at free depth.** Aptitude, measure, finer cut are not three
 * natures: they are nodes, and depth only says how finely one can weigh. So the question "is
 * this a measure or a category of measure?" never comes up, and going finer means digging a
 * branch rather than changing mechanism.
 *
 * **The note of a node is the weighted mean of its present children**, and nothing else: no
 * floor, no bonus, no formula of its own for an aptitude. A node has **neither elements nor a
 * raw number**, its figure being a weighted mean in no unit at all -- so on a node the note is
 * the only one of the three readings available.
 */
data class Branch(override val name: String, val children: List<Node>) : Node {
    init { require(children.isNotEmpty()) { "$name: a branch with no child" } }
}

/**
 * A sheet: a set of elements, a way of giving an element a value, and a series.
 *
 * The third is always the same; it is the second that changes shape.
 */
data class Sheet(
    override val name: String,
    /** What it reads. The denominator is this, and never the whole passage. */
    val elements: Elements,
    val reading: Reading,
    val unit: Unit,
    /**
     * Which way is good -- **declared only where the figure keeps a raw unit**.
     *
     * Null everywhere else, because the value of an element is always a quality between 0 and
     * 1 with the top as the good end, so there is nothing to say.
     */
    val direction: Direction?,
    /**
     * The judged marking it reads, or null when it is calculated.
     *
     * **Only a judged marking takes instructions**, and they attach to the marking rather than
     * to a sheet: two sheets read one and the same pass of the judge over the spans, so there
     * is nothing for a per-sheet instruction to hang on.
     */
    val from: Marking?,
    /**
     * The ordered values a sensitivity window picks four consecutive bounds from, **most
     * lenient first**. Null on a sheet that gives no note.
     *
     * Nothing is computed here, everything is written: an impossible edge shows **while
     * writing the series** rather than at run time on a learner who cannot understand why A is
     * out of reach. A single transformation -- "severe is the bounds halved" -- was tried and
     * does not hold: the same gesture soundly tightens gross misses and breaks melody, where
     * two semitones become one, that is, below the machine's own noise band.
     */
    val series: List<Float>?,
) : Node {

    /** Whether it gives a note at all. Some exist only for conditions to read. */
    val scored: Boolean get() = series != null

    init {
        require((direction != null) == (reading is Reading.Raw && unit.raw)) {
            "$name: a direction is declared exactly where the figure keeps a raw unit"
        }
        series?.let { s ->
            require(s.size == SERIES_LENGTH) {
                "$name: a series of ${s.size}, not $SERIES_LENGTH -- " +
                    "$POSITIONS sensitivity positions need three more bounds than positions"
            }
            val towardsGood = if (direction == Direction.LowIsGood) -1 else 1
            require(s.zipWithNext().all { (a, b) -> (b - a) * towardsGood > 0 }) {
                "$name: a series that does not climb towards its good end"
            }
        }
    }
}

/**
 * How many sensitivity positions every sheet offers.
 *
 * **A position costs one number, not four**, since a position is a window of four consecutive
 * bounds in the series: the question stopped being a trade between expressiveness and work.
 * Five rather than the three the doc sketches, for one reason that is not taste -- the arcade
 * ramp climbs whole notches during a game, and with three positions a single move from normal
 * exhausts the lever.
 */
const val POSITIONS = 5

/** A window is four bounds, so a series holds three more values than it has positions. */
const val SERIES_LENGTH = POSITIONS + 3

/**
 * What a sheet reads, which is also its denominator.
 *
 * **Never the whole passage unless it says so**: on a challenge weighing only the *th*, one
 * miss out of the sentence's four *th* is 25% and not 3%. The weight picks the columns, the
 * denominator follows the weight.
 */
enum class Elements {
    /** The sounds of the passage. */
    Sounds,
    /** The syllables. */
    Syllables,
    /** The words of more than one syllable the model stresses clearly. */
    StressedWords,
    /** The words that are part of the sentence -- filler and abandoned starts left out. */
    KeptWords,
    /** Everything the mouth said, filler and abandoned starts included. */
    SpokenWords,
    /** The turn's time, split into silence between words and speech. */
    TurnTime,
    /** The passage, as one element. Nothing to average. */
    Whole,
}

/** How an element takes its value. */
sealed interface Reading {

    /**
     * One value per notch, for the judged markings.
     *
     * [notches] are declared **strongest precedence first**, which is what guarantees a word
     * carries at most one: two overlapping spans do not count twice, and a word takes the
     * worst notch it carries. Where no precedence applies -- an element the judge marks
     * exactly once -- the order carries nothing.
     *
     * [fallback] names the notch an unmarked element takes, and is null where the judge marks
     * every element. **An unmarked word is `ok`, which is a notch like any other and not an
     * absence.**
     */
    data class Column(val notches: List<Notch>, val fallback: String?) : Reading {
        init {
            require(notches.isNotEmpty()) { "a column with no notch" }
            require(notches.all { it.value in 0f..1f }) { "a notch outside 0..1" }
            require(fallback == null || notches.any { it.name == fallback }) {
                "the fallback names no notch of the column"
            }
        }
    }

    /**
     * A step: below [at] the element is worth 1, above it 0.
     *
     * Grading it would take over the other sheet's work, and the two would say the same thing
     * less clearly. The line is the one the screen's ramp saturates at, and it is the same for
     * the sheet and for the screen -- elsewhere, two letters painted the same solid red would
     * count differently in the note with no way to see the difference the note acts on.
     */
    data class Cliff(val at: Float) : Reading

    /** A continuous ramp: [from] is worth 1, [to] is worth 0, gross misses included. */
    data class Slope(val from: Float, val to: Float) : Reading

    /** True or false: the stress landed on the right syllable, or it did not. */
    data object Either : Reading

    /**
     * Nothing: the element already carries the sheet's own unit.
     *
     * A ramp only ever served to bring heterogeneous elements onto a common scale before
     * averaging them. Where the figure keeps its raw unit, it has no object.
     */
    data object Raw : Reading
}

/** One notch of a column, and what a passage made entirely of it would be worth. */
data class Notch(val name: String, val value: Float)

/** The unit of a sheet's figure. */
enum class Unit(val raw: kotlin.Boolean) {
    /** A proportion, which is the mean of a 0/1 -- one recipe, not two. */
    Share(raw = false),
    /** The value of a notch, on a sheet whose single element is the passage. */
    NotchValue(raw = false),
    Semitones(raw = true),
    Seconds(raw = true),
    /** Points of percentage, which can be negative. */
    PercentPoints(raw = true),
    Percent(raw = true),
    /** A whole count. Never in a note: it climbs at every attempt. */
    Times(raw = true),
}

/** Which end of a raw figure is the good one. */
enum class Direction { HighIsGood, LowIsGood }

/**
 * The three markings a language model returns. Everything else is calculated.
 *
 * This is where an instruction hangs: free text entering the criterion the judge reads. On the
 * spans, an instruction touches **relevance** only -- the notches of correctness are absolute.
 */
enum class Marking {
    /** A group of words, one correctness notch and one relevance notch. */
    LanguageSpans,
    /** Each spoken word: kept, abandoned, or filler. */
    Stumbling,
    /** One notch per passage: what the answer proves it took in. */
    Following,
}
