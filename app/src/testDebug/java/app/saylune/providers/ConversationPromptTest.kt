package app.saylune.providers

import app.saylune.activity.Text
import app.saylune.chain.Asked
import app.saylune.chain.Present
import app.saylune.chain.Scene
import app.saylune.scene.Kind
import app.saylune.scene.Reach
import app.saylune.scene.Role
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What is proved here is the **layering**, not the wording: which part of the instruction a
 * thing belongs to, and what the app refuses to ask for. The prose itself is material and
 * moves at every bench pass.
 */
class ConversationPromptTest {

    private val about = "You are Vera, and you are bored of this job."

    private fun role(key: String, about: String = "About $key.") =
        Role(key, Text(mapOf("en" to key)), about)

    // ── Part 1 holds no character ───────────────────────────────────────────────────────

    /**
     * The permanent part is what every activity shares, so a trait of character in it would
     * govern the hostile bouncer as much as the warm partner, and no scene could get out from
     * under it. Who is speaking comes from the file.
     */
    @Test
    fun `the permanent part carries no persona`() {
        listOf("warm", "curious", "friendly").forEach {
            assertFalse(it, ConversationPrompt.SPEAKING.lowercase().contains(" $it"))
        }
    }

    /** What is permanent is the founding gesture, and that does stay. */
    @Test
    fun `the permanent part keeps what never changes`() {
        assertTrue(ConversationPrompt.SPEAKING.contains("never interrupt"))
    }

