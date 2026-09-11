package app.saylune.scene

import app.saylune.activity.Text
import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Direction
import app.saylune.levers.Move
import app.saylune.levers.Numeric
import app.saylune.levers.Positions
import app.saylune.levers.Stepped

/**
 * What a moment does to a scene (`docs/design/scene-state.md`, "L'ordre, dans un moment").
 *
 * 0. **What does not come from the events is laid first**: the app cases, what the leader
 *    wrote, and the draws of events that test nothing but the moment. At the casino, *when
 *    `hand` = won, chips + 1* reads the hand just drawn, whatever order the file lists them in.
 * 1. The events of the moment go, each at most once per moment.
 * 2. Those a change has just set off go in turn, until nothing moves.
 * 3. Only then does the app look at whether the scene is over -- which is what lets a false
 *    death refill the lives in the very moment they fell to zero.
 * 4. Then the texts arrive, in [Resolution].
 *
 * **Each event goes at most once per moment, and that bound is what makes it stop**: there are
 * finitely many, so the cascade ends even where two set each other off.
 *
 * **Inside a wave every test reads the same state, and every effect lands together.** Two
 * events of one wave writing the same case is a writing error, save where both put the same
 * value; what two shifts compose to depends on where the case started.
 *
 * **Who listens is read once, at the start of the moment.** Activating an event changes no fact
 * of the world, only who is watching, so it takes effect from the next moment: the event that
 * deactivates itself and activates another does not have the second fire in its own breath.
 *
 * Pure logic in plain JVM, so it is **proved by tests**, as the engine it replaces was.
 */
class Engine(cases: List<Case>, private val events: List<Event>) {

    private val declared: Map<String, Case> = cases.associateBy { it.key }

    init {
        require(declared.size == cases.size) {
            "two cases called ${cases.groupBy { it.key }.filterValues { it.size > 1 }.keys}"
        }
        val twice = events.groupBy { it.key }.filterValues { it.size > 1 }.keys
        require(twice.isEmpty()) { "two events called ${twice.joinToString()}" }
    }

    /** The case of the file called [key], or null where the file declares none. */
    fun case(key: String): Case? = declared[key]

    /** What [key] holds, whichever family declares it. An unknown name fails outright. */
    fun kindOf(key: String): Kind = when (family(key)) {
        Family.Lever -> leverOf(key).asKind()
        Family.App -> AppCases.kindOf(key) ?: error("$key: the app declares no such case")
        Family.File -> declared[key]?.kind ?: error("$key: the file declares no such case")
    }

    /** Where a scene starts: every case at its starting value, the events active at first. */
    fun start(positions: Positions): State = State(
        values = declared.values.mapNotNull { case -> case.start?.let { case.key to it } }.toMap(),
        positions = positions,
        active = events.filter { it.active }.map { it.key }.toSet(),
    )

    /** What [key] holds in [state], null where it is empty. */
    fun read(state: State, key: String): Value? = when (family(key)) {
        Family.Lever -> state.positions.of(key.removePrefix(LEVER)).asValue()
        else -> state.values[key]
    }

