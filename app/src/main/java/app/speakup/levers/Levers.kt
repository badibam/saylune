package app.speakup.levers

import app.speakup.R
import app.speakup.sheets.POSITIONS
import app.speakup.sheets.SERIES_LENGTH
import app.speakup.sheets.Sheets

/**
 * Every lever the app has, declared in one place.
 *
 * **This is what stays code** (`activity-model.md`). A definition is data and declares nothing
 * new: it puts positions on these keys, weights on the sheet tree, free text, and rules made
 * of listed kinds. What a model is given so it can write a definition is this catalogue, never
 * the code.
 *
 * The default of each is **what a free conversation is**: what the app does when nobody has
 * asked for anything. Free conversation is itself a delivered definition, so these defaults
 * are what that definition will be written against rather than a second way of saying it.
 *
 * The sections are how the doc reads them, aptitude by aptitude, and a reading convenience
 * only: **a lever declares no belonging**.
 */
object Levers {

    // ── Elocution ───────────────────────────────────────────────────────────────────────

    /** Zero is *from memory*: zero turns the lever off where that is true, so one lever. */
    val MODEL_LISTENS = Numeric(
        key = "model-listens",
        says = R.plurals.lever_model_listens,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysZero = R.string.lever_model_listens_none,
        saysUnbounded = R.string.lever_model_listens_free,
    )

    val TEMPO = Stepped(
        key = "tempo",
        steps = listOf(
            Step("free", R.string.lever_tempo_free),
            Step("set", R.string.lever_tempo_set),
        ),
        fallback = "free",
        held = Held.App,
    )

    /**
     * Two levers and not one, by the rule that cuts them: no percentage means *no constraint*
     * -- a pace at 0% would demand the instantaneous -- so nothing would express "this
     * activity does not impose a pace".
     *
     * **A ceiling on the turn's length, not a tolerance around the model's pace.** The doc
     * writes the value as a percentage of the model's length and, two lines later, says that
     * on a spontaneous turn it renders a verdict *"too slow, do it again"* -- which only
     * makes sense of a ceiling. So 100 is the model's own length and 120 allows a fifth more.
     *
     * The default allows that fifth rather than demanding the model's exact length: a scene
     * that turns the pace on without saying more is asking to keep up, not to match.
     */
    val TEMPO_VALUE = Numeric(
        key = "tempo.value",
        says = R.plurals.lever_tempo_value,
        min = 50, max = 300, step = 10,
        fallback = Count(120),
        hard = HardSide.Low,
        held = Held.App,
        needs = Requirement("tempo", setOf("set"), R.string.lever_tempo_value_moot),
    )

    val RETAKES = Numeric(
        key = "retakes-allowed",
        says = R.plurals.lever_retakes,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysUnbounded = R.string.lever_retakes_free,
    )

    val ELOCUTION_SENDS_BACK = sendsBack(
        "pronunciation", R.string.lever_elocution_lets_through, R.string.lever_elocution_sends_back,
    )

    // ── Understanding ───────────────────────────────────────────────────────────────────

    val AI_LENGTH = Stepped(
        key = "ai-turn.length",
        steps = listOf(
            Step("short", R.string.lever_ai_length_short,
                 "Keep each reply to a sentence or two."),
            Step("medium", R.string.lever_ai_length_medium,
                 "Keep each reply to two or three sentences."),
            Step("long", R.string.lever_ai_length_long,
                 "Your replies may run to five or six sentences."),
        ),
        fallback = "medium",
        held = Held.Model,
    )

    /**
     * The positions are **deliberately generic**: it is a lever the model holds, so it reads
     * them against the scene, and prescriptive names would take that suppleness away without
     * guaranteeing anything in exchange.
     */
    val AI_COMPLEXITY = Stepped(
        key = "ai-turn.complexity",
        steps = listOf(
            Step("low", R.string.lever_ai_complexity_low,
                 "Use plain, common words and simple sentence shapes."),
            Step("medium", R.string.lever_ai_complexity_medium,
                 "Use ordinary everyday language."),
            Step("high", R.string.lever_ai_complexity_high,
                 "Use a wider vocabulary and more involved sentence shapes."),
        ),
        fallback = "medium",
        held = Held.Model,
    )

