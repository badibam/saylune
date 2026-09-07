package app.saylune.ui

import androidx.annotation.StringRes
import app.saylune.R

/**
 * What a screen calls the things the catalogue names in code.
 *
 * **A key to a resource, held at the screen's level** and never a field on `Sheet`: `Sheets` is
 * a pure JVM catalogue that `ConversationPrompt` and `ReplyReader` read outside any UI, and
 * hanging an Android resource on it would make it inseparable from the platform for the
 * convenience of one screen.
 *
 * The criterion is how it reads on screen and never how exact the term is -- which is what gave
 * `Grammar` over `correctness`, and `Form` and `Choice` under it, two short opposites that make
 * visible that the same words are read twice: *is this English*, then *was it the English that
 * was wanted*.
 *
 * Each of these **fails outright** on a name the catalogue does not hold. The catalogue is the
 * list of them, so a name missing from here is a name missing from the app, and answering with
 * a blank would put an unlabelled row on screen.
 */

/** The five aptitude titles, which head the groups of the screen. */
@StringRes
fun nameOfAptitude(name: String): Int = when (name) {
    "pronunciation" -> R.string.aptitude_pronunciation
    "understanding" -> R.string.aptitude_understanding
    "correctness" -> R.string.aptitude_correctness
    "relevance" -> R.string.aptitude_relevance
    "fluency" -> R.string.aptitude_fluency
    else -> error("$name: no screen name. The tree is the list of them.")
}

/**
 * The short name of the sheet at [path].
 *
 * **A key to a resource, held at the screen's level** and never a field on `Sheet`: `Sheets` is
 * a pure JVM catalogue that `ConversationPrompt` and `ReplyReader` read outside any UI, and
 * hanging an Android resource on it would make it inseparable from the platform for the
 * convenience of one screen.
 *
 * The criterion is how it reads on screen and never how exact the term is -- which is what gave
 * `Grammar` over `correctness`, and `Form` and `Choice` under it, two short opposites that make
 * visible that the same words are read twice: *is this English*, then *was it the English that
 * was wanted*.
 */
@StringRes
fun nameOfSheet(path: String): Int = when (path) {
    INTELLIGIBILITY -> R.string.sheet_intelligibility
    PROXIMITY -> R.string.sheet_proximity
    MELODY -> R.string.sheet_melody
    STRESS -> R.string.sheet_lexical_stress
    CORRECTNESS -> R.string.sheet_correctness
    RELEVANCE -> R.string.sheet_relevance
    UPTAKE -> R.string.sheet_uptake
    CONTINUITY -> R.string.sheet_continuity
    LONGEST_SILENCE -> R.string.sheet_longest_silence
    PACE -> R.string.sheet_pace
    STUMBLING -> R.string.sheet_stumbling
    else -> error("$path: no screen name. The tree is the list of them.")
}

/** The following's six notches in words, the one sheet whose measure is a notch and not a figure. */
@StringRes
fun nameOfNotch(notch: String): Int = when (notch) {
    "implied" -> R.string.notch_implied
    "precise" -> R.string.notch_precise
    "on-point" -> R.string.notch_on_point
    "on-topic" -> R.string.notch_on_topic
    "vague" -> R.string.notch_vague
    "off-target" -> R.string.notch_off_target
    else -> error("$notch: no screen name. The column is the list of them.")
}

/** Where each sheet sits in the tree, which is how everything here addresses one. */
internal const val INTELLIGIBILITY = "pronunciation/intelligibility"
internal const val PROXIMITY = "pronunciation/proximity"
internal const val MELODY = "pronunciation/melody"
internal const val STRESS = "pronunciation/lexical-stress"
internal const val CORRECTNESS = "correctness/correctness"
internal const val RELEVANCE = "relevance/relevance"
internal const val UPTAKE = "understanding/uptake"
internal const val CONTINUITY = "fluency/continuity"
internal const val LONGEST_SILENCE = "fluency/longest-silence"
internal const val PACE = "fluency/pace"
internal const val STUMBLING = "fluency/stumbling"
