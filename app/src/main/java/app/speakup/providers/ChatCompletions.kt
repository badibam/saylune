package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.chain.Exchange
import app.speakup.chain.Reply
import app.speakup.chain.Word
import app.speakup.debug.Trace
import org.json.JSONArray
import org.json.JSONObject

/**
 * The call two providers make in the same shape: OpenAI's `/chat/completions`.
 *
 * It is here rather than copied into each because the shape is genuinely one thing -- the
 * messages, the JSON guarantee, the reading of `choices[0].message.content` -- and a second
 * copy beside a second caller is a second source that drifts. What stays with each provider
 * is what is actually its own: where it lives, which key opens it, and the fields it takes
 * for reasoning, which no two of them spell the same way.
 */
internal object ChatCompletions {

    /**
     * Ask [model] at [base], and read the reply.
     *
     * [extra] is where a provider adds what only it takes. It runs on the body after the
     * common fields, so a provider can add to them and never has to rebuild them.
     */
    suspend fun ask(
        base: String,
        key: String,
        model: String,
        history: List<Exchange>,
        heard: List<Word>,
        titled: String?,
        say: String,
        extra: JSONObject.() -> Unit,
    ): Reply {
        val transcript = heard.joinToString(" ") { it.text }
        Trace.add(
            say,
            "system prompt" to ConversationPrompt.system(titled),
            "turns of history" to history.size.toString(),
            "transcript" to transcript,
        )

        val messages = JSONArray().apply {
            put(ConversationPrompt.message("system", ConversationPrompt.system(titled)))
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
            .apply(extra)
            .toString()

        val answer = Http.post(
            url = "${base.trimEnd('/')}/chat/completions",
            headers = mapOf("Authorization" to "Bearer $key"),
            contentType = "application/json",
            body = body.toByteArray(),
        )

        val content = JSONObject(answer.decodeToString())
            .optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content")
            ?: throw ChainFailure("$model returned no message")

        if (content.isBlank()) {
            Trace.fail("conversation: answered with nothing at all",
                       "chars" to content.length.toString())
            throw ChainFailure("$model answered with nothing at all")
        }
        return ReplyReader.read(content, transcript)
    }
}
