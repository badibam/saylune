package app.saylune.scene

import app.saylune.activity.Caution
import app.saylune.activity.Door
import app.saylune.activity.Text
import app.saylune.conversation.Speaker
import app.saylune.levers.Positions
import app.saylune.notes.Weights
import app.saylune.store.Sitting
import org.json.JSONArray
import org.json.JSONObject

/**
 * A scene written in advance, as a file shipped with the app (`docs/design/scene-state.md`,
 * "La fiche et l'histoire").
 *
 * **What a file declares never moves during play** -- the title, the situation, the cast,
 * where the levers start, the weights, the warnings -- and **what moves is cases and events**.
 * Passing the first off as events sent at the opening *for the whole play* would hide a
 * brief under a lifetime invented for it.
 *
 * **The format is this reader and nothing else** (`docs/scene-authoring.md`): whoever writes a
 * scene reads the code, and a shipped scene is the example of the shape.
 */
data class SceneFile(
    val id: String,
    /** The release's, inherited for nothing. */
    val version: String,
    /** Where it is offered. Read by the screens and by [SceneFiles.check], never by the engine. */
    val door: Door,
    val title: Text,
    /** Ten characters at most, what the status line has room for. */
    val short: Text,
    /**
     * Where the learner stands, in the second person and in his language, read on screen. It
     * may cite a case -- the hole he filled at the launch.
     */
    val situation: Text,
    val cast: List<Role>,
    val cases: List<Case>,
    /** Where the levers start. Always empty behind the free door. */
    val levers: Positions,
    val weights: Weights,
    /** Every event, the ones unfolded from [questions] included. */
    val events: List<Event>,
    /** The questions as they were written, which the pipeline reads to put and to put again. */
    val questions: List<Question>,
    val cautions: List<Caution>,
) {
    /** The one the tile shows, or null where nobody stands out. */
    val face: Role? get() = cast.firstOrNull { it.main }
}

/**
 * Someone the leader plays. **Its description belongs to it** and goes to the leader whenever it
 * is present -- in English, never shown.
 *
 * Its voice and its gender are not cases: they are fixed by the file, else asked, else drawn,
 * once, and kept with it. No event tests them.
 */
data class Role(
    val key: String,
    val short: Text,
    val about: String,
    val main: Boolean = false,
    val gender: String? = null,
)

/**
 * A question to the learner, as written: **a case and four or five events, written in one go**,
 * which the loader unfolds and the engine never sees as such.
 *
 * Asked by the narrator or a character in a line written in advance ([text]), or improvised by
 * the leader on a [direction]. [answers] is null where the answer is words -- a name. [none] is
 * the value that says there was no answer; [again] is how many times the question is put again
 * before that is the answer, null without limit, which blocks.
 */
data class Question(
    val key: String,
    val askedBy: String,
    val text: Text?,
    val direction: String?,
    val answers: List<String>?,
    val none: String,
    val again: Int?,
) {
    init {
        require((text == null) != (direction == null)) { "$key: a line written, or a direction" }
        require((askedBy == LEADER) == (direction != null)) {
            "$key: the leader improvises on a direction, anybody else says a written line"
        }
        require(again == null || again >= 0) { "$key: put again $again times" }
    }

    companion object {
        /** Who improvises a question it is told to put. */
        const val LEADER = "leader"
    }
}

/**
 * The files, read, unfolded and checked.
 *
 * **Every problem fails the loading.** The test that loads every shipped file runs without a
 * device, so a broken file does not pass the test and does not ship; and a warning is never
 * read, which leaves the learner to find the mistake by playing.
 */
object SceneFiles {

    /** What an instruction attaches to: the three judged markings that take one. */
    val MARKINGS = setOf("spans", "stumbling", "following")

