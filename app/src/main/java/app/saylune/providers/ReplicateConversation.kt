package app.saylune.providers

import app.saylune.chain.ChainFailure
import app.saylune.chain.Conversation
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
 * answer is unwrapped and searched rather than trusted. What it is searched for, and every
 * check on what comes back, is [ReplyReader]'s: the reading is one for every provider, since
 * what differs between them is how the JSON is guaranteed and never what is asked.
 */
internal class ReplicateConversation(
    private val client: ReplicateClient,
    private val model: String,
) : Conversation {

    override suspend fun reply(
        history: List<Exchange>, heard: List<Word>, scene: Scene, present: Present,
    ): Reply {
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
            put(ConversationPrompt.message("user", ConversationPrompt.turn(transcript, present)))
        }

        val content = ask(ConversationPrompt.system(scene), messages)
        return ReplyReader.read(content, transcript, present.provoked, present.asking)
    }

    /**
     * The judge, in the same shape minus the roles: its whole context is one message.
     *
     * Which suits this route: the written record needs no alternating turns, so nothing here
     * has to pretend a judge ever spoke.
     */
    override suspend fun judge(
        history: List<Exchange>, said: String, answered: String, situation: String,
        present: Present,
    ): Verdict {
        Trace.add(
            "judgement: asking replicate/$model",
            "turns of history" to history.size.toString(),
            "intended" to said,
        )
        val messages = JSONArray().put(ConversationPrompt.message(
            "user", ConversationPrompt.judged(history, said, answered, present),
        ))
        return VerdictReader.read(ask(ConversationPrompt.judging(situation), messages), said)
    }

    /** One prediction, unwrapped. The instruction is the only JSON guarantee on this route. */
    private suspend fun ask(system: String, messages: JSONArray): String {
        val input = JSONObject()
            .put("system_prompt", system + ConversationPrompt.JSON_ONLY)
            .put("messages", messages)
            // The whole point of this route. The task is judgement against a written
            // instruction, not a problem to work through, and the measurements say the
            // thinking is what the learner waits for.
            .put("reasoning_effort", "low")

        // What goes out, whole, for the passage's own screen. It is this shape and not the
        // chat one: what a provider takes is its own, and showing a body it never sent would
        // be showing a rebuild.
        Trace.asking(input.toString())

        val prediction = client.predict(model, input)
        val content = joined(prediction.opt("output"))
        if (content.isBlank()) {
            Trace.fail("conversation: answered with nothing at all")
            throw ChainFailure("$model answered with nothing at all")
        }
        return unwrapped(content)
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
