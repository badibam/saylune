package app.saylune.chain

import app.saylune.capture.Ending
import app.saylune.judged.Judgement
import app.saylune.conversation.Speaker
import app.saylune.levers.Positions
import app.saylune.scene.Kind
import app.saylune.scene.Reach
import app.saylune.scene.Role

/**
 * The language model: one who speaks, one who judges.
 *
 * **Two calls, because they are two jobs.** [reply] writes what the character says and nothing
 * else; [judge] reads the turn that has just been said and marks it. The single call that did
 * both is gone, and what it cost is the reason: the staging and what the learner asked to be
 * steered around sat in the judge's context, where they have no business, and what held them
 * at arm's length was a sentence of prompt that nothing checked. **They cannot reach the judge
 * now**, there being no parameter that carries them -- the debt is closed rather than watched.
 *
 * **The two may live at two providers**, and nothing here presumes they do not. What they
 * share is the shape: a stable head, a history that grows only at the tail, and a short tail of
 * its own for each call. A cached prefix belongs to one provider and one key, so it is each
 * side that earns its own, from one turn to the next.
 *
 * **`intended` is written by the one who speaks and handed to the judge as it stands.** The
 * contract has it drafted before the character's voice is taken, and that scaffolding is what
 * makes the model commit to `aunt` rather than answer around `ant`. Were the judge to rebuild
 * it on its own, a turn where the two disagreed would have the model voice say a word nobody
 * spoke, and every mark of the turn would land beside its sound. So there is one `intended` in
 * the whole system, produced where the scaffolding earns something, read by the judge and by
 * the analysis. What it costs is written down: its quality now follows a model that may have
 * been picked for its voice or its speed (`../../../../../../TODO.md`).
 *
 * **The judge is given the reply that has just been said**, at the end of its tail. That is the
 * one defect the split introduces and the parade against it: in one call the spoken repair and
 * the marks came out of the same act, so they agreed by construction; in two they can diverge.
 * A repair heard with nothing on screen is exactly what `reference.md` writes against -- without
 * the trace, the discretion turns on itself -- so the judge marks knowing what has already been
 * picked up, and the divergence runs one way only.
 */
interface Conversation {

    /**
     * Answer [heard] in the context of [history]. Throws [ChainFailure]; the caller retries
     * from the kept audio file rather than asking for the sentence again.
     *
     * [scene] is what this activity is, frozen at launch: who is speaking, and what is being
     * played.
     */
    suspend fun reply(
        history: List<Exchange>, heard: List<Word>, scene: Scene, present: Present = Present(),
    ): Reply

    /**
     * Mark the turn [said] in the context of [history], knowing what was [answered] to it.
     *
     * **The signature is the contract.** Nothing of the character reaches here: not the
     * staging, not the persona, not the passage number, not what the learner asked to be
     * steered around. What does reach it is [situation] -- the half of the brief addressed to
     * the learner, which carries the instruction -- because that is what relevance is judged
     * against, and `reference.md` requires it: *"I'll go there"* is perfect English that a
     * challenge to speak in the past makes off-target.
     *
     * [said] is `intended`, exactly as [reply] wrote it. [answered] is the reply that was
     * given, echo and continuation joined as the learner heard them.
     *
     * **Never called on a provoked turn.** Nobody spoke into it, so there is no sentence to
     * mark -- and the eight lines that used to tell the model to leave those fields out are
     * gone with it, rather than being kept up.
     */
    suspend fun judge(
        history: List<Exchange>,
        said: String,
        answered: String,
        situation: String,
        present: Present = Present(),
    ): Verdict
}

/**
 * What governs the turn being answered, rebuilt every time.
 *
 * [positions] are the sitting's levers, of which only the **requested** ones reach the model
 * -- the ones it holds, which exist as nothing but an instruction. What the app does itself
 * has nothing to say here.
 *
 * [ending] is how the recording stopped, and the app knows it rather than leaving the model to
 * guess from a transcript: faced with *"I went to the"*, rebuilding *"I went to the market"*
 * would have the model's voice say a word nobody said, and every mark on the turn would land
 * beside its sound.
 */
/**
 * Why the character is taking a turn nobody spoke into.
 *
 * One, and it is a rule of the scene having laid some prose in front of the character. A
 * sitting picked back up is not one of these: nothing is asked of the model there, the last
 * turn being said again from what is on file.
 */