    fun parse(id: String, version: String, json: String): SceneFile {
        val file = JSONObject(json)
        known(file, "file", "id", "door", "title", "short", "situation", "cast", "cases", "levers",
              "weights", "events", "questions", "cautions")
        require(file.getString("id") == id) { "$id: the file calls itself ${file.getString("id")}" }
        val door = when (val at = file.getString("door")) {
            "story" -> Door.Story
            "challenges" -> Door.Challenges
            "arcade" -> Door.Arcade
            "free" -> Door.Free
            else -> error("$id: '$at' is not a door this build knows")
        }
        val cases = file.optJSONArray("cases").objects().map { case(it) }
        val declared = cases.associateBy { it.key }
        val questions = file.optJSONArray("questions").objects().map { question(it) }
        val unfolded = questions.map { unfold(it, file.optJSONArray("questions").objects()
            .first { q -> q.getString("key") == it.key }, declared) }
        val all = cases + unfolded.flatMap { it.first }
        val events = file.optJSONArray("events").objects().map { event(it, all.associateBy { c -> c.key }) } +
            unfolded.flatMap { it.second }
        val weights = file.optJSONObject("weights")
        return SceneFile(
            id = id,
            version = version,
            door = door,
            title = text(file.get("title")),
            short = text(file.get("short")),
            situation = text(file.get("situation")),
            cast = file.optJSONArray("cast").objects().map {
                known(it, "cast", "key", "short", "about", "main", "gender")
                Role(
                    key = it.getString("key"),
                    short = text(it.get("short")),
                    about = it.getString("about"),
                    main = it.optBoolean("main"),
                    gender = it.optString("gender").ifEmpty { null },
                )
            },
            cases = all,
            levers = Sitting.readPositions(file.optJSONObject("levers")?.toString() ?: "{}"),
            // **Behind the free door the app puts 1 everywhere**, and a file declares none.
            weights = if (weights == null) Weights(AppCases.treePaths().associateWith { 1f })
            else Weights(weights.keys().asSequence().associateWith { weights.getDouble(it).toFloat() }),
            events = events,
            questions = questions,
            cautions = file.optJSONArray("cautions")?.let { declaredCautions ->
                (0 until declaredCautions.length()).map { Caution.of(declaredCautions.getString(it)) }
            } ?: emptyList(),
        ).also { check(it, declaredWeights = weights != null) }
    }

    // ── Cases ───────────────────────────────────────────────────────────────────────────

    private fun case(json: JSONObject): Case {
        known(json, "case", "key", "kind", "min", "max", "values", "ordered", "start", "about", "hidden")
        val kind = when (val sort = json.getString("kind")) {
            "flag" -> Kind.Flag
            "number" -> Kind.Number(
                min = if (json.has("min")) json.getDouble("min") else null,
                max = if (json.has("max")) json.getDouble("max") else null,
            )
            "list" -> Kind.Choice(
                json.getJSONArray("values").let { v -> (0 until v.length()).map { v.getString(it) } },
                ordered = json.optBoolean("ordered"),
            )
            "text" -> Kind.Words
            else -> error("'$sort' is not a kind of case this build knows")
        }
        return Case(
            key = json.getString("key"),
            kind = kind,
            start = if (json.has("start")) value(kind, json.get("start")) else null,
            about = json.optString("about").ifEmpty { null },
            hidden = json.optBoolean("hidden"),
        )
    }

    /** A value written in a file, read by what the case holds. */
    private fun value(kind: Kind, raw: Any): Value = when (kind) {
        Kind.Flag -> Value.Flag(raw as? Boolean ?: error("'$raw' is not true or false"))
        is Kind.Number -> Value.Num(if (raw == JSONObject.NULL) null
            else (raw as? Number)?.toDouble() ?: error("'$raw' is not a number"))
        is Kind.Choice -> Value.Pick(raw as? String ?: error("'$raw' is not one of ${kind.among}"))
        Kind.Words -> Value.Words(raw as? String ?: error("'$raw' is not words"))
    }

    // ── Events ──────────────────────────────────────────────────────────────────────────

    private fun event(json: JSONObject, declared: Map<String, Case>): Event {
        known(json, "event", "key", "at", "test", "do", "active")
        val key = json.getString("key")
        return try {
            Event(
                key = key,
                at = moment(json.getString("at")),
                test = json.optJSONObject("test")?.let { test(it, declared) } ?: Test.None,
                effects = json.getJSONArray("do").objects().map { effect(it, declared) },
                active = json.optBoolean("active", true),
            )
        } catch (e: IllegalStateException) {
            throw IllegalArgumentException("event $key: ${e.message}", e)
        }
    }

