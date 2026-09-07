package app.speakup.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.speakup.R
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Share
import app.speakup.conversation.Side
import app.speakup.marking.AddedSound
import app.speakup.marking.readoutRows
import app.speakup.ui.theme.Speakup
import app.speakup.ui.theme.Typography
import kotlin.math.roundToInt

/**
 * What the analysis found, **one sound per line**, so the phrase reads down the column.
 *
 * Every character of the turn is here, sound or no sound. A letter no sound carries -- a silent
 * one, punctuation -- gets a line of its own with its letters and nothing beside them. Without
 * them this is a list of sounds and not a sentence, and there is no way to tell where in the
 * phrase a line sits. **A stretch that is only blank draws an empty row**, which is what a space
 * is; quoted and dashed, as it was, the spaces were the loudest thing on a screen where they are
 * the one thing that says nothing.
 *
 * A sound the learner **added** gets a line too, in its place, with a dash on the model's side:
 * there was nothing there to compare it to, which is exactly what it says. No points and no bar
 * on that line -- an insertion has no model side, so it has no degree, and inventing one would
 * put it on a scale it does not belong to.
 *
 * **The row has one target and one only: opening.** It carried three -- hear the model, hear
 * oneself, unfold -- and three buttons inside the eleven pixels of a line are missed. **The two
 * listenings go down into the open block**, where the room exists. What is lost is a gesture,
 * two presses instead of one to hear a sound; what is kept is the point of the screen, since at
 * three lines a sound one sees ten of them and the phrase disappears. By the time one wants to
 * listen, one has stopped on a line anyway.
 *
 * **The block opens in the list and is never a pop-up.** One row open at a time: tapping it
 * again closes it, tapping elsewhere moves the opening, so there is no dismissing gesture to
 * invent and nothing covers the phrase one is reading. **And the list scrolls the opened row to
 * the top** -- otherwise twenty lines unfold under a row near the bottom and nobody sees them.
 *
 * **The frame goes round the whole block, and that is what says it is a level down.** Unframed,
 * its rows were the same ink at the same size as the list around them, and one could not see
 * where an opening began or ended. Inside it nothing is framed again: a frame in a frame says a
 * level that is not there.
 *
 * **Two natures of listening live in the block, and their place separates them.** At the top,
 * `MODEL` and `YOU` play **this recording**, at this place in the phrase, each on a whole row of
 * its own. Under them, the symbols of the two spreads play the **reference sound**, recorded once
 * and for all: what that sound *is*, not what was made of it here. Without the difference one
 * believes that touching `d` replays his own `d`.
 *
 * **The three listening gestures of the app do not overlap**: touching a word of the marked turn
 * plays that word, the row of commands plays the whole phrase, and here one goes down to the
 * sound.
 */
