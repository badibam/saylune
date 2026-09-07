package app.saylune.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.saylune.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * What the app sent to the language model for one passage, whole.
 *
 * **Deliberately outside the register**, and the only screen of the app that is. Every other
 * one is drawn in the eleven-pixel font on the grid, which is the app; this is an instrument
 * for reading a body of eight thousand characters at a desk, and the register's own rules --
 * a cell of eleven pixels, a line that falls on the grid -- would make that unreadable. So it
 * takes the platform's monospace at the platform's own size, the full width, and nothing
 * else. Same reason [DebugPanel] is Material and not the register.
 *
 * **What is shown is what went out.** Nothing here rebuilds a prompt from the sitting: the
 * body is the one the provider handed to the network, kept by [app.saylune.debug.Trace] under
 * the passage it was for. So a passage that this run of the app did not send says so plainly
 * rather than showing something that merely resembles what was sent.
 *
 * The body is JSON, and read as JSON where it can be: the messages come out one under the
 * other, each under its role, because a prompt is a conversation and reading it as one line of
 * escaped text is what makes it unreadable. What a provider wraps them in is its own -- the
 * chat shape and Replicate's are not the same object -- so anything the walk does not
 * recognise falls back to the body pretty-printed, and the raw text is one tap away besides.
 */
@Composable
fun PromptScreen(
    /** Which passage of the sitting this was, counting from one, for the title alone. */
    passage: Int,
    /** What went out, or null where this run of the app never sent it. */
    body: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var raw by rememberSaveable { mutableStateOf(false) }
    val parts = remember(body) { body?.let { parts(it) } }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.debug_prompt_title, passage),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (parts != null) {
                        TextButton(onClick = { raw = !raw }) {
                            Text(stringResource(R.string.debug_prompt_raw))
                        }
                    }
                    TextButton(onClick = onClose) {
                        Text(stringResource(R.string.debug_prompt_close))
                    }
                }
            }
            HorizontalDivider()

            if (body == null) {
                Text(
                    stringResource(R.string.debug_prompt_none),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
                return@Column
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    if (parts == null) stringResource(R.string.debug_prompt_size, body.length)
                    else stringResource(R.string.debug_prompt_chars, body.length, parts.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (raw || parts == null) {
                    // Opened out rather than as it went over the wire: one line of eight
                    // thousand characters is not something anyone reads.
                    Body(pretty(body))
                } else {
                    parts.forEach { part ->
                        Text(
                            part.role,
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Body(part.text)
                    }
                }
            }
        }
    }
}

/** The whole of a body, selectable so a turn can leave the phone by the clipboard. */
@Composable
private fun Body(text: String) {
    SelectionContainer {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** One message of the prompt: who it is from, and what it says. */
private data class Part(val role: String, val text: String)

/**
 * The messages of a body, or null where its shape is not one this knows.
 *
 * Two shapes, because two routes send two objects: `messages` at the top for the chat call,
 * and `input.system_prompt` plus `input.messages` for Replicate. A system message is a message
 * like the others here -- what the reader wants is the instruction in the order it was sent.
 */
private fun parts(body: String): List<Part>? = runCatching {
    val json = JSONObject(body)
    val inside = json.optJSONObject("input") ?: json
    val messages = inside.optJSONArray("messages") ?: return@runCatching null
    val parts = mutableListOf<Part>()
    inside.optString("system_prompt").takeIf { it.isNotBlank() }?.let {
        parts += Part("system", it)
    }
    for (at in 0 until messages.length()) {
        val message = messages.optJSONObject(at) ?: continue
        parts += Part(
            message.optString("role", "?"),
            // An assistant turn is itself a JSON object, replayed as the model emitted it.
            // Opened out, it reads as what it is; left as one line it reads as noise.
            pretty(message.optString("content")),
        )
    }
    parts.takeIf { it.isNotEmpty() }
}.getOrNull()

/** A JSON string opened out, or the string as it stands when it is not JSON. */
private fun pretty(text: String): String = runCatching {
    when {
        text.trimStart().startsWith("{") -> JSONObject(text).toString(2)
        text.trimStart().startsWith("[") -> JSONArray(text).toString(2)
        else -> text
    }
}.getOrDefault(text)
