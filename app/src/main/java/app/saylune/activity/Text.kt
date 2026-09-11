package app.saylune.activity

/**
 * One of the four doors of the title screen -- **where a scene is offered**.
 *
 * Read by the screens and by the check of a scene file, which holds a file to the rules of the
 * door it is offered behind. The engine never reads it: nothing plays differently by the door.
 */
enum class Door {
    /** Scenes in order, what one leaves behind carried to the next. */
    Story,
    /** An announced goal, and an ending. */
    Challenges,
    /** Replayed without end, a ramp that tightens, the end at zero lives, and a score. */
    Arcade,
    /** **No stakes**: no lever, no instruction, no ending. */
    Free,
}

/**
 * One string, in the languages the file carries.
 *
 * **A file carries its own translations**, in a language-to-text table. A scene mixes two
 * languages by construction: what goes to the leader is in English, the language of the
 * conversation and of the judge's criterion; what the learner reads is in his interface
 * language. One file per language would duplicate the English prose and let it drift from copy
 * to copy. It is a **deliberate departure from the `android` facette**, declared in the
 * manifest: that norm was written for **interface** text, and the prose of a scene is
 * **content**.
 *
 * **English is required, and it is what a missing language falls back on.** The learner reads
 * English rather than nothing: these files ship with the app, and what a gap in them costs a
 * reader is worse than what it costs an author.
 */
@JvmInline
value class Text(val byLanguage: Map<String, String>) {

    init {
        require(BASE in byLanguage) { "a text with no $BASE: $byLanguage" }
    }

    /** What to show someone reading in [language], the English when the file has no such line. */
    fun inLanguage(language: String): String = byLanguage[language] ?: byLanguage.getValue(BASE)

    companion object {
        /** The language every scene writes, being the one the leader is spoken to in. */
        const val BASE = "en"
    }
}
