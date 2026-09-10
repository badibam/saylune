package app.saylune.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import app.saylune.R
import app.saylune.analysis.Weights
import app.saylune.keys.Secret
import app.saylune.keys.SecretStore
import app.saylune.providers.Provider
import app.saylune.providers.LatencyTest
import app.saylune.providers.Task
import app.saylune.providers.VoiceOption
import app.saylune.providers.effortFor
import app.saylune.providers.modelFor
import app.saylune.chain.Voice
import app.saylune.providers.ChosenSynthesis
import app.saylune.capture.Playback
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Where the user brings their own keys and says who does what.
 *
 * **What the app looks like is not here any more**: the weight, the size, the register, the
 * palette, the language and the voice cache have a screen of their own (`AppSettingsScreen`),
 * because this one is an installation and those are settings one comes back to.
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
        WeightsSection()

        HorizontalDivider()
        HorizontalDivider()
        LatencySection(store)

        SoundCredits()
    }
}

/**
 * The analysis model: whether it is here, and the two ways of getting it.
 *
 * **The section is here whether or not the model is.** An option that is off carries its
 * reason, which is what the absent state does; and a model that is present needs somewhere
 * to be removed from, 359 MB being the largest thing the app puts on the phone. A section
 * that vanished once installed would leave that nowhere.
 *
 * It reads the flows the download writes rather than owning the work, so leaving the screen
 * does not stop it and coming back finds it where it got to ([Weights.progress]).
 */
@Composable
private fun WeightsSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<Weights.State?>(null) }
    var brought by remember { mutableStateOf<String?>(null) }
    val progress by Weights.progress.collectAsState()
    val failure by Weights.failure.collectAsState()
    val running = progress != null

    // Keyed on whether a download runs rather than on how far it got: the counter moves
    // every few kilobytes, and re-reading the files that often would hash 359 MB on a loop.
    // What this is for is the two moments the disk changes -- opening, and a download ending.
    LaunchedEffect(running) { if (!running) state = Weights.state(context) }

    // The system picker, which is what the android wisdom asks for over a broad permission:
    // the user points at one file and the app is given that one, with nothing to declare.
    val bring = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            brought = Weights.adopt(context, uri).fold(
                onSuccess = { context.getString(R.string.weights_brought, it.name) },
                onFailure = { it.message },
            )
            state = Weights.state(context)
        }
    }

    Text(stringResource(R.string.weights_title), style = MaterialTheme.typography.titleSmall)

    when {
        running -> Text(
            stringResource(R.string.weights_getting, size(progress ?: 0L), size(Weights.total)),
            style = MaterialTheme.typography.bodySmall,
        )

        state is Weights.State.Ready -> {
            Text(
                stringResource(R.string.weights_ready, size(Weights.total)),
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(onClick = {
                scope.launch { Weights.erase(context); state = Weights.state(context) }
            }) { Text(stringResource(R.string.weights_erase)) }
        }

        else -> {
            val wrong = state as? Weights.State.Wrong
            Text(
                if (wrong != null) stringResource(R.string.weights_wrong, wrong.piece)
                else stringResource(R.string.weights_absent),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                stringResource(R.string.weights_cost, size(Weights.total)),
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { Weights.start(context) }) {
                    Text(stringResource(R.string.weights_get))
                }
                // Any type: the picker filters on what the provider declares, and a phone
                // has no idea what an .onnx is. What it really is gets settled by its
                // digest anyway, which is the only check that means anything here.
                OutlinedButton(onClick = { bring.launch(arrayOf("*/*")) }) {
                    Text(stringResource(R.string.weights_bring))
                }
            }
        }
    }

    failure?.let {
        Text(
            stringResource(R.string.weights_failed, it),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    brought?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
}

/** Bytes as the offer says them: whole megabytes, decimal, like every download ever named. */
private fun size(bytes: Long): String = "${(bytes + 500_000) / 1_000_000} MB"

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

        // The reasoning level of a language link. It is a setting and not a silence:
        // DeepSeek runs thinking at `high` unless told otherwise, so the app names a level on
        // every call and this is where that level is chosen. **It is written under the link
        // this row is for**: two links reason, and one entry for both would have the judge's
        // menu set what the character spends.
        val efforts = chosen?.efforts.orEmpty()
        val spending = task.effort
        if (efforts.isNotEmpty() && spending != null) {
            Picker(
                label = stringResource(R.string.setting_effort),
                options = efforts.map { it.id to stringResource(it.label) },
                selected = chosen?.let { effortFor(it, task, stored) }?.id,
                onPick = { id -> scope.launch { store.write(spending, id) } },
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
