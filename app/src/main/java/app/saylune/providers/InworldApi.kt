package app.saylune.providers

import app.saylune.chain.ChainFailure
import app.saylune.keys.Secret
import app.saylune.keys.SecretStore
import kotlinx.coroutines.flow.first

/**
 * What the three Inworld links share: where it lives, and the key that opens it.
 *
 * It is the second provider after Replicate to do all three tasks, and the only one whose
 * three are its own rather than other people's hosted side by side. One key at the BYOK wall
 * instead of three is the whole reason it is here.
 *
 * **Two authentication schemes, and they are theirs and not a mistake.** The Router answers
 * OpenAI's `/chat/completions` and takes `Authorization: Bearer`, which is what
 * [ChatCompletions] sends; their own speech APIs take `Authorization: Basic` with the key
 * used raw, no colon and no second field. Nothing here can make those one.
 */
internal object InworldApi {

    /**
     * The host, with the user's override in front of it -- depending on a protocol rather
     * than on one hostname is what the wisdom asks of any service the app talks to.
     */
    const val DEFAULT_BASE = "https://api.inworld.ai"

    suspend fun base(store: SecretStore): String = base(store.values().first())

    fun base(values: Map<Secret, String>): String =
        (values[Secret.InworldEndpoint]?.ifBlank { null } ?: DEFAULT_BASE).trimEnd('/')

    suspend fun key(store: SecretStore): String = key(store.values().first())

    fun key(values: Map<Secret, String>): String =
        values[Secret.InworldApiKey]?.ifBlank { null }
            ?: throw ChainFailure("no Inworld key has been entered")

    /** What their speech APIs want, as against the Router's bearer token. */
    fun basic(key: String): Map<String, String> = mapOf("Authorization" to "Basic $key")
}
