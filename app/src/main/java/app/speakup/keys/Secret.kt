package app.speakup.keys

import androidx.annotation.StringRes
import app.speakup.R

/**
 * One thing the user types in so a remote link can work.
 *
 * Not all of these are secrets -- an Azure region and, later, a provider's endpoint are
 * configuration, and the endpoint is configurable on purpose: depending on a protocol rather
 * than on one host is the difference the wisdom asks for. They share the store anyway,
 * because two mechanisms for "what the user typed" would be two places to get wrong for no
 * gain, and encrypting a region costs nothing.
 *
 * [id] is the storage key and never changes: renaming an entry would silently lose whatever
 * the user had already entered under the old name.
 */
enum class Secret(
    val id: String,
    @StringRes val label: Int,
    val required: Boolean = true,
    /** False for what is configuration rather than a credential, and safe to read back. */
    val masked: Boolean = true,
) {
    AzureSpeechKey("azure.speech.key", R.string.secret_azure_key),
    AzureSpeechRegion("azure.speech.region", R.string.secret_azure_region, masked = false),
    DeepseekApiKey("deepseek.api.key", R.string.secret_deepseek_key),
    ElevenlabsKey("elevenlabs.key", R.string.secret_elevenlabs_key),
    ;

    companion object {
        /** Whether every link of the chain has what it needs to be called at all. */
        fun allPresent(values: Map<Secret, String>): Boolean =
            entries.filter { it.required }.all { !values[it].isNullOrBlank() }
    }
}
