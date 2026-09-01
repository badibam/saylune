package app.speakup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.speakup.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import app.speakup.providers.Renders
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import app.speakup.providers.Provider
import app.speakup.providers.LatencyTest
import app.speakup.providers.Task
import app.speakup.providers.VoiceOption
import app.speakup.providers.modelFor
import app.speakup.chain.Voice
import app.speakup.providers.ChosenSynthesis
import app.speakup.capture.Playback
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Where the user brings their own keys and says who does what.
 *
 * Two sections, and the order matters: a provider is only offered for a task once its key is
 * there, so the keys come first and the menus below them fill up as they are filled in.
 *
 * The catalogue -- the voices a synthesis model offers -- is fetched **every time the screen
 * opens, with no cache**. Settings change rarely, and a cached list dies in silence; the
 * doc's own measured lesson is that what a provider advertises is not what a particular key
 * unlocks. The fetch is also the probe: a list that comes back is a key that works, and a
 * key that fails says so here rather than in the middle of a conversation.
 *
 * It does not block. The rest of the screen is usable while a list is loading, and only the
 * selector that is waiting says it is waiting.
 */
/**
 * How much of the voice cache to keep, written as it is typed.
 *
 * Written straight through rather than under the Save button, the way the selectors are: the
 * button belongs to the fields that are typed together and saved together, and a lone number
 * that looked saved and was not would be found out a fortnight later, by a cache that had
 * never been pruned.
 */