    /**
     * Scrambled by default, and it is this position rather than a preference (`pixel-ui.md`).
     * It falls between the plain text and *only who speaks*: the support left standing is one
     * aid fewer than the text and one more than nothing. A preference would hold everywhere
     * and no challenge could take it back.
     */
    val AI_DISPLAY = Stepped(
        key = "ai-turn.display",
        steps = listOf(
            Step("text", R.string.lever_ai_display_text),
            Step("scrambled", R.string.lever_ai_display_scrambled),
            Step("speaker", R.string.lever_ai_display_speaker),
            Step("nothing", R.string.lever_ai_display_nothing),
        ),
        fallback = "scrambled",
        held = Held.App,
    )

    val REPLAY = Numeric(
        key = "replays",
        says = R.plurals.lever_replay,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysZero = R.string.lever_replay_none,
        saysUnbounded = R.string.lever_replay_free,
    )

    /**
     * Noise and filter were one lever and make two, because they are independent: a quiet room
     * on a bad line, a loud café on a clean one.
     *
     * **Declared and unused for now.** What is open is how a position is made to mean a
     * comparable difficulty from one background to another: the signal-to-noise ratio
     * standardises the level and not the difficulty -- competing speech is far harder than
     * pink noise at the same ratio -- and the filter wants its own closed list of named
     * effects. Neither is decided, so neither has a file behind it.
     */
    val NOISE = Stepped(
        key = "noise",
        steps = listOf(
            Step("none", R.string.lever_noise_none),
            Step("some", R.string.lever_noise_some),
            Step("heavy", R.string.lever_noise_heavy),
        ),
        fallback = "none",
        held = Held.App,
    )

    val FILTER = Stepped(
        key = "filter",
        steps = listOf(
            Step("none", R.string.lever_filter_none),
            Step("light", R.string.lever_filter_light),
            Step("heavy", R.string.lever_filter_heavy),
        ),
        fallback = "none",
        held = Held.App,
    )

    val UNDERSTANDING_SENDS_BACK = sendsBack(
        "understanding",
        R.string.lever_understanding_lets_through, R.string.lever_understanding_sends_back,
    )

    // ── Correctness ─────────────────────────────────────────────────────────────────────

    /** An aid, so its positions read backwards: it is the absence that is the hard notch. */
    val ECHO = Stepped(
        key = "echo",
        steps = listOf(
            Step("explicit", R.string.lever_echo_explicit,
                 "When they get something wrong, say the corrected form back to them "
                     + "plainly, then carry on with your reply."),
            Step("indirect", R.string.lever_echo_indirect,
                 "When they get something wrong, recast it inside your own reply without "
                     + "pointing at it."),
            Step("none", R.string.lever_echo_none,
                 "Do not correct them in your reply. Answer as if nothing were wrong."),
        ),
        fallback = "indirect",
        held = Held.Model,
    )

    /**
     * Three positions and not two. Without one that says *none*, an ordinary conversation --
     * where the app explains nothing -- was not expressible at all.
     */
    val EXPLANATION = Stepped(
        key = "explanation",
        steps = listOf(
            Step("rule-and-sentence", R.string.lever_explanation_rule_and_sentence),
            Step("rule", R.string.lever_explanation_rule),
            Step("none", R.string.lever_explanation_none),
        ),
        fallback = "none",
        held = Held.Model,
    )

    val REWORDINGS = Numeric(
        key = "rewordings-allowed",
        says = R.plurals.lever_rewordings,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysUnbounded = R.string.lever_rewordings_free,
    )

