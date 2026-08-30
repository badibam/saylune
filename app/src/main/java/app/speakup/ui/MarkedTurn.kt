package app.speakup.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.speakup.marking.Contour
import app.speakup.marking.TurnMarking

/**
 * One turn of the learner, carrying the three scales at once.
 *
 * The text is measured here rather than handed to a `Text`, because the paint order matters:
 * the melody line runs *behind* the glyphs and the halo has to sit between the two. A `Text`
 * fills its glyphs once and offers no seam to slide the halo into.
 *
 * Order, back to front: stress rules, model contour, learner contour, halo pass, glyph pass,
 * then the wedges of the sounds that were added, which sit between glyphs and so cannot be
 * part of any of them.
 */
@Composable
fun MarkedTurn(
    marking: TurnMarking,
    modifier: Modifier = Modifier,
    onTapCharacter: ((Int) -> Unit)? = null,
) {
    val measurer = rememberTextMeasurer()
    val colors = markingColors()
    val density = LocalDensity.current
    val style = MaterialTheme.typography.headlineSmall

    BoxWithConstraints(modifier) {
        val widthPx = with(density) { maxWidth.roundToPx() }

        // Two layouts from the same constraints and the same weight spans, so they align to
        // the pixel. The halo pass must not inherit the phoneme colours, or the outline
        // itself would be coloured; a uniform string is the only way to be sure of that.
        val layouts = remember(marking, colors, widthPx, style) {
            TurnLayouts(
                filled = measurer.measure(
                    annotate(marking, colors, tinted = true),
                    style,
                    constraints = Constraints(maxWidth = widthPx),
                ),
                outline = measurer.measure(
                    annotate(marking, colors, tinted = false),
                    style,
                    constraints = Constraints(maxWidth = widthPx),
                ),
            )
        }

        val tapModifier = if (onTapCharacter == null) Modifier else {
            // The layout is its own hit map: no per-word touch target to lay out or keep in sync.
            Modifier.pointerInput(layouts) {
                detectTapGestures { pos -> onTapCharacter(layouts.filled.getOffsetForPosition(pos)) }
            }
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(with(density) { layouts.filled.size.height.toDp() })
                .then(tapModifier)
        ) {
            drawStressRules(layouts.filled, marking, colors, density)
            drawContours(layouts.filled, marking, colors, density)
            drawText(layouts.outline, color = colors.surface, drawStyle = haloStroke(density))
            drawText(layouts.filled)
            drawAdded(layouts.filled, marking, colors, density)
        }
    }
}

private class TurnLayouts(val filled: TextLayoutResult, val outline: TextLayoutResult)

/**
 * Weight says where the model put its stress -- a property of the utterance being imitated,
 * not a verdict on the learner. Colour says how far a phoneme fell below it.
 */
private fun annotate(marking: TurnMarking, colors: MarkingColors, tinted: Boolean): AnnotatedString =
    buildAnnotatedString {
        append(marking.text)
        marking.syllables.filter { it.modelStressed }.forEach {
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), it.start, it.end)
        }
        if (tinted) {
            addStyle(SpanStyle(color = colors.ink), 0, marking.text.length)
            marking.phonemes.forEach {
                addStyle(SpanStyle(color = phonemeColor(it.points, colors)), it.start, it.end)
            }
            // Last, so it covers the ramp underneath rather than sitting beside it: a word
            // no sound of which came through says one thing, not five. It is also the only
            // pass that reaches the word's silent letters, which carry no sound and would
            // otherwise stay in neutral ink inside a word that is wholly wrong.
            marking.words.forEach {
                addStyle(SpanStyle(color = colors.wordFault), it.start, it.end)
            }
            // A silent letter the learner voiced. It carries no sound, so no ramp reaches
            // it and it would otherwise stay in neutral ink -- which is the whole reason
            // the fault was invisible before this channel existed.
            marking.added.forEach { added ->
                added.at?.let { addStyle(SpanStyle(color = colors.added), it, it + 1) }
            }
        } else {
            addStyle(SpanStyle(color = Color.Black), 0, marking.text.length)
        }
    }

private fun haloStroke(density: Density) =
    Stroke(width = with(density) { 1.2.dp.toPx() }, join = StrokeJoin.Round)

/**
 * A rule under the syllable the stress strayed onto, another under the one it belonged to.
 * Nothing at all while the two agree -- a correct turn carries no rule.
 */
