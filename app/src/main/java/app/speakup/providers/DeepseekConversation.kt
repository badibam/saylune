package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Reply
import app.speakup.chain.Word
import app.speakup.debug.Trace
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * DeepSeek. It measured the chain's latency and that is all it has done -- the provider is
 * not chosen (`../../../../../../TODO.md`, chantier 2).
 *
 * The instruction is [ConversationPrompt]'s and the reading is [ReplyReader]'s: what a
 * provider carries is how the JSON is guaranteed -- here `response_format`, which DeepSeek
 * cannot answer outside of -- and never what is asked.
 */
class DeepseekConversation(
    private val store: SecretStore,
    private val model: String,
    /**
     * How much reasoning to spend, **always sent**.
     *
     * Leaving these fields out is not a neutral default: DeepSeek's doc says thinking is on
     * at `high` unless told otherwise, so a silent call pays the fullest reasoning it has.
     * That is what the turns of 2026-09-06 measured on this link, and it is the reason the
     * value is named on every call rather than only when the user has picked one.
     */
    private val effort: Effort,
) : Conversation {

    override suspend fun reply(history: List<Exchange>, heard: List<Word>, titled: String?): Reply =
        withContext(Dispatchers.IO) {
            val values = store.values().first()
            val key = values[Secret.DeepseekApiKey]
                ?: throw ChainFailure("no DeepSeek key has been entered")
            val base = (values[Secret.DeepseekEndpoint] ?: DEFAULT_BASE).trimEnd('/')

            ChatCompletions.ask(
                base = base,
                key = key,
                model = model,
                history = history,
                heard = heard,
                titled = titled,
                say = "conversation: asking deepseek/$model at ${effort.id}",
            ) {
                put("thinking", JSONObject().put(
                    "type", if (effort == Effort.None) "disabled" else "enabled",
                ))
                // Only alongside thinking that is on: with it off there is no effort to
                // grade, and naming one would ask for two different things at once.
                if (effort != Effort.None) put("reasoning_effort", effort.id)
            }
        }

    private companion object {
        /**
         * The host, with the user's override in front of it -- depending on a protocol
         * rather than on one hostname is what the wisdom asks of any service the app talks
         * to.
         */
        const val DEFAULT_BASE = "https://api.deepseek.com"
    }
}