    /** The one aptitude a free conversation sends back on. */
    val CORRECTNESS_SENDS_BACK = Stepped(
        key = "correctness.sends-back",
        steps = listOf(
            Step("no", R.string.lever_correctness_lets_through),
            Step("yes", R.string.lever_correctness_sends_back),
        ),
        fallback = "yes",
        held = Held.App,
    )

    // ── Relevance ───────────────────────────────────────────────────────────────────────

    /**
     * Its only lever, and it says nothing of what the aptitude demands: everything it demands
     * lives in the **instruction**, which it alone carries. A register, an imposed length, a
     * forbidden word have no hard end, so they are not levers.
     */
    val RELEVANCE_SENDS_BACK = sendsBack(
        "relevance", R.string.lever_relevance_lets_through, R.string.lever_relevance_sends_back,
    )

    // ── Fluency ─────────────────────────────────────────────────────────────────────────

    /**
     * The three positions of [CAPTURE], named once so nothing spells one out again.
     *
     * What separates them is **who opens the mic and whether the pause exists**, and not the
     * gesture: all three open with a press (`activity-model.md`). [BY_HAND] is the only one
     * with a pause, which is exactly why its silences say nothing -- between two stretches
     * the blank measures the thumb.
     */
    const val BY_HAND = "by-hand"
    const val ARMED = "armed"
    const val ARMED_AND_SENDING = "armed-and-sending"

    val CAPTURE = Stepped(
        key = "capture",
        steps = listOf(
            Step(BY_HAND, R.string.lever_capture_finger),
            Step(ARMED, R.string.lever_capture_armed),
            Step(ARMED_AND_SENDING, R.string.lever_capture_armed_and_sending),
        ),
        fallback = BY_HAND,
        held = Held.App,
    )

    /**
     * Two levers and not one: no number means *no threshold* -- zero would send the turn at
     * once -- and what separates the second capture position from the third is not x but the
     * fact that the turn goes on its own.
     */
    val SILENCE_THRESHOLD = Numeric(
        key = "silence-threshold",
        says = R.plurals.lever_silence_threshold,
        min = 1, max = 30, step = 1,
        fallback = Count(5),
        hard = HardSide.Low,
        held = Held.App,
        needs = Requirement(
            "capture", setOf(ARMED_AND_SENDING), R.string.lever_silence_threshold_moot,
        ),
    )

    /**
     * One variable, whose technical ceiling is its highest admissible value: asking for an
     * answer in five seconds and tolerating thirty at most are the same thing set differently.
     * The ceiling is the analysis pass, whose memory grows as the **square** of the turn's
     * length; it goes up when windowing arrives, and the lever stays.
     */
    val TURN_LENGTH = Numeric(
        key = "turn-length",
        says = R.plurals.lever_turn_length,
        min = 3, max = 30, step = 1,
        fallback = Count(30),
        hard = HardSide.Low,
        held = Held.App,
    )

    /**
     * The time between the end of the AI's answer and the mic being armed -- and not thinking
     * time granted *inside* the turn, which would be a line internal to the fluency measure,
     * and an internal line is never settable. Living outside the turn, it touches no measure.
     */
    val PREPARATION = Numeric(
        key = "preparation",
        says = R.plurals.lever_preparation,
        min = 0, max = 30, step = 1,
        fallback = Count(0),
        hard = HardSide.Low,
        held = Held.App,
        needs = Requirement(
            "capture", setOf(ARMED, ARMED_AND_SENDING), R.string.lever_preparation_moot,
        ),
    )

