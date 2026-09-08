package app.saylune.providers

import app.saylune.activity.Answers
import app.saylune.activity.Question
import app.saylune.activity.Rung
import app.saylune.chain.ChainFailure
import app.saylune.chain.Reply
import app.saylune.debug.Trace
import app.saylune.judged.Judgement
import app.saylune.judged.Marked
import app.saylune.judged.Span
import app.saylune.judged.Unreadable
import app.saylune.judged.unfold
import app.saylune.sheets.Reading
import app.saylune.sheets.Sheets
import org.json.JSONArray
import org.json.JSONObject

/**
 * The enriched contract, read back and checked at the seam.
 *
 * One reader for every provider, for the reason the prompt is held in one place: what differs
 * between providers is how the JSON is guaranteed, never what is asked or how it is read.
 *
 * **Nothing here falls back to a plausible value.** A field the contract requires and the model
 * left out is the model breaking its contract, which is a failure like any other and says so.
 * The one documented exception is `intended`, where falling back to the raw transcript is
 * harmless: the analysis then measures against exactly what was heard.
 */
internal object ReplyReader {

    /**
     * [provoked] says there was **no learner turn**, so there is nothing to judge: the model
     * was asked for what it says and nothing else, and asking for the rest back would be
     * asking it to invent a sentence nobody spoke. Everything the reader checks is a field
     * about that turn, so on this path only `spoken` is required.
     */
    fun read(
        content: String,
        transcript: String,
        provoked: Boolean = false,
        asking: List<Question> = emptyList(),
    ): Reply {
        val parsed = runCatching { JSONObject(content) }.getOrElse {
            // The raw answer goes in the trace: a model that breaks its format is only
            // fixable by someone who can read what it actually wrote.
            Trace.fail("conversation: not the JSON it was asked for", "content" to content)
            throw ChainFailure("the model did not answer with the JSON it was asked for")
        }

        val spoken = parsed.optString("spoken")
        if (spoken.isBlank()) {
            Trace.fail("conversation: nothing to say", "content" to content)
            throw ChainFailure("the model returned nothing to say")
        }

        val choice = parsed.optString("choice").trim().ifBlank { null }
        val established = established(parsed, asking, content)

        if (provoked) {
            Trace.add("conversation: spoke of its own accord", "spoken" to spoken,
                      "picked from the menu" to choice,
                      "settled" to established.keys.joinToString().ifEmpty { null })
            return Reply(
                judged = null, spoken = spoken, echo = null, choice = choice,
                established = established,
            )
        }

        val intended = parsed.optString("intended").ifBlank { transcript }

        val spans = parsed.optJSONArray("spans").objects().map { span ->
            Span(
                from = span.getInt("from"),
                to = span.getInt("to"),
                correctness = span.getString("correctness"),
                relevance = span.getString("relevance"),
            )
        }
        val stumbling = parsed.optJSONArray("stumbling").objects().map { part ->
            Marked(part.getInt("from"), part.getInt("to"), part.getString("notch"))
        }

        val following = parsed.required("following", content)
        val reach = parsed.required("reach", content)
        val difficulty = parsed.required("difficulty", content)
        check(following, Sheets.columnOf("understanding/uptake"), "following", content)
        check(reach, Sheets.columnOf("relevance/reach"), "reach", content)
        check(difficulty, Sheets.DIFFICULTY, "difficulty", content)

        // **Kept to the keys the contract names, and a stray one is dropped rather than fatal.**
        // It is prose: nothing weighs it, nothing gates on it, so losing a line costs a line --
        // where throwing would cost the turn, the recording and the reply with it.
        val remarks = parsed.optJSONObject("remarks")?.let { written ->
            ConversationPrompt.REMARKED
                .mapNotNull { key ->
                    written.optString(key).trim().ifBlank { null }
                        ?.take(ConversationPrompt.REMARK_LIMIT)?.let { key to it }
                }
                .toMap()
        } ?: emptyMap()

        // Unfolding is where the bounds are checked against the words of `intended`, so it
        // runs here rather than downstream: a span that misses a word boundary is a broken
        // answer, and the place to say so is the seam that read it.
        val judged =
            Judgement(intended, spans, stumbling, following, reach, difficulty, remarks)
        runCatching { judged.words() }.onFailure {
            Trace.fail("conversation: a marking does not fit its own text",
                       "why" to it.message, "content" to content)
            // The two ways a marking is unreadable send whoever reads the failure to two
            // different places, so they are not given the same sentence.
            throw ChainFailure(
                when ((it as? Unreadable)?.kind) {
                    Unreadable.Kind.Notch -> "the model marked with a notch that does not exist"
                    else -> "the model marked outside the words it wrote"
                }
            )
        }

        val echo = parsed.optString("echo").trim().ifBlank { null }

        Trace.add(
            "conversation: answered",
            "spoken" to spoken,
            "intended" to intended,
            "spans" to spans.size.toString(),
            "stumbling" to stumbling.size.toString(),
            "following" to following,
            "reach" to reach,
            "difficulty" to difficulty,
            "remarks" to remarks.entries.joinToString("; ") { "${it.key}: ${it.value}" }
                .ifEmpty { null },
            "echo" to echo,
            "picked from the menu" to choice,
            "intended fell back to the transcript" to
                if (parsed.optString("intended").isBlank()) "yes" else null,
        )
        return Reply(
            judged = judged, spoken = spoken, echo = echo, choice = choice,
            established = established,
        )
    }

