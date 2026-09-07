package app.saylune.rules

import app.saylune.levers.Direction
import app.saylune.levers.Move
import app.saylune.levers.Position
import app.saylune.levers.Positions

/**
 * What a moment does to a sitting: the rules of that moment fire, and their effects land.
 *
 * **Its own module, outside the turn's pipeline**, and that is deliberate: this is pure logic
 * that runs in plain JVM with no device and no network, so it is **proved by tests in the
 * repository** rather than looked at on a bench. The project tests little outside its benches,
 * which holds while what is written is measurement -- a bench *is* a test. Resolving by waves
 * is not measurement, it is an interpreter: a termination to prove, and a writing error to
 * detect.
 *
 * **By waves.** Every trigger is read against the **same snapshot**, the one at the start of
 * the moment; then every effect lands together. What those effects have just made true opens
 * the next wave, and so on until nothing new fires.
 *
 * **A rule fires at most once per moment, and that bound is what makes it terminate**: there
 * are finitely many rules, each goes at most once, so the cascade stops even if two rules set
 * each other off. A rule going twice in one instant would be a writing error anyway.
 *
 * **The closing moment is a resolve of its own, and the caller runs it.** Once a moment has
 * ended the sitting the state carries that outcome and cannot lose it, so what fires at
 * [Moment.Closing] is a **coda**: it can add a last word and have the open questions answered,
 * and it can no longer un-finish. Letting it would make *is it over?* undecided during its own
 * wave.
 *
 * **Inside a wave, order changes nothing**, so the order rules are declared in has no meaning
 * to carry. One case would bite: two patches of one wave on the same key. That is not a case
 * to arbitrate by priority, it is a **writing error** -- except where they lay the same
 * absolute position, the one case the coincidence can be seen without running anything. Two
 * moves, or a move and a position, cannot be read: what they compose to depends on where one
 * started.
 *
 * What that costs, said plainly: authoring systems usually do the opposite, in sequence, where
 * laying a value down and reading it back works in one breath. What is lost is a chain of
 * three things in one instant; what is gained is that a definition can be read without being
 * run, which is this doc's written fear. And the beats of a scene are separated by turns of
 * speech anyway, so by distinct moments.
 */
class Engine(private val rules: List<Rule>) {

    init {
        val twice = rules.groupBy { it.key }.filterValues { it.size > 1 }.keys
        require(twice.isEmpty()) { "two rules called ${twice.joinToString()}" }
    }

