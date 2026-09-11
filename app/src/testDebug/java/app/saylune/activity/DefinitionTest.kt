package app.saylune.activity

import app.saylune.conversation.Speaker
import app.saylune.scene.SceneFiles
import app.saylune.scene.Value
import app.saylune.sheets.Branch
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The shipped scenes are read here as files, off the same folder the app packs.
 *
 * What is proved is that a scene holds together and that a sitting takes from it what it keeps
 * -- not the contents of any one scene, which is material.
 */
class DefinitionTest {

    private val folder = File("src/main/assets/definitions")

    private fun shipped(id: String) = Shipped.definition(id)

    @Test
    fun `every shipped scene reads`() {
        val files = folder.listFiles { file -> file.extension == "json" }.orEmpty()
        assertTrue("no scene is shipped at all", files.isNotEmpty())
        files.forEach { file -> shipped(file.nameWithoutExtension) }
    }

    /**
     * The free conversation is a shipped scene like any other, which is what takes away a
     * *default* that would have had to be wired somewhere else: it sets no lever, so every one
     * of them answers with the catalogue's own.
     */
    @Test
    fun `the free conversation leaves every lever where the catalogue puts it`() {
        val free = shipped(Definitions.FREE_CONVERSATION)
        assertEquals(emptyMap<String, Any>(), free.levers.all())
        assertEquals(listOf(Speaker.SAYLUNE), free.cast.map { it.key })
        // Nobody is met in a free conversation: the tile is identified by its place.
        assertNull(free.face)
        assertTrue(free.cast.single().about.isNotBlank())
    }

    /**
     * **Its situation is the hole and nothing else**, so leaving it blank gives back the
     * themeless conversation rather than a sentence about an empty subject.
     */
    @Test
    fun `the free conversation's situation is what the learner typed, or nothing`() {
        val free = shipped(Definitions.FREE_CONVERSATION)
        assertEquals(listOf("about"), free.holes.map { it.case })
        val said = free.situation.inLanguage(Text.BASE)
        assertEquals("cats", SceneFiles.cite(said) { Value.Words("cats") }.trim())
        assertEquals("", SceneFiles.cite(said) { null }.trim())
    }

    /**
     * The gender: chosen where the file leaves it open, drawn where nobody chose, and told to
     * the character in its own description, which the learner never reads.
     */
    @Test
    fun `the main character takes the gender chosen, and one is drawn when none is`() {
        val scene = SceneFiles.parse(
            "the-platform", "test",
            File("src/testDebug/resources/scenes/the-platform.json").readText(),
        )
        assertEquals(true, scene.face?.main)
        assertNull(scene.face?.gender)

        val chosen = Activity.from(scene, gender = "woman")
        val main = chosen.cast.single { it.main }
        assertEquals("woman", main.gender)
        assertTrue(main.about.endsWith("You are a woman."))

        val drawn = Activity.from(scene).cast.single { it.main }.gender
        assertTrue(drawn in Activity.GENDERS)
    }

    /**
     * What a hole is worth: **one answer, on the sitting's line**, cited wherever the scene
     * names it. Written apart, a situation and a description would drift on the first run.
     */
    @Test
    fun `what the learner fills is kept on the sitting, by case`() {
        val sitting = Activity.from(Shipped.free(), mapOf("about" to " cats "))
        assertEquals(mapOf("about" to "cats"), sitting.filled)
        // A blank answer is left out, the case staying empty rather than holding nothing.
        assertEquals(emptyMap<String, String>(), Activity.from(Shipped.free(), mapOf("about" to " ")).filled)
    }

    /**
     * What the weights of the free conversation say: **it looks at everything, and every
     * aptitude counts the same**. A weight being a share among siblings, 1 across the tree is
     * what says it -- and behind the free door the app puts it there, a file declaring none.
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
}
