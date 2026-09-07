package app.speakup.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import java.util.Locale

/**
 * The language the interface is read in, when the learner has said which.
 *
 * **The system's is the default and is stored as nothing** (`keys/Secret.kt`): a phone set to
 * French opens a French app with no preference written anywhere, and *system* as a stored value
 * would be the same state said twice.
 *
 * **It overrides the resources rather than the activity.** The platform's own per-app language
 * lands in `LocaleManager`, which is API 33 and this app runs from 26, and the compatibility
 * route through `AppCompatDelegate` costs the whole of `appcompat` for one preference -- a
 * dependency the `android` facet asks not to take without a need. Handing the composition the
 * resources of a context configured for the language does the same work: every `stringResource`
 * under it reads that language, and changing the preference redraws rather than restarts.
 *
 * **What is deliberately left alone is `LocalContext`.** Providing the configured context there
 * works for strings and breaks everything that looks for the activity through it: the
 * permission launcher of the conversation screen resolves its registry by walking the context
 * up to the activity, and a configuration context is not one -- it threw on the way into a
 * conversation, which is the only screen that asks for a permission.
 *
 * The default locale is moved with it. Nothing in a resource file reads it, but the definitions
 * do -- their titles are a table of language to text, read with `Locale.getDefault()`, and a
 * screen in French naming a theme in English would be the same bug the other way round.
 */
@Composable
fun InLanguage(tag: String, content: @Composable () -> Unit) {
    val context = LocalContext.current
    // What the phone was set to, taken once: it is what *system* means, and the app cannot ask
    // for it again after it has moved the default itself.
    val system = remember { Locale.getDefault() }
    val locale = remember(tag, system) { if (tag.isEmpty()) system else Locale.forLanguageTag(tag) }
    SideEffect { Locale.setDefault(locale) }
    val configuration = remember(context, locale) {
        Configuration(context.resources.configuration).apply { setLocale(locale) }
    }
    val localised = remember(context, configuration) {
        context.createConfigurationContext(configuration)
    }
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalResources provides localised.resources,
        content = content,
    )
}

/** The languages the app is written in. Their tags are what the store holds. */
val LANGUAGES = listOf("fr", "en")
