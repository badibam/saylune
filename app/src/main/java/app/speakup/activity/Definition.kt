package app.speakup.activity

import app.speakup.conversation.Speaker
import app.speakup.levers.Positions
import app.speakup.notes.Weights
import app.speakup.rules.Instructing
import app.speakup.rules.Rule
import app.speakup.rules.Trigger

/**
 * An activity written in advance, and **it is data and not code**. Replayed as often as one
 * likes (`docs/activity.md`).
 *
 * Nothing a definition holds is logic: declared positions, weights on a declared tree, free
 * text, and rules made of enumerated kinds. What stays in the code is the **catalogue** -- the
 * levers with their positions, the tree of sheets, the kinds of trigger, the kinds of effect.
 * A twenty-scene story written in Kotlin would be content nobody can fix, translate or share,
 * and changing one line would mean recompiling the app.
 *
 * **It lives in a file shipped with the app**, self-contained, read as it stands. Two things
 * fall out of that for nothing: the **version** is the release's, where a row created at
 * runtime has none of its own and would need one stamped on it and a migration written for it;
 * and correcting, translating and sharing all stay possible, which a table loses by half.
 *
 * **The settings are always copied onto the sitting's line**, so a definition that changes
 * never rewrites the past, and a definition is a template applied at creation rather than a
 * dependency kept afterwards.
 */
data class Definition(
    /** What the sitting's [Origin] names, and what the file is called. */
    val id: String,
    /** The release's, inherited for nothing. Also named by [Origin]. */
    val version: String,
    /**
     * The name of this activity, as a tile and a heading carry it.
     *
     * **Every definition names itself**, the plainest one included -- the tile with no theme
     * is called *Free*. So nothing in the app is anonymous, and no model ever names anything:
     * what a sitting is called is decided by the file it came from, before a word is said.
     */
    val title: Text,
    /**
     * The same name cut to what the status line has room for, ten characters at most.
     *
     * **Two readers, two budgets**: a tile takes half the width of a portrait screen, the
     * status line takes what is left once the lives, the score and the note are laid out on
     * its right. Declaring both lets the author choose what survives the cut rather than
     * suffer a truncation (`ui.md`, "La charpente").
     */
    val short: Text,
    /**
     * Which of the four doors this file is offered behind.
     *
     * **Read by the screens, never by the engine nor by a rule.** It says **where a definition
     * is offered** and never **how it is played**: the day a condition, an effect or a
     * passage's gate reads it, the problem `activity.md` closed is open again -- a round name
     * taken for the model, which forced the same sitting to be described twice and made it
     * impossible for a campaign to hold an arcade level, both being values of one field.
     *
     * That invariant's scope is **behaviour**, and this is not behaviour: it is a fact of
     * **delivery**, which grid shows this file. It is called `door` and not `mode` on purpose
     * -- the doc reserved that word and refused it, and taking it back would start the
     * confusion again. A door is a place one comes in by, not a way of playing.
     *
     * **One value and never a list.** A scene offered at two doors would be two files anyway,
     * its weights and its rules differing; a single value reads better.
     */
    val door: Door,
    /** What opens the sitting. Null in a free conversation, whose brief comes from the learner. */
    val brief: Brief? = null,
    /** Who speaks, besides the learner. */
    val cast: List<Character> = emptyList(),
    /**
     * The holes the learner fills before it starts.
     *
     * **The same hole appears on both sides of the [brief] and one answer fills them
     * together** (`activity.md`): written apart, the situation and the staging drift on the
     * first run -- a conference on neurophysics and one on gender theory do not make the same
     * speaker. The answer is substituted at creation and copied onto the line, so a sitting
     * reads on its own afterwards.
     */
    val slots: List<Slot> = emptyList(),
    val settings: Positions = Positions(),
    val weights: Weights,
    val instructions: List<Instructing> = emptyList(),
    val rules: List<Rule> = emptyList(),
    /**
     * The holes of the fiction the model fills, at the moments the author wrote.
     *
     * **Declared by the scene that produces the answer**, never by the one that needs it: only
     * the model that was there can answer. The price is an author's -- writing scene 2, one
     * has to foresee what will matter at scene 5 -- and that is the ordinary work of writing a
     * story.
     */
    val questions: List<Question> = emptyList(),
) {

    /**
     * The one the tile shows, or null where nobody stands out.
     *
     * **The main one is flagged where the character is, not named from outside.** A key held
     * at the top of the file would be a second place to keep right, and a file that renamed a
     * character and forgot it would point at nobody -- silently, since a tile with no name is
     * exactly what a definition with no cast has. The flag cannot dangle.
     */
    val face: Character? get() = cast.firstOrNull { it.main }

    init {
        require(id.isNotBlank()) { "a definition with no id" }
        require(cast.count { it.main } <= 1) { "$id: two characters called the main one" }
        require(slots.map { it.key }.toSet().size == slots.size) { "$id: two slots, one key" }
        require(cast.none { it.key == Speaker.LEARNER }) {
            "${Speaker.LEARNER} is the learner's own key and a character may not take it"
        }
        require(short.byLanguage.values.all { it.length <= SHORT_LIMIT }) {
            "$id: a short name over $SHORT_LIMIT characters"
        }
        require(cast.map { it.key }.toSet().size == cast.size) { "$id: two characters, one key" }
        require(questions.map { it.key }.toSet().size == questions.size) {
            "$id: two questions, one key"
        }
    }

    companion object {
        /** What the status line has room for once the right-hand fields are full. */
        const val SHORT_LIMIT = 10
    }
}

