package app.speakup.store

import app.speakup.activity.Activity
import app.speakup.activity.Chosen
import app.speakup.activity.Origin
import app.speakup.activity.Prescriber
import app.speakup.activity.Status
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Levers
import app.speakup.levers.Positions
import app.speakup.rules.Decider
import app.speakup.rules.Effect
import app.speakup.rules.Instructing
import app.speakup.rules.Moment
import app.speakup.rules.Outcome
import app.speakup.rules.Pack
import app.speakup.rules.Rule
import app.speakup.rules.Staging
import app.speakup.rules.Trigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What a sitting keeps of itself, written down and read back.
 *
 * The properties here are the ones step 11 of the plan says it proves: the positions are
 * always **on the line**, the origin only ever groups, and a change of rules engine takes the
 * resumption away.
 */
class SittingTest {

    private val set = Positions(mapOf(
        Levers.CAPTURE.key to At(Levers.ARMED_AND_SENDING),
        Levers.SILENCE_THRESHOLD.key to Count(4),
        // *No maximum* is a position and not an absence, so it has to survive the round trip
        // as one -- read back as a missing key it would answer with the catalogue's default.
        "retakes-allowed" to Count(null),
    ))

    private fun sitting() = Activity(
        matter = "the barman",
        settings = set,
        origin = Origin("pub/opening", "1.4.0"),
        journal = listOf(Chosen("R1", passage = 3, pack = 1, at = 1_700_000L)),
        status = Status.Running,
        createdAt = 1L,
        by = Prescriber.Learner,
    )

    /**
     * **The positions are always on the line, a definition or no definition.** A pointer to
     * follow would make one field sometimes a pointer and sometimes values, and force every
     * reader to settle which before reading.
     */
    @Test
    fun `a sitting from a definition still carries its own positions`() {
        val back = sitting().row().activity()
        assertEquals(set.all(), back.settings.all())
        assertEquals(At(Levers.ARMED_AND_SENDING), back.settings.of(Levers.CAPTURE.key))
    }

    /** And a position that means *no maximum* comes back as that, not as a key nobody set. */
    @Test
    fun `no maximum survives as a position`() {
        val back = sitting().row().activity()
        assertTrue("retakes-allowed" in back.settings.all())
        assertEquals(Count(null), back.settings.of("retakes-allowed"))
    }

    /**
     * **The origin only ever groups** -- opening the next level, filing a score -- and is
     * never consulted to know how the sitting was set. So a sitting read back with its origin
     * dropped is set exactly as it was.
     */
    @Test
    fun `dropping the origin changes nothing about how the sitting was set`() {
        val withOrigin = sitting().row().activity()
        val without = sitting().copy(origin = null).row().activity()
        assertEquals(withOrigin.settings.all(), without.settings.all())
        assertEquals(Origin("pub/opening", "1.4.0"), withOrigin.origin)
    }

    /**
     * **A change of rules engine takes the resumption away** rather than replaying the journal
     * under another semantics, which would give a different state without saying so.
     */
    @Test
    fun `a sitting whose engine has moved is readable and not resumable`() {
        assertTrue(sitting().resumable)
        assertFalse(sitting().copy(engine = Activity.ENGINE - 1).resumable)
    }

    /** A journal with nothing in it has nothing to reread, so nothing can be reread differently. */
    @Test
    fun `an empty journal replays under any semantics`() {
        assertTrue(sitting().copy(journal = emptyList(), engine = 0).resumable)
    }

    /** The journal keeps the choice and nothing else: the effects follow from rule and choice. */
    @Test
    fun `the journal comes back as it went in`() {
        assertEquals(sitting().journal, sitting().row().activity().journal)
    }

    /**
     * A rule with one of each kind of effect, through the store and back. **An unknown name
     * fails outright** rather than being dropped: a rule read back short of an effect is a
     * rule that does something else.
     */
    @Test
    fun `a rule survives every one of its parts`() {
        val rules = listOf(
            Rule(
                key = "R1",
                whenever = Trigger.Reaches(
                    "lives.left", Count(0), Moment.PassageClosed,
                ),
                choice = listOf(Pack(listOf(
                    Effect.Message("the barman looks away", now = true),
                    Effect.Patch(
                        positions = mapOf("lives.left" to Count(1)),
                        moves = mapOf(Levers.SILENCE_THRESHOLD.key to 1),
                        instructions = listOf(Instructing("relevance", "speak in the past", 3)),
                        arming = mapOf("R1" to false, "R2" to true),
                        staging = Staging("a passer-by knocks into you", before = true),
                    ),
                ))),
            ),
            Rule(
                key = "R2",
                whenever = Trigger.Judged("if he oversteps politeness", Moment.EndOfAttempt),
                choice = listOf(
                    Pack(listOf(Effect.Finish(Outcome.Failed))),
                    Pack(listOf(Effect.Finish(Outcome.LetTheNoteDecide))),
                ),
                decider = Decider.Model,
                armed = false,
            ),
        )
        assertEquals(rules, sitting().copy(rules = rules).row().activity().rules)
    }

    /** Every kind of trigger, since each one is read back by its name and never by its rank. */
    @Test
    fun `every kind of trigger comes back as itself`() {
        val triggers = listOf(
            Trigger.Clock(Trigger.Clock.Which.Silence, 5_000),
            Trigger.Node("pronunciation/melody", Trigger.Node.Reads.Note, "D", Moment.EndOfAttempt),
            Trigger.Passages(every = 3),
            Trigger.Passages(at = 10),
            Trigger.Judged("if the room has been booked", Moment.PassageClosed),
            Trigger.Moved("lives.left", harder = true, moment = Moment.PassageClosed),
            Trigger.Reaches("capture", At(Levers.ARMED), Moment.PassageClosed),
        )
        val rules = triggers.mapIndexed { at, trigger ->
            Rule("R$at", trigger, listOf(Pack(listOf(Effect.Finish(Outcome.Passed)))))
        }
        assertEquals(rules, sitting().copy(rules = rules).row().activity().rules)
    }

    /** A conversation nobody has set anything for stores an empty set, not a made-up one. */
    @Test
    fun `a free conversation writes down no position it was never given`() {
        val back = Activity.conversation().row().activity()
        assertEquals(emptyMap<String, At>(), back.settings.all())
        // And it still answers, with the catalogue's declared default.
        assertEquals(At(Levers.BY_HAND), back.settings.of(Levers.CAPTURE.key))
    }

    /**
     * **The learner is a reserved identity and not a case beside the others.** Everything asks
     * an utterance the same question -- who said this -- so a name is what comes back, and
     * only one branch in the whole app turns on it.
     */
    @Test
    fun `a speaker is a name, and the learner's is reserved`() {
        assertTrue(app.speakup.conversation.Speaker.Learner.isLearner)
        assertFalse(app.speakup.conversation.Speaker.Ai.isLearner)
        assertFalse(app.speakup.conversation.Speaker("le-barman").isLearner)
    }
}
