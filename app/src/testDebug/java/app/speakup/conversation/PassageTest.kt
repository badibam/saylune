package app.speakup.conversation

import app.speakup.activity.Activity
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Positions
import app.speakup.notes.Measured
import app.speakup.notes.Passage as Scored
import app.speakup.notes.Weights
import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The passage, its attempts, and the two gates.
 *
 * What is proved is the shape and the ordering, never the numbers: every series and every
 * column in the catalogue is a first pass the calibration bench takes over, so a test that
 * pinned one would only say the bench had not run yet.
 */
class PassageTest {

    private var next = 0

    private fun said(text: String, repeats: String? = null, attempt: Attempt? = null) =
        Utterance(
            speaker = Speaker.Learner, text = text, activity = "a",
            repeats = repeats, attempt = attempt, id = "u${next++}",
        )

    private fun replied(to: String, text: String) = Utterance(
        speaker = Speaker.Ai, text = text, activity = "a", answers = to, id = "u${next++}",
    )

    private fun state(vararg run: Utterance) =
        ConversationState(activity = Activity.conversation(), utterances = run.toList())

    // ── The passage, derived ────────────────────────────────────────────────────────────

    /** An utterance that repeats nothing opens one; what points back at it are its attempts. */
    @Test
    fun `a passage is an utterance and every attempt at it`() {
        val opener = said("I have 25 years")
        val again = said("I am 25 years old", repeats = opener.id, attempt = Attempt.Rewording)
        val passages = Passage.of(listOf(opener, replied(opener.id, "Ah"), again))
        assertEquals(1, passages.size)
        assertEquals(listOf(opener, again), passages.single().attempts)
        assertEquals(again, passages.single().last)
    }

    /**
     * **Two counters, one per kind of repair, and they do not steal from each other**: a
     * challenge aiming only at pronunciation keeps its repeats intact whatever happens on the
     * words' side.
     */
    @Test
    fun `rewordings and repeats do not steal from each other`() {
        val opener = said("hello")
        val passage = Passage(opener, listOf(
            opener,
            said("hello there", repeats = opener.id, attempt = Attempt.Rewording),
            said("hello there", repeats = opener.id, attempt = Attempt.Repeat),
            said("hello there", repeats = opener.id, attempt = Attempt.Repeat),
        ))
        assertEquals(1, passage.rewordings)
        assertEquals(2, passage.repeats)
        val settings = Positions(mapOf(
            Attempt.Rewording.lever to Count(1),
            Attempt.Repeat.lever to Count(3),
        ))
        assertFalse(passage.spare(Attempt.Rewording, settings))
        assertTrue(passage.spare(Attempt.Repeat, settings))
    }

    /** *No maximum* is a position, and it spares without counting. */
    @Test
    fun `no maximum always has one more`() {
        val opener = said("hello")
        val passage = Passage(opener, listOf(opener) + (1..40).map {
            said("hello", repeats = opener.id, attempt = Attempt.Repeat)
        })
        assertTrue(passage.spare(Attempt.Repeat, Positions()))
    }

    // ── The history ─────────────────────────────────────────────────────────────────────

    /**
     * **The whole property of the relaunch**: the history never carries a reply and a sentence
     * that do not answer each other.
     *
     * A rewording makes a fresh call as though it were the first attempt, so the new reply
     * answers the new attempt and the old one stops being the passage's last -- while staying
     * in the store, superseded rather than deleted.
     */
    @Test
    fun `a reworded passage sends the new sentence with the reply made to it`() {
        val opener = said("I have 25 years")
        val stale = replied(opener.id, "How long have you had them?")
        val again = said("I am 25 years old", repeats = opener.id, attempt = Attempt.Rewording)
        val fresh = replied(again.id, "Ah, and where do you live?")
        val history = state(opener, stale, again, fresh).history()
        assertEquals(
            listOf("I am 25 years old", "Ah, and where do you live?"),
            history.map { it.text },
        )
    }

    /** And the superseded reply is still in the run: it is a fact about the sitting. */
    @Test
    fun `the reply taken out of the thread stays in the store`() {
        val opener = said("I have 25 years")
        val stale = replied(opener.id, "How long have you had them?")
        val again = said("I am 25", repeats = opener.id, attempt = Attempt.Rewording)
        val run = state(opener, stale, again, replied(again.id, "Ah"))
        assertTrue(stale in run.utterances)
    }

    /**
     * **A repeat never reaches the language model** -- pipe B alone, on a text already settled
     * -- and what the model sees of a passage is the last attempt, like the screen: the
     * character has no business knowing the sentence was said three times.
     */
    @Test
    fun `repeats never reach the model and the reply is kept`() {
        val opener = said("I am twenty five")
        val answer = replied(opener.id, "Nice")
        val once = said("I am twenty five", repeats = opener.id, attempt = Attempt.Repeat)
        val twice = said("I am twenty five", repeats = opener.id, attempt = Attempt.Repeat)
        val history = state(opener, answer, once, twice).history()
        assertEquals(listOf("I am twenty five", "Nice"), history.map { it.text })
    }

