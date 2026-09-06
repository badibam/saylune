package app.speakup.rules

import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Levers
import app.speakup.levers.Positions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The interpreter, proved rather than looked at.
 *
 * This is pure logic in plain JVM, which is why it gets tests at all where the rest of the
 * project leans on its benches: a bench *is* a test for measurement, and resolving by waves is
 * not measurement. What is proved is the termination, the writing error the form exists to
 * expose, and the three cases the doc says the shape was chosen for.
 */
class EngineTest {

    private val lives = Engine.LIVES

    private fun start(vararg armed: String, at: Positions = Positions()) =
        State(positions = at, armed = armed.toSet())

    private fun patch(
        positions: Map<String, app.speakup.levers.Position> = emptyMap(),
        moves: Map<String, Int> = emptyMap(),
        arming: Map<String, Boolean> = emptyMap(),
        staging: Staging? = null,
    ) = Pack(listOf(Effect.Patch(positions, moves, emptyList(), arming, staging)))

    /** Fires on nothing unless asked; a rule's own trigger says what it wants. */
    private object Quiet : Facts

    private class AtPassage(override val passage: Int) : Facts

    // ── Termination ─────────────────────────────────────────────────────────────────────

    /**
     * **A rule fires at most once per moment, and that bound is what makes it terminate.**
     * There are finitely many rules and each goes at most once, so the cascade stops even
     * where two rules set each other off -- which without the bound is a loop with nothing to
     * break it.
     */
    @Test
    fun `two rules that set each other off still stop`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1),
                 listOf(patch(arming = mapOf("B" to true)))),
            Rule("B", Trigger.Passages(every = 1),
                 listOf(patch(arming = mapOf("A" to true))), armed = false),
        ))
        // It returns at all, which is the property; and both have fired by the time it does.
        val out = engine.resolve(Moment.PassageClosed, start("A"), AtPassage(1))
        assertTrue("A" in out.state.armed && "B" in out.state.armed)
    }

    /**
     * And a rule that arms itself does not go round again inside the same moment: the bound is
     * per moment, so this is what would otherwise never end.
     */
    @Test
    fun `a rule that rearms itself fires once in the moment`() {
        var landed = 0
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1),
                 listOf(patch(moves = mapOf(lives to 1), arming = mapOf("A" to true)))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("A", at = Positions(mapOf(lives to Count(3)))),
            AtPassage(1),
        )
        landed = out.notices.count { it is Notice.Moved }
        assertEquals(1, landed)
    }

    // ── The writing error the form exists to expose ─────────────────────────────────────

    /**
     * **Two patches of one wave on the same key is a writing error, not a case to arbitrate.**
     * Two moves, or a move and a position, cannot be read: what they compose to depends on
     * where one started. Letting a priority settle it is exactly what makes a definition
     * unreadable without running it.
     */
    @Test
    fun `two patches of one wave on one key fail outright`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), listOf(patch(moves = mapOf(lives to -1)))),
            Rule("B", Trigger.Passages(every = 1), listOf(patch(moves = mapOf(lives to -1)))),
        ))
        val failed = runCatching {
            engine.resolve(
                Moment.PassageClosed,
                start("A", "B", at = Positions(mapOf(lives to Count(3)))),
                AtPassage(1),
            )
        }.exceptionOrNull()
        assertTrue("it went through", failed != null)
        assertTrue("$failed", failed!!.message!!.contains(lives))
    }

    /**
     * **Except where both lay the same absolute position**, which is the one case the
     * coincidence is visible without running anything.
     */
    @Test
    fun `two patches laying the same position agree and go through`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1),
                 listOf(patch(positions = mapOf(lives to Count(1))))),
            Rule("B", Trigger.Passages(every = 1),
                 listOf(patch(positions = mapOf(lives to Count(1))))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("A", "B", at = Positions(mapOf(lives to Count(3)))),
            AtPassage(1),
        )
        assertEquals(Count(1), out.state.positions.of(lives))
    }

    // ── The scripted death ──────────────────────────────────────────────────────────────

    /**
     * **The one case the shape was chosen for.** Zero lives ends the sitting, and that is read
     * **once, after every wave, on the settled state** -- never on the transition. On the
     * transition the ending would always win the race against the rule that refills the lives,
     * and a boss one cannot beat, whose ending is a false death one walks through, would be
     * unwritable.
     */
    @Test
    fun `a scripted death is walked through rather than ending the sitting`() {
        val engine = Engine(listOf(
            Rule("R1", Trigger.Reaches(lives, Count(0), Moment.PassageClosed), listOf(Pack(listOf(
                Effect.Message("the barman looks away"),
                Effect.Patch(
                    positions = mapOf(lives to Count(1)),
                    arming = mapOf("R1" to false, "R2" to true),
                ),
            )))),
            Rule("R2", Trigger.Reaches(lives, Count(0), Moment.PassageClosed),
                 listOf(Pack(listOf(Effect.Finish(Outcome.Failed)))), armed = false),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("R1", at = Positions(mapOf(lives to Count(0)))),
            Quiet,
        )
        assertNull("the sitting ended anyway", out.state.ended)
        assertEquals(Count(1), out.state.positions.of(lives))
        assertEquals(listOf("the barman looks away"), out.messages.map { it.prose })
    }

    /** And the second time round, with R2 armed and R1 not, the ending falls. */
    @Test
    fun `once the false death is spent the real one ends it`() {
        val engine = Engine(listOf(
            Rule("R1", Trigger.Reaches(lives, Count(0), Moment.PassageClosed),
                 listOf(patch(positions = mapOf(lives to Count(1)))), armed = false),
            Rule("R2", Trigger.Reaches(lives, Count(0), Moment.PassageClosed),
                 listOf(Pack(listOf(Effect.Finish(Outcome.Failed))))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("R2", at = Positions(mapOf(lives to Count(0)))),
            Quiet,
        )
        assertEquals(Outcome.Failed, out.state.ended)
    }

    /** Zero lives ends it even where no rule says so: it is a property of the lever. */
    @Test
    fun `zero lives ends the sitting with no rule to say it`() {
        val out = Engine(emptyList()).resolve(
            Moment.PassageClosed,
            start(at = Positions(mapOf(lives to Count(0)))),
            Quiet,
        )
        assertEquals(Outcome.Failed, out.state.ended)
    }

    // ── A move that changed nothing ─────────────────────────────────────────────────────

    /**
     * **A move stops at the lever's bound, which is not an error** -- a ramp reaches its top
     * notch by design -- **and what changed nothing announces nothing**, saying *this tightens*
     * with nothing tightened being a lie.
     */
    @Test
    fun `a move already at the bound notifies nothing`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1),
                 listOf(patch(moves = mapOf(Levers.CAPTURE.key to 1)))),
        ))
        val top = Positions(mapOf(Levers.CAPTURE.key to At(Levers.ARMED_AND_SENDING)))
        val out = engine.resolve(Moment.PassageClosed, start("A", at = top), AtPassage(1))
        assertEquals(emptyList<Notice>(), out.notices)
        assertEquals(At(Levers.ARMED_AND_SENDING), out.state.positions.of(Levers.CAPTURE.key))
    }

    /** And a move that did something says so, with the way it went. */
    @Test
    fun `a move that landed says which way it went`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1),
                 listOf(patch(moves = mapOf(Levers.CAPTURE.key to 1)))),
        ))
        val out = engine.resolve(Moment.PassageClosed, start("A"), AtPassage(1))
        val moved = out.notices.filterIsInstance<Notice.Moved>().single()
        assertEquals(app.speakup.levers.Direction.Harder, moved.move.direction)
    }

    // ── The overload ────────────────────────────────────────────────────────────────────

    /**
     * **The overload is two rules of which exactly one is armed at any instant**, and that
     * saves two mechanisms: no cap on firings, and no declaration order with any meaning to
     * carry.
     *
     * This is the test that found the contradiction (2026-09-06). Let arming open a wave and
     * the rule just armed fires in the same breath as the one that armed it, so both phrases
     * land on the first passage and the overload gives *both, then the other* instead of *one,
     * then the other*.
     */
    @Test
    fun `one occasion does one thing then another, with no cap and no ordering`() {
        val rules = listOf(
            Rule("first", Trigger.Passages(every = 1),
                 listOf(patch(staging = Staging("the barman raises an eyebrow"),
                              arming = mapOf("first" to false, "then" to true)))),
            Rule("then", Trigger.Passages(every = 1),
                 listOf(patch(staging = Staging("the barman turns his back"))), armed = false),
        )
        val engine = Engine(rules)
        var state = start("first")
        val said = mutableListOf<String>()
        repeat(3) {
            val out = engine.resolve(Moment.PassageClosed, state, AtPassage(it + 1))
            state = out.state
            said += out.notices.filterIsInstance<Notice.Staged>().map { s -> s.staging.text }
        }
        assertEquals(
            listOf(
                "the barman raises an eyebrow",
                "the barman turns his back",
                "the barman turns his back",
            ),
            said,
        )
    }

    // ── Reading a change rather than a position ─────────────────────────────────────────

    /**
     * **The trigger that reads a change is what makes the front door composable.** Reacting to
     * a life lost would otherwise mean gluing the same message onto every rule that takes one,
     * so rewriting it as many times as there are ways to lose one.
     *
     * And this is the test that says why there are waves at all: the life is taken **during**
     * this moment, so with a single wave nobody rereads and the barman reacts a passage late.
     * Measured -- collapsing the loop to one pass fails this and nothing else.
     */
    @Test
    fun `a rule reacts to a life lost whichever rule took it`() {
        val engine = Engine(listOf(
            Rule("takes", Trigger.Passages(every = 1),
                 listOf(patch(moves = mapOf(lives to 1)))),
            Rule("reacts", Trigger.Moved(lives, harder = true, moment = Moment.PassageClosed),
                 listOf(Pack(listOf(Effect.Message("the barman has noticed"))))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("takes", "reacts", at = Positions(mapOf(lives to Count(3)))),
            AtPassage(1),
        )
        assertEquals(listOf("the barman has noticed"), out.messages.map { it.prose })
    }

    /** And it reads a change and never a position: standing at a value sets nothing off. */
    @Test
    fun `a lever already there has not moved`() {
        val engine = Engine(listOf(
            Rule("reacts", Trigger.Moved(lives, harder = true, moment = Moment.PassageClosed),
                 listOf(Pack(listOf(Effect.Message("the barman has noticed"))))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("reacts", at = Positions(mapOf(lives to Count(1)))),
            Quiet,
        )
        assertEquals(emptyList<String>(), out.messages.map { it.prose })
    }

    // ── The menu ────────────────────────────────────────────────────────────────────────

    /**
     * **The menu is computed, not maintained**: it is the packs the rules firing now put on
     * offer, and on most turns it is empty and nothing is sent.
     */
    @Test
    fun `the menu is empty when nothing offers a choice`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), listOf(patch(moves = mapOf(lives to 1)))),
        ))
        assertEquals(emptyList<Offer>(), engine.menu(Moment.PassageClosed, start("A"), AtPassage(1)))
    }

    @Test
    fun `the menu offers only what a rule firing now put up`() {
        val offered = listOf(
            Pack(listOf(Effect.Message("he softens"))),
            Pack(listOf(Effect.Message("he hardens"))),
        )
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), offered, decider = Decider.Model),
            Rule("later", Trigger.Passages(at = 9), offered, decider = Decider.Model),
        ))
        val menu = engine.menu(Moment.PassageClosed, start("A", "later"), AtPassage(1))
        assertEquals(listOf(Offer("A", offered)), menu)
    }

    /** What the model took is written down, since a choice is what does not come back on its own. */
    @Test
    fun `what a chooser took goes to the journal`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), listOf(
                Pack(listOf(Effect.Message("he softens"))),
                Pack(listOf(Effect.Message("he hardens"))),
            ), decider = Decider.Model),
        ))
        val out = engine.resolve(
            Moment.PassageClosed, start("A"),
            object : Facts {
                override val passage = 1
                override fun chose(rule: Rule) = 1
            },
        )
        assertEquals(listOf(Choice("A", 1)), out.chosen)
        assertEquals(listOf("he hardens"), out.messages.map { it.prose })
    }

    /** A rule the written data decides has nothing to write down: there was no choice. */
    @Test
    fun `a written rule leaves no trace in the journal`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), listOf(patch(moves = mapOf(lives to 1)))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("A", at = Positions(mapOf(lives to Count(3)))),
            AtPassage(1),
        )
        assertEquals(emptyList<Choice>(), out.chosen)
    }

    // ── The moments ─────────────────────────────────────────────────────────────────────

    /** A rule of another moment does not fire: the moments differ by what exists at that instant. */
    @Test
    fun `only the rules of this moment fire`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Clock(Trigger.Clock.Which.Silence, 0), listOf(
                Pack(listOf(Effect.Message("too quiet"))),
            )),
        ))
        val out = engine.resolve(Moment.PassageClosed, start("A"), Quiet)
        assertEquals(emptyList<String>(), out.messages.map { it.prose })
    }

    // ── Instructions ────────────────────────────────────────────────────────────────────

    /** A marking carries several at once, each with its own life. */
    @Test
    fun `two instructions on one marking coexist`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), listOf(Pack(listOf(Effect.Patch(
                instructions = listOf(Instructing("pertinence", "speak in the past", 2)),
            ))))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("A", at = Positions()).copy(
                instructions = listOf(Instructing("pertinence", "keep it formal")),
            ),
            AtPassage(1),
        )
        assertEquals(2, out.state.instructions.size)
    }

    /** One with a life runs out between passages; one with none stays for the sitting. */
    @Test
    fun `an instruction with a life expires and a lasting one does not`() {
        val engine = Engine(emptyList())
        var state = start().copy(instructions = listOf(
            Instructing("pertinence", "speak in the past", 2),
            Instructing("suivi", "keep it formal"),
        ))
        state = engine.aged(state)
        assertEquals(2, state.instructions.size)
        state = engine.aged(state)
        assertEquals(listOf("keep it formal"), state.instructions.map { it.text })
    }

    /** A null text takes off what was laid on that marking. */
    @Test
    fun `an instruction can be taken off`() {
        val engine = Engine(listOf(
            Rule("A", Trigger.Passages(every = 1), listOf(Pack(listOf(Effect.Patch(
                instructions = listOf(Instructing("pertinence", null)),
            ))))),
        ))
        val out = engine.resolve(
            Moment.PassageClosed,
            start("A").copy(instructions = listOf(
                Instructing("pertinence", "speak in the past"),
                Instructing("suivi", "answer the question"),
            )),
            AtPassage(1),
        )
        assertEquals(listOf("suivi"), out.state.instructions.map { it.marking })
    }
}
