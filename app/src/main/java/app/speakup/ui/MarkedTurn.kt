package app.speakup.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import app.speakup.judged.Judgement
import app.speakup.judged.Span
import app.speakup.judged.Word
import app.speakup.analysis.AnalysedSound
import app.speakup.marking.Pause
import app.speakup.marking.TurnMarking
import app.speakup.marking.pausesOf
import app.speakup.providers.words
import app.speakup.ui.theme.Grid
import app.speakup.ui.theme.Rhythm
import app.speakup.ui.theme.Speakup
import app.speakup.ui.theme.Typography

/**
 * One turn of the learner, with everything measured on it.
 *
 * This is the densest screen of the app and the only one every pixel of which carries a
 * measure. Its whole shape comes from one fact: **the grid is horizontal**, so a word starts
 * at its column times the cell and every mark covers a whole number of cells. Nothing here is
 * measured at the sub-pixel any more; the layout is arithmetic and lives in `TurnLayout.kt`,
 * where it can be proved without a device.
 *
 * ## What is drawn, and in what order
 *
 * The order is imposed, not incidental: **the melody, the relevance, the letters -- set-aside
 * fragments and the pause column included, which live in the cells -- then the squiggle, then
 * the rule.** The letters are painted over the brackets, so an enclosure never eats a glyph.
 *
 * **The marks live in the leading, not in the cell.** Eleven pixels do not hold the letters,
 * their enclosure, the rule and the squiggle at once, so the letters keep their cell and
 * everything else spreads into the room added below. The share is not symmetric: above the
 * letters there is only the top of the enclosure, below there is the enclosure, the rule and
 * the squiggle.
 *
 * **The rule and the squiggle have a row each**: at two pixels thick they no longer hold
 * mixed, and one group often carries both.
 */
@Composable
fun MarkedTurn(
    marking: TurnMarking,
    /** What the language model marked, or null on a turn nothing judged. */
    judged: Judgement?,
    /** Which marks the learner has left on. */
    channels: Channels = Channels.All,
    /** Where each sound was said, which is what the pauses are read off. */
    sounds: List<AnalysedSound> = emptyList(),
    /** How long the recording ran, which is what the closing silence is measured against. */
    recorded: Int? = null,
    modifier: Modifier = Modifier,
    onTapCharacter: ((Int) -> Unit)? = null,
) {
    val grid = Speakup.grid
    val rhythm = Speakup.rhythm
    val colors = markingColors()
    val measurer = rememberTextMeasurer()
    val style = Speakup.type.text
    val density = LocalDensity.current

    BoxWithConstraints(modifier) {
        val widthPx = with(density) { maxWidth.roundToPx() }
        val cell = grid.painted(Grid.CELL).toInt()
        val columns = (widthPx / cell).coerceAtLeast(1)

        val pauses = remember(marking, sounds, recorded) {
            pausesOf(marking.text, sounds, recorded)
        }
        val laid = remember(marking, judged, columns) {
            val stumbling = judged?.words()?.stumbling ?: kept(marking.text)
            wrap(tokensOf(marking.text, stumbling), columns)
        }
        val lineHeight = Stack(rhythm).height

        // Measured once per line and reused for drawing: the font is fixed-width at a whole
        // scale, so a character's column times the cell is where it is, and the layout is only
        // asked for its baseline.
        val painted = remember(laid, colors, style, channels) {
            laid.map { line -> measurer.measure(tinted(line, marking, channels, colors), style, softWrap = false) }
        }

        val tap = rememberUpdatedState(onTapCharacter)
        val tapModifier = if (onTapCharacter == null) Modifier else {
            Modifier.pointerInput(laid, cell, lineHeight) {
                detectTapGestures { at ->
                    offsetAt(laid, at.x.toInt() / cell, at.y.toInt() / (lineHeight * grid.scale))
                        ?.let { offset -> tap.value?.invoke(offset) }
                }
            }
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(grid.drawn(laid.size * lineHeight))
                .then(tapModifier)
        ) {
            laid.forEachIndexed { index, line ->
                drawLine(line, painted[index], marking, judged, pauses, channels,
                         colors, grid.scale, rhythm, index)
            }
        }
    }
}

