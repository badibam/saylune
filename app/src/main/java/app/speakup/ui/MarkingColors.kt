package app.speakup.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.speakup.ui.theme.Palette
import app.speakup.ui.theme.Speakup

/**
 * What each mark is painted with, drawn from the register's one palette.
 *
 * **The form says which scale, the colour says which side and how far from neutral.** Marked
 * things share the alarm ramp and are told apart by their stroke: the tint is on the letters,
 * a rule under a syllable is the stress, a squiggle under a group is correctness, brackets
 * around one are relevance, a band above the line is melody.
 */
@Immutable
data class MarkingColors(
    /** Neutral ink: what a letter with nothing to report is painted with. */
    val ink: Color,
    /** The alarm's four notches, amber to red, which every marked thing shares. */
    val ramp: List<Color>,
    val modelContour: Color,
    val learnerContour: Color,
    /**
     * A word not one sound of which came through. The saturated end of the ramp rather than a
     * colour of its own: it is the same alarm said about a bigger thing, and a second hue
     * would read as a different kind of fault.
     */
    val wordFault: Color,
    /**
     * A sound the learner added. The same saturated end, because the alarm is the same: this
     * is not a degree of wrong, it is a thing that is there and should not be. The channel is
     * the **form** -- a wedge between two letters -- never a colour of its own.
     */
    val added: Color,
    /** Where the stress landed and had no business being. */
    val stressStray: Color,
    /** Where it belonged. Never shown on a correct turn, so it marks a destination, not a pass. */
    val stressTarget: Color,
    /** The ground the halo is punched out of; must match what is actually behind the text. */
    val surface: Color,
)

@Composable
fun markingColors(): MarkingColors = marksOf(Speakup.palette)

/** Everything a mark is painted with, so the mapping lives in one place and is provable. */
fun marksOf(palette: Palette): MarkingColors = MarkingColors(
    ink = palette.ink.srgb,
    ramp = palette.ramp.map { it.srgb },
    modelContour = palette.melodyModel.srgb,
    learnerContour = palette.melodyLearner.srgb,
    wordFault = palette.ramp.last().srgb,
    added = palette.ramp.last().srgb,
    stressStray = palette.ramp.last().srgb,
    stressTarget = palette.accent.srgb,
    surface = palette.ground.srgb,
)

/**
 * Which notch of the ramp a sound's gap falls on, and 0 for no mark at all.
 *
 * Below the noise band the letter keeps the neutral ink: a tint there would be a lie of
 * precision, the engine's own spread being that wide. Above the gross-miss line the ramp
 * saturates -- past it the image stops changing because the difference stops meaning
 * something, and that line is the same for the screen and for the sheet.
 */
fun notchOf(points: Float): Int {
    if (points <= NOISE_BAND) return 0
    val across = (points - NOISE_BAND) / (SATURATES - NOISE_BAND)
    return (across * Palette.RAMP_NOTCHES).toInt().coerceIn(0, Palette.RAMP_NOTCHES - 1) + 1
}

/** The colour a sound's gap is painted with: the neutral ink, or one notch of the ramp. */
fun phonemeColor(points: Float, colors: MarkingColors): Color =
    notchOf(points).let { if (it == 0) colors.ink else colors.ramp[it - 1] }

/** Points of deviation below which the engine's own noise cannot be told from a fault. */
const val NOISE_BAND = 5f

/** Where the ramp saturates: the word changed, or it did not. */
const val SATURATES = 30f
