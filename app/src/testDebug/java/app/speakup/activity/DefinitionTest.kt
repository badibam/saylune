package app.speakup.activity

import app.speakup.conversation.Speaker
import app.speakup.sheets.Branch
import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        // Its brief comes from the learner, so the file has none to declare.
        assertNull(free.brief)
        assertEquals(listOf(Speaker.SPEAKUP), free.cast.map { it.key })
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

    @Test
    fun `a short title over the line the status line leaves fails`() {
        assertThrows(IllegalArgumentException::class.java) {
            Definitions.parse(
                "some-scene", VERSION,
                minimal().replace(""""en": "Scene"""", """"en": "A far longer title""""),
            )
        }
    }

    /** A shape of answer this build does not know fails outright rather than being dropped. */
    @Test
    fun `an unknown shape of answer fails`() {
        assertThrows(IllegalStateException::class.java) {
            Definitions.parse(
                "some-scene", VERSION,
                minimal(
                    """"questions": [{ "key": "k", "ask": "did he?", "answers": "a-number" }],""",
                ),
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
