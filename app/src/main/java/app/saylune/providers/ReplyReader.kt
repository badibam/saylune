package app.saylune.providers

import app.saylune.chain.Asked
import app.saylune.chain.ChainFailure
import app.saylune.chain.Reply
import app.saylune.chain.Said
import app.saylune.chain.Verdict
import app.saylune.debug.Trace
import app.saylune.judged.Judgement
import app.saylune.judged.Marked
import app.saylune.judged.Span
import app.saylune.judged.Unreadable
import app.saylune.judged.unfold
import app.saylune.scene.Kind
import app.saylune.scene.Reach
import app.saylune.scene.Value
import app.saylune.sheets.Reading
import app.saylune.sheets.Sheets
import org.json.JSONArray
import org.json.JSONObject

/**
 * The two contracts, read back and checked at their seams.
 *
 * One pair of readers for every provider, for the reason the prompts are held in one place:
 * what differs between providers is how the JSON is guaranteed, never what is asked or how it
 * is read.
 *
 * **Nothing here falls back to a plausible value.** A field the contract requires and the model
 * left out is the model breaking its contract, which is a failure like any other and says so.
 * The one documented exception is `intended`, where falling back to the raw transcript is
 * harmless: the analysis then measures against exactly what was heard.
 */
internal object ReplyReader {

    /**
     * What the one who speaks sent back.
     *
     * [provoked] says there was **no learner turn**: nobody spoke into it, so there is no
     * sentence to write out and no slip to pick up, and only `said` is required.
     */
    fun read(
        content: String,
        transcript: String,
        provoked: Boolean = false,
        asking: List<Asked> = emptyList(),
    ): Reply {
        val parsed = parsed(content)

        val turn = turn(parsed, content)
        val established = established(parsed, asking, content)

        if (provoked) {
            Trace.add("conversation: spoke of its own accord", "said" to written(turn),
                      "settled" to established.keys.joinToString().ifEmpty { null })
            return Reply(intended = null, said = turn, echo = null, established = established)
        }

        val intended = parsed.optString("intended").ifBlank { transcript }
        val echo = parsed.optString("echo").trim().ifBlank { null }

        Trace.add(
            "conversation: answered",
            "said" to written(turn),
            "intended" to intended,
            "echo" to echo,
            "settled" to established.keys.joinToString().ifEmpty { null },
            "intended fell back to the transcript" to
                if (parsed.optString("intended").isBlank()) "yes" else null,
        )
        return Reply(
            intended = intended, said = turn, echo = echo, established = established,
        )
    }

    /**
     * The run of utterances the turn is, read and bounded.
     *
     * **The ceiling refuses rather than trims.** A run cut at six is a scene missing its last
     * two lines, said as though it were whole, and nothing on screen would say so -- where a
     * refusal keeps the recording and offers the button that sends it again. It is a contract
     * broken like any other, and the trace carries what the model actually wrote.
     *
     * A kind the contract does not name is refused for the same reason: guessing *speech*
     * would put a narrator's line in a character's mouth, which is exactly the confusion the
     * two kinds exist to prevent.
     */
    private fun turn(parsed: JSONObject, content: String): List<Said> {
        val written = parsed.optJSONArray("said").objects()
        if (written.isEmpty()) {
            Trace.fail("conversation: nothing to say", "content" to content)
            throw ChainFailure("the model returned nothing to say")
        }
        if (written.size > Said.CEILING) {
            Trace.fail("conversation: more utterances in one turn than the contract allows",
                       "said" to written.size.toString(), "content" to content)
            throw ChainFailure(
                "the model answered with ${written.size} utterances in one turn, " +
                    "where at most ${Said.CEILING} are allowed",
            )
        }
        return written.map { one ->
            val text = one.optString("text").trim()
            if (text.isBlank()) {
                Trace.fail("conversation: an utterance with no words", "content" to content)
                throw ChainFailure("the model wrote an utterance with nothing in it")
            }
            val kind = when (one.optString("kind").trim()) {
                "speech" -> Said.Kind.Speech
                "stage" -> Said.Kind.StageDirection
                else -> {
                    Trace.fail("conversation: an utterance of no known kind",
                               "kind" to one.optString("kind"), "content" to content)
                    throw ChainFailure(
                        "the model wrote an utterance of kind \"${one.optString("kind")}\"",
                    )
                }
            }
            val who = one.optString("who").trim()
            if (who.isBlank()) {
                Trace.fail("conversation: an utterance nobody said", "content" to content)
                throw ChainFailure("the model wrote an utterance with no speaker")
            }
            Said(kind, who, text)
        }
    }

