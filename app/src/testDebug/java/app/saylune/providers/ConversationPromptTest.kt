package app.saylune.providers

import app.saylune.activity.Answers
import app.saylune.activity.Brief
import app.saylune.activity.Character
import app.saylune.activity.Text
import app.saylune.activity.Question
import app.saylune.activity.Rung
import app.saylune.chain.Present
import app.saylune.chain.Scene
import app.saylune.rules.Trigger
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
        val alone = Scene(cast = listOf(character("saylune")))
        val several = Scene(cast = listOf(character("vera"), character("barman")))
        assertFalse(ConversationPrompt.activity(alone).contains("Say who is speaking"))
        assertTrue(ConversationPrompt.activity(several).contains("Say who is speaking"))
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

    // ── The questions the app puts ──────────────────────────────────────────────────────

    /**
     * A question is put in part 4, which is rebuilt every turn, because that is what it is:
     * put at one moment and at no other. Its shape and its rung go with it rather than being
     * declared once, a sitting mixing questions of different rungs.
     */
    @Test
    fun `a question is put with its shape and its rung`() {
        val put = ConversationPrompt.present(Present(asking = listOf(
            Question("mood", "How did his talk go?", Answers.Free,
                     moments = listOf(Trigger.Opening), rung = Rung.MayInvent),
        )))
        assertTrue(put.contains("\"mood\""))
        assertTrue(put.contains("How did his talk go?"))
        assertTrue(put.contains("Answer in prose"))
        // The staircase: the floor is repeated at every rung, or invention overrules truth.
        assertTrue(put.contains("Answer from what has been said."))
        assertTrue(put.contains("decide."))
    }

    /** *It does not know* is in the menu at the first rung and at that one alone. */
    @Test
    fun `it does not know is offered at the first rung only`() {
        fun put(rung: Rung) = ConversationPrompt.present(Present(asking = listOf(
            Question("safe", "Is the queen safe?",
                     Answers.OneOf(listOf(Text(mapOf("en" to "yes")), Text(mapOf("en" to "no")))),
                     moments = listOf(Trigger.Closing), rung = rung),
        )))
        assertTrue(put(Rung.FromTheTalk).contains(Question.DONT_KNOW))
        assertFalse(put(Rung.MayExtrapolate).contains(Question.DONT_KNOW))
    }

    /** The field is declared once, in the permanent part, and it comes before the reply. */
    @Test
    fun `what is established is written before the character speaks`() {
        assertTrue(
            ConversationPrompt.APP.indexOf("\"established\"") <
                ConversationPrompt.APP.indexOf("\"spoken\": your reply"),
        )
    }

    private fun character(key: String) = Character(key, Text(mapOf("en" to key)))
}
