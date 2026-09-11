package app.saylune.providers

import app.saylune.chain.Exchange
import app.saylune.levers.Levers
import app.saylune.levers.Position
import app.saylune.capture.Ending
import app.saylune.chain.Asked
import app.saylune.scene.Kind
import app.saylune.scene.Reach
import app.saylune.chain.Provoked
import app.saylune.chain.Present
import app.saylune.chain.Said
import app.saylune.chain.Scene
import app.saylune.levers.Stepped
import app.saylune.sheets.Sheets
import org.json.JSONArray
import org.json.JSONObject

/**
 * The two instructions every conversation provider is given, and the shape of the history.
 *
 * Held in one place because it **is** the division of labour the project rests on -- the
 * recognition transcribes the mouth, the language model decides the intention -- and a
 * second copy beside a second provider would be a second source that drifts from the first.
 * What differs between providers is how the JSON is guaranteed, never what is asked.
 *
 * **Two calls since 2026-09-10, so two instructions**: [SPEAKING] for the one who takes the
 * character's voice, [JUDGING] for the one who marks. They are laid out below in the shape the
 * speaker has kept; the judge's is simpler -- a head, then one written record -- and the
 * reasons are on [judged].
 *
 * **Four parts, ordered by how often they change**, which is also the order in which an
 * instruction is best followed: what governs the turn sits closest to it.
 *
 * 1. [SPEAKING] -- permanent, identical for every activity and every user.
 * 2. [activity] -- frozen at launch: the brief, the cast, what earlier scenes answered.
 * 3. the history -- past turns, replies only, plus what an event told the leader at the
 *    passage it went, stable at the head and growing at the tail.
 * 4. [present] -- rebuilt every turn: the instructions in force, the state of the levers the
 *    model holds, the turn an event has directed, the questions of this turn.
 *
 * **1 and 2 are the system message; 3 is the message list; 4 rides on the last message**,
 * in front of the turn it governs. Parts 1 and 2 are what a provider's cache can keep, and the
 * history grows at the tail behind them, so a call shares almost all of the one before it.
 *
 * **The instructions are in 4 and not in 3**, though they change a handful of times a sitting
 * and so look semi-stable. Laid before the history, they would be buried under thirty turns at
 * the very moment they have to govern the next one. What is lost is a cached prefix, counted
 * in thousandths; what is gained is an instruction read where it applies.
 *
 * **Part 4 used to be glued onto the system message**, so it sat in front of the history and
 * broke the stable head every turn -- the opposite of what this list said, and never weighed:
 * it was written inside [system] the day it was invented. What it cost is recorded on
 * [PROVOKED], where a model followed the last message and dropped a field part 4 had asked
 * for two thousand tokens earlier. Moved to the tail on 2026-09-10.
 */
internal object ConversationPrompt {