enum class Provoked { ByRule }

data class Present(
    val positions: Positions = Positions(),
    val ending: Ending? = null,
    /**
     * Which passage of the sitting is being spoken, counting from one.
     *
     * **A passage and not a turn**: it holds every attempt at one thing the learner set out
     * to say, and the model sees only the last of them anyway, so counting recordings would
     * count something nobody shows it.
     *
     * **Sent every turn, and it is a fact rather than a verdict.** The front-door rule keeps
     * the state away from the model because a receptionist who lets slip *"you have one
     * chance left"* breaks his own fiction -- but that guards **judgements about the
     * learner**. Where the scene stands is the shape of the scene, an author's business, and
     * a character plays it. What it means is left to the brief: the permanent context says
     * the number carries no instruction of its own, so in a free conversation, which has no
     * brief and no ending, it means nothing. **Not proved**: that the model leaves it alone
     * when nothing asks it to is a bench check, not a guarantee.
     *
     * One by default, which is what a sitting with nothing said yet is on.
     */
    val passage: Int = 1,
    /**
     * The instructions standing on the judged markings, in the words a definition wrote.
     *
     * **A instruction has three readers, which is what tells it from every other effect**: the
     * learner, who has to read it to follow it; the character, who has to know it to play with
     * it; and the judge, who marks against it. This is the character's copy.
     */
    val instructions: List<String> = emptyList(),
    /**
     * The turn an event has directed, in an author's words -- *the traveller arrives and argues
     * with Lou*.
     *
     * **It is here and not in the conversation**, where every other text for the leader sits:
     * it governs the turn about to be taken rather than saying something about the world, and
     * a direction read thirty turns after the moment it asked for would have nothing to direct.
     * What an event merely tells the leader is placed in the conversation at the passage it
     * went, and stays there ([Exchange.told]).
     */
    val directed: List<String> = emptyList(),
    /**
     * Whether this turn has **no learner turn in front of it**.
     *
     * A rule can make the character speak of its own accord -- the alarm going off, the
     * passer-by, the last word of a scene that is ending -- and then there is nothing to
     * transcribe, nothing to mark and nothing to answer. It is declared here rather than read
     * off an empty transcript: an emptiness is indistinguishable from a recording nobody spoke
     * into, and the two want opposite things.
     *
     * **It falls only at the opening, at a passage's close, or on the way back into a thread**,
     * never while somebody is recording: nothing cuts off a person who is still speaking.
     *
     * **The reason is carried and not just the fact**, because the two are not the same turn to
     * take -- though what reads it back is the fact alone: the reader is told there was no
     * learner turn, which both reasons share, and only the prompt cares which. A rule asked for one and the character has an instruction in front of it; nobody
     * asked for the other -- the learner has simply come back -- and a character told it is
     * acting on an instruction it never got would invent one.
     */
    val provoked: Provoked? = null,
    /**
     * The questions the app is putting on this turn, whose answers are **required fields**.
     *
     * **The app serves them and the model answers on the spot.** That is the inversion the
     * mechanism rests on: not a menu of empty fields the model fills when it likes, but a
     * question put at the moment its author wrote, so that the presence of an answer is
     * checkable like any other field of the contract.
     *
     * They ride on the first call after the moment that put them, exactly as a rule's message
     * does: a question due at the end of an attempt is one the call for that attempt has
     * already gone without, and the next call is the earliest there is.
     */
    val asking: List<Asked> = emptyList(),
)

/**
 * A case the leader is asked to write on this turn: its key, the line that says what it is,
 * what it holds, and how far the leader may go.
 */
data class Asked(val key: String, val about: String, val kind: Kind, val reach: Reach) {
    companion object {
        /**
         * The one answer that is not the fiction: **it does not know**. Offered at the first
         * step of [Reach] alone, and it leaves the case as it was.
         */
        const val DONT_KNOW = "I don't know"
    }
}

/**
 * What this activity is, frozen at launch: part 2 of the instruction.
 *
 * **This is where the persona lives, and it had to leave the permanent context.** *Warm and
 * curious* is a trait of character, and written into the part every activity shares it would
 * have governed the hostile bouncer and the bored receptionist too. What is permanent is what
 * the app is and what it returns; who is speaking comes from the definition, where an author
 * can write someone else.
 *
 * **The name is not here**, because the model has no use for one: what a sitting is called is
 * a label on a tile, decided by the file it came from, and the brief already says everything
 * about the scene that the character has to know.
 */
