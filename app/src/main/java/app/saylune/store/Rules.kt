package app.saylune.store

import app.saylune.levers.At
import app.saylune.levers.Count
import app.saylune.levers.Position
import app.saylune.rules.Decider
import app.saylune.rules.Effect
import app.saylune.rules.Instructing
import app.saylune.rules.Moment
import app.saylune.rules.Outcome
import app.saylune.rules.Pack
import app.saylune.rules.Rule
import app.saylune.rules.Staging
import app.saylune.rules.Trigger
import org.json.JSONArray
import org.json.JSONObject

/**
 * The rules of a sitting, written down and read back.
 *
 * Copied onto the line with everything else a definition lays down: **a sitting whose settings
 * a rule moved cannot recompute its own state without them**, and a definition that changes in
 * a later release must not rewrite what a sitting was played under.
 *
 * **Every kind travels by name and never by rank.** A rank is a promise never to reorder a
 * list, which nobody remembers making, and inserting a kind of trigger would silently reread
 * every stored sitting as the kind after it. **And an unknown name fails outright** rather than
 * being dropped: a rule read back short of one of its effects is a rule that does something
 * else, and nothing downstream could tell that apart from a rule that was written that way.
 */
internal object Rules {

    fun write(rules: List<Rule>): String = JSONArray().apply {
        rules.forEach { rule ->
            put(JSONObject()
                .put("key", rule.key)
                .put("armed", rule.armed)
                .put("decider", rule.decider.name)
                .put("when", trigger(rule.whenever))
                .put("choice", JSONArray().apply {
                    rule.choice.forEach { pack ->
                        put(JSONArray().apply { pack.effects.forEach { put(effect(it)) } })
                    }
                }))
        }
    }.toString()

    fun read(stored: String): List<Rule> = JSONArray(stored).objects().map { json ->
        Rule(
            key = json.getString("key"),
            whenever = trigger(json.getJSONObject("when")),
            choice = json.getJSONArray("choice").arrays().map { pack ->
                Pack(pack.objects().map { effect(it) })
            },
            decider = Decider.valueOf(json.getString("decider")),
            armed = json.getBoolean("armed"),
        )
    }

    /**
     * A bare list of triggers, which is what a question's moments are.
     *
     * The same reader a rule's trigger goes through, for the reason every seam here is one
     * reader: a moment written one way in a rule and another in a question would be two
     * spellings of one kind, and the day one of them moves the other is silently wrong.
     */
    fun readTriggers(stored: String): List<Trigger> =
        JSONArray(stored).objects().map { trigger(it) }

    fun writeTriggers(of: List<Trigger>): String =
        JSONArray().apply { of.forEach { put(trigger(it)) } }.toString()

    // ── Triggers ────────────────────────────────────────────────────────────────────────

    private fun trigger(of: Trigger): JSONObject = JSONObject().apply {
        // The moment is written even where it is deduced: reading it back is then one rule
        // and not a table of which kinds carry it.
        put("moment", of.moment.name)
        when (of) {
            is Trigger.Clock ->
                put("kind", "clock").put("which", of.which.name).put("ms", of.ms)
            is Trigger.Node ->
                put("kind", "node").put("path", of.path)
                    .put("reads", of.reads.name).put("value", of.value)
            is Trigger.Passages ->
                put("kind", "passages")
                    .put("at", of.at ?: JSONObject.NULL)
                    .put("every", of.every ?: JSONObject.NULL)
            is Trigger.Judged -> put("kind", "judged").put("prose", of.prose)
            is Trigger.Moved ->
                put("kind", "moved").put("key", of.key).put("harder", of.harder)
            is Trigger.Reaches ->
                put("kind", "reaches").put("key", of.key).put("position", position(of.position))
            // The two that bound the sitting carry nothing but their name: they test nothing,
            // and their moment is deduced like the clock's.
            is Trigger.Opening -> put("kind", "opening")
            is Trigger.Closing -> put("kind", "closing")
        }
    }