    private fun moment(name: String): Moment = MOMENTS[name]
        ?: error("'$name' is not a moment this build knows")

    private val MOMENTS = mapOf(
        "launch" to Moment.Launch, "opening" to Moment.Opening, "recording" to Moment.Recording,
        "answer" to Moment.Answer, "attempt-end" to Moment.AttemptEnd,
        "passage-close" to Moment.PassageClose, "closing" to Moment.Closing,
    )

    private val OPS = mapOf(
        "=" to Op.Eq, "!=" to Op.Ne, "<" to Op.Lt, "<=" to Op.Le, ">" to Op.Gt, ">=" to Op.Ge,
    )

    /**
     * A test, one of five shapes: `{"case", "op", "value"}` a state, `{"case", "every"}` a
     * number's multiple, `{"case", "empty": true}`, `{"case", "becomes", "value"}` a change into
     * a state, `{"case", "goes"}` a change by its direction.
     */
    private fun test(json: JSONObject, declared: Map<String, Case>): Test {
        known(json, "test", "case", "op", "value", "every", "empty", "becomes", "goes")
        val case = json.getString("case")
        fun against() = value(kindOf(case, declared), json.get("value"))
        return when {
            json.has("op") -> Test.Holds(case, op(json.getString("op")), against())
            json.has("every") -> Test.Every(case, json.getInt("every"))
            json.optBoolean("empty") -> Test.Empty(case)
            json.has("becomes") -> Test.Becomes(case, op(json.getString("becomes")), against())
            json.has("goes") -> Test.Goes(case, way(json.getString("goes")))
            else -> error("$case: a test that says nothing")
        }
    }

    private fun op(name: String) = OPS[name] ?: error("'$name' is not a comparison")

    private fun way(name: String) = when (name) {
        "up" -> Way.Up
        "down" -> Way.Down
        "harder" -> Way.Harder
        "easier" -> Way.Easier
        else -> error("'$name' is not a way a case goes")
    }

    /** One effect, named by its verb: the one key of its object that is a verb. */
    private fun effect(json: JSONObject, declared: Map<String, Case>): Effect {
        val verbs = json.keys().asSequence().filter { it in VERBS }.toList()
        require(verbs.size == 1) { "an effect names one verb among $VERBS, not $verbs" }
        return when (val verb = verbs.single()) {
            "put" -> {
                known(json, verb, "put", "value")
                val case = json.getString("put")
                Effect.Put(case, value(kindOf(case, declared), json.get("value")))
            }
            "shift" -> known(json, verb, "shift", "by").let {
                Effect.Shift(json.getString("shift"), json.getDouble("by"))
            }
            "step" -> known(json, verb, "step", "way").let {
                Effect.Step(json.getString("step"), way(json.getString("way")))
            }
            "activate" -> known(json, verb, "activate").let {
                Effect.Switch(json.getString("activate"), true)
            }
            "deactivate" -> known(json, verb, "deactivate").let {
                Effect.Switch(json.getString("deactivate"), false)
            }
            "finish" -> known(json, verb, "finish").let {
                Effect.Finish(when (val how = json.getString("finish")) {
                    "passed" -> Outcome.Passed
                    "failed" -> Outcome.Failed
                    "by-note" -> Outcome.ByNote
                    else -> error("'$how' is not an outcome")
                })
            }
            "tell" -> {
                known(json, verb, "tell", "to", "said-by")
                val reader = when (val to = json.getString("to")) {
                    "leader" -> Reader.Leader
                    "learner" -> Reader.Learner
                    "both" -> Reader.Both
                    else -> error("'$to' is not a reader")
                }
                Effect.Tell(
                    text(json.get("tell")), reader,
                    json.optString("said-by").ifEmpty { null }?.let { Form.Thread(it) } ?: Form.Notice,
                )
            }
            "instruct" -> {
                known(json, verb, "instruct", "marking", "text", "hidden", "lasts")
                Effect.Instruct(
                    key = json.getString("instruct"),
                    marking = json.getString("marking"),
                    text = text(json.get("text")),
                    hidden = json.optBoolean("hidden"),
                    lasts = if (json.has("lasts")) json.getInt("lasts") else null,
                )
            }
            "lift" -> known(json, verb, "lift").let { Effect.Lift(json.getString("lift")) }
            "direct" -> known(json, verb, "direct").let { Effect.Direct(json.getString("direct")) }
            "ask-leader" -> {
                known(json, verb, "ask-leader", "reach")
                Effect.AskLeader(json.getString("ask-leader"), when (val far = json.getString("reach")) {
                    "said" -> Reach.Said
                    "deduce" -> Reach.Deduce
                    "invent" -> Reach.Invent
                    else -> error("'$far' is not how far the leader may go")
                })
            }
            "ask-learner" -> known(json, verb, "ask-learner", "ask").let {
                Effect.AskLearner(json.getString("ask-learner"), text(json.get("ask")))
            }
            "draw" -> {
                known(json, verb, "draw", "weights")
                Effect.Draw(json.getString("draw"), json.optJSONObject("weights")?.let { w ->
                    w.keys().asSequence().associateWith { w.getInt(it) }
                } ?: emptyMap())
            }
            else -> error("unreachable")
        }
    }

