package app.saylune.store

import org.json.JSONObject

/**
 * What the model settled on one turn, written out by question key.
 *
 * **It sits on the turn that settled it and nowhere else.** Everything else about a question
 * is a walk of the run: the series of answers is the turns in order, and the passage that
 * dates each one is where in the run its turn sits. A table beside the run would be a second
 * source that could fall out of step with it, and the prompt wants it here anyway -- a fact
 * goes back to the model in the turn it was established on, never in a heading.
 *
 * **Nothing is overwritten**, ever: an answer at passage 15 succeeds the one at passage 5, and
 * both are on their own turns.
 */
internal object Established {

    fun write(byKey: Map<String, String>): String =
        JSONObject().apply { byKey.forEach { (key, answer) -> put(key, answer) } }.toString()

    fun read(stored: String): Map<String, String> {
        val json = JSONObject(stored)
        return json.keys().asSequence().associateWith { json.getString(it) }
    }
}
