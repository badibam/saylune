package app.speakup.activity

import android.content.Context
import app.speakup.BuildConfig
import app.speakup.store.Rules
import app.speakup.store.Sitting
import org.json.JSONArray
import org.json.JSONObject

/**
 * Where definitions are read from, which today is the files shipped with the app.
 *
 * **Importing the files into the store is ruled out**: the table would be the copy of a source
 * already on the disk, readable and versioned, so the second source that drifts which the
 * project refuses everywhere -- plus a reconciliation at every update of the app.
 *
 * **One interface, two sources, zero copy.** What the model makes up on the fly is not a
 * definition: a character invented during a sitting fills the fields of an activity, so a row
 * and not a template. It becomes a definition the day one wants to **replay** it, and that day,
 * named, opens the table -- beside the files, this reader uniting the two at the read.
 */
object Definitions {

    /** The free conversation, which is a delivered definition like any other. */
    const val FREE_CONVERSATION = "free-conversation"

    private const val FOLDER = "definitions"

    /** Every definition the app ships with, in no particular order. */
    fun all(context: Context, version: String = BuildConfig.VERSION_NAME): List<Definition> =
        context.assets.list(FOLDER).orEmpty()
            .filter { it.endsWith(SUFFIX) }
            .map { of(context, it.removeSuffix(SUFFIX), version) }

    /**
     * The definition called [id].
     *
     * **An id nobody ships fails outright.** A definition is named by an origin that was
     * written down, so one that cannot be found is a release that dropped a file, and a sitting
     * carrying on under a substitute would be a sitting nobody can read back.
     */
    fun of(
        context: Context,
        id: String,
        version: String = BuildConfig.VERSION_NAME,
    ): Definition = parse(
        id = id,
        version = version,
        json = context.assets.open("$FOLDER/$id$SUFFIX").use { it.readBytes().decodeToString() },
    )

    /**
     * One file, read.
     *
     * **The file is shaped exactly as the store writes**, so the readers of a sitting's own
     * columns read it as it stands -- `Sitting` for the positions, the weights and the
     * instructions, `Rules` for the rules. Nothing is emitted by hand: a model writing against
     * the catalogue, or a form, and both do better copying back the very strings the catalogue
     * declares than filling a shape invented for a reader's eye.
     */
    fun parse(id: String, version: String, json: String): Definition {
        val file = JSONObject(json)
        require(file.getString("id") == id) {
            "$id: the file calls itself ${file.getString("id")}"
        }
        return Definition(
            id = id,
            version = version,
            title = text(file.getJSONObject("title")),
            short = text(file.getJSONObject("short")),
            // **Required, and an unknown name fails outright**, like the rung and like the
            // moments: a file that does not say where it is offered is a file nobody can
            // place, and a silent default would file it somewhere without saying so.
            door = when (val at = file.getString("door")) {
                "story" -> Door.Story
                "challenges" -> Door.Challenges
                "arcade" -> Door.Arcade
                "free" -> Door.Free
                else -> error("$id: '$at' is not a door this build knows")
            },
            brief = file.optJSONObject("brief")?.let { Sitting.readBrief(it.toString()) },
            cast = file.optJSONArray("cast").objects().map {
                Character(
                    key = it.getString("key"),
                    short = text(it.getJSONObject("short")),
                    main = it.optBoolean("main"),
                    gender = it.optString("gender").takeIf { said -> said.isNotEmpty() },
                )
            },
            slots = file.optJSONArray("slots").objects().map {
                Slot(it.getString("key"), text(it.getJSONObject("ask")))
            },
            settings = Sitting.readPositions(file.optJSONObject("settings")?.toString() ?: "{}"),
            weights = Sitting.readWeights(file.getJSONObject("weights").toString()),
            instructions = Sitting.readInstructions(
                file.optJSONArray("instructions")?.toString() ?: "[]",
            ),
            rules = Rules.read(file.optJSONArray("rules")?.toString() ?: "[]"),
            questions = file.optJSONArray("questions").objects().map { question(it) },
        ).also { validate(it) }
    }

    /**
     * What a definition has to hold together, checked before anything runs on it.
     *
     * **A stub: the call site exists and it checks nothing.** The doc repeats that a thing
     * *"shows at the writing, without executing"* -- two patches of one wave on the same key, a
     * missing staging line, a series with impossible edges, a narrative direction contradicting
     * the mechanical one -- and names nobody who looks. Until someone does, all of those
     * happen at run time, on a learner (`../../../../../../TODO.md`).
     *
     * What already fails without it is what a type can say: a title over its limit, a character
     * taking the learner's key, a lever position the catalogue does not declare, a weight on a
     * path that is not in the tree.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun validate(definition: Definition) = Unit

    private fun text(json: JSONObject) = Text(
        json.keys().asSequence().associateWith { json.getString(it) },
    )

    private fun question(json: JSONObject) = Question(
        key = json.getString("key"),
        ask = json.getString("ask"),
        answers = when (val kind = json.getString("answers")) {
            "free" -> Answers.Free
            // Each option is an author's text, so it carries its own table of languages; the
            // English is the key, being what goes out and what comes back.
            "one-of" -> Answers.OneOf(
                json.getJSONArray("among").let { among ->
                    (0 until among.length()).map { text(among.getJSONObject(it)) }
                },
            )
            else -> error("'$kind' is not a shape of answer this build knows")
        },
        // The moments are triggers, read by the same reader a rule's is: an unknown name fails
        // outright rather than being dropped, a question served at another moment than the one
        // written being a question put somewhere nobody asked for it.
        moments = Rules.readTriggers(json.getJSONArray("when").toString()),
        rung = when (val far = json.optString("rung").ifEmpty { "from-the-talk" }) {
            "from-the-talk" -> Rung.FromTheTalk
            "may-extrapolate" -> Rung.MayExtrapolate
            "may-invent" -> Rung.MayInvent
            else -> error("'$far' is not a rung this build knows")
        },
        shown = json.optBoolean("shown"),
    )

    private const val SUFFIX = ".json"

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }
}