    /**
     * Run [moment] against [state], reading the world through [facts].
     *
     * [facts] is re-read at each wave: a trigger reads what is true now, and what the previous
     * wave made true is exactly what opens this one.
     */
    fun resolve(moment: Moment, state: State, facts: Facts): Resolution {
        var now = state
        val fired = mutableSetOf<String>()
        val notices = mutableListOf<Notice>()
        val messages = mutableListOf<Effect.Message>()
        val chosen = mutableListOf<Choice>()
        // What the wave just past moved. **A lever having moved is read here and not asked of
        // the outside**: it is a change this module made, and the one place that knows a
        // change happened is the wave that made it.
        var justMoved = emptyList<Move>()
        // What a rule of this moment asked to end, read once the waves are over like the
        // lives are -- see the terminal check below.
        var finishing: Outcome? = null

        // Who is listening, read once at the start of the moment and not again.
        //
        // **Arming does not open a wave** (settled 2026-09-06, found by writing the overload).
        // A wave opens on what the effects made **true** -- a life lost, a threshold reached --
        // and arming changes no fact of the world: it changes which rules are watching. Let it
        // open one and the overload the doc describes collapses, the rule just armed firing in
        // the same breath as the one that armed it, so both its phrases land on one passage.
        //
        // This is not the same as having one wave. Two triggers read the state -- a lever
        // having **moved**, and a lever **reaching** a value -- and both are facts a patch of
        // this very moment can make true. Without waves, reacting to a life lost would arrive
        // a passage late, and the scripted death would fall one passage after the death.
        val listening = now.armed

        while (true) {
            val going = rules.filter {
                it.key !in fired &&
                    it.key in listening &&
                    it.whenever.moment == moment &&
                    facts.holds(it.whenever, now, justMoved)
            }
            if (going.isEmpty()) break
            going.forEach { fired += it.key }

            // Which pack each rule takes. A rule with one pack has no choice to make and
            // nothing to write down; the other two deciders leave a trace, because what does
            // not come back on its own is the choice.
            val packs = going.map { rule ->
                val at = when (rule.decider) {
                    Decider.Written -> 0
                    Decider.Chance, Decider.Model -> facts.chose(rule).also {
                        require(it in rule.choice.indices) {
                            "${rule.key}: pack $it of ${rule.choice.size}"
                        }
                    }
                }
                if (rule.decider != Decider.Written) {
                    chosen += Choice(rule.key, at)
                }
                rule to rule.choice[at]
            }

            val landed = land(packs, now)
            now = landed.state
            notices += landed.notices
            messages += landed.messages
            justMoved = landed.moves
            finishing = finishing ?: landed.finishing
        }

        // **The terminal check comes after the waves**, once, on the settled state and never
        // on the transition -- otherwise the ending would always win the race against the rule
        // that refills the lives, and the scripted death would be unwritable.
        //
        // **[Effect.Finish] is read here too, and that is the generalisation**: the ending by
        // zero lives already behaved this way, and finishing by rule did not, so the app had
        // two kinds of ending that did not behave alike. Both now settle on the stabilised
        // state, and what a rule declared outright wins over what the lives say.
        val over = now.ended ?: finishing ?: endingOf(now)
        return Resolution(now.copy(ended = over), notices, messages, chosen)
    }

    /**
     * Every choice the rules of [moment] are offering right now: **computed, never maintained**.
     *
     * On most turns it is empty and nothing is sent. It is the set of packs the rules that
     * fire now put on offer, and it is what bounds the model: **nothing undeclared can be
     * chosen**, for want of anything to carry it out. The model picks a key; it never invents
     * one.
     */
    fun menu(moment: Moment, state: State, facts: Facts): List<Offer> = rules
        .filter {
            it.key in state.armed && it.whenever.moment == moment &&
                it.decider == Decider.Model && facts.holds(it.whenever, state, emptyList())
        }
        .map { Offer(it.key, it.choice) }

    // ── One wave ────────────────────────────────────────────────────────────────────────

    private fun land(packs: List<Pair<Rule, Pack>>, state: State): Landed {
        val effects = packs.flatMap { (rule, pack) -> pack.effects.map { rule to it } }
        val patches = effects.mapNotNull { (rule, effect) ->
            (effect as? Effect.Patch)?.let { rule to it }
        }
        clash(patches)

        var positions = state.positions
        var armed = state.armed
        var instructions = state.instructions
        var finishing: Outcome? = null
        val moves = mutableListOf<Move>()
        val notices = mutableListOf<Notice>()
        val messages = mutableListOf<Effect.Message>()

        effects.forEach { (_, effect) ->
            when (effect) {
                is Effect.Patch -> {
                    effect.positions.forEach { (key, at) ->
                        val (next, move) = positions.at(key, at)
                        positions = next
                        move?.let { moves += it }
                    }
                    effect.moves.forEach { (key, by) ->
                        val (next, move) = positions.by(key, by)
                        positions = next
                        move?.let { moves += it }
                    }
                    effect.instructions.forEach { laid ->
                        instructions =
                            // **A marking carries several at once, each with its own life**,
                            // so laying one adds rather than replaces: one slot replaced each
                            // time would not do the moment two have different lives, the
                            // merged text having to be rewritten at every expiry.
                            if (laid.text != null) instructions + laid
                            // A null text takes off **every** instruction on that marking.
                            // Taking off only what this rule laid would mean tracking who laid
                            // what, and an author has no way of naming one of several from
                            // outside; blunt and readable beats fine and unsayable.
                            else instructions.filterNot { it.marking == laid.marking }
                    }
                    armed = effect.arming.entries.fold(armed) { held, (key, on) ->
                        if (on) held + key else held - key
                    }
                    // **What changed nothing announces nothing**: saying *this tightens* with
                    // nothing tightened is a lie. So the staging line rides on the patch and
                    // the mechanical ones ride on the moves that really happened.
                    effect.staging?.let { notices += Notice.Staged(it) }
                }
                // Not landed on the state: it is read once the waves are over, with the
                // lives, so the two kinds of ending behave alike. The first one declared
                // wins, order inside a wave carrying no meaning anywhere else either.
                is Effect.Finish -> finishing = finishing ?: effect.outcome
                is Effect.Message -> messages += effect
            }
        }
        moves.forEach { notices += Notice.Moved(it) }
        return Landed(
            State(positions, armed, instructions, state.ended),
            notices, messages, moves, finishing,
        )
    }