    private val VERBS = setOf(
        "put", "shift", "step", "activate", "deactivate", "finish", "tell", "instruct", "lift",
        "direct", "ask-leader", "ask-learner", "draw",
    )

    // ── Questions ───────────────────────────────────────────────────────────────────────

    private fun question(json: JSONObject): Question {
        known(json, "question", "key", "asked-by", "at", "test", "text", "direction", "answers",
              "none", "again", "about")
        val answers = json.opt("answers")
        return Question(
            key = json.getString("key"),
            askedBy = json.getString("asked-by"),
            text = json.opt("text")?.let { text(it) },
            direction = json.optString("direction").ifEmpty { null },
            answers = when (answers) {
                "text" -> null
                is JSONArray -> (0 until answers.length()).map { answers.getString(it) }
                else -> error("${json.getString("key")}: answers are a list, or \"text\"")
            },
            none = json.getString("none"),
            again = if (json.isNull("again")) null else json.getInt("again"),
        )
    }

    /**
     * A question, unfolded into its case, its counter and its events.
     *
     * - `<key>.ask`, at the written moment: says the question, arms the counter, and holds the
     *   passage until the answer.
     * - `<key>.took`, at the answer, on anything but *no answer*: lets the passage go.
     * - `<key>.again`, on *no answer* while it may be asked again: asks again, counting down.
     * - `<key>.last`, on *no answer* once it may not: lets the passage go, *no answer* being
     *   the answer, which the author's own events read.
     * - `<key>.spent`, when the counter reaches zero: hands over from the second to the third,
     *   activation standing for the "and" one test cannot say.
     */
    private fun unfold(
        question: Question, json: JSONObject, declared: Map<String, Case>,
    ): Pair<List<Case>, List<Event>> {
        val key = question.key
        val left = "$key.left"
        val none = if (question.answers == null) Value.Words(question.none) else Value.Pick(question.none)
        val asked = question.text?.inLanguage(Text.BASE) ?: question.direction!!
        val cases = listOfNotNull(
            Case(
                key = key,
                kind = question.answers?.let { Kind.Choice(it + question.none) } ?: Kind.Words,
                about = json.optString("about").ifEmpty { "Their answer to: $asked" },
            ),
            question.again?.takeIf { it > 0 }?.let {
                Case(left, Kind.Number(0.0, it.toDouble()), hidden = true)
            },
        )
        val limited = question.again != null && question.again > 0
        val all = declared + cases.associateBy { it.key }
        val asking = buildList {
            add(question.text?.let { Effect.Tell(it, Reader.Both, Form.Thread(question.askedBy)) }
                ?: Effect.Direct(question.direction!!))
            add(Effect.Pose(key))
            if (limited) {
                add(Effect.Put(left, Value.Num(question.again!!.toDouble())))
                add(Effect.Switch("$key.again", true))
                add(Effect.Switch("$key.last", false))
            }
        }
        val events = listOfNotNull(
            Event("$key.ask", moment(json.getString("at")),
                  json.optJSONObject("test")?.let { test(it, all) } ?: Test.None, asking),
            Event("$key.took", Moment.Answer, Test.Holds(key, Op.Ne, none),
                  listOf(Effect.Release(key)), question = key),
            Event("$key.again", Moment.Answer, Test.Holds(key, Op.Eq, none),
                  listOfNotNull(Effect.Reask(key), if (limited) Effect.Shift(left, -1.0) else null),
                  active = question.again != 0, question = key),
            if (question.again == null) null else Event(
                "$key.last", Moment.Answer, Test.Holds(key, Op.Eq, none),
                listOf(Effect.Release(key)), active = question.again == 0, question = key,
            ),
            if (!limited) null else Event(
                "$key.spent", Moment.Answer, Test.Becomes(left, Op.Eq, Value.Num(0.0)),
                listOf(Effect.Switch("$key.again", false), Effect.Switch("$key.last", true)),
                question = key,
            ),
        )
        return cases to events
    }

