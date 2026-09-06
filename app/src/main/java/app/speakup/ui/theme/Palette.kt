package app.speakup.ui.theme

import androidx.compose.runtime.Immutable

/**
 * The seven numbers a register is made of.
 *
 * **Two registers, one hue: plum, in night or in pale.** Only the lightness swaps, and the
 * decor tones run the other way with it -- on a dark ground a panel lightens, on a pale ground
 * it darkens. Everything else in [Palette] is derived from these, which is what keeps the two
 * registers the same palette rather than two palettes that have to be kept in agreement.
 *
 * Plum because **it is the one cool hue no measure has reserved**: amber and red are the alarm,
 * green serves twice -- the stress target, the *juste* label -- and blue carries the model's
 * melody contour. A ground in one of those families would make a measure look like a shade of
 * the ground.
 *
 * The values are the bench's, read off a full screen drawn at a phone's real resolution
 * (`tmp/pixel-bench.html`), because they are judged by looking rather than by reasoning.
 */
@Immutable
data class Register(
    /** Ground: lightness, chroma, hue. */
    val groundLightness: Float,
    val groundChroma: Float,
    val hue: Float,
    /** Neutral ink's lightness. Its chroma is the faint plum every register's text carries. */
    val inkLightness: Float,
    /** The alarm ramp: one lightness and one chroma for all four notches. */
    val rampLightness: Float,
    val rampChroma: Float,
    /** The hue the ramp ends on. It starts at amber and turns toward red. */
    val rampEndHue: Float,
)

/**
 * **The night plum.**
 *
 * On a dark ground the marking distance is carried mostly by lightness: a marked letter
 * **lights up**.
 */
val NightPlum = Register(
    groundLightness = 0.20f,
    groundChroma = 0.045f,
    hue = 300f,
    inkLightness = 0.64f,
    rampLightness = 0.78f,
    rampChroma = 0.130f,
    rampEndHue = 32f,
)

/**
 * **The pale plum, and it is mid-light rather than white** -- the ramp is what demands it. The
 * higher the ground goes, the further the ramp has to come down to stand off it, and under
 * 0,40 an amber is a brown: the amber-to-red arc would lose its lower half.
 *
 * Here the distance is carried by chroma, the neutral ink being nearly colourless: a marked
 * letter **takes on colour**.
 */
val PalePlum = Register(
    groundLightness = 0.94f,
    groundChroma = 0.075f,
    hue = 301f,
    inkLightness = 0.40f,
    rampLightness = 0.43f,
    rampChroma = 0.140f,
    rampEndHue = 29f,
)

/**
 * Every colour of the register, derived from its seven numbers.
 *
 * **The form carries the channel and the colour carries only the alarm and the side**: marked
 * things share the one ramp and are told apart by their stroke. That is what holds the budget
 * to some thirty entries, and it is what `docs/reference.md` protects -- the tint of a letter
 * **is** the gap to the model, so no dressing may spend that channel on decoration.
 *
 * Entries are held as [Lch] rather than as ready colours: the halo mixes toward the ground in
 * OKLab, and a colour already flattened into sRGB cannot be mixed correctly any more.
 */
@Immutable
class Palette(private val register: Register) {

    private val hue = register.hue

    /** Which way the decor runs: up off a dark ground, down off a pale one. */
    private val up = if (register.groundLightness < 0.5f) 1f else -1f

    // ── The three grounds ───────────────────────────────────────────────────────────────

    /** The screen, and the ground every halo is mixed toward. */
    val ground = Lch(register.groundLightness, register.groundChroma, hue)

    /** A panel: a flat field on the cells, which is the softest shape available -- it has no
     *  edge to soften. */
    val panel = Lch(register.groundLightness + 0.055f * up, register.groundChroma, hue)

    /**
     * A frame, in **two tones by two exactly aligned layers of text** -- the two outer pixels
     * of the border in the light tone, the two inner in the dark. That is what gives the soft
     * relief of a console border rather than a flat band.
     */
    val frameLight = Lch(register.groundLightness + 0.46f * up, decorChroma, hue + 14f)
    val frameDark = Lch(register.groundLightness + 0.20f * up, decorChroma, hue)

    // ── The inks ────────────────────────────────────────────────────────────────────────

    /** What a letter with nothing to report is painted with. */
    val ink = Lch(register.inkLightness, INK_CHROMA, hue)

    /** Dimmed: a label, and what is set aside from the sentence between brackets. */
    val dim = Lch(register.inkLightness - 0.24f * up, INK_CHROMA, hue)

    // ── The alarm ───────────────────────────────────────────────────────────────────────

    /**
     * Four notches from amber to red **at constant lightness**, so the ramp reads as one
     * family. Its chroma stays high: that is what carries legibility, and softening it would
     * cost that. The register's gentleness comes from the ground, the frames and the shapes,
     * never from starving the ramp.
     */
    val ramp: List<Lch> = (0 until RAMP_NOTCHES).map {
        val turn = (register.rampEndHue - RAMP_START_HUE) * it / (RAMP_NOTCHES - 1)
        Lch(register.rampLightness, register.rampChroma, RAMP_START_HUE + turn)
    }

    // ── The two greens ──────────────────────────────────────────────────────────────────

    /**
     * The good end, wherever it exists: the `juste` notch of relevance and `entre les lignes`
     * of following. It is the only measure of the project that has a good end.
     */
    val green = Lch(register.rampLightness, 0.10f, GREEN_HUE)

    /** Where the stress belonged. Never shown on a correct turn, so it marks a destination. */
    val accent = Lch(register.rampLightness - 0.10f * up, 0.09f, GREEN_HUE)

    // ── The melody ──────────────────────────────────────────────────────────────────────

    /**
     * The model's contour, painted **over** the learner's at the same thickness, in a calm
     * blue. Where the two agree the red underneath is entirely covered, so **speaking well
     * makes the colour disappear** rather than change it, and the amount of red showing is the
     * amount of the gap. Melody has no colour of its own: it borrows the common ramp.
     */
    val melodyModel = Lch(register.inkLightness + 0.08f * up, 0.055f, MELODY_HUE)

    /** The learner's contour, on the red end of the ramp. */
    val melodyLearner = Lch(register.rampLightness, register.rampChroma, register.rampEndHue)

    /**
     * A stroke's softened edge: the same colour mixed toward the ground.
     *
     * **Outside the stroke, never carved into it.** A two-pixel stroke whose edges were
     * softened would have no pixel left at full colour; it would look thinner and duller than
     * it is.
     */
    fun halo(of: Lch, strength: Float): Lch = of.mix(ground, strength)

    private val decorChroma: Float
        get() = maxOf(register.groundChroma, 0.045f)

    companion object {
        /** The faint plum the text carries, so the ink belongs to the register. */
        const val INK_CHROMA = 0.018f

        /** Four notches: enough gradation to read, few enough that each one means something. */
        const val RAMP_NOTCHES = 4

        /** Amber. The ramp turns from here toward its register's end hue. */
        const val RAMP_START_HUE = 88f

        const val GREEN_HUE = 150f
        const val MELODY_HUE = 250f
    }
}