/**
 * The vertical stack of one wrapped line, in drawing pixels from its own top.
 *
 * **A line of a marked turn is not a whole number of grid rows, and that is the point.** In
 * the vertical nothing is anchored (`ui.md`): the frames, the panels and the margins sit
 * on the cell, the height a line of text sits at does not. Rounding this up to five rows is
 * what ate the four pixels the interface doc had budgeted -- the curve's halo then ran into
 * the squiggle of the line above, and the enclosure had nothing between it and the band.
 *
 * The count, top down: the halo of a curve touching the very top, the band, air, the arm of
 * the enclosure standing above the ink box, the box and everything hanging under it, and air
 * before the next line's halo.
 */
private class Stack(rhythm: Rhythm) {
    /** The band's own top: the halo of a curve at its ceiling reaches above it. */
    val bandTop = rhythm.halo

    /** The ink box's top, which everything under the line is measured off -- never the baseline. */
    val boxTop = bandTop + rhythm.melodyBand + CLEAR + 1 + rhythm.reach

    /** Where what hangs under the line begins: past the air and the gap. */
    val markTop = boxTop + Typography.BOX + rhythm.air + rhythm.gap

    /** The squiggle has its own row, under the rule's: at two pixels thick they no longer mix. */
    val squiggleTop = markTop + rhythm.stroke + 1

    val height = squiggleTop + rhythm.stroke + CLEAR
}

/**
 * The air kept clear on both sides of the band, so no two marks of two lines ever touch.
 *
 * Two pixels was what the arithmetic asked for and it read as touching: the curve's halo lands
 * in the row above the band, so what the eye sees between a squiggle and the next line's melody
 * is this less the halo. Four is what it takes for the two to read as separate.
 */
private const val CLEAR = 4

private fun DrawScope.drawLine(
    line: List<Placed>,
    painted: TextLayoutResult,
    marking: TurnMarking,
    judged: Judgement?,
    pauses: List<Pause>,
    channels: Channels,
    colors: MarkingColors,
    scale: Int,
    rhythm: Rhythm,
    index: Int,
) {
    val stack = Stack(rhythm)
    fun px(pixels: Int) = (pixels * scale).toFloat()
    val top = (index * stack.height * scale).toFloat()
    val bandTop = top + px(stack.bandTop)
    val ty = top + px(stack.boxTop)

    if (Channel.Melody in channels) melody(line, marking, colors, scale, rhythm, bandTop)
    if (Channel.Brackets in channels) judged?.spans?.forEach { span ->
        enclosure(line, span, colors, scale, rhythm, ty)
    }
    drawText(painted, topLeft = Offset(0f, ty + px(Typography.ASCENT) - painted.firstBaseline))
    if (Channel.Pauses in channels) points(line, pauses, colors, scale, ty)
    if (Channel.Squiggle in channels) judged?.spans?.forEach { span ->
        squiggle(line, span, colors, scale, rhythm, top + px(stack.squiggleTop))
    }
    if (Channel.Stress in channels) rules(line, marking, colors, scale, rhythm,
                                          top + px(stack.markTop))
    seams(line, marking, colors, scale, rhythm, ty)
}

// ── The letters ─────────────────────────────────────────────────────────────────────────

/**
 * One line as a string, with the tint on the letters.
 *
 * **The tint is the gap to the model**, one notch of the ramp per letter. A word not one sound
 * of which came through is painted last, over the ramp underneath: it says one thing and not
 * five, and it is the only pass that reaches a word's silent letters.
 *
 * A set-aside fragment is dimmed whole, brackets included -- they are the app's own characters
 * and carry no measure.
 */
