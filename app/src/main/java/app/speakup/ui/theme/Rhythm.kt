package app.speakup.ui.theme

import androidx.compose.runtime.Immutable

/**
 * The rhythms of the marking: stroke widths, halo, air, leading.
 *
 * These are numbers shared between the text and what is painted around it, so they live in
 * the theme for the same reason the grid does -- laid down twice, they drift. They are held
 * in **drawing pixels**, whole and density-free; a caller turns them into lengths through
 * [Grid.drawn] or into paint through [Grid.painted]. That is also what keeps them provable
 * without a screen.
 *
 * **The marks live in the leading, not in the cell** (`pixel-ui.md`). Eleven pixels do not
 * hold the letters, their enclosure, the rule and the wave all at once. The letters keep
 * their cell; everything else spreads into the room added below.
 *
 * The share is not symmetric: above the letters there is only the top of the enclosure, below
 * there is the enclosure, the rule and the wave.
 */
@Immutable
data class Rhythm(
    /** The letters' own thickness, which every mark's stroke matches -- brackets included. */
    val stroke: Int = 2,

    /**
     * The melody's own band, above the line of text.
     *
     * Its vertical is a continuous quantity, and there is no reason for its fineness to be
     * capped by the height of a text cell: the band gives it twice the steps the line would
     * offer. Its horizontal stays anchored to the characters.
     */
    val melodyBand: Int = 22,

    /**
     * The halo, **outside** the stroke and never eaten into it.
     *
     * A two-pixel stroke whose edges were softened would have no pixel left at full colour;
     * it would look thinner and duller than it is. Being opaque and mixed toward the ground,
     * it does not give away the overlap either.
     *
     * With the stroke, this is the melody's sensitivity threshold, and the threshold is
     * geometric: below it the two contours cover each other and nothing shows; the error then
     * appears gradually, by the halo before the full stroke.
     */
    val halo: Int = 2,

    /** How far the halo is mixed toward the ground. Opaque in both registers. */
    val haloStrength: Float = 0.50f,

    /**
     * Between the letters and their enclosure, counted **below the descenders** and not below
     * the baseline. The whole stack drops by two, and the accents ask two more above before
     * the enclosure's top edge: the line grows by four pixels and nothing is squeezed.
     */
    val air: Int = 5,

    /** Before the marks that hang under the enclosure. */
    val gap: Int = 1,

    /** Where the text sits inside its line. */
    val baseline: Int = 8,

    /** Between two lines of a wrapped turn, in grid rows. */
    val leading: Int = 1,

    /**
     * How far the relevance enclosure reaches past the letters, above and below.
     *
     * The arms' horizontal standoff is pinned to the pixel -- at a group's edge the pause
     * column and both arms share the one blank cell -- so the height is the only parameter
     * left of that mark.
     */
    val reach: Int = 5,
)