    /**
     * Throwing a take away before it is sent. Nothing has gone, nothing was measured, and no
     * attempt is spent -- which is exactly why it is a lever and not a plain gesture: offered
     * freely it walks around the attempt counters, since a challenge granting one attempt can
     * be restarted ten times by throwing each take away.
     *
     * **No object at the third capture position**, and the reason is not the manual send,
     * which stays the normal gesture there. It is that a clock sends too: the silence one
     * hesitates through is what sends the take, so the button would be a race against it.
     */
    val DISCARD_TAKE = Stepped(
        key = "discard-take",
        steps = listOf(
            Step("allowed", R.string.lever_discard_take_allowed),
            Step("forbidden", R.string.lever_discard_take_forbidden),
        ),
        fallback = "allowed",
        held = Held.App,
        needs = Requirement(
            "capture", setOf(BY_HAND, ARMED), R.string.lever_discard_take_moot,
        ),
    )

    val FLUENCY_SENDS_BACK = sendsBack(
        "fluency", R.string.lever_fluency_lets_through, R.string.lever_fluency_sends_back,
    )

    // ── The activity itself ─────────────────────────────────────────────────────────────

    /**
     * Lives are a lever: three of them press the learner the way a short silence threshold
     * does. **Its position is the number left** and not an allowance set beside a counter --
     * which is what makes there be no lives object, and so no kind of effect "take a life"
     * beside "lay a patch".
     */
    val LIVES = Stepped(
        key = "lives",
        steps = listOf(
            Step("no-lives", R.string.lever_lives_off),
            Step("counted", R.string.lever_lives_on),
        ),
        fallback = "no-lives",
        held = Held.App,
    )

    /** Zero ends the sitting, which is a declared property of the lever and not a rule. */
    val LIVES_LEFT = Numeric(
        key = "lives.left",
        says = R.plurals.lever_lives_left,
        min = 0, max = null, step = 1,
        fallback = Count(3),
        hard = HardSide.Low,
        held = Held.App,
        saysUnbounded = R.string.lever_lives_left_free,
        needs = Requirement("lives", setOf("counted"), R.string.lever_lives_left_moot),
    )

    /**
     * Two levers, for the reason that already cut the attempts in two: a challenge aiming only
     * at pronunciation keeps its retakes intact whatever happens on the words' side.
     *
     * And *waiting* does not want the same thing on the two sides. On the words, the verdict
     * arrives with the answer, so waiting means **playing the echo instead of the
     * continuation** -- a choice between two things already rendered, and free. On the sound,
     * the verdict arrives after the call, so waiting means only that **the passage does not
     * close**: the answer plays, and the big button stays unavailable until a retake is made.
     */
    val ADVANCE_WORDS = Stepped(
        key = "advance.words",
        steps = listOf(
            Step("carries-on", R.string.lever_advance_words_carries_on),
            Step("waits", R.string.lever_advance_words_waits),
        ),
        fallback = "carries-on",
        held = Held.App,
    )

    val ADVANCE_SOUND = Stepped(
        key = "advance.sound",
        steps = listOf(
            Step("carries-on", R.string.lever_advance_sound_carries_on),
            Step("waits", R.string.lever_advance_sound_waits),
        ),
        fallback = "carries-on",
        held = Held.App,
    )

    // ── One sensitivity per sheet ───────────────────────────────────────────────────────

    /**
     * The five positions every sensitivity has, declared once.
     *
     * **A sensitivity is a lever and a weight is not**, and the test is the direction: going
     * towards severe tightens, always and for everybody, where raising melody's weight
     * tightens the sitting of whoever has a bad one and eases the sitting of whoever has a
     * good one -- so its direction depends on the learner, whom a lever neither knows nor has
     * to know. What each says is *how much is demanded*, against the weight's *what is looked
     * at*.
     *
     * Five because a position is a **window of four consecutive bounds** in a series of
     * [SERIES_LENGTH], and one notch is worth exactly one letter (`notes/Sensitivity.kt`).
     * A position more would cost one number in each series and not four, so the count is not
     * an arbitration between expressiveness and work -- it follows from the series.
     *
     * **This is the one lever family whose phrase does not stand on its own**, and it is said
     * here rather than papered over: there are eleven of it, so the phrase says the level and
     * the screen supplies which sheet it judges, from the sheet-name resources step 17 owns.
     * Writing fifty-five strings instead would say the same five things eleven times.
     */
    private val SENSITIVITY_STEPS = listOf(
        Step("lenient-plus", R.string.lever_sensitivity_lenient_plus),
        Step("lenient", R.string.lever_sensitivity_lenient),
        Step("normal", R.string.lever_sensitivity_normal),
        Step("severe", R.string.lever_sensitivity_severe),
        Step("severe-plus", R.string.lever_sensitivity_severe_plus),
    )