/**
 * One of the four doors of the title screen -- **where a definition is offered**.
 *
 * Read by the screens alone (see [Definition.door]): the engine knows nothing of it.
 */
enum class Door {
    /** A block with **ordered** access, the declared questions carrying one scene to the next. */
    Story,
    /** A block with **free** access, each with its own weights, instructions, rules and issue. */
    Challenges,
    /** Replayed without end, a ramp that tightens, the end at zero lives, and a score. */
    Arcade,
    /** **No stakes**: no lives, no ending rule, no issue, no letters. */
    Free,
}

/**
 * One string, in the languages the file carries.
 *
 * **A file carries its own translations**, in a language-to-text table. A definition mixes two
 * languages by construction: the brief and the staging that go to the model are in English,
 * the language of the conversation and of the judge's criterion; what the learner reads is in
 * his interface language. One file per language would duplicate the English prose and let it
 * drift from copy to copy, and keys into `res/` would break the self-containment. It is a
 * **deliberate departure from the `android` facette**, declared in the manifest: that norm was
 * written for **interface** text, and the prose of a scene is **content**.
 *
 * **English is required, and it is what a missing language falls back on.** The learner reads
 * English rather than nothing, which is the choice made against failing outright: these files
 * ship with the app, and what a gap in them costs a reader is worse than what it costs an
 * author. The cost is named: a definition never translated does not announce itself, and it is
 * the validator's job to say so (`Definitions`).
 */
@JvmInline
value class Text(val byLanguage: Map<String, String>) {

    init {
        require(BASE in byLanguage) { "a text with no $BASE: $byLanguage" }
    }

    /** What to show someone reading in [language], the English when the file has no such line. */
    fun inLanguage(language: String): String = byLanguage[language] ?: byLanguage.getValue(BASE)

    companion object {
        /** The language every definition writes, being the one the model is spoken to in. */
        const val BASE = "en"
    }
}

/**
 * Someone who speaks in a definition.
 *
 * **What this leaves owed is the voice.** A character carries one, and how it gets one is
 * written down (`activity.md`, "Le personnage") and outside this step's perimeter, where
 * only the four generic voices exist. Declaring the field now with nothing reading it would be
 * a field nobody uses (`../../../../../../TODO.md`).
 */
data class Character(
    /** How an utterance names its speaker. */
    val key: String,
    /**
     * The name the line that names the turn carries, truncated by the app in any case.
     *
     * **When a name overflows, the name is what gets cut and never the marks**: the marks
     * carry a measure, the label carries an identity one already knows (`ui.md`).
     */
    val short: Text,
    /**
     * Whether this is the one the theme is met as: the name its tile carries.
     *
     * **One meets a character rather than launching a theme** (`../NOTES.md`), and a tile that
     * named only its situation left the person out of the one place the learner chooses from.
     * At most one character carries it; a definition whose cast is a crowd of equals carries
     * none, and its tile is its title alone.
     */
    val main: Boolean = false,
    /**
     * Which gender the file settles for this character, or null where it lets it be asked.
     *
     * **What says who decides is the presence of the field** (`activity.md`): a definition
     * that declares one imposes it, a definition that declares none has it asked at the launch
     * screen, *no matter* by default -- which is not a third gender but the absence of a
     * constraint, and draws. So there is no *asked* flag to write, and a file that wants
     * either answer to work writes names that do not carry a gender.
     */
    val gender: String? = null,
)

/**
 * A hole a definition leaves for the learner, filled once at the launch screen.
 *
 * [ask] is in the learner's language, because what matters is that he describes what interests
 * him rather than what he can write in English; the answer travels into the English brief as it
 * was typed. Two or three words, and the screen says so rather than the type.
 */
data class Slot(val key: String, val ask: Text) {
    init { require(key.isNotBlank()) { "a slot with no key" } }
}

