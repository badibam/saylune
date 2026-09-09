package app.saylune.conversation

import android.content.Context
import app.saylune.chain.ChainFailure
import app.saylune.chain.Conversation
import app.saylune.chain.Exchange
import app.saylune.chain.Present
import app.saylune.chain.Recognition
import app.saylune.chain.Reply
import app.saylune.chain.Scene
import app.saylune.chain.Word
import app.saylune.activity.Activity
import app.saylune.activity.Chosen
import app.saylune.activity.Definition
import app.saylune.activity.Definitions
import app.saylune.activity.Question
import app.saylune.activity.Settled
import app.saylune.activity.Status
import app.saylune.capture.Ending
import app.saylune.capture.Playback
import app.saylune.capture.Take
import app.saylune.analysis.Analysed
import app.saylune.analysis.AnalysedSound
import app.saylune.analysis.timed
import app.saylune.analysis.Analysis
import app.saylune.analysis.Readiness
import app.saylune.debug.Trace
import app.saylune.judged.Judgement
import app.saylune.judged.Kept
import app.saylune.judged.Marked
import app.saylune.fluency.Fluency
import app.saylune.notes.Measured
import app.saylune.notes.noteOver
import app.saylune.notes.Passage as Scored
import app.saylune.rules.Effect
import app.saylune.rules.Engine
import app.saylune.rules.Moment
import app.saylune.rules.Notice
import app.saylune.rules.Outcome
import app.saylune.rules.State as RuleState
import app.saylune.rules.Instructing
import app.saylune.rules.Trigger
import app.saylune.rules.holds
import app.saylune.notes.Sheeting
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets
import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Levers
import app.saylune.levers.Positions
import app.saylune.levers.severityOn
import app.saylune.levers.Stepped
import app.saylune.marking.AddedSound
import app.saylune.marking.TurnMarking
import app.saylune.providers.ChosenSynthesis
import app.saylune.providers.words
import app.saylune.store.ArchiveDao
import app.saylune.store.Recordings
import app.saylune.store.activity
import app.saylune.store.row
import app.saylune.store.utterance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID

/**
 * Where a turn has got to, and **[Idle] is the app saying the learner may go on**.
 *
 * That is more than a caption, which is why the last cran exists. The big button reads it,
 * and what it opens is the next passage -- so idle before the sound's gate has been read
 * would be an answer given without knowing, since that gate is read at the end of the
 * analysis and can hold the passage back. [Measuring] is the app still deciding.
 */
enum class Phase { Idle, Hearing, Thinking, Speaking, Measuring }

/** Whose recording a manual gesture plays. */
enum class Side { Model, Learner }

/**
 * Who said it -- **an identity, and no longer learner-or-AI**.
 *
 * An activity points at a **cast** rather than at one interlocutor, so an utterance has to say
 * which of them is speaking: the synthesis picks a voice **per utterance**, which the cache
 * takes in its stride, being keyed by text and by voice. On the model's side the AI returns
 * the key of the character speaking -- the cheapest enumerated answer there is.
 *
 * **The learner is a reserved identity and not a case beside the others.** Everything asks the
 * same question of an utterance -- who said this -- and a sum type with the learner on one
 * side would make every reader take a branch before reading a name. A human model would be a
 * third identity and would change nothing about the shape.
 *
 * Stored by name, like every enum that reaches the store: a rank is a promise never to reorder
 * a list, which nobody remembers making.
 */
@JvmInline
value class Speaker(val key: String) {

    /** Whether this is the learner, which is the one thing the whole app branches on. */
    val isLearner: Boolean get() = key == LEARNER

    companion object {
        /**
         * The learner's own key. Reserved: a cast may not declare a character called this,
         * or an utterance of a character would read as one of the learner's own.
         */
        const val LEARNER = "learner"

        /**
         * The one voice a free conversation has.
         *
         * **What this leaves owed**: the identity is in place and one voice fills it, so the
         * cast and several characters are written as a field and have no screen
         * (`../../../../../../TODO.md`).
         */
        const val SAYLUNE = "saylune"

        val Learner = Speaker(LEARNER)
        val Ai = Speaker(SAYLUNE)
    }
}

/**
 * One thing said: a text, a speaker, the audio, the analysis.
 *
 * **Everything said in the app is one of these** -- the answer of the model as much as the
 * learner's sentence (`docs/activity.md`). Most carry no analysis, and the
 * answers carry none at all; keeping one table for all of it is what gives the thread of the
 * conversation for free, as a plain ordered run.
 *
 * [repeats] points at the utterance this one says again. That is the whole of saying a
 * sentence again on the spot: another utterance in the same conversation, tied to the one it
 * replays, with no second notion needed for it.
 */
data class Utterance(
    val speaker: Speaker,
    val text: String,
    /**
     * The activity this belongs to, and **always exactly one**.
     *
     * One parent and not several is what keeps every request on the matter simple, whichever
     * activity the utterance came from.
     */
    val activity: String,
    /** The recording it was said in. The answers of the model have none. */
    val said: File? = null,
    /**
     * Which capture position was in force, or null on anything that was not recorded.
     *
     * **Every turn carries its own**, because it is what says whether its silences mean
     * anything, and **nothing aggregates across positions**: adding a turn caught at the
     * thumb to one caught automatically gives a figure that looks like fluency without being
     * it (`docs/activity.md`).
     */
    val capture: String? = null,
    /**
     * How it ended: null for a turn sent by hand, otherwise which clock closed it.
     *
     * A fact about the recording and not a measure, at the capture position's side. Two
     * readers: the language model, whom it forbids to complete an unfinished sentence, and
     * the sheet of the interrupted turn.
     */
    val ending: Ending? = null,
    /**
     * What was marked and what was measured, or null when nothing read this.
     *
     * Null is not "nothing to report": a turn the gate held back and a turn read clean must
     * not be drawn alike. [faulty] tells the two apart.
     */
    val marking: TurnMarking? = null,
    /**
     * What the model settled on this very turn, by question key. Empty on nearly every one.
     *
     * **The fact is kept where it was established**, and everything else about a question is
     * derived from that: the run in order gives the series of answers, and where in the run it
     * sits gives the passage it is dated by. A table beside the run would be a second source
     * that could fall out of step with it, and the prompt wants it here anyway -- a fact goes
     * back to the model in the turn it was settled on, not in a heading.
     */
    val established: Map<String, String> = emptyMap(),
    val sounds: List<AnalysedSound> = emptyList(),
    /**
     * How long the recording ran, in milliseconds. Null where nothing analysed it.
     *
     * **It comes from the analysis pass and not from the file**, so it is on the same clock as
     * the sounds' own times: what it is read for is the stretch between the last sound and the
     * end of the recording, and a duration measured elsewhere would make that stretch a
     * subtraction between two clocks.
     */
    val recorded: Int? = null,
    /**
     * What this attempt made of each sheet, by path. Empty where nothing measured it.
     *
     * **Kept rather than recomputed**, which is the project's own criterion: one stores what
     * depends on something that will not be found again. Three of the eleven sheets are in that
     * case even before the purge -- the continuity, the longest silence and the pace are read
     * off timings the analysis pass produced and nothing else holds -- and the purge puts the
     * rest there too by erasing the recordings.
     *
     * It fills in **two goes**, at the two moments the doc names: the judged sheets when the
     * call returns, the sound's when the analysis ends. A passage whose words' gate closed
     * therefore carries the first set and not the second, which is the truth about it: a sheet
     * absent from here never counts as a zero.
     */
    val measured: Map<String, Float> = emptyMap(),
    /**
     * Which side of the model the pace fell on: **true for slower**, null where it was not
     * measured.
     *
     * A fact beside the figure and never inside it. The pace's figure is a **distance**,
     * symmetric by construction so that twice as slow and twice as fast weigh the same in the
     * note; the side is what the chevrons read on the line that names the turn, and what tells
     * a condition *never more than 20% slower* from *never faster*.
     */
    val slower: Boolean? = null,
    /**
     * The synthesis of this very text, kept to be heard again.
     *
     * One render serves three times, exactly as the doc says: a yardstick for the measure, a
     * model to hear, and a model to hear again at every retry. An utterance that repeats
     * another has none of its own -- the text is the same, so the model is the same file, and
     * [modelOf] reads it through [repeats] rather than keeping a second name for it.
     */
    val model: File? = null,
    /**
     * What the language model marked on this turn, or null when nothing judged it.
     *
     * It replaces the single `faulty` boolean, which was a verdict over the whole turn: that
     * flattened the fact that a passage can carry several faults and left the portion
     * concerned with nowhere to be marked. One notch per group of words settles both.
     */
    val judged: Judgement? = null,
    /** The folder this take was written to on disk, or null when nothing was kept. */
    val take: String? = null,
    /**
     * The utterance this says again, **by its identity**. Null when it says something new.
     *
     * Its identity and not its place in the run: a place is only true for as long as nothing
     * is inserted before it, and it stops being true the moment the run is written down and
     * read back. An identity survives both.
     */
    val repeats: String? = null,
    /**
     * Which of the two repairs this is, or null when it opens its passage.
     *
     * [repeats] says **which** utterance is being attempted again and this says **how**, and
     * the two are not one field: a rewording remakes the exchange where a repeat leaves it
     * alone, so a reader that could not tell them apart would have to guess whether to call
     * the model again.
     */
    val attempt: Attempt? = null,
    /**
     * For a turn of the AI, the learner's utterance it answers. Null on anything else.
     *
     * **A reply taken out of the thread is not deleted from the store** -- it is superseded,
     * exactly as an attempt is, by pointing at the utterance it answered. A rewording makes a
     * fresh call as though it were the first attempt, so the new reply answers the new attempt
     * and the old one stops being the passage's last. Deleting it instead would lose what was
     * actually said to the learner, which is a fact about the sitting.
     */
    val answers: String? = null,
    val id: String = UUID.randomUUID().toString(),
    /** When it was said. Its order in the run is the run's; this is the wall clock. */
    val at: Long = System.currentTimeMillis(),
    /**
     * The engine that produced [marking], or null when nothing read this.
     *
     * Everything stored carries the version of what produced it: a reading redone with
     * different weights does not give the same numbers, and two eras of measurement that add
     * up in silence make a comparison that is wrong with nothing to say so.
     */
    val engine: String? = null,
)

