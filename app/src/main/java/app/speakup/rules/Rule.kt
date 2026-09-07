package app.speakup.rules

import app.speakup.levers.Position

/**
 * What changes during a sitting, and when.
 *
 * The simplest thing that could work is a list of `passage → change`, and it does not do, for
 * two separate reasons: not every trigger is a passage number, and the decider is not always
 * the written data. Both are settled by pulling apart three things the word *ramp* held
 * together -- **when**, **what**, and **who chooses** (`activity.md`).
 *
 * **The types are here and the resolution is in [Engine]** -- by waves, against one snapshot,
 * once per rule per moment -- because that is written where it runs. What this file owes the
 * activity is a field that persists: the rules are what a definition declares, so a sitting
 * whose settings were moved by a rule cannot recompute its own state without them.
 *
 * **Nothing here is a mini-language of conditions**, and that is deliberate: the kinds of
 * trigger are a closed, short list, declared like a lever's positions. The slope is to slip a
 * small expression language in, and the day that happens nobody can say what a definition
 * does without running it.
 */
data class Rule(
    /** Its own name, which is how a patch arms or disarms it and how the journal names it. */
    val key: String,
    val whenever: Trigger,
    /**
     * The packs on offer. **One element is no choice at all**, which is the ordinary rule.
     *
     * Several is what [Decider.Chance] and [Decider.Model] pick between, and it is the menu:
     * the app declares what is available and the model **picks a key**, it never invents one.
     */
    val choice: List<Pack>,
    val decider: Decider = Decider.Written,
    /** Whether it is armed to begin with. A patch moves this, and the switch carries a phrase. */
    val armed: Boolean = true,
) {
    init {
        require(choice.isNotEmpty()) { "$key: a rule with nothing to do" }
        require(decider == Decider.Written || choice.size > 1) {
            "$key: ${decider.name.lowercase()} chooses, but there is only one pack"
        }
    }
}

/**
 * Who chooses among the packs.
 *
 * **What a draw or the model chose is written to the activity**, which is the price of these
 * two deciders and follows the project's criterion: store what depends on something that will
 * not be found again. Keeping a random seed would cost less and would only replay right if
 * the code had not moved.
 */
enum class Decider { Written, Chance, Model }

/** A bundle of effects that go together. */
data class Pack(val effects: List<Effect>) {
    init { require(effects.isNotEmpty()) { "a pack with no effect" } }
}

/**
 * What a rule does. **Three, and the list is closed.**
 *
 * Finishing cannot be a lever: it would need a hard end, and finishing is neither harder nor
 * easier than carrying on -- it is a door one goes through once, not a position. And putting
 * it through the lives would force a challenge saying *ten passages and it is over* to invent
 * itself a single life, so to show a heart to someone who has none.
 */
sealed interface Effect {

    /**
     * Positions or moves, and possibly instructions.
     *
     * **Both forms are needed and neither replaces the other.** `vies.restantes ← 1` of the
     * scripted death is only sayable as a position; *lose a life* is only sayable as a move,
     * whoever writes the rule not knowing how many are left when it fires -- and there is no
     * dedicated kind of effect to put it in, the list being closed.
     */
    data class Patch(
        /** A key set to a position, absolutely. */
        val positions: Map<String, Position> = emptyMap(),
        /**
         * A key moved by so many notches, positive towards the hard end.
         *
         * **A move stops at the lever's bound**, which is not an error -- a ramp reaches its
         * top notch by design -- and **a move that changed nothing announces nothing**, since
         * saying *this tightens* with nothing tightened is a lie.
         */
        val moves: Map<String, Int> = emptyMap(),
        /** Instructions laid on a judged marking, or taken off it. */
        val instructions: List<Instructing> = emptyList(),
        /**
         * Rules armed or disarmed by name.
         *
         * **A rule is never removed; what it did is undone.** A patch moves a lever both ways,
         * so easing is a patch like any other, and a rule that should no longer apply is one
         * **disarmed by its switch**, which carries its phrase -- not a second silent storey
         * where nobody could say what a definition does without running it.
         *
         * This is also what writes the overload, and it saves two mechanisms: one occasion
         * that must do one thing the first time and another after is two rules, the first
         * disarming itself and arming the other. No cap on firings, and no declaration order
         * with any meaning to carry: at any instant exactly one of the two is armed.
         *
         * **A flag is this and not a fourth thing** (found in writing, 2026-09-06): the doc
         * names a flag as a third family a patch lays, read by a trigger and showing nothing
         * -- and then declares the kinds of trigger closed at six, none of which reads one.
         * Arming already does the work, and the doc's own chain-of-flags example is written
         * as one rule arming the next. So a flag is an arming switch with no phrase, and the
         * question of whether it wants a name of its own is open
         * (`../../../../../../TODO.md`).
         */
        val arming: Map<String, Boolean> = emptyMap(),
        val staging: Staging? = null,
    ) : Effect {
        init {
            require(
                positions.keys.none { it in moves.keys },
            ) { "a patch that both sets and moves ${positions.keys.intersect(moves.keys)}" }
        }
    }

