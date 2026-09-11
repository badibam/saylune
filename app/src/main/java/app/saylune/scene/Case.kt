package app.saylune.scene

import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.HardSide
import app.saylune.levers.Lever
import app.saylune.levers.Levers
import app.saylune.levers.Numeric
import app.saylune.levers.Position
import app.saylune.levers.Stepped
import app.saylune.notes.Letter
import app.saylune.sheets.Reading
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets

/**
 * One thing that can change during a scene: a name and a value (`docs/design/scene-state.md`).
 *
 * **Three families, told apart by who declares them and by their name.** A lever (`lever.`) is
 * declared by the app's catalogue, which knows what it means and applies it. An app case
 * (`app.`) is declared and written by the app alone -- the notes, the gates, the passage, the
 * clocks. A case of the file has no prefix: the app keeps it, tests it and cites it, and knows
 * nothing of what it means. The prefix of the first two is what keeps a case of the file from
 * looking like a lever.
 *
 * Only the third is declared by a file, so it is the only one this type describes; the two
 * others answer [kindOf] from their catalogues.
 */
data class Case(
    val key: String,
    val kind: Kind,
    /** Its value when the scene opens. Null is empty until something writes it. */
    val start: Value? = null,
    /**
     * A short line in English -- *"the barman's patience"* -- which is what the leader reads
     * when the case changes. Required on a case the leader knows, checked at loading.
     */
    val about: String? = null,
    /**
     * Kept from the leader: a technical counter, or a secret it must not give away. **A case of
     * the file is known to the leader by default**, and this is the author's choice to say
     * otherwise, which nothing else in the file could let the app deduce.
     */
    val hidden: Boolean = false,
) {
    init {
        require(family(key) == Family.File) { "$key: a file declares no lever and no app case" }
        start?.let { require(kind.holds(it)) { "$key: starts at $it, which it cannot hold" } }
    }
}

/** Who declares a case, read off its name. */
enum class Family { Lever, App, File }

/**
 * What [key] holds, whichever family declares it, the file's cases being [declared]. An unknown
 * name fails outright: a file is written against the catalogues, so a name nobody declares is a
 * writing mistake and never something to guess at.
 */
fun kindOf(key: String, declared: Map<String, Case>): Kind = when (family(key)) {
    Family.Lever -> leverOf(key).asKind()
    Family.App -> AppCases.kindOf(key) ?: error("$key: the app declares no such case")
    Family.File -> declared[key]?.kind ?: error("$key: the file declares no such case")
}

fun family(key: String): Family = when {
    key.startsWith(LEVER) -> Family.Lever
    key.startsWith(APP) -> Family.App
    else -> Family.File
}

const val LEVER = "lever."
const val APP = "app."

/**
 * What a case can hold. **Four sorts**, and the sort does not depend on the writer, save that
 * chance writes no [Words], knowing only how to pick among values given.
 */
sealed interface Kind {

    fun holds(value: Value): Boolean

    /** True or false. A list of *yes / no* is this, which is only written one way. */
    data object Flag : Kind {
        override fun holds(value: Value) = value is Value.Flag
    }

    /**
     * A number with its bounds, either of which may be open. A shift stops at a bound.
     *
     * [unbounded] is for a lever with **no maximum** alone, whose sentinel is a position
     * sitting above every number.
     */
    data class Number(
        val min: Double? = null,
        val max: Double? = null,
        val unbounded: Boolean = false,
    ) : Kind {
        override fun holds(value: Value) = value is Value.Num && when (val n = value.n) {
            null -> unbounded
            else -> (min == null || n >= min) && (max == null || n <= max)
        }

        fun clamp(n: Double): Double = n.coerceIn(min ?: n, max ?: n)
    }

