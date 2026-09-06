package app.speakup.providers

import app.speakup.activity.Brief
import app.speakup.activity.Character
import app.speakup.activity.Text
import app.speakup.chain.Present
import app.speakup.chain.Scene
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What is proved here is the **layering**, not the wording: which part of the instruction a
 * thing belongs to, and what the app refuses to ask for. The prose itself is material and
 * moves at every bench pass.
 */
class ConversationPromptTest {

    private val staging = "You are Vera, and you are bored of this job."

    // ── Part 1 holds no character ───────────────────────────────────────────────────────

    /**
     * The permanent part is what every activity shares, so a trait of character in it would
     * govern the hostile bouncer as much as the warm partner, and no definition could get out
     * from under it. Who is speaking comes from the definition.
     */
    @Test
    fun `the permanent part carries no persona`() {
        listOf("warm", "curious", "friendly").forEach {
            assertFalse(it, ConversationPrompt.APP.lowercase().contains(" $it"))
        }
    }

    /** What is permanent is the founding gesture, and that does stay. */
    @Test
    fun `the permanent part keeps what never changes`() {
        assertTrue(ConversationPrompt.APP.contains("never interrupt"))
    }

    // ── Part 2 is the scene ─────────────────────────────────────────────────────────────

    @Test
    fun `the staging reaches the model`() {
        assertTrue(ConversationPrompt.activity(Scene(brief = Brief("", staging))).contains(staging))
    }

    /**
     * **The situation is shown and the staging is not**, and the two travel together here
     * because part 2 is the only reader that gets both. What keeps the staging off a screen is
     * elsewhere; what this holds is that neither is dropped on the way to the model.
     */
    @Test
    fun `both halves of the brief reach the model`() {
        val part = ConversationPrompt.activity(Scene(brief = Brief("A hotel desk, late", staging)))
        assertTrue(part.contains("A hotel desk, late"))
        assertTrue(part.contains(staging))
    }

    /** Naming the one voice a free conversation has would say nothing it could use. */
    @Test
    fun `who is speaking is only asked for where there are several`() {
        val alone = Scene(cast = listOf(character("speakup")))
        val several = Scene(cast = listOf(character("vera"), character("barman")))
        assertFalse(ConversationPrompt.activity(alone).contains("Say who is speaking"))
        assertTrue(ConversationPrompt.activity(several).contains("Say who is speaking"))
    }

    // ── A name is asked for once, and only where there is none ──────────────────────────

    /**
     * The rule does not have to know what kind of activity it is looking at, only whether what
     * it has is named: a scene is named by its definition before a word is said, a free
     * conversation until the model finds one.
     */
    @Test
    fun `a name is asked for only while there is none`() {
        assertTrue(ConversationPrompt.activity(Scene(titled = null)).contains("no name yet"))
        val named = ConversationPrompt.activity(Scene(titled = "The interview"))
        assertTrue(named.contains("The interview"))
        assertTrue(named.contains("do not send another"))
    }

    // ── Part 4 says where the conversation stands ───────────────────────────────────────

    @Test
    fun `the passage number is sent every turn`() {
        assertTrue(ConversationPrompt.present(Present(passage = 6)).contains("passage 6"))
    }

    /** It is a fact and not an instruction, and the permanent part is what says so. */
    @Test
    fun `what the passage number means is said once, in the permanent part`() {
        assertTrue(ConversationPrompt.APP.contains("carries no instruction of its own"))
    }

    private fun character(key: String) = Character(key, Text(mapOf("en" to key)))
}