/**
 * A take that has not got through the chain, with the facts it was recorded under.
 *
 * The audio alone was not enough: a retry after a link gave way has to send the **same turn**,
 * and a turn resent without its capture position and its ending would look hand-sent -- so
 * the sheet of the interrupted turn and the instruction that forbids completing an unfinished
 * sentence would both read something that never happened.
 */
data class Pending(val take: Take, val capture: String?, val ending: Ending?)

data class ConversationState(
    /**
     * The conversation itself, which is an activity like any other.
     *
     * No default: a sitting is opened from a definition and there is no other way to make
     * one, so a state that made itself an activity would be the second way.
     */
    val activity: Activity,
    /**
     * The definition this sitting came from, which is what names it on screen.
     *
     * **Not a setting read back**: an origin only ever groups, and how the sitting was set is
     * on its own line. What is read here is the identity -- the name a tile carries, and the
     * short one the status line has room for -- which lives nowhere else, nothing in the app
     * being named by the model.
     */
    val definition: Definition,
    /**
     * Everything said, oldest first. The thread is this run and nothing else carries it.
     *
     * A single ordered table rather than a list of exchanges with marks and takes and models
     * in maps beside it: those maps were four sources keyed by one index, and every one of
     * them could drift from the others by a single append in the wrong order.
     */
    val utterances: List<Utterance> = emptyList(),
    val phase: Phase = Phase.Idle,
    /** The provider's own words when a link gave way. Cleared by the next attempt. */
    val failure: String? = null,
    /** Kept so a failed send is retried without saying the sentence again. */
    val pending: Pending? = null,
    /**
     * Which recording a manual gesture plays: the voice being imitated, or one's own.
     *
     * It governs the play button and a tap on a word, and nothing else. A tap on a symbol
     * in the readout names its side outright -- the model's column and the learner's are
     * side by side there -- so a selector would only be able to contradict what the reader
     * just pointed at.
     */
    val side: Side = Side.Model,
    /**
     * How fast anything played by hand is played. Never applied to the answer being spoken:
     * that is the conversation, and slowing it would be the app deciding how the
     * conversation goes rather than the learner deciding what to examine.
     */
    val speed: Float = 1f,
    /** Whether the marks are on at all, settled once for the conversation. Null until asked. */
    val analysis: Readiness? = null,
    /**
     * Why each gate closed on the open passage's last attempt, or null where it let through.
     *
     * Held rather than recomputed on every read, and that is not a second source: they are
     * written at exactly the two moments the doc names -- when the call returns, and when the
     * analysis ends -- and cleared the instant another attempt starts. What they read is gone
     * by then anyway, the analysis of one attempt not being kept in memory.
     */
    val wordsGate: Closing? = null,
    val soundGate: Closing? = null,
    /**
     * The continuation the app is holding because it played the echo instead.
     *
     * **Nothing has to be refabricated for the way out of a blocked passage**: the call
     * returned the continuation and the echo together, and in *waits* only the echo was played.
     * When the rewordings run out, the app plays the continuation it was holding. No second
     * call, nothing retracted -- which is what makes that way out free.
     */
    val held: String? = null,
    /**
     * What the rules have made of the sitting: the **effective** positions, which rules are
     * armed, the instructions standing, and the outcome once something has ended it.
     *
     * **The declared position of a lever and its effective position are two things**, and
     * confusing them is what made it look contradictory that the settings are fixed for the
     * whole sitting while the lives run out. The definition writes the first, which never
     * moves and lives on the activity's line; the patches move the second, which is what the
     * learner lives.
     *
     * Held as a value rather than recomputed on every read because a wave has to see what the
     * wave before it did -- and **null until a moment has run**, which is what keeps it from
     * going stale: a state carrying a copy of settings that a later activity replaced would
     * answer with the settings of a sitting one has left. Null means nothing has moved yet, so
     * [positions] reads the declared ones and there is nothing to keep in step.
     *
     * **What it does not do yet is survive a reopening**: replaying the journal to rebuild it
     * needs the facts each past moment was read against, which nothing keeps, so a sitting
     * picked up again starts from its declared positions (`../../../../../../TODO.md`).
     */
    val effective: RuleState? = null,
    /**
     * What the last moment left to show, until the pop-up has shown it.
     *
     * Cleared by the screen and not by a clock here: a notice nobody has seen is a change the
     * learner cannot reconstruct, and the whole value of the mechanical phrase is that he can.
     */
    val notices: List<Notice> = emptyList(),
    /**
     * How many times each answer of the AI has been played again, by utterance.
     *
     * **In memory and never stored**, which is the same limit every effective position has: a
     * sitting picked up again starts from its declared ones, so a reopened conversation has its
     * replays back (`../../../../../../TODO.md`). Kept here rather than on the utterance
     * because it is not a fact about what was said -- it is where a lever stands.
     */
    val replayed: Map<String, Int> = emptyMap(),
) {

    /**
     * Where every lever of this sitting sits **right now**.
     *
     * The effective positions and not the declared ones: everything that reads a setting --
     * the clocks, the buttons, the gates, the prompt -- wants where the lever is at this
     * instant, which is the declared position plus whatever the patches have moved. The
     * declared ones stay on the activity's line, where nothing rewrites them.
     */
    val positions: Positions get() = (effective ?: stateOf(activity)).positions

    /** Where the rules stand, which is the declared state until one of them has fired. */
    val standing: RuleState get() = effective ?: stateOf(activity)

    /**
     * The attempts at the utterance [of] that still stand, oldest first.
     *
     * Each one is addressed by its own identity, which every listening gesture then carries:
     * a reading whose audio came from one take and whose times came from another plays a
     * different part of the sentence.
     *
     * **It starts at the last rewording**, the words having changed there: everything said
     * before it is an attempt at a sentence the learner has since replaced, so showing its
     * marks would put marks on words that are no longer on screen.
     *
     * **And an attempt with no marking is here too.** It was going to be filtered out, one
     * that carries none never having been read -- but a rewording exists from the moment the
     * call returns and is read seconds later, so filtering left the screen showing the
     * sentence that was just replaced for the whole of the analysis.
     *
     * Derived and never stored. It is a walk of the run, and a walk of the run cannot fall
     * out of step with the run -- which is exactly what a list kept beside it used to do.
     */
    fun readings(of: String): List<Utterance> {
        val root = utterances.firstOrNull { it.id == of } ?: return emptyList()
        val attempts = utterances.filter { it.id == root.id || it.repeats == root.id }
        val reworded = attempts.indexOfLast { it.attempt == Attempt.Rewording }
        return attempts.drop(if (reworded < 0) 0 else reworded)
    }

    /**
     * The model to imitate for the utterance [of], read through what it repeats.
     *
     * A repeat says the same text, so it is the same render; asking the utterance it repeats
     * is what keeps one file under one text instead of a copy per attempt.
     */
    fun modelOf(of: String): File? = utterances.firstOrNull { it.id == of }?.let { spoken ->
        spoken.model ?: spoken.repeats?.let { id -> utterances.firstOrNull { it.id == id }?.model }
    }

    /**
     * Whether the sitting is over, which is a fact about the activity and not about the turn.
     *
     * **Nothing more is said in it once this is true**: no take is sent, no passage closes,
     * the microphone does not arm. What is left is reading it back, which is what a finished
     * sitting is for.
     */
    val over: Boolean get() = activity.status == Status.Finished

    /** Every passage of this run, oldest first. Derived, and never stored. */
    fun passages(): List<Passage> = Passage.of(utterances)

    /** The passage still open, which is the last one. Null before anything has been said. */
    fun open(): Passage? = passages().lastOrNull()

    /**
     * Where the open passage stands.
     *
     * **Derived and not stored**: it follows from the two gates and the counts of attempts,
     * both already there. The words come before the sound, which is the order the gates
     * themselves are read in -- a sentence about to be rewritten is not one to say again.
     *
     * **When the attempts of a kind run out, that repair is over** and the passage stands open
     * again: *"waits" does not guarantee the repair, it guarantees the attempts get spent*, so
     * a passage to reword can close without ever having been reworded.
     */
    fun standing(): Standing {
        val passage = open() ?: return Standing.Open
        val settings = positions
        if (wordsGate != null && passage.spare(Attempt.Rewording, settings)) {
            return Standing.ToReword
        }
        if (soundGate != null && passage.spare(Attempt.Repeat, settings)) {
            return Standing.ToSayAgain
        }
        return Standing.Open
    }

    /**
     * Whether the big button is available, which is what closes a passage.
     *
     * **In "waits" it is not**, or one would leave a blocked passage by simply saying something
     * else and the attempts would stop being the only way out. **It comes back when they run
     * out**, or nothing would ever move on -- which is why *waits* guarantees the spending and
     * not the repair.
     *
     * The two advances are separate levers for the reason that already split the attempts: a
     * challenge aiming only at pronunciation keeps its behaviour on the words' side whatever
     * happens, and with one lever neither of the two could be written.
     */
    fun closes(): Boolean = if (over) false else when (standing()) {
        Standing.ToReword -> !waits(Levers.ADVANCE_WORDS.key)
        Standing.ToSayAgain -> !waits(Levers.ADVANCE_SOUND.key)
        else -> true
    }

    private fun waits(key: String) = (positions.of(key) as? At)?.name == "waits"

    /**
     * The run as the language model should remember it: **the last attempt of each passage,
     * with the reply made to that one**.
     *
     * It used to be *every utterance that repeats nothing*, which was right for exactly as long
     * as an attempt carried the same text. A **rewording** carries different words, so keeping
     * the opener would send the model a sentence the learner has since replaced, together with
     * a reply that was made to something else.
     *
     * **The property is that the history never carries a reply and a sentence that do not
     * answer each other.** So the reply is looked up by what it answers rather than by where it
     * sits in the run: a superseded reply is still in the store, and it is only its pointing at
     * an attempt that is no longer the last that keeps it out of here.
     *
     * What is left out is deliberate on both counts. **Repeats never reach the model** -- pipe
     * B alone, on a text already settled -- and what the model sees of a passage is the last
     * attempt, like the screen: the character has no business knowing the sentence was said
     * three times, and hearing it stutter three times pushes it to remark on it. The price is
     * owned: it will not pick up a fault that keeps coming back of its own accord, and a rule
     * is what that takes.
     */
    fun history(): List<Exchange> {
        val passages = passages()
        val out = mutableListOf<Exchange>()
        var next = 0
        utterances.forEach { one ->
            // **A provoked turn answers nobody and belongs to no passage**, so what says when
            // it was said is its place in the run and nothing else. It falls between passages
            // by construction -- at the opening, or at a passage's close -- so putting each
            // passage out whole at its opener keeps the two in the order they happened.
            if (!one.speaker.isLearner && one.answers == null) {
                out += Exchange(false, one.text, one.established)
            } else if (one.id == passages.getOrNull(next)?.opener?.id) {
                out += exchangesOf(passages[next])
                next++
            }
        }
        return out
    }

    private fun exchangesOf(passage: Passage): List<Exchange> = listOfNotNull(
        Exchange(fromLearner = true, text = passage.last.text),
        standingReply(passage)?.let { Exchange(false, it.text, it.established) },
    )

    /**
     * The reply that stands for [passage], or null before one has been made.
     *
     * The last reply of the **passage**, and not the one answering the last attempt: a repeat
     * is never answered at all -- it is pipe B alone, on a text already settled -- so looking
     * it up by the last attempt would lose the reply the moment the learner said the sentence
     * again. A rewording, which does make a fresh call, is the case where the two coincide.
     */
    private fun standingReply(passage: Passage): Utterance? =
        utterances.lastOrNull { reply -> passage.attempts.any { reply.answers == it.id } }

    /**
     * The run as the screen draws it: **what opens a passage, and the reply that stands**.
     *
     * Two things are left out, and the same fact leaves them: an attempt is not drawn where it
     * sits in the run, it is one of the readings grouped under the utterance it repeats, which
     * is where the learner is looking. And a reply that a rewording superseded goes with it --
     * a rewording remakes the exchange, so the answer made to the sentence that was replaced
     * is an answer to something nobody said any more.
     *
     * **Superseded is not deleted**: the old reply stays in the store, where it is what was
     * actually said to the learner, and it is only its pointing at an attempt that is no
     * longer the last that keeps it off the screen. The same rule keeps it out of [history],
     * which is what makes the two agree.
     */
    fun thread(): List<Utterance> {
        val standing = passages().mapNotNull { standingReply(it)?.id }.toSet()
        return utterances.filter {
            when {
                it.speaker.isLearner -> it.repeats == null
                // A provoked turn answers nobody, so nothing can supersede it.
                it.answers == null -> true
                else -> it.id in standing
            }
        }
    }

    /**
     * What the model has settled so far, by question key, oldest first.
     *
     * **Derived from the run and never stored.** Each answer sits on the turn that settled it,
     * and where that turn sits in the run is what dates it -- so this is a walk, and a walk of
     * the run cannot fall out of step with the run.
     *
     * **Nothing is ever overwritten**: a later answer *succeeds* the one before it rather than
     * correcting it. *The queen was safe at passage 5 and is not at 15* is a story, not a
     * mistake put right, so whoever reads one says which it reads -- the last, or the run.
     */
    fun settled(): Map<String, List<Settled>> {
        val out = mutableMapOf<String, MutableList<Settled>>()
        var passage = 0
        utterances.forEach { one ->
            if (one.speaker.isLearner && one.repeats == null) passage++
            one.established.forEach { (key, answer) ->
                out.getOrPut(key) { mutableListOf() } += Settled(passage, answer)
            }
        }
        return out
    }
}