    // ── Citations ───────────────────────────────────────────────────────────────────────

    /** `{case}` or `{case | what to write when it is empty}`. */
    private val CITATION = Regex("""\{([A-Za-z0-9_.\-/]+)(\s*\|([^}]*))?\}""")

    /** The cases [text] cites, each with whether it says what to write when empty. */
    fun citations(text: String): List<Pair<String, Boolean>> =
        CITATION.findAll(text).map { it.groupValues[1] to it.groups[2].let { g -> g != null } }.toList()

    /** [text] with the value of each case it cites at the moment it leaves. */
    fun cite(text: String, read: (String) -> Value?): String = CITATION.replace(text) { found ->
        when (val value = read(found.groupValues[1])) {
            null -> found.groups[3]?.value?.trim()
                ?: error("${found.groupValues[1]} is empty and the text says nothing to write")
            is Value.Flag -> if (value.on) "yes" else "no"
            is Value.Num -> value.n?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                ?: "no maximum"
            is Value.Pick -> value.name
            is Value.Words -> value.text
        }
    }

    // ── The check ───────────────────────────────────────────────────────────────────────

    /**
     * What a scene has to hold together, checked at loading (`scene-state.md`, "La
     * vérification"). **Every problem is listed, then the loading fails**: fixing one to meet
     * the next is the slow way through a file.
     */
    fun check(scene: SceneFile, declaredWeights: Boolean) {
        val problems = mutableListOf<String>()
        fun say(line: String) { problems += line }
        val declared = scene.cases.associateBy { it.key }
        fun kind(key: String): Kind? = runCatching { kindOf(key, declared) }.getOrNull()
        val speakers = scene.cast.map { it.key }.toSet() + Speaker.NARRATOR +
            (if (scene.cast.isEmpty()) setOf(Speaker.SAYLUNE) else emptySet())
        val events = scene.events.associateBy { it.key }
        if (events.size != scene.events.size) say("two events share a key")
        if (declared.size != scene.cases.size) say("two cases share a key")
        if (scene.cast.count { it.main } > 1) say("two characters called the main one")
        if (scene.short.byLanguage.values.any { it.length > 10 }) say("a short name over ten characters")

        // Who writes what: a case nobody writes and that has no start can never hold anything.
        val written = scene.events.flatMap { event ->
            event.effects.mapNotNull {
                when (it) {
                    is Effect.Put -> it.case
                    is Effect.Shift -> it.case
                    is Effect.Step -> it.case
                    is Effect.Draw -> it.case
                    is Effect.AskLeader -> it.case
                    is Effect.AskLearner -> it.case
                    else -> null
                }
            }
        }.toSet() + scene.questions.map { it.key }
        scene.cases.forEach { case ->
            if (case.start == null && case.key !in written) {
                say("${case.key}: nobody writes it and it has no starting value")
            }
            if (case.kind is Kind.Choice && case.kind.among.toSet() == setOf("yes", "no")) {
                say("${case.key}: a list of yes and no is a flag")
            }
            if (!case.hidden && case.about == null) say("${case.key}: known to the leader, and no line about it")
        }

        val leaderTexts = mutableListOf<String>()
        scene.events.forEach { event ->
            val where = "event ${event.key}"
            event.test.case?.let { case ->
                val k = kind(case)
                when {
                    k == null -> say("$where: tests $case, which nobody declares")
                    k is Kind.Words && event.question == null -> say("$where: tests the words of $case")
                    family(case) == Family.App && !AppCases.existsAt(case, event.at) ->
                        say("$where: $case does not exist at ${event.at}")
                }
                val test = event.test
                val (op, value) = when (test) {
                    is Test.Holds -> test.op to test.value
                    is Test.Becomes -> test.op to test.value
                    else -> null to null
                }
                if (k != null && value != null && !k.holds(value)) say("$where: $case cannot hold $value")
                if (k != null && op != null && op != Op.Eq && op != Op.Ne &&
                    !(k is Kind.Number || (k is Kind.Choice && k.ordered))) {
                    say("$where: $case has no order to compare by")
                }
                if (test is Test.Every && k !is Kind.Number) say("$where: every n of a non-number")
                if (test is Test.Goes) {
                    val lever = family(case) == Family.Lever
                    if ((test.way == Way.Harder || test.way == Way.Easier) && !lever) {
                        say("$where: only a lever gets harder or easier")
                    }
                    if ((test.way == Way.Up || test.way == Way.Down) &&
                        !(k is Kind.Number || (k is Kind.Choice && k.ordered))) {
                        say("$where: $case has no way up")
                    }
                }
            }
            event.effects.forEach { effect ->
                when (effect) {
                    is Effect.Put -> writes(effect.case, kind(effect.case), where, ::say)
                        ?.let { if (!it.holds(effect.value)) say("$where: ${effect.case} cannot hold ${effect.value}") }
                    is Effect.Shift -> writes(effect.case, kind(effect.case), where, ::say)
                        ?.let { if (it !is Kind.Number) say("$where: shifts ${effect.case}, not a number") }
                    is Effect.Step -> writes(effect.case, kind(effect.case), where, ::say)?.let {
                        if (!(it is Kind.Choice && it.ordered)) say("$where: steps ${effect.case}, a list with no order")
                        val lever = family(effect.case) == Family.Lever
                        if (lever != (effect.way == Way.Harder || effect.way == Way.Easier)) {
                            say("$where: a lever steps harder or easier, a case of the file up or down")
                        }
                    }
                    is Effect.Draw -> writes(effect.case, kind(effect.case), where, ::say)?.let {
                        if (it !is Kind.Choice) say("$where: draws ${effect.case}, which has no values")
                        else if (!it.among.containsAll(effect.weights.keys)) say("$where: weighs a value ${effect.case} lacks")
                    }
                    is Effect.Switch -> if (effect.event !in events) say("$where: switches ${effect.event}, which does not exist")
                    is Effect.Tell -> {
                        if (effect.reader != Reader.Learner) leaderTexts += effect.text.inLanguage(Text.BASE)
                        (effect.form as? Form.Thread)?.let { form ->
                            if (form.who !in speakers) say("$where: a line said by '${form.who}', who is not in the scene")
                            speaks(event, where, ::say)
                        }
                        effect.text.byLanguage.values.forEach { cites(it, declared, where, ::say) }
                    }
                    is Effect.Direct -> {
                        leaderTexts += effect.prose
                        speaks(event, where, ::say)
                        cites(effect.prose, declared, where, ::say)
                    }
                    is Effect.Instruct -> {
                        if (effect.marking !in MARKINGS) say("$where: an instruction on '${effect.marking}', not one of $MARKINGS")
                        if (!effect.hidden) leaderTexts += effect.text.inLanguage(Text.BASE)
                    }
                    is Effect.AskLeader -> {
                        if (kind(effect.case) == null) say("$where: asks for ${effect.case}, which nobody declares")
                        if (family(effect.case) != Family.File) say("$where: the leader writes cases of the file alone")
                        if (declared[effect.case]?.hidden == true) say("$where: asks the leader for ${effect.case}, which is hidden from it")
                    }
                    is Effect.AskLearner -> {
                        if (event.at != Moment.Launch) say("$where: the learner is asked at the launch only")
                        if (family(effect.case) != Family.File) say("$where: the learner fills cases of the file alone")
                    }
                    is Effect.Finish, is Effect.Lift, is Effect.Pose, is Effect.Release, is Effect.Reask -> Unit
                }
            }
        }
        leaderTexts.forEach { text ->
            citations(text).forEach { (case, _) ->
                if (declared[case]?.hidden == true) say("a text for the leader cites $case, which is hidden from it")
            }
        }
        scene.situation.byLanguage.values.forEach { cites(it, declared, "the situation", ::say) }
        scene.questions.forEach { question ->
            if (question.askedBy != Question.LEADER && question.askedBy !in speakers) {
                say("question ${question.key}: asked by '${question.askedBy}', who is not in the scene")
            }
            if (question.answers?.contains(question.none) == true) {
                say("question ${question.key}: '${question.none}' is both an answer and no answer")
            }
        }

        if (scene.door == Door.Free) free(scene, declaredWeights, ::say)
        require(problems.isEmpty()) { "${scene.id}:\n" + problems.joinToString("\n") { "- $it" } }
    }

