package app.saylune.store

import app.saylune.activity.Activity
import app.saylune.activity.Origin
import app.saylune.activity.Prescriber
import app.saylune.activity.Shipped
import app.saylune.activity.Status
import app.saylune.activity.Text
import app.saylune.activity.Writer
import app.saylune.activity.Written
import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Levers
import app.saylune.levers.Positions
import app.saylune.scene.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What a sitting keeps of itself, written down and read back: the positions are always **on the
 * line**, the scene is copied there, the origin only ever groups, and a change of engine takes
 * the resumption away.
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
        settings = set,
        scene = """{"id": "pub/opening"}""",
        cast = listOf(Role("barman", Text(mapOf("en" to "Barman")), "You are the barman.",
                           main = true, gender = "man")),
        origin = Origin("pub/opening", "1.4.0"),
        journal = listOf(
            Written("about", "cats", Writer.Learner, 0),
            Written("mood", "cross", Writer.Chance, 3),
        ),
        status = Status.Running,
        createdAt = 1L,
        by = Prescriber.Learner,
    )

    /**
     * **The positions are always on the line, a scene or no scene.** A pointer to follow would
     * make one field sometimes a pointer and sometimes values, and force every reader to settle
     * which before reading.
     */
    @Test
    fun `a sitting from a scene still carries its own positions`() {
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

    /** The scene is copied onto the line, so reopening asks no release for its file. */
    @Test
    fun `the scene comes back as the file it was opened from`() {
        assertEquals(sitting().scene, sitting().row().activity().scene)
    }

    /** The cast comes back whole, descriptions and gender: an utterance names its speaker by key. */
    @Test
    fun `the cast comes back with what each one is`() {
        val back = sitting().row().activity().cast.single()
        assertEquals("barman", back.key)
        assertEquals("You are the barman.", back.about)
        assertEquals("man", back.gender)
        assertTrue(back.main)
    }

    /**
     * **The origin only ever groups** -- opening the next level, filing a score -- and is never
     * consulted to know how the sitting was set.
     */
    @Test
    fun `dropping the origin changes nothing about how the sitting was set`() {
        val withOrigin = sitting().row().activity()
        val without = sitting().copy(origin = null).row().activity()
        assertEquals(withOrigin.settings.all(), without.settings.all())
        assertEquals(Origin("pub/opening", "1.4.0"), withOrigin.origin)
    }

    /** What the learner filled and what chance drew come back with their writer. */
    @Test
    fun `the journal comes back as it went in`() {
        assertEquals(sitting().journal, sitting().row().activity().journal)
        assertEquals(mapOf("about" to "cats"), sitting().filled)
    }

    /**
     * **A change of engine takes the resumption away** rather than replaying under another
     * semantics, which would give a different state without saying so.
     */
    @Test
    fun `a sitting whose engine has moved is readable and not resumable`() {
        assertTrue(sitting().resumable)
        assertFalse(sitting().copy(engine = Activity.ENGINE - 1).resumable)
    }

    /** And one with no scene on its line was opened before scenes were files. */
    @Test
    fun `a sitting with no scene on its line does not carry on`() {
        assertFalse(sitting().copy(scene = null).resumable)
    }

    /** A conversation nobody has set anything for stores an empty set, not a made-up one. */
    @Test
    fun `a free conversation writes down no position it was never given`() {
        val back = Shipped.freeConversation().row().activity()
        assertEquals(emptyMap<String, At>(), back.settings.all())
        // And it still answers, with the catalogue's declared default.
        assertEquals(At(Levers.BY_HAND), back.settings.of(Levers.CAPTURE.key))
    }

    /**
     * **The learner is a reserved identity and not a case beside the others.** Everything asks
     * an utterance the same question -- who said this -- so a name is what comes back, and only
     * one branch in the whole app turns on it.
     */
    @Test
    fun `a speaker is a name, and the learner's is reserved`() {
        assertTrue(app.saylune.conversation.Speaker.Learner.isLearner)
        assertFalse(app.saylune.conversation.Speaker.Ai.isLearner)
        assertFalse(app.saylune.conversation.Speaker("le-barman").isLearner)
    }
}