    /**
     * Part 1 for **the one who speaks**: what the app is, what to say back, and the
     * invariants that never move.
     *
     * **No persona here.** *Warm and curious* used to open this text, and it was a trait of
     * character sitting in the part every activity shares: it governed the hostile bouncer
     * and the bored receptionist too, and no definition could get out from under it. Who is
     * speaking is part 2's, out of the definition, where an author writes someone else.
     *
     * **No marking here either, since 2026-09-10.** The three markings, the following, the
     * reach, the remarks and the difficulty went to [JUDGING] with the call that reads them.
     * What is left is the character's own work, and `intended`, which stays because the
     * scaffolding is what makes the model settle the learner's words before it answers them.
     */
    val SPEAKING = """
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
        actually said and invent nothing: there is no sentence to rebuild, and nothing
        here asks you to make one.

        "established": include this field only when the instruction for this turn lists
        questions to settle. An object whose keys are exactly the keys listed there and
        whose values are your answers, one per question and none left out. Each question
        says what shape its answer takes and how far you may go. What the conversation has
        already established always wins; how far you may go only says what to do when it
        does not settle the matter. Write an answer the way the situation is written: the
        learner is "you", everybody else is named or spoken of in the third person -- these
        answers are reread later by another character, and by the learner, so a "I am still
        stung" would have lost its subject by then.

        "echo": include this field only when they got something wrong and there is
        something to pick up. One short line that picks the slip up and hands the sentence
        back, in your own voice, the way a native speaker would. To "I have twenty five
        years": "Ah, you're twenty-five!". It is the **opening of what you say**: the app
        lays it in front of the first speech of "said", with no pause between them, so
        write the two to read as one utterance. It never opens a stage direction, a
        narrator remarking on somebody's grammar being no part of this. Leave the field
        out entirely when there was nothing to pick up.

        "said": your turn, as a list of utterances played one after another. Never empty,
        and at most ${Said.CEILING} of them. Each is an object with three fields:

          "kind": "speech" for somebody talking, "stage" for a stage direction.
          "who": the key of whoever it belongs to, from the cast above, or "narrator".
          "text": the words, in English, as they should be said aloud.

        A speech is somebody talking -- to the learner, or to another character. A stage
        direction is matter of the story that is **not** said to the learner: what is
        happening, what is seen, what someone does. It is played aloud like the rest.

        No order is imposed. One reply on its own is the ordinary turn. A character may
        speak and then a second character answer them. A stage direction may open the
        turn, close it, or fall between two replies. Use several only when the scene
        really has several beats: every one of them is time the learner waits before they
        may speak.

        **Never pick the slip up in here** -- that is what "echo" is for, and it has
        already been said. After "Ah, you're twenty-five!", the first speech is "And where
        do you work?", never "Ah, you're twenty-five! And where do you work?".
    """.trimIndent()

