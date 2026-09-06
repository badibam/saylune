package app.speakup.store

import app.speakup.activity.Brief
import app.speakup.activity.Character
import app.speakup.activity.Chosen
import app.speakup.activity.Origin
import app.speakup.activity.Text
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Position
import app.speakup.levers.Positions
import app.speakup.notes.Weights
import app.speakup.rules.Instructing
import org.json.JSONArray
import org.json.JSONObject

/**
 * How a sitting's own settings are written down and read back.
 *
 * These get **one column each and not a table each**, and the reason is the one the store
 * already gives about the marks: what is kept is what the app holds, without a modelling
 * layer added on top. A table of lever positions would be five tables to keep in step for
 * something no query ever asks a question of -- a sitting reads all of its settings or none.
 *
 * **Everything travels by name.** A position is a step's name or a number, an instruction is
 * its marking and its text; nothing here is a rank into a list, which would be a promise never
 * to reorder that list, and which nobody remembers making.
 */
internal object Sitting {

    /**
     * Lever positions, as one object of key to value.
     *
     * A number is written as a number, and **no maximum** as null -- which is a position and
     * not an absence, so the key is still there. A named step is written as its name.
     */
    fun write(positions: Positions): String = JSONObject().apply {
        positions.all().forEach { (key, position) ->
            when (position) {
                is At -> put(key, position.name)
                is Count -> put(key, position.n ?: JSONObject.NULL)
            }
        }
    }.toString()

    fun readPositions(stored: String): Positions {
        val json = JSONObject(stored)
        return Positions(
            json.keys().asSequence().associateWith { key ->
                if (json.isNull(key)) Count(null)
                else when (val value = json.get(key)) {
                    is Int -> Count(value)
                    else -> At(value.toString())
                } as Position
            },
        )
    }

    /** The weights, by the path of the node each one sits on. */
    fun write(weights: Weights): String = JSONObject().apply {
        weights.byPath.forEach { (path, weight) -> put(path, weight.toDouble()) }
    }.toString()

    fun readWeights(stored: String): Weights {
        val json = JSONObject(stored)
        return Weights(json.keys().asSequence().associateWith { json.getDouble(it).toFloat() })
    }

    /**
     * The cast, each character with its key and its short name by language.
     *
     * Copied onto the line like every other thing a definition lays down: an utterance names
     * its speaker by key, so a sitting that lost its cast would hold names it cannot read.
     */
    fun writeCast(of: List<Character>): String = JSONArray().apply {
        of.forEach { character ->
            put(JSONObject()
                .put("key", character.key)
                .put("short", JSONObject().apply {
                    character.short.byLanguage.forEach { (language, text) -> put(language, text) }
                }))
        }
    }.toString()

    fun readCast(stored: String): List<Character> = JSONArray(stored).objects().map { json ->
        val short = json.getJSONObject("short")
        Character(
            key = json.getString("key"),
            short = Text(short.keys().asSequence().associateWith { short.getString(it) }),
        )
    }

    fun write(brief: Brief): String =
        JSONObject().put("situation", brief.situation).put("staging", brief.staging).toString()

    fun readBrief(stored: String): Brief = JSONObject(stored).let {
        Brief(it.optString("situation"), it.optString("staging"))
    }

    fun write(origin: Origin): String =
        JSONObject().put("definition", origin.definition).put("version", origin.version).toString()

    fun readOrigin(stored: String): Origin = JSONObject(stored).let {
        Origin(it.getString("definition"), it.getString("version"))
    }

    fun writeInstructions(of: List<Instructing>): String = JSONArray().apply {
        of.forEach {
            put(JSONObject()
                .put("marking", it.marking)
                .put("text", it.text ?: JSONObject.NULL)
                .put("lasts", it.lasts ?: JSONObject.NULL))
        }
    }.toString()

    fun readInstructions(stored: String): List<Instructing> =
        JSONArray(stored).objects().map {
            Instructing(
                marking = it.getString("marking"),
                text = if (it.isNull("text")) null else it.getString("text"),
                lasts = if (it.isNull("lasts")) null else it.getInt("lasts"),
            )
        }

    /**
     * The journal: which rule fired, at which passage, and which pack was taken.
     *
     * **The choice and nothing else.** The effects follow from the rule and the choice, so
     * writing them too would be a second source that could drift from the first.
     */
    fun writeJournal(of: List<Chosen>): String = JSONArray().apply {
        of.forEach {
            put(JSONObject()
                .put("rule", it.rule).put("passage", it.passage)
                .put("pack", it.pack).put("at", it.at))
        }
    }.toString()

    fun readJournal(stored: String): List<Chosen> = JSONArray(stored).objects().map {
        Chosen(it.getString("rule"), it.getInt("passage"), it.getInt("pack"), it.getLong("at"))
    }

    private fun JSONArray.objects(): List<JSONObject> =
        (0 until length()).map { getJSONObject(it) }
}
