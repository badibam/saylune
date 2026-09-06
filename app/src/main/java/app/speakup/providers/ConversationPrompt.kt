package app.speakup.providers

import app.speakup.chain.Exchange
import app.speakup.levers.Levers
import app.speakup.levers.Position
import app.speakup.levers.Positions
import app.speakup.levers.Stepped
import app.speakup.sheets.Sheets
import org.json.JSONObject

/**
 * The instruction every conversation provider is given, and the shape of the history.
 *
 * Held in one place because it **is** the division of labour the project rests on -- the
 * recognition transcribes the mouth, the language model decides the intention -- and a
 * second copy beside a second provider would be a second source that drifts from the first.
 * What differs between providers is how the JSON is guaranteed, never what is asked.
 *
 * **Four parts, ordered by how often they change**, which is also the order in which an
 * instruction is best followed: what governs the turn sits closest to it.
 *
 * 1. [APP] -- permanent, identical for every activity and every user.
 * 2. [activity] -- frozen at launch: the brief, the cast, what earlier scenes answered.
 * 3. the history -- past turns, replies only, stable at the head and growing at the tail.
 * 4. [present] -- rebuilt every turn: the instructions in force, the state of the levers the
 *    model holds, the message a rule has just laid, the menu if there is one.
 *
 * **The instructions are in 4 and not in 3**, though they change a handful of times a sitting
 * and so look semi-stable. Laid before the history, they would be buried under thirty turns at
 * the very moment they have to govern the next one. What is lost is a cached prefix, counted
 * in thousandths; what is gained is an instruction read where it applies.
 */
internal object ConversationPrompt {

    /**
     * Part 1: what the app is, what to send back, and the invariants that never move.
     *
     * The **norm of correctness** lives here and nowhere else, because it has to be identical
     * for every activity: that invariance is what makes correctness checkable at the bench on
     * isolated sentences. It asks one question and one only -- is the sentence built right --
     * and it knows nothing of variety or register. `Gonna try?` and `I ain't got none` are
     * English people speak, so they pass. What a challenge wants to demand of the variety or
     * the register is an instruction on **relevance**, which is where situation is judged.
     */
    val APP = """
        You are a warm, curious English conversation partner for someone practising
        speaking. Talk with them; never run a lesson and never interrupt.

        You receive their turn as a raw transcript: lower case, no punctuation, and
        possibly a word the recogniser misheard.

        Answer with a JSON object holding these fields, in this order. Write them in this
        order and do not reorder them: each one you write shapes the next, and the
        learner's own words have to be settled before you take your character's voice.

        "intended": the learner's own turn, written out, hesitations and all. Repair only
        the transcription -- a word the recogniser clearly got wrong given the
        conversation -- and punctuate it according to what they meant, since you know
        which intention you are answering. Never repair their grammar, their word choice
        or their style: a wrong tense, a missing article, a clumsy turn of phrase must
        survive here exactly as they said it, and so must every "um", every false start
        and every repeated word. If nothing was misheard, return the transcript with
        punctuation and capitals only. Write every number, date and amount in **words**,
        never in digits: "twenty five", not "25". The analysis places each sound on the
        letters that write it, and digits have none.

        Two turns need saying plainly. If the turn was cut off mid-sentence, leave it cut
        off -- never finish it for them; the voice would then say a word nobody spoke and
        every mark on the turn would land beside its sound. And if the turn is entirely
        filler and abandoned starts, or is not in English at all, write down what they
        actually said and invent nothing: there is no sentence to rebuild, and the app
        reads that from the markings below.

        "spans": the groups of words worth marking, as a list. Each is
        {"from": <int>, "to": <int>, "correctness": <notch>, "relevance": <notch>} where
        "from" and "to" are character offsets into "intended", "from" included and "to"
        excluded, and both must fall exactly on a word boundary. Mark only what is worth
        marking: a word in no span counts as "ok" on both scales. Correctness notches are
        "ne-se-dit-pas", "mal-forme", "ok"; relevance notches are "a-cote", "plat", "ok",
        "juste".

        Correctness asks one question and one only: **is the sentence built right?** Not
        whether it is polished, formal, or the phrasing you would have picked. Spoken
        English counts as English: "Gonna try?", "Going out later?", "I ain't got none",
        "I'm doing good" are all "ok". Use "mal-forme" for a sentence a native speaker of
        no variety and no register would produce -- a wrong tense, a missing or wrong
        article, a wrong preposition, an impossible word order, a verb that does not take
        that object. Use "ne-se-dit-pas" only when the words do not exist as English at
        all, which is rarer than it sounds.

        Relevance asks the other question: **did they aim right?** -- for the situation,
        for who is listening, for what was asked. "juste" is the turn of phrase a native
        would have reached for and they found it; "plat" is aiming right but limply --
        vague, basic, or said before; "a-cote" is missing what was called for, including
        the tone and the register. This is where variety, register and any instruction
        you have been given are judged, never in correctness.

        "stumbling": the parts of "intended" that are not part of the sentence, as a list
        of {"from": <int>, "to": <int>, "notch": <notch>} on the same offsets and the same
        word-boundary rule. Notches are "abandonne" for words belonging to a start they
        gave up on, and "remplissage" for "um", "like", "I mean" used as a crutch. Anything
        you do not mark counts as kept. Judge the use and not the word: "I mean what I say"
        is kept, "it was, I mean, hard" is filler.

        "following": one notch for the whole turn, saying what their answer proves they
        took in from your last turn. "entre-les-lignes" answers what was implied and not
        said; "precis" picks up something only somebody who heard could pick up;
        "en-rapport" answers what the turn said without picking up a detail;
        "sur-le-sujet" catches the topic and not the turn; "vague" would have worked
        whatever you had said; "a-cote" answers something else. Judge what the answer shows
        they took in, never how well it was said: a badly built sentence can be perfectly
        on point. If your last turn does not exist, use "a-cote" only if they really are
        off; a first turn has nothing to follow.

        "spoken": your reply, in English, as it should be said aloud.

        "difficulty": one notch for the turn you have just written, weighing its length,
        its vocabulary and its structure together: "tres-facile", "facile", "moyen",
        "difficile", "tres-difficile".

        "echo": include this field only when you marked something in "spans". One short
        line that picks the slip up and hands the sentence back, in your own voice, the way
        a friendly native speaker would. To "I have twenty five years": "Ah, you're
        twenty-five!". Leave the field out entirely when nothing was marked.

        "title": include this field only when there is a reason to -- the conversation has
        no title yet, or what you have been talking about has moved far enough that the
        current title no longer describes it. Six words at most, in English, no full stop.
        On any other turn leave the field out entirely. A title rewritten every turn is a
        title nobody can recognise in a list, which is the only thing it is for.
    """.trimIndent()