    /**
     * **Behind the free door there is nothing at stake**, and the settings are the learner's:
     * no lever, no instruction, no weight, no end, no test on how he speaks -- the passage alone,
     * which paces the story and judges nothing -- and one hole at most.
     */
    private fun free(scene: SceneFile, declaredWeights: Boolean, say: (String) -> Unit) {
        if (scene.levers.all().isNotEmpty()) say("free: a lever set at the start")
        if (declaredWeights) say("free: weights declared, where the app puts 1 everywhere")
        var holes = 0
        scene.events.forEach { event ->
            event.test.case?.let {
                if (family(it) == Family.App && it != AppCases.PASSAGE) say("free: event ${event.key} reads $it")
            }
            event.effects.forEach { effect ->
                when (effect) {
                    is Effect.Instruct -> say("free: event ${event.key} lays an instruction")
                    is Effect.Finish -> say("free: event ${event.key} ends the scene")
                    is Effect.AskLearner -> holes++
                    is Effect.Put -> if (family(effect.case) == Family.Lever) say("free: event ${event.key} moves a lever")
                    is Effect.Shift -> if (family(effect.case) == Family.Lever) say("free: event ${event.key} moves a lever")
                    is Effect.Step -> if (family(effect.case) == Family.Lever) say("free: event ${event.key} moves a lever")
                    else -> Unit
                }
            }
        }
        if (holes > 1) say("free: $holes holes, where one is an invitation and two a form")
    }

