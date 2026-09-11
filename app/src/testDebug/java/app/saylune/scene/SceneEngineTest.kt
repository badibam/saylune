package app.saylune.scene

import app.saylune.activity.Text
import app.saylune.levers.Count
import app.saylune.levers.Direction
import app.saylune.levers.Positions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import app.saylune.scene.Test as Check

/**
 * The engine of cases and events, proved on the scenes of the doc's test set
 * (`docs/design/scene-state.md`, "Le jeu d'épreuve") rather than looked at on a phone.
 */
class SceneEngineTest {

    private fun en(text: String) = Text(mapOf(Text.BASE to text))
    private fun num(n: Int) = Value.Num(n.toDouble())
    private fun pick(name: String) = Value.Pick(name)
    private val yes = Value.Flag(true)
    private val lives = "lever.lives.left"
    private val counted = Positions(mapOf("lives" to app.saylune.levers.At("counted")))

    // ── Termination ─────────────────────────────────────────────────────────────────────

    /** Each event goes at most once per moment, so two that set each other off still stop. */
    @Test
    fun `two events that set each other off still stop`() {
        val engine = Engine(
            listOf(Case("a", Kind.Number(0.0, 10.0), num(0)), Case("b", Kind.Number(0.0, 10.0), num(0))),
            listOf(
                Event("A", Moment.PassageClose, Check.Goes("b", Way.Up), listOf(Effect.Shift("a", 1.0))),
                Event("B", Moment.PassageClose, Check.Goes("a", Way.Up), listOf(Effect.Shift("b", 1.0))),
                Event("kick", Moment.PassageClose, effects = listOf(Effect.Shift("a", 1.0))),
            ),
        )
        val out = engine.resolve(Moment.PassageClose, engine.start(Positions()))
        assertEquals(setOf("A", "B", "kick"), out.fired)
        assertEquals(num(2), out.state.values["a"])
        assertEquals(num(1), out.state.values["b"])
    }

    // ── The false death ─────────────────────────────────────────────────────────────────

    /**
     * A (active) -- when the lives are at zero, give one back, deactivate A, activate B;
     * B (inactive) -- when they are at zero, finish. **The end is looked at after the waves**,
     * which is what lets A refill the lives in the moment they fell.
     */
    @Test
    fun `the false death gives a life back once, then ends`() {
        val engine = Engine(
            emptyList(),
            listOf(
                Event("cost", Moment.PassageClose, Check.Holds(AppCases.WORDS_GATE, Op.Eq, yes),
                      listOf(Effect.Shift(lives, -1.0))),
                Event("A", Moment.PassageClose, Check.Holds(lives, Op.Eq, num(0)),
                      listOf(Effect.Put(lives, num(1)), Effect.Switch("A", false), Effect.Switch("B", true))),
                Event("B", Moment.PassageClose, Check.Holds(lives, Op.Eq, num(0)),
                      listOf(Effect.Finish(Outcome.Failed)), active = false),
            ),
        )
        var state = engine.start(counted.at("lives.left", Count(1)).first)
        val closed = mapOf(AppCases.WORDS_GATE to yes)

        val first = engine.resolve(Moment.PassageClose, state, closed)
        assertNull(first.state.ended)
        assertEquals(num(1), engine.read(first.state, lives))
        state = first.state

        val second = engine.resolve(Moment.PassageClose, state, closed)
        assertEquals(Outcome.Failed, second.state.ended)
    }

    // ── The barman ──────────────────────────────────────────────────────────────────────

    private val barman = Engine(
        listOf(Case("patience", Kind.Number(0.0, 3.0), num(1), about = "The barman's patience")),
        listOf(
            Event("badly", Moment.PassageClose, Check.Holds(AppCases.WORDS_GATE, Op.Eq, yes),
                  listOf(Effect.Shift(lives, -1.0), Effect.Shift("patience", -1.0),
                         Effect.Tell(en("Reproach them."), Reader.Leader))),
            Event("impatient", Moment.PassageClose, Check.Becomes("patience", Op.Eq, num(0)),
                  listOf(Effect.Put("lever.turn-length", num(10)),
                         Effect.Tell(en("The barman is getting impatient"), Reader.Learner),
                         Effect.Direct("The barman snaps at them."))),
        ),
    )

    /**
     * The words' gate closes: a life lost, patience down, a line to the leader. Patience
     * reaches zero: the turn shortens, and the learner is shown it with the author's line in
     * one notification. **The chain started from the turn, so the directed line is held**,
     * while the lever and the notification are not.
     */
    @Test
    fun `the barman's chain holds the line and shows the lever`() {
        val out = barman.resolve(
            Moment.PassageClose, barman.start(counted), mapOf(AppCases.WORDS_GATE to yes),
        )
        assertEquals(num(2), barman.read(out.state, lives))
        assertEquals(num(10), barman.read(out.state, "lever.turn-length"))
        assertEquals(listOf("Reproach them."), out.leader)
        assertEquals(listOf(Directed("The barman snaps at them.", held = true)), out.directions)

        val impatient = out.notices.filterIsInstance<Notice.Event>().single { it.event == "impatient" }
        assertEquals(1, impatient.moves.size)
        assertEquals(Direction.Harder, impatient.moves.single().direction)
        assertEquals("The barman is getting impatient", impatient.texts.single().inLanguage("en"))

        val line = out.changed.single()
        assertEquals("patience", line.case.key)
        assertEquals(num(1) to num(0), line.from to line.to)
    }