private fun tinted(
    line: List<Placed>,
    marking: TurnMarking,
    channels: Channels,
    colors: MarkingColors,
): AnnotatedString {
    val width = line.lastOrNull()?.after ?: 0
    val chars = CharArray(width) { ' ' }
    line.forEach { placed ->
        placed.token.text.forEachIndexed { i, c -> chars[placed.column + i] = c }
    }
    return buildAnnotatedString {
        append(String(chars))
        line.forEach { placed ->
            val token = placed.token
            if (token.aside) {
                addStyle(SpanStyle(color = colors.dim), placed.column, placed.after)
                return@forEach
            }
            (0 until token.width).forEach { i ->
                val offset = token.at.first + i
                val points = marking.phonemes.firstOrNull { offset in it.start until it.end }?.points
                val faulty = marking.words.any { offset in it.start until it.end }
                val colour = when {
                    Channel.Tint !in channels -> colors.ink
                    faulty -> colors.wordFault
                    points != null -> phonemeColor(points, colors)
                    else -> colors.ink
                }
                addStyle(SpanStyle(color = colour), placed.column + i, placed.column + i + 1)
            }
        }
    }
}

// ── The melody ──────────────────────────────────────────────────────────────────────────

/**
 * The band above the line: the learner's contour underneath on the ramp's red end, the model's
 * over it in a calm blue, at the same thickness.
 *
 * **What shows is the error.** Where the two agree the red is entirely covered, so speaking
 * well makes the colour disappear rather than change it, and the amount of red showing is the
 * amount of the gap. Its sensitivity threshold is geometric -- the stroke plus the halo --
 * so below it the two cover each other and nothing appears.
 */
private fun DrawScope.melody(
    line: List<Placed>,
    marking: TurnMarking,
    colors: MarkingColors,
    scale: Int,
    rhythm: Rhythm,
    bandTop: Float,
) {
    val model = marking.modelContour()
    if (model.isEmpty) return
    val learner = marking.learnerContour()
    val bounds = marking.pitchBounds()
    val span = (bounds.endInclusive - bounds.start).takeIf { it > 0f } ?: return

    fun rowOf(pitch: Float): Int {
        val across = ((pitch - bounds.start) / span).coerceIn(0f, 1f)
        val room = rhythm.melodyBand - rhythm.stroke
        return room - Math.round(across * room)
    }

    voiced(line).forEach { reach ->
        val first = offsetOf(line, reach.column) ?: return@forEach
        // Within a run of words that are in the sentence, a column and a character advance
        // together -- the one blank between two words is the one space between them -- so the
        // character coordinate of a pixel is arithmetic and the curve crosses a word gap
        // without anything having to be done about it.
        fun charAt(pixel: Int) = first + pixel.toFloat() / Grid.CELL
        if (!learner.isEmpty) {
            contour(reach, bandTop, scale, rhythm, colors.learnerContour, colors.learnerHalo) {
                val at = charAt(it)
                if (marking.learnerPitchKnownAt(at.toInt())) rowOf(learner.at(at)) else null
            }
        }
        contour(reach, bandTop, scale, rhythm, colors.modelContour, colors.modelHalo) {
            rowOf(model.at(charAt(it)))
        }
    }
}

/**
 * One contour, **painted as a surface and not as a run of segments**.
 *
 * **For every pixel column** the vertical extent the curve occupies is worked out -- the step
 * it is on, unioned with the joint to the column before. That extent is in one piece by
 * construction, so neither a stub nor a stray pixel can appear at a corner, and the curve
 * climbs one pixel at a time rather than one cell at a time: smooth to the pixel, with no blur
 * and no anti-aliasing, both of which would touch the rgb of a surface that carries a measure.
 *
 * **The halo is outside the stroke, never carved into it**: a two-pixel stroke whose edges
 * were softened would have no pixel left at full colour. Being opaque and mixed toward the
 * ground, it does not give away what the stroke covers either.
 */