    /**
     * Run [moment] on [state].
     *
     * [outside] is step 0: the app cases the caller computed, and what the leader wrote,
     * which the events read at the passage's close and never before -- a retaken attempt takes
     * its writings with it before anything has read them. A null value empties the case.
     *
     * [spent] is the events already gone earlier in the same moment, which is how a moment cut
     * into two instants stays one: the end of an attempt runs when the call returns and again
     * when the analysis ends, and recording runs at every tick of its clocks.
     */
    fun resolve(
        moment: Moment,
        state: State,
        outside: Map<String, Value?> = emptyMap(),
        chance: Chance = Chance.NONE,
        spent: Set<String> = emptySet(),
    ): Resolution {
        val out = Gathering()
        var now = state
        val before = state

        // **An instruction expires between passages and never inside one**: a passage already
        // spoken is never judged again. One laid at this very close is not aged by it.
        if (moment == Moment.PassageClose) now = aged(now, out)

        outside.forEach { (key, value) ->
            require(family(key) != Family.Lever) {
                "$key: a lever is written by the author and his events, never from outside"
            }
            value?.let { require(kindOf(key).holds(it)) { "$key cannot hold $it" } }
        }
        now = now.copy(values = outside.entries.fold(now.values) { held, (key, value) ->
            if (value == null) held - key else held + (key to value)
        })

        val listening = events.filter {
            it.at == moment && it.key in now.active &&
                (now.awaiting == null || it.question == now.awaiting)
        }

        // The draws of events testing nothing but the moment are laid with the rest of step 0,
        // so an event of the same moment reads them whatever the order of the file.
        val drawnFirst = mutableSetOf<Pair<String, Int>>()
        listening.filter { it.test == Test.None && it.key !in spent }.forEach { event ->
            event.effects.forEachIndexed { at, effect ->
                if (effect is Effect.Draw) {
                    now = now.copy(values = now.values + (effect.case to draw(effect, chance, out)))
                    drawnFirst += event.key to at
                }
            }
        }

        var changes = changesBetween(before, now)
        // What a chain started from the turn has written: an event reading one of these is
        // part of the chain, and the line it makes somebody say waits for the next call.
        val fromTheTurn = outside.keys.filter(AppCases::readsTheTurn).toMutableSet()
        val fired = spent.toMutableSet()
        var finishing: Outcome? = null
        var active = now.active

        while (true) {
            val going = listening.filter { it.key !in fired && holds(it.test, now, changes) }
            if (going.isEmpty()) break
            going.forEach { fired += it.key }

            val writes = mutableMapOf<String, MutableList<Writing>>()
            val wave = mutableListOf<Pair<Event, Boolean>>()
            going.forEach { event ->
                val held = event.test.case?.let {
                    AppCases.readsTheTurn(it) || it in fromTheTurn
                } ?: false
                wave += event to held
                event.effects.forEachIndexed { at, effect ->
                    if (event.key to at in drawnFirst) return@forEachIndexed
                    val value = when (effect) {
                        is Effect.Put -> effect.value
                        is Effect.Shift -> shifted(now, effect)
                        is Effect.Step -> stepped(now, effect)
                        is Effect.Draw -> draw(effect, chance, out)
                        else -> null
                    }
                    value?.let { written(effect)!! to it }?.let { (key, value) ->
                        require(family(key) != Family.App) {
                            "${event.key}: $key is written by the app alone"
                        }
                        require(kindOf(key).holds(value)) { "${event.key}: $key cannot hold $value" }
                        writes.getOrPut(key) { mutableListOf() } +=
                            Writing(event.key, value, absolute = effect is Effect.Put)
                    }
                }
            }
            clash(writes)

            val landed = now
            writes.forEach { (key, by) -> now = write(now, key, by.first().value) }
            wave.forEach { (event, held) ->
                val moves = mutableListOf<Move>()
                val texts = mutableListOf<Text>()
                event.effects.forEach { effect ->
                    when (effect) {
                        is Effect.Put, is Effect.Shift, is Effect.Step, is Effect.Draw -> {
                            val key = written(effect)!!
                            if (held) fromTheTurn += key
                            if (family(key) == Family.Lever) moveOf(key, landed, now)?.let { moves += it }
                        }
                        is Effect.Switch -> active = if (effect.on) active + effect.event
                        else active - effect.event
                        is Effect.Finish -> finishing = finishing ?: effect.outcome
                        is Effect.Tell -> {
                            if (effect.reader != Reader.Learner) {
                                out.leader += effect.text.inLanguage(Text.BASE)
                            }
                            if (effect.reader != Reader.Leader) when (val form = effect.form) {
                                Form.Notice -> texts += effect.text
                                is Form.Thread -> out.thread += Line(form.who, effect.text, held)
                            }
                        }
                        is Effect.Instruct -> {
                            now = now.copy(
                                instructions = now.instructions.filterNot {
                                    it.instruct.key == effect.key
                                } + Standing(effect, effect.lasts),
                            )
                            out.notices += Notice.Instruction(effect, on = true)
                        }
                        is Effect.Lift -> now.instructions.firstOrNull {
                            it.instruct.key == effect.key
                        }?.let { lifted ->
                            now = now.copy(instructions = now.instructions - lifted)
                            out.notices += Notice.Instruction(lifted.instruct, on = false)
                        }
                        is Effect.Direct -> out.directions += Directed(effect.prose, held)
                        is Effect.AskLeader -> out.asking += effect
                        is Effect.AskLearner -> {
                            require(moment == Moment.Launch) {
                                "${event.key}: the learner is asked at the launch only"
                            }
                            out.learnerAsks += effect
                        }
                    }
                }
                // **A lever that changes shows itself**, with its mechanical phrase; a text the
                // same event sends the learner is shown in the same notification.
                if (moves.isNotEmpty() || texts.isNotEmpty()) {
                    out.notices += Notice.Event(event.key, moves, texts)
                }
            }
            changes = changesBetween(landed, now)
        }

        now = now.copy(active = active, ended = now.ended ?: finishing)
        return Resolution(
            state = now,
            fired = fired,
            notices = out.notices,
            thread = out.thread,
            leader = out.leader,
            directions = out.directions,
            asking = out.asking,
            learnerAsks = out.learnerAsks,
            drawn = out.drawn,
            changed = declared.values.filter { !it.hidden }.mapNotNull { case ->
                val from = before.values[case.key]
                val to = now.values[case.key]
                if (from == to) null else Change(case, from, to)
            },
        )
    }

