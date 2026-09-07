package app.speakup.ui

import android.graphics.Paint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import app.speakup.R
import app.speakup.levers.At
import app.speakup.levers.Levers
import app.speakup.levers.Positions
import app.speakup.ui.theme.Speakup

/**
 * What the AI's turn shows, which is the position of `ai-turn.display` and never a preference.
 *
 * Four positions, from the most helped to the barest: the text, the **scrambled** text, only
 * who speaks, nothing. The scrambled one is the **default** (`ui.md`): the ear is the
 * app's main channel and a legible text would preempt the listening, so what is left standing
 * is the support of the phrase without its content. It falls between the plain text and *only
 * who speaks* -- one aid fewer than the text and one more than nothing.
 *
 * **A preference would hold everywhere and no challenge could take it back**, which is exactly
 * why this is a lever: in a free conversation the learner moves it himself and it stays where
 * he left it, while an activity that wants the plain text, or wants none of it, sets it like
 * any other position.
 */
enum class Display(val position: String) {
    Text("text"), Scrambled("scrambled"), Speaker("speaker"), Nothing("nothing");

    companion object {
        /** Where this sitting has it. The catalogue's own default answers for what is unset. */
        fun of(positions: Positions): Display {
            val name = (positions.of(Levers.AI_DISPLAY.key) as? At)?.name
            return entries.firstOrNull { it.position == name } ?: Scrambled
        }
    }
}

/**
 * A turn of the AI's, shown as [display] asks.
 *
 * The four positions are drawn from the line that names the turn as much as from the text
 * under it: *nothing* is neither, *only who speaks* is the line alone -- which takes its sense
 * with several characters, where one knows it is Vera speaking without reading what she says --
 * and the two above it are the line with the text under it, scrambled or plain.
 *
 * The line carries **no pastille and no pace**: those two measure the learner's turn, and an
 * empty slot on this line would say *not measured*, which is exactly right -- nothing measures
 * what the AI said.
 */
@Composable
fun Heard(
    text: String,
    /** The short name of whoever said it, which a definition declares per character. */
    speaker: String,
    display: Display,
    channels: Channels,
    modifier: Modifier = Modifier,
) {
    if (display == Display.Nothing) return
    val scramble = rememberScramble()
    Column(modifier) {
        TurnLabel(name = speaker, following = null, pace = null, channels = channels)
        if (display == Display.Speaker) return@Column
        Text(
            if (display == Display.Scrambled) scramble(text) else text,
            style = Speakup.type.text,
            color = Speakup.palette.ink.srgb,
        )
    }
}

/**
 * The scrambling: **a font and not an effect**.
 *
 * Each letter has its scrambled twin at its own codepoint **plus `0xE100`**, so the app's table
 * is an addition and it never has to decide what a letter is -- a character scrambles if the
 * shifted codepoint is in the font (`font/README.md`). What survives is the support: the height
 * and width of each letter, so ascenders stay tall and descenders low, the length of the words,
 * the punctuation and the line breaks. One reads the panel as text without reading a letter of
 * it.
 *
 * **It is fixed**: the twin is drawn from a seed that is the letter itself, so the same letter
 * is covered the same way everywhere and forever, like a mark rather than like static.
 *
 * **A letter with no twin falls back on the generic one** at `U+E100` -- the shift itself, which
 * is the scramble of no character at all. It never serves while the font's list is up to date;
 * it is the net for the letter someone adds one day and forgets to scramble. **Punctuation has
 * no fallback** and stays clear on purpose: it, with the line breaks, is what makes a scrambled
 * panel read as a phrase.
 */
@Composable
fun rememberScramble(): (String) -> String {
    val context = LocalContext.current
    // The font is asked itself rather than a list being kept here: a second list is a second
    // source, and this one would go out of step with `font/scramble.py` the first time a
    // character was added there.
    val paint = remember(context) {
        Paint().apply {
            typeface = ResourcesCompat.getFont(context, R.font.speakup_tile_regular)
        }
    }
    val known = remember(paint) { mutableMapOf<Char, Char?>() }
    return remember(paint) {
        { text ->
            buildString {
                text.forEach { character ->
                    append(known.getOrPut(character) { twin(paint, character) } ?: character)
                }
            }
        }
    }
}

/**
 * The scrambled twin of [character], or null where it keeps its own shape.
 *
 * Null is punctuation, whitespace and everything else that is not a letter: those are the
 * support, and covering them would take the shape of the phrase away with its content.
 */
private fun twin(paint: Paint, character: Char): Char? {
    if (!character.isLetterOrDigit()) return null
    val shifted = (character.code + SHIFT).toChar()
    return if (paint.hasGlyph(shifted.toString())) shifted else GENERIC
}

/** Where the scrambled twins live: the letter's own codepoint, shifted. */
private const val SHIFT = 0xE100

/** The twin of no character at all, which is what an unscrambled letter falls back on. */
private const val GENERIC = '\uE100'
