package app.speakup.levers

import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets

/**
 * Where every lever of one sitting sits.
 *
 * **This is the whole of what a sitting stores of its settings** -- an open list of lever
 * positions and nothing else (`activity.md`). The test is that every entry answers the
 * same question: *where is this parameter right now*. A ramp does not answer it, it says how
 * things will change; an origin does not either, it says where the settings came from. Those
 * have fields of their own.
 *
 * What is held here is the **declared** position. The **effective** one is this plus whatever
 * the rules have patched, and confusing the two is what makes it look contradictory that the
 * settings are fixed for the whole sitting while the lives run out.
 */
data class Positions(private val set: Map<String, Position> = emptyMap()) {

    /**
     * Only what this sitting actually says, which is what gets written down.
     *
     * A key nobody moved is not in here: it answers with the catalogue's declared default,
     * and storing that would write a decision where nobody made one -- so a default that
     * changed in a later release would look like a choice the learner had taken.
     */
    fun all(): Map<String, Position> = set

    /**
     * Where [key] sits, its declared default when this sitting says nothing about it.
     *
     * **An undeclared key fails outright**, and that is the whole point of there being a
     * catalogue: falling back on a default for a key nobody declared is the silent default the
     * project forbids everywhere. A key that *is* declared and that this sitting leaves alone
     * is the other case entirely -- an expected absence with a documented default.
     */
    fun of(key: String): Position = set[key] ?: Levers.of(key).fallback()

    /** Whether [key] is read at all, or sits behind a requirement that does not hold. */
    fun live(key: String): Boolean {
        val needs = Levers.of(key).needs ?: return true
        val on = of(needs.key)
        return on is At && on.name in needs.positions
    }

    /**
     * Put [key] at [position], absolutely.
     *
     * The form a scripted death needs: `vies.restantes ← 1` cannot be written as a move, the
     * rule not knowing how many are left when it fires.
     */
    fun at(key: String, position: Position): Pair<Positions, Move?> {
        val lever = Levers.of(key)
        val from = of(key)
        val to = lever.hold(position)
        return moved(key, lever, from, to)
    }

    /**
     * Move [key] by [by] notches, up towards the hard end when [by] is positive.
     *
     * The form *losing a life* needs: whoever writes the rule does not know how many are left.
     * **A move stops at the bound**, which is not an error -- a ramp reaches its top notch by
     * design -- and **a move that changed nothing returns no [Move]**.
     */
    fun by(key: String, by: Int): Pair<Positions, Move?> {
        val lever = Levers.of(key)
        val from = of(key)
        val to = when (lever) {
            is Stepped -> {
                val name = (from as? At)?.name ?: lever.fallback
                At(lever.steps[(lever.rank(name) + by).coerceIn(lever.steps.indices)].name)
            }
            is Numeric -> {
                val n = (from as? Count)?.n
                    // The sentinel has no neighbour: *no maximum* minus one notch is not a
                    // number, and picking one would be inventing where the doc says nothing.
                    ?: error("${lever.key}: cannot move from 'no maximum'; set a position first")
                val towards = if (lever.hard == HardSide.Low) -by else by
                lever.clamp(Count(n + towards * lever.step))
            }
        }
        return moved(key, lever, from, to)
    }

    private fun moved(key: String, lever: Lever, from: Position, to: Position):
        Pair<Positions, Move?> {
        if (to == from) return this to null
        return Positions(set + (key to to)) to
            Move(lever, from, to, if (harder(lever, from, to)) Direction.Harder else Direction.Easier)
    }

    /**
     * Which way the change went.
     *
     * **The direction is read the same way in both forms**, which is the whole reason a number
     * costs nothing extra: a stepped lever is ordered by its declaration, a numeric one by
     * arithmetic plus the end it names as hard. Reading "two rewordings allowed" does not say
     * whether it just went up or down, and that is not the same news.
     */
    private fun harder(lever: Lever, from: Position, to: Position): Boolean = when (lever) {
        is Stepped -> lever.rank((to as At).name) > lever.rank((from as At).name)
        is Numeric -> {
            // No maximum sits above every number, whichever end is the hard one.
            val was = (from as Count).n ?: Int.MAX_VALUE
            val now = (to as Count).n ?: Int.MAX_VALUE
            if (lever.hard == HardSide.Low) now < was else now > was
        }
    }
}

private fun Lever.fallback(): Position = when (this) {
    is Stepped -> At(fallback)
    is Numeric -> fallback
}

/** [position] checked against this lever, and brought inside its bounds when it is a number. */
private fun Lever.hold(position: Position): Position = when (this) {
    is Stepped -> {
        val name = (position as? At)?.name
            ?: error("$key: a stepped lever was given $position")
        rank(name)
        position
    }
    is Numeric -> clamp(
        position as? Count ?: error("$key: a numeric lever was given $position"),
    )
}

/**
 * How severe these positions are on [sheet], as a rank of its sensitivity lever.
 *
 * **The sensitivity is a lever attached to a sheet**, and it is the one place the settings touch
 * a note: going towards severe tightens, always and for everybody, where a weight's direction
 * depends on the learner.
 *
 * A key this sitting says nothing about answers with the catalogue's declared default, which is
 * the middle position -- and one the catalogue does not declare **fails outright**, which is what
 * makes the sensitivities levers rather than a value read off the line beside them.
 *
 * It lives here, beside the positions, because two things read it: the gates, which turn a figure
 * into a letter to decide whether a passage may be left, and the passage's own summary, which
 * shows that letter. Read in two places it would be two severities to keep in step.
 */
fun Positions.severityOn(sheet: Sheet): Int {
    val key = Levers.sensitivityOf(Sheets.pathOf(sheet))
    return (Levers.of(key) as Stepped).rank((of(key) as At).name)
}