private inline fun DrawScope.contour(
    reach: Reach,
    bandTop: Float,
    scale: Int,
    rhythm: Rhythm,
    colour: Color,
    halo: Color,
    row: (Int) -> Int?,
) {
    val width = reach.width * Grid.CELL
    if (width <= 0) return
    val here = IntArray(width) { row(it) ?: MISSING }
    val x0 = reach.column * Grid.CELL
    for (x in 0 until width) {
        val y = here[x]
        if (y == MISSING) continue
        var top = y
        var bottom = y + rhythm.stroke - 1
        // The joint to the column before, which is what makes a slope one surface rather than
        // a stack of disconnected strokes.
        val before = if (x > 0) here[x - 1] else MISSING
        if (before != MISSING) {
            top = minOf(top, before)
            bottom = maxOf(bottom, before + rhythm.stroke - 1)
        }
        for (k in 1..rhythm.halo) {
            dot(x0 + x, top - k, bandTop, scale, halo)
            dot(x0 + x, bottom + k, bandTop, scale, halo)
        }
        drawRect(
            colour,
            topLeft = Offset(((x0 + x) * scale).toFloat(), bandTop + (top * scale)),
            size = Size(scale.toFloat(), ((bottom - top + 1) * scale).toFloat()),
        )
    }
}

/** No pitch here: the harmonic-lock filter threw the syllable away, which is not flat. */
private const val MISSING = Int.MIN_VALUE

private fun DrawScope.dot(x: Int, y: Int, origin: Float, scale: Int, colour: Color) {
    drawRect(
        colour,
        topLeft = Offset((x * scale).toFloat(), origin + y * scale),
        size = Size(scale.toFloat(), scale.toFloat()),
    )
}

// ── The marks under and around the letters ──────────────────────────────────────────────

/**
 * **The relevance is marked by brackets at the two ends of the group**, not by a complete
 * enclosure. `apt` is marked too, being the only measure of the project that has a good side,
 * so nearly every group carries a label and an enclosure running along the text would turn the
 * line into a chain of boxes.
 *
 * They stay an enclosure, so they are distinct from the straight rule and from the squiggle,
 * and they neither sit inside the letters nor on the baseline -- a word can be taken between
 * brackets and carry its tinted letters without the two scales being confused.
 */
private fun DrawScope.enclosure(
    line: List<Placed>,
    span: Span,
    colors: MarkingColors,
    scale: Int,
    rhythm: Rhythm,
    ty: Float,
) {
    val colour = when (span.relevance) {
        "apt" -> colors.apt
        "flat" -> colors.ramp[1]
        "off-target" -> colors.ramp.last()
        else -> return
    }
    val reach = reachOf(line, span.from, span.to) ?: return
    val stroke = rhythm.stroke
    val air = rhythm.air
    val topY = ty - (1 + rhythm.reach) * scale
    val height = ((Typography.BOX + 2 * rhythm.reach) * scale).toFloat()
    // The arms' horizontal standoff is pinned to the pixel: at a group's edge the pause column
    // shares the one blank cell with both arms, so this is not a setting.
    val foot = 2 + air + stroke
    listOf(true, false).forEach { left ->
        if (left && !reach.opens) return@forEach
        if (!left && !reach.closes) return@forEach
        val armX =
            if (left) (reach.column * Grid.CELL - 4)
            else (reach.column + reach.width) * Grid.CELL - 1 + air - stroke
        val footX = if (left) armX else armX + stroke - foot
        drawRect(colour, Offset((armX * scale).toFloat(), topY),
                 Size((stroke * scale).toFloat(), height))
        drawRect(colour, Offset((footX * scale).toFloat(), topY),
                 Size((foot * scale).toFloat(), (stroke * scale).toFloat()))
        drawRect(colour, Offset((footX * scale).toFloat(), topY + height - (stroke * scale)),
                 Size((foot * scale).toFloat(), (stroke * scale).toFloat()))
    }
}