    /**
     * One value among values written in advance.
     *
     * **An ordered list is declared from its low end to its high end**, so *up* goes towards
     * the last: the notes run E to A. A lever's steps are declared easy to hard, which is the
     * same reading with *harder* for *up*.
     */
    data class Choice(val among: List<String>, val ordered: Boolean = false) : Kind {
        init {
            require(among.size >= 2) { "a list of fewer than two values" }
            require(among.toSet().size == among.size) { "a list holding a value twice" }
        }

        override fun holds(value: Value) = value is Value.Pick && value.name in among

        fun rank(name: String): Int = among.indexOf(name).also {
            require(it >= 0) { "'$name' is not one of $among" }
        }
    }

    /** Words, which no event tests: nothing in the code understands *"the flat in Lyon"*. */
    data object Words : Kind {
        override fun holds(value: Value) = value is Value.Words
    }
}

sealed interface Value {
    data class Flag(val on: Boolean) : Value
    /** A number, or **no maximum** when null, which only a lever holds. */
    data class Num(val n: Double?) : Value
    data class Pick(val name: String) : Value
    data class Words(val text: String) : Value
}

/**
 * The cases the app declares and writes, and what each holds.
 *
 * **They translate what the engine already reads**, adding nothing but the sitting: the
 * passage, the two clocks, a note for every node of the tree, a figure and a count per notch
 * for every sheet, and the two gates. A proportion is read off a sheet's figure and never off a
 * count put over another: *at least one* is what an element says, *what share* is what the
 * figure says.
 */
object AppCases {

    const val PASSAGE = "app.passage"
    const val TURN_TIME = "app.turn-time"
    const val SILENCE = "app.silence"
    const val WORDS_GATE = "app.words-gate"
    const val SOUND_GATE = "app.sound-gate"
    private const val SITTING = "app.sitting."
    private const val OUTCOME = "app.outcome."

    /** The two clocks, which exist while recording and at no other moment. */
    val CLOCKS = setOf(TURN_TIME, SILENCE)

    /**
     * The three fixed notches the sounds are counted on, the ones the screen shows: **in the
     * noise** under 5 points, where a letter keeps its neutral ink; **off** from 5 to 30, tinted
     * amber to red; **gross** at 30 and over, full red, the line where *the word changed*.
     */
    val SOUND_NOTCHES = listOf("noise", "off", "gross")

    private val NOTES = Kind.Choice(Letter.entries.map { it.name }, ordered = true)
    private val FIGURE = Kind.Number()
    private val COUNT = Kind.Number(min = 0.0)

    /** The two sheets whose elements are the sounds, and so counted on [SOUND_NOTCHES]. */
    private val SOUNDED = setOf("pronunciation/intelligibility", "pronunciation/proximity")

    /**
     * Every app case of one scene, by key.
     *
     * The notes, the figures and the counts exist twice: once for the attempt or the passage
     * -- the moment says which --, and once for the whole sitting under `app.sitting.`, which
     * a closing's note and a round-up need and which *this passage fell to C* does not say.
     */
    private val declared: Map<String, Kind> = buildMap {
        put(PASSAGE, Kind.Number(min = 0.0))
        put(TURN_TIME, Kind.Number(min = 0.0))
        put(SILENCE, Kind.Number(min = 0.0))
        put(WORDS_GATE, Kind.Flag)
        put(SOUND_GATE, Kind.Flag)
        fun both(key: String, kind: Kind) {
            put("$APP$key", kind)
            put("$SITTING$key", kind)
        }
        treePaths().forEach { both("$it.note", NOTES) }
        Sheets.all.forEach { sheet ->
            val path = Sheets.pathOf(sheet)
            both("$path.figure", FIGURE)
            notchesOf(sheet).forEach { both("$path.$it.count", COUNT) }
        }
    }

    /** Every node of the tree that gives a note, branches and sheets alike. */
    fun treePaths(): List<String> = buildList {
        fun walk(node: app.saylune.sheets.Node, above: String) {
            val path = if (above.isEmpty()) node.name else "$above/${node.name}"
            if (node.name.isNotEmpty()) add(path)
            if (node is app.saylune.sheets.Branch) node.children.forEach { walk(it, path) }
        }
        walk(Sheets.tree, "")
    }