@Composable
private fun CacheCap(stored: String, onWrite: (String) -> Unit) {
    var typed by remember(stored) { mutableStateOf(stored) }
    Text(stringResource(R.string.setting_cache_title), style = MaterialTheme.typography.titleSmall)
    OutlinedTextField(
        value = typed,
        onValueChange = { entry ->
            // Digits only. A ceiling is a number, and a field that accepts letters and then
            // quietly falls back to the default is a field that lies about what it holds.
            typed = entry.filter { it.isDigit() }
            onWrite(typed)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text(stringResource(R.string.setting_cache_cap)) },
        supportingText = {
            Text(stringResource(R.string.setting_cache_cap_help, Renders.DEFAULT_CAP_MB))
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun SettingsScreen(store: SecretStore, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val stored by store.values().collectAsState(initial = emptyMap())
    val edits = remember { mutableStateMapOf<Secret, String>() }
    // Saving has to say so. The only other sign is a warning line going away, which is
    // no sign at all to someone who never saw it.
    var saved by remember { mutableStateOf(false) }
    var seeded by remember { mutableStateOf(false) }

    // Only the fields this form owns. The rest of the store -- who does which link, with
    // which model and voice -- belongs to the selectors below, which write it as it is
    // picked. Seeding those here as empty and writing them back on Save erased every choice
    // the user had just made, and did it under a button that says "Saved".
    val typed = remember { Provider.entries.flatMap { it.needs } }
    // Seeded from the store's own first emission, and never again: re-seeding on every
    // emission would overwrite what the user is in the middle of typing.
    //
    // From `first()` and not from `stored`, which starts at the placeholder empty map the
    // collection is given before DataStore has answered. Seeding off that filled every
    // field with "" and then declined to seed again, so the screen showed blanks over real
    // keys -- and saving wrote the blanks back, which removes them.
    LaunchedEffect(Unit) {
        val held = store.values().first()
        typed.forEach { secret ->
            if (secret !in edits) edits[secret] = held[secret].orEmpty()
        }
        seeded = true
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.settings_lead), style = MaterialTheme.typography.bodyMedium)

        CacheCap(stored[Secret.RenderCacheCap].orEmpty()) { typed ->
            scope.launch { store.write(Secret.RenderCacheCap, typed) }
        }

        Provider.entries.forEach { provider ->
            Text(provider.label, style = MaterialTheme.typography.titleSmall)
            provider.needs.forEach { secret ->
                OutlinedTextField(
                    value = edits[secret].orEmpty(),
                    onValueChange = { edits[secret] = it; saved = false },
                    label = { Text(stringResource(secret.label)) },
                    singleLine = true,
                    visualTransformation =
                        if (secret.masked) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Button(
            onClick = {
                scope.launch {
                    // Only what actually changed. A blank write removes the entry, so
                    // saving after touching one field would otherwise wipe every other --
                    // and a key is not something the user can get back by retyping what
                    // they no longer have.
                    edits.forEach { (secret, value) ->
                        if (value != stored[secret].orEmpty()) store.write(secret, value)
                    }
                    saved = true
                }
            },
            // The android wisdom's rule, and this screen is exactly what it is for: an
            // action whose data is still loading is disabled, not merely slow. Left
            // clickable it would save the empty form over the real one and say "Saved".
            enabled = seeded,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_save))
        }

        if (saved) {
            Text(
                stringResource(R.string.settings_saved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        HorizontalDivider()

        Text(stringResource(R.string.tasks_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.tasks_lead), style = MaterialTheme.typography.bodyMedium)

        Task.entries.forEach { task ->
            TaskSection(task, store, stored)
        }

        HorizontalDivider()
        HorizontalDivider()
        LatencySection(store)

        SoundCredits()
    }
}

/**
 * The stopwatch: every model whose key is filled, on the same two sentences.
 *
 * Beside the settings because it is what one wants while choosing, and its results are what
 * the choice should rest on. It is deliberately not automatic: a sweep spends a call per
 * model per sentence per repeat, at the user's expense, so it says what it will cost and
 * waits to be told.
 */
@Composable
private fun LatencySection(store: SecretStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var repeats by rememberSaveable { mutableStateOf(3) }
    var calls by remember { mutableStateOf<Int?>(null) }
    var asking by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<LatencyTest.Trial>>(emptyList()) }
    // Offered back rather than asked afresh: the same place typed three ways groups three
    // ways, and the run one repeats is the run one already named.
    var note by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { if (note == null) note = LatencyTest.lastNote(context) }

    Text(stringResource(R.string.latency_title), style = MaterialTheme.typography.titleSmall)
    Text(stringResource(R.string.latency_lead), style = MaterialTheme.typography.bodySmall)

    OutlinedTextField(
        value = note.orEmpty(),
        onValueChange = { note = it },
        enabled = progress == null,
        singleLine = true,
        label = { Text(stringResource(R.string.latency_note)) },
        supportingText = { Text(stringResource(R.string.latency_note_help)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.latency_repeats), style = MaterialTheme.typography.bodySmall)
        listOf(1, 3, 5).forEach { n ->
            TextButton(onClick = { repeats = n }, enabled = progress == null) {
                Text(
                    n.toString(),
                    fontWeight = if (n == repeats) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }

    Button(
        // A sweep costs money and minutes, and an unnamed one cannot be told apart from
        // the next: naming is the cheapest part of it, so it is required rather than
        // hoped for.
        enabled = progress == null && !note.isNullOrBlank(),
        onClick = {
            scope.launch {
                calls = LatencyTest.planned(store, repeats)
                asking = true
            }
        },
    ) { Text(stringResource(R.string.latency_run)) }

    progress?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(stringResource(R.string.latency_confirm_title)) },
            text = { Text(stringResource(R.string.latency_confirm_body, calls ?: 0)) },
            confirmButton = {
                TextButton(onClick = {
                    asking = false
                    scope.launch {
                        progress = ""
                        results = LatencyTest.run(
                            context, store, repeats, note.orEmpty().trim(),
                        ) { progress = it }
                        progress = null
                    }
                }) { Text(stringResource(R.string.latency_go)) }
            },
            dismissButton = {
                TextButton(onClick = { asking = false }) {
                    Text(stringResource(R.string.latency_cancel))
                }
            },
        )
    }

    // The median rather than the mean: the defect this exists to find is the occasional
    // very slow call, and a mean lets one of those hide a link that is usually fine.
    results.groupBy { it.label to it.size }.toSortedMap(compareBy({ it.first }, { it.second }))
        .forEach { (key, trials) ->
            val ok = trials.mapNotNull { it.ms }.sorted()
            val line = if (ok.isEmpty()) trials.firstNotNullOfOrNull { it.why }.orEmpty()
            else "${ok[ok.size / 2] / 1000f} s" +
                (if (ok.size > 1) "  [${ok.first() / 1000f}–${ok.last() / 1000f}]" else "") +
                (if (ok.size < trials.size) "  ${trials.size - ok.size} en échec" else "")
            Text(
                "${key.first} · ${key.second} — $line",
                style = MaterialTheme.typography.bodySmall,
            )
        }
}

/**
 * One link: who does it, with which model, and in which voice.
 *
 * A cascade, and it unfolds only as far as the choice above it allows -- there is no model
 * menu until a provider is chosen, and no voice list until a model is. Anything absent says
 * why it is absent: an option the user cannot see and cannot explain reads as a bug in the
 * app rather than as a key they have not entered.
 */
@Composable
private fun TaskSection(task: Task, store: SecretStore, stored: Map<Secret, String>) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sample = stringResource(R.string.setting_sample)
    val offered = task.offered(stored)
    val chosen = task.chosen(stored)
    val model = chosen?.let { modelFor(task, it, stored) }

    var voices by remember { mutableStateOf<List<VoiceOption>>(emptyList()) }
    var previewing by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var refused by remember { mutableStateOf<String?>(null) }

    // Re-asked whenever the provider or the model changes, and once more every time the
    // screen is opened -- keyed on nothing that survives it, which is what "no cache" means
    // here. A provider with no voices to list is not asked.
    LaunchedEffect(task, chosen, model) {
        voices = emptyList()
        refused = null
        if (task.voice == null || chosen == null || model == null) return@LaunchedEffect
        loading = true
        runCatching { chosen.voices(store, model) }
            .onSuccess { voices = it }
            .onFailure { refused = it.message }
        loading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(task.label), style = MaterialTheme.typography.titleSmall)

        if (offered.isEmpty()) {
            Text(
                stringResource(R.string.task_no_provider),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            return@Column
        }

        Picker(
            label = stringResource(R.string.setting_provider),
            options = offered.map { it.id to it.label },
            selected = chosen?.id,
            onPick = { id -> scope.launch { store.write(task.provider, id) } },
        )

        val models = chosen?.models?.get(task).orEmpty()
        if (models.isNotEmpty()) {
            Picker(
                label = stringResource(R.string.setting_model),
                options = models.map { it to it },
                selected = model,
                onPick = { id -> scope.launch { store.write(task.model, id) } },
            )
        }

        if (task.voice != null && chosen != null) {
            when {
                loading -> Text(
                    stringResource(R.string.setting_loading),
                    style = MaterialTheme.typography.bodySmall,
                )
                // The reason travels with the absence, as the doc requires of any option
                // that is off: this is also the only place a bad key is caught before a
                // conversation is under way.
                refused != null -> Text(
                    stringResource(R.string.setting_refused, refused.orEmpty()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                voices.isEmpty() -> Text(
                    stringResource(R.string.setting_no_voices),
                    style = MaterialTheme.typography.bodySmall,
                )
                else -> Picker(
                    label = stringResource(R.string.setting_voice),
                    options = voices.map { it.id to it.label },
                    selected = stored[task.voice],
                    onPick = { id -> scope.launch { store.write(task.voice, id) } },
                    // Chatterbox publishes nothing but a first name, so hearing the voice is
                    // the only way to know what it sounds like -- and it is the criterion
                    // that matters anyway: the ear judges a voice as a model to imitate.
                    onHear = { id ->
                        scope.launch {
                            previewing = id
                            runCatching {
                                Playback.play(
                                    ChosenSynthesis(context, store).speak(
                                        sample,
                                        Voice(provider = chosen!!.id, id = id),
                                    )
                                )
                            }.onFailure { refused = it.message }
                            previewing = null
                        }
                    },
                    busy = previewing,
                )
            }
        }
    }
}

/**
 * A menu of [options], each an id and what to show for it.
 *
 * [onHear] adds a button beside every entry that plays the option rather than picking it --
 * the menu stays open, because trying three voices in a row is the point. [busy] is the id
 * currently being fetched and said, so that one entry says it is working and the others do
 * not.
 */
@Composable
private fun Picker(
    label: String,
    options: List<Pair<String, String>>,
    selected: String?,
    onPick: (String) -> Unit,
    onHear: ((String) -> Unit)? = null,
    busy: String? = null,
) {
    var open by remember { mutableStateOf(false) }
    val shown = options.firstOrNull { it.first == selected }?.second

    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Text(
            if (shown == null) stringResource(R.string.setting_unset, label)
            else stringResource(R.string.setting_chosen, label, shown)
        )
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        options.forEach { (id, text) ->
            DropdownMenuItem(
                text = { Text(text) },
                onClick = { open = false; onPick(id) },
                trailingIcon = onHear?.let {
                    {
                        // A word and not a glyph: an icon would cost a dependency to
                        // rebuild offline, for one triangle.
                        TextButton(
                            onClick = { it(id) },
                            // One preview at a time. Two voices over each other tell
                            // nothing about either.
                            enabled = busy == null,
                        ) {
                            Text(
                                stringResource(
                                    if (busy == id) R.string.setting_hearing
                                    else R.string.setting_hear
                                )
                            )
                        }
                    }
                },
            )
        }
    }
}