    /** How a sitting names the sensitivity of the sheet at [path]. */
    fun sensitivityOf(path: String): String = "$path.sensitivity"

    /**
     * One per **scored** sheet, and none anywhere else.
     *
     * A sheet outside the tree gives no note, so its bounds decide nothing: the interrupted
     * turn is binary -- every possible bound renders the same pair of letters -- and the
     * counts are whole numbers that do not normalise. Neither sensitivity nor weight on those,
     * two fields with no object rather than two declared inert, and the custom screen has no
     * slider to offer for them.
     */
    val SENSITIVITIES: List<Stepped> = Sheets.all
        .mapNotNull(Sheets::scoredPathOf)
        .map { path ->
            Stepped(
                key = sensitivityOf(path),
                steps = SENSITIVITY_STEPS,
                fallback = "normal",
                held = Held.App,
            )
        }

    // ────────────────────────────────────────────────────────────────────────────────────

    /**
     * The five `fait-refaire`, which all have the same shape: whether failing this aptitude
     * puts the passage up to be done again.
     *
     * A different axis from the two `avance`, which say whether the conversation **waits**
     * while it is done again. Marking without blocking is the ordinary free conversation.
     */
    private fun sendsBack(aptitude: String, lets: Int, sends: Int) = Stepped(
        key = "$aptitude.sends-back",
        steps = listOf(Step("no", lets), Step("yes", sends)),
        fallback = "no",
        held = Held.App,
    )

    val all: List<Lever> = listOf(
        MODEL_LISTENS, TEMPO, TEMPO_VALUE, RETAKES, ELOCUTION_SENDS_BACK,
        AI_LENGTH, AI_COMPLEXITY, AI_DISPLAY, REPLAY, NOISE, FILTER, UNDERSTANDING_SENDS_BACK,
        ECHO, EXPLANATION, REWORDINGS, CORRECTNESS_SENDS_BACK,
        RELEVANCE_SENDS_BACK,
        CAPTURE, SILENCE_THRESHOLD, TURN_LENGTH, PREPARATION, DISCARD_TAKE, FLUENCY_SENDS_BACK,
        LIVES, LIVES_LEFT, ADVANCE_WORDS, ADVANCE_SOUND,
    ) + SENSITIVITIES

    private val byKey: Map<String, Lever> = all.associateBy { it.key }.also {
        require(it.size == all.size) { "two levers share a key" }
        // The positions are the windows a series of SERIES_LENGTH leaves, so declaring one
        // here that the series cannot open would put a name on a window nobody can read.
        require(SENSITIVITY_STEPS.size == POSITIONS) {
            "${SENSITIVITY_STEPS.size} sensitivity steps for $POSITIONS windows " +
                "in a series of $SERIES_LENGTH"
        }
        all.mapNotNull { lever -> lever.needs }.forEach { needs ->
            val on = it[needs.key] as? Stepped ?: error("${needs.key}: no such stepped lever")
            needs.positions.forEach(on::rank)
        }
    }

    /**
     * The lever called [key].
     *
     * **Fails outright on a key nobody declared**, rather than handing back something that
     * would then take a silent default. That is what the declaration buys.
     */
    fun of(key: String): Lever =
        byKey[key] ?: error("$key: no such lever. The catalogue is the list of them.")
}
