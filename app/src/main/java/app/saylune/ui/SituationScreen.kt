package app.saylune.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import app.saylune.R
import app.saylune.activity.Caution
import app.saylune.scene.SceneFile
import app.saylune.ui.theme.Saylune
import java.util.Locale

/**
 * What a tile opens: **one screen with two states**.
 *
 * **Filled**, it shows the situation as the learner completed it, with what he brought himself
 * **in colour**, so that one sees at a glance what one added. It costs nothing: the situation
 * cites its holes by name, and what filled them is on the sitting's line.
 *
 * **Empty**, it shows the situation with its holes, one question per hole, and the choice of
 * **gender** where the file leaves it open -- *no matter* by default, which is not a third
 * gender but the absence of a constraint, and draws (`activity.md`).
 *
 * **Two ways out, and one of them asks.** *Carry on* takes the sitting as it stands, or opens
 * one where there is none -- so it is called *begin* until there is something to carry on.
 * *Start over* is the only thing in the app that puts a sitting out of reach, so it asks, on
 * **no** by default, in the register rather than in a dialogue box.
 *
 * **Saying yes puts the old sitting out of reach and starts nothing.** The situation comes back
 * with its holes, and the new sitting is opened by the press on *begin* that follows. What makes
 * the form stay is that the answer is **written down** rather than held here.
 */