data class Scene(
    /** Where the learner stands, in English, with the cases it cites filled in. */
    val situation: String = "",
    /** Who the leader plays, each with the description that goes to it. */
    val cast: List<Role> = emptyList(),
    /**
     * What the learner asked the conversation to stay away from, in their own words.
     *
     * **It is not the activity's and it sits here anyway**, which is worth saying rather than
     * leaving to be noticed: it is a fact about the person, identical in every scene they
     * open. What puts it in part 2 is that part 2 is the last stable thing before the history
     * -- it does not change inside a sitting, so it belongs in the head of the instruction that
     * a provider's cache can keep, and not in part 4, which is rebuilt and resent every turn.
     *
     * Blank is the ordinary case and nothing at all is written then.
     */
    val avoid: String = "",
)

/**
 * One past turn, as the model should remember it.
 *
 * [established] is what the model settled on that very turn, and it travels with it rather
 * than in a section of its own. **The fact is laid where it was established**, which carries
 * its date for nothing -- a fact settled at the twelfth passage and put in a heading would
 * read as a given of the start, and the model would play somebody who had always known it --
 * and which does not break the cached prefix, the history being stable at the head and growing
 * at the tail where a heading would grow in the middle.
 */
/**
 * One turn of the record the model is shown: whose it is, and what was said in it.
 *
 * **The app's own turn is a run and is carried as one.** It used to be a single line, so from
 * the passage after it the model saw one sentence where it had written three -- it lost its
 * own narration out of its own memory, which is the real structural cost of a turn being a
 * run. What is put back is the shape it wrote.
 *
 * The learner's turn is one utterance by construction: one recording, one sentence.
 */
data class Exchange(
    val fromLearner: Boolean,
    val said: List<Said>,
    val established: Map<String, String> = emptyMap(),
    /**
     * What the app told the leader **here**, nobody having spoken it: the texts an event sent
     * it, and the line a case it knows left when it changed.
     *
     * An exchange carrying these carries nothing else. It sits at the passage where the texts
     * went and stays there, so the leader reads them dated -- a barman who changed mood four
     * times has four traces in their places, and can play the path and not just the state.
     * Laid in a heading rebuilt every turn instead, they would read as a given of the start.
     */
    val told: List<String> = emptyList(),
) {

    /** The turn as one string, for whoever reads it whole rather than utterance by utterance. */
    val text: String get() = if (isAside) told.joinToString("\n") else said.joinToString(" ") { it.text }

    /** Whether this is the app talking to the leader rather than a turn somebody spoke. */
    val isAside: Boolean get() = told.isNotEmpty()

    companion object {
        /** The learner's turn, which is always one speech of theirs. */
        fun ofLearner(text: String) =
            Exchange(true, listOf(Said(Said.Kind.Speech, Speaker.LEARNER, text)))

        /** What the app tells the leader between two turns, in the author's words. */
        fun aside(told: List<String>) = Exchange(false, emptyList(), told = told)
    }
}

/**
 * One utterance of a turn: what kind it is, who it belongs to, and its words.
 *
 * [who] is a character's key, drawn from the cast the scene declares, or [Speaker.NARRATOR]
 * for the one member no cast writes. It is the cheapest enumerated answer there is, and the
 * synthesis takes a voice per utterance without being asked twice, the cache being keyed by
 * text and voice.
 */
data class Said(val kind: Kind, val who: String, val text: String) {

    /** Whether it is spoken **to** the learner, which is what an echo may open. */
    val isSpeech: Boolean get() = kind == Kind.Speech

    /** What an utterance of a turn is. */
    enum class Kind {
        /** A character speaking, to the learner or to another character. */
        Speech,

        /** Matter of the fiction, which is **not** said to the learner. */
        StageDirection,
    }

    companion object {
        /**
         * How many utterances a turn may hold. **Set by hand, and to be revised by ear.**
         *
         * Not a limit of technique: it is the wait before the learner may speak. A model that
         * runs away brings back a dozen, which is a dozen syntheses and a minute of listening.
         * The project bounds everywhere -- 45 characters for a remark, 30 s for a turn -- and
         * a bound carrying its reason beats a drift found in use.
         */
        const val CEILING = 6

        /**
         * The silence between two utterances of a turn, in milliseconds. **Set by hand.**
         *
         * The second of the two numbers this shape needed and neither was measured; both are
         * to be revised by ear once a shipped tile exercises them.
         */
        const val PAUSE_MS = 1_000L
    }
}

