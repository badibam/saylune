package app.speakup.providers

import app.speakup.chain.Exchange
import app.speakup.levers.Levers
import app.speakup.levers.Position
import app.speakup.capture.Ending
import app.speakup.activity.Brief
import app.speakup.chain.Present
import app.speakup.chain.Scene
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
     * **No persona here.** *Warm and curious* used to open this text, and it was a trait of
     * character sitting in the part every activity shares: it governed the hostile bouncer
     * and the bored receptionist too, and no definition could get out from under it. Who is
     * speaking is part 2's, out of the definition, where an author writes someone else.
     *
     * The **norm of correctness** lives here and nowhere else, because it has to be identical
     * for every activity: that invariance is what makes correctness checkable at the bench on
     * isolated sentences. It asks one question and one only -- is the sentence built right --
     * and it knows nothing of variety or register. `Gonna try?` and `I ain't got none` are
     * English people speak, so they pass. What a challenge wants to demand of the variety or
     * the register is an instruction on **relevance**, which is where situation is judged.
     */
    val APP = """
        You are talking with someone who is practising spoken English. Whoever you are
        playing is said below; here is what never changes, whoever that is. You are having
        a conversation, not running a lesson, and you never interrupt: the app marks what
        was said, and no character of yours corrects on the spot unless asked to by an echo.

        You receive their turn as a raw transcript: lower case, no punctuation, and
        possibly a word the recogniser misheard.

        You are also told which passage of the conversation you are on. A passage is one
        thing the learner set out to say plus any further attempt at it, so it counts
        exchanges and not recordings. The number carries no instruction of its own: it
        says where the conversation stands, and only what this activity tells you about
        its shape can give it a meaning. Unless something says otherwise, there is no
        length to reach and no point at which to wrap things up.

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
        excluded, and both must fall exactly on a word boundary. A word is a run of
        characters with no space in it, so whatever punctuation touches a word belongs to
        that word: "from" is the first character of the first word, and "to" is one past
        the last character of the last word -- past its comma or full stop, and never on
        the space that follows. In "Well, what's your expertise domain?", the group
        "expertise domain?" is {"from": 18, "to": 35}: not 34, which would stop before the
        question mark, and not 36, which is past the end. Mark only what is worth marking:
        a word in no span counts as "ok" on both scales. Correctness notches are
        "not-said", "malformed", "ok"; relevance notches are "off-target", "flat", "ok",
        "apt".

        Correctness asks one question and one only: **is the sentence built right?** Not
        whether it is polished, formal, or the phrasing you would have picked. Spoken
        English counts as English: "Gonna try?", "Going out later?", "I ain't got none",
        "I'm doing good" are all "ok". Use "malformed" for a sentence a native speaker of
        no variety and no register would produce -- a wrong tense, a missing or wrong
        article, a wrong preposition, an impossible word order, a verb that does not take
        that object. Use "not-said" only when the words do not exist as English at
        all, which is rarer than it sounds.

        Relevance asks the other question: **did they aim right?** -- for the situation,
        for who is listening, for what was asked. "apt" is the turn of phrase a native
        would have reached for and they found it; "flat" is aiming right but limply --
        vague, basic, or said before; "off-target" is missing what was called for, including
        the tone and the register. This is where variety, register and any instruction
        you have been given are judged, never in correctness.

        "stumbling": the parts of "intended" that are not part of the sentence, as a list
        of {"from": <int>, "to": <int>, "notch": <notch>} on the same offsets and the same
        word-boundary rule. Notches are "abandoned" for words belonging to a start they
        gave up on, and "filler" for "um", "like", "I mean" used as a crutch. Anything
        you do not mark counts as kept. Judge the use and not the word: "I mean what I say"
        is kept, "it was, I mean, hard" is filler.

        "following": one notch for the whole turn, saying what their answer proves they
        took in from your last turn. "implied" answers what was implied and not
        said; "precise" picks up something only somebody who heard could pick up;
        "on-point" answers what the turn said without picking up a detail;
        "on-topic" catches the topic and not the turn; "vague" would have worked
        whatever you had said; "off-target" answers something else. Judge what the answer shows
        they took in, never how well it was said: a badly built sentence can be perfectly
        on point. If your last turn does not exist, use "off-target" only if they really are
        off; a first turn has nothing to follow.

        "spoken": your reply, in English, as it should be said aloud.

        "difficulty": one notch for the turn you have just written, weighing its length,
        its vocabulary and its structure together: "very-easy", "easy", "medium",
        "hard", "very-hard".

        "echo": include this field only when you marked something in "spans". One short
        line that picks the slip up and hands the sentence back, in your own voice, the way
        a native speaker would. To "I have twenty five years": "Ah, you're
        twenty-five!". Leave the field out entirely when nothing was marked.
    """.trimIndent()

    /**
     * Part 2: what this activity is, frozen at launch -- who is speaking, and what is played.
     *
     * **The staging is here and it is never shown to the learner.** The brief cuts in two and
     * no further: the [Brief.situation], true for everybody and readable on a pre-game screen,
     * and the [Brief.staging], which addresses the character alone and would sabotage itself
     * if displayed. The price of the single call is here and it is said once -- the staging
     * sits in the judge's context, where it has no business, and what holds it at arm's length
     * is the sentence saying the judge's criterion is the instruction. That is checked at a
     * bench; it is not proved.
     */
    fun activity(scene: Scene): String {
        val lines = mutableListOf<String>()
        scene.brief?.staging?.takeIf { it.isNotBlank() }?.let { lines += it }
        scene.brief?.situation?.takeIf { it.isNotBlank() }?.let { lines += "The situation: $it" }
        // Only where there are several: naming the one voice a free conversation has would be
        // telling the model something it has no use for.
        if (scene.cast.size > 1) {
            lines += "Say who is speaking, by key, among: " +
                scene.cast.joinToString(", ") { it.key }
        }
        return lines.joinToString("\n\n")
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
    fun present(present: Present): String {
        val lines = mutableListOf<String>()
        // Where the conversation stands, said plainly and left uninterpreted: what part 1
        // declares is that the number asks for nothing by itself.
        lines += "This is passage ${present.passage}."
        Levers.all
            .filterIsInstance<Stepped>()
            .filter {
                it.held == app.speakup.levers.Held.Model && present.positions.live(it.key)
            }
            .forEach { lever ->
                val name = (present.positions.of(lever.key) as? Position.At)?.name
                    ?: return@forEach
                lever.steps.first { it.name == name }.tells?.let { lines += it }
            }
        // **How the turn ended, told rather than guessed.** The instruction above already
        // forbids finishing a cut-off turn; this is what says one was cut off, which the
        // transcript alone cannot -- a sentence that reads whole can have been truncated on
        // a word the recogniser dropped, and one that reads broken can be how someone talks.
        // The instructions in force, in the words a definition wrote. They sit here, in part 4,
        // and not with the scene: laid before the history they would be buried under thirty
        // turns at the very moment they have to govern the next one.
        present.instructions.mapNotNull { it.text }.forEach { lines += it }
        // What a rule has just laid, which is the whole of what the state says to the model.
        present.said.forEach { lines += it }
        present.ending?.let {
            lines += when (it) {
                Ending.ByLength -> "The turn you are reading was cut off: the recording " +
                    "reached the time it was allowed. Treat it as unfinished."
                Ending.BySilence -> "The turn you are reading was sent because the learner " +
                    "fell silent. It may be unfinished; treat it as it stands."
            }
        }
        return if (lines.isEmpty()) "" else "For this turn:\n" + lines.joinToString("\n") { "- $it" }
    }

    /** The whole instruction: part 1, then part 2, then part 4. Part 3 is the message list. */
    fun system(scene: Scene, present: Present = Present()): String =
        listOf(APP, activity(scene), present(present))
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
    val CORRECTNESS get() = Sheets.columnOf("correctness/correctness")
    val RELEVANCE get() = Sheets.columnOf("relevance/relevance")
    val STUMBLING get() = Sheets.columnOf("fluency/stumbling")
    val FOLLOWING get() = Sheets.columnOf("understanding/uptake")
}
