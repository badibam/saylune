package app.speakup.providers

import app.speakup.chain.Exchange
import org.json.JSONObject

/**
 * The instruction every conversation provider is given, and the shape of the history.
 *
 * Held in one place because it **is** the division of labour the project rests on -- the
 * recognition transcribes the mouth, the language model decides the intention -- and a
 * second copy beside a second provider would be a second source that drifts from the first.
 * What differs between providers is how the JSON is guaranteed, never what is asked.
 */
internal object ConversationPrompt {

    val SYSTEM = """
        You are a warm, curious English conversation partner for someone practising
        speaking. Talk with them; never run a lesson and never interrupt.

        When they make a mistake, do not stop on it: recast it inside your own reply, the
        way a friendly native speaker would. To "I have 25 years", answer something like
        "Ah, you're 25! And where...".

        You receive their turn as a raw transcript: lower case, no punctuation, and
        possibly a word the recogniser misheard.

        Answer with a JSON object holding exactly three fields.

        "spoken": your reply, in English, as it should be said aloud.

        "intended": the learner's own turn, written out. Repair only the transcription --
        a word the recogniser clearly got wrong given the conversation -- and punctuate
        it according to what they meant, since you know which intention you are
        answering. Never repair their grammar, their word choice or their style: a wrong
        tense, a missing article, a clumsy turn of phrase must survive here exactly as
        they said it. If nothing was misheard, return the transcript with punctuation and
        capitals only. Write every number, date and amount in **words**, never in digits:
        "twenty five", not "25". The analysis places each sound on the letters that write
        it, and digits have none -- a number in figures loses its sounds to the word
        beside it and puts that word's marks on the wrong letters.

        "faulty": true when the turn you wrote into "intended" is not correct English --
        a wrong tense, a missing or wrong article, a wrong preposition, a word order or a
        construction English does not use. false when it is correct, however simple or
        short. Judge what they said and not how well they said it: a plain sentence, a
        three-word answer or a hesitation is not a fault. This decides whether the app
        works on their pronunciation for this turn, so a false alarm costs them the
        exercise.
    """.trimIndent()

    /**
     * Said again to a provider that has no JSON mode to enforce the shape.
     *
     * DeepSeek is asked for `json_object` and cannot answer anything else; the models
     * reached through Replicate expose no such input, so the only guarantee left is the
     * instruction, and the parser downstream has to be ready for it to be broken.
     */
    val JSON_ONLY = "\n\nAnswer with that JSON object and nothing else: no explanation " +
        "before it, no code fence around it."

    /**
     * A past answer of the model, written as the object it actually emitted.
     *
     * `faulty` is left out of the replay: it was a verdict on the learner's turn, not part
     * of the answer, and putting it back would invite the model to keep re-judging a turn
     * that is already behind.
     *
     * Measured on the device: replaying these as bare prose makes the third turn come back
     * as twenty spaces with `finish_reason: stop`. The conversation then shows the model its
     * own answers in prose while `response_format` only lets it emit JSON, and whitespace is
     * the one thing legal at the start of a JSON document. It gets worse turn by turn,
     * because each turn adds one more example pulling the other way.
     */
    fun answered(history: List<Exchange>, at: Int): String {
        val said = JSONObject().put("spoken", history[at].text)
        // `intended` belonged to the learner's turn just before, which is where the pipeline
        // always puts it. Written out only when it really is there.
        history.getOrNull(at - 1)?.takeIf { it.fromLearner }?.let { said.put("intended", it.text) }
        return said.toString()
    }

    fun message(role: String, content: String): JSONObject =
        JSONObject().put("role", role).put("content", content)
}
