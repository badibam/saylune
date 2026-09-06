package app.speakup.activity

import app.speakup.conversation.Speaker
import app.speakup.levers.Positions
import app.speakup.notes.Weights
import app.speakup.rules.Instructing
import app.speakup.rules.Pack
import app.speakup.rules.Rule

/**
 * An activity written in advance, and **it is data and not code**. Replayed as often as one
 * likes (`docs/design/activity-model.md`).
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
     * suffer a truncation (`pixel-ui.md`, "La charpente").
     */
    val short: Text,
    /** What opens the sitting. Null in a free conversation, whose brief comes from the learner. */
    val brief: Brief? = null,
    /** Who speaks, besides the learner. */
    val cast: List<Character> = emptyList(),
    val settings: Positions = Positions(),
    val weights: Weights,
    val instructions: List<Instructing> = emptyList(),
    val rules: List<Rule> = emptyList(),
    /**
     * What the model answers at the end of the sitting, and what the next scenes receive.
     *
     * **Declared by the scene that produces the answer**, never by the one that needs it: only
     * the model that was there can answer. The price is an author's -- writing scene 2, one
     * has to foresee what will matter at scene 5 -- and that is the ordinary work of writing a
     * story.
     */
    val questions: List<Question> = emptyList(),
    /**
     * The pack of effects that opens the sitting, if it opens with anything.
     *
     * **A pack and not a text**: opening a scene often wants two things at once -- a message
     * to the model, *"you bump into Simon and apologise profusely"*, and a line shown to the
     * learner, *"a passer-by bumps into you"*. A text field carries only one. The three ways
     * of opening -- the learner speaks first, the character says a written line, the model
     * improvises on an instruction -- are three ways of filling this pack, not three fields.
     *
     * The start of a sitting is **not a trigger**: a trigger exists to test something at a
     * moment that comes round again, and the start happens once and unconditionally.
     */
    val opening: Pack? = null,
) {

    init {
        require(id.isNotBlank()) { "a definition with no id" }
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
 * written down (`activity-model.md`, "Le personnage") and outside this step's perimeter, where
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
     * carry a measure, the label carries an identity one already knows (`pixel-ui.md`).
     */
    val short: Text,
)

/**
 * Something the model answers at the end of the sitting.
 *
 * **The free text of an outcome is these answers and nothing else.** A challenge wanting a
 * closing comment declares the question; one that declares none has no text.
 *
 * Two limits, and they are known. **The model writes its own memory**, and nothing checks it --
 * the same family as `intended`. And **it grows**: at scene 12 the answers of eleven scenes
 * travel. Only the declared questions travel, which bounds it, not always enough.
 */
data class Question(
    val key: String,
    /** Asked of the model, so in English like everything else it reads. */
    val ask: String,
    val answers: Answers,
) {
    init { require(key.isNotBlank()) { "a question with no key" } }
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

    /** One of these, which code reads. Yes/no is this with two of them. */
    data class OneOf(val among: List<String>) : Answers {
        init { require(among.size >= 2) { "a closed answer with fewer than two options" } }
    }
}
