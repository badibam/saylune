package app.saylune.ui

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
import app.saylune.R
import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Direction
import app.saylune.levers.Move
import app.saylune.levers.Numeric
import app.saylune.levers.Position
import app.saylune.levers.Stepped
import app.saylune.rules.Notice
import app.saylune.levers.Levers
import app.saylune.sheets.Sheets
import app.saylune.ui.theme.Saylune

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
 * **It is dismissed by the finger, or by a clock that shows itself.** What the doc refused is a
 * notice that **vanishes unseen**: a change nobody has seen is one the learner cannot
 * reconstruct. A countdown on the notice is not that, so at the two automatic capture positions
 * it goes on its own once read, and the preparation starts then; by hand it waits for the
 * finger, nothing else being due.
 *
 * [left] is the seconds left on that clock, null where there is none.
 */
@Composable
fun RuleNotice(
    notices: List<Notice>, onSeen: () -> Unit, modifier: Modifier = Modifier, left: Int? = null,
) {
    if (notices.isEmpty()) return
    val grid = Saylune.grid
    val palette = Saylune.palette
    Box(modifier.fillMaxWidth().padding(grid.cell), contentAlignment = Alignment.Center) {
        Framed(Modifier.fillMaxWidth().clickable(onClick = onSeen)) {
            Column(Modifier.padding(vertical = grid.cell)) {
                // The fiction first and the rules after, which is the rhythm of a game:
                // something happens in the story, then one sees what it changes.
                noticeLines(notices).forEach { line ->
                    Text(
                        line,
                        style = Saylune.type.text,
                        color = palette.ink.srgb,
                    )
                }
                left?.let {
                    Text("$it s", style = Saylune.type.text, color = palette.dim.srgb)
                }
            }
        }
    }
}

/**
 * How long a notice stays up on its own: a floor, plus so much per character.
 *
 * **Not strictly proportional**: four words at a proportional rate would flicker. Both numbers
 * are set by hand and to be revised by eye, once a shipped tile lays a patch -- none does yet.
 */
fun readingMs(lines: List<String>): Long =
    NOTICE_FLOOR_MS + NOTICE_PER_CHARACTER_MS * lines.sumOf { it.length }

private const val NOTICE_FLOOR_MS = 3_000L
private const val NOTICE_PER_CHARACTER_MS = 60L

/** Every line of the notice: the staging, then what moved. */
@Composable
fun noticeLines(notices: List<Notice>): List<String> =
    notices.filterIsInstance<Notice.Staged>().map { it.staging.text } +
        notices.filterIsInstance<Notice.Moved>().map { phraseOf(it.move) }

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
