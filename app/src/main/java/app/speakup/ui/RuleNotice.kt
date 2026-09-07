package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.speakup.R
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Direction
import app.speakup.levers.Move
import app.speakup.levers.Numeric
import app.speakup.levers.Position
import app.speakup.levers.Stepped
import app.speakup.rules.Notice
import app.speakup.levers.Levers
import app.speakup.sheets.Sheets
import app.speakup.ui.theme.Speakup

/**
 * What a rule has just changed, shown as a **pop-up** and not as a state.
 *
 * The three notifications of the app are not one kind (`ui.md`). Two of them are **states**
 * and live in the status line for as long as they are true -- the words' gate, which names every
 * aptitude in cause, and the sound's gate, which names nothing, being wired to elocution and
 * fluency alone so that what it named would be a constant. This one is a **receipt**: it says
 * what has just happened, it is read once, and it goes.
 *
 * **The mechanical phrase is obligatory and the narrative one optional.** Whoever reads only
 * *"the barman seems in a hurry"* does not know his turn now goes on its own after five seconds,
 * and will take it for a bug the first time it happens -- and the whole value of the mechanical
 * phrase is being able to reconstruct why a note moved. A free conversation shows the mechanical
 * one alone, having no fiction; a definition that writes no narrative one is dry, not broken.
 *
 * **The mechanical one also says which way it went.** Reading *two attempts allowed* does not
 * say whether that has just gone up or down, and it is not the same news: the positions of a
 * lever are ordered and every lever knows which end is the hard one, so a move carries its
 * direction.
 *
 * **It is dismissed by the finger and not by a clock**: a notice nobody has seen is a change the
 * learner cannot reconstruct.
 */
@Composable
fun RuleNotice(notices: List<Notice>, onSeen: () -> Unit, modifier: Modifier = Modifier) {
    if (notices.isEmpty()) return
    val grid = Speakup.grid
    val palette = Speakup.palette
    Box(modifier.fillMaxWidth().padding(grid.cell), contentAlignment = Alignment.Center) {
        Framed(Modifier.fillMaxWidth().clickable(onClick = onSeen)) {
            Column(Modifier.padding(vertical = grid.cell)) {
                // The scene's lines that set the ground come first, the mechanical ones and the
                // receipts after: *"a passer-by knocks into you"* stages the turn that follows,
                // so read after the reply it would drop out of nowhere, where *"you have one
                // life left"* states what has just happened.
                said(notices).forEach { line ->
                    Text(
                        line,
                        style = Speakup.type.text,
                        color = palette.ink.srgb,
                    )
                }
            }
        }
    }
}

/** Every line of the notice, staging first, in the order the doc puts them. */
@Composable
private fun said(notices: List<Notice>): List<String> {
    val before = notices.filterIsInstance<Notice.Staged>().filter { it.staging.before }
    val after = notices.filterIsInstance<Notice.Staged>().filter { !it.staging.before }
    val moved = notices.filterIsInstance<Notice.Moved>().map { phraseOf(it.move) }
    return before.map { it.staging.text } + moved + after.map { it.staging.text }
}

/**
 * What a move reads as: its lever's phrase for the position it arrived at, and which way it went.
 *
 * **One lever family's phrase does not stand on its own**, and it is the sensitivities: there are
 * eleven of them, so the phrase says the level -- *judged severely* -- and the screen supplies
 * which sheet it judges. Writing fifty-five strings instead would say the same five things eleven
 * times over.
 */
@Composable
private fun phraseOf(move: Move): String {
    val says = when (val lever = move.lever) {
        is Stepped -> stringResource(
            lever.steps.first { it.name == (move.to as At).name }.says
        )
        is Numeric -> reads(lever, move.to)
        else -> ""
    }
    val way = stringResource(
        if (move.direction == Direction.Harder) R.string.notice_harder
        else R.string.notice_easier
    )
    return sheetOf(move.lever.key)?.let { sheet ->
        stringResource(R.string.notice_of_sheet, way, stringResource(nameOfSheet(sheet)), says)
    } ?: stringResource(R.string.notice_move, way, says)
}

/**
 * What a numeric lever's position reads as.
 *
 * Three cases, and each is declared with the lever: the sentinel, which is what *no maximum*
 * looks like; zero, wherever it means something of its own -- *from memory*, *replay forbidden*;
 * and the count itself, which is a plural because one attempt and two attempts are not the same
 * sentence in any language.
 */
@Composable
private fun reads(lever: Numeric, at: Position): String {
    val count = (at as? Count)?.n
    return when {
        count == null -> stringResource(lever.saysUnbounded!!)
        count == 0 && lever.saysZero != null -> stringResource(lever.saysZero!!)
        else -> pluralStringResource(lever.says, count, count)
    }
}

/** Which sheet a sensitivity judges, or null on every other lever. */
private fun sheetOf(key: String): String? = Sheets.all
    .mapNotNull(Sheets::scoredPathOf)
    .firstOrNull { Levers.sensitivityOf(it) == key }
