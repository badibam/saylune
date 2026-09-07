package app.speakup.ui

/**
 * What the font carries where Unicode carries nothing.
 *
 * The pieces of a frame and the furniture live in the private use area from `U+E000`, and
 * never on Unicode's own box-drawing codepoints, so that nothing breaks if the base font
 * changes (`font/README.md`). They are written as escapes and named here so that nothing else
 * carries a bare codepoint: a private-use character in a layout says nothing about what it
 * draws, and it does not survive a copy through a tool that does not know the font.
 *
 * **A frame in a grid of characters *is* text**: one lays a corner, some edges, another
 * corner, and it aligns itself for the price of a string. That is why these are glyphs rather
 * than shapes painted on a `Canvas`, which would have to be measured (`ui.md`).
 */
object Glyphs {

    /**
     * The eight pieces of a frame, in the order the font cut them.
     *
     * A frame is written **twice**, once per layer, in two decor tones exactly on top of each
     * other: the two outer pixels of the border in the light tone, the two inner in the dark.
     * That is what gives the soft relief of a console border rather than a flat band. The two
     * sets are complementary -- no pixel belongs to both -- so the order they are drawn in
     * does not matter.
     */
    enum class Piece { TopLeft, Top, TopRight, Left, Right, BottomLeft, Bottom, BottomRight }

    /** The piece [of], in the light layer or the dark one. */
    fun frame(of: Piece, dark: Boolean): Char =
        (FRAMES + (if (dark) Piece.entries.size else 0) + of.ordinal).toChar()

    // ── The furniture ───────────────────────────────────────────────────────────────────
    //
    // Named for what they are, never for the one place they are used: the triangle is `PLAY`
    // and not `hear-the-model`, and the same glyph serves wherever something is played.

    /** The transport family, drawn whole so none of them looks lighter than its neighbour. */
    const val PLAY = '\uE010'
    const val RECORD = '\uE011'
    const val PAUSE = '\uE01D'
    const val STOP = '\uE01E'

    /** The four gauge blocks, a quarter of a cell apart, which pave a bar with no gutter. */
    const val GAUGE_QUARTER = '\uE012'
    const val GAUGE_HALF = '\uE013'
    const val GAUGE_THREE_QUARTERS = '\uE014'
    const val GAUGE_FULL = '\uE015'

    const val ARROW_LEFT = '\uE016'
    const val ARROW_UP = '\uE017'
    const val ARROW_RIGHT = '\uE018'
    const val ARROW_DOWN = '\uE019'

    const val CHECK = '\uE01A'
    const val CROSS = '\uE01B'

    /** The lives, which the status line reads up to three of without counting. */
    const val HEART = '\uE01C'

    const val MIC = '\uE01F'
    const val MAGNIFIER = '\uE020'

    /** What is displayed, and what this sitting leaves open -- two entries of the action bar. */
    const val EYE = '\uE021'
    const val LEVERS = '\uE022'

    /**
     * The return key's arrow, which the action bar does not use: going back a screen is a
     * direction, and it is [ARROW_LEFT] that says it.
     */
    const val BACK = '\uE023'

    /** Start the take over, in capture by hand. */
    const val REDO = '\uE024'

    const val LOCK = '\uE025'

    /** The passage's notes. */
    const val HISTOGRAM = '\uE026'

    /** Where a stepped lever sits: one filled dot among hollow ones. */
    const val DOT_FILLED = '\uE027'
    const val DOT_HOLLOW = '\uE028'

    private const val FRAMES = 0xE000
}