@Composable
fun SituationScreen(
    theme: SceneFile,
    /** What the learner filled on the sitting that stands, or null where none was opened. */
    started: Map<String, String>?,
    /**
     * How many passages that sitting holds.
     *
     * **What one carries on is something said, not a row in a table.** A sitting is written the
     * moment the situation is settled, so a scene opened and left before a word was spoken has
     * one -- and *carry on* on a conversation with nothing in it says the wrong thing.
     */
    passages: Int,
    /** Whether the learner asked to be shown what a scene declares. Off by default. */
    showTriggers: Boolean,
    onCarryOn: () -> Unit,
    /** Put the sitting out of reach, and open none. */
    onStartOver: () -> Unit,
    onStart: (answers: Map<String, String>, gender: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Saylune.grid
    val palette = Saylune.palette
    val type = Saylune.type
    val language = Locale.getDefault().language
    val answers = remember(theme.id) { mutableStateMapOf<String, String>() }
    var gender by rememberSaveable(theme.id) { mutableStateOf("") }
    var asking by rememberSaveable(theme.id) { mutableStateOf(false) }
    val situation = theme.situation.inLanguage(language)

    Column(modifier.fillMaxSize().padding(horizontal = grid.cell)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(grid.cell),
        ) {
            if (started != null) {
                // What is not filled reads as the text the citation says to write when the case
                // is empty, which is what the leader is given too.
                Text(
                    filled(situation, started, palette.own.srgb),
                    style = type.text,
                    color = palette.ink.srgb,
                )
            } else {
                situation.takeIf { it.isNotBlank() }?.let {
                    // The holes fill as they are typed, in the same colour they will keep once
                    // the sitting is open: what one is about to walk into is read here, not
                    // after pressing.
                    Text(
                        filling(it, answers, palette.own.srgb, palette.dim.srgb),
                        style = type.text,
                        color = palette.ink.srgb,
                    )
                }
                theme.holes.forEach { hole ->
                    Column(verticalArrangement = Arrangement.spacedBy(grid.cell)) {
                        Text(
                            hole.ask.inLanguage(language),
                            style = type.text,
                            color = palette.ink.srgb,
                        )
                        Field(
                            value = answers[hole.case].orEmpty(),
                            onChange = { answers[hole.case] = it },
                            placeholder = stringResource(R.string.situation_words),
                        )
                    }
                }
                // Asked only where the file leaves it open: one that declares a gender imposes
                // it, and what says who decides is the presence of the field.
                if (theme.face?.gender == null && theme.face != null) {
                    Picks(
                        name = stringResource(R.string.situation_gender),
                        options = listOf(
                            "" to stringResource(R.string.gender_any),
                            MAN to stringResource(R.string.gender_man),
                            WOMAN to stringResource(R.string.gender_woman),
                        ),
                        chosen = gender,
                    ) { gender = it }
                }
            }
            // **Last on the screen, and only where it was asked for.** It is read after the
            // situation and the form, at the moment of pressing rather than before reading what
            // the scene is -- and to whoever left the setting off it does not exist.
            //
            // **It says what the scene is set up to do, never what the sitting will be.** Most
            // of these scenes are built on a hole the learner types themselves, so the author
            // answers for the frame and the rest arrives from the learner and the model. It is
            // shown and never used to hide a tile: matching one person's own words against
            // these keys is a judgement the app cannot make.
            if (showTriggers) {
                Column(
                    Modifier.padding(top = grid.cell),
                    verticalArrangement = Arrangement.spacedBy(grid.cell),
                ) {
                    Text(
                        stringResource(R.string.situation_triggers),
                        style = type.text,
                        color = palette.ink.srgb,
                    )
                    if (theme.cautions.isEmpty()) {
                        Text(
                            stringResource(R.string.situation_triggers_none),
                            style = type.thin,
                            color = palette.dim.srgb,
                        )
                    }
                    // Grouped by family, and the two headings are worth their lines: what a
                    // character does to you and what a scene is about are not the same kind of
                    // warning, and read in one list they would be taken for one.
                    Caution.Kind.entries.forEach { kind ->
                        val declared = theme.cautions.filter { it.kind == kind }
                        if (declared.isEmpty()) return@forEach
                        Text(
                            stringResource(
                                if (kind == Caution.Kind.Manner) R.string.triggers_manner
                                else R.string.triggers_subject
                            ),
                            style = type.thin,
                            color = palette.dim.srgb,
                        )
                        declared.forEach { caution ->
                            Text(
                                stringResource(caution.says),
                                style = type.text,
                                color = palette.ink.srgb,
                            )
                        }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = grid.cell).height(grid.cell * BUTTON_ROWS),
            horizontalArrangement = Arrangement.spacedBy(grid.cell),
        ) {
            if (asking) {
                Way(stringResource(R.string.situation_no), Modifier.weight(1f)) { asking = false }
                Way(stringResource(R.string.situation_yes), Modifier.weight(1f)) {
                    asking = false
                    onStartOver()
                }
            } else {
                Way(
                    stringResource(
                        if (passages == 0) R.string.situation_begin else R.string.situation_carry_on
                    ),
                    Modifier.weight(1f),
                ) {
                    if (started == null) onStart(answers.toMap(), gender.takeIf { it.isNotEmpty() })
                    else onCarryOn()
                }
                Way(
                    stringResource(R.string.situation_over),
                    Modifier.weight(1f),
                    // Nothing to start over before something has started, and an entry that is
                    // off is dimmed and still there rather than absent.
                    enabled = started != null,
                ) { asking = true }
            }
        }

        if (asking) {
            Text(
                stringResource(R.string.situation_over_warns),
                modifier = Modifier.padding(bottom = grid.cell),
                style = type.thin,
                color = palette.dim.srgb,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** One way out: a frame with its word in it, like every button of the register. */
@Composable
private fun Way(
    says: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onPress: () -> Unit,
) {
    val palette = Saylune.palette
    Framed(modifier.fillMaxSize().let { if (enabled) it.clickable(onClick = onPress) else it }) {
        Text(
            says,
            modifier = Modifier.align(Alignment.Center),
            style = Saylune.type.text,
            color = if (enabled) palette.ink.srgb else palette.dim.srgb,
            maxLines = 1,
        )
    }
}

/**
 * The situation as the sitting has it: what the learner brought in colour, and what he left
 * empty as the text the citation says to write instead.
 */
private fun filled(
    template: String, answers: Map<String, String>, ink: androidx.compose.ui.graphics.Color,
): AnnotatedString = buildAnnotatedString {
    walk(template) { key, fallback ->
        val answer = answers[key].orEmpty()
        if (answer.isEmpty()) append(fallback)
        else withStyle(SpanStyle(color = ink)) { append(answer) }
    }
}

/**
 * The situation while it is being filled: the answers in colour, and a hole nobody has answered
 * yet as a blank to be written on.
 *
 * The key is never shown. It is the author's name for the case, in English and in the shape a
 * programmer writes -- what the learner is being asked is on the line below, in his language.
 */
private fun filling(
    template: String,
    answers: Map<String, String>,
    ink: androidx.compose.ui.graphics.Color,
    dim: androidx.compose.ui.graphics.Color,
): AnnotatedString = buildAnnotatedString {
    walk(template) { key, _ ->
        val answer = answers[key].orEmpty()
        if (answer.isEmpty()) withStyle(SpanStyle(color = dim)) { append(BLANK) }
        else withStyle(SpanStyle(color = ink)) { append(answer) }
    }
}

/**
 * [template] appended piece by piece, [hole] being handed the case each citation names and what
 * it says to write when that case is empty.
 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.walk(
    template: String, hole: (key: String, fallback: String) -> Unit,
) {
    var at = 0
    HOLE.findAll(template).forEach { found ->
        append(template.substring(at, found.range.first))
        hole(found.groupValues[1].trim(), found.groupValues[2].trim())
        at = found.range.last + 1
    }
    append(template.substring(at))
}

/** What an unanswered hole looks like: something to write on. */
private const val BLANK = "_____"

/**
 * What a citation looks like in a scene's prose: a case in braces, and what to write when it is
 * empty after a bar.
 */
// Both braces escaped: ICU's engine, which Android uses, refuses a bare closing one.
private val HOLE = Regex("\\{([^}|]+)(?:\\|([^}]*))?\\}")

/** What the store holds for a gender the learner chose. */
const val MAN = "man"
const val WOMAN = "woman"

/** A button: one line inside, and the border each side. */
private const val BUTTON_ROWS = 3