    /**
     * A passage has closed: every instruction with a life left loses one, and one that runs
     * out goes.
     *
     * **What is laid only ever counts for what follows**: a passage already spoken is never
     * rejudged, so an instruction expires between passages and not inside one.
     */
    fun aged(state: State): State = state.copy(
        instructions = state.instructions.mapNotNull {
            when {
                it.lasts == null -> it
                it.lasts > 1 -> it.copy(lasts = it.lasts - 1)
                else -> null
            }
        },
    )

    /**
     * Two patches of one wave on the same key.
     *
     * **A writing error and not a case to arbitrate** -- except where both lay the **same
     * absolute position**, which is the one case the coincidence is visible without running
     * anything. Two moves, or a move and a position, cannot be read: what they compose to
     * depends on where one started.
     */
    private fun clash(patches: List<Pair<Rule, Effect.Patch>>) {
        val laid = mutableMapOf<String, MutableList<Pair<String, Position?>>>()
        patches.forEach { (rule, patch) ->
            patch.positions.forEach { (key, at) ->
                laid.getOrPut(key) { mutableListOf() } += rule.key to at
            }
            patch.moves.keys.forEach { key ->
                laid.getOrPut(key) { mutableListOf() } += rule.key to null
            }
        }
        laid.forEach { (key, by) ->
            if (by.size < 2) return@forEach
            val positions = by.map { it.second }
            val agree = positions.all { it != null } && positions.distinct().size == 1
            require(agree) {
                "$key is patched in one wave by ${by.joinToString { it.first }}, " +
                    "and what they compose to depends on where it started"
            }
        }
    }

    /**
     * **Zero lives ends it, and that is a declared property of the lever rather than a rule.**
     *
     * It is read once per moment, after every wave has finished, on the settled state -- which
     * is what lets a rule refill the lives inside the same moment without the ending falling:
     * the scripted death one walks through.
     */
    private fun endingOf(state: State): Outcome? {
        val lives = state.positions.of(LIVES)
        return if ((lives as? app.saylune.levers.Count)?.n == 0) Outcome.Failed else null
    }

    companion object {
        /**
         * The lives, whose **position is the number left** -- not an allocation sitting beside
         * a counter. That is what makes there be no lives object, so no kind of effect *take a
         * life* beside *lay a patch*.
         */
        const val LIVES = "lives.left"
    }
}

/**
 * What a sitting's rules have made of it, at one instant.
 *
 * All of it is **derived**: recomputable at any moment from the settings, the rules and the
 * journal (`activity.md`). It is held as a value rather than recomputed on every read
 * because a wave has to see what the wave before it did.
 */
data class State(
    /** The **effective** positions: the declared ones plus what the patches have moved. */
    val positions: Positions,
    /** Which rules are armed. A rule is never removed; what it did is undone. */
    val armed: Set<String>,
    val instructions: List<Instructing> = emptyList(),
    /** The outcome, once something has ended the sitting. Null while it runs. */
    val ended: Outcome? = null,
)

