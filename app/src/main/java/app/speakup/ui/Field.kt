package app.speakup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import app.speakup.ui.theme.Speakup

/**
 * Something typed, in the register: a frame with the text inside it.
 *
 * **The frame is the field**, as it is the button and the panel -- one word of the register for
 * *this is an object*, and here what one does with it is type. Material's own field brought a
 * floating label, an underline and a focus colour, none of which the register has a way of
 * saying, and it dressed the one place on a screen where the learner writes.
 *
 * The placeholder is the register's dim ink and disappears on the first character, which is what
 * says a blank field is not an empty one: everywhere in this app a blank means the default, and
 * the placeholder is where that default is named.
 */
@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    digits: Boolean = false,
    rows: Int = FIELD_ROWS,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    Framed(modifier.fillMaxWidth().height(grid.cell * rows)) {
        Box(Modifier.align(Alignment.CenterStart).padding(horizontal = grid.cell)) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, style = type.thin, color = palette.dim.srgb, maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = rows <= FIELD_ROWS,
                textStyle = type.text.copy(color = palette.ink.srgb),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(palette.ink.srgb),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (digits) KeyboardType.Number else KeyboardType.Text,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** One line of text inside a frame: the line, and the border each side. */
const val FIELD_ROWS = 3