    /**
     * Part 2: what this activity is, frozen at launch.
     *
     * Empty in a free conversation, which has no brief, no cast and no earlier scene to
     * remember. The part exists all the same so nothing has to move when definitions arrive.
     */
    fun activity(titled: String?): String = when {
        titled.isNullOrBlank() -> "This conversation has no title yet."
        else -> "This conversation is currently titled: $titled"
    }

    /**
     * Part 4: what governs this turn, rebuilt every time.
     *
     * Only the levers the model **holds** appear, and only where a position has something to
     * ask for: a lever the app simply does has nothing to say here, and a model-held position
     * whose channel does not exist yet says nothing either.
     *
     * **The state reaches the model through the front door alone.** A lost life, a failed
     * passage, a threshold that just got shorter reach it only if a rule decided to tell it,
     * by a message, in an author's words. Pasting the state here permanently would make it
     * react always, in every scene, without anyone having wanted it to.
     */
    fun present(positions: Positions): String {
        val asked = Levers.all
            .filterIsInstance<Stepped>()
            .filter { it.held == app.speakup.levers.Held.Model && positions.live(it.key) }
            .mapNotNull { lever ->
                val name = (positions.of(lever.key) as? Position.At)?.name ?: return@mapNotNull null
                lever.steps.first { it.name == name }.tells
            }
        return if (asked.isEmpty()) "" else "For this turn:\n" + asked.joinToString("\n") { "- $it" }
    }

    /** The whole instruction: part 1, then part 2, then part 4. Part 3 is the message list. */
    fun system(titled: String?, positions: Positions = Positions()): String =
        listOf(APP, activity(titled), present(positions))
            .filter { it.isNotBlank() }
            .joinToString("\n\n")

    /**
     * Said again to a provider that has no JSON mode to enforce the shape.
     *
     * DeepSeek is asked for `json_object` and cannot answer anything else; the models
     * reached through Replicate expose no such input, so the only guarantee left is the
     * instruction, and the parser downstream has to be ready for it to be broken.
     */
    val JSON_ONLY = "\n\nAnswer with that JSON object and nothing else: no explanation " +
        "before it, no code fence around it."

    /**
     * A past answer of the model, written as the object it actually emitted.
     *
     * **The marks stay out of the replay**, and the reason is the one already written about
     * `faulty`: a model shown its last twenty verdicts becomes consistent with them rather
     * than with the turn it is reading. So does the echo, the difficulty and the title -- the
     * name in force is in the system message, and a history of past names is an invitation to
     * add one more.
     *
     * Measured on the device: replaying these as bare prose makes the third turn come back
     * as twenty spaces with `finish_reason: stop`. The conversation then shows the model its
     * own answers in prose while `response_format` only lets it emit JSON, and whitespace is
     * the one thing legal at the start of a JSON document.
     */
    fun answered(history: List<Exchange>, at: Int): String {
        val said = JSONObject().put("spoken", history[at].text)
        // `intended` belonged to the learner's turn just before, which is where the pipeline
        // always puts it. Written out only when it really is there.
        history.getOrNull(at - 1)?.takeIf { it.fromLearner }?.let { said.put("intended", it.text) }
        return said.toString()
    }

    fun message(role: String, content: String): JSONObject =
        JSONObject().put("role", role).put("content", content)

    /** The notch names the contract above lists, straight from the catalogue that owns them. */
    val CORRECTNESS get() = Sheets.columnOf("correction/correction")
    val RELEVANCE get() = Sheets.columnOf("pertinence/pertinence")
    val STUMBLING get() = Sheets.columnOf("fluidite/remplissage-reprises")
    val FOLLOWING get() = Sheets.columnOf("comprehension/suivi")
}
