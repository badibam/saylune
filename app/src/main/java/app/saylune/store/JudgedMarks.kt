package app.saylune.store

import app.saylune.judged.Judgement
import app.saylune.judged.Marked
import app.saylune.judged.Span
import org.json.JSONArray
import org.json.JSONObject

/**
 * What the language model marked, written out and read back as it came.
 *
 * The same choice as the sound marks next door, and for the same reason: **archived exactly
 * as the screen shows it, with no model added on top** -- plain facts, whose later uses derive
 * themselves if they ever come. That is why this is a text column and not four tables.
 *
 * Every offset here indexes the utterance's own text, so a judgement read back is only ever
 * shown against the string it was marked on.
 */
internal object JudgedMarks {

    fun write(judged: Judgement): String = JSONObject()
        .put("intended", judged.intended)
        .put("following", judged.following)
        .put("reach", judged.reach)
        .put("difficulty", judged.difficulty)
        .put("remarks", JSONObject().apply {
            judged.remarks.forEach { (aptitude, line) -> put(aptitude, line) }
        })
        .put("spans", JSONArray().apply {
            judged.spans.forEach {
                put(JSONObject().put("from", it.from).put("to", it.to)
                    .put("correctness", it.correctness).put("relevance", it.relevance))
            }
        })
        .put("stumbling", JSONArray().apply {
            judged.stumbling.forEach {
                put(JSONObject().put("from", it.from).put("to", it.to).put("notch", it.notch))
            }
        })
        .toString()

    fun read(stored: String): Judgement {
        val json = JSONObject(stored)
        return Judgement(
            intended = json.getString("intended"),
            spans = json.getJSONArray("spans").each {
                Span(it.getInt("from"), it.getInt("to"),
                     it.getString("correctness"), it.getString("relevance"))
            },
            stumbling = json.getJSONArray("stumbling").each {
                Marked(it.getInt("from"), it.getInt("to"), it.getString("notch"))
            },
            following = json.getString("following"),
            // A judgement written before this notch existed carries none, and a sitting
            // reopened has to stay readable. The empty string names no notch of the column,
            // so the sheet drops out of the sum -- which is what an absent measure does
            // everywhere else, rather than a value nobody produced.
            reach = json.optString("reach"),
            difficulty = json.getString("difficulty"),
            // Same as `reach` above: a judgement written before these existed carries none,
            // and an empty map is what an absent remark already looks like on a clean turn.
            remarks = json.optJSONObject("remarks")?.let { written ->
                written.keys().asSequence().associateWith { written.getString(it) }
            } ?: emptyMap(),
        )
    }

    private fun <T> JSONArray.each(read: (JSONObject) -> T): List<T> =
        (0 until length()).map { read(getJSONObject(it)) }
}
