package app.speakup.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import app.speakup.ui.theme.Speakup

/**
 * The root of everything: the four modes, and the doors that are not modes.
 *
 * **The app opens on a title screen presenting the four** -- Story, Challenges, Arcade, Free
 * (`ui.md`, settled 2026-09-06). Each is an **access layer**, a way of arriving at a
 * definition, and the same engine runs behind all of them. What each shows behind itself is
 * decided only for Free, which offers themes.
 *
 * ## The shape
 *
 * Four **full-width** tiles for the modes, then the doors that are not modes as **half-width**
 * tiles at the bottom, two to a line and room kept for four. The eight together take about the
 * whole height and never more: the tiles have their own size, and whatever the screen has
 * spare falls **between the two zones**, so both blocks stay where the thumb learnt them.
 *
 * A tile is framed, which is the rule the frame carries: one can say what one does with it.
 *
 * **A mode with nothing behind it is off and carries its reason**, in the tile itself. There
 * is no status line on this screen to carry it, and a mode that simply did nothing when
 * pressed would read as the app being broken.
 */
@Composable
fun TitleScreen(
    /** The four modes, in the order the title screen presents them. */
    modes: List<Tile>,
    /** What is not a mode: the app's own settings, and whatever else comes to live there. */
    doors: List<Tile>,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    BoxWithConstraints(modifier.padding(grid.cell)) {
        val rows = (maxHeight / grid.cell).toInt()
        // The bottom block is fixed: two lines of two, whether or not four doors exist. Room
        // kept rather than room taken later, so the block does not move down the day a third
        // door arrives.
        val bottom = DOOR_ROWS * DOOR_LINES + (DOOR_LINES - 1)
        val left = rows - bottom - (modes.size - 1) - LEAST_SLACK
        // The modes share what is left, so the eight tiles take about the whole height and
        // never more. There is no cap: a cap would leave the spare height in the middle of the
        // screen, and what is spare here is only what the division does not divide.
        val tall = (left / modes.size).coerceAtLeast(2)
        Column(Modifier.fillMaxSize()) {
            modes.forEachIndexed { index, mode ->
                if (index > 0) Spacer(Modifier.height(grid.cell))
                Tile(mode, Modifier.fillMaxWidth().height(grid.cell * tall))
            }
            // The spare height, and the only place it goes.
            Spacer(Modifier.weight(1f))
            (0 until DOOR_LINES).forEach { line ->
                if (line > 0) Spacer(Modifier.height(grid.cell))
                Row(
                    Modifier.fillMaxWidth().height(grid.cell * DOOR_ROWS),
                    horizontalArrangement = Arrangement.spacedBy(grid.cell),
                ) {
                    (0 until PER_LINE).forEach { column ->
                        val door = doors.getOrNull(line * PER_LINE + column)
                        // An empty slot is empty, not filled with a tile that does nothing:
                        // it is room kept, and room kept has nothing to say.
                        if (door == null) Spacer(Modifier.weight(1f))
                        else Tile(door, Modifier.weight(1f).fillMaxSize())
                    }
                }
            }
        }
    }
}

/** One thing the title screen leads to, or says it cannot lead to yet. */
data class Tile(
    @param:StringRes val name: Int,
    /** Why it is off, or null when it opens. */
    @param:StringRes val reason: Int? = null,
    val open: () -> Unit = {},
)

@Composable
private fun Tile(tile: Tile, modifier: Modifier = Modifier) {
    val palette = Speakup.palette
    val type = Speakup.type
    val open = tile.reason == null
    Framed(modifier.let { if (open) it.clickable(onClick = tile.open) else it }) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(tile.name),
                style = type.text,
                color = if (open) palette.ink.srgb else palette.dim.srgb,
                textAlign = TextAlign.Center,
            )
            tile.reason?.let {
                Text(
                    stringResource(it),
                    style = type.thin,
                    color = palette.dim.srgb,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** A door's tile: two rows of frame and room for two lines inside. */
private const val DOOR_ROWS = 4

/** Two lines of doors, room kept for four. */
private const val DOOR_LINES = 2
private const val PER_LINE = 2

/** What stays between the two zones even on the shortest screen. */
private const val LEAST_SLACK = 2