    // ── Tests ───────────────────────────────────────────────────────────────────────────

    private fun holds(test: Test, state: State, changes: Map<String, Pair<Value?, Value?>>): Boolean =
        when (test) {
            Test.None -> true
            is Test.Holds -> read(state, test.case)?.let { compare(test.case, it, test.op, test.value) }
                ?: false
            is Test.Every -> (read(state, test.case) as? Value.Num)?.n?.let {
                it > 0 && it % test.n == 0.0
            } ?: false
            is Test.Empty -> read(state, test.case) == null
            is Test.Becomes -> changes[test.case]?.let { (from, to) ->
                val was = from?.let { compare(test.case, it, test.op, test.value) } ?: false
                val now = to?.let { compare(test.case, it, test.op, test.value) } ?: false
                !was && now
            } ?: false
            is Test.Goes -> changes[test.case]?.let { (from, to) ->
                from != null && to != null && went(test.case, from, to, test.way)
            } ?: false
        }

    /** Whether [value] of [key] stands in [op] to [against]. Order is the case's own. */
    fun compare(key: String, value: Value, op: Op, against: Value): Boolean {
        if (op == Op.Eq) return value == against
        if (op == Op.Ne) return value != against
        val sign = compareTo(key, value, against)
        return when (op) {
            Op.Lt -> sign < 0
            Op.Le -> sign <= 0
            Op.Gt -> sign > 0
            Op.Ge -> sign >= 0
            else -> error("unreachable")
        }
    }

    private fun compareTo(key: String, a: Value, b: Value): Int = when {
        a is Value.Num && b is Value.Num ->
            (a.n ?: Double.POSITIVE_INFINITY).compareTo(b.n ?: Double.POSITIVE_INFINITY)
        a is Value.Pick && b is Value.Pick -> {
            val kind = kindOf(key) as? Kind.Choice
            require(kind != null && kind.ordered) { "$key: an order on a list that has none" }
            kind.rank(a.name).compareTo(kind.rank(b.name))
        }
        else -> error("$key: $a and $b have no order")
    }