@Composable
fun AnalysisReadout(
    text: String,
    sounds: List<AnalysedSound>,
    added: List<AddedSound>,
    /**
     * The scroll the readout is laid in, so an opened row can be brought to the top.
     *
     * Handed in rather than owned: the readout sits inside the thread's own scroll, and a second
     * scroll nested in the first would fight it for every drag.
     */
    scroll: ScrollState,
    /** One sound of this turn, in one of the two recordings, at its place in the phrase. */
    onHearSound: (AnalysedSound, Side) -> Unit = { _, _ -> },
    /**
     * A symbol on its own, from the pre-recorded set -- what `ʃ` means, not how this turn said
     * it. The spread names sounds nobody produced here: they are the runners-up of a
     * distribution, so there is no stretch of either recording to point at, and a recording of
     * the sound itself is the only thing that can answer.
     */
    onHearSymbol: (String) -> Unit = {},
    /**
     * A sound the learner added, in his own recording, at the place he made it.
     *
     * **One press plays it, with no block to open**, where every other row opens one. There is
     * nothing to put in a block: the model's side does not exist -- that is what the line says
     * -- so there is no second listening, no spread to face it, and no degree. A block holding
     * one row would be an object made to look like the others while saying less.
     */
    onHearAdded: (AddedSound) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (sounds.isEmpty()) return
    var open by remember { mutableStateOf<Int?>(null) }
    // Where the readout starts inside the scroll, so a row's own offset can be turned into a
    // scroll position. Read off the layout rather than counted in rows: a row's height is the
    // grid's business and counting it here would be a second arithmetic to keep in step.
    var top by remember { mutableStateOf(0f) }
    var rowTop by remember { mutableStateOf(0f) }
    LaunchedEffect(open) {
        if (open != null) scroll.animateScrollTo((scroll.value + rowTop - top).roundToInt())
    }
    val rows = remember(text, sounds, added) {
        readoutRows(text, interleaved(sounds, added)) { entry ->
            when (entry) {
                is Entry.Heard -> entry.sound.at
                // Claims no text: it belongs to no word, so it sits in the seam after the
                // sound it follows rather than over any character.
                is Entry.Added -> IntRange.EMPTY
            }
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .onGloballyPositioned { top = it.positionInRoot().y },
    ) {
        rows.forEachIndexed { index, row ->
            when (val entry = row.of) {
                null -> Unheard(text.substring(row.at.first, row.at.last + 1))
                is Entry.Added -> Inserted(entry.sound, onHearAdded)
                is Entry.Heard -> {
                    // The row hands its own top up as it is tapped, rather than the parent
                    // reading it after the fact: read afterwards it would be whatever the last
                    // layout pass left, which is a race with the block that has just opened.
                    Line(entry.sound) { y ->
                        open = if (open == index) null else index
                        rowTop = y
                    }
                    if (open == index) {
                        Opened(entry.sound, onHearSound, onHearSymbol)
                    }
                }
            }
        }
    }
}

/** What one line is about: a sound of the model's grid, or one the learner added to it. */
private sealed interface Entry {
    class Heard(val sound: AnalysedSound) : Entry
    class Added(val sound: AddedSound) : Entry
}

/**
 * The two kinds of line in one list, in the order of the phrase.
 *
 * Both are placed from the same number, the offset of the character they come after, which is
 * the one the wedge over the phrase is drawn from too. A mark goes immediately before the first
 * sound that claims a character past it; a sound holding none is transparent to that test and
 * keeps its own place, so the table can show it without moving anything.
 */
private fun interleaved(sounds: List<AnalysedSound>, added: List<AddedSound>): List<Entry> {
    val out = mutableListOf<Entry>()
    val pending = added.sortedBy { it.after }.toMutableList()
    for (sound in sounds) {
        val claims = sound.at.lastOrNull()
        if (claims != null) {
            while (pending.isNotEmpty() && pending.first().after < claims) {
                out.add(Entry.Added(pending.removeAt(0)))
            }
        }
        out.add(Entry.Heard(sound))
    }
    pending.forEach { out.add(Entry.Added(it)) }
    return out
}

/**
 * One sound: its letters, the two symbols, its points, and a bar.
 *
 * Twenty-seven of the twenty-eight columns the worst screen gives: six for the letters, eight
 * for `model → you`, five for the points, six for the bar, and two of separation.
 *
 * **The arrow is a column and not a character in a string.** Written as one field the arrow
 * moved from row to row -- a diphthong is two cells and a consonant one -- so the eye had no
 * line to run down and the two sides could not be told apart at a glance. Three fields now,
 * with the model's symbol pushed right against the arrow and the learner's left against it.
 *
 * **The number and the bar are two columns too**, for the same reason: the number was pushed
 * right, so its units digit was steady and its hundreds moved, which is exactly backwards --
 * what one compares down a column is how far each row got, and that is read from the left.
 */
@Composable
private fun Line(sound: AnalysedSound, onTap: (Float) -> Unit) {
    val colors = markingColors()
    var top by remember { mutableStateOf(0f) }
    Row(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned { top = it.positionInRoot().y }
            .clickable { onTap(top) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Field(sound.letters.ifEmpty { "·" } + if (sound.borrowed) "*" else "",
              LETTER_COLUMNS, colors.dim)
        // A hint and never a verdict: the app depends on no symbol here, only on the gap
        // between the two whole shapes.
        Sides(spelt(sound.symbol), spelt(sound.said.firstOrNull()?.symbol ?: "?"), colors.ink)
        Field("%.0f".format(sound.points), POINTS_COLUMNS, phonemeColor(sound.points, colors))
        Gauge(
            (sound.points / SATURATES).coerceIn(0f, 1f),
            phonemeColor(sound.points, colors),
            BAR_COLUMNS,
        )
    }
}

/**
 * The two sides of a row, with the arrow on a column of its own.
 *
 * [model] or [said] may be null, which is what a row with only one side draws: the dash goes
 * in the missing one's place, dimmed, and the arrow stays where it is on every other row.
 */
@Composable
private fun Sides(model: String?, said: String?, colour: Color) {
    val colors = markingColors()
    Field(model ?: "-", SIDE_COLUMNS, if (model == null) colors.dim else colour,
          align = TextAlign.End)
    Field(Glyphs.ARROW_RIGHT.toString(), ARROW_COLUMNS, colour, align = TextAlign.Center)
    Field(said ?: "-", SIDE_COLUMNS, if (said == null) colors.dim else colour)
}

/**
 * What the open row holds: the two recordings, then the two spreads.
 *
 * A score of lines, and it costs nothing: the height is only precious at the folded level,
 * where one wants the whole phrase.
 *
 * **The whole block is framed, and that is what says it is a level down.** Unframed, its rows
 * were the same ink at the same size as the twenty rows of the list around them and one could
 * not see where the opening began or ended -- two levels drawn as one. A frame is the
 * register's own word for *this is an object*, so the block wears it and the two listenings
 * inside it lose theirs: nesting a frame in a frame says a level that is not there.
 */
@Composable
private fun Opened(
    sound: AnalysedSound,
    onHearSound: (AnalysedSound, Side) -> Unit,
    onHearSymbol: (String) -> Unit,
) {
    val grid = Speakup.grid
    Framed(Modifier.fillMaxWidth()) {
        Column {
            Row(
                Modifier.fillMaxWidth().height(grid.cell * TOUCH_ROWS),
                horizontalArrangement = Arrangement.spacedBy(grid.cell),
            ) {
                Play(stringResource(R.string.readout_model), Modifier.weight(1f)) {
                    onHearSound(sound, Side.Model)
                }
                Play(stringResource(R.string.readout_you), Modifier.weight(1f)) {
                    onHearSound(sound, Side.Learner)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Spread(sound.model, Modifier.weight(1f), onHearSymbol)
                Spread(sound.said, Modifier.weight(1f), onHearSymbol)
            }
        }
    }
}

/**
 * One of the two listenings: the whole row is the target, not the arrow alone.
 *
 * Bare, the block's own frame being what says these are pressed. The label and the triangle at
 * the two ends of the width is what keeps the target obvious without a second border.
 */
@Composable
private fun Play(label: String, modifier: Modifier, onClick: () -> Unit) {
    val palette = Speakup.palette
    Row(
        modifier.fillMaxHeight().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = Speakup.type.text, color = palette.ink.srgb, maxLines = 1)
        Text(Glyphs.PLAY.toString(), style = Speakup.type.text, color = palette.ink.srgb)
    }
}

/**
 * One side's spread: the sounds it resembles, most first.
 *
 * The symbols are **bare**, which is what says they play the reference recording and not this
 * turn, and each takes the three rows everything touchable does.
 */
@Composable
private fun Spread(shares: List<Share>, modifier: Modifier, onHearSymbol: (String) -> Unit) {
    val grid = Speakup.grid
    val colors = markingColors()
    Column(modifier) {
        shares.forEach { share ->
            Row(
                Modifier
                    .height(grid.cell * TOUCH_ROWS)
                    .clickable { onHearSymbol(share.symbol) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Field(spelt(share.symbol), SYMBOL_FIELD, colors.ink)
                Field("%.0f".format(share.part * PER_CENT), SHARE_COLUMNS, colors.dim,
                      align = TextAlign.End)
            }
        }
    }
}

/**
 * A stretch of the turn no sound claims. Its letters, and nothing else: there is nothing to say
 * about it, and the line exists so that nothing is missing from the phrase.
 *
 * Not tappable, and no bar. A dash is not a good score -- it is the absence of a reading -- and
 * drawing an empty bar for it would put it on the same scale as a sound that came through
 * clean.
 *
 * **A stretch that is only blank draws nothing at all.** It used to be quoted and given a dash,
 * on the reasoning that an unquoted space is an empty line that looks like a bug -- and read
 * down a list, the quotes and the dashes were the loudest thing on the screen, a row of marks
 * where the phrase says nothing. An empty row between two words is what a space *is*, and in a
 * list where every other row is full it is read as the space it is.
 */
@Composable
private fun Unheard(letters: String) {
    val colors = markingColors()
    val grid = Speakup.grid
    if (letters.isBlank()) {
        Box(Modifier.fillMaxWidth().height(grid.drawn(Typography.TEXT_STEP)))
        return
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Field(letters, LETTER_COLUMNS, colors.dim)
    }
}

/**
 * A sound the learner made that the model did not: a wedge, and a dash where the model was.
 *
 * **Pressing it plays it**, in his own recording, where every other row opens a block instead.
 * There is nothing to put in a block here -- no model side, so no second listening, no spread
 * to face it and no degree -- and a block holding one row would be an object made to look like
 * the others while saying less. The triangle at the end is what says the row is pressed.
 *
 * A row whose place was never carried is not pressable, and shows no triangle.
 */
@Composable
private fun Inserted(added: AddedSound, onHear: (AddedSound) -> Unit) {
    val colors = markingColors()
    val plays = added.saidMs != null
    Row(
        Modifier
            .fillMaxWidth()
            .let { if (plays) it.clickable { onHear(added) } else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Field(Glyphs.ARROW_UP.toString(), LETTER_COLUMNS, colors.added)
        Sides(model = null, said = spelt(added.symbol), colour = colors.added)
        if (plays) {
            Field(Glyphs.PLAY.toString(), POINTS_COLUMNS, colors.added)
        }
    }
}

/** One field of a row, a whole number of cells wide so the columns line up down the list. */
@Composable
private fun Field(
    text: String,
    columns: Int,
    colour: Color,
    align: TextAlign = TextAlign.Start,
) {
    val grid = Speakup.grid
    Text(
        text,
        modifier = Modifier.width(grid.cell * columns),
        style = Speakup.type.text,
        color = colour,
        textAlign = align,
        maxLines = 1,
    )
}

/**
 * A bar paved with the font's gauge blocks: the filled part in [colour], the rest in dimmed ink.
 *
 * **The empty part of a bar is a colour and not a shape** (`ui.md`), so the two are drawn
 * one on top of the other: the whole width in dimmed ink, and the filled part over it. The
 * quarter blocks give a cell four steps, and a partial cell over a dimmed full one reads as a
 * part-filled cell rather than as a gap.
 */
@Composable
private fun Gauge(part: Float, colour: Color, columns: Int) {
    val grid = Speakup.grid
    val colors = markingColors()
    val quarters = (part * columns * QUARTERS).roundToInt()
    Box(Modifier.width(grid.cell * columns)) {
        Text(
            Glyphs.GAUGE_FULL.toString().repeat(columns),
            style = Speakup.type.text, color = colors.dim, maxLines = 1,
        )
        Text(
            Glyphs.GAUGE_FULL.toString().repeat(quarters / QUARTERS) +
                when (quarters % QUARTERS) {
                    1 -> Glyphs.GAUGE_QUARTER.toString()
                    2 -> Glyphs.GAUGE_HALF.toString()
                    3 -> Glyphs.GAUGE_THREE_QUARTERS.toString()
                    else -> ""
                },
            style = Speakup.type.text, color = colour, maxLines = 1,
        )
    }
}

/**
 * How a symbol is **written**, which is not always how it is stored.
 *
 * **The two affricate ligatures are deliberately absent from the font.** In a fixed-width font
 * a ligature has to fit one cell anyway, so crushing a `d` and a `ʒ` at a two-pixel stroke into
 * one is strictly worse than taking two. The screen writes `dʒ` and `tʃ`, which is just as
 * standard an IPA and the commonest notation of the dictionaries, and the diphthongs already
 * take two cells, so this list is of variable-width symbols either way.
 *
 * **It is a correspondence at display and nothing else**: the data keeps `ʤ` and `ʧ`, which the
 * model emits and which every turn on the disk carries.
 */
private fun spelt(symbol: String): String = when (symbol) {
    "ʤ" -> "dʒ"
    "ʧ" -> "tʃ"
    else -> symbol
}

/** Six of the twenty-eight columns, which is what the letters of one sound take. */
private const val LETTER_COLUMNS = 6

/**
 * `model → you`, in three fields so the arrow is a column: three cells a side, which holds a
 * diphthong and the `dʒ` the display spells in two, and two for the arrow and its air.
 */
private const val SIDE_COLUMNS = 3
private const val ARROW_COLUMNS = 2

private const val POINTS_COLUMNS = 5
private const val BAR_COLUMNS = 6

/** Inside the open block, where the two spreads share the width. */
private const val SYMBOL_FIELD = 4
private const val SHARE_COLUMNS = 4

/** What every touchable entry of the app is tall. */
private const val TOUCH_ROWS = 3

/** How many steps a gauge cell has, which is what the four blocks give it. */
private const val QUARTERS = 4

private const val PER_CENT = 100
