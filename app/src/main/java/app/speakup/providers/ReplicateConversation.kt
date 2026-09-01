package app.speakup.providers

import app.speakup.chain.ChainFailure
import app.speakup.chain.Conversation
import app.speakup.chain.Exchange
import app.speakup.chain.Reply
import app.speakup.chain.Word
import app.speakup.debug.Trace
import org.json.JSONArray
import org.json.JSONObject

/**
 * A frontier model reached through the Replicate key the app already holds.
 *
 * Two things make this worth a second route to a language model. One key fewer at the BYOK
 * wall, since Replicate already carries the recognition and the synthesis. And
 * `reasoning_effort`, which is the exact lever the latency measurements point at: the
 * language model is the link that costs the most once the synthesis is fast, and what it
 * spends is reasoning -- 638 tokens measured deciding whether one word had been misheard
 * (`../../../../../../TODO.md`).
 *
 * **There is no JSON mode here, and that is the cost.** DeepSeek is asked for
 * `json_object` and cannot answer anything else; none of the models Replicate proxies takes
 * such an input. The three fields are then guaranteed by the instruction alone, so the
 * answer is unwrapped and searched rather than trusted -- and a missing `faulty` is still a
 * failure, never a default, because it decides whether the sound analysis runs at all.
 */
internal class ReplicateConversation(
    private val client: ReplicateClient,
    private val model: String,
) : Conversation {

    override suspend fun reply(history: List<Exchange>, heard: List<Word>, titled: String?): Reply {
        val transcript = heard.joinToString(" ") { it.text }
        Trace.add(
            "conversation: asking replicate/$model",
            "turns of history" to history.size.toString(),
            "transcript" to transcript,
        )

        val messages = JSONArray().apply {
            history.forEachIndexed { at, exchange ->
                if (exchange.fromLearner) put(ConversationPrompt.message("user", exchange.text))
                else put(ConversationPrompt.message("assistant",
                    ConversationPrompt.answered(history, at)))
            }
            put(ConversationPrompt.message("user", transcript))
        }

        val input = JSONObject()
            .put("system_prompt", ConversationPrompt.system(titled) + ConversationPrompt.JSON_ONLY)
            .put("messages", messages)
            // The whole point of this route. The task is judgement against a written
            // instruction, not a problem to work through, and the measurements say the
            // thinking is what the learner waits for.
            .put("reasoning_effort", "low")

        val prediction = client.predict(model, input)
        val content = joined(prediction.opt("output"))
        if (content.isBlank()) {
            Trace.fail("conversation: answered with nothing at all")
            throw ChainFailure("$model answered with nothing at all")
        }

        val parsed = runCatching { JSONObject(unwrapped(content)) }.getOrElse {
            // The raw answer goes in the trace: a model that breaks its format is only
            // fixable by someone who can read what it actually wrote.
            Trace.fail("conversation: not the JSON it was asked for", "content" to content)
            throw ChainFailure("$model did not answer with the JSON it was asked for")
        }

        val spoken = parsed.optString("spoken")
        if (spoken.isBlank()) {
            Trace.fail("conversation: nothing to say", "content" to content)
            throw ChainFailure("$model returned nothing to say")
        }
        // Falling back to the raw transcript is the documented, harmless case: the analysis
        // then measures against exactly what was heard.
        val intended = parsed.optString("intended").ifBlank { transcript }
        // No default. Absent, it would have to stand for something, and both readings are
        // wrong: "not faulty" runs the sound analysis on a turn about to be rewritten,
        // "faulty" silently withholds marks.
        if (!parsed.has("faulty")) {
            Trace.fail("conversation: no grammatical verdict", "content" to content)
            throw ChainFailure("$model left out the grammatical verdict")
        }
        val faulty = parsed.getBoolean("faulty")
        // Absent is the ordinary answer and means "keep the name it has". Blank is treated
        // the same: a model that sends an empty title has not named anything.
        val title = parsed.optString("title").trim().ifBlank { null }
        Trace.add(
            "conversation: answered",
            "spoken" to spoken,
            "intended" to intended,
            "grammatically faulty" to faulty.toString(),
            "renamed the conversation" to title,
        )
        return Reply(spoken = spoken, intended = intended, faulty = faulty, title = title)
    }

    /** Replicate hands text back in pieces as it is produced, or whole. Both are answers. */
    private fun joined(output: Any?): String = when (output) {
        null -> ""
        is JSONArray -> (0 until output.length()).joinToString("") { output.optString(it) }
        else -> output.toString()
    }

    /**
     * The object inside whatever the model wrapped it in.
     *
     * Only because there is no JSON mode on this route: a fence or a sentence in front is
     * exactly what the instruction asks against, and a parser that refused them would turn
     * a model's habit into a broken turn. What is not tolerated is the object being absent.
     */
    private fun unwrapped(content: String): String {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        return if (start >= 0 && end > start) content.substring(start, end + 1) else content
    }
}