    /** The sitting ends, carrying the outcome it opens. */
    data class Finish(val outcome: Outcome) : Effect

    /**
     * Prose put into the prompt -- *"the barman has worked out you lied to him"*.
     *
     * This is where the doc says to be generous: what nobody reads back is free. It does not
     * double [Staging], which is shown to the **learner** where this goes to the **model**,
     * and a rule may want one, the other, or both.
     *
     * [now] is what lets a scene make the AI speak of its own accord -- the alarm going off,
     * the passer-by, the character who prompts whoever has gone quiet. **A provoked turn falls
     * only at a passage's close or at the opening**, never while somebody is recording:
     * nothing cuts off a person who is still speaking.
     */
    data class Message(val prose: String, val now: Boolean = false) : Effect
}

/** What an ending opens. */
enum class Outcome {
    Passed,
    Failed,
    /** Neither is declared: the sitting's note at the A-B bar decides, as everywhere. */
    LetTheNoteDecide,
}

/**
 * An instruction laid on one judged marking, or taken off it.
 *
 * **An instruction has three readers**, and that is what tells it apart from every other
 * effect: the learner, who must read it to follow it; the character, who must know it to play
 * with it; and the judge, who scores by it. So **an instruction is always shown**, the
 * starting one included -- being scored on a criterion nobody told you gives a note you
 * cannot read back.
 *
 * [lasts] is a number of passages, or null for as long as the sitting. **A marking carries
 * several at once, each with its own life**: one slot replaced each time would not do the
 * moment two instructions have different lives, since the merged text would have to be
 * rewritten at every expiry.
 *
 * **An instruction can only tighten.** For a sheet to count less, or not at all, there is the
 * weight and nothing else: *"ignore the tenses"* is the forbidden case, a verdict softened by
 * a text, which no module may do.
 */
data class Instructing(
    /** Which judged marking it attaches to: the spans, the stumbling, or the following. */
    val marking: String,
    /** Null takes off whatever this rule had laid on that marking. */
    val text: String?,
    val lasts: Int? = null,
)

/**
 * The staging line a patch carries, written in advance and never produced by the model.
 *
 * The **mechanical** phrase is declared with the lever, so once for the whole app: *"five
 * seconds of silence"* cannot say *the barman is getting impatient* in a pub and *the
 * recruiter is waiting* in an interview. The face that plays the scene depends on the scene,
 * so it lives on the patch, in the rule that writes it.
 *
 * **The mechanical one is read after the reply and the narrative one says which of the two
 * places it takes.** They do different work: *"you have one life left"* states what has just
 * happened, so it is a receipt and comes after; *"a passer-by knocks into you"* sets the scene
 * for the turn that follows, so read after the reply it drops the passer-by out of nowhere.
 *
 * **Both are shown, and the narrative never replaces the mechanical.** Whoever reads only
 * *"the barman seems in a hurry"* does not know their turn now goes on its own after five
 * seconds, and will take it for a bug the first time it happens -- and the whole value of the
 * mechanical phrase is that they can reconstruct why their note moved.
 */
data class Staging(val text: String, val before: Boolean = false)

/**
 * When a rule fires. **Three moments, and they differ by what is computed at that instant**
 * rather than by taste.
 */