private fun DrawScope.drawStressRules(
    layout: TextLayoutResult,
    marking: TurnMarking,
    colors: MarkingColors,
    density: Density,
) {
    val height = with(density) { 3.dp.toPx() }
    val gap = with(density) { 1.dp.toPx() }
    marking.syllables.forEach { syllable ->
        val color = when {
            syllable.stressStrayed -> colors.stressStray
            syllable.stressMissing -> colors.stressTarget
            else -> return@forEach
        }
        val line = layout.getLineForOffset(syllable.start)
        val left = layout.getBoundingBox(syllable.start).left
        val right = layout.getBoundingBox(syllable.end - 1).right
        drawRect(
            color = color,
            topLeft = Offset(left, layout.getLineBottom(line) - height - gap),
            size = Size(right - left, height),
        )
    }
}

/**
 * A wedge under the seam between two letters, for a sound the learner added that no letter
 * of the word can carry -- an inserted word, or a vowel the spelling does not write.
 *
 * Between the glyphs and not on one, because that is where the sound is: putting it on a
 * neighbouring letter would accuse a letter that was said correctly. It is the shape the
 * gutter has been owed since it was first measured, and nothing drew until now.
 */
private fun DrawScope.drawAdded(
    layout: TextLayoutResult,
    marking: TurnMarking,
    colors: MarkingColors,
    density: Density,
) {
    val half = with(density) { 3.dp.toPx() }
    val height = with(density) { 4.dp.toPx() }
    val gap = with(density) { 1.dp.toPx() }
    marking.added.filter { it.at == null }.forEach { added ->
        // Just after the last letter anyone claimed; at the very start when none was.
        val anchor = added.after.coerceIn(0, marking.text.length - 1)
        val line = layout.getLineForOffset(anchor)
        val box = layout.getBoundingBox(anchor)
        val x = if (added.after < 0) box.left else box.right
        val bottom = layout.getLineBottom(line) - gap
        drawPath(
            Path().apply {
                moveTo(x - half, bottom)
                lineTo(x + half, bottom)
                lineTo(x, bottom - height)
                close()
            },
            colors.added,
        )
    }
}

/**
 * The two contours, sampled at character boundaries so the curve follows the letters and
 * crosses the spaces without a seam. It breaks at each line wrap -- the only interruption
 * melody legitimately takes -- and wherever the learner's pitch was thrown away by the
 * harmonic-lock filter, which leaves a gap rather than an invented join.
 */
private fun DrawScope.drawContours(
    layout: TextLayoutResult,
    marking: TurnMarking,
    colors: MarkingColors,
    density: Density,
) {
    val model = marking.modelContour()
    val learner = marking.learnerContour()
    if (model.isEmpty) return

    val bounds = marking.pitchBounds()
    val span = (bounds.endInclusive - bounds.start).takeIf { it > 0f } ?: return
    val stroke = Stroke(with(density) { 2.dp.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val topInset = with(density) { 3.dp.toPx() }
    val bottomInset = with(density) { 8.dp.toPx() }

    for (line in 0 until layout.lineCount) {
        val start = layout.getLineStart(line)
        val end = layout.getLineEnd(line, visibleEnd = true)
        if (end <= start) continue

        val top = layout.getLineTop(line) + topInset
        val bottom = layout.getLineBottom(line) - bottomInset
        fun y(hz: Float) = bottom - ((hz - bounds.start) / span).coerceIn(0f, 1f) * (bottom - top)
        fun x(offset: Int) =
            if (offset < end) layout.getBoundingBox(offset).left
            else layout.getBoundingBox(end - 1).right

        drawPath(pathAcross(start, end, ::x, ::y, model) { true }, colors.modelContour, style = stroke)
        if (!learner.isEmpty) {
            drawPath(
                pathAcross(start, end, ::x, ::y, learner) { marking.learnerPitchKnownAt(it) },
                colors.learnerContour,
                style = stroke,
            )
        }
    }
}

private inline fun pathAcross(
    start: Int,
    end: Int,
    x: (Int) -> Float,
    y: (Float) -> Float,
    contour: Contour,
    defined: (Int) -> Boolean,
): Path {
    val path = Path()
    var drawing = false
    for (offset in start..end) {
        val sample = offset.coerceAtMost(end - 1)
        if (!defined(sample)) {
            drawing = false
            continue
        }
        val point = Offset(x(offset), y(contour.at(offset.toFloat())))
        if (drawing) path.lineTo(point.x, point.y) else path.moveTo(point.x, point.y)
        drawing = true
    }
    return path
}
