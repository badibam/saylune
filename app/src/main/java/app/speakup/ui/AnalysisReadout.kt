package app.speakup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Share
import app.speakup.conversation.Side
import app.speakup.marking.AddedSound
import app.speakup.marking.readoutRows
import java.util.Locale

/**
 * What the analysis found, sound by sound, laid out rather than written out.
 *
 * The first shape of this was a monospace block, and it wrapped: a table wide enough to be
 * complete is wider than a phone, and no choice of column widths makes that safe. Laid out,
 * the two halves are each a weight of the row, so they face each other whatever the width --
 * which is the whole point, since what has to be compared is two spreads side by side.
 *
 * Every character of the turn is here, sound or no sound. A letter no sound carries -- a
 * silent one, a space, punctuation -- gets a line of its own with a dash where the verdict
 * would be. Without them this is a list of sounds and not a sentence, and there is no way to
 * tell where in the phrase a line sits; with them the phrase reads down the column.
 *
 * A sound the learner **added** gets a line too, in its place, with a dash on the model's
 * side: there was nothing there to compare it to, which is exactly what it says. No points
 * and no bar on that line -- an insertion has no model side, so it has no degree.
 *
 * No times here. They are in logcat, where width costs nothing, together with the widened
 * durations that are how a degenerate alignment gives itself away.
 */