/**
 * A hole of the fiction the model fills, at a moment the author wrote.
 *
 * *How did his talk go? Did he find the miller's wife? Is the queen safe?* The answer becomes
 * a **fact of the sitting**: it comes back to the model on every turn after, it may be shown
 * to the learner, and a later scene reads it.
 *
 * **This is what makes a tile worth replaying.** A fixed staging does not vary, and a menu of
 * rules varies only between the branches an author enumerated. A free answer settled at the
 * opening has the model write down a fact nobody enumerated and **freezes** it -- it commits
 * once instead of re-improvising its character every turn. Variety stops being drift.
 *
 * **The app serves the question and the model answers on the spot**, which is the inversion
 * the whole thing rests on: not a menu of empty fields it fills when it likes, but a question
 * put at the moment said, whose answer is a **required field of that turn**. The judgement is
 * still taken on trust; the **presence** of an answer is checked like any other field.
 *
 * **None of it is mechanical.** An answer is prose the prompt rereads: no condition reads it,
 * no patch hangs off it. Whatever the app has to carry out goes through the rules, whose menu
 * is closed. Letting the model choose outside the machinery is the one place generosity costs
 * nothing -- what nobody reads back needs neither contract nor guard.
 *
 * Two limits, and they are known. **Nothing validates a free answer**, the same family as
 * `intended`. And **it grows**: at scene 12 the answers of eleven scenes travel. Only the
 * declared questions travel, which bounds it, not always enough.
 */
data class Question(
    /** What names it in the answer and in the run. */
    val key: String,
    /** Put to the model, so in English like everything else it reads. */
    val ask: String,
    val answers: Answers,
    /**
     * When it is put. **Triggers, exactly as a rule's are**, and several are allowed.
     *
     * The author writes *when* and the app serves the question then, which takes every bit of
     * discretion over the calendar away from the model -- and asks it nothing the project does
     * not already ask: a `Judged` trigger, *when he has got the appointment*, is the same
     * judgement as a model left free to fill a field whenever it likes, except that it is
     * **written by the author**, so readable in the file and recorded in the journal.
     */
    val moments: List<Trigger>,
    val rung: Rung = Rung.FromTheTalk,
    /**
     * Whether the learner is told. **One flag, and it governs both surfaces.**
     *
     * Up: he is told when the fact is settled, and the resume screen reminds him of it. Down:
     * it is plumbing for the model, never shown, neither at the time nor after.
     *
     * **One flag because the two sets are necessarily the same**: a resume screen can only
     * show what the learner has **already learnt**, or reopening a conversation would reveal
     * what playing it did not -- and the app cannot know what he worked out by talking.
     *
     * It is a real choice of staging. *The speaker is stung, his room was half empty* with the
     * flag up is a **set** one is given and reminded of; with it down it is something one
     * **finds out by talking**, and the app will never bring it back up.
     */
    val shown: Boolean = false,
) {
    init {
        require(key.isNotBlank()) { "a question with no key" }
        require(ask.isNotBlank()) { "$key: a question with nothing asked" }
        require(moments.isNotEmpty()) { "$key: a question with no moment to be put at" }
    }

    /**
     * The one answer that is not the fiction: **it does not know**.
     *
     * It is in the menu at the first rung and at that one alone, whether the shape is free or
     * closed. **Said rather than left out**, because a missing field is indistinguishable from
     * a model that forgot and from a parse that failed -- the project's rule against hiding a
     * gap behind a silence. At the two other rungs the model cannot run out of an answer, so
     * the case does not arise.
     */
    companion object { const val DONT_KNOW = "I don't know" }
}

/**
 * How far the model may go. **A staircase with a floor, not three modes.**
 *
 * What the conversation has established **always wins**; the rungs only say what happens when
 * it does not settle it. Written as a staircase and not as three labels, or invention will
 * overrule the truth and an answer will contradict what has just happened.
 */
enum class Rung {
    /** Only from what has been said. Where nothing establishes it, it does not know. */
    FromTheTalk,
    /** Failing certainty, conclude from what has been said. */
    MayExtrapolate,
    /** Failing anything to conclude from, decide. */
    MayInvent,
}

/**
 * What shape an answer takes.
 *
 * The distinction earns its keep: **a closed answer is read by code**, so it branches -- *"has
 * he got the key? yes"* opens scene 5 and otherwise 5 bis. That is the testable fact, obtained
 * with no new effect and no new object, what reads it being the block's order, which already
 * derives from what the scenes returned. [Free] text feeds the prompt of the scenes that
 * follow, so that the barman knows what he is talking about.
 */
sealed interface Answers {
    /** Prose, which only the next prompt reads. */
    data object Free : Answers

    /**
     * One of these, which code reads. Yes/no is this with two of them.
     *
     * **The options are an author's text**, so each carries its own table of languages like a
     * title does. The English is required and it is the **key**: it is what goes out in the
     * prompt, what the model answers with, and what the answer is checked against by
     * membership. The screen shows the translation where the file has one.
     */
    data class OneOf(val among: List<Text>) : Answers {
        init {
            require(among.size >= 2) { "a closed answer with fewer than two options" }
        }

        /** What goes to the model and comes back: the English of each option. */
        val keys: List<String> get() = among.map { it.byLanguage.getValue(Text.BASE) }
    }
}

/**
 * One thing a model settled, and when.
 *
 * **A later answer succeeds the one before it, it does not correct it.** *The queen was safe
 * at passage 5 and is not at passage 15* is a story, not a mistake put right -- so nothing is
 * ever overwritten, and a question put at several moments carries a run of answers.
 *
 * [passage] is where the sitting stood when it was settled, which is what makes it dated.
 */
data class Settled(val passage: Int, val text: String)