/**
 * Where a sitting's rules stand when nothing has fired yet: the declared positions, the rules
 * armed at the start, the instructions the definition laid.
 *
 * A rule is **never removed**; what it did is undone, and being armed is what says whether it
 * is watching. So the arming starts from what each rule declares and moves only by a patch.
 */
fun stateOf(activity: Activity): RuleState = RuleState(
    positions = activity.settings,
    armed = activity.rules.filter { it.armed }.map { it.key }.toSet(),
    instructions = activity.instructions,
)

/**
 * One turn through the three links: what was heard, what to answer, and the voice answering.
 *
 * The chain is bloquante but repairable by construction. Nothing here throws a turn away:
 * the recorded file stays in [ConversationState.pending] until a run of the chain succeeds,
 * so a failure costs a button and never a spoken sentence.
 *
 * The analysis reads `intended` and the very file this kept, and it runs **after** the answer
 * has been said: the two pipes are independent, and the conversation is never made to wait on
 * the measure.
 */
class TurnPipeline(
    private val context: Context,
    private val recognition: Recognition,
    private val conversation: Conversation,
    private val synthesis: ChosenSynthesis,
    private val analysis: Analysis,
    private val archive: ArchiveDao,
) {
    /**
     * The free conversation, read once off the assets it ships in.
     *
     * Read here rather than per sitting because it does not change while the app runs, and
     * read at all rather than defaulted because it **is** where the free conversation's
     * settings live -- a default wired in code would be a second place they could be written.
     * A missing file fails loudly, which is what a release that dropped it deserves.
     */
    private val free = Definitions.of(context, Definitions.FREE_CONVERSATION)

    private val _state = MutableStateFlow(ConversationState(Activity.from(free), definition = free))
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    /**
     * Where a turn runs. **A turn outlives the screen that started it.**
     *
     * Measured on the phone: the chain was launched from the conversation screen's own scope,
     * and sending a take pushes the passage's notes over that screen -- so the screen left the
     * composition, its scope was cancelled, and the turn died at its first resumption, just
     * after the recognition. The phase stayed on *hearing* for good, with nothing running and
     * nothing said. The first turn of a sitting escaped it, having no earlier passage to show.
     *
     * A turn belongs to the sitting and not to what is on screen: it keeps an audio file, it
     * spends money at three providers, and it writes to the store. Whoever is looking at what
     * while it runs is none of its business.
     */
    val turns = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * One writer at a time, over every entry that changes the run.
     *
     * Each of those entries reads the state, works across a suspension -- a recognition, a
     * call, a playback -- and writes back what it read plus its own change. Two of them
     * running at once therefore lose one of the two writes, and nothing in the types says so.
     *
     * The screen does keep them apart today, by refusing every gesture while `phase` is not
     * `Idle`. That is a convention of one screen, reread from scratch at every screen added,
     * and it reads `busy` out of the very state that concurrent writes would rewrite. The
     * lock puts the property in the structure instead.
     *
     * Not reentrant: what is under it -- [write], [update], [name], [keep], [examine] -- is
     * private and never takes it. The two listening gestures that only read a snapshot stay
     * outside it on purpose (see [hear]).
     */
    private val writing = Mutex()

    /**
     * Pick the conversation back up, and settle whether the marks are on -- once, before the
     * first turn.
     *
     * **Carrying on is continuing an activity that did not finish**, and for the conversation
     * that is a policy on its status rather than a structure of its own: an unfinished one is
     * reopened as it stands, utterances and all. A fresh one is only started when there is
     * none to carry on.
     *
     * Readiness is asked every time and never read back from the store. It is a fact about
     * the device and not about the conversation -- the weights can have gone since -- and
     * asking it here rather than on the first turn is what keeps an app from discovering it
     * has no engine after somebody has already spoken.
     */
    suspend fun prepare() = writing.withLock {
        if (!opened) {
            opened = true
            // **A sitting whose definition the release no longer ships cannot be opened**, and
            // this is the one call that meets one without anybody having asked for it: the
            // tiles are the shipped files, so nothing else offers a way back to it. A fresh
            // conversation is started instead rather than the launch failing -- which is what
            // it did, the whole app going down on one retired scene.
            val latest = archive.latest()?.activity()?.id
            if (latest == null || !openLocked(latest)) beginLocked()
            // Once, here, and here only: at startup nothing is in flight and nothing has
            // been recorded, so a file the store does not name is one nothing will name.
            runCatching { Recordings.sweepOrphans(context, archive.recordings().toSet()) }
                .onFailure { Trace.fail("recordings: not swept", "why" to it.message) }
        }
        recheckAnalysis()
    }

    /**
     * Ask again whether the marks can be produced, and keep an answer that says they can.
     *
     * **An `On` is settled for the sitting, an `Off` is not.** The doc's rule is that a turn
     * stays analysable as long as the conversation started with its model loaded -- it guards
     * against *losing* the marks halfway, and says nothing about gaining them. Kept both ways,
     * the refusal survived the model arriving: whoever brought the three files while the app
     * was open was told no until the process was killed, on a screen that was at that moment
     * declaring the same files verified.
     *
     * Cheap when the answer has not moved: the engine remembers its refusal against the files
     * it refused over and only looks at them again when they change.
     */
    suspend fun recheckAnalysis() {
        if (_state.value.analysis is Readiness.On) return
        val readiness = analysis.readiness()
        _state.update { it.copy(analysis = readiness) }
    }

    /** So a second call does not open a second conversation while the first is loading. */
    private var opened = false

    /**
     * Open the conversation [id], utterances and all.
     *
     * Nothing has to have been closed for this: an activity that did not finish is one to
     * carry on with, and every conversation but the one being had is in that state.
     */
    suspend fun open(id: String) = writing.withLock { openLocked(id) }

    /**
     * Whether it opened. False says the sitting is **there and cannot be carried on**.
     *
     * The one way that happens is a release that no longer ships the definition it came from.
     * Reading it under a substitute is refused -- that would be a sitting nobody can read back
     * -- so the sitting stays in the store, readable, and simply does not reopen. What is
     * missing is a screen that says so: the tiles are the shipped files, so there is no way
     * back to it and nothing tells the learner why (`../../../../../../TODO.md`).
     */
    private suspend fun openLocked(id: String): Boolean {
        val row = archive.activity(id) ?: return false
        val activity = row.activity()
        val definition = activity.origin?.let { from ->
            runCatching { Definitions.of(context, from.definition) }.getOrElse {
                Trace.fail("conversation: its definition is not shipped any more",
                           "activity" to id, "definition" to from.definition)
                return false
            }
            // A sitting made before the free conversation was itself a definition has no
            // origin, and there was no other kind of sitting then: reading `free` for it is a
            // fact about that release, not a stand-in for something missing.
        } ?: free
        opened = true
        val run = archive.utterances(activity.id).map { it.utterance() }
        _state.update {
            it.copy(
                activity = activity,
                definition = definition,
                utterances = run,
                // **From the declared positions, not from where the last sitting's rules left
                // them**: replaying the journal to rebuild the effective state needs the facts
                // each past moment was read against, and nothing keeps those
                // (`../../../../../../TODO.md`).
                effective = null,
                notices = emptyList(),
                // The run is another conversation's now, so anything that pointed into the
                // old one has to go: a retry of a recording from the conversation just left
                // would send it into this one.
                pending = null,
                failure = null,
            )
        }
        Trace.add("conversation: opened", "activity" to activity.id,
                  "utterances" to _state.value.utterances.size.toString())
        return true
    }

    /**
     * Start a fresh conversation.
     *
     * The one being left is not marked finished. Nothing prevents starting another, and
     * calling the old one finished would be the app deciding it is over on no evidence -- it
     * stays there to be carried on with.
     */
    /**
     * Open a sitting of [definition], with the holes it declares filled.
     *
     * The definition is a parameter and no longer the free conversation alone: what stands
     * behind the free door is one tile per definition the app ships, and starting one is this
     * call with that file. [answers] are what the learner typed into its slots, [gender] what
     * he chose for its main character where the file left it open.
     */
    suspend fun begin(
        definition: Definition = free,
        answers: Map<String, String> = emptyMap(),
        gender: String? = null,
    ) = writing.withLock { beginLocked(definition, answers, gender) }

    private suspend fun beginLocked(
        definition: Definition = free,
        answers: Map<String, String> = emptyMap(),
        gender: String? = null,
    ) {
        val fresh = Activity.from(definition, answers, gender)
        opened = true
        archive.put(fresh.row())
        _state.update {
            it.copy(
                activity = fresh,
                definition = definition,
                utterances = emptyList(),
                effective = null,
                notices = emptyList(),
                pending = null,
                failure = null,
            )
        }
        Trace.add("conversation: begun", "activity" to fresh.id)
        // **The opening, which is a moment like any other.** A scene that opens with anything
        // does it here: a rule on the opening trigger lays its staging line and, where it asks
        // for one, the character's first turn -- said before anybody has spoken to it.
        fire(Moment.Opening, world())
        // **Scheduled and not awaited.** The opening turn is a call and a playback, and the
        // screen has to be there while they happen rather than after them: awaited, pressing
        // *start* sat on the previous screen until the character had finished speaking. It
        // takes the lock this still holds, so it begins the instant the opening is settled.
        turns.launch { writing.withLock { provokeIfAsked() } }
    }

    /**
     * Put the sitting [id] out of reach: it stays in the store, and no screen offers it again.
     *
     * **This is what *start over* answers yes to** (`docs/ui.md`), and it is written down
     * rather than held on the screen it was answered on. Held there, it came back the moment
     * one left the situation screen and opened it again: the old sitting stood as if nothing
     * had been said, the form was gone, and it could be started over a second time.
     *
     * Abandoned is the status the project already had for it, and it is the honest one: the
     * sitting did not finish, nobody judged it, and it is not coming back.
     */
    suspend fun abandon(id: String) = writing.withLock {
        val row = archive.activity(id) ?: return@withLock
        val ended = row.activity()
            .copy(status = Status.Abandoned, endedAt = System.currentTimeMillis())
        archive.update(ended.row())
        // **The one in hand may be it**: the launch reopens the most recent sitting, which is
        // the one the tile is standing on. Left running in the state, the next thing to write
        // the activity down would put it back.
        if (_state.value.activity.id == id) _state.update { it.copy(activity = ended) }
        Trace.add("sitting: put out of reach", "activity" to id)
    }

    /** Every conversation, most recent first, for the tiles to find their sittings in. */
    fun conversations() = archive.conversations()

    /** How many passages each sitting holds, by sitting: what a tile shows of itself. */
    fun passages() = archive.passages(Speaker.LEARNER)

    /**
     * Run [audio] through the chain, or run again what a previous failure left pending.
     *
     * [capture] and [ending] are the two facts the recording carries: which position was in
     * force, and which clock closed the turn when one did. They are held on the pending take
     * as well, so a retry after a link gave way sends the same turn with the same facts
     * rather than a turn that looks hand-sent.
     */
    suspend fun submit(
        audio: Take? = null, capture: String? = null, ending: Ending? = null,
    ) = writing.withLock { submitLocked(audio, capture, ending) }

    /**
     * Say the open passage differently -- **a rewording, which remakes the exchange**.
     *
     * In *waits* the AI has only played an echo and its answer is built on the corrected
     * version; in *carries on* it has spoken, and the app **makes the call afresh as though
     * this were the first attempt** -- the prompt carries nothing of the earlier wordings but
     * the counters -- plays the new answer, and the previous one leaves the thread.
     *
     * What it costs is a whole run of the chain per rewording, and hearing two answers to
     * nearly the same sentence. What it buys is a thread that never contradicts itself.
     *
     * **A repeat is the other gesture and it relaunches nothing**, the words being the same:
     * there is nothing new to answer. It stays what it was, an exercise -- [redo].
     */
    suspend fun reword(
        of: String, audio: Take, capture: String? = null, ending: Ending? = null,
    ) = writing.withLock { submitLocked(audio, capture, ending, rewords = of) }

    private suspend fun submitLocked(
        audio: Take?, capture: String?, ending: Ending?, rewords: String? = null,
    ) {
        // **Nothing more is said in a sitting that is over.** The screen already refuses the
        // gesture; refusing it here too is what makes that a property of the sitting rather
        // than a convention of one screen.
        if (_state.value.over) return
        val held = _state.value.pending
        val take = audio ?: held?.take ?: return
        // The turn as it was said, blanks and all: it is what is stored, what the analysis
        // reads, and what is played back. The recognition gets the other one.
        val turn = take.whole
        // A retry carries the facts the take was recorded under, never fresh ones.
        val position = if (audio == null) held?.capture else capture
        val closedBy = if (audio == null) held?.ending else ending
        Trace.turn()
        Trace.add(
            if (audio == null) "turn: sending again what was kept" else "turn: a new recording",
            "file" to turn.path,
            "bytes" to turn.length().toString(),
            "sent to the recognition" to take.spoken.length().toString(),
        )
        _state.update {
            it.copy(phase = Phase.Hearing, failure = null,
                    pending = Pending(take, position, closedBy))
        }
        // **Taken before the call and put back if the call gives way.** What the front door
        // carries is a rule's message and the questions of the moment just past, and the call
        // is what delivers them: dropped on a link that gave way, a question the app chose to
        // put would simply never be asked, and nothing would say so.
        val door = frontDoor()
        try {
            // **No silence goes to the network**: the recognition is given the speech
            // alone, which is what it has any use for and what it charges for.
            val heard = recognition.transcribe(take.spoken)
            if (heard.isEmpty()) {
                Trace.add("turn: nothing was said, dropped")
                putBack(door)
                // Holding the button by accident is not a failure and must not read as one.
                _state.update { it.copy(phase = Phase.Idle, pending = null) }
                return
            }

            _state.update { it.copy(phase = Phase.Thinking) }
            val reply = conversation.reply(
                _state.value.history(), heard,
                scene = Scene(
                    brief = _state.value.activity.brief,
                    cast = _state.value.activity.cast,
                ),
                // What governs this turn: the levers the model holds, and how the recording
                // stopped -- which the app knows and does not leave it to guess.
                present = door.let {
                    Present(
                        _state.value.positions, closedBy,
                        // A rewording is another attempt at the passage that is already open;
                        // a turn that rewords nothing opens the next one. Derived from the
                        // run, like the passages themselves, so it cannot fall out of step
                        // with it.
                        passage = _state.value.passages().size + if (rewords == null) 1 else 0,
                        instructions = it.instructions,
                        said = it.prose,
                        asking = it.asked,
                    )
                },
            )

            // **A turn nobody prompted has nothing to judge, and this is not one.** The
            // reader hands back a judgement on every path but the provoked one, so this can
            // only be a provider that broke its contract -- which is a failure like any
            // other, and the recording is kept for another go at it.
            val judged = reply.judged
                ?: throw ChainFailure("the model judged nothing of what was said")

            // The two utterances are held by identity from here on. Their place in the run is
            // where they happen to sit, and it is not what addresses them: everything that
            // follows -- the take, the marks, the analysis -- names them by their id.
            val said = Utterance(
                speaker = Speaker.Learner,
                activity = _state.value.activity.id,
                text = judged.intended,
                said = turn,
                capture = position,
                ending = closedBy,
                judged = judged,
                // A rewording is an attempt at the passage it points back at; a turn that
                // rewords nothing opens one of its own.
                repeats = rewords,
                attempt = rewords?.let { Attempt.Rewording },
            )
            // The call that has just answered belongs to the passage this attempt is in, and
            // the passage is named by the utterance that opened it: a rewording is an attempt
            // at the one it points back at.
            Trace.askedFor(rewords ?: said.id)
            _state.update {
                it.copy(
                    utterances = it.utterances + said,
                    pending = null,
                    // Another attempt: what the gates said of the one before it is about to
                    // be replaced, and holding it until then would leave the screen saying
                    // *reword it* about a sentence already reworded.
                    wordsGate = null,
                    soundGate = null,
                )
            }
            // The speech-only copy was transport and nothing names it: it goes as soon as no
            // retry can want it again. The startup sweep would get it too, and waiting for a
            // launch to reclaim what is already spent is not a reason to leave it.
            take.spoken.delete()

            write(said.id)

            val marked = judged.words()
            val groundless = marked.correctness.any { it.notch == Gates.UNSAYABLE }
            // **The words' gate is read here, and it is the first of the two moments** -- and
            // before anything is played, because what is played depends on it.
            val closing = wordsGate(said, judged, groundless)

            // **The call returned the continuation and the echo together, and the app plays
            // one.** So nothing is ever contradicted inside an attempt: it is literally *the
            // model plays, the app decides*, the model supplying the matter of both outcomes
            // without settling which. The echo is played only where the passage is to reword
            // **and** the conversation waits -- *waits* forces the repair, so only an echo is
            // heard until it is made; *carries on* offers it, and the conversation advances
            // whatever happens.
            val echoing = closing != null && reply.echo != null &&
                (_state.value.positions.of(Levers.ADVANCE_WORDS.key) as? At)?.name == "waits"
            val spoken = if (echoing) reply.echo!! else reply.spoken
            // **The thread carries what was heard**, which is why the reply is made after the
            // gate and not before it. Written before, it held the continuation while the echo
            // was what played, so the screen ran ahead of the voice by a whole reply -- and
            // the learner read an answer to a sentence he was being asked to say differently.
            val answer = Utterance(
                speaker = Speaker.Ai,
                activity = _state.value.activity.id,
                text = spoken,
                answers = said.id,
                // On the turn that settled it, which is where the prompt puts it back and
                // what dates it. Empty on nearly every turn.
                established = reply.established,
            )
            _state.update {
                it.copy(utterances = it.utterances + answer, phase = Phase.Speaking)
            }
            write(answer.id)
            if (echoing) {
                Trace.add("turn: the echo is played, the continuation is held")
                _state.update { it.copy(held = reply.spoken) }
            }
            Playback.play(synthesis.speak(spoken, synthesis.voice())) {
                // The number the doc puts on the chain, and the only one the learner feels.
                Trace.add("turn: first sound")
            }
            Trace.add("turn: said, and done")

            // **The turn is not idle because the voice has stopped.** What follows still
            // decides whether the passage may be left -- the sound's gate is read at the very
            // end of the analysis -- and idle here would light the big button on an answer
            // nobody has yet, the press then waiting on the lock with nothing on screen
            // saying why. Idle is said once there is something to say it about.
            _state.update { it.copy(phase = Phase.Measuring) }

            // A new name arrives only when there is a reason for one; any other turn leaves
            // the conversation called what it was called.

            if (closing != null || groundless) {
                Trace.add(
                    "turn: no sound analysis",
                    "why" to if (groundless) "a word does not exist in the language"
                             else "the words' gate closed",
                    "in cause" to (closing as? Closing.Aptitudes)?.names?.joinToString(),
                    "said" to judged.intended,
                )
                // **And it is read all the same, on the one channel it has.** The sound
                // analysis does not run, but what the judge marked is exactly what has to be
                // seen -- it is the reason the passage is being sent back, and a turn shown
                // as plain text leaves the learner told to say it differently with nothing on
                // screen saying what was wrong. So a marking with the text and no sound in
                // it, which is the truth about this turn; and it is what makes the passage's
                // row of commands appear, a reading being what the run is walked for.
                update(said.id) { it.copy(marking = TurnMarking.wordsOnly(judged.intended)) }
                write(said.id)
                // Kept even so, and especially so: a turn like this is a real learner fault
                // the recognition could not have guessed, which is what the fidelity bench
                // is short of.
                keep(said.id, Takes.keep(context, turn, null, heard, judged.intended, true,
                                         null, stumbling = judged.stumbling,
                                         turn = turnOf(said.id),
                                         attempt = attemptOf(said.id)))
            } else {
                examine(
                    of = said.id, said = turn, heard = heard,
                    text = judged.intended,
                    kept = Kept.of(judged.intended, judged.stumbling),
                    stumbling = judged.stumbling,
                )
            }
            _state.update { it.copy(phase = Phase.Idle) }
        } catch (failure: ChainFailure) {
            Trace.fail("turn: a link gave way, the recording is kept", "why" to failure.message)
            putBack(door)
            _state.update {
                it.copy(
                    phase = Phase.Idle,
                    failure = failure.message,
                    pending = Pending(take, position, closedBy),
                )
            }
        }
    }

    /**
     * **The first of the two moments: the words' gate, read when the call returns.**
     *
     * What it needs exists already -- the judged sheets, and the ones the audio and the text
     * give. The sound analysis has not run, and that is the ordering itself: when this closes,
     * the sound's gate never gets the chance to speak.
     *
     * **Nothing judged goes out when it closes.** Every judged sheet computes, since they are
     * what decide whether it closes; putting them out by their own decision would be circular.
     */
    private suspend fun wordsGate(
        said: Utterance, judged: Judgement, groundless: Boolean,
    ): Closing? {
        val activity = _state.value.activity
        val marked = judged.words()
        val measured = Sheeting.of(judged, analysed = null, timed = null)
        val scored = Scored(
            keptWords = marked.kept.size, difficulty = null, measured = measured,
        )
        // The figures the gate read, kept on the line rather than dropped with it. The gate
        // hands on a verdict; what a passage is read back from later is the numbers.
        note(said.id, measured)
        val closing = activity.weights?.let { weights ->
            Gates.words(
                measured = measured,
                passage = scored,
                settings = _state.value.positions,
                weights = weights,
                sensitivity = ::sensitivityOf,
                truncated = said.ending != null,
                keptWords = marked.kept.size,
            )
        }
        // Nothing weighing anything means no node has a note and no gate has a failure to
        // declare -- a free conversation is in that state until its definition is written --
        // and it says nothing about the rules: a clock, a passage count and a lever are all
        // readable with no tree at all. So the moment fires either way.
        _state.update { it.copy(wordsGate = closing) }
        endOfAttempt(
            measured, scored,
            Material(
                correctness = marked.correctness,
                relevance = marked.relevance,
                stumbling = marked.stumbling,
            ),
        )
        return closing
    }

    /**
     * **The end of an attempt, and the rules of that moment fire here.**
     *
     * Within it the exact instant **follows from the sheet** rather than being declared: a
     * language sheet exists from the moment the call returns, a sound sheet only once the
     * analysis has finished. So this runs twice, and each run sees the sheets that exist by
     * then -- which is the order of the two gates the doc already writes.
     */
    private suspend fun endOfAttempt(measured: List<Measured>, scored: Scored, of: Material) {
        fire(Moment.EndOfAttempt, world(measured, scored, of))
    }

    /** What the rules read of the sitting right now. */
    private fun world(
        measured: List<Measured> = emptyList(),
        scored: Scored? = null,
        of: Material = Material(),
        clocks: Map<Trigger.Clock.Which, Int> = emptyMap(),
    ) = World(
        // How many passages have **closed**, which is one fewer than the run holds while the
        // last one is still open.
        passage = (_state.value.passages().size - 1).coerceAtLeast(0),
        measured = measured,
        scored = scored,
        weights = _state.value.activity.weights,
        sensitivity = ::sensitivityOf,
        material = of,
        clocks = clocks,
    )

    /**
     * How severe this sitting is on [sheet], as a position of its sensitivity.
     *
     * **The sensitivity is a lever attached to a sheet**, and it is the one place the settings
     * touch a note: going towards severe tightens, always and for everybody, where a weight's
     * direction depends on the learner.
     *
     * A key this sitting says nothing about answers with the catalogue's declared default,
     * which is the middle position -- and one the catalogue does not declare **fails
     * outright**, which is what makes the sensitivities levers rather than a value read off
     * the line beside them.
     */
    private fun sensitivityOf(sheet: Sheet): Int = _state.value.positions.severityOn(sheet)

    /**
     * What the learner did differently from the model, on the turn just spoken.
     *
     * The model is synthesised from the **kept words** of `intended`, in the very voice that
     * just answered -- which is the default the doc sets: the model to imitate is the voice
     * already being heard, and the accent setting governs both because there is no third
     * thing to align. The kept words and not the whole of it, because the model is what is
     * given to imitate: making it say *"It was, like, um, I went to the…"* would give a
     * stumble to copy. On a clean turn the two are the same string.
     */
    private suspend fun examine(
        of: String, said: File, heard: List<Word>, text: String, kept: Kept,
        stumbling: List<Marked>,
    ) {
        recheckAnalysis()
        val readiness = _state.value.analysis
        if (readiness !is Readiness.On) {
            keep(of, Takes.keep(context, said, null, heard, text, false, null,
                                stumbling = stumbling,
                                turn = turnOf(of), attempt = attemptOf(of)))
            return
        }
        try {
            val model = synthesis.speak(kept.text, synthesis.voice())
            val analysed = analysis.examine(said, model, text, kept)
            update(of) {
                it.copy(marking = analysed.marking, sounds = analysed.sounds, model = model,
                        recorded = analysed.recorded, engine = readiness.version)
            }
            write(of)
            soundGate(of, analysed, stumbling)
            keep(of, Takes.keep(context, said, model, heard, text, false, analysed,
                                stumbling = stumbling,
                                turn = turnOf(of), attempt = attemptOf(of)))
        } catch (failure: ChainFailure) {
            // The model has to be synthesised, so this branch depends on the network the
            // analysis itself does not. The turn stands either way: it was answered and
            // said, and only its marks are missing.
            Trace.fail("analysis: no model to measure against", "why" to failure.message)
        }
    }

    /**
     * Close the open passage, which is what the big button does.
     *
     * **Nothing else closes a passage** -- not the AI's answer, which arrives before it in
     * *carries on*, and not time passing. So retaking a sentence is always possible: it is
     * enough not to move on, and that is the best moment for it, the marks being on screen and
     * the model just synthesised. Once closed it is never retouched again: it is listened to
     * and reread for good.
     *
     * **In *waits* it is not available** until the attempts run out, or one would leave a
     * blocked passage by simply saying something else. When they do run out, the continuation
     * the app was holding is played -- nothing is refabricated, and the passage closes
     * **unrepaired**.
     */
    suspend fun close() = writing.withLock {
        if (!_state.value.closes()) return@withLock
        _state.value.held?.let { continuation ->
            Trace.add("passage: the attempts ran out, the held continuation is played")
            _state.update { it.copy(phase = Phase.Speaking, held = null) }
            Playback.play(synthesis.speak(continuation, synthesis.voice()))
            // The thread follows the voice here too: the echo was what was heard, and the
            // continuation is what is heard now, so it is the reply that stands.
            _state.value.open()?.last?.id?.let { attempt ->
                _state.value.utterances.lastOrNull { it.answers == attempt }?.let { reply ->
                    update(reply.id) { it.copy(text = continuation) }
                    write(reply.id)
                }
            }
            _state.update { it.copy(phase = Phase.Idle) }
        }
        // The gates spoke about a passage that is over. What follows opens a fresh one, and
        // its own gates are read when its own call returns.
        _state.update { it.copy(wordsGate = null, soundGate = null) }
        // **The passage's close, where everything else falls**: the patches, the ramp, the
        // lives, the end of the sitting. Its note is the last attempt's and the count of
        // attempts is known, which is what makes this the moment for them.
        val last = _state.value.open()?.last
        val measured = last?.measured.orEmpty().mapNotNull { (path, figure) ->
            (Sheets.of(path) as? Sheet)?.let { Measured(it, figure) }
        }
        fire(
            Moment.PassageClosed,
            world(
                measured = measured,
                scored = Scored(
                    keptWords = last?.judged?.words()?.kept?.size ?: 0,
                    difficulty = null,
                    measured = measured,
                ),
            ),
        )
        // **What is laid only counts for what follows**: a passage already spoken is never
        // rejudged, so an instruction expires between passages and never inside one.
        _state.update {
            // Nothing standing, nothing to age -- and materialising the effective state for a
            // sitting whose rules never fired would freeze a copy of settings for no reason.
            if (it.standing.instructions.isEmpty()) it
            else it.copy(effective = Engine(it.activity.rules).aged(it.standing))
        }
        // A rule of the close may have asked the character to speak. It falls here, between
        // two passages, which is the only place it can: nothing cuts off somebody recording.
        provokeIfAsked()
    }

    /**
     * **The second of the two moments: the sound's gate, read when the analysis has finished.**
     *
     * It only ever runs on a passage the words' gate let through -- a sentence about to be
     * rewritten has no sound analysis at all -- so **the two exhaustions do not fall in the
     * same place**: the rewordings' before the sound analysis has run, the repeats' after.
     */
    private suspend fun soundGate(of: String, analysed: Analysed, stumbling: List<Marked>) {
        val activity = _state.value.activity
        val spoken = _state.value.utterances.firstOrNull { it.id == of } ?: return
        val timed = analysed.timed(stumbling)
        val measured = Sheeting.of(spoken.judged, analysed, timed)
        val scored = Scored(
            keptWords = timed.kept.size, difficulty = null, measured = measured,
        )
        val closing = activity.weights?.let { weights ->
            Gates.sound(
                measured = measured,
                passage = scored,
                settings = _state.value.positions,
                weights = weights,
                sensitivity = ::sensitivityOf,
            )
        }
        // The second of the two goes: the sound's sheets join the judged ones already on the
        // line, rather than replacing them. The pace's side rides with them, being the one
        // thing its symmetric figure cannot say.
        note(of, measured)
        update(of) { it.copy(slower = Fluency.slower(timed)) }
        _state.update { it.copy(soundGate = closing) }
        if (closing != null) {
            // **The notification does not name anything**, and that is not an inconsistency.
            // The sound's gate is wired to elocution and fluency alone, so what it named would
            // be the same word every time, therefore a constant, therefore nothing. What shows
            // where to look is already on screen -- the marks, invariant, each shape saying its
            // own scale -- and naming the worst of them would be an election.
            Trace.add("passage: to say again -- hear the model and say it again")
        }
        // The second run of the moment, on the sheets that exist only now. Which instant a
        // rule falls on inside it follows from the sheet it reads, and is never declared.
        endOfAttempt(
            measured, scored,
            Material(
                correctness = spoken.judged?.words()?.correctness.orEmpty(),
                relevance = spoken.judged?.words()?.relevance.orEmpty(),
                stumbling = spoken.judged?.words()?.stumbling.orEmpty(),
                sounds = analysed.sounds,
                blanks = Fluency.blanks(timed),
            ),
        )
    }

    /**
     * Say an **answer of the AI's** again, and spend one of the replays.
     *
     * **The replays are a lever**, so hearing an answer twice is a setting and not a free
     * gesture: a mode that means to train the ear gives none, and a free conversation gives as
     * many as one likes. The synthesis is not asked again -- the cache is keyed by text and
     * voice, so a replay costs nothing but the speaker.
     *
     * **What is spent is counted here and not stored**, which is a real limit and the one this
     * lever shares with every effective position: a sitting picked up again starts from its
     * declared ones, so a reopened conversation has its replays back
     * (`../../../../../../TODO.md`).
     */
    suspend fun replay(of: String) = writing.withLock {
        if (replaysLeft(of) == 0) return@withLock
        val spoken = _state.value.utterances.firstOrNull { it.id == of } ?: return@withLock
        if (spoken.speaker.isLearner) return@withLock
        _state.update {
            it.copy(
                phase = Phase.Speaking,
                replayed = it.replayed + (of to (it.replayed[of] ?: 0) + 1),
            )
        }
        Playback.play(synthesis.speak(spoken.text, synthesis.voice()), _state.value.speed)
        _state.update { it.copy(phase = Phase.Idle) }
    }

    /** How many replays the utterance [of] has left, or null where the lever bounds none. */
    fun replaysLeft(of: String): Int? {
        val allowed = (_state.value.positions.of(Levers.REPLAY.key) as? Count)?.n ?: return null
        return (allowed - (_state.value.replayed[of] ?: 0)).coerceAtLeast(0)
    }

    /**
     * Say the model of the utterance [of] again.
     *
     * The remedy for a sound fault is to hear the model and say it again, not to be given a
     * written instruction about the tongue (`docs/reference.md`). This is the hearing half.
     *
     * Under the lock, because it is the app speaking and everything waits for it: letting a
     * turn start while a model is being played would put two voices on one speaker.
     */
    suspend fun hear(of: String) = writing.withLock {
        val wav = chosen(of) ?: return@withLock
        _state.update { it.copy(phase = Phase.Speaking) }
        Playback.play(wav, _state.value.speed)
        _state.update { it.copy(phase = Phase.Idle) }
    }

    /**
     * The recording the selector points at, for the utterance [of]: its model, or itself.
     *
     * [of] is the reading being looked at and not the turn it belongs to, which is what keeps
     * the audio and the times on the same take: a turn holds several readings, and reading
     * one recording's clock onto another's audio played a different part of the sentence.
     */
    private fun chosen(of: String): File? = when (_state.value.side) {
        Side.Model -> _state.value.modelOf(of)
        Side.Learner -> _state.value.utterances.firstOrNull { it.id == of }?.said
    }

    fun side(side: Side) { _state.update { it.copy(side = side) } }

    fun speed(speed: Float) { _state.update { it.copy(speed = speed) } }

    /**
     * One stretch of the utterance [of], on whichever side the selector points at.
     *
     * Used by a tap on a word: the word's bounds in each recording are read off the sounds
     * it covers, so nothing new is computed and the two sides stay in step by construction.
     *
     * Outside the lock, and that is the point: it changes nothing, it reads one snapshot, and
     * it lasts a fraction of a second. Putting it behind the writer would make a tap on a
     * word wait out the turn that is running, which is the freeze this gesture exists to
     * avoid.
     */
    suspend fun hear(of: String, fromMs: Int, toMs: Int) {
        val wav = chosen(of) ?: return
        Playback.play(wav, fromMs, toMs, _state.value.speed, Playback.WORD_MARGIN_MS)
    }

    /**
     * Say one sound of the utterance at [at] -- the same remedy, one notch finer.
     *
     * The doc calls the isolated sound a level of listening: the objection to it was about
     * **production**, a sound got right alone still being got wrong in the word, and it does
     * not carry over to listening, which asks nothing of the mouth. It costs nothing to offer
     * -- the model is synthesised anyway and the analysis already says where in it each sound
     * sits.
     *
     * It is the model's own voice and not a specimen from elsewhere, which is what keeps it
     * inside the rule that nothing outside the two recordings is ever consulted. It says
     * where, in the recording being imitated, this mark is about.
     *
     * The phase is left alone. Hearing the whole model is the app speaking and everything
     * waits for it; a tap on a symbol is a fraction of a second, and freezing the screen for
     * it would be a worse lie than the wait it prevents.
     */
    /**
     * Say a sound the learner **added** -- his own recording, at the place he made it.
     *
     * **Only his side exists**, and that is what the line says: there was nothing of the
     * model's there to compare it to. Hearing what one actually said is worth as much here as
     * anywhere else, and more -- an added sound is by definition a thing one did not know one
     * was doing.
     *
     * Nothing plays where the place is absent, which is a turn read before it was carried.
     */
    suspend fun hear(of: String, added: AddedSound) {
        val at = added.saidMs ?: return
        val wav = _state.value.utterances.firstOrNull { it.id == of }?.said ?: return
        Playback.play(wav, at.first, at.last, _state.value.speed)
    }

    suspend fun hear(of: String, sound: AnalysedSound, side: Side) {
        val wav = (if (side == Side.Model) _state.value.modelOf(of)
                   else _state.value.utterances.firstOrNull { it.id == of }?.said) ?: return
        val span = if (side == Side.Model) sound.modelMs else sound.saidMs
        Playback.play(wav, span.first, span.last, _state.value.speed)
    }

    /**
     * Measure [audio] against the utterance at [at] again -- the saying-again half.
     *
     * **It does not go through the conversation.** No recognition, no language model, no
     * reply: this is pipe B alone, on a sentence whose text is already settled. That is what
     * makes it cheap and what makes it honest -- the new take is scored against a reference
     * text known in advance, which is the one thing free conversation cannot offer.
     *
     * It **appends an utterance** pointing at the one it repeats, rather than adding to a
     * list held beside the turn. Nothing of the earlier reading enters this one -- the same
     * fault must produce the same mark at any moment, so a second take is read exactly like a
     * first -- and the earlier reading stays where it was rather than being overwritten.
     */
    suspend fun redo(
        of: String, audio: Take, capture: String? = null, ending: Ending? = null,
    ) = writing.withLock { redoLocked(of, audio, capture, ending) }

    private suspend fun redoLocked(of: String, audio: Take, capture: String?, ending: Ending?) {
        if (_state.value.over) return
        val spoken = _state.value.utterances.firstOrNull { it.id == of } ?: return
        val model = _state.value.modelOf(of) ?: return
        // Its own clock: this is pipe B alone, and timing it from the conversation turn it
        // repeats would add every second of that turn to a chain that never ran here.
        Trace.turn("— redo —")
        Trace.add("redo: same sentence, same model", "text" to spoken.text)
        try {
            // The kept stretches are the ones the model file was rendered on, so they come
            // from the turn being repeated and never from this take: nothing judges a redo --
            // it is pipe B alone -- so there is no second marking of the stumbling to read,
            // and reading one would set the marks against a model that says something else.
            val stumbling = spoken.judged?.stumbling ?: emptyList()
            val kept = Kept.of(spoken.text, stumbling)
            // The whole take: a repeat is measured, and every measure reads the turn as it
            // was said rather than the speech cut out of it.
            val said = audio.whole
            val analysed = analysis.examine(said, model, spoken.text, kept)
            val stamp = Takes.keep(context, said, model, emptyList(), spoken.text, false,
                                   analysed, redo = true, stumbling = stumbling,
                                   turn = turnOf(of), attempt = attemptOf(of))
            val again = Utterance(
                speaker = Speaker.Learner,
                activity = spoken.activity,
                text = spoken.text,
                said = said,
                capture = capture,
                ending = ending,
                marking = analysed.marking,
                sounds = analysed.sounds,
                recorded = analysed.recorded,
                take = stamp,
                engine = (_state.value.analysis as? Readiness.On)?.version,
                repeats = spoken.id,
            )
            _state.update { it.copy(utterances = it.utterances + again) }
            // **The sheets are measured on this take, like on any other.** Without this the
            // reading carried its marks and nothing else: the line that names it lost its
            // pastille and its pace, and the passage's summary went blank the moment one said
            // the sentence better -- the one moment one wants to read it.
            //
            // The sound's gate is **not** read again here, and that is a hole rather than a
            // choice: a passage the gate closed stays closed however well it is said again.
            // It does not bite in a free conversation, which declares no weights and so never
            // closes it, and it is written down as owed.
            val timed = analysed.timed(stumbling)
            note(again.id, Sheeting.of(spoken.judged, analysed, timed))
            update(again.id) { it.copy(slower = Fluency.slower(timed)) }
            write(again.id)
        } catch (failure: ChainFailure) {
            Trace.fail("redo: could not be measured", "why" to failure.message)
        }
    }

    /**
     * Put the utterance [of] in the store, as it now stands.
     *
     * Called again whenever it changes rather than only once, because it changes after it
     * appears: a turn is said before it is read, and its marks arrive a few seconds later.
     * The row is replaced, so writing twice costs a write and never a duplicate.
     *
     * The rank is looked up here rather than passed in. It is the utterance's place in the
     * run, which the row keeps because SQL will not give the insertion order back -- but it
     * is not what addresses anything, so nobody outside this line has to carry it.
     */
    private suspend fun write(of: String) {
        val rank = _state.value.utterances.indexOfFirst { it.id == of }
        if (rank < 0) return
        val spoken = _state.value.utterances[rank]
        runCatching { archive.put(spoken.row(rank)) }.onFailure {
            // Keeping the trace is not the turn. A store that will not write must not cost
            // the learner the conversation -- but it says so rather than passing for kept.
            Trace.fail("archive: not written", "why" to it.message)
        }
    }

    /**
     * **The first moment: while recording**, where the two clocks are all there is to read.
     *
     * Nothing else exists at that instant -- the person is speaking, no sheet has been read --
     * so a rule of this moment reads a clock and nothing more. That is not a restriction laid
     * down, it is a fact about what exists.
     *
     * Driven from the screen, which is what watches the recorder, and once a second rather than
     * on every frame: a rule fires at most once per moment anyway, and a clock trigger names
     * whole seconds.
     */
    suspend fun ticking(elapsedMs: Int, silenceMs: Int) = writing.withLock {
        if (_state.value.activity.rules.isEmpty()) return@withLock
        fire(
            Moment.Recording,
            world(clocks = mapOf(
                Trigger.Clock.Which.TurnLength to elapsedMs,
                Trigger.Clock.Which.Silence to silenceMs,
            )),
        )
    }

    /**
     * Take back what the screen has shown of the last moment.
     *
     * The notices are cleared by whoever showed them and not by a clock here: a notice nobody
     * saw is a change the learner cannot reconstruct, and being able to reconstruct why a note
     * moved is the whole value of the mechanical phrase.
     */
    fun shown() {
        _state.update { it.copy(notices = emptyList()) }
    }

    /**
     * Run the rules of [moment] against [world], and land what they do.
     *
     * **By waves, and the engine is what does that**: every trigger reads the same snapshot,
     * every effect lands together, and what those made true opens the next wave. This only
     * hands it the world and takes the result.
     *
     * Three things come back and each goes where it belongs. The **state** replaces the
     * effective one, which is what every reader of a setting sees from here on. The
     * **notices** wait on the state until the pop-up has shown them -- a change nobody saw is
     * a change the learner cannot reconstruct. And what a **draw or the model chose** goes to
     * the journal and to the store at once: without it the effective state of a sitting no
     * longer recomputes, and the sitting stops comparing to itself three weeks later.
     *
     * A sitting with no rules is the ordinary free conversation, and it returns without
     * building anything.
     */
    private suspend fun fire(moment: Moment, world: World) {
        val activity = _state.value.activity
        if (activity.rules.isEmpty()) return
        // Whether it was already over when this moment began, which is what tells an ending
        // that has just fallen from one this moment inherited.
        val was = _state.value.standing.ended
        val out = Engine(activity.rules).resolve(moment, _state.value.standing, world)
        _state.update { it.copy(effective = out.state, notices = it.notices + out.notices) }
        // **Which questions this moment puts.** They are read against the same world and the
        // same settled state the rules were, and against what this moment's waves moved -- a
        // moment is one instant, and a question due at it is due on what that instant made
        // true, not on what it was before.
        asking += questionsOf(
            moment, world, out.state,
            out.notices.filterIsInstance<Notice.Moved>().map { it.move },
        )
        // The messages are prose for the model, and they are held until a call carries them:
        // the front door opens on the turn that follows, not on the one just read. **The flag
        // rides with them**, since a message that declares it provokes a turn is what decides
        // there is a turn at all.
        messages += out.messages
        if (out.chosen.isNotEmpty()) {
            val at = System.currentTimeMillis()
            val journal = activity.journal + out.chosen.map {
                Chosen(it.rule, world.passage, it.pack, at)
            }
            _state.update { it.copy(activity = it.activity.copy(journal = journal)) }
            runCatching { archive.update(_state.value.activity.row()) }.onFailure {
                Trace.fail("archive: the journal was not written", "why" to it.message)
            }
        }
        // **The ending falls in two moments and not in one instant.** What this moment settled
        // is read here, after its own waves -- a rule that refilled the lives has already had
        // its say -- and the coda runs then, never inside the moment that ended it.
        val ended = out.state.ended
        if (was == null && ended != null && moment != Moment.Closing) ending(ended)
    }

    /**
     * The sitting is over: **the coda runs, then it is filed**.
     *
     * The coda is a moment of its own and not a wave of the one that ended it, and it is a
     * coda and not a reprieve: the state already carries the outcome and nothing takes it
     * back, so a rule firing here can add a last word and no more. Letting it un-finish would
     * make *is it over?* undecided during its own wave.
     *
     * **A sitting the learner walked away from never gets here**, so it has no outcome -- and
     * that is right: without an ending there is no issue, so there is nothing for a later
     * scene to read.
     */
    private suspend fun ending(declared: Outcome) {
        Trace.add("sitting: it is over", "declared" to declared.name)
        fire(Moment.Closing, world())
        // **The closing call exists only where something asked for it.** A sitting whose
        // author wrote no last word ends without one: the character's parting line is never a
        // behaviour of the app, it is a rule somebody wrote.
        provokeIfAsked(sweeping = true)
        val note = sittingNote()
        // **The verdict is the one that actually fell**, never the declaration. *Let the note
        // decide* is a thing an author writes and not an issue anybody can read back, so it is
        // resolved here, against the one bar the project has. Where nothing measured there is
        // no note to resolve it with, and the declaration is written down as it stands: that
        // says *the note was to decide and there was none*, which is true, where a Passed or a
        // Failed picked in its place would be invented.
        val verdict = when (declared) {
            Outcome.Passed, Outcome.Failed -> declared
            Outcome.LetTheNoteDecide ->
                note?.let { if (it.passes) Outcome.Passed else Outcome.Failed } ?: declared
        }
        val at = System.currentTimeMillis()
        _state.update {
            it.copy(
                activity = it.activity.copy(
                    status = Status.Finished,
                    endedAt = at,
                    outcome = app.saylune.activity.Outcome(
                        verdict = verdict.name,
                        // **What settled it, and not who spoke.** A rule that declares an
                        // issue is judged by nobody; a note is the whole chain's, and which
                        // model produced each of its markings is not written down anywhere
                        // (`../../../../../../TODO.md`), so naming one here would be a name
                        // picked rather than read.
                        judge = if (declared == Outcome.LetTheNoteDecide && note != null)
                            JUDGED_BY_THE_NOTE else JUDGED_BY_A_RULE,
                        at = at,
                    ),
                ),
            )
        }
        runCatching { archive.update(_state.value.activity.row()) }.onFailure {
            Trace.fail("archive: the outcome was not written", "why" to it.message)
        }
        Trace.add("sitting: filed", "verdict" to verdict.name, "note" to note?.toString())
    }

    /**
     * The sitting's note, over every passage it holds.
     *
     * **The same flat mean the whole project reads** (`notes/Aggregate.kt`): each passage
     * hands in the figures of its **last** attempt, which is the note of a passage, and a
     * sheet with no figure drops out of the sum rather than counting as a zero.
     *
     * The difficulty is null here, as everywhere the app writes a passage today, so the
     * following drops out with it.
     */
    private fun sittingNote(): app.saylune.notes.Note? {
        val weights = _state.value.activity.weights ?: return null
        return noteOver(
            _state.value.passages().map { passage ->
                val last = passage.last
                Scored(
                    keptWords = last.judged?.words()?.kept?.size ?: 0,
                    difficulty = null,
                    measured = last.measured.mapNotNull { (path, figure) ->
                        (Sheets.of(path) as? Sheet)?.let { Measured(it, figure) }
                    },
                )
            },
            weights,
            ::sensitivityOf,
        )
    }

    /**
     * What a rule has told the model and the next call has not yet carried.
     *
     * Held here rather than on the state because it is **transport**: it belongs to the call
     * being built, it is emptied by it, and nothing on screen reads it. The screen's half of a
     * change is the notice, which is a different thing said to a different reader.
     */
    private var messages = mutableListOf<Effect.Message>()

    /**
     * The questions a moment has put and no call has carried yet.
     *
     * Held beside the messages and emptied by the same gesture, because they are the same kind
     * of thing: transport for the call being built. **They ride on the first call after the
     * moment that put them** -- a question due at the end of an attempt is one whose own call
     * has already gone, so the next one is the earliest there is.
     */
    private var asking = mutableListOf<Question>()

    /**
     * Every question of [moment] that is due right now.
     *
     * A moment is a trigger, exactly as a rule's is, and read by the same reader: a question
     * served at another instant than the one written would be a question put where nobody
     * asked for it. **Asked once per moment**, like a rule fires once, and one already waiting
     * is not asked twice.
     */
    private fun questionsOf(
        moment: Moment, facts: World, state: RuleState, moved: List<app.saylune.levers.Move>,
    ): List<Question> = _state.value.definition.questions
        .filter { question ->
            question !in asking &&
                question.moments.any { it.moment == moment && facts.holds(it, state, moved) }
        }

    /**
     * The instructions standing, what a rule has just said, and the questions being put --
     * **taken off the queue**, the call being what delivers them.
     */
    private fun frontDoor(): FrontDoor {
        val door = FrontDoor(
            _state.value.standing.instructions, messages.toList(), asking.toList(),
        )
        messages = mutableListOf()
        asking = mutableListOf()
        return door
    }

    /**
     * Put back what a call that never went was carrying, in front of anything since.
     *
     * **A question goes back as it was, and that breaks nothing**: a call that gave way wrote
     * no utterance at all -- the learner's turn is written after the reply, and there is no AI
     * turn -- so the fiction is where it was and the passage number has not moved. It rides on
     * the retry of the same take. The one place it slides is the opening, where the character
     * said nothing, the learner speaks first, and the fact is settled alongside that first
     * reply: one of the three ways of opening, not a break.
     *
     * **A message goes back without its flag.** The prose is not wrong and an author wrote it,
     * so it enters by the front door on the next turn like any other message. Its *now* is
     * wrong: that flag means this instant, and the instant has gone. Kept, the next passage's
     * close would see it and have the character speak out of time -- *"ah, there you are"*
     * after two exchanges, which is the one thing here that cannot be caught up.
     */
    private fun putBack(door: FrontDoor) {
        messages = (door.laid.map { it.copy(now = false) } + messages).toMutableList()
        asking = (door.asked + asking).toMutableList()
    }

    /**
     * Let the character speak of its own accord, where a rule of the moment just past asked
     * for it. **Does nothing otherwise, which is every ordinary turn.**
     *
     * A provoked turn is an AI turn with **no learner turn in front of it**: nothing was
     * recorded, so nothing is transcribed, nothing is judged and no passage opens. What the
     * call carries is the history, the scene, and the prose of every message waiting -- the
     * one that asked for the turn and any other the same moment laid down, since they are all
     * things this character has just been told and it would be odd to say one and hold the
     * rest.
     *
     * **It falls only where it is called from**: the opening, a passage's close, and the coda.
     * Never while somebody is recording -- nothing cuts off a person who is still speaking --
     * and never at the end of an attempt, where the character has just answered.
     *
     * [sweeping] is the coda, and the one place a **question** on its own is enough to make a
     * turn: there is no next turn for it to ride on, so a question left open would never be
     * put at all. Everywhere else a question waits for the learner to speak, and only a
     * message that asks for a turn makes one.
     *
     * A link giving way here costs the turn and nothing else. There is no recording to keep
     * and nothing to retry from, so it is said and the sitting carries on: refusing to go on
     * because a scene lost its opening line would be worse than the missing line.
     */
    private suspend fun provokeIfAsked(sweeping: Boolean = false) {
        if (messages.none { it.now } && !(sweeping && asking.isNotEmpty())) return
        val door = frontDoor()
        _state.update { it.copy(phase = Phase.Thinking) }
        try {
            val reply = conversation.reply(
                _state.value.history(), heard = emptyList(),
                scene = Scene(
                    brief = _state.value.activity.brief,
                    cast = _state.value.activity.cast,
                ),
                present = Present(
                    _state.value.positions,
                    ending = null,
                    // The one being spoken into, which is the one nobody has opened yet: a
                    // provoked turn falls between passages, never inside one.
                    passage = _state.value.passages().size + 1,
                    instructions = door.instructions,
                    said = door.prose,
                    provoked = true,
                    asking = door.asked,
                ),
            )
            val answer = Utterance(
                speaker = Speaker.Ai,
                activity = _state.value.activity.id,
                text = reply.spoken,
                // It answers nobody, and that is what tells it from every other AI turn.
                answers = null,
                established = reply.established,
            )
            // Keyed by the utterance itself, there being no passage to key it by. Nothing
            // opens it from the screen; what this is for is clearing the body, which would
            // otherwise show up under the next passage as though it had been sent for it.
            Trace.askedFor(answer.id)
            _state.update {
                it.copy(utterances = it.utterances + answer, phase = Phase.Speaking)
            }
            write(answer.id)
            Playback.play(synthesis.speak(reply.spoken, synthesis.voice()))
        } catch (failure: ChainFailure) {
            Trace.fail("turn: the character had a turn to take and the link gave way",
                       "why" to failure.message)
            putBack(door)
            _state.update { it.copy(failure = failure.message) }
        }
        _state.update { it.copy(phase = Phase.Idle) }
    }

    /**
     * Write on the utterance [of] what [measured] made of each sheet.
     *
     * **Merged and never replaced**, because the figures arrive in two goes at the two moments
     * the doc names: a sheet the first pass could not read comes back absent from the second's
     * list too, and overwriting would lose the ones it did read. A sheet with no figure is
     * simply not put down -- absent is what it is, and never a zero.
     */
    private fun note(of: String, measured: List<Measured>) {
        val figures = measured.mapNotNull { one ->
            one.figure?.let { Sheets.pathOf(one.sheet) to it }
        }.toMap()
        if (figures.isEmpty()) return
        update(of) { it.copy(measured = it.measured + figures) }
    }

    /** Replace the utterance [of] with what [change] makes of it. */
    private fun update(of: String, change: (Utterance) -> Utterance) {
        _state.update { state ->
            state.copy(
                utterances = state.utterances.map { spoken ->
                    if (spoken.id == of) change(spoken) else spoken
                },
            )
        }
    }

    /**
     * The turn's name on disk: the take of the utterance the run starts from.
     *
     * A turn is named by its first take, so every later take can say which turn it repeats.
     * Null while that first take has not been written.
     */
    private fun turnOf(of: String): String? =
        _state.value.utterances.firstOrNull { it.id == of }?.take

    /** Which take of this turn is about to be written, the first being 1. */
    private fun attemptOf(of: String): Int {
        val run = _state.value.utterances
        val root = run.firstOrNull { it.id == of } ?: return 1
        val written = (listOf(root) + run.filter { it.repeats == root.id })
            .count { it.take != null }
        return written + 1
    }

    /** Remember a take that was written, so the next one can name the turn and its rank. */
    private suspend fun keep(of: String, stamp: String?) {
        if (stamp == null) return
        update(of) { it.copy(take = stamp) }
        write(of)
    }

    /**
     * What the call about to go carries from the front door.
     *
     * [laid] keeps the messages whole rather than their prose alone, so a call that never went
     * can hand them back with the flag that says one of them asked for a turn.
     */
    private data class FrontDoor(
        val instructions: List<Instructing>,
        val laid: List<Effect.Message>,
        val asked: List<Question>,
    ) {
        /** What goes into the prompt: the prose, the flag having done its work already. */
        val prose: List<String> get() = laid.map { it.prose }
    }

    companion object {
        /**
         * What settled an outcome, which is what its judge names.
         *
         * **What settled it and not who spoke.** A rule that declares an issue is judged by
         * nobody, and a note is the whole chain's -- which model marked each of its passages
         * is not written down anywhere, so any single name here would be picked rather than
         * read (`../../../../../../TODO.md`). What these two do carry is the one thing that
         * makes two outcomes comparable at all: whether either was judged.
         */
        const val JUDGED_BY_A_RULE = "rule"
        const val JUDGED_BY_THE_NOTE = "note"
    }
}
