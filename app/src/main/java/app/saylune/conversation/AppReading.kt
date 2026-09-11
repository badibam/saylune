package app.saylune.conversation

import app.saylune.analysis.AnalysedSound
import app.saylune.judged.Word
import app.saylune.notes.Measured
import app.saylune.notes.Passage as Scored
import app.saylune.notes.Weights
import app.saylune.notes.noteOver
import app.saylune.scene.AppCases
import app.saylune.scene.Value
import app.saylune.sheets.Sheet
import app.saylune.sheets.Sheets

/**
 * What the app writes into its own cases (`docs/design/scene-state.md`, "Les cases").
 *
 * **They translate what the app already reads**, adding nothing: a note for every node of the
 * tree, a figure and a count per notch for every sheet that has elements, and the two gates.
 *
 * **Every case is written at every moment, empty where nothing measured it.** Left at the value
 * the last passage gave it, a pronunciation note would be tested on a passage whose analysis
 * never ran -- and a test on an empty case is false, which is what that passage deserves.
 */
object AppReading {

    /** What one attempt, or the passage that closes on it, made of each case. */
    fun attempt(
        measured: List<Measured>,
        scored: Scored,
        weights: Weights?,
        sensitivity: (Sheet) -> Int,
        material: Material,
        /** Whether the words' gate closed, or null before it has been read. */
        wordsGate: Boolean?,
        /** Whether the sound's gate closed, or null where the analysis did not run. */
        soundGate: Boolean?,
    ): Map<String, Value?> = buildMap {
        val passage = listOf(scored.copy(measured = measured))
        AppCases.treePaths().forEach { path ->
            put("${AppCases.APP_NOTE}$path.note", note(path, passage, weights, sensitivity))
        }
        Sheets.all.forEach { sheet ->
            val path = Sheets.pathOf(sheet)
            put(
                "${AppCases.APP_NOTE}$path.figure",
                measured.firstOrNull { Sheets.pathOf(it.sheet) == path }?.figure
                    ?.let { Value.Num(it.toDouble()) },
            )
            AppCases.notchesOf(sheet).forEach { notch ->
                put(
                    "${AppCases.APP_NOTE}$path.$notch.count",
                    material.count(path, notch)?.let { Value.Num(it.toDouble()) },
                )
            }
        }
        put(AppCases.WORDS_GATE, wordsGate?.let { Value.Flag(it) })
        put(AppCases.SOUND_GATE, soundGate?.let { Value.Flag(it) })
        // The two clocks exist while recording and at no other moment.
        AppCases.CLOCKS.forEach { put(it, null) }
    }

    /**
     * What the whole sitting makes of each note, which is not what this passage makes of it:
     * *if the sitting's correctness falls to C* and *if this passage does* are two things, and
     * a round-up said at the close needs the first.
     */
    fun sitting(
        passages: List<Scored>, weights: Weights?, sensitivity: (Sheet) -> Int,
    ): Map<String, Value?> = AppCases.treePaths().associate { path ->
        "${AppCases.APP_SITTING}$path.note" to note(path, passages, weights, sensitivity)
    }

    /** The note of the node at [path] over [passages], or null where nothing under it measured. */
    private fun note(
        path: String, passages: List<Scored>, weights: Weights?, sensitivity: (Sheet) -> Int,
    ): Value? {
        weights ?: return null
        val under = passages
            .map { passage ->
                passage.copy(
                    measured = passage.measured.filter {
                        Sheets.pathOf(it.sheet).let { at -> at == path || at.startsWith("$path/") }
                    },
                )
            }
            .filter { it.measured.isNotEmpty() }
        if (under.isEmpty()) return null
        return noteOver(under, weights, sensitivity)?.let { Value.Pick(it.letter.name) }
    }
}

/**
 * What the elements of a sheet are made of, on the attempt being read.
 *
 * Handed in rather than derived here, because it is the very material the sheets were computed
 * from: deriving it a second time would be a second arithmetic to hold in step with the first.
 */
data class Material(
    /** The three markings unfolded onto the words, or empty where nothing judged the turn. */
    val correctness: List<Word> = emptyList(),
    val relevance: List<Word> = emptyList(),
    val stumbling: List<Word> = emptyList(),
    /** The sounds, or empty where the analysis did not run. */
    val sounds: List<AnalysedSound> = emptyList(),
) {

    /**
     * How many elements of the sheet at [path] sit on [notch], or null where nothing read it.
     *
     * **Null and never zero**: a turn nobody judged has no `malformed` count, where a turn
     * judged clean has one and it is zero -- and *at least one* has to be false on the first
     * and on the second alike, which only an empty case says.
     */
    fun count(path: String, notch: String): Int? = when (path) {
        "correctness/correctness" -> correctness.ifEmpty { null }?.count { it.notch == notch }
        "relevance/relevance" -> relevance.ifEmpty { null }?.count { it.notch == notch }
        "fluency/stumbling" -> stumbling.ifEmpty { null }?.count { it.notch == notch }
        // The sounds are the elements of both pronunciation sheets, counted on the three
        // notches the screen shows.
        "pronunciation/intelligibility", "pronunciation/proximity" ->
            sounds.ifEmpty { null }?.count { AppCases.soundNotch(it.points) == notch }
        else -> null
    }
}
