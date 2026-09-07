package app.speakup.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.speakup.ui.theme.Grid
import app.speakup.ui.theme.Speakup

/**
 * The line that names a turn, and the two marks that judge the whole of it.
 *
 * **They sit at the right end of it, aligned** (`ui.md`): the following's pastille, then
 * the pace. That takes no column from the text and adds no row, the label already existing and
 * its right half being empty. In exchange the line becomes **structural** -- it can no longer
 * leave the scaffold.
 *
 * **An empty slot says *not measured***, and not *nothing to report*. That is the one exception
 * to the project's rule, where an absent mark says there was nothing to say, and it is paid for
 * because the slot is fixed: an empty slot cannot be told from a slot nobody drew, so reading
 * approval into it would be reading approval into a hole.
 *
 * **When a name overflows, the name is cut and never the marks.** The marks carry a measure;
 * the label carries an identity one already knows, two or three speakers taking turns.
 */
@Composable
fun TurnLabel(
    /** The short name of whoever said it, which a definition declares per character. */
    name: String,
    /** The following's notch, or null where nothing judged the turn. */
    following: String?,
    /** How far the pace fell from the model's, in percent, or null where it was not measured. */
    pace: Float?,
    /** Which marks the learner has left on. */
    channels: Channels = Channels.All,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val colors = markingColors()
    val type = Speakup.type
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            name,
            modifier = Modifier.weight(1f),
            style = type.text,
            color = colors.dim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // **A channel that is off gives its slot up rather than emptying it.** These two live
        // on a line where an empty slot says *not measured*, so leaving one blank would make
        // the line lie. No slot, no channel; an empty slot, not measured.
        if (Channel.Following in channels) {
            Spacer(Modifier.width(grid.cell))
            Pastille(following?.let { followingColour(it, colors) })
        }
        if (Channel.Pace in channels) {
            Spacer(Modifier.width(grid.cell))
            Text(
                pace?.let { paceOf(it) } ?: "",
                modifier = Modifier.width(grid.cell * PACE_COLUMNS),
                style = type.text,
                color = pace?.let { paceColour(it, colors) } ?: colors.ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/**
 * A filled disc of nine pixels with a one-pixel halo, the whole eleven of the cell.
 *
 * The halo comes from the same recipe as the melody's -- opaque, mixed toward the ground --
 * so the two soft edges of the app are the one shape.
 */
@Composable
private fun Pastille(colour: Color?) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val rhythm = Speakup.rhythm
    if (colour == null) {
        // Not measured, so nothing is drawn -- and the slot stays, which is what makes the
        // emptiness readable.
        Spacer(Modifier.width(grid.cell).height(grid.cell))
        return
    }
    val halo = palette.halo(palette.ink, rhythm.haloStrength).srgb
    Canvas(Modifier.width(grid.cell).height(grid.cell)) {
        val k = grid.scale.toFloat()
        RING.forEachIndexed { row, width ->
            drawRect(halo, Offset((Grid.CELL - width) / 2f * k, row * k), Size(width * k, k))
        }
        DISC.forEachIndexed { row, inset ->
            drawRect(colour, Offset((1 + inset) * k, (1 + row) * k),
                     Size((9 - 2 * inset) * k, k))
        }
    }
}

/** The disc's inset per row, and the halo's ring around it -- the bench's own nine pixels. */
private val DISC = intArrayOf(2, 1, 0, 0, 0, 0, 0, 1, 2)
private val RING = intArrayOf(7, 7, 9, 11, 11, 11, 11, 11, 9, 7, 7)

/**
 * What the following's notch is painted with.
 *
 * **Green for `implied`, neutral ink for `precise` and `on-point`, yellow for `on-topic` and
 * `vague`, red for `off-target`** (`activity.md`). The green marks the good end wherever
 * it exists, and a correct following shows a neutral pastille rather than nothing -- the rule
 * of the dashboard, where a hole means unmeasured.
 */
private fun followingColour(notch: String, colors: MarkingColors): Color = when (notch) {
    "implied" -> colors.apt
    "precise", "on-point" -> colors.ink
    "on-topic", "vague" -> colors.ramp[1]
    else -> colors.ramp.last()
}

/**
 * The pace, in characters: the points closing in for a word squeezed, opening out for one
 * stretched.
 *
 * **The form carries the side, so the colour carries only the distance.** `=` is not a
 * compliment: it says the measure took place and found nothing, which is what the dashboard
 * demands. The five positions then read like a needle, and *not measured* has no sign of its
 * own -- it is the empty slot.
 *
 * **Two notches per side, at fixed thresholds**, written once and for all like the sounds'
 * ramp and never drawn from the sensitivity: a mark that moved with a setting would stop
 * carrying anything.
 */
private fun paceOf(gap: Float): String {
    val far = kotlin.math.abs(gap)
    return when {
        far <= INSIDE -> "="
        far <= FAR -> if (gap > 0) "><" else "<->"
        else -> if (gap > 0) ">><<" else "<-->"
    }
}

private fun paceColour(gap: Float, colors: MarkingColors): Color = when {
    kotlin.math.abs(gap) <= INSIDE -> colors.ink
    kotlin.math.abs(gap) <= FAR -> colors.ramp[1]
    else -> colors.ramp.last()
}

/** Four columns, so the pastille does not move when the notch changes. */
private const val PACE_COLUMNS = 4

/** Inside the band: the pace was measured and found nothing. */
private const val INSIDE = 8f

/** Past this, the far notch. Both are material for the calibration bench. */
private const val FAR = 30f
