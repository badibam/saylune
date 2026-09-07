package app.speakup.activity

import app.speakup.conversation.Speaker
import app.speakup.rules.Trigger
import app.speakup.sheets.Branch
import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The shipped files are read here as files, off the same folder the app packs.
 *
 * What is proved is that a definition holds together and that the reader is the store's own --
 * not the contents of any one scene, which is material.
 */
class DefinitionTest {

    private val folder = File("src/main/assets/definitions")

    private fun shipped(id: String) =
        Definitions.parse(id, VERSION, File(folder, "$id.json").readText())

    // ── The files the app ships ─────────────────────────────────────────────────────────

    @Test
    fun `every shipped definition reads`() {
        val files = folder.listFiles { file -> file.extension == "json" }.orEmpty()
        assertTrue("no definition is shipped at all", files.isNotEmpty())
        files.forEach { file -> shipped(file.nameWithoutExtension) }
    }

    /**
     * The free conversation is a delivered definition like any other, which is what takes away
     * a *default* that would have had to be wired somewhere else.
     */
    @Test
    fun `the free conversation is one of them, and leaves every lever where the catalogue puts it`() {
        val free = shipped(Definitions.FREE_CONVERSATION)
        assertEquals(emptyMap<String, Any>(), free.settings.all())
        // The two halves of the brief come from two places here: the file declares who the
        // character is, the learner writes what to talk about -- and its situation is that
        // hole and nothing else, so leaving it blank gives back the themeless conversation.
        assertTrue(free.brief!!.staging.isNotBlank())
        assertEquals(listOf("about"), free.slots.map { it.key })
        assertEquals("{about}", free.brief!!.situation)
        assertEquals(listOf(Speaker.SPEAKUP), free.cast.map { it.key })
        // Nobody is met in a free conversation: the tile is identified by its place.
        assertEquals(null, free.face)
    }

    /**
     * What a hole is worth: **one answer, both sides of the brief**.
     *
     * The situation and the staging are written apart and would drift on the first run if two
     * answers filled them -- a talk on neurophysics and one on gender theory do not make the
     * same speaker.
     */
    @Test
    fun `an answer fills the same hole on both sides of the brief`() {
        val talk = shipped("after-the-talk")
        assertEquals(listOf("subject"), talk.slots.map { it.key })
        val sitting = Activity.from(talk, mapOf("subject" to "quantum computing"))
        assertTrue(sitting.brief!!.situation.contains("A talk on quantum computing"))
        assertTrue(sitting.brief!!.staging.contains("a talk on quantum computing"))
        assertTrue("{subject}" !in sitting.brief!!.situation + sitting.brief!!.staging)
    }

    /**
     * The gender: chosen where the file leaves it open, drawn where nobody chose, and told to
     * the character rather than to the learner.
     */
    @Test
    fun `the main character takes the gender chosen, and one is drawn when none is`() {
        val talk = shipped("after-the-talk")
        assertEquals(true, talk.face?.main)
        assertEquals(null, talk.face?.gender)

        val chosen = Activity.from(talk, gender = "woman")
        assertEquals("woman", chosen.cast.single { it.main }.gender)
        assertTrue(chosen.brief!!.staging.endsWith("You are a woman."))
        // The situation is what the learner reads and he has just answered it: nothing of the
        // gender goes there.
        assertTrue("woman" !in chosen.brief!!.situation)

        val drawn = Activity.from(talk).cast.single { it.main }.gender
        assertTrue(drawn in Activity.GENDERS)
    }

    /**
     * What the weights of the free conversation say: **it looks at everything, and every
     * aptitude counts the same**. A weight being a share among siblings, writing 1 across the
     * tree is what says it.
     */
    @Test
    fun `the free conversation gives every aptitude the same say`() {
        val weights = shipped(Definitions.FREE_CONVERSATION).weights
        Sheets.tree.children.forEach { branch ->
            val share = (branch as Branch).children
                .filterIsInstance<Sheet>()
                .map { weights.of(it) }
                .sum()
            assertEquals(branch.name, 1f / Sheets.tree.children.size, share, 1e-5f)
        }
    }

    // ── What a definition will not hold ─────────────────────────────────────────────────

    @Test
    fun `a file that calls itself something else fails`() {
        assertThrows(IllegalArgumentException::class.java) {
            Definitions.parse("elsewhere", VERSION, minimal())
        }
    }

