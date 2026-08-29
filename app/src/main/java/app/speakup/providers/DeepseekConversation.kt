package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Reply
import app.speakup.chain.Word
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

            val messages = JSONArray().apply {
                put(message("system", SYSTEM))
                history.forEach { put(message(if (it.fromLearner) "user" else "assistant", it.text)) }
                put(message("user", heard.joinToString(" ") { it.text }))
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

            val parsed = runCatching { JSONObject(content) }.getOrElse {
                throw ChainFailure("DeepSeek did not answer with the JSON it was asked for")
            }
            val spoken = parsed.optString("spoken")
            if (spoken.isBlank()) throw ChainFailure("DeepSeek returned nothing to say")
            Reply(
                spoken = spoken,
                // Falling back to the raw transcript is the documented, harmless case: the
                // analysis then measures against exactly what was heard.
                intended = parsed.optString("intended").ifBlank { heard.joinToString(" ") { it.text } },
            )
        }

    private fun message(role: String, content: String) =
        JSONObject().put("role", role).put("content", content)

    private companion object {
        /** Provisional, like the provider. */
        const val MODEL = "deepseek-chat"

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
