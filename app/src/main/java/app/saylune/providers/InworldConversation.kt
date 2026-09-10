package app.saylune.providers

import app.saylune.chain.Conversation
import app.saylune.chain.Exchange
import app.saylune.chain.Present
import app.saylune.chain.Reply
import app.saylune.chain.Verdict
import app.saylune.chain.Scene
import app.saylune.chain.Word
import app.saylune.keys.SecretStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Inworld's Router: OpenAI's `/chat/completions` in front of other people's models.
 *
 * So the instruction is [ConversationPrompt]'s and the reading is [ReplyReader]'s, and what
 * this carries is only what is its own -- where it lives and which key opens it. Nothing is
 * added to the body: `response_format` is already sent by [ChatCompletions], and the Router
 * passes it through to whichever model was named.
 *
 * **The model is named with its namespace**, `openai/gpt-4o-mini` and not `gpt-4o-mini`.
 * That is not decoration: `docs/reference.md` holds that a judgement is not the same from
 * one provider to the next, so the route is part of what identifies the model, exactly as it
 * is for the same models reached through Replicate.
 *
 * **No reasoning level is sent**, and that is a statement about what is known rather than
 * about the models. Nothing here has read whether the Router forwards `reasoning_effort` to
 * a model that takes one, so naming a level would be asking for something unverified. Same
 * standing as Replicate's, and the same entry in `../../../../../../TODO.md`.
 */
class InworldConversation(
    private val store: SecretStore,
    private val model: String,
) : Conversation {

    override suspend fun reply(
        history: List<Exchange>, heard: List<Word>, scene: Scene, present: Present,
    ): Reply = withContext(Dispatchers.IO) {
        val values = store.values().first()
        ChatCompletions.reply(
            base = "${InworldApi.base(values)}/v1",
            key = InworldApi.key(values),
            model = model,
            history = history, heard = heard, scene = scene, present = present,
            say = "conversation: asking inworld/$model",
        ) {}
    }

    override suspend fun judge(
        history: List<Exchange>, said: String, answered: String, situation: String,
        present: Present,
    ): Verdict = withContext(Dispatchers.IO) {
        val values = store.values().first()
        ChatCompletions.judge(
            base = "${InworldApi.base(values)}/v1",
            key = InworldApi.key(values),
            model = model,
            history = history, said = said, answered = answered, situation = situation,
            present = present,
            say = "judgement: asking inworld/$model",
        ) {}
    }
}
