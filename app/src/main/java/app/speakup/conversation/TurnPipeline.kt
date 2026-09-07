package app.speakup.conversation

import android.content.Context
import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Present
import app.speakup.chain.Recognition
import app.speakup.chain.Reply
import app.speakup.chain.Scene
import app.speakup.chain.Word
import app.speakup.activity.Activity
import app.speakup.activity.Chosen
import app.speakup.activity.Definition
import app.speakup.activity.Definitions
import app.speakup.capture.Ending
import app.speakup.capture.Playback
import app.speakup.capture.Take
import app.speakup.analysis.Analysed
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.timed
import app.speakup.analysis.Analysis
import app.speakup.analysis.Readiness
import app.speakup.debug.Trace
import app.speakup.judged.Judgement
import app.speakup.judged.Kept
import app.speakup.judged.Marked
import app.speakup.fluency.Fluency
import app.speakup.notes.Measured
import app.speakup.notes.Passage as Scored
import app.speakup.rules.Engine
import app.speakup.rules.Moment
import app.speakup.rules.Notice
import app.speakup.rules.State as RuleState
import app.speakup.rules.Instructing
import app.speakup.rules.Trigger
import app.speakup.notes.Sheeting
import app.speakup.sheets.Sheet
import app.speakup.sheets.Sheets
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Levers
import app.speakup.levers.Positions
import app.speakup.levers.severityOn
import app.speakup.levers.Stepped
import app.speakup.marking.AddedSound
import app.speakup.marking.TurnMarking
import app.speakup.providers.ChosenSynthesis
import app.speakup.providers.words
import app.speakup.store.ArchiveDao
import app.speakup.store.Recordings
import app.speakup.store.activity
import app.speakup.store.row
import app.speakup.store.utterance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        const val SPEAKUP = "speakup"

        val Learner = Speaker(LEARNER)
        val Ai = Speaker(SPEAKUP)
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
    val sounds: List<AnalysedSound> = emptyList(),
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
     * Every reading of the utterance [of], oldest first: its own, then each one that says it
     * again.
     *
     * Each reading is addressed by its own identity, which every listening gesture then
     * carries: a reading whose audio came from one take and whose times came from another
     * plays a different part of the sentence.
     *
     * Derived and never stored. It is a walk of the run, and a walk of the run cannot fall
     * out of step with the run -- which is exactly what a list kept beside it used to do.
     * Only the readings that carry a marking are here: one that carries none was never read,
     * so there is nothing of it to look at.
     */
    fun readings(of: String): List<Utterance> {
        val root = utterances.firstOrNull { it.id == of } ?: return emptyList()
        return utterances.filter {
            (it.id == root.id || it.repeats == root.id) && it.marking != null
        }
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
    fun closes(): Boolean = when (standing()) {
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
    fun history(): List<Exchange> = passages().flatMap { passage ->
        val said = passage.last
        // The last reply of the **passage**, and not the one answering the last attempt: a
        // repeat is never answered at all -- it is pipe B alone, on a text already settled --
        // so looking it up by the last attempt would lose the reply the moment the learner
        // said the sentence again. A rewording, which does make a fresh call, is the case
        // where the two coincide.
        val answered = utterances.lastOrNull { reply ->
            passage.attempts.any { reply.answers == it.id }
        }
        listOfNotNull(
            Exchange(fromLearner = true, text = said.text),
            answered?.let { Exchange(fromLearner = false, text = it.text) },
        )
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
            archive.latest()?.let { openLocked(it.activity().id) } ?: beginLocked()
            // Once, here, and here only: at startup nothing is in flight and nothing has
            // been recorded, so a file the store does not name is one nothing will name.
            runCatching { Recordings.sweepOrphans(context, archive.recordings().toSet()) }
                .onFailure { Trace.fail("recordings: not swept", "why" to it.message) }
        }
        if (_state.value.analysis == null) {
            val readiness = analysis.readiness()
            _state.update { it.copy(analysis = readiness) }
        }
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

    private suspend fun openLocked(id: String) {
        val row = archive.activity(id) ?: return
        val activity = row.activity()
        opened = true
        val run = archive.utterances(activity.id).map { it.utterance() }
        _state.update {
            it.copy(
                activity = activity,
                // A sitting made before the free conversation was itself a definition has no
                // origin, and there was no other kind of sitting then: reading `free` for it
                // is a fact about that release, not a stand-in for something missing.
                definition = activity.origin
                    ?.let { from -> Definitions.of(context, from.definition) } ?: free,
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
    }

    /**
     * Start a fresh conversation.
     *
     * The one being left is not marked finished. Nothing prevents starting another, and
     * calling the old one finished would be the app deciding it is over on no evidence -- it
     * stays there to be carried on with.
     */
    suspend fun begin() = writing.withLock { beginLocked() }

    private suspend fun beginLocked() {
        val fresh = Activity.from(free)
        opened = true
        archive.put(fresh.row())
        _state.update {
            it.copy(
                activity = fresh,
                definition = free,
                utterances = emptyList(),
                effective = null,
                notices = emptyList(),
                pending = null,
                failure = null,
            )
        }
        Trace.add("conversation: begun", "activity" to fresh.id)
    }

    /** Every conversation, most recent first, for the list to draw. */
    fun conversations() = archive.conversations()

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
        try {
            // **No silence goes to the network**: the recognition is given the speech
            // alone, which is what it has any use for and what it charges for.
            val heard = recognition.transcribe(take.spoken)
            if (heard.isEmpty()) {
                Trace.add("turn: nothing was said, dropped")
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
                present = frontDoor().let { (standing, laid) ->
                    Present(
                        _state.value.positions, closedBy,
                        // A rewording is another attempt at the passage that is already open;
                        // a turn that rewords nothing opens the next one. Derived from the
                        // run, like the passages themselves, so it cannot fall out of step
                        // with it.
                        passage = _state.value.passages().size + if (rewords == null) 1 else 0,
                        instructions = standing,
                        said = laid,
                    )
                },
            )

            // The two utterances are held by identity from here on. Their place in the run is
            // where they happen to sit, and it is not what addresses them: everything that
            // follows -- the take, the marks, the analysis -- names them by their id.
            val said = Utterance(
                speaker = Speaker.Learner,
                activity = _state.value.activity.id,
                text = reply.judged.intended,
                said = turn,
                capture = position,
                ending = closedBy,
                judged = reply.judged,
                // A rewording is an attempt at the passage it points back at; a turn that
                // rewords nothing opens one of its own.
                repeats = rewords,
                attempt = rewords?.let { Attempt.Rewording },
            )
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

            val marked = reply.judged.words()
            val groundless = marked.correctness.any { it.notch == Gates.UNSAYABLE }
            // **The words' gate is read here, and it is the first of the two moments** -- and
            // before anything is played, because what is played depends on it.
            val closing = wordsGate(said, reply, groundless)

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
                    "said" to reply.judged.intended,
                )
                // **And it is read all the same, on the one channel it has.** The sound
                // analysis does not run, but what the judge marked is exactly what has to be
                // seen -- it is the reason the passage is being sent back, and a turn shown
                // as plain text leaves the learner told to say it differently with nothing on
                // screen saying what was wrong. So a marking with the text and no sound in
                // it, which is the truth about this turn; and it is what makes the passage's
                // row of commands appear, a reading being what the run is walked for.
                update(said.id) { it.copy(marking = TurnMarking.wordsOnly(reply.judged.intended)) }
                write(said.id)
                // Kept even so, and especially so: a turn like this is a real learner fault
                // the recognition could not have guessed, which is what the fidelity bench
                // is short of.
                keep(said.id, Takes.keep(context, turn, null, heard, reply.judged.intended, true,
                                         null, stumbling = reply.judged.stumbling,
                                         turn = turnOf(said.id),
                                         attempt = attemptOf(said.id)))
            } else {
                examine(
                    of = said.id, said = turn, heard = heard,
                    text = reply.judged.intended,
                    kept = Kept.of(reply.judged.intended, reply.judged.stumbling),
                    stumbling = reply.judged.stumbling,
                )
            }
            _state.update { it.copy(phase = Phase.Idle) }
        } catch (failure: ChainFailure) {
            Trace.fail("turn: a link gave way, the recording is kept", "why" to failure.message)
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
    private suspend fun wordsGate(said: Utterance, reply: Reply, groundless: Boolean): Closing? {
        val activity = _state.value.activity
        val marked = reply.judged.words()
        val measured = Sheeting.of(reply.judged, analysed = null, timed = null)
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
        val readiness = _state.value.analysis
            ?: analysis.readiness().also { ready -> _state.update { it.copy(analysis = ready) } }
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
                        engine = readiness.version)
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
        val out = Engine(activity.rules).resolve(moment, _state.value.standing, world)
        _state.update { it.copy(effective = out.state, notices = it.notices + out.notices) }
        // The messages are prose for the model, and they are held until the next call carries
        // them: the front door opens on the turn that follows, not on the one just read.
        // **The prose only, and the flag not yet.** A message that declares it provokes a
        // turn at once wants an AI turn with no learner turn in front of it, which is the same
        // thing a definition's opening wants -- and neither is written (`../../TODO.md`). What
        // is carried is what every message says either way.
        messages += out.messages.map { it.prose }
        if (out.chosen.isEmpty()) return
        val at = System.currentTimeMillis()
        val journal = activity.journal + out.chosen.map {
            Chosen(it.rule, world.passage, it.pack, at)
        }
        _state.update { it.copy(activity = it.activity.copy(journal = journal)) }
        runCatching { archive.update(_state.value.activity.row()) }.onFailure {
            Trace.fail("archive: the journal was not written", "why" to it.message)
        }
    }

    /**
     * What a rule has told the model and the next call has not yet carried.
     *
     * Held here rather than on the state because it is **transport**: it belongs to the call
     * being built, it is emptied by it, and nothing on screen reads it. The screen's half of a
     * change is the notice, which is a different thing said to a different reader.
     */
    private var messages = mutableListOf<String>()

    /** The instructions standing, and what a rule has just said, for the call about to go. */
    private fun frontDoor(): Pair<List<Instructing>, List<String>> {
        val laid = messages.toList()
        messages = mutableListOf()
        return _state.value.standing.instructions to laid
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
}