    /** The run as one line of trace: who said what, in order. */
    private fun written(turn: List<Said>): String =
        turn.joinToString(" | ") { "${it.who}: ${it.text}" }

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
        parsed: JSONObject, asking: List<Asked>, content: String,
    ): Map<String, String> {
        if (asking.isEmpty()) return emptyMap()
        val settled = parsed.optJSONObject("established")
        return asking.associate { case ->
            val answer = settled?.optString(case.key).orEmpty().trim()
            if (answer.isBlank()) {
                Trace.fail("conversation: a case was asked for and not written",
                           "case" to case.key, "content" to content)
                throw ChainFailure("the model left \"${case.key}\" unanswered")
            }
            if (!holds(case, answer)) {
                Trace.fail("conversation: a value the case cannot hold",
                           "case" to case.key, "answered" to answer, "content" to content)
                throw ChainFailure(
                    "the model answered \"$answer\" for \"${case.key}\", which cannot hold it",
                )
            }
            case.key to answer
        }
    }

    /**
     * Whether [answer] is a value [case] can hold.
     *
     * *It does not know* is a value at the first step of the reach and at that one alone: it is
     * what the model says instead of leaving a field out, a gap behind a silence being
     * unreadable, and the case is left as it was.
     */
    private fun holds(case: Asked, answer: String): Boolean = when {
        answer == Asked.DONT_KNOW -> case.reach == Reach.Said
        case.kind is Kind.Flag -> answer == "true" || answer == "false"
        case.kind is Kind.Number ->
            answer.toDoubleOrNull()?.let { (case.kind as Kind.Number).holds(Value.Num(it)) } ?: false
        case.kind is Kind.Choice -> answer in (case.kind as Kind.Choice).among
        else -> true
    }
}

/**
 * What the one who judges sent back, checked against the `intended` it was given.
 *
 * **It never sees the transcript and never writes `intended`**, so there is nothing to fall
 * back to here: the string the marks index into came out of the other call, and the app hands
 * it straight to [Judgement].
 */
internal object VerdictReader {

    fun read(content: String, intended: String): Verdict {
        val parsed = parsed(content)

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
            Trace.fail("judgement: a marking does not fit the turn it was given",
                       "why" to it.message, "content" to content)
            // The two ways a marking is unreadable send whoever reads the failure to two
            // different places, so they are not given the same sentence.
            throw ChainFailure(
                when ((it as? Unreadable)?.kind) {
                    Unreadable.Kind.Notch -> "the model marked with a notch that does not exist"
                    else -> "the model marked outside the words it was given"
                }
            )
        }

        val choice = parsed.optString("choice").trim().ifBlank { null }

        Trace.add(
            "judgement: marked",
            "intended" to intended,
            "spans" to spans.size.toString(),
            "stumbling" to stumbling.size.toString(),
            "following" to following,
            "reach" to reach,
            "difficulty" to difficulty,
            "remarks" to remarks.entries.joinToString("; ") { "${it.key}: ${it.value}" }
                .ifEmpty { null },
            "picked from the menu" to choice,
        )
        return Verdict(judgement = judged, choice = choice)
    }
}

/** The object the model was asked for, or the failure that says it did not send one. */
private fun parsed(content: String): JSONObject =
    runCatching { JSONObject(content) }.getOrElse {
        // The raw answer goes in the trace: a model that breaks its format is only
        // fixable by someone who can read what it actually wrote.
        Trace.fail("conversation: not the JSON it was asked for", "content" to content)
        throw ChainFailure("the model did not answer with the JSON it was asked for")
    }

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
