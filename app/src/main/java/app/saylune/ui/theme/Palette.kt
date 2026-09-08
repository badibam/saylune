package app.saylune.ui.theme

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
    /**
     * How far the ramp's lightness spreads across its four notches, away from the ground as
     * the alarm rises. Zero in the plain registers, where the ramp is at one lightness and
     * reads as one family; the spare spends it because a hue arc alone does not separate
     * four notches for an eye that does not tell amber from red.
     */
    val rampLightnessSpread: Float = 0f,
    /**
     * The hue of the good end -- the `apt` bracket, the stress target, `implied`.
     * Green, unless the eye reading it does not separate green from red.
     */
    val goodHue: Float = 150f,
    /** The model's melody contour: a calm tone laid over the learner's on the ramp's red end. */
    val melodyHue: Float = 250f,
    val melodyChroma: Float = 0.055f,
    /**
     * The remark line of the round-up: prose the model wrote about an aptitude.
     *
     * **Its own two numbers rather than the melody's**, and that is what the spare needs: there
     * the good end has taken the blue, so a remark left on the melody blue would wear the
     * colour of *this went well* while saying the opposite. Held apart, the spare can empty it
     * instead.
     */
    val remarkHue: Float = 250f,
    val remarkChroma: Float = 0.055f,
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
        val across = it.toFloat() / (RAMP_NOTCHES - 1)
        val turn = (register.rampEndHue - RAMP_START_HUE) * across
        // Away from the ground as the alarm rises, so the notches separate by lightness too
        // where they cannot separate by hue. Zero spread leaves the constant lightness.
        val lightness = register.rampLightness +
            up * register.rampLightnessSpread * (across - 0.5f)
        Lch(lightness, register.rampChroma, RAMP_START_HUE + turn)
    }

    // ── The two greens ──────────────────────────────────────────────────────────────────

    /**
     * The good end, wherever it exists: the `apt` notch of relevance and `implied`
     * of following. It is the only measure of the project that has a good end.
     */
    val green = Lch(register.rampLightness, 0.10f, register.goodHue)

    /** Where the stress belonged. Never shown on a correct turn, so it marks a destination. */
    val accent = Lch(register.rampLightness - 0.10f * up, 0.09f, register.goodHue)

    /** The one calm colour of the register, which two things use and neither of them judges. */
    private val calm =
        Lch(register.inkLightness + 0.08f * up, register.melodyChroma, register.melodyHue)

    /**
     * What the learner brought himself, where a screen shows his words inside the app's.
     *
     * **The calm blue and never the ramp**: the ramp is the alarm, and its amber on a word
     * somebody just typed would be the marking's colour saying something the marking never
     * said. It is the melody model's blue, which is the register's other named colour and
     * carries no verdict.
     */
    val own = calm

    // ── The melody ──────────────────────────────────────────────────────────────────────

    /**
     * The model's contour, painted **over** the learner's at the same thickness, in a calm
     * blue. Where the two agree the red underneath is entirely covered, so **speaking well
     * makes the colour disappear** rather than change it, and the amount of red showing is the
     * amount of the gap. Melody has no colour of its own: it borrows the common ramp.
     */
    val melodyModel = calm

    /**
     * The round-up's remark: **a calm blue, and it is not a verdict**.
     *
     * A sentence in a column of figures, saying what stands out about an aptitude. It carries
     * no note, so it takes neither the ramp nor the good end -- and where the spare register
     * has spent the blue on the good end, it drains to the plain ink rather than compete: a
     * line of prose is already told from a figure by being a line of prose.
     */
    val remark =
        Lch(register.inkLightness + 0.08f * up, register.remarkChroma, register.remarkHue)

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

    }
}

/**
 * The spare, for an eye that does not separate red from green. **A first pass, laid blind:
 * nothing here is measured, and it is the bench that has to say whether it works.**
 *
 * **The case that demands it is the worst in the doc.** *Juste* and *à côté* are **the same
 * shape** -- brackets -- and **two opposite verdicts**, told apart by colour alone, green
 * against red. Under deuteranopia the two converge: the mark of a success becomes the mark of
 * a fault. The stress rule has the same defect -- the target against the stray -- and the
 * following pastille has it over four values.
 *
 * So it is **not a permutation of hues**. Four things change, and each answers one of those:
 *
 * - **The good end goes blue.** Blue against amber-to-red is the one strong contrast that
 *   survives a red-green deficiency, since it lives on the axis that is not damaged.
 * - **The model's melody contour goes nearly colourless**, because the good end has taken the
 *   blue it used to hold. It loses nothing by it: its job is to cover the learner's contour,
 *   and what has to be told apart there is covered from not-covered.
 * - **The ramp spreads its lightness.** Four notches from amber to red are four shades of one
 *   thing to an eye that does not read that arc; spreading the lightness gives them a second
 *   axis to separate on, and the alarm gets lighter off a dark ground and darker off a pale
 *   one.
 * - **The round-up's remark drains to the ink**, for the same reason as the contour and with
 *   less to lose: it is a sentence, told from a figure by being a sentence, and left blue it
 *   would wear the colour this register gives to *this went well*.
 *
 * The plain registers keep their colours for everyone. Correcting the base palette instead
 * would tax the default with an aesthetic chosen for a perception most people do not have.
 *
 * **What it covers is not settled either.** The red-green confusions are what break the
 * brackets, so they are what is treated here. Tritanopia would touch the melody blue, which
 * this spare has already emptied of colour -- whether that is enough, or whether the case is
 * out of scope, is not answered.
 */
val NightPlumSpare = NightPlum.copy(
    rampLightnessSpread = 0.18f,
    goodHue = 255f,
    melodyChroma = 0.012f,
    remarkChroma = 0f,
)

val PalePlumSpare = PalePlum.copy(
    rampLightnessSpread = 0.18f,
    goodHue = 255f,
    melodyChroma = 0.012f,
    remarkChroma = 0f,
)