@Composable
fun AnalysisReadout(
    text: String,
    sounds: List<AnalysedSound>,
    added: List<AddedSound>,
    /** One sound of this turn, in one of the two recordings, at its place in the phrase. */
    onHearSound: (AnalysedSound, Side) -> Unit = { _, _ -> },
    /**
     * A symbol on its own, from the pre-recorded set -- what `ʃ` means, not how this turn
     * said it. The spread names sounds nobody produced here: they are the runners-up of a
     * distribution, so there is no stretch of either recording to point at, and a recording
     * of the sound itself is the only thing that can answer.
     */
    onHearSymbol: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (sounds.isEmpty()) return
    var open by remember { mutableStateOf<Int?>(null) }
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

    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            when (val entry = row.of) {
                null -> Unheard(text.substring(row.at.first, row.at.last + 1))
                is Entry.Added -> Inserted(entry.sound, text)
                is Entry.Heard -> {
                    Line(
                        entry.sound,
                        marked = entry.sound.points > NOISE_BAND,
                        onHear = { side -> onHearSound(entry.sound, side) },
                    ) {
                        open = if (open == index) null else index
                    }
                    if (open == index) Spreads(entry.sound, onHearSymbol)
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
 * Both are placed from the same number, the offset of the character they come after, which
 * is the one the wedge under the phrase is drawn from too. A mark goes immediately before
 * the first sound that claims a character past it; a sound holding none is transparent to
 * that test and keeps its own place, so the table can show it without moving anything.
 *
 * It used to be merged on a line number carried beside the offset, and the two drifted --
 * the number was counted over the sounds that hold letters while the table draws a row for
 * every sound. One position, read twice, cannot disagree with itself.
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
 * A stretch the learner said that belongs to no word.
 *
 * A wedge where a letter would be and a dash where the model's symbol would be, because
 * there was neither -- that is the whole content of the line. No points and no bar: every other reading is two spreads
 * compared, and this one has a single side, so there is no degree to report and inventing
 * one would put it on a scale it does not belong to.
 */
@Composable
private fun Inserted(added: AddedSound, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "\u25b2",
            modifier = Modifier.weight(1.1f),
            style = mono,
            color = markingColors().added,
        )
        Text(
            "-\u2192${added.symbol}",
            modifier = Modifier.weight(1.2f),
            style = mono,
            color = markingColors().added,
        )
        Text(
            "add",
            modifier = Modifier.weight(2.2f),
            style = mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A stretch of the turn no sound claims. Its letters, and a dash: there is nothing to say
 * about it, and the line exists so that nothing is missing from the phrase.
 *
 * Not tappable, and no bar. A dash is not a good score -- it is the absence of a reading --
 * and drawing an empty bar for it would put it on the same scale as a sound that came
 * through clean.
 *
 * Quoted, because such a stretch is often nothing but the space between two words, and an
 * unquoted space is an empty line that looks like a bug.
 */
@Composable
private fun Unheard(letters: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "\"$letters\"",
            modifier = Modifier.weight(1.1f),
            style = mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "-",
            modifier = Modifier.weight(3.4f),
            style = mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * One sound: its letters, the two peaks, its points, and a bar.
 *
 * The model's symbol is its own target and the row is another: tapping the symbol plays that
 * sound of the model, tapping anywhere else opens the two spreads. A symbol printed with no
 * way to hear it is a name for something the reader has never heard, which is most of what
 * IPA is to most people.
 */
@Composable
private fun Line(
    sound: AnalysedSound,
    marked: Boolean,
    onHear: (Side) -> Unit,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTap).padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            sound.letters.ifEmpty { "·" } + if (sound.borrowed) "*" else "",
            modifier = Modifier.weight(1.1f),
            style = mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // A hint and never a verdict: the app depends on no symbol here, only on the gap
        // between the two whole shapes.
        // Two targets facing each other: the model's sound at this place, and the
        // learner's at the same place. Which is which is written rather than selected --
        // they sit side by side, so a selector could only contradict the finger.
        Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                sound.symbol,
                modifier = Modifier
                    .clickable { onHear(Side.Model) }
                    .padding(horizontal = 2.dp),
                style = mono,
                fontWeight = FontWeight.Bold,
            )
            Text("→", style = mono)
            Text(
                sound.said.firstOrNull()?.symbol ?: "?",
                modifier = Modifier
                    .clickable { onHear(Side.Learner) }
                    .padding(horizontal = 2.dp),
                style = mono,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            "%.1f".format(Locale.ROOT, sound.points),
            modifier = Modifier.weight(0.8f),
            style = mono,
            textAlign = TextAlign.End,
            fontWeight = if (marked) FontWeight.Bold else FontWeight.Normal,
        )
        Bar(
            part = (sound.points / SATURATES).coerceIn(0f, 1f),
            modifier = Modifier.weight(1.4f).padding(start = 6.dp),
        )
    }
}

/** The two spreads, each half a weight of the row, so they cannot drift out of line. */
@Composable
private fun Spreads(sound: AnalysedSound, onHearSymbol: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Half("model", sound.model, Modifier.weight(1f), onHearSymbol)
        VerticalDivider(modifier = Modifier.height(100.dp).padding(horizontal = 4.dp))
        Half("you", sound.said, Modifier.weight(1f), onHearSymbol)
    }
    HorizontalDivider()
}

@Composable
private fun Half(
    title: String,
    shares: List<Share>,
    modifier: Modifier,
    onHearSymbol: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text(title, style = mono, fontWeight = FontWeight.Bold)
        shares.forEach { share ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    share.symbol,
                    modifier = Modifier
                        .width(34.dp)
                        .clickable { onHearSymbol(share.symbol) },
                    style = mono,
                )
                Bar(share.part, Modifier.weight(1f))
                Text(
                    "%.2f".format(Locale.ROOT, share.part),
                    modifier = Modifier.width(44.dp),
                    style = mono,
                    textAlign = TextAlign.End,
                )
            }
        }
        // What the kept shares leave out, so the column is honest about being a top few.
        val rest = (1f - shares.sumOf { it.part.toDouble() }).coerceAtLeast(0.0)
        Text(
            "rest %.2f".format(Locale.ROOT, rest),
            style = mono,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Bar(part: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(6.dp)) {
        Box(
            Modifier
                .fillMaxWidth(part)
                .height(6.dp)
                .background(phonemeColor(part * SATURATES, markingColors()))
        )
    }
}

// 11 sp fitted the widest row on the narrowest phone and was unreadable doing it. The row
// is laid out by weights rather than by columns of characters, so it reflows instead of
// wrapping, and the size is free to be chosen for the eye. The fixed widths below follow it.
private val mono = androidx.compose.ui.text.TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
)
