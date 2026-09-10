package app.saylune.providers

import app.saylune.chain.ChainFailure
import app.saylune.chain.Exchange
import app.saylune.chain.Present
import app.saylune.chain.Reply
import app.saylune.chain.Verdict
import app.saylune.chain.Word
import app.saylune.chain.Scene
import app.saylune.debug.Trace
import org.json.JSONArray
import org.json.JSONObject

/**
 * The call three providers make in the same shape: OpenAI's `/chat/completions`.
 *
 * It is here rather than copied into each because the shape is genuinely one thing -- the
 * messages, the JSON guarantee, the reading of `choices[0].message.content` -- and a second
 * copy beside a second caller is a second source that drifts. What stays with each provider
 * is what is actually its own: where it lives, which key opens it, and the fields it takes
 * for reasoning, which no two of them spell the same way.
 *
 * **Two jobs, one transport.** [reply] and [judge] differ in what they assemble and which
 * reader they hand the answer to; everything from the body down is shared, so a provider that
 * knows how to reach a model knows how to reach it for either.
 */
internal object ChatCompletions {

    /**
     * The one who speaks: the stable head, the history as chat roles, the turn last.
     *
     * [extra] is where a provider adds what only it takes. It runs on the body after the
     * common fields, so a provider can add to them and never has to rebuild them.
     */
    suspend fun reply(
        base: String,
        key: String,
        model: String,
        history: List<Exchange>,
        heard: List<Word>,
        scene: Scene,
        present: Present,
        say: String,
        extra: JSONObject.() -> Unit,
    ): Reply {
        val transcript = heard.joinToString(" ") { it.text }
        Trace.add(
            say,
            "system prompt" to ConversationPrompt.system(scene),
            "turns of history" to history.size.toString(),
            "transcript" to transcript,
        )

        val turns = JSONArray().apply {
            history.forEachIndexed { at, exchange ->
                if (exchange.fromLearner) put(ConversationPrompt.message("user", exchange.text))
                else put(ConversationPrompt.message("assistant",
                                                   ConversationPrompt.answered(history, at)))
            }
            put(ConversationPrompt.message("user", ConversationPrompt.turn(transcript, present)))
        }
        val content = post(base, key, model, ConversationPrompt.system(scene), turns, extra)
        return ReplyReader.read(content, transcript, present.provoked, present.asking)
    }

    /**
     * The one who judges: its own head, and **one message** holding everything else.
     *
     * The history is not sent as chat roles here, and [ConversationPrompt.judged] says why: a
     * judge has no past assistant turns that are not its own verdicts, and showing a model its
     * verdicts makes it consistent with them rather than with the turn in front of it.
     */
    suspend fun judge(
        base: String,
        key: String,
        model: String,
        history: List<Exchange>,
        said: String,
        answered: String,
        situation: String,
        present: Present,
        say: String,
        extra: JSONObject.() -> Unit,
    ): Verdict {
        val system = ConversationPrompt.judging(situation)
        Trace.add(
            say,
            "system prompt" to system,
            "turns of history" to history.size.toString(),
            "intended" to said,
        )
        val turns = JSONArray().put(ConversationPrompt.message(
            "user", ConversationPrompt.judged(history, said, answered, present),
        ))
        return VerdictReader.read(post(base, key, model, system, turns, extra), said)
    }

    /** Ask [model] at [base], and hand back what it wrote. */
    private suspend fun post(
        base: String,
        key: String,
        model: String,
        system: String,
        turns: JSONArray,
        extra: JSONObject.() -> Unit,
    ): String {
        val messages = JSONArray()
            .put(ConversationPrompt.message("system", system))
            .also { for (i in 0 until turns.length()) it.put(turns.get(i)) }
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("response_format", JSONObject().put("type", "json_object"))
            .apply(extra)
            .toString()
        // Whole and uncut, for the passage's own screen: the trace cuts a body at its ceiling
        // and the point here is to read the instruction entire.
        Trace.asking(body)

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
        return content
    }
}