    private fun went(key: String, from: Value, to: Value, way: Way): Boolean = when (way) {
        Way.Harder, Way.Easier -> {
            require(family(key) == Family.Lever) { "$key: only a lever gets harder or easier" }
            harder(leverOf(key), from.asPosition(), to.asPosition()) == (way == Way.Harder)
        }
        Way.Up -> compareTo(key, to, from) > 0
        Way.Down -> compareTo(key, to, from) < 0
    }

    // ── Writes ──────────────────────────────────────────────────────────────────────────

    /** The case [effect] writes, or null where it writes none. */
    private fun written(effect: Effect): String? = when (effect) {
        is Effect.Put -> effect.case
        is Effect.Shift -> effect.case
        is Effect.Step -> effect.case
        is Effect.Draw -> effect.case
        else -> null
    }

    private fun shifted(state: State, shift: Effect.Shift): Value {
        val kind = kindOf(shift.case) as? Kind.Number ?: error("${shift.case}: a shift on a non-number")
        val n = (read(state, shift.case) as? Value.Num)?.n
            ?: error("${shift.case}: shifted while it is empty or has no maximum")
        return Value.Num(kind.clamp(n + shift.by))
    }

    private fun stepped(state: State, step: Effect.Step): Value {
        val kind = kindOf(step.case) as? Kind.Choice
        require(kind != null && kind.ordered) { "${step.case}: a step on a list with no order" }
        val lever = family(step.case) == Family.Lever
        require(lever == (step.way == Way.Harder || step.way == Way.Easier)) {
            "${step.case}: a lever steps harder or easier, a case of the file up or down"
        }
        val at = (read(state, step.case) as? Value.Pick)?.name
            ?: error("${step.case}: stepped while it is empty")
        val by = if (step.way == Way.Up || step.way == Way.Harder) 1 else -1
        return Value.Pick(kind.among[(kind.rank(at) + by).coerceIn(kind.among.indices)])
    }

    private fun draw(effect: Effect.Draw, chance: Chance, out: Gathering): Value {
        val among = (kindOf(effect.case) as? Kind.Choice)?.among
            ?: error("${effect.case}: chance picks among values, and this case has none")
        val drawn = chance.draw(effect.case, among, among.map { effect.weights[it] ?: 1 })
        require(drawn in among) { "${effect.case}: drew '$drawn', not one of $among" }
        out.drawn += effect.case to drawn
        return Value.Pick(drawn)
    }

    private fun write(state: State, key: String, value: Value): State = when (family(key)) {
        Family.Lever -> state.copy(
            positions = state.positions.at(key.removePrefix(LEVER), value.asPosition()).first,
        )
        else -> state.copy(values = state.values + (key to value))
    }

    private fun moveOf(key: String, from: State, to: State): Move? {
        val lever = leverOf(key)
        val was = from.positions.of(lever.key)
        val now = to.positions.of(lever.key)
        if (was == now) return null
        return Move(lever, was, now, if (harder(lever, was, now)) Direction.Harder else Direction.Easier)
    }

    /**
     * Two writings of one case in one wave. **Allowed only where every one puts the same
     * value**, the one case the coincidence shows without running anything; two shifts that
     * happen to land on the same number from this start would not from another.
     */
    private fun clash(writes: Map<String, List<Writing>>) = writes.forEach { (key, by) ->
        require(by.size < 2 || (by.all { it.absolute } && by.map { it.value }.distinct().size == 1)) {
            "$key is written in one wave by ${by.joinToString { it.event }}, " +
                "and what they compose to depends on where it started"
        }
    }

    private data class Writing(val event: String, val value: Value, val absolute: Boolean)

    private fun changesBetween(a: State, b: State): Map<String, Pair<Value?, Value?>> {
        val keys = a.values.keys + b.values.keys
        val levers = (a.positions.all().keys + b.positions.all().keys).map { "$LEVER$it" }
        return (keys + levers).mapNotNull { key ->
            val from = read(a, key)
            val to = read(b, key)
            if (from == to) null else key to (from to to)
        }.toMap()
    }