enum class Moment {
    /**
     * While recording. Two clocks run and nothing else exists yet -- the person is speaking,
     * no sheet has been read -- so a rule of this moment can read a clock and nothing more.
     * Not a restriction laid down: a fact about what exists.
     */
    Recording,

    /**
     * The turn has gone, the model has answered, the analysis has run. What concerns this
     * attempt is settled here: the two gates, and whether the conversation waits or carries
     * on. Within it the exact instant **follows from the sheet** and is not declared: a sound
     * sheet does not exist before the analysis has finished, a language sheet exists from the
     * moment the call returns.
     */
    EndOfAttempt,

    /**
     * The passage's note is the last attempt's and the count of attempts is known. Everything
     * else falls here: the patches, the ramp, the lives, the end of the sitting.
     */
    PassageClosed,
}

/**
 * **Six kinds, and the list is closed.**
 *
 * The first has only the first moment and the third only the last, so theirs is deduced. The
 * second has to say which, and that is a real distinction -- *"correctness is under B"* means
 * *block now* at the end of an attempt and *lose a life* at the passage's close.
 *
 * **No kind reads a clock outside the recording, and that is a chosen bound.** *"After two
 * minutes of conversation"* is not sayable: the only clocks are the turn's. The system runs by
 * turns, so a sitting's duration governs nothing that could fire between two utterances -- and
 * the list being declared closed, the absence would read as an oversight if it were not named.
 */
sealed interface Trigger {

    /** Which moment it belongs to, deduced wherever there is only one it could be. */
    val moment: Moment

    /** One of the two clocks reaches [ms]. Only ever while recording. */
    data class Clock(val which: Which, val ms: Int) : Trigger {
        override val moment = Moment.Recording

        /** The turn's own length, or the silence of the third capture position. */
        enum class Which { TurnLength, Silence }
    }

    /**
     * A node of the tree says something: which node, which of the readings it offers, and a
     * value.
     *
     * **The note is the only reading available on a branch**, a branch having neither elements
     * nor a raw figure; all three are there on a sheet. Reading the note **follows the
     * sensitivity**, which is what moves the A-E bounds: raising severity makes the rule fire
     * more often without anybody touching it, and that is a service.
     */
    data class Node(
        val path: String,
        val reads: Reads,
        /** A letter for [Reads.Note], a number in the sheet's own unit otherwise. */
        val value: String,
        override val moment: Moment,
    ) : Trigger {
        enum class Reads { Element, Figure, Note }
    }

    /** At passage [at], or every [every] passages. Only ever at a passage's close. */
    data class Passages(val at: Int? = null, val every: Int? = null) : Trigger {
        override val moment = Moment.PassageClosed
        init {
            require((at == null) != (every == null)) { "either a passage or every so many" }
        }
    }

    /**
     * Free prose the model answers yes or no to -- *"if he oversteps politeness"*.
     *
     * **It costs no call**: the model answers in the one already made, as an enumerated field,
     * the cheapest contract there is. It carries the **requested** flag with everything that
     * implies -- nobody checks it, it does not replay, the bench cannot test it.
     */
    data class Judged(val prose: String, override val moment: Moment) : Trigger

    /**
     * A lever **moved**: which one, and which way. It reads a **change** and never a position,
     * and its moment is that of the patch that moved it.
     *
     * This is what makes the front door composable. Reacting to a life lost would otherwise
     * mean gluing the same message onto every rule that takes one, so rewriting it as many
     * times as there are ways to lose one -- an exhaustiveness no author can hold.
     */
    data class Moved(val key: String, val harder: Boolean, override val moment: Moment) : Trigger

    /**
     * A lever **reaches** a value: which one, and which.
     *
     * Added for one case nothing else could write: **the scripted death**. A boss who cannot
     * be beaten, whose ending is a false death one walks through -- zero lives, a scene, then
     * a life comes back. Without it *"when the lives fall to zero"* is not sayable: [Moved]
     * fires at *every* life lost, and no other kind reads a position.
     */
    data class Reaches(
        val key: String, val position: Position, override val moment: Moment,
    ) : Trigger
}