    /**
     * Part 1 for **the one who judges**: what to mark, and against what.
     *
     * **It plays nobody.** The first line says so, and the signature of `Conversation.judge`
     * makes it true: the staging, the persona and what the learner asked to be steered around
     * have no parameter to travel on. That is the point of the split -- these used to sit in
     * this context, and what held them at arm's length was a sentence of prose nobody checked.
     *
     * The **norm of correctness** lives here and nowhere else, because it has to be identical
     * for every activity: that invariance is what makes correctness checkable at the bench on
     * isolated sentences. It asks one question and one only -- is the sentence built right --
     * and it knows nothing of variety or register. `Gonna try?` and `I ain't got none` are
     * English people speak, so they pass. What a challenge wants to demand of the variety or
     * the register is an instruction on **relevance**, which is where situation is judged.
     *
     * **The origin of the span offsets is spelled out** because a worked example implying it
     * was not enough: a turn came back with every bound one too high but the first, which is
     * what counting from one gives, and the marking was refused (2026-09-08).
     *
     * **"intended" keeps its name here although this call does not write it.** It is handed
     * over instead, under that name, so every line about offsets reads exactly as it did when
     * one call did both jobs -- and prompt prose is material the bench measures, so what does
     * not have to move does not move.
     */
    val JUDGING = """
        You are marking one turn of a conversation in which somebody is practising spoken
        English. You take no part in it: you never write a reply and you never speak as
        anybody in it. You are shown the conversation so far, the turn the learner has
        just taken, and what was said back to them.

        The learner's turn is handed to you written out, under the name "intended". It is
        theirs as they said it: a wrong tense, a missing article, a clumsy turn of phrase
        are what you are here to mark, so never rewrite it and never mark against a
        repaired version of it. Every offset below counts into that string.

        Answer with a JSON object holding these fields, in this order.

        "spans": the groups of words worth marking, as a list. Each is
        {"from": <int>, "to": <int>, "correctness": <notch>, "relevance": <notch>} where
        "from" and "to" are character offsets into "intended", "from" included and "to"
        excluded, and both must fall exactly on a word boundary. Offsets count from zero:
        the first character of "intended" is 0, and a group running to the end of the turn
        has a "to" equal to the number of characters in "intended". A word is a run of
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
        you have been given are judged, never in correctness. Relevance takes those
        four notches and no others: "following" below names a different scale, and
        its notches never appear here.

        "stumbling": the parts of "intended" that are not part of the sentence, as a list
        of {"from": <int>, "to": <int>, "notch": <notch>} on the same offsets and the same
        word-boundary rule. Notches are "abandoned" for words belonging to a start they
        gave up on, and "filler" for "um", "like", "I mean" used as a crutch. Anything
        you do not mark counts as kept. Judge the use and not the word: "I mean what I say"
        is kept, "it was, I mean, hard" is filler.

        "following": one notch for the whole turn, saying what their answer proves they
        took in from the turn spoken to them just before it. "implied" answers what was
        implied and not said; "precise" picks up something only somebody who heard could
        pick up; "on-point" answers what that turn said without picking up a detail;
        "on-topic" catches the topic and not the turn; "vague" would have worked whatever
        had been said; "off-target" answers something else. Judge what the answer shows
        they took in, never how well it was said: a badly built sentence can be perfectly
        on point. If there is no turn before theirs, use "off-target" only if they really
        are off; a first turn has nothing to follow.

        "reach": one notch for the whole turn, saying how much of a sentence they built,
        and nothing else. "built" is a sentence whose structure carries the meaning -- a
        condition, a subordinate clause, tenses stacked; "extended" is two clauses joined,
        or one tense other than the present held to the end; "plain" is one clause, one
        tense, subject-verb-object; "bare" is a fragment that answers without making a
        sentence; "minimal" is one word, an interjection, or a set phrase -- "yeah",
        "me too", "I don't know". Judge only what came out of their mouth: this scale says
        nothing about whether they were right, whether they aimed well, or whether it was
        the answer the moment called for -- those are the other fields. A short answer that
        was exactly what was asked for is still "bare" or "minimal" here, and that is the
        point of the field.

        "remarks": a short line about an aptitude, as an object, for the ones worth one.
        Keys are "understanding", "correctness" and "relevance", and you include only the
        keys you have something to say under -- leaving all three out is the ordinary case
        on a turn with nothing remarkable about it. **At most 45 characters each**: it is
        one line on a small screen, not a sentence of explanation. Say what stands out
        over the whole turn, and **never where a fault is**: every span you marked above is
        already drawn on the words it falls on, so naming one here would only pick a
        favourite among marks the learner can already see. "The topic was never touched"
        and "Answers, but never asks anything back" are remarks; "the past tense is wrong
        in the second clause" is not.

        "difficulty": one notch for the reply that was said back to them, weighing its
        length, its vocabulary and its structure together: "very-easy", "easy", "medium",
        "hard", "very-hard". It says how hard that reply is to follow, and nothing about
        the learner.
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
    /**
     * What the learner asked to be steered around, handed over with what to do about it.
     *
     * **Said as prose and left to the model's reading**, which is the only thing that could
     * work: what a person needs kept away is theirs and does not come out of anybody's
     * vocabulary, so a list of categories would only be a worse version of the sentence they
     * wrote. What the app adds is the instruction around it -- steer, do not announce -- since
     * a character that names the subject in order to avoid it has raised it.
     *
     * **It is a setting sitting in the judge's context**, and that is the same known the
     * staging already carries: nothing here may move a mark, and what holds it at arm's length
     * is the sentence saying the judge marks against the instruction. Checkable at a bench, not
     * proved (`../../../../../../TODO.md`).
     */
    fun avoiding(said: String): String = """
        One more thing, and it comes from the person you are talking to rather than from the
        scene. They have written down what they would rather this conversation stayed away
        from, in their own words:

        "$said"