/** What a moment left behind. */
data class Resolution(
    val state: State,
    val notices: List<Notice>,
    val messages: List<Effect.Message>,
    /** What a draw or the AI took, for the journal. Empty when nothing had a choice to make. */
    val chosen: List<Choice>,
)

/** One rule, and which of its packs was taken. */
data class Choice(val rule: String, val pack: Int)

/** One rule's packs, offered to the model in the call already being made. */
data class Offer(val rule: String, val packs: List<Pack>)

/**
 * What the learner is shown of a change.
 *
 * **The mechanical one is obligatory and the narrative one is optional.** Whoever reads only
 * *"the barman seems in a hurry"* does not know their turn now goes on its own after five
 * seconds, and will take it for a bug the first time it happens -- and the whole value of the
 * mechanical phrase is being able to reconstruct why a note moved. A free conversation shows
 * the mechanical one alone, having no fiction; a definition that writes no narrative one is
 * dry, not broken.
 */
sealed interface Notice {

    /** A lever really moved: the phrase declared with it, plus which way it went. */
    data class Moved(val move: Move) : Notice

    /** The scene's own line, whose flag says which of the two places it takes. */
    data class Staged(val staging: Staging) : Notice
}

/**
 * What the world says, read afresh at each wave.
 *
 * It is an interface and not a snapshot because **what the previous wave made true is what
 * opens this one**: a trigger reads what is true now. Inside one wave every trigger sees the
 * same thing, which is what makes order not matter there.
 */
interface Facts {

    /** How far a clock has run, in milliseconds. Only ever read while recording. */
    fun clock(which: Trigger.Clock.Which): Int = 0

    /** How many passages have closed. */
    val passage: Int get() = 0

    /**
     * Whether a node of the tree says what the trigger says it says.
     *
     * The three readings live here together because what each one means is the sheet's
     * business and not this module's: a note is a letter, a figure is in the sheet's own unit,
     * an element is a fact about one of the things it reads.
     */
    fun node(of: Trigger.Node): Boolean = false

    /** What the model answered to a piece of free prose. Requested, so nobody checks it. */
    fun judged(prose: String): Boolean = false

    /** Which pack a draw or the model took for this rule. */
    fun chose(rule: Rule): Int = 0

}

/**
 * Whether [trigger] holds right now: against [state] for what reads a position, against
 * [moved] for what reads a change.
 *
 * **Public because a rule is not the only thing a trigger says *when* for.** A question's
 * moments are triggers too, and read by anything but this they would be a second reading of
 * one kind -- two spellings that drift the day either moves. [moved] is what the wave just
 * past moved, and is empty for anybody reading outside a wave, which is what makes a trigger
 * on a change read false there rather than guess.
 */
fun Facts.holds(trigger: Trigger, state: State, moved: List<Move> = emptyList()): Boolean =
    when (trigger) {
        is Trigger.Clock -> clock(trigger.which) >= trigger.ms
        is Trigger.Node -> node(trigger)
        is Trigger.Passages ->
            trigger.at?.let { passage == it }
                ?: (trigger.every!!.let { passage > 0 && passage % it == 0 })
        is Trigger.Judged -> judged(trigger.prose)
        // **It reads a change and never a position**, which is what makes reacting to a life
        // lost composable: the reaction is written once for the scene, whichever rule caused
        // it, instead of being glued onto every rule that takes one.
        is Trigger.Moved -> moved.any {
            it.lever.key == trigger.key &&
                (it.direction == Direction.Harder) == trigger.harder
        }
        is Trigger.Reaches -> state.positions.of(trigger.key) == trigger.position
        // The two that bound the sitting test nothing: their moment is the whole of what
        // they say, and reaching that moment is what makes them hold.
        is Trigger.Opening, is Trigger.Closing -> true
    }

private data class Landed(
    val state: State,
    val notices: List<Notice>,
    val messages: List<Effect.Message>,
    val moves: List<Move>,
    /** What a rule of this wave asked to end, settled after every wave and not here. */
    val finishing: Outcome?,
)
