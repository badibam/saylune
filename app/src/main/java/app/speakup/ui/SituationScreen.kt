package app.speakup.ui

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
import app.speakup.R
import app.speakup.activity.Brief
import app.speakup.activity.Definition
import app.speakup.ui.theme.Speakup
import java.util.Locale

/**
 * What a tile opens: **one screen with two states**.
 *
 * **Filled**, it shows the situation as it was completed, with what the learner brought
 * himself **in colour**, so that one sees at a glance what one added. It costs nothing: the
 * template is in the file and the finished text is on the line, so where the holes were is
 * read back by laying one against the other.
 *
 * **Empty**, it shows the situation with its holes, one question per hole, and the choice of
 * **gender** where the file leaves it open -- *no matter* by default, which is not a third
 * gender but the absence of a constraint, and draws (`activity.md`).
 *
 * **Two ways out, and one of them asks.** *Carry on* takes the sitting as it stands, or opens
 * one where there is none -- so it is called *begin* until there is something to carry on,
 * which is the one place this departs from `ui.md`: two buttons that never change their words
 * would have one of them lying on a theme nobody has opened. *Start over* is the only thing in
 * the app that puts a sitting out of reach -- the old one stays in the base, and with no
 * history screen out of reach is lost to use -- so it asks, on **no** by default, in the
 * register rather than in a dialogue box: the two buttons become the question, and the answer
 * is where the hand already is.
 *
 * **Saying yes puts the old sitting out of reach and starts nothing.** The situation comes
 * back with its holes, and the new sitting is opened by the press on *begin* that follows,
 * like on a theme nobody had touched -- so the questions are asked before a word is spoken,
 * and a scene whose character speaks first does not start talking over an empty situation.
 * What makes the form stay is that the answer is **written down** rather than held here: this
 * screen has no memory of its own between two visits, and a flag held on it let the old
 * sitting stand again on the way back in.
 */
@Composable
fun SituationScreen(
    theme: Definition,
    /** The situation as it stands on the sitting, or null where none was ever opened. */
    started: Brief?,
    /**
     * How many passages that sitting holds.
     *
     * **What one carries on is something said, not a row in a table.** A sitting is written the
     * moment the situation is settled, so a theme opened and left before a word was spoken has
     * one -- and *carry on* on a conversation with nothing in it says the wrong thing. The word
     * is *begin* until a passage exists; what pressing does is unchanged, and it opens that
     * empty sitting rather than piling a second one on top of it.
     */
    passages: Int,
    onCarryOn: () -> Unit,
    /**
     * Put the sitting out of reach, and open none.
     *
     * The screen empties because the sitting it was standing on stops being offered, not
     * because anything here remembers the answer.
     */
    onStartOver: () -> Unit,
    onStart: (answers: Map<String, String>, gender: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    val language = Locale.getDefault().language
    val answers = remember(theme.id) { mutableStateMapOf<String, String>() }
    var gender by rememberSaveable(theme.id) { mutableStateOf("") }
    var asking by rememberSaveable(theme.id) { mutableStateOf(false) }

    Column(modifier.fillMaxSize().padding(horizontal = grid.cell)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(grid.cell),
        ) {
            if (started != null) {
                Text(
                    said(theme.brief?.situation.orEmpty(), started.situation, palette.own.srgb),
                    style = type.text,
                    color = palette.ink.srgb,
                )
            } else {
                theme.brief?.situation?.takeIf { it.isNotBlank() }?.let { situation ->
                    // The holes fill as they are typed, in the same colour they will keep
                    // once the sitting is open: what one is about to walk into is read here,
                    // not after pressing.
                    Text(
                        filling(situation, answers, palette.own.srgb, palette.dim.srgb),
                        style = type.text,
                        color = palette.ink.srgb,
                    )
                }
                theme.slots.forEach { slot ->
                    Column(verticalArrangement = Arrangement.spacedBy(grid.cell)) {
                        Text(
                            slot.ask.inLanguage(language),
                            style = type.text,
                            color = palette.ink.srgb,
                        )
                        Field(
                            value = answers[slot.key].orEmpty(),
                            onChange = { answers[slot.key] = it },
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
    val palette = Speakup.palette
    Framed(modifier.fillMaxSize().let { if (enabled) it.clickable(onClick = onPress) else it }) {
        Text(
            says,
            modifier = Modifier.align(Alignment.Center),
            style = Speakup.type.text,
            color = if (enabled) palette.ink.srgb else palette.dim.srgb,
            maxLines = 1,
        )
    }
}

/**
 * The finished situation, with what the learner brought in colour.
 *
 * The template is cut on its holes, and what stands between two of its literal pieces in the
 * finished text is what filled a hole. A piece that cannot be found leaves the rest plain --
 * the sitting was opened from another release of the file, which is exactly what the version
 * on its origin exists to make visible, and a colour that guessed would be worse than none.
 */
private fun said(template: String, filled: String, ink: androidx.compose.ui.graphics.Color):
    AnnotatedString = buildAnnotatedString {
    val pieces = template.split(HOLE).filter { it.isNotEmpty() }
    var rest = filled
    pieces.forEach { piece ->
        val at = rest.indexOf(piece)
        if (at < 0) {
            append(rest)
            rest = ""
            return@forEach
        }
        if (at > 0) withStyle(SpanStyle(color = ink)) { append(rest.take(at)) }
        append(piece)
        rest = rest.drop(at + piece.length)
    }
    if (rest.isNotEmpty()) withStyle(SpanStyle(color = ink)) { append(rest) }
}

/**
 * The situation as it stands while it is being filled: the answers in colour, and a hole
 * nobody has answered yet as a blank to be written on.
 *
 * The key is never shown. It is the author's name for the hole, in English and in the shape a
 * programmer writes -- what the learner is being asked is on the line below, in his language.
 */
private fun filling(
    template: String,
    answers: Map<String, String>,
    ink: androidx.compose.ui.graphics.Color,
    dim: androidx.compose.ui.graphics.Color,
): AnnotatedString = buildAnnotatedString {
    var at = 0
    HOLE.findAll(template).forEach { hole ->
        append(template.substring(at, hole.range.first))
        val answer = answers[hole.value.trim('{', '}')].orEmpty()
        if (answer.isEmpty()) withStyle(SpanStyle(color = dim)) { append(BLANK) }
        else withStyle(SpanStyle(color = ink)) { append(answer) }
        at = hole.range.last + 1
    }
    append(template.substring(at))
}

/** What an unanswered hole looks like: something to write on. */
private const val BLANK = "_____"

/** What a hole looks like in a definition's prose: a key in braces. */
// Both braces escaped: ICU's engine, which Android uses, refuses a bare closing one.
private val HOLE = Regex("\\{[^}]+\\}")

/** What the store holds for a gender the learner chose. */
const val MAN = "man"
const val WOMAN = "woman"

/** A button: one line inside, and the border each side. */
private const val BUTTON_ROWS = 3