    private fun trigger(json: JSONObject): Trigger {
        val moment = Moment.valueOf(json.getString("moment"))
        return when (val kind = json.getString("kind")) {
            "clock" -> Trigger.Clock(
                Trigger.Clock.Which.valueOf(json.getString("which")), json.getInt("ms"),
            )
            "node" -> Trigger.Node(
                json.getString("path"),
                Trigger.Node.Reads.valueOf(json.getString("reads")),
                json.getString("value"),
                moment,
            )
            "passages" -> Trigger.Passages(
                at = if (json.isNull("at")) null else json.getInt("at"),
                every = if (json.isNull("every")) null else json.getInt("every"),
            )
            "judged" -> Trigger.Judged(json.getString("prose"), moment)
            "moved" -> Trigger.Moved(json.getString("key"), json.getBoolean("harder"), moment)
            "reaches" -> Trigger.Reaches(
                json.getString("key"), position(json.getJSONObject("position")), moment,
            )
            "opening" -> Trigger.Opening
            "closing" -> Trigger.Closing
            else -> error("'$kind' is not a kind of trigger this build knows")
        }
    }

    // ── Effects ─────────────────────────────────────────────────────────────────────────

    private fun effect(of: Effect): JSONObject = when (of) {
        is Effect.Patch -> JSONObject()
            .put("kind", "patch")
            .put("positions", JSONObject().apply {
                of.positions.forEach { (key, at) -> put(key, position(at)) }
            })
            .put("moves", JSONObject().apply { of.moves.forEach { (k, by) -> put(k, by) } })
            .put("instructions", JSONArray().apply {
                of.instructions.forEach { put(instructing(it)) }
            })
            .put("arming", JSONObject().apply { of.arming.forEach { (k, on) -> put(k, on) } })
            .put("staging", of.staging?.let {
                JSONObject().put("text", it.text).put("before", it.before)
            } ?: JSONObject.NULL)
        is Effect.Finish -> JSONObject().put("kind", "finish").put("outcome", of.outcome.name)
        is Effect.Message ->
            JSONObject().put("kind", "message").put("prose", of.prose).put("now", of.now)
    }

    private fun effect(json: JSONObject): Effect = when (val kind = json.getString("kind")) {
        "patch" -> Effect.Patch(
            positions = json.getJSONObject("positions").let { held ->
                held.keys().asSequence().associateWith { position(held.getJSONObject(it)) }
            },
            moves = json.getJSONObject("moves").let { held ->
                held.keys().asSequence().associateWith { held.getInt(it) }
            },
            instructions = json.getJSONArray("instructions").objects().map { instructing(it) },
            arming = json.getJSONObject("arming").let { held ->
                held.keys().asSequence().associateWith { held.getBoolean(it) }
            },
            staging = if (json.isNull("staging")) null else json.getJSONObject("staging").let {
                Staging(it.getString("text"), it.getBoolean("before"))
            },
        )
        "finish" -> Effect.Finish(Outcome.valueOf(json.getString("outcome")))
        "message" -> Effect.Message(json.getString("prose"), json.getBoolean("now"))
        else -> error("'$kind' is not a kind of effect this build knows")
    }

    private fun instructing(of: Instructing): JSONObject = JSONObject()
        .put("marking", of.marking)
        .put("text", of.text ?: JSONObject.NULL)
        .put("lasts", of.lasts ?: JSONObject.NULL)

    private fun instructing(json: JSONObject) = Instructing(
        marking = json.getString("marking"),
        text = if (json.isNull("text")) null else json.getString("text"),
        lasts = if (json.isNull("lasts")) null else json.getInt("lasts"),
    )

    /**
     * A position: a step's name, or a number of which **no maximum** is null.
     *
     * Wrapped in an object rather than written bare, so *no maximum* stays a position and does
     * not read as a key that is not there.
     */
    private fun position(of: Position): JSONObject = when (of) {
        is At -> JSONObject().put("at", of.name)
        is Count -> JSONObject().put("count", of.n ?: JSONObject.NULL)
    }

    private fun position(json: JSONObject): Position =
        if (json.has("at")) At(json.getString("at"))
        else Count(if (json.isNull("count")) null else json.getInt("count"))

    private fun JSONArray.objects(): List<JSONObject> =
        (0 until length()).map { getJSONObject(it) }

    private fun JSONArray.arrays(): List<JSONArray> = (0 until length()).map { getJSONArray(it) }
}