    @Test
    fun `a character may not take the learner's key`() {
        assertThrows(IllegalArgumentException::class.java) {
            Definitions.parse(
                "some-scene", VERSION,
                minimal(
                    """"cast": [{ "key": "${Speaker.LEARNER}",
                        "short": { "en": "You" } }],""",
                ),
            )
        }
    }

    /** Two names, two budgets: only the short one answers to the status line's ten columns. */
    @Test
    fun `a short name over what the status line leaves fails`() {
        assertThrows(IllegalArgumentException::class.java) {
            Definitions.parse(
                "some-scene", VERSION,
                minimal().replace(
                    """"short": { "en": "Scene" }""",
                    """"short": { "en": "A far longer name" }""",
                ),
            )
        }
    }

    /** The long one has no ceiling: a tile has room a status line does not. */
    @Test
    fun `a long name is not held to the short one's line`() {
        val long = Definitions.parse(
            "some-scene", VERSION,
            minimal().replace(""""title": { "en": "Scene" }""", """"title": { "en": "A far longer name" }"""),
        )
        assertEquals("A far longer name", long.title.inLanguage("en"))
    }

    /** A shape of answer this build does not know fails outright rather than being dropped. */
    @Test
    fun `an unknown shape of answer fails`() {
        assertThrows(IllegalStateException::class.java) {
            Definitions.parse(
                "some-scene", VERSION,
                minimal(
                    """"questions": [{ "key": "k", "ask": "did he?", "answers": "a-number",
                        "when": [{ "kind": "closing", "moment": "Closing" }] }],""",
                ),
            )
        }
    }

    /**
     * A question reads back whole: its moments are triggers like a rule's, and its rung says
     * how far the model may go.
     */
    @Test
    fun `a question carries its moments and its rung`() {
        val scene = Definitions.parse(
            "some-scene", VERSION,
            minimal(
                """"questions": [{ "key": "safe", "ask": "Is the queen safe?",
                    "answers": "one-of",
                    "among": [{ "en": "yes" }, { "en": "no", "fr": "non" }],
                    "rung": "may-extrapolate", "shown": true,
                    "when": [{ "kind": "passages", "moment": "PassageClosed",
                               "at": null, "every": 5 }] }],""",
            ),
        )
        val question = scene.questions.single()
        assertEquals(listOf("yes", "no"), (question.answers as Answers.OneOf).keys)
        assertEquals("non", (question.answers as Answers.OneOf).among[1].inLanguage("fr"))
        assertEquals(Rung.MayExtrapolate, question.rung)
        assertEquals(true, question.shown)
        assertEquals(listOf(Trigger.Passages(every = 5)), question.moments)
    }

    // ── The door ────────────────────────────────────────────────────────────────────────

    /**
     * Every shipped file says where it is offered. It is read by the screens alone, and a file
     * that does not say it is a file nobody can place.
     */
    @Test
    fun `every shipped definition declares a door`() {
        folder.listFiles { file -> file.extension == "json" }.orEmpty().forEach { file ->
            assertTrue(shipped(file.nameWithoutExtension).door in Door.entries)
        }
    }

    @Test
    fun `a door this build does not know fails`() {
        assertThrows(IllegalStateException::class.java) {
            Definitions.parse(
                "some-scene", VERSION,
                minimal().replace(""""door": "free"""", """"door": "the-cellar""""),
            )
        }
    }

    // ── The language table ──────────────────────────────────────────────────────────────

    @Test
    fun `a language the file does not carry reads the English`() {
        val text = Text(mapOf("en" to "Free", "fr" to "Libre"))
        assertEquals("Libre", text.inLanguage("fr"))
        assertEquals("Free", text.inLanguage("de"))
    }

    @Test
    fun `a text with no English at all fails`() {
        assertThrows(IllegalArgumentException::class.java) { Text(mapOf("fr" to "Libre")) }
    }

    /** The whole tree is weighed, so [minimal] can stand in for any scene. */
    private fun minimal(extra: String = "") = """
        {
          "id": "some-scene",
          "title": { "en": "Scene" },
          "short": { "en": "Scene" },
          "door": "free",
          $extra
          "weights": { ${
        Sheets.tree.children.joinToString(",") { branch ->
            (listOf(branch) + (branch as Branch).children)
                .joinToString(",") { """"${Sheets.pathOf(it)}": 1""" }
        }
    } }
        }
    """.trimIndent()

    private companion object {
        const val VERSION = "0.1.0"
    }
}