    /** A passage still waiting for its reply sends the sentence and nothing after it. */
    @Test
    fun `an unanswered passage carries no reply`() {
        val opener = said("hello")
        assertEquals(listOf("hello"), state(opener).history().map { it.text })
    }

    // ── The gates ───────────────────────────────────────────────────────────────────────

    private val weights = Weights(Sheets.all.mapNotNull { Sheets.scoredPathOf(it) }
        .flatMap { path -> path.split("/").runningReduce { a, b -> "$a/$b" } }
        .distinct().associateWith { 1f })

    private val strict: (Sheet) -> Int = { 0 }

    private fun scored(vararg measured: Measured) =
        Scored(keptWords = 5, difficulty = 1f, measured = measured.toList())

    private fun sheet(path: String) = Sheets.of(path) as Sheet

    private fun sendsBack(vararg aptitudes: String) = Positions(
        aptitudes.associate { "$it.sends-back" to At("yes") },
    )

    /**
     * **A truncated turn closes the words' gate without reading any note.** There is no
     * complete sentence, and letting it through would mean having a fragment said again with
     * no right to finish it. It is an absence of matter and never a verdict of correctness.
     */
    @Test
    fun `a truncated turn closes the words gate on no note at all`() {
        val closing = Gates.words(
            measured = emptyList(), passage = scored(), settings = Positions(),
            weights = weights, sensitivity = strict, truncated = true, keptWords = 5,
        )
        assertEquals(Closing.Truncated, closing)
    }

    /** And a turn with no kept word: there is not even a note to read. */
    @Test
    fun `a turn with no kept word closes it too`() {
        val closing = Gates.words(
            measured = emptyList(), passage = scored(), settings = Positions(),
            weights = weights, sensitivity = strict, truncated = false, keptWords = 0,
        )
        assertEquals(Closing.NothingKept, closing)
    }

    /**
     * **It names every aptitude that says so and never the worst.** Several may say it at once,
     * and they are not competing criteria -- register and grammar are two ways for the words to
     * change. Naming only one would be an election, which the project does nowhere.
     */
    @Test
    fun `the words gate names all the aptitudes in cause`() {
        val closing = Gates.words(
            measured = listOf(
                Measured(sheet("correctness/correctness"), 0f),
                Measured(sheet("relevance/relevance"), 0f),
            ),
            passage = scored(), settings = sendsBack("correctness", "relevance"),
            weights = weights, sensitivity = strict, truncated = false, keptWords = 5,
        )
        assertEquals(Closing.Aptitudes(listOf("correctness", "relevance")), closing)
    }

    /** An aptitude the activity does not send back never closes a gate, however it scored. */
    @Test
    fun `an aptitude that does not send back lets through`() {
        val closing = Gates.words(
            measured = listOf(Measured(sheet("correctness/correctness"), 0f)),
            passage = scored(),
            settings = Positions(mapOf("correctness.sends-back" to At("no"))),
            weights = weights, sensitivity = strict, truncated = false, keptWords = 5,
        )
        assertNull(closing)
    }

    /**
     * **A free conversation sends back on correctness and on nothing else**, which is the
     * catalogue's own default and not something a sitting has to say.
     */
    @Test
    fun `a free conversation sends back on correctness alone`() {
        val flat = Sheets.all.mapNotNull { Sheets.scoredPathOf(it) }
        val closing = Gates.words(
            measured = flat.map { Measured(sheet(it), 0f) },
            passage = scored(), settings = Positions(),
            weights = weights, sensitivity = strict, truncated = false, keptWords = 5,
        )
        assertEquals(Closing.Aptitudes(listOf("correctness")), closing)
        assertNull(Gates.sound(
            measured = flat.map { Measured(sheet(it), 0f) },
            passage = scored(), settings = Positions(),
            weights = weights, sensitivity = strict,
        ))
    }

    /**
     * **A node with nothing measured under it has no note, and no note lets through**: a gate
     * is a failure to declare, and there is nothing here to declare one about.
     */
    @Test
    fun `an aptitude nothing measured lets through`() {
        val closing = Gates.sound(
            measured = emptyList(), passage = scored(),
            settings = sendsBack("pronunciation", "fluency"),
            weights = weights, sensitivity = strict,
        )
        assertNull(closing)
    }

    /** Clearing the bar lets through, which is the whole of an open gate. */
    @Test
    fun `a passage over the bar lets through`() {
        val closing = Gates.words(
            measured = listOf(Measured(sheet("correctness/correctness"), 1f)),
            passage = scored(), settings = sendsBack("correctness"),
            weights = weights, sensitivity = strict, truncated = false, keptWords = 5,
        )
        assertNull(closing)
    }

    // ── What cuts the sound analysis ────────────────────────────────────────────────────

    /**
     * **The ordering falls out on its own**: the words' gate is read before the analysis runs,
     * so when it closes the sound's gate never gets the chance to speak.
     */
    @Test
    fun `the words gate closed means the sound is never analysed`() {
        assertFalse(Gates.soundAnalysisRuns(wordsClosed = true, unsayable = false))
    }