    /** The kind of a case an event writes, or null after saying why it may not. */
    private fun writes(case: String, kind: Kind?, where: String, say: (String) -> Unit): Kind? {
        if (kind == null) { say("$where: writes $case, which nobody declares"); return null }
        if (family(case) == Family.App) { say("$where: writes $case, which the app writes alone"); return null }
        return kind
    }

    /** A line or a turn is never made while somebody speaks, nor at the end of an attempt. */
    private fun speaks(event: Event, where: String, say: (String) -> Unit) {
        if (event.at in setOf(Moment.Launch, Moment.Recording, Moment.AttemptEnd)) {
            say("$where: makes somebody speak at ${event.at}")
        }
    }

    private fun cites(text: String, declared: Map<String, Case>, where: String, say: (String) -> Unit) =
        citations(text).forEach { (case, fallback) ->
            val known = runCatching { kindOf(case, declared) }.isSuccess
            if (!known) say("$where: cites $case, which nobody declares")
            else if (!fallback && family(case) == Family.File && declared[case]?.start == null) {
                say("$where: cites $case, which has no starting value, without saying what to write when it is empty")
            }
        }

    // ── JSON ────────────────────────────────────────────────────────────────────────────

    /** A text in its languages, or a bare string for English alone -- a line for the leader. */
    private fun text(raw: Any): Text = when (raw) {
        is String -> Text(mapOf(Text.BASE to raw))
        is JSONObject -> Text(raw.keys().asSequence().associateWith { raw.getString(it) })
        else -> error("'$raw' is not a text")
    }

    /** **An unknown field fails outright**: a field the code does not read is not written. */
    private fun known(json: JSONObject, what: String, vararg fields: String) {
        val stray = json.keys().asSequence().filter { it !in fields }.toList()
        require(stray.isEmpty()) { "$what: ${stray.joinToString()} is not a field this build reads" }
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }
}
