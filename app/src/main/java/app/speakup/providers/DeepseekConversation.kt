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
import org.json.JSONArray
import org.json.JSONObject

/**
 * DeepSeek. It measured the chain's latency and that is all it has done -- the provider is
 * not chosen (`../../../../../../TODO.md`, chantier 2).
 *
 * The instruction below is the whole division of labour written out: the model repairs the
 * *transcript* and never the learner's grammar. A model that quietly fixed the grammar here
 * would erase the very thing the grammatical gate exists to catch, one link before anything
 * had judged it.
 */
class DeepseekConversation(
    private val store: SecretStore,
    private val model: String,
) : Conversation {

    override suspend fun reply(history: List<Exchange>, heard: List<Word>): Reply =
        withContext(Dispatchers.IO) {
            val values = store.values().first()
            val key = values[Secret.DeepseekApiKey]
                ?: throw ChainFailure("no DeepSeek key has been entered")
            val base = (values[Secret.DeepseekEndpoint] ?: DEFAULT_BASE).trimEnd('/')

            val transcript = heard.joinToString(" ") { it.text }
            Trace.add(
                "conversation: asking $model",
                "system prompt" to ConversationPrompt.SYSTEM,
                "turns of history" to history.size.toString(),
                "transcript" to transcript,
            )

            val messages = JSONArray().apply {
                put(ConversationPrompt.message("system", ConversationPrompt.SYSTEM))
                history.forEachIndexed { at, exchange ->
                    if (exchange.fromLearner) put(ConversationPrompt.message("user", exchange.text))
                    else put(ConversationPrompt.message("assistant", ConversationPrompt.answered(history, at)))
                }
                put(ConversationPrompt.message("user", transcript))
            }
            val body = JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("response_format", JSONObject().put("type", "json_object"))
                .toString()

            val answer = Http.post(
                url = "$base/chat/completions",
                headers = mapOf("Authorization" to "Bearer $key"),
                contentType = "application/json",
                body = body.toByteArray(),
            )

            val content = JSONObject(answer.decodeToString())
                .optJSONArray("choices")?.optJSONObject(0)
                ?.optJSONObject("message")?.optString("content")
                ?: throw ChainFailure("DeepSeek returned no message")

            if (content.isBlank()) {
                Trace.fail("conversation: answered with nothing at all", "chars" to content.length.toString())
                throw ChainFailure("DeepSeek answered with nothing at all")
            }
            val parsed = runCatching { JSONObject(content) }.getOrElse {
                // The raw answer goes in the trace: a model that breaks its format is only
                // fixable by someone who can read what it actually wrote.
                Trace.fail("conversation: not the JSON it was asked for", "content" to content)
                throw ChainFailure("DeepSeek did not answer with the JSON it was asked for")
            }
            val spoken = parsed.optString("spoken")
            if (spoken.isBlank()) {
                Trace.fail("conversation: nothing to say", "content" to content)
                throw ChainFailure("DeepSeek returned nothing to say")
            }
            // Falling back to the raw transcript is the documented, harmless case: the
            // analysis then measures against exactly what was heard.
            val intended = parsed.optString("intended").ifBlank { transcript }
            // No default. Absent, it would have to stand for something, and both readings
            // are wrong: "not faulty" runs the sound analysis on a turn about to be
            // rewritten, "faulty" silently withholds marks. A missing field is the model
            // breaking its contract, which is a failure like any other and says so.
            if (!parsed.has("faulty")) {
                Trace.fail("conversation: no grammatical verdict", "content" to content)
                throw ChainFailure("DeepSeek left out the grammatical verdict")
            }
            val faulty = parsed.getBoolean("faulty")
            Trace.add(
                "conversation: answered",
                "spoken" to spoken,
                "intended" to intended,
                "grammatically faulty" to faulty.toString(),
                "intended fell back to the transcript" to
                    if (parsed.optString("intended").isBlank()) "yes" else null,
            )
            Reply(spoken = spoken, intended = intended, faulty = faulty)
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
