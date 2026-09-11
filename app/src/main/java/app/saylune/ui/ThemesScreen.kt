package app.saylune.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.saylune.R
import app.saylune.scene.SceneFile
import app.saylune.ui.theme.Saylune
import java.util.Locale

/**
 * The free door: **one tile per scene the app ships**, and the themeless one first.
 *
 * **A surface of fixed size, which is what separates it from a catalogue** (`ui.md`): the tiles
 * are the files delivered, so how many there are is decided by the app and never by use. One
 * keeps several conversations alive without having a list to administer.
 *
 * **The first is full width and the rest are half.** The themeless conversation is always shown
 * first and is not one theme among others: it is the one place where the learner brings the
 * situation himself, so it is given the width rather than a rank.
 *
 * **A tile carries a name, a face and a count.** The name is the scene's, declared in its file;
 * the face is the character the file flags as the main one, because one meets somebody rather
 * than launching a subject (`../../../../../../NOTES.md`); and the count is how many passages
 * the sitting behind it holds -- a count of rows, so nothing is stored for it. A scene nobody
 * has opened shows no count rather than a zero: what it says is *not started*, and a zero says
 * *started and empty*.
 *
 * **The tiles do not move, except that the started ones come forward**, the first staying
 * first. Their order is otherwise the order the files are read in.
 */
@Composable
fun ThemesScreen(
    /** Every scene the app ships behind this door, the free conversation included. */
    themes: List<SceneFile>,
    /** How many passages each scene's sitting holds, by id. Absent for unopened. */
    passages: Map<String, Int>,
    onOpen: (SceneFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Saylune.grid
    val first = themes.firstOrNull { it.id == FREE } ?: themes.firstOrNull() ?: return
    val rest = themes.filter { it !== first }
        // Started ones forward, and stable within each group, so nothing else moves.
        .sortedBy { if (it.id in passages) 0 else 1 }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = grid.cell),
        verticalArrangement = Arrangement.spacedBy(grid.cell),
    ) {
        Theme(first, passages[first.id], Modifier.fillMaxWidth().height(grid.cell * WIDE_ROWS)) {
            onOpen(first)
        }
        rest.chunked(PER_LINE).forEach { line ->
            Row(
                Modifier.fillMaxWidth().height(grid.cell * ROWS),
                horizontalArrangement = Arrangement.spacedBy(grid.cell),
            ) {
                line.forEach { theme ->
                    Theme(theme, passages[theme.id], Modifier.weight(1f).fillMaxSize()) {
                        onOpen(theme)
                    }
                }
                // A half-empty line keeps its half empty rather than stretching the one tile
                // it has: the tiles are a grid and a lone wide one would read as another kind.
                repeat(PER_LINE - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** One scene: what it is called, who one meets there, and how far one got. */
@Composable
private fun Theme(theme: SceneFile, passages: Int?, modifier: Modifier, onOpen: () -> Unit) {
    val grid = Saylune.grid
    val palette = Saylune.palette
    val type = Saylune.type
    val language = Locale.getDefault().language
    Framed(modifier.clickable(onClick = onOpen)) {
        Column(
            Modifier.align(Alignment.Center).padding(horizontal = grid.cell),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(grid.cell),
        ) {
            Text(
                theme.title.inLanguage(language),
                style = type.text,
                color = palette.ink.srgb,
                textAlign = TextAlign.Center,
                maxLines = TITLE_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            theme.face?.let { face ->
                Text(
                    face.short.inLanguage(language),
                    style = type.thin,
                    color = palette.dim.srgb,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            passages?.let {
                Text(
                    pluralStringResource(R.plurals.theme_passages, it, it),
                    style = type.thin,
                    color = palette.dim.srgb,
                    maxLines = 1,
                )
            }
        }
    }
}

/** The scene with no theme, which is delivered like any other and shown first. */
private const val FREE = "free-conversation"

/** Two to a line, which is what half a portrait width is. */
private const val PER_LINE = 2

/** A tile: a title of up to two lines, a name, a count, and the air between them. */
private const val ROWS = 8
private const val WIDE_ROWS = 6
private const val TITLE_LINES = 2