/**
 * What the one who speaks sends back: the learner's words settled, and what to say.
 *
 * **Nothing marked is in here.** The markings are [Verdict]'s, and the two objects are the two
 * jobs: this one is written by whoever takes the character's voice, and it is the only place
 * `intended` is produced.
 */
data class Reply(
    /**
     * The learner's own turn, written out, hesitations and all. **Null on a provoked turn**,
     * where nobody spoke and there is nothing to write out.
     *
     * It is a repair of the **transcript** and never of the grammar, and it is drafted before
     * the character's voice is taken: faced with *"my ant"*, the model has to settle on `aunt`
     * or `ant` before it can answer either. It travels on to the judge and to the analysis
     * unchanged, so there is one of it in the whole system.
     */
    val intended: String?,
    /**
     * The turn: **a run of utterances**, said one after another, never empty.
     *
     * It was one line, plus [echo] as the opening of that same line, and every turn went out
     * under a single identity. A turn is a run because a scene is: one character speaks and
     * then another, a stage direction falls between two replies, or a reply stands alone.
     *
     * **No order is imposed** -- not *stage direction then speech*, not one speaker per turn.
     * What bounds it is [Said.CEILING], and what bounds it is not technique: a model that
     * runs away brings back a dozen, which is a dozen syntheses and a minute of listening,
     * and what is felt there is the wait before the learner may speak.
     *
     * **None of them picks the slip up**: that is [echo]'s work, and [echo] opens the first
     * of them that is a speech.
     */
    val said: List<Said>,
    /**
     * The short line that picks the slip up, or null when there was nothing to pick up.
     *
     * **It is not a competing reply, it is the opening of one.** The call returns this and
     * the turn as one utterance cut in two, and the app composes them -- so nothing is ever
     * retracted, and the founding gesture of the project, *"Ah, you're twenty-five! And where
     * do you work?"*, is the two of them joined.
     *
     * **It opens the first *speech* of the turn and never a stage direction.** That is the one
     * real constraint on the free order: it is read in one breath with what follows, and laid
     * on a narrator's line it becomes the narrator remarking on the learner's grammar, which
     * this project refuses everywhere. A turn may open on a stage direction all the same --
     * the echo simply skips to the first reply.
     *
     * The app hears it **alone** in one case only: the passage is to reword and the
     * conversation waits.
     *
     * **Who decides there is a slip is now the one who speaks**, on the turn it is reading,
     * and no longer a field it marked a moment earlier. That is what the split moves here, and
     * the parade is on the judge's side: it is shown this line, so it marks knowing what has
     * already been said back.
     */
    val echo: String?,
    /**
     * What the model settled, by question key. Empty where the turn put none.
     *
     * **It comes before `spoken` in the object the model writes**, and that is not a detail of
     * order: the contract says each field written conditions the next, so the character speaks
     * knowing what has just been established and never the other way round.
     *
     * It stays with the one who speaks for that same reason. A question is a hole in the
     * fiction, its answer is reread by another character, and it is the reply that has to be
     * consistent with it -- none of which is the judge's business.
     */
    val established: Map<String, String> = emptyMap(),
)

/**
 * What the one who judges sends back: the markings, and the key it picked from a menu.
 *
 * `faulty` is long gone, absorbed by the marking. It was a boolean over the whole turn, which
 * flattened the fact that a passage can carry several faults and made it impossible to mark
 * the portion concerned; one notch per group of words settles both.
 */
data class Verdict(
    /** What was marked on the learner's turn, indexed into the `intended` it was given. */
    val judgement: Judgement,
    /**
     * The key the model picked out of the menu a rule offered, or null when none was offered.
     *
     * Free prose on the way out, listed keys on the way back: the app declares what is
     * available, the model **picks a key** and never invents one. Choosing from a list is what
     * models do best; calibrating a fresh constraint is not.
     *
     * **It has no channel yet** -- no field of the contract asks for one -- and it is on this
     * side because deciding is judging (`../../../../../../TODO.md`).
     */
    val choice: String?,
)