    /**
     * What the model settled, checked against what it was asked.
     *
     * **The presence of an answer is checked and its content is not**, which is the whole of
     * what serving the question buys: the app put the question at a moment it chose, so a
     * missing answer is the contract broken and not a model that had nothing to say. Judgement
     * is still taken on trust.
     *
     * A closed shape is checked by **membership**, on the English of each option -- which is
     * the key that went out and the one that comes back. *It does not know* is a member at the
     * first rung and at that one alone, whether the shape is free or closed: it is what the
     * model says instead of leaving a field out, a gap behind a silence being unreadable.
     */
    private fun established(
        parsed: JSONObject, asking: List<Question>, content: String,
    ): Map<String, String> {
        if (asking.isEmpty()) return emptyMap()
        val settled = parsed.optJSONObject("established")
        return asking.associate { question ->
            val answer = settled?.optString(question.key).orEmpty().trim()
            if (answer.isBlank()) {
                Trace.fail("conversation: a question was put and not answered",
                           "question" to question.key, "content" to content)
                throw ChainFailure("the model left \"${question.key}\" unanswered")
            }
            val among = question.answers as? Answers.OneOf
            if (among != null && answer !in allowed(among, question)) {
                Trace.fail("conversation: an answer outside the options offered",
                           "question" to question.key, "answered" to answer,
                           "content" to content)
                throw ChainFailure(
                    "the model answered \"$answer\" to \"${question.key}\", " +
                        "which is not one of its options",
                )
            }
            question.key to answer
        }
    }

    private fun allowed(among: Answers.OneOf, question: Question): List<String> =
        among.keys + if (question.rung == Rung.FromTheTalk) listOf(Question.DONT_KNOW)
        else emptyList()

    private fun JSONObject.required(field: String, content: String): String {
        val value = optString(field).trim()
        if (value.isBlank()) {
            Trace.fail("conversation: no $field", "content" to content)
            throw ChainFailure("the model left out \"$field\"")
        }
        return value
    }

    private fun check(notch: String, column: Reading.Column, field: String, content: String) {
        if (column.notches.none { it.name == notch }) {
            Trace.fail("conversation: a notch the catalogue does not declare",
                       field to notch, "content" to content)
            throw ChainFailure("the model answered \"$notch\" for \"$field\"")
        }
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }
}

/**
 * The three markings unfolded onto the words of `intended`, each word carrying one notch.
 *
 * Computed and never stored: it is a walk of the marking, and a walk cannot fall out of step
 * with what it walks. It is also the check -- it fails outright on a bound that misses a word
 * boundary, or on a notch the catalogue does not declare.
 */
fun Judgement.words(): Words {
    val correctness = Sheets.columnOf("correctness/correctness")
    val relevance = Sheets.columnOf("relevance/relevance")
    val stumble = Sheets.columnOf("fluency/stumbling")
    return Words(
        correctness = unfold(
            intended, spans.map { Marked(it.from, it.to, it.correctness) },
            correctness.notches.map { it.name }, correctness.fallback!!,
        ),
        relevance = unfold(
            intended, spans.map { Marked(it.from, it.to, it.relevance) },
            relevance.notches.map { it.name }, relevance.fallback!!,
        ),
        stumbling = unfold(
            intended, stumbling, stumble.notches.map { it.name }, stumble.fallback!!,
        ),
    )
}

/**
 * One notch per word, on each of the three scales that mark words.
 *
 * The three lists cover the same words in the same order, so the words a sheet reads are a
 * filter over them: **the kept words** are those the stumbling calls `kept`, and that is
 * what `kept` means for the analysis seam.
 */
data class Words(
    val correctness: List<app.saylune.judged.Word>,
    val relevance: List<app.saylune.judged.Word>,
    val stumbling: List<app.saylune.judged.Word>,
) {

    /** The stretches of `intended` the model was synthesised on: everything not given up. */
    val kept: List<IntRange> get() = stumbling.filter { it.notch == "kept" }.map { it.at }
}