    /** A test on an empty case is false: no analysis ran, so no life is lost for it. */
    @Test
    fun `an empty case tests false`() {
        val engine = Engine(
            emptyList(),
            listOf(Event("cost", Moment.PassageClose,
                         Check.Holds("app.pronunciation.note", Op.Le, pick("C")),
                         listOf(Effect.Shift(lives, -1.0)))),
        )
        val out = engine.resolve(Moment.PassageClose, engine.start(counted))
        assertEquals(num(3), engine.read(out.state, lives))
        val worse = engine.resolve(
            Moment.PassageClose, engine.start(counted), mapOf("app.pronunciation.note" to pick("D")),
        )
        assertEquals(num(2), engine.read(worse.state, lives))
    }

    // ── The casino ──────────────────────────────────────────────────────────────────────

    /** A draw of an event testing the moment alone is laid before any event reads it. */
    @Test
    fun `a draw is read in its own moment whatever the order`() {
        val engine = Engine(
            listOf(Case("hand", Kind.Choice(listOf("lost", "won"))),
                   Case("chips", Kind.Number(min = 0.0), num(0))),
            listOf(
                Event("win", Moment.PassageClose, Check.Holds("hand", Op.Eq, pick("won")),
                      listOf(Effect.Shift("chips", 1.0))),
                Event("deal", Moment.PassageClose,
                      effects = listOf(Effect.Draw("hand", mapOf("won" to 1, "lost" to 2)))),
            ),
        )
        val won = Chance { _, _, _ -> "won" }
        var state = engine.start(Positions())
        repeat(2) { state = engine.resolve(Moment.PassageClose, state, chance = won).state }
        assertEquals(num(2), state.values["chips"])
    }

    // ── Dana ────────────────────────────────────────────────────────────────────────────

    /** Asked at every close until it becomes true; the change goes once, then the ask stops. */
    @Test
    fun `dana is asked until named becomes true`() {
        val engine = Engine(
            listOf(Case("named", Kind.Flag, about = "Whether they named what hurts Dana")),
            listOf(
                Event("ask", Moment.PassageClose,
                      effects = listOf(Effect.AskLeader("named", Reach.Deduce))),
                Event("told", Moment.PassageClose, Check.Becomes("named", Op.Eq, yes),
                      listOf(Effect.Tell(en("Dana said what hurt her"), Reader.Learner),
                             Effect.Switch("ask", false))),
            ),
        )
        var out = engine.resolve(Moment.PassageClose, engine.start(Positions()))
        assertEquals(1, out.asking.size)
        out = engine.resolve(Moment.PassageClose, out.state, mapOf("named" to yes))
        assertTrue("told" in out.fired)
        assertFalse("ask" in out.state.active)
        out = engine.resolve(Moment.PassageClose, out.state, mapOf("named" to yes))
        assertTrue(out.fired.isEmpty())
    }

    // ── Instructions ────────────────────────────────────────────────────────────────────

    /** Three passages are three real passages; the one laid at a close is not aged by it. */
    @Test
    fun `an instruction lasts its passages and says when it ends`() {
        val engine = Engine(
            emptyList(),
            listOf(Event("past", Moment.Opening, effects = listOf(
                Effect.Instruct("past", "relevance/relevance", en("Tell it in the past."), lasts = 3),
            ))),
        )
        var state = engine.resolve(Moment.Opening, engine.start(Positions())).state
        repeat(2) { state = engine.resolve(Moment.PassageClose, state).state }
        assertEquals(1, state.instructions.size)
        val last = engine.resolve(Moment.PassageClose, state)
        assertTrue(last.state.instructions.isEmpty())
        assertEquals(false, (last.notices.single() as Notice.Instruction).on)
    }

    // ── Writing errors ──────────────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `two shifts of one case in one wave are a writing error`() {
        val engine = Engine(
            listOf(Case("p", Kind.Number(0.0, 3.0), num(3))),
            listOf(
                Event("x", Moment.Opening, effects = listOf(Effect.Shift("p", -1.0))),
                Event("y", Moment.Opening, effects = listOf(Effect.Shift("p", -1.0))),
            ),
        )
        engine.resolve(Moment.Opening, engine.start(Positions()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `an event does not write an app case`() {
        val engine = Engine(
            emptyList(),
            listOf(Event("x", Moment.Opening, effects = listOf(Effect.Put(AppCases.WORDS_GATE, yes)))),
        )
        engine.resolve(Moment.Opening, engine.start(Positions()))
    }

    // ── A question waiting ──────────────────────────────────────────────────────────────

    /** While a question is asked again, every event waits but the question's own. */
    @Test
    fun `while a question waits only its events go`() {
        val engine = Engine(
            listOf(Case("way", Kind.Choice(listOf("left", "right", "neither")))),
            listOf(
                Event("clock", Moment.AttemptEnd, effects = listOf(Effect.Tell(en("tick"), Reader.Leader))),
                Event("again", Moment.AttemptEnd, effects = listOf(Effect.AskLeader("way", Reach.Deduce)),
                      question = "way"),
            ),
        )
        val waiting = engine.start(Positions()).copy(awaiting = "way")
        assertEquals(setOf("again"), engine.resolve(Moment.AttemptEnd, waiting).fired)
    }

    // ── Chance ──────────────────────────────────────────────────────────────────────────

    @Test
    fun `a weighted draw keeps to its values`() {
        val chance = Chance.of(kotlin.random.Random(7))
        val drawn = (1..300).map { chance.draw("mood", listOf("calm", "cross"), listOf(2, 1)) }
        assertTrue(drawn.count { it == "calm" } > drawn.count { it == "cross" })
        assertEquals(setOf("calm", "cross"), drawn.toSet())
    }
}
