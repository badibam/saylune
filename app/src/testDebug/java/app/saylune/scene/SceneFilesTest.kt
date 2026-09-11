package app.saylune.scene

import app.saylune.levers.Positions
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reader, the unfolding of a question and the check, on three scenes of the doc's test set
 * written as files -- the platform (free), the counter (a challenge), the volley (arcade).
 */
class SceneFilesTest {

    private val folder = File("src/testDebug/resources/scenes")

    private fun scene(id: String) =
        SceneFiles.parse(id, "test", File(folder, "$id.json").readText())

    private fun engine(scene: SceneFile) = Engine(scene.cases, scene.events)

    @Test
    fun `the three scenes of the test set read`() {
        listOf("the-platform", "the-counter", "the-volley").forEach { scene(it) }
    }

    /** Written once, a question comes back as a case, a counter, and five events. */
    @Test
    fun `a question unfolds into its case and its events`() {
        val platform = scene("the-platform")
        assertEquals(
            setOf("wait.ask", "wait.took", "wait.again", "wait.last", "wait.spent"),
            platform.events.map { it.key }.filter { it.startsWith("wait.") }.toSet(),
        )
        assertTrue(platform.cases.single { it.key == "wait.left" }.hidden)
    }

    /**
     * Put at passage 12; *neither* is asked again once; *neither* a second time is the answer,
     * and the passage is let go.
     */
    @Test
    fun `a question is asked again, then no answer is the answer`() {
        val platform = scene("the-platform")
        val engine = engine(platform)
        var state = engine.start(Positions())
        val posed = engine.resolve(
            Moment.PassageClose, state, mapOf(AppCases.PASSAGE to Value.Num(12.0)),
        )
        assertEquals("wait", posed.state.awaiting)
        assertEquals("lou", (posed.thread.single().who))
        state = posed.state

        val neither = mapOf("wait" to Value.Pick("neither"))
        val first = engine.resolve(Moment.Answer, state, neither)
        assertEquals(listOf("wait"), first.reasks)
        assertEquals("wait", first.state.awaiting)

        val second = engine.resolve(Moment.Answer, first.state, neither)
        assertTrue(second.reasks.isEmpty())
        assertNull(second.state.awaiting)
    }

    @Test
    fun `an answer lets the passage go at once`() {
        val engine = engine(scene("the-platform"))
        val posed = engine.resolve(
            Moment.PassageClose, engine.start(Positions()), mapOf(AppCases.PASSAGE to Value.Num(12.0)),
        )
        val took = engine.resolve(Moment.Answer, posed.state, mapOf("wait" to Value.Pick("walk")))
        assertNull(took.state.awaiting)
    }

    /** Three closed gates: three lives gone, and the scene lost. */
    @Test
    fun `the counter is lost at zero lives`() {
        val counter = scene("the-counter")
        val engine = engine(counter)
        var state = engine.resolve(Moment.Opening, engine.start(counter.levers)).state
        assertEquals(1, state.instructions.size)
        repeat(3) {
            state = engine.resolve(
                Moment.PassageClose, state, mapOf(AppCases.WORDS_GATE to Value.Flag(true)),
            ).state
        }
        assertEquals(Outcome.Failed, state.ended)
        assertEquals(Value.Num(10.0), engine.read(state, "lever.turn-length"))
    }

    // ── What the check refuses ──────────────────────────────────────────────────────────

    private val base = """
        {"id": "x", "door": "free", "title": "X", "short": "X", "situation": "S",
         "cast": [{"key": "lou", "short": "Lou", "about": "Lou."}]}
    """.trimIndent()

    private fun with(vararg fields: Pair<String, String>): String {
        val json = JSONObject(base)
        fields.forEach { (key, raw) -> json.put(key, JSONObject("{\"v\": $raw}").get("v")) }
        return json.toString()
    }

    private fun refused(json: String, saying: String) {
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            SceneFiles.parse("x", "test", json)
        }
        assertTrue("expected '$saying' in: ${thrown.message}", thrown.message!!.contains(saying))
    }

    @Test
    fun `free sets no lever`() =
        refused(with("levers" to """{"turn-length": 10}"""), "free: a lever")

    @Test
    fun `free reads nothing of how the learner speaks`() = refused(
        with("events" to """[{"key": "e", "at": "passage-close",
            "test": {"case": "app.correctness.note", "op": "<=", "value": "C"},
            "do": [{"tell": "Frown.", "to": "leader"}]}]"""),
        "free: event e reads app.correctness.note",
    )

    @Test
    fun `nobody speaks at the end of an attempt`() = refused(
        with("door" to "\"challenges\"",
             "events" to """[{"key": "e", "at": "attempt-end", "do": [{"direct": "Speak."}]}]"""),
        "makes somebody speak at AttemptEnd",
    )

    @Test
    fun `a hidden case is not asked of the leader`() = refused(
        with("cases" to """[{"key": "c", "kind": "flag", "hidden": true}]""",
             "events" to """[{"key": "e", "at": "passage-close", "do": [{"ask-leader": "c", "reach": "said"}]}]"""),
        "hidden from it",
    )

    @Test
    fun `a case with no start is cited with what to write when empty`() = refused(
        with("situation" to "\"You wished for {wish}.\"",
             "cases" to """[{"key": "wish", "kind": "text", "about": "Their wish"}]""",
             "events" to """[{"key": "e", "at": "launch", "do": [{"ask-learner": "wish", "ask": "Your wish"}]}]"""),
        "without saying what to write",
    )

    @Test
    fun `a switch names an event that exists`() = refused(
        with("events" to """[{"key": "e", "at": "opening", "do": [{"activate": "nobody"}]}]"""),
        "switches nobody",
    )

    @Test
    fun `yes and no make a flag`() = refused(
        with("cases" to """[{"key": "c", "kind": "list", "values": ["yes", "no"], "start": "no", "about": "C"}]"""),
        "is a flag",
    )

    @Test
    fun `no event tests words`() = refused(
        with("cases" to """[{"key": "c", "kind": "text", "start": "a", "about": "C"}]""",
             "events" to """[{"key": "e", "at": "passage-close",
                 "test": {"case": "c", "op": "=", "value": "a"}, "do": [{"tell": "T", "to": "leader"}]}]"""),
        "tests the words of c",
    )

    @Test
    fun `a field the code does not read is not written`() =
        refused(with("mode" to "\"arcade\""), "mode is not a field")
}
