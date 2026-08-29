package app.speakup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.speakup.R
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Where the user brings their own keys.
 *
 * Bare on purpose. What `docs/reference.md` asks for beyond this -- a guided screen and a
 * "test the key" button that validates on the spot -- is the real cost of BYOK and it is
 * owed, because without immediate validation every later failure gets blamed on the app.
 * It is finish, and finish waits (`../../../../../../TODO.md`, chantier 0).
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

    // Seeded from the store's own first emission, and never again: re-seeding on every
    // emission would overwrite what the user is in the middle of typing.
    //
    // From `first()` and not from `stored`, which starts at the placeholder empty map the
    // collection is given before DataStore has answered. Seeding off that filled every
    // field with "" and then declined to seed again, so the screen showed blanks over real
    // keys -- and saving wrote the blanks back, which removes them.
    LaunchedEffect(Unit) {
        val held = store.values().first()
        Secret.entries.forEach { secret ->
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

        Secret.entries.forEach { secret ->
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

        if (!Secret.allPresent(stored)) {
            Text(
                stringResource(R.string.settings_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
