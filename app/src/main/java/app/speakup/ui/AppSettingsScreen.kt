package app.speakup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.speakup.R
import app.speakup.SPARE
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import app.speakup.providers.Renders
import app.speakup.ui.theme.Speakup
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * How the app itself is set: what one reads it in, and what it looks like.
 *
 * **It is not the keys screen, and the split is the visit.** Bringing a key and saying who does
 * which link is an installation, done three times in a life and never thought about again;
 * these are the settings one comes back to. The keys screen kept them because it was the only
 * screen there was.
 *
 * **Every position is written straight through, and there is no save.** A setting one can see
 * the effect of the instant it is pressed -- the whole screen redraws in the weight, the size,
 * the register, the language just chosen -- has nothing to confirm: the confirmation is the
 * screen. The one field that is typed rather than pressed writes as it is typed, for the same
 * reason the cache ceiling always did.
 *
 * **Blank is the default everywhere**, and for the two that follow the phone -- the register and
 * the language -- the default is what the phone says. That is why *system* is offered as a
 * position and stored as nothing: written down, it would be the same state held in two places,
 * and it would stop following the phone the day the phone changed.
 */
@Composable
fun AppSettingsScreen(
    store: SecretStore,
    stored: Map<Secret, String>,
    modifier: Modifier = Modifier,
) {
    val grid = Speakup.grid
    val palette = Speakup.palette
    val type = Speakup.type
    val scope = rememberCoroutineScope()
    fun write(secret: Secret, value: String) = scope.launch { store.write(secret, value) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = grid.cell),
        verticalArrangement = Arrangement.spacedBy(grid.cell * SECTIONS),
    ) {
        Picks(
            name = stringResource(R.string.setting_language),
            options = listOf(
                "" to stringResource(R.string.setting_system),
                // Named in themselves and never translated: someone who has landed in a
                // language they do not read has to be able to find their way out, and
                // "Français" says more to them than the word for French in Finnish.
                *LANGUAGES.map { tag ->
                    tag to Locale.forLanguageTag(tag).getDisplayLanguage(Locale.forLanguageTag(tag))
                        .replaceFirstChar { it.titlecase(Locale.forLanguageTag(tag)) }
                }.toTypedArray(),
            ),
            chosen = stored[Secret.Language].orEmpty(),
        ) { write(Secret.Language, it) }

        Picks(
            name = stringResource(R.string.setting_weight),
            options = listOf(
                "" to stringResource(R.string.weight_normal),
                THIN to stringResource(R.string.weight_thin),
            ),
            chosen = stored[Secret.TextWeight].orEmpty(),
        ) { write(Secret.TextWeight, it) }

        // The size is the grid's whole factor moved by a step, which is the only way this app
        // can enlarge: the system's text scale multiplies by a fraction, and a fraction of a
        // drawing pixel is what the register cannot have (`theme/Grid.kt`).
        Picks(
            name = stringResource(R.string.setting_size),
            options = listOf(
                SMALLER to stringResource(R.string.size_smaller),
                "" to stringResource(R.string.size_ordinary),
                BIGGER to stringResource(R.string.size_bigger),
            ),
            chosen = stored[Secret.TextScale].orEmpty(),
        ) { write(Secret.TextScale, it) }

        Picks(
            name = stringResource(R.string.setting_register),
            options = listOf(
                "" to stringResource(R.string.setting_system),
                NIGHT to stringResource(R.string.register_night),
                PALE to stringResource(R.string.register_pale),
            ),
            chosen = stored[Secret.Register].orEmpty(),
        ) { write(Secret.Register, it) }

        // Not a correction of the palette but a replacement of it: the plain registers keep
        // their colours for everyone, and whoever does not separate them changes palette
        // (`ui.md`).
        Picks(
            name = stringResource(R.string.setting_palette),
            options = listOf(
                "" to stringResource(R.string.palette_plain),
                SPARE to stringResource(R.string.palette_spare),
            ),
            chosen = stored[Secret.SparePalette].orEmpty(),
        ) { write(Secret.SparePalette, it) }

        Column(verticalArrangement = Arrangement.spacedBy(grid.cell)) {
            Text(
                stringResource(R.string.setting_cache_title),
                style = type.text,
                color = palette.ink.srgb,
            )
            var typed by remember(stored[Secret.RenderCacheCap]) {
                mutableStateOf(stored[Secret.RenderCacheCap].orEmpty())
            }
            Field(
                value = typed,
                // Digits only. A ceiling is a number, and a field that accepts letters and
                // then quietly falls back to the default is a field that lies about what it
                // holds.
                onChange = { entry ->
                    typed = entry.filter { it.isDigit() }
                    write(Secret.RenderCacheCap, typed)
                },
                placeholder = stringResource(R.string.setting_cache_default, Renders.DEFAULT_CAP_MB),
                digits = true,
            )
            Text(
                stringResource(R.string.setting_cache_cap_help, Renders.DEFAULT_CAP_MB),
                style = type.thin,
                color = palette.dim.srgb,
            )
        }
    }
}

/** What the store holds for the position that is not the default. */
const val THIN = "thin"
const val SMALLER = "smaller"
const val BIGGER = "bigger"
const val NIGHT = "night"
const val PALE = "pale"

/** The air between two settings: more than between two positions of one, so a family reads as one. */
private const val SECTIONS = 2