/**
 * **The correction is underlined in a squiggle** -- yellow for *malformed*, red for *does not
 * exist*. It waves by as many pixels as it is thick: a two-pixel stroke rising by one is a
 * smudged band and not a wave.
 *
 * A straight rule would fall exactly where the stress rule falls, which is why this one waves:
 * it reads as a fault of language without anyone having to learn it.
 */
private fun DrawScope.squiggle(
    line: List<Placed>,
    span: Span,
    colors: MarkingColors,
    scale: Int,
    rhythm: Rhythm,
    y: Float,
) {
    val colour = when (span.correctness) {
        "malformed" -> colors.ramp[1]
        "not-said" -> colors.ramp.last()
        else -> return
    }
    val reach = reachOf(line, span.from, span.to) ?: return
    val x0 = reach.column * Grid.CELL
    for (i in 0 until reach.width * Grid.CELL - 1) {
        drawRect(
            colour,
            topLeft = Offset(((x0 + i) * scale).toFloat(), y + ((i / 2) % 2) * rhythm.stroke * scale),
            size = Size(scale.toFloat(), (rhythm.stroke * scale).toFloat()),
        )
    }
}

/**
 * A rule under the syllable the stress strayed onto, another under the one it belonged to.
 * Nothing at all while the two agree -- a correct turn carries no rule.
 */
private fun DrawScope.rules(
    line: List<Placed>,
    marking: TurnMarking,
    colors: MarkingColors,
    scale: Int,
    rhythm: Rhythm,
    y: Float,
) {
    marking.syllables.forEach { syllable ->
        val colour = when {
            syllable.stressStrayed -> colors.stressStray
            syllable.stressMissing -> colors.stressTarget
            else -> return@forEach
        }
        // **The rule holds one syllable**, so it is measured in characters and not in words:
        // a word's own columns would put the rule under `table` whole where the stress is on
        // one of its two halves, which is exactly what the mark is there to say.
        val first = columnOf(line, syllable.start) ?: return@forEach
        val last = columnOf(line, syllable.end - 1) ?: return@forEach
        drawRect(
            colour,
            topLeft = Offset((first * Grid.CELL * scale).toFloat(), y),
            size = Size((((last - first + 1) * Grid.CELL - 1) * scale).toFloat(),
                        (rhythm.stroke * scale).toFloat()),
        )
    }
}

/**
 * The wedges of the sounds no letter of the text carries: a sound the learner added, and one
 * the model says that English writes with nothing.
 *
 * **Between two letters and never on one**, because that is where the sound is: a sound that
 * lands on a letter belongs to that letter's word and the tint speaks for it, so what is left
 * over is exactly what no letter could write. Putting it on a neighbour would accuse a letter
 * that was said correctly.
 *
 * One shape for both, filled or hollow, because they are the same kind of thing. What tells
 * them apart cannot be colour -- added matter is always the ramp's saturated end and a bad
 * enough gap reaches the same place -- so it is the fill: **filled** is matter that is there
 * and should not be, **hollow** is a sound that belongs and that has no letter.
 *
 * They live in the air between the ink box and the marks, which is the one band nothing else
 * uses.
 */
private fun DrawScope.seams(
    line: List<Placed>,
    marking: TurnMarking,
    colors: MarkingColors,
    scale: Int,
    rhythm: Rhythm,
    ty: Float,
) {
    // **Above the letters, pointing down at the seam** (settled 2026-09-07): what hangs under
    // a line already carries the rule and the squiggle, and the air down there is spoken for.
    // Above, the enclosure only draws its two arms and their feet, so the room between them is
    // free. The tip points at the seam either way, which is what says where the sound is.
    val bottom = ty - 2 * scale
    fun wedge(after: Int, colour: Color, filled: Boolean) {
        val column = if (after < 0) 0 else columnOf(line, after)?.plus(1) ?: return
        val x = column * Grid.CELL
        (0 until WEDGE).forEach { row ->
            val half = WEDGE - row
            val y = bottom - (WEDGE - 1 - row) * scale
            if (filled || row == WEDGE - 1) {
                drawRect(colour, Offset(((x - half + 1) * scale).toFloat(), y),
                         Size(((2 * half - 1) * scale).toFloat(), scale.toFloat()))
            } else {
                listOf(x - half + 1, x + half - 1).forEach { at ->
                    drawRect(colour, Offset((at * scale).toFloat(), y),
                             Size(scale.toFloat(), scale.toFloat()))
                }
            }
        }
    }
    // Silent below the band, like every other sound mark: there the engine's own spread is
    // that wide, and a wedge drawn anyway would be a lie of precision.
    marking.gutters.filter { it.points > NOISE_BAND }
        .forEach { wedge(it.after, phonemeColor(it.points, colors), filled = false) }
    marking.added.forEach { wedge(it.after, colors.added, filled = true) }
}

