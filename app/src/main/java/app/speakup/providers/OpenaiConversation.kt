package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Present
import app.speakup.chain.Reply
import app.speakup.chain.Word
import app.speakup.chain.Scene
import app.speakup.keys.Secret
import app.speakup.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * OpenAI, reached directly.
 *
 * It exists to close a hole the latency measurements named: **no language model had ever been
 * reached by both routes**, so nothing said what `gpt-5-nano` costs direct against the same
 * model through Replicate. The synthesis was measured paying eight to eleven seconds for the
 * detour; that the language link pays the same is a deduction and not a figure, and it stays
 * one until these two models answer from here.
 *
 * The instruction is [ConversationPrompt]'s and the reading is [ReplyReader]'s. What this
 * carries is the endpoint, the key, and `reasoning_effort` -- which on this provider is one
 * field with no companion, where DeepSeek needs `thinking` beside it.
 */
class OpenaiConversation(
    private val store: SecretStore,
    private val model: String,
    /**
     * How much reasoning to spend, or null to leave the field out.
     *
     * Null here genuinely means *not sent*, and unlike DeepSeek that is not a hidden maximum:
     * each model applies its own documented default. It is what a level this catalogue cannot
     * vouch for resolves to, rather than sending a value the model might refuse.
     */
    private val effort: Effort?,
) : Conversation {

    override suspend fun reply(
        history: List<Exchange>, heard: List<Word>, scene: Scene, present: Present,
    ): Reply =
        withContext(Dispatchers.IO) {
            val values = store.values().first()
            val key = values[Secret.OpenaiApiKey]
                ?: throw ChainFailure("no OpenAI key has been entered")
            val base = (values[Secret.OpenaiEndpoint] ?: DEFAULT_BASE).trimEnd('/')

            ChatCompletions.ask(
                base = base,
                key = key,
                model = model,
                history = history,
                heard = heard,
                scene = scene,
                present = present,
                say = "conversation: asking openai/$model" +
                    (effort?.let { " at ${it.id}" } ?: ""),
            ) {
                effort?.let { put("reasoning_effort", it.id) }
            }
        }

    private companion object {
        /** The host, with the user's override in front of it. */
        const val DEFAULT_BASE = "https://api.openai.com/v1"
    }
}
