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
class DeepseekConversation(private val store: SecretStore) : Conversation {

    override suspend fun reply(history: List<Exchange>, heard: List<Word>): Reply =
        withContext(Dispatchers.IO) {
            val key = store.values().first()[Secret.DeepseekApiKey]
                ?: throw ChainFailure("no DeepSeek key has been entered")

            val transcript = heard.joinToString(" ") { it.text }
            Trace.add(
                "conversation: asking $MODEL",
                "system prompt" to SYSTEM,
                "turns of history" to history.size.toString(),
                "transcript" to transcript,
            )

            val messages = JSONArray().apply {
                put(message("system", SYSTEM))
                history.forEachIndexed { at, exchange ->
                    if (exchange.fromLearner) put(message("user", exchange.text))
                    else put(message("assistant", answered(history, at)))
                }
                put(message("user", transcript))
            }
            val body = JSONObject()
                .put("model", MODEL)
                .put("messages", messages)
                .put("response_format", JSONObject().put("type", "json_object"))
                .toString()

            val answer = Http.post(
                url = "https://api.deepseek.com/chat/completions",
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
            Trace.add(
                "conversation: answered",
                "spoken" to spoken,
                "intended" to intended,
                "intended fell back to the transcript" to
                    if (parsed.optString("intended").isBlank()) "yes" else null,
            )
            Reply(spoken = spoken, intended = intended)
        }

    /**
     * A past answer of the model, written as the object it actually emitted.
     *
     * Measured on the device: replaying these as bare prose makes the third turn come back
     * as twenty spaces with `finish_reason: stop`. The conversation then shows the model its
     * own answers in prose while `response_format` only lets it emit JSON, and whitespace is
     * the one thing legal at the start of a JSON document. It gets worse turn by turn,
     * because each turn adds one more example pulling the other way.
     */
    private fun answered(history: List<Exchange>, at: Int): String {
        val said = JSONObject().put("spoken", history[at].text)
        // `intended` belonged to the learner's turn just before, which is where the pipeline
        // always puts it. Written out only when it really is there.
        history.getOrNull(at - 1)?.takeIf { it.fromLearner }?.let { said.put("intended", it.text) }
        return said.toString()
    }

    private fun message(role: String, content: String) =
        JSONObject().put("role", role).put("content", content)

    private companion object {
        /**
         * Provisional, like the provider -- but named outright rather than left to the
         * alias: `deepseek-chat` was being served by `deepseek-v4-flash`, which the trace
         * showed and nothing else would have. A measure is worth what the model behind it
         * is known to be.
         */
        const val MODEL = "deepseek-v4-pro"

        val SYSTEM = """
            You are a warm, curious English conversation partner for someone practising
            speaking. Talk with them; never run a lesson and never interrupt.

            When they make a mistake, do not stop on it: recast it inside your own reply, the
            way a friendly native speaker would. To "I have 25 years", answer something like
            "Ah, you're 25! And where...".

            You receive their turn as a raw transcript: lower case, no punctuation, and
            possibly a word the recogniser misheard.

            Answer with a JSON object holding exactly two fields.

            "spoken": your reply, in English, as it should be said aloud.

            "intended": the learner's own turn, written out. Repair only the transcription --
            a word the recogniser clearly got wrong given the conversation -- and punctuate
            it according to what they meant, since you know which intention you are
            answering. Never repair their grammar, their word choice or their style: a wrong
            tense, a missing article, a clumsy turn of phrase must survive here exactly as
            they said it. If nothing was misheard, return the transcript with punctuation and
            capitals only.
        """.trimIndent()
    }
}