    /** And an unsayable word cuts it whatever the settings: there is no ground, not a decision. */
    @Test
    fun `an unsayable word cuts it on its own`() {
        assertFalse(Gates.soundAnalysisRuns(wordsClosed = false, unsayable = true))
    }

    @Test
    fun `an ordinary passage is analysed`() {
        assertTrue(Gates.soundAnalysisRuns(wordsClosed = false, unsayable = false))
    }

    /**
     * **Nothing judged goes out when the gate closes**: every judged sheet computes, since they
     * are what decide whether it closes -- putting them out by their own decision would be
     * circular. What goes out is the sound analysis, and it alone.
     */
    @Test
    fun `the judged sheets are all there on a passage the gate held back`() {
        val read = app.speakup.notes.Sheeting.of(
            judged = app.speakup.judged.Judgement(
                intended = "I go there yesterday",
                spans = emptyList(), stumbling = emptyList(),
                following = "precise", difficulty = "medium",
            ),
            analysed = null,
            timed = null,
        )
        val judged = listOf("correctness/correctness", "relevance/relevance", "understanding/uptake")
        judged.forEach { path ->
            val measured = read.first { Sheets.scoredPathOf(it.sheet) == path }
            assertTrue("$path has no figure", measured.figure != null)
        }
        // And the sound ones are absent rather than zero: absent leaves the sum, a zero enters it.
        listOf("pronunciation/proximity", "pronunciation/melody").forEach { path ->
            assertNull(read.first { Sheets.scoredPathOf(it.sheet) == path }.figure)
        }
    }

    // ── The way out of a blocked passage ────────────────────────────────────────────────

    private fun waiting(vararg on: String) = Positions(
        on.associate { "advance.$it" to At("waits") } +
            mapOf("correctness.sends-back" to At("yes")),
    )

    /**
     * **In "waits" the big button is not available**, or one would leave a blocked passage by
     * simply saying something else, and the attempts would stop being the only way out.
     */
    @Test
    fun `waiting holds the big button until the attempts run out`() {
        val opener = said("I go there yesterday")
        val blocked = state(opener).copy(
            activity = Activity.conversation().copy(settings = waiting("words")),
            wordsGate = Closing.Aptitudes(listOf("correctness")),
        )
        assertEquals(Standing.ToReword, blocked.standing())
        assertFalse(blocked.closes())
    }

    /**
     * **And it comes back when they run out**, or nothing would ever move on -- which is why
     * *waits* guarantees the spending and not the repair: a passage to reword can close
     * without ever having been reworded.
     */
    @Test
    fun `the attempts spent give the big button back`() {
        val opener = said("I go there yesterday")
        val spent = state(opener).copy(
            activity = Activity.conversation().copy(
                settings = Positions(
                    waiting("words").all() + mapOf(Attempt.Rewording.lever to Count(0)),
                ),
            ),
            wordsGate = Closing.Aptitudes(listOf("correctness")),
        )
        assertEquals(Standing.Open, spent.standing())
        assertTrue(spent.closes())
    }

    /**
     * **"Waits" with zero attempts allowed is not a dead end**: the attempts are spent from the
     * outset, the passage closes unrepaired at once, and *waits* never waits.
     */
    @Test
    fun `waiting with no attempt allowed is not a dead end`() {
        val opener = said("I go there yesterday")
        val none = state(opener).copy(
            activity = Activity.conversation().copy(
                settings = Positions(
                    waiting("words", "sound").all() + mapOf(
                        Attempt.Rewording.lever to Count(0),
                        Attempt.Repeat.lever to Count(0),
                    ),
                ),
            ),
            wordsGate = Closing.Aptitudes(listOf("correctness")),
            soundGate = Closing.Aptitudes(listOf("pronunciation")),
        )
        assertTrue(none.closes())
    }

    /**
     * **The two exhaustions do not fall in the same place**: the rewordings' before the sound
     * analysis has run, the repeats' after -- so a passage whose words still have to change is
     * never sent back on its sound.
     */
    @Test
    fun `the words come before the sound`() {
        val opener = said("I go there yesterday")
        val both = state(opener).copy(
            activity = Activity.conversation().copy(settings = waiting("words")),
            wordsGate = Closing.Aptitudes(listOf("correctness")),
            soundGate = Closing.Aptitudes(listOf("pronunciation")),
        )
        assertEquals(Standing.ToReword, both.standing())
    }

    /** With the rewordings spent, what is left to repair is the sound. */
    @Test
    fun `the sound gate speaks once the rewordings are spent`() {
        val opener = said("I go there yesterday")
        val left = state(opener).copy(
            activity = Activity.conversation().copy(
                settings = Positions(mapOf(Attempt.Rewording.lever to Count(0))),
            ),
            wordsGate = Closing.Aptitudes(listOf("correctness")),
            soundGate = Closing.Aptitudes(listOf("pronunciation")),
        )
        assertEquals(Standing.ToSayAgain, left.standing())
    }

    /** Nothing to redo is an open passage, and the big button closes it. */
    @Test
    fun `a passage with nothing to redo is open`() {
        val run = state(said("hello"))
        assertEquals(Standing.Open, run.standing())
        assertTrue(run.closes())
    }
}
