package app.speakup.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The colours of the register, in whichever of the two it is showing.
 *
 * **Colour carries a measure, and that is what bounds everything else** (`pixel-ui.md`). On
 * the learner's turn the tint of a letter **is** the gap to the model, the rule under a
 * syllable is the stress, the band above the line is the melody. Those are not colours of
 * decoration: they are the only visible outputs of the analysis, and `docs/reference.md`
 * forbids a setting or a dressing to move them. So the register never spends the tint channel
 * on decoration over that surface, and never lays a filter on top -- anything that touches the
 * rgb would move how the tint reads.
 *
 * The values below are the neutral pair in service. What each of the two registers is made
 * of -- the grounds, the inks, the four-step alarm ramp, the greens, the melody blue, the
 * decor and halo tones -- is settled at the bench that draws a full screen at a phone's real
 * resolution, because it is judged by looking rather than by reasoning.
 */
@Immutable
data class Palette(
    /** The screen's own ground, and the ground a halo is punched out of. */
    val screen: Color,
    /** What a letter with nothing to report is painted with. */
    val ink: Color,
)

val DarkRegister = Palette(
    screen = Color.hsl(220f, 0.06f, 0.10f),
    ink = Color.hsl(220f, 0.06f, 0.92f),
)

val LightRegister = Palette(
    screen = Color.hsl(220f, 0.06f, 0.96f),
    ink = Color.hsl(220f, 0.06f, 0.14f),
)
