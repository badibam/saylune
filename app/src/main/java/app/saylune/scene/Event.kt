package app.saylune.scene

import app.saylune.activity.Text

/**
 * When an event can go. **Each moment sees only what exists at that instant**: while
 * recording, the clocks alone; an app case read off the turn, from the end of an attempt on.
 */
enum class Moment {
    /** Before the opening, on the situation screen: the only moment the learner is asked. */
    Launch,
    Opening,
    Recording,
    /** A question has just been answered, between the leader's short call and its reply. */
    Answer,
    AttemptEnd,
    PassageClose,
    /** The scene is over. A coda: what goes here can add a last word, never un-finish. */
    Closing,
    /** A story's own moment, between two of its scenes, where it branches. */
    AfterScene,
}

enum class Op { Eq, Ne, Lt, Le, Gt, Ge }

/** Which way a case went. Up and down on a number or an ordered list; harder, easier on a lever. */
enum class Way { Up, Down, Harder, Easier }

/**
 * What an event reads. **One test per event, and activation stands for "and"**: two tests
 * combined would open the way to "or", then to brackets, so to a language of conditions in
 * which nobody could say what a file does without playing it.
 *
 * **A test on an empty case is always false**, save [Empty], which is the test made for it --
 * or *a pronunciation note at C or below costs a life* would take one for an analysis that
 * never ran.
 */
sealed interface Test {

    /** The case read, or null for a test on the moment alone. */
    val case: String?

    /** Nothing but the moment: *at the opening, Nico says your name*. */
    data object None : Test {
        override val case: String? = null
    }

    /** A state, which goes again at every moment it is true: every bad sentence costs a life. */
    data class Holds(override val case: String, val op: Op, val value: Value) : Test

    /** A state on a number: a multiple of [n], zero excluded -- *every five passages*. */
    data class Every(override val case: String, val n: Int) : Test {
        init { require(n > 0) { "$case: every $n" } }
    }

    data class Empty(override val case: String) : Test

    /** A change into a state, which goes once per change: *`named` becomes true*. */
    data class Becomes(override val case: String, val op: Op, val value: Value) : Test

    /** A change by its direction: *`lever.lives` goes down*, *a lever gets harder*. */
    data class Goes(override val case: String, val way: Way) : Test
}

/** Who reads a text: the leader, the learner, or both. The judge reads what the learner reads. */
enum class Reader { Leader, Learner, Both }

/** Where the learner reads a text: in the thread, said by somebody, or in a notification. */
sealed interface Form {
    data object Notice : Form
    data class Thread(val who: String) : Form
}

/** What an ending opens. *By note* is the sitting's note at the A-B bar, as everywhere. */
enum class Outcome { Passed, Failed, ByNote }

/**
 * How far the leader may go when asked to write a case. **A staircase with a floor**: what the
 * conversation has established always wins, and at the first step it may not know.
 */
enum class Reach { Said, Deduce, Invent }

sealed interface Effect {

    /** Give a case of the file or a lever a value. */
    data class Put(val case: String, val value: Value) : Effect

    /** Move a number by [by], stopping at its bound without error. */
    data class Shift(val case: String, val by: Double) : Effect

    /**
     * Move an ordered list one step: a lever [Way.Harder] or [Way.Easier], by the hard end it
     * declares, so without the author knowing that the echo is hardest at its first step; a
     * case of the file [Way.Up] or [Way.Down], in the direction it declares.
     */
    data class Step(val case: String, val way: Way) : Effect

    data class Switch(val event: String, val on: Boolean) : Effect

    data class Finish(val outcome: Outcome) : Effect

    /**
     * A text, and who reads it. For the leader alone it is a secret, in English; for the
     * learner alone the leader knows nothing of it -- a receptionist saying *"you have one
     * chance left"* would break its own fiction.
     */
    data class Tell(val text: Text, val reader: Reader, val form: Form = Form.Notice) : Effect

    /**
     * An instruction: a text tied to a judged marking, which can only tighten. The judge reads
     * it to judge by, the leader to play by -- to make the occasion, and not to sabotage it.
     *
     * [hidden] keeps it from the leader, never from the learner: the judge follows the learner,
     * and an instruction hidden from both would touch no mark at all. [lasts] is a number of
     * passages, or null for the rest of the scene.
     */
    data class Instruct(
        val key: String,
        val marking: String,
        val text: Text,
        val hidden: Boolean = false,
        val lasts: Int? = null,
    ) : Effect {
        init { require(lasts == null || lasts > 0) { "$key: lasts $lasts passages" } }
    }

    /** Lift the instruction [key] before its time. */
    data class Lift(val key: String) : Effect

    /**
     * A turn of the leader nobody spoke before: *"The traveller arrives and argues with Lou"*.
     * A written line is a [Tell] into the thread; this is the direction the leader plays.
     */
    data class Direct(val prose: String) : Effect

    /** Ask the leader to write [case] in its next call, as far as [reach] lets it. */
    data class AskLeader(val case: String, val reach: Reach) : Effect

    /** Ask the learner to fill [case] on the situation screen. At the launch only. */
    data class AskLearner(val case: String, val ask: Text) : Effect

    /**
     * Draw a value of [case] at random. [weights] is by value, one by default: the weight is
     * the draw's and not the case's, so the same case draws with other odds at another moment.
     */
    data class Draw(val case: String, val weights: Map<String, Int> = emptyMap()) : Effect

    // The three below are never written in a file: a question unfolds into them at loading.

    /** A question is put: the passage does not close until it has an answer. */
    data class Pose(val question: String) : Effect

    /** It has its answer, or the author's path once it has been asked enough times. */
    data class Release(val question: String) : Effect

    /** It was not answered, and is asked again in the form its asker calls for. */
    data class Reask(val question: String) : Effect
}

/**
 * One event: a moment, being active, and a test.
 *
 * [question] names the question it was unfolded from, where it was: while a question is being
 * asked again, every event of the scene waits but that question's own.
 */
data class Event(
    val key: String,
    val at: Moment,
    val test: Test = Test.None,
    val effects: List<Effect>,
    val active: Boolean = true,
    val question: String? = null,
) {
    init { require(effects.isNotEmpty()) { "$key: an event that does nothing" } }
}