    private fun aged(state: State, out: Gathering): State = state.copy(
        instructions = state.instructions.mapNotNull {
            when {
                it.left == null -> it
                it.left > 1 -> it.copy(left = it.left - 1)
                else -> {
                    out.notices += Notice.Instruction(it.instruct, on = false)
                    null
                }
            }
        },
    )

    private class Gathering {
        val notices = mutableListOf<Notice>()
        val thread = mutableListOf<Line>()
        val leader = mutableListOf<String>()
        val directions = mutableListOf<Directed>()
        val asking = mutableListOf<Effect.AskLeader>()
        val learnerAsks = mutableListOf<Effect.AskLearner>()
        val drawn = mutableListOf<Pair<String, String>>()
    }
}

/**
 * Where a scene stands at one instant.
 *
 * **All of it is recomputed on a reopening** from what was stored -- what the leader wrote,
 * what chance drew, what the learner filled -- by running the moments again. Storing what the
 * events wrote as well would keep one thing twice, the writing and what makes it again.
 */
data class State(
    /** The cases of the file and of the app. A key absent is an empty case. */
    val values: Map<String, Value>,
    /** Where every lever sits, **effectively**: the declared positions plus what events moved. */
    val positions: Positions,
    val active: Set<String>,
    val instructions: List<Standing> = emptyList(),
    val ended: Outcome? = null,
    /** The question being asked again, while it is: every other event waits. */
    val awaiting: String? = null,
)

/** An instruction in force, and how many passages it has left, null for the rest of the scene. */
data class Standing(val instruct: Effect.Instruct, val left: Int?)

/**
 * Where a draw comes from. **What it drew is stored**, and a reopening hands the stored draws
 * back rather than drawing again: a seed would replay right only as long as the code had not
 * moved.
 */
fun interface Chance {
    fun draw(case: String, among: List<String>, weights: List<Int>): String

    companion object {
        val NONE = Chance { case, _, _ -> error("$case: a draw with nothing to draw from") }

        /** A real draw, each value with its weight. */
        fun of(random: kotlin.random.Random) = Chance { _, among, weights ->
            var left = random.nextInt(weights.sum())
            var at = 0
            while (left >= weights[at]) left -= weights[at++]
            among[at]
        }
    }
}

/** What a moment left behind, in the order the doc gives the texts. */
data class Resolution(
    val state: State,
    /** Every event gone in the moment so far, the [Engine.resolve] `spent` included. */
    val fired: Set<String>,
    val notices: List<Notice>,
    /** Lines written in advance, to be played in the thread. */
    val thread: List<Line>,
    /** Texts for the leader, which go with its next call. */
    val leader: List<String>,
    /** Turns of the leader nobody spoke before. */
    val directions: List<Directed>,
    /** The cases the leader is to write in its next call. */
    val asking: List<Effect.AskLeader>,
    val learnerAsks: List<Effect.AskLearner>,
    /** What chance drew, by case, to be stored. */
    val drawn: List<Pair<String, String>>,
    /** Every case known to the leader that changed over the moment, from its value at the start. */
    val changed: List<Change>,
)

/**
 * A line said in the thread, or a turn directed.
 *
 * [held] where the chain that made it started from the turn -- a note, a gate, a measure: the
 * leader has just answered without knowing the judgement, so the line waits for the next call
 * rather than contradict the reply. The rest of the chain is not held back.
 */
data class Line(val who: String, val text: Text, val held: Boolean)

data class Directed(val prose: String, val held: Boolean)

data class Change(val case: Case, val from: Value?, val to: Value?)

sealed interface Notice {
    /** What one event shows: the levers it moved, and the texts it sends the learner. */
    data class Event(val event: String, val moves: List<Move>, val texts: List<Text>) : Notice

    /** An instruction falling, or ceasing. */
    data class Instruction(val instruct: Effect.Instruct, val on: Boolean) : Notice
}
