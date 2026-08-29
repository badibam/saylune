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
import java.util.Locale

/**
 * What the analysis found, sound by sound, laid out rather than written out.
 *
 * The first shape of this was a monospace block, and it wrapped: a table wide enough to be
 * complete is wider than a phone, and no choice of column widths makes that safe. Laid out,
 * the two halves are each a weight of the row, so they face each other whatever the width --
 * which is the whole point, since what has to be compared is two spreads side by side.
 *
 * No times here. They are in logcat, where width costs nothing, together with the widened
 * durations that are how a degenerate alignment gives itself away.
 */
@Composable
fun AnalysisReadout(sounds: List<AnalysedSound>, modifier: Modifier = Modifier) {
    if (sounds.isEmpty()) return
    var open by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        sounds.forEachIndexed { index, sound ->
            Line(sound, marked = sound.points > NOISE_BAND) {
                open = if (open == index) null else index
            }
            if (open == index) Spreads(sound)
        }
    }
}

/** One sound: its letters, the two peaks, its points, and a bar. */
@Composable
private fun Line(sound: AnalysedSound, marked: Boolean, onTap: () -> Unit) {
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
        Text(
            "${sound.symbol}→${sound.said.firstOrNull()?.symbol ?: "?"}",
            modifier = Modifier.weight(1.2f),
            style = mono,
        )
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
private fun Spreads(sound: AnalysedSound) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Half("model", sound.model, Modifier.weight(1f))
        VerticalDivider(modifier = Modifier.height(80.dp).padding(horizontal = 4.dp))
        Half("you", sound.said, Modifier.weight(1f))
    }
    HorizontalDivider()
}

@Composable
private fun Half(title: String, shares: List<Share>, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(title, style = mono, fontWeight = FontWeight.Bold)
        shares.forEach { share ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(share.symbol, modifier = Modifier.width(28.dp), style = mono)
                Bar(share.part, Modifier.weight(1f))
                Text(
                    "%.2f".format(Locale.ROOT, share.part),
                    modifier = Modifier.width(36.dp),
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

/** Where the ramp of `MarkingColors.kt` tops out, so a full bar means a saturated mark. */
private const val SATURATES = 30f

private val mono = androidx.compose.ui.text.TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 11.sp,
)