    /**
     * The notches a sheet's elements are counted by: its column's, or the three of the sounds.
     * A sheet read otherwise counts nothing -- the silences keep their figure in seconds, no
     * threshold on them having a declared meaning.
     */
    fun notchesOf(sheet: Sheet): List<String> = when {
        sheet.reading is Reading.Column -> (sheet.reading as Reading.Column).notches.map { it.name }
        Sheets.pathOf(sheet) in SOUNDED -> SOUND_NOTCHES
        else -> emptyList()
    }

    /** Which notch a sound falls in, by its gap in points -- the screen's own two lines. */
    fun soundNotch(points: Float): String = when {
        points < NOISE_BAND -> "noise"
        points < GROSS -> "off"
        else -> "gross"
    }

    const val NOISE_BAND = 5f
    const val GROSS = 30f

    /** The case holding the outcome of [scene], which a story reads to branch. */
    fun outcomeOf(scene: String) = "$OUTCOME$scene"

    val OUTCOMES = Kind.Choice(listOf("failed", "passed"), ordered = true)

    /**
     * What the app case [key] holds, or null where the app declares no such case.
     *
     * An outcome is the one app case whose name carries a scene, so it is read off its prefix.
     */
    fun kindOf(key: String): Kind? =
        declared[key] ?: if (key.startsWith(OUTCOME)) OUTCOMES else null

    /**
     * Whether [key] is read off the turn -- a note, a gate, a figure, a count, a clock.
     *
     * **A chain of events that starts from one of these holds back the line it makes a
     * character say**: the leader has just answered without knowing the judgement, and a
     * barman who replies as if he understood and then says *"Wait, what?"* contradicts the
     * thread. The passage number reads nothing of the turn, and neither does an outcome.
     */
    fun readsTheTurn(key: String): Boolean =
        family(key) == Family.App && key != PASSAGE && !key.startsWith(OUTCOME)

    /** Whether [key] exists at [moment]: the clocks while recording, the rest outside it. */
    fun existsAt(key: String, moment: Moment): Boolean = when (key) {
        in CLOCKS -> moment == Moment.Recording
        PASSAGE -> true
        else -> key.startsWith(OUTCOME) || key.startsWith(SITTING) ||
            moment in setOf(Moment.AttemptEnd, Moment.PassageClose, Moment.Answer)
    }
}

/** The lever a `lever.` case names in the catalogue. */
fun leverOf(key: String): Lever = Levers.of(key.removePrefix(LEVER))

/**
 * What a lever holds, seen as a case: a stepped lever is an ordered list, easy first, and a
 * numeric one a number with its bounds.
 */
fun Lever.asKind(): Kind = when (this) {
    is Stepped -> Kind.Choice(steps.map { it.name }, ordered = true)
    is Numeric -> Kind.Number(min.toDouble(), max?.toDouble(), unbounded = max == null)
}

fun Position.asValue(): Value = when (this) {
    is At -> Value.Pick(name)
    is Count -> Value.Num(n?.toDouble())
}

fun Value.asPosition(): Position = when (this) {
    is Value.Pick -> At(name)
    is Value.Num -> Count(n?.let { Math.round(it).toInt() })
    else -> error("a lever cannot hold $this")
}

/**
 * Whether [to] is harder than [from] on [lever]: later in its steps, or towards the end its
 * numbers name as hard. No maximum sits above every number.
 */
fun harder(lever: Lever, from: Position, to: Position): Boolean = when (lever) {
    is Stepped -> lever.rank((to as At).name) > lever.rank((from as At).name)
    is Numeric -> {
        val was = (from as Count).n ?: Int.MAX_VALUE
        val now = (to as Count).n ?: Int.MAX_VALUE
        if (lever.hard == HardSide.Low) now < was else now > was
    }
}
