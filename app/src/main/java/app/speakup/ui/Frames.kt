package app.speakup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import app.speakup.ui.theme.Grid
import app.speakup.ui.theme.Speakup
import kotlin.math.roundToInt

/**
 * A frame, written as text, around [content].
 *
 * **A frame says "object"** (`ui.md`): a menu, a dialogue, an activity's tile, the
 * selected thing. Never the whole screen, and never the thread nor the marked turn, which
 * need every column. The rule is checked by writing a screen: if a thing is framed, one must
 * be able to say what one does with it; otherwise it is a flat field.
 *
 * **Two tones by two exactly aligned layers of text**, both drawn under the content: the two
 * outer pixels of the border in the light tone, the two inner in the dark. The two sets are
 * complementary, so nothing depends on the order they are laid in.
 *
 * ## What decides its size
 *
 * The frame sits on whole cells in both directions -- that is what makes frames, panels and
 * margins compose with each other -- and the two directions are not measured the same way,
 * because the grid is not the same in the two.
 *
 * **Across, the cell is anchored**, so the content is inset by a whole cell each side and a
 * frame of *n* columns holds *n − 2* columns of text. The border is four pixels of the eleven,
 * so seven pixels of air are left inside it without anyone having to ask for them.
 *
 * **Down, nothing is anchored**: what has to be a whole number of cells is the frame's own
 * height, and the content is centred in what the two borders leave. The smallest frame is
 * therefore **two rows**, which is where this arithmetic pays: four pixels of border, the
 * font's fourteen-row box, four pixels of border -- exactly twenty-two, the two tiles. One
 * line of text in a frame costs two grid rows and not three.
 */
@Composable
fun Framed(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val style = Speakup.type.text
    val light = palette.frameLight.srgb
    val dark = palette.frameDark.srgb
    SubcomposeLayout(modifier) { constraints ->
        val tile = grid.painted(Grid.CELL).roundToInt()
        val border = grid.painted(BORDER).roundToInt()
        // A size the caller imposed is honoured, rounded down to whole cells: a frame told to
        // fill a row of two must not decide its own width off its label. Where nothing is
        // imposed, the content decides.
        val given = if (constraints.hasFixedWidth)
            (constraints.maxWidth / tile).coerceAtLeast(2) else null
        val gave = if (constraints.hasFixedHeight)
            (constraints.maxHeight / tile).coerceAtLeast(2) else null
        val room = given?.let { (it - 2) * tile }
            ?: (constraints.maxWidth - 2 * tile).coerceAtLeast(0)
        val inside = subcompose(Slot.Content) { Box(content = content) }.map {
            it.measure(
                Constraints(
                    minWidth = if (given != null) room else 0,
                    maxWidth = room,
                    maxHeight = when {
                        gave != null -> (gave * tile - 2 * border).coerceAtLeast(0)
                        constraints.hasBoundedHeight ->
                            (constraints.maxHeight - 2 * border).coerceAtLeast(0)
                        else -> Constraints.Infinity
                    },
                )
            )
        }
        val wide = inside.maxOfOrNull { it.width } ?: 0
        val tall = inside.maxOfOrNull { it.height } ?: 0
        val columns = given ?: (cells(wide, tile) + 2).coerceAtLeast(2)
        val rows = gave ?: cells(tall + 2 * border, tile).coerceAtLeast(2)
        val width = columns * tile
        val height = rows * tile

        // One `Text` per row per layer rather than one multi-line `Text`: the row pitch has to
        // be exactly the tile, and a paragraph's own leading distributes what it is given
        // rather than landing on a whole number of drawing pixels.
        val laid = subcompose(Slot.Frame) {
            (0 until rows).forEach { row ->
                FrameRow(line(columns, row, rows, dark = false), light, style)
                FrameRow(line(columns, row, rows, dark = true), dark, style)
            }
        }.map { it.measure(Constraints(maxWidth = width)) }

        layout(width, height) {
            laid.forEachIndexed { index, placeable ->
                // Placed by its baseline, which is the only anchor that says where the ink is:
                // a frame piece fills the eleven rows above the baseline, so the row's tile
                // ends exactly there.
                val bottom = (index / LAYERS + 1) * tile
                placeable.place(0, bottom - placeable[FirstBaseline])
            }
            inside.forEach {
                // Centred in what the borders leave, rounded down to a whole drawing pixel --
                // free in the vertical, as long as it is whole.
                val slack = (height - it.height) / 2 / grid.scale * grid.scale
                it.place(tile, slack)
            }
        }
    }
}

/** One row of the frame, in one of the two layers. */
@Composable
private fun FrameRow(line: String, colour: Color, style: TextStyle) {
    Text(text = line, color = colour, style = style, maxLines = 1, softWrap = false)
}

/** Row [row] of a frame [rows] tall and [columns] wide, in one of the two layers. */
private fun line(columns: Int, row: Int, rows: Int, dark: Boolean): String {
    fun piece(of: Glyphs.Piece) = Glyphs.frame(of, dark)
    val (start, middle, end) = when (row) {
        0 -> Triple(Glyphs.Piece.TopLeft, piece(Glyphs.Piece.Top), Glyphs.Piece.TopRight)
        rows - 1 -> Triple(
            Glyphs.Piece.BottomLeft, piece(Glyphs.Piece.Bottom), Glyphs.Piece.BottomRight,
        )
        // A middle row is hollow: the space is the ordinary blank, which advances one cell like
        // every other character, so what is under the frame shows through it.
        else -> Triple(Glyphs.Piece.Left, ' ', Glyphs.Piece.Right)
    }
    return piece(start) + middle.toString().repeat(columns - 2) + piece(end)
}

private enum class Slot { Content, Frame }

/** How many whole cells [span] pixels take. */
private fun cells(span: Int, tile: Int): Int = (span + tile - 1) / tile

/** The border's thickness, in drawing pixels: the bench's, and the font is cut to it. */
private const val BORDER = 4

/** Light and dark: a frame is written twice. */
private const val LAYERS = 2