    /**
     * **A turn nobody prompted names only fields the contract declares.** The field of the
     * turn was renamed and this instruction kept the old name, so every turn the character
     * took of its own accord was written under a key the reader does not know.
     */
    @Test
    fun `the provoked turn names only declared fields`() {
        val declared = Regex("""(?m)^\s*"(\w+)":""").findAll(ConversationPrompt.SPEAKING)
            .map { it.groupValues[1] }.toSet()
        val named = Regex(""""(\w+)"""").findAll(ConversationPrompt.PROVOKED)
            .map { it.groupValues[1] }.toSet()
        assertTrue("$named against $declared", declared.containsAll(named))
        assertTrue(named.contains("said"))
    }

    // ── Part 2 is the scene ─────────────────────────────────────────────────────────────

    /** A description belongs to whoever it describes, and it goes out with them. */
    @Test
    fun `a character's description reaches the model`() {
        assertTrue(ConversationPrompt.activity(Scene(cast = listOf(role("vera", about))))
                       .contains(about))
    }

    /**
     * **The situation is shown and the description is not**, and the two travel together here
     * because part 2 is the only reader that gets both. What keeps the description off a screen
     * is elsewhere; what this holds is that neither is dropped on the way to the model.
     */
    @Test
    fun `the situation and the description both reach the model`() {
        val part = ConversationPrompt.activity(
            Scene(situation = "A hotel desk, late", cast = listOf(role("vera", about))),
        )
        assertTrue(part.contains("A hotel desk, late"))
        assertTrue(part.contains(about))
    }

    /** Naming the one voice a free conversation has would say nothing it could use. */
    @Test
    fun `who is speaking is only asked for where there are several`() {
        val alone = Scene(cast = listOf(role("saylune")))
        val several = Scene(cast = listOf(role("vera"), role("barman")))
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
        assertTrue(ConversationPrompt.SPEAKING.contains("carries no instruction of its own"))
    }

    // ── The cases the app asks the leader for ───────────────────────────────────────────

    /**
     * A case is asked for in part 4, which is rebuilt every turn, because that is what it is:
     * asked at one moment and at no other. What it holds and how far the leader may go go with
     * it rather than being declared once, a sitting mixing cases of every kind.
     */
    @Test
    fun `a case is asked for with what it holds and how far the leader may go`() {
        val put = ConversationPrompt.present(Present(asking = listOf(
            Asked("mood", "How his talk went", Kind.Words, Reach.Invent),
        )))
        assertTrue(put.contains("\"mood\""))
        assertTrue(put.contains("How his talk went"))
        assertTrue(put.contains("Answer in prose"))
        // The staircase: the floor is repeated at every step, or invention overrules truth.
        assertTrue(put.contains("Answer from what has been said."))
        assertTrue(put.contains("decide."))
    }

    /** *It does not know* is in the menu at the first step and at that one alone. */
    @Test
    fun `it does not know is offered at the first step only`() {
        fun put(reach: Reach) = ConversationPrompt.present(Present(asking = listOf(
            Asked("safe", "Whether the queen is safe", Kind.Choice(listOf("yes", "no")), reach),
        )))
        assertTrue(put(Reach.Said).contains(Asked.DONT_KNOW))
        assertFalse(put(Reach.Deduce).contains(Asked.DONT_KNOW))
    }

    /** A number says its bounds, which is what makes an answer outside them refusable. */
    @Test
    fun `a number is asked for with its bounds`() {
        val put = ConversationPrompt.present(Present(asking = listOf(
            Asked("glasses", "How many glasses he has poured", Kind.Number(0.0, 10.0), Reach.Deduce),
        )))
        assertTrue(put.contains("0 at the least"))
        assertTrue(put.contains("10 at the most"))
    }

    /**
     * The field is declared once, in the permanent part, and it comes before the reply.
     *
     * Anchored on the declaration and not on its wording: a field is declared at the head of
     * its own line, where "said" is also named mid-sentence by the echo's description.
     */
    @Test
    fun `what is established is written before the character speaks`() {
        assertTrue(
            ConversationPrompt.SPEAKING.indexOf("\n\"established\":") <
                ConversationPrompt.SPEAKING.indexOf("\n\"said\":"),
        )
    }

    /**
     * The echo is the opening of the utterance, so it is written before what follows it: asked
     * for the continuation first, the model has to write an opening for a sentence it has
     * already finished.
     */
    @Test
    fun `the echo is written before the continuation it opens`() {
        assertTrue(
            ConversationPrompt.SPEAKING.indexOf("\n\"echo\":") <
                ConversationPrompt.SPEAKING.indexOf("\n\"said\":"),
        )
    }

    // ── The judge plays nobody ──────────────────────────────────────────────────────────

    /**
     * **What the split closes, and it closes by construction.** The description addresses the
     * character alone and used to ride in the judge's context, held at arm's length by a
     * sentence of prose that nothing checked; the same went for what the learner asked to be
     * steered around. Neither has a parameter to travel on now.
     */
    @Test
    fun `the judge is given the situation and nothing else of the scene`() {
        val head = ConversationPrompt.judging("A hotel desk, late")
        assertTrue(head.contains("A hotel desk, late"))
        assertFalse(head.contains(about))
        assertFalse(head.contains("Vera"))
    }

    /** It marks; it does not answer. Nothing in its instruction asks it for a reply. */
    @Test
    fun `the judge is never asked for anything to say`() {
        assertFalse(ConversationPrompt.JUDGING.contains("\"spoken\""))
        assertFalse(ConversationPrompt.JUDGING.contains("\"echo\""))
        assertTrue(ConversationPrompt.JUDGING.contains("take no part in it"))
    }

    /** And the one who speaks no longer marks: the four markings left with the second call. */
    @Test
    fun `the one who speaks is never asked for a marking`() {
        listOf("\"spans\"", "\"stumbling\"", "\"following\"", "\"reach\"", "\"difficulty\"")
            .forEach { assertFalse(it, ConversationPrompt.SPEAKING.contains(it)) }
    }

    /**
     * The reply is the last thing the judge reads, and the parade against the one defect the
     * split introduces: a repair heard with no mark on screen.
     */
    @Test
    fun `the judge is shown the reply that was given, last`() {
        val message = ConversationPrompt.judged(
            history = emptyList(),
            said = "I have twenty five years",
            answered = "Ah, you're twenty-five! And where do you work?",
            present = Present(),
        )
        assertTrue(message.contains("I have twenty five years"))
        assertTrue(message.indexOf("I have twenty five years") <
                       message.indexOf("Ah, you're twenty-five!"))
    }

    /** The passage number says how the character is to play, so it stops at the judge. */
    @Test
    fun `the judge is told nothing of how the character plays`() {
        val message = ConversationPrompt.judged(
            emptyList(), "I go there yesterday", "Ah, yesterday!", Present(passage = 6),
        )
        assertFalse(message.contains("passage 6"))
    }
}