        Read it as they meant it and steer around it: choose other ground, let a subject drop,
        take the scene elsewhere. **Never announce that you are doing so** and never name what
        you are avoiding -- a character who says what he will not talk about has talked about
        it. Whoever you are playing stays who they are; this changes what they bring up, not
        their temper.
    """.trimIndent()

    fun activity(scene: Scene): String {
        val lines = mutableListOf<String>()
        // **A description belongs to whoever it describes**, and goes out whenever they are
        // present. With one voice there is nobody to tell apart, so it is said bare.
        if (scene.cast.size == 1) {
            scene.cast.single().about.takeIf { it.isNotBlank() }?.let { lines += it }
        } else {
            scene.cast.forEach { lines += "${it.key}: ${it.about}" }
        }
        scene.situation.takeIf { it.isNotBlank() }?.let { lines += "The situation: $it" }
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
     * passage, a threshold that just got shorter reach it only if an event decided to tell it,
     * in an author's words -- and that text is laid in the conversation where it went, not
     * here. Pasting the state here permanently would make it react always, in every scene,
     * without anyone having wanted it to.
     */
    fun present(present: Present): String {
        val lines = mutableListOf<String>()
        // Where the conversation stands, said plainly and left uninterpreted: what part 1
        // declares is that the number asks for nothing by itself.
        lines += "This is passage ${present.passage}."
        Levers.all
            .filterIsInstance<Stepped>()
            .filter {
                it.held == app.saylune.levers.Held.Model && present.positions.live(it.key)
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
        present.instructions.forEach { lines += it }
        // **The questions of this turn, whose answers are required fields.** They go here, in
        // the part rebuilt every turn, because that is what they are: put at one moment and at
        // no other, so laid where they apply rather than in a heading every call carries.
        present.asking.forEach { lines += asked(it) }
        // The turn an event has directed, which is what asked for this turn to be taken at
        // all. What an event merely tells the leader is not here: it is placed in the
        // conversation at the passage it went, where it is read with its date.
        present.directed.forEach { lines += it }
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

    /**
     * One question, as the turn's instruction puts it.
     *
     * The shape and the rung are said with it rather than declared once in part 1: a sitting
     * mixes questions of different rungs, and a rule stated in general would have to be
     * matched back to each question by the reader.
     *
     * **The rung is written as a staircase**, each one repeating the floor under it -- what
     * the conversation established wins -- or invention overrules the truth and an answer
     * contradicts what has just happened.
     */
    private fun asked(asked: Asked): String {
        val unsure = if (asked.reach == Reach.Said) listOf(Asked.DONT_KNOW) else emptyList()
        val shape = when (val kind = asked.kind) {
            is Kind.Words -> "Answer in prose, a sentence or two."
            is Kind.Flag -> "Answer with exactly one of: " +
                (listOf("true", "false") + unsure).joinToString(", ")
            is Kind.Choice -> "Answer with exactly one of: " +
                (kind.among + unsure).joinToString(", ")
            is Kind.Number -> "Answer with a number" +
                (kind.min?.let { ", ${it.toLong()} at the least" } ?: "") +
                (kind.max?.let { ", ${it.toLong()} at the most" } ?: "") + "."
        }
        val far = when (asked.reach) {
            Reach.Said ->
                "Answer only from what has been said in this conversation. If nothing there " +
                    "settles it, answer exactly \"${Asked.DONT_KNOW}\"."
            Reach.Deduce ->
                "Answer from what has been said. Where that does not settle it, conclude " +
                    "from what has been said."
            Reach.Invent ->
                "Answer from what has been said. Where that does not settle it, conclude " +
                    "from what has been said; and where there is nothing to conclude from, " +
                    "decide."
        }
        return "Settle this, under the key \"${asked.key}\" of \"established\": " +
            "${asked.about}. $shape $far"
    }

    /**
     * The stable head: part 1, then part 2. Part 3 is the message list, part 4 rides on [turn].
     *
     * **It is the same text from the first turn of a sitting to its last**, which is what makes
     * it worth caching at a provider that caches prefixes. Nothing rebuilt each turn belongs
     * here.
     *
     * What the learner asked to be steered around goes between 1 and 2 -- after everything
     * that is the same for everybody, before anything that is this scene's, and inside the
     * stable head either way.
     */
    fun system(scene: Scene): String =
        listOf(
            SPEAKING,
            scene.avoid.trim().takeIf { it.isNotEmpty() }?.let { avoiding(it) }.orEmpty(),
            activity(scene),
        )
            .filter { it.isNotBlank() }
            .joinToString("\n\n")

    /**
     * The last message: part 4, then the turn being answered.
     *
     * **Everything rebuilt each turn is here and nowhere else**, which is what leaves parts 1
     * and 2 stable behind the history. What governs a turn is then read where it applies,
     * immediately in front of the words it governs, rather than thirty turns upstream.
     *
     * The transcript carries a line naming it, which a bare message did not need: it is no
     * longer alone in its message, and an unannounced sentence after a list of instructions
     * would read as one more instruction.
     *
     * **Something is always sent as the last message.** A call ending on the character's own
     * previous answer is a shape not every provider accepts, and the one thing the app can
     * truthfully put there is that nobody spoke.
     */
    fun turn(transcript: String, present: Present): String =
        listOf(
            present(present),
            when (present.provoked) {
                Provoked.ByRule -> PROVOKED
                null -> "What they just said, as the recogniser heard it:\n\n$transcript"
            },
        )
            .filter { it.isNotBlank() }
            .joinToString("\n\n")

    /**
     * The turn nobody prompted, said to the model in the app's own voice.
     *
     * It names **what is missing** rather than asking for a shorter answer: those fields hang
     * off a learner turn, so with none there is nothing for them to attach to, and a model
     * told that works out the rest. The echo goes with them -- it picks up a slip, and there
     * is no slip where there is no sentence.
     *
     * **It named eight fields and now names two**, and nothing was decided to shrink it: six
     * of them left with the marking, and a turn nobody prompted simply does not call the
     * judge. What had to be kept up is gone instead.
     *
     * **What it must not do is say *`said` alone*.** It did, and that took the questions of
     * the opening down with it: the model answered without a field it had just been asked for.
     * What has nothing to attach to is named; nothing else is forbidden.
     */
    val PROVOKED = """
        Nobody has spoken to you this turn. You are taking it of your own accord, on the
        instruction you have just been given. There is no learner turn to read, so
        "intended" and "echo" have nothing to attach to: leave those two out. Answer with
        "said", and with "established" as well if the instruction for this turn lists
        questions to settle.
    """.trimIndent()

    // ── The one who judges: its head, and its one message ───────────────────────────────

    /**
     * The judge's stable head: part 1, then the situation, and nothing else of the scene.
     *
     * **The staging is not here and cannot be.** It addresses the character alone, it is never
     * shown to the learner, and it has no business shaping a mark; the same goes for what the
     * learner asked to be steered around. Both used to ride in this context under a sentence
     * of prose that told the model to mark against the instruction and nothing else -- watched,
     * never checked. There is now no parameter that carries them here.
     *
     * **The situation stays**, and it is required rather than tolerated: relevance is judged
     * against what the activity asked for, and *"I'll go there"* is faultless English that a
     * challenge to speak in the past makes off-target (`docs/reference.md`).
     */
    fun judging(situation: String): String =
        listOf(JUDGING, situation.trim().takeIf { it.isNotEmpty() }?.let { "The situation: $it" })
            .filterNotNull()
            .joinToString("\n\n")

    /**
     * Everything else the judge is given, as **one message**.
     *
     * **The dialogue is written out rather than replayed as roles**, and that is the fork the
     * design left open. The judge emits verdicts, so a replay in the assistant role could only
     * be its own past verdicts -- and a model shown its last twenty verdicts becomes consistent
     * with them rather than with the turn it is reading, which is measured and is the one thing
     * this must not do. Replaying the character's replies there instead would show it an
     * assistant that speaks, which it never does. So it is one written record: an observer's
     * shape, and one the two providers that have no chat roles take unchanged.
     *
     * It still grows only at the tail, so a provider that caches prefixes keeps all of it but
     * the last turn from one call to the next.
     *
     * The instructions in force sit between the record and the turn, where they govern; the
     * reply comes last because it is the newest thing said.
     */
    fun judged(
        history: List<Exchange>, said: String, answered: String, present: Present,
    ): String = listOf(
        record(history),
        judgeTail(present),
        "The learner's turn, written out. This is \"intended\":\n\n$said",
        "What was said back to them:\n\n$answered",
    )
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

    /**
     * The conversation up to this turn, said plainly, the learner's side named as theirs.
     *
     * **What the app told the leader is left out**, and that is the rule and not a tidying:
     * the judge sees what the learner can know, and a text for the leader alone is a secret of
     * the scene. What was shown to the learner is in the thread already, as a turn somebody
     * said.
     */
    private fun record(history: List<Exchange>): String {
        val spoken = history.filterNot { it.isAside }
        return if (spoken.isEmpty()) ""
        else "The conversation so far:\n\n" + spoken.joinToString("\n") {
            (if (it.fromLearner) "Learner: " else "Character: ") + it.text
        }
    }

    /**
     * Part 4 for the judge: the instructions in force, and how the recording stopped.
     *
     * **Everything else of the turn stays out.** The passage number, the prose a rule has just
     * laid and the levers the model holds all say how the character is to play; the questions
     * of the turn are answered by whoever speaks. What is left is what a mark is read against:
     * the instruction, which relevance is judged on, and a turn cut off by a clock, which is
     * not a sentence somebody chose to leave unfinished.
     */
    private fun judgeTail(present: Present): String {
        val lines = present.instructions.toMutableList()
        present.ending?.let {
            lines += when (it) {
                Ending.ByLength -> "The turn was cut off: the recording reached the time it " +
                    "was allowed. Treat it as unfinished."
                Ending.BySilence -> "The turn was sent because the learner fell silent. It " +
                    "may be unfinished; mark it as it stands."
            }
        }
        return if (lines.isEmpty()) "" else "For this turn:\n" + lines.joinToString("\n") { "- $it" }
    }

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
        val said = JSONObject()
        // **Before `spoken`, as the contract asks it to be written**: what the character says
        // is said knowing what has just been settled, so a replay that put the fact after the
        // reply would show the model an order it is being told never to write in.
        history[at].established.takeIf { it.isNotEmpty() }?.let { facts ->
            said.put("established", JSONObject().apply {
                facts.forEach { (key, answer) -> put(key, answer) }
            })
        }
        // The same shape the contract asks for, and the whole run: what the model wrote is
        // what it is shown again, so a turn of three utterances does not come back as one
        // sentence a passage later.
        said.put("said", JSONArray().apply {
            history[at].said.forEach { one ->
                put(
                    JSONObject()
                        .put("kind", if (one.isSpeech) "speech" else "stage")
                        .put("who", one.who)
                        .put("text", one.text),
                )
            }
        })
        // `intended` belonged to the learner's turn just before, which is where the pipeline
        // always puts it. Written out only when it really is there.
        history.getOrNull(at - 1)?.takeIf { it.fromLearner }?.let { said.put("intended", it.text) }
        return said.toString()
    }

    /**
     * Part 3 and part 4 as the message list: every past turn in its role, then the turn to come.
     *
     * **One copy, though two routes assemble a call.** What a turn of history becomes is the
     * same question whichever provider is asked, and the two loops that answered it separately
     * were two things to keep in step.
     *
     * An **aside** -- what an event told the leader, and the line a case it knows left when it
     * changed -- goes in the app's role and never in the leader's: read back as the leader's
     * own writing, it would take what it was told for something it had already said.
     */
    fun turns(history: List<Exchange>, transcript: String, present: Present): JSONArray =
        JSONArray().apply {
            history.forEachIndexed { at, exchange ->
                if (exchange.isAside || exchange.fromLearner)
                    put(message("user", exchange.text))
                else put(message("assistant", answered(history, at)))
            }
            put(message("user", turn(transcript, present)))
        }

    fun message(role: String, content: String): JSONObject =
        JSONObject().put("role", role).put("content", content)

    /**
     * The aptitudes a remark may be written about: the three the language model judges.
     *
     * The other two are the app's own -- nothing about the sounds or the timing reaches the
     * model -- so a line about them could only be invented.
     */
    val REMARKED = listOf("understanding", "correctness", "relevance")

    /** How long a remark may be. One line of the round-up on the narrowest screen holds 23. */
    const val REMARK_LIMIT = 45

    /** The notch names the contract above lists, straight from the catalogue that owns them. */
    val CORRECTNESS get() = Sheets.columnOf("correctness/correctness")
    val RELEVANCE get() = Sheets.columnOf("relevance/relevance")
    val STUMBLING get() = Sheets.columnOf("fluency/stumbling")
    val FOLLOWING get() = Sheets.columnOf("understanding/uptake")
}
