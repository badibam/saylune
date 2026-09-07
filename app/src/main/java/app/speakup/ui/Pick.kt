package app.speakup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.speakup.ui.theme.Speakup

/**
 * One position of a setting: the dot that says whether it is the one in force, and its name.
 *
 * The same row serves a setting that is on or off and a setting that is one of several -- what
 * differs is how many of them are drawn together, not what one of them looks like. The dot is
 * the register's word for *here*, and it is what the stepped levers use.
 */
@Composable
fun Pick(on: Boolean, says: String, onPress: () -> Unit) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onPress),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(grid.cell),
    ) {
        Text(
            (if (on) Glyphs.DOT_FILLED else Glyphs.DOT_HOLLOW).toString(),
            style = type.text,
            color = if (on) palette.ink.srgb else palette.dim.srgb,
        )
        Text(says, style = type.text, color = if (on) palette.ink.srgb else palette.dim.srgb)
    }
}

/**
 * A setting that is one of several: its name, then one [Pick] per position.
 *
 * **The positions are always all drawn**, never folded into a menu that opens: a list of three
 * short words costs three lines and says what one can have, where a menu says only where one is
 * and hides the rest behind a gesture. The screens that do open a menu are the ones whose list
 * comes from a provider and is not known in advance.
 */
@Composable
fun Picks(
    name: String,
    /** The positions, in the order they are offered: what it is stored as, and what it is called. */
    options: List<Pair<String, String>>,
    chosen: String,
    modifier: Modifier = Modifier,
    onPick: (String) -> Unit,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    Column(modifier.fillMaxWidth()) {
        Text(
            name,
            modifier = Modifier.padding(bottom = grid.cell),
            style = type.text,
            color = palette.ink.srgb,
        )
        options.forEach { (value, says) ->
            Pick(on = value == chosen, says = says) { onPick(value) }
        }
    }
}