/**
 * A pause: a column of points stacked **in the blank the text already has** between two words.
 *
 * Never a column of its own: the melody is anchored to the characters, so an inserted column
 * would slide the curve. Two pixels wide of the eleven, which is what lets the two arms of a
 * bracket keep the rest of the blank at a group's edge. The silences at the edges go in the
 * blank column bordering the turn.
 *
 * **No colour on the points.** What makes a pause too long depends on the setting, and no
 * setting touches a mark; tinting the longest would be worse still, the same two-second pause
 * coming out tinted in a calm turn and grey in one where a three-second blank drags. The points
 * carry the duration and nothing else.
 *
 * **Middle points and not baseline points**: three low dots mean text was taken out, which is
 * what the dimmed brackets of a fragment already say.
 */
private fun DrawScope.points(
    line: List<Placed>,
    pauses: List<Pause>,
    colors: MarkingColors,
    scale: Int,
    ty: Float,
) {
    pauses.forEach { pause ->
        val column =
            if (pause.after < 0) (line.firstOrNull()?.column ?: return@forEach) - 1
            else columnOf(line, pause.after)?.plus(1) ?: return@forEach
        if (column < 0) return@forEach
        // The blank a pause sits in has to exist: a turn whose last word ends the line has no
        // column after it, and the closing silence makes that the ordinary case rather than a
        // corner one. Painted anyway, it would be a half point at the edge of the canvas.
        if ((column + 1) * Grid.CELL * scale > size.width) return@forEach
        AT[pause.notches - 1].forEach { row ->
            drawRect(
                colors.ink,
                topLeft = Offset(((column * Grid.CELL + 4) * scale).toFloat(), ty + row * scale),
                size = Size((2 * scale).toFloat(), (2 * scale).toFloat()),
            )
        }
    }
}

/** Where the points of one, two or three notches sit inside the ink box. */
private val AT = arrayOf(intArrayOf(6), intArrayOf(4, 8), intArrayOf(2, 6, 10))

/** How many pixels tall a seam's wedge is. */
private const val WEDGE = 4

// ── Reading the layout back ─────────────────────────────────────────────────────────────

/** Which character of the turn sits at [column] on [line], or null in a blank or a bracket. */
private fun offsetOf(line: List<Placed>, column: Int): Int? {
    val placed = line.firstOrNull { column >= it.column && column < it.after } ?: return null
    if (placed.token.aside) return null
    return placed.token.at.first + (column - placed.column)
}

/** Which column the character at [offset] of the turn falls on, or null when it is elsewhere. */
private fun columnOf(line: List<Placed>, offset: Int): Int? {
    val placed = line.firstOrNull { !it.token.aside && offset in it.token.at } ?: return null
    return placed.column + (offset - placed.token.at.first)
}

private fun offsetAt(laid: List<List<Placed>>, column: Int, line: Int): Int? =
    laid.getOrNull(line)?.let { offsetOf(it, column) }

/** Every word kept, which is what a turn nothing judged looks like. */
private fun kept(text: String): List<Word> =
    app.speakup.judged.words(text).map { Word(it, "kept") }
