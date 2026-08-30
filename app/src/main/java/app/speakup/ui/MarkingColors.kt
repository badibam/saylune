package app.speakup.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The marking palette. Three scales, three graphic channels, and colour carries only the
 * alarm -- never the scale, which is carried by the form: letters coloured for the phoneme,
 * a rule underneath for stress, a line behind the text for melody.
 */
@Immutable
data class MarkingColors(
    /** Neutral ink: what a letter with nothing to report is painted with. */
    val ink: Color,
    /**
     * Lightness the phoneme ramp is drawn at. It cannot be the ink's own lightness: a hue
     * laid down at 16% is black, and the fault would be painted invisibly.
     */
    val phonemeLightness: Float,
    val modelContour: Color,
    val learnerContour: Color,
    /**
     * A word not one sound of which came through. The saturated end of the phoneme ramp
     * rather than a colour of its own: it is the same alarm, said about a bigger thing, and
     * a second hue would read as a different kind of fault.
     */
    val wordFault: Color,
    /** Where the stress landed and had no business being. */
    val stressStray: Color,
    /** Where it belonged. Never shown on a correct turn, so it marks a destination, not a pass. */
    val stressTarget: Color,
    /** The ground the halo is punched out of; must match what is actually behind the text. */
    val surface: Color,
)

@Composable
fun markingColors(): MarkingColors {
    val dark = isSystemInDarkTheme()
    val surface = MaterialTheme.colorScheme.surface
    return if (dark) {
        MarkingColors(
            ink = Color.hsl(220f, 0.06f, 0.92f),
            phonemeLightness = 0.68f,
            wordFault = Color.hsl(8f, 0.90f, 0.68f),
            modelContour = Color.hsl(212f, 0.18f, 0.52f),
            learnerContour = Color.hsl(199f, 0.86f, 0.62f),
            stressStray = Color.hsl(8f, 0.86f, 0.60f),
            stressTarget = Color.hsl(148f, 0.52f, 0.52f),
            surface = surface,
        )
    } else {
        MarkingColors(
            ink = Color.hsl(220f, 0.06f, 0.14f),
            phonemeLightness = 0.44f,
            wordFault = Color.hsl(8f, 0.90f, 0.44f),
            modelContour = Color.hsl(212f, 0.26f, 0.66f),
            learnerContour = Color.hsl(212f, 0.74f, 0.44f),
            stressStray = Color.hsl(8f, 0.82f, 0.46f),
            stressTarget = Color.hsl(148f, 0.58f, 0.33f),
            surface = surface,
        )
    }
}

/**
 * Amber to red as the gap widens, at a fixed lightness so the ramp reads as one family.
 * Below the noise band the letter keeps the neutral ink: a tint there would be a lie of
 * precision, since the engine's own spread is that wide.
 */
fun phonemeColor(points: Float, colors: MarkingColors): Color {
    if (points <= NOISE_BAND) return colors.ink
    val t = ((points - NOISE_BAND) / 25f).coerceIn(0f, 1f)
    return Color.hsl(55f - 47f * t, 0.90f, colors.phonemeLightness)
}

/** Points of deviation below which the engine's own noise cannot be told from a fault. */
const val NOISE_BAND = 5f
