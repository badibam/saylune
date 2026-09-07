package app.speakup.ui

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
import app.speakup.R
import app.speakup.activity.Definition
import app.speakup.ui.theme.Speakup
import java.util.Locale

/**
 * The free door: **one tile per theme the app ships**, and the themeless one first.
 *
 * **A surface of fixed size, which is what separates it from a catalogue** (`ui.md`): the
 * tiles are the definitions delivered, so how many there are is decided by the app and never
 * by use. One keeps several conversations alive without having a list to administer, and the
 * list of sittings this replaces -- one row per sitting, growing forever -- is gone with the
 * proof of concept it came from.
 *
 * **The first is full width and the rest are half.** The themeless conversation is always
 * shown first and is not one theme among others: it is the one place where the learner brings
 * the situation himself, so it is given the width rather than a rank.
 *
 * **A tile carries a name, a face and a count.** The name is the theme, declared in its file;
 * the face is the character the file flags as the main one, because one meets somebody rather
 * than launching a subject (`../../../../../../NOTES.md`); and the count is how many passages
 * the sitting behind it holds -- a count of rows, so nothing is stored for it. A theme nobody
 * has opened shows no count rather than a zero: what it says is *not started*, and a zero says
 * *started and empty*.
 *
 * **The tiles do not move, except that the started ones come forward**, the first staying
 * first. Their order is otherwise the order the files are read in.
 */
@Composable
fun ThemesScreen(
    /** Every definition the app ships, the free conversation included. */
    themes: List<Definition>,
    /** How many passages each theme's sitting holds, by definition id. Absent for unopened. */
    passages: Map<String, Int>,
    onOpen: (Definition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
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

/** One theme: what it is called, who one meets there, and how far one got. */
@Composable
private fun Theme(theme: Definition, passages: Int?, modifier: Modifier, onOpen: () -> Unit) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
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

/** The definition with no theme, which is delivered like any other and shown first. */
private const val FREE = "free-conversation"

/** Two to a line, which is what half a portrait width is. */
private const val PER_LINE = 2

/** A tile: a title of up to two lines, a name, a count, and the air between them. */
private const val ROWS = 8
private const val WIDE_ROWS = 6
private const val TITLE_LINES = 2
