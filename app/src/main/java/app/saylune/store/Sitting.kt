package app.saylune.store

import app.saylune.activity.Origin
import app.saylune.activity.Text
import app.saylune.activity.Writer
import app.saylune.activity.Written
import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Position
import app.saylune.levers.Positions
import app.saylune.notes.Weights
import app.saylune.scene.Role
import org.json.JSONArray
import org.json.JSONObject

/**
 * How a sitting's own settings are written down and read back.
 *
 * **One column each and not a table each**: a sitting reads all of its settings or none, and
 * no query asks a question of them. **Everything travels by name**, never by a rank into a
 * list, which would be a promise never to reorder that list.
 */
internal object Sitting {

    /**
     * Lever positions, as one object of key to value. **No maximum** is null -- a position and
     * not an absence, so the key is still there.
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

    /** The cast, each one with its key, its short name by language, and its description. */
    fun writeCast(of: List<Role>): String = JSONArray().apply {
        of.forEach { role ->
            put(JSONObject()
                .put("key", role.key)
                .put("main", role.main)
                .put("gender", role.gender)
                .put("about", role.about)
                .put("short", JSONObject().apply {
                    role.short.byLanguage.forEach { (language, text) -> put(language, text) }
                }))
        }
    }.toString()

    /** A cast written before descriptions were on the line comes back with none. */
    fun readCast(stored: String): List<Role> = JSONArray(stored).objects().map { json ->
        val short = json.getJSONObject("short")
        Role(
            key = json.getString("key"),
            short = Text(short.keys().asSequence().associateWith { short.getString(it) }),
            about = json.optString("about"),
            main = json.optBoolean("main"),
            gender = json.optString("gender").takeIf { it.isNotEmpty() },
        )
    }

    fun write(origin: Origin): String =
        JSONObject().put("definition", origin.definition).put("version", origin.version).toString()

    fun readOrigin(stored: String): Origin = JSONObject(stored).let {
        Origin(it.getString("definition"), it.getString("version"))
    }

    /** What the learner filled and chance drew: the case, the value, who, and when. */
    fun writeJournal(of: List<Written>): String = JSONArray().apply {
        of.forEach {
            put(JSONObject()
                .put("case", it.case).put("value", it.value)
                .put("by", it.by.name).put("passage", it.passage))
        }
    }.toString()

    fun readJournal(stored: String): List<Written> = JSONArray(stored).objects().map {
        Written(it.getString("case"), it.getString("value"), Writer.valueOf(it.getString("by")),
                it.getInt("passage"))
    }

    private fun JSONArray.objects(): List<JSONObject> =
        (0 until length()).map { getJSONObject(it) }
}
