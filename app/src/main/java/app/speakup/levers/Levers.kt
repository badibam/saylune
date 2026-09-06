package app.speakup.levers

import app.speakup.R

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
        key = "ecoutes-modele",
        says = R.plurals.lever_model_listens,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysZero = R.string.lever_model_listens_none,
        saysUnbounded = R.string.lever_model_listens_free,
    )

    val PACE = Stepped(
        key = "cadence",
        steps = listOf(
            Step("libre", R.string.lever_pace_free),
            Step("imposee", R.string.lever_pace_set),
        ),
        fallback = "libre",
        held = Held.App,
    )

    /**
     * Two levers and not one, by the rule that cuts them: no percentage means *no constraint*
     * -- a pace at 0% would demand the instantaneous -- so nothing would express "this
     * activity does not impose a pace".
     *
     * The default sits at the model's own length. A scene that turns the pace on without
     * saying more asks to keep up with the model, which is the neutral thing to ask.
     */
    val PACE_VALUE = Numeric(
        key = "cadence.valeur",
        says = R.plurals.lever_pace_value,
        min = 10, max = 300, step = 5,
        fallback = Count(100),
        hard = HardSide.Low,
        held = Held.App,
        needs = Requirement("cadence", setOf("imposee"), R.string.lever_pace_value_moot),
    )

    val RETAKES = Numeric(
        key = "redites-permises",
        says = R.plurals.lever_retakes,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysUnbounded = R.string.lever_retakes_free,
    )

    val ELOCUTION_SENDS_BACK = sendsBack(
        "elocution", R.string.lever_elocution_lets_through, R.string.lever_elocution_sends_back,
    )

    // ── Understanding ───────────────────────────────────────────────────────────────────

    val AI_LENGTH = Stepped(
        key = "tour-ia.longueur",
        steps = listOf(
            Step("courte", R.string.lever_ai_length_short),
            Step("moyenne", R.string.lever_ai_length_medium),
            Step("longue", R.string.lever_ai_length_long),
        ),
        fallback = "moyenne",
        held = Held.Model,
    )

    /**
     * The positions are **deliberately generic**: it is a lever the model holds, so it reads
     * them against the scene, and prescriptive names would take that suppleness away without
     * guaranteeing anything in exchange.
     */
    val AI_COMPLEXITY = Stepped(
        key = "tour-ia.complexite",
        steps = listOf(
            Step("basse", R.string.lever_ai_complexity_low),
            Step("moyenne", R.string.lever_ai_complexity_medium),
            Step("elevee", R.string.lever_ai_complexity_high),
        ),
        fallback = "moyenne",
        held = Held.Model,
    )

    /**
     * Scrambled by default, and it is this position rather than a preference (`pixel-ui.md`).
     * It falls between the plain text and *only who speaks*: the support left standing is one
     * aid fewer than the text and one more than nothing. A preference would hold everywhere
     * and no challenge could take it back.
     */
    val AI_DISPLAY = Stepped(
        key = "tour-ia.affichage",
        steps = listOf(
            Step("texte", R.string.lever_ai_display_text),
            Step("brouille", R.string.lever_ai_display_scrambled),
            Step("qui-parle", R.string.lever_ai_display_speaker),
            Step("rien", R.string.lever_ai_display_nothing),
        ),
        fallback = "brouille",
        held = Held.App,
    )

    val REPLAY = Numeric(
        key = "reecoute",
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
        key = "bruit",
        steps = listOf(
            Step("aucun", R.string.lever_noise_none),
            Step("present", R.string.lever_noise_some),
            Step("fort", R.string.lever_noise_heavy),
        ),
        fallback = "aucun",
        held = Held.App,
    )

    val FILTER = Stepped(
        key = "filtre",
        steps = listOf(
            Step("aucun", R.string.lever_filter_none),
            Step("leger", R.string.lever_filter_light),
            Step("marque", R.string.lever_filter_heavy),
        ),
        fallback = "aucun",
        held = Held.App,
    )

    val UNDERSTANDING_SENDS_BACK = sendsBack(
        "comprehension",
        R.string.lever_understanding_lets_through, R.string.lever_understanding_sends_back,
    )

    // ── Correctness ─────────────────────────────────────────────────────────────────────

    /** An aid, so its positions read backwards: it is the absence that is the hard notch. */
    val ECHO = Stepped(
        key = "echo",
        steps = listOf(
            Step("explicite", R.string.lever_echo_explicit),
            Step("indirect", R.string.lever_echo_indirect),
            Step("absent", R.string.lever_echo_none),
        ),
        fallback = "indirect",
        held = Held.Model,
    )

    /**
     * Three positions and not two. Without one that says *none*, an ordinary conversation --
     * where the app explains nothing -- was not expressible at all.
     */
    val EXPLANATION = Stepped(
        key = "explication",
        steps = listOf(
            Step("regle-et-phrase", R.string.lever_explanation_rule_and_sentence),
            Step("regle", R.string.lever_explanation_rule),
            Step("aucune", R.string.lever_explanation_none),
        ),
        fallback = "aucune",
        held = Held.Model,
    )

    val REWORDINGS = Numeric(
        key = "reformulations-permises",
        says = R.plurals.lever_rewordings,
        min = 0, max = null, step = 1,
        fallback = Count(null),
        hard = HardSide.Low,
        held = Held.App,
        saysUnbounded = R.string.lever_rewordings_free,
    )

    /** The one aptitude a free conversation sends back on. */
    val CORRECTNESS_SENDS_BACK = Stepped(
        key = "correction.fait-refaire",
        steps = listOf(
            Step("non", R.string.lever_correctness_lets_through),
            Step("oui", R.string.lever_correctness_sends_back),
        ),
        fallback = "oui",
        held = Held.App,
    )

    // ── Relevance ───────────────────────────────────────────────────────────────────────

    /**
     * Its only lever, and it says nothing of what the aptitude demands: everything it demands
     * lives in the **instruction**, which it alone carries. A register, an imposed length, a
     * forbidden word have no hard end, so they are not levers.
     */
    val RELEVANCE_SENDS_BACK = sendsBack(
        "pertinence", R.string.lever_relevance_lets_through, R.string.lever_relevance_sends_back,
    )

    // ── Fluency ─────────────────────────────────────────────────────────────────────────

    val CAPTURE = Stepped(
        key = "capture",
        steps = listOf(
            Step("doigt", R.string.lever_capture_finger),
            Step("armee", R.string.lever_capture_armed),
            Step("armee-et-silence", R.string.lever_capture_armed_and_sending),
        ),
        fallback = "doigt",
        held = Held.App,
    )

    /**
     * Two levers and not one: no number means *no threshold* -- zero would send the turn at
     * once -- and what separates the second capture position from the third is not x but the
     * fact that the turn goes on its own.
     */
    val SILENCE_THRESHOLD = Numeric(
        key = "seuil-silence",
        says = R.plurals.lever_silence_threshold,
        min = 1, max = 30, step = 1,
        fallback = Count(5),
        hard = HardSide.Low,
        held = Held.App,
        needs = Requirement(
            "capture", setOf("armee-et-silence"), R.string.lever_silence_threshold_moot,
        ),
    )

    /**
     * One variable, whose technical ceiling is its highest admissible value: asking for an
     * answer in five seconds and tolerating thirty at most are the same thing set differently.
     * The ceiling is the analysis pass, whose memory grows as the **square** of the turn's
     * length; it goes up when windowing arrives, and the lever stays.
     */
    val TURN_LENGTH = Numeric(
        key = "duree-tour",
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
            "capture", setOf("armee", "armee-et-silence"), R.string.lever_preparation_moot,
        ),
    )

    val FLUENCY_SENDS_BACK = sendsBack(
        "fluidite", R.string.lever_fluency_lets_through, R.string.lever_fluency_sends_back,
    )

    // ── The activity itself ─────────────────────────────────────────────────────────────

    /**
     * Lives are a lever: three of them press the learner the way a short silence threshold
     * does. **Its position is the number left** and not an allowance set beside a counter --
     * which is what makes there be no lives object, and so no kind of effect "take a life"
     * beside "lay a patch".
     */
    val LIVES = Stepped(
        key = "vies",
        steps = listOf(
            Step("pas-de-vies", R.string.lever_lives_off),
            Step("comptees", R.string.lever_lives_on),
        ),
        fallback = "pas-de-vies",
        held = Held.App,
    )

    /** Zero ends the sitting, which is a declared property of the lever and not a rule. */
    val LIVES_LEFT = Numeric(
        key = "vies.restantes",
        says = R.plurals.lever_lives_left,
        min = 0, max = null, step = 1,
        fallback = Count(3),
        hard = HardSide.Low,
        held = Held.App,
        saysUnbounded = R.string.lever_lives_left_free,
        needs = Requirement("vies", setOf("comptees"), R.string.lever_lives_left_moot),
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
        key = "avance.mots",
        steps = listOf(
            Step("poursuit", R.string.lever_advance_words_carries_on),
            Step("attend", R.string.lever_advance_words_waits),
        ),
        fallback = "poursuit",
        held = Held.App,
    )

    val ADVANCE_SOUND = Stepped(
        key = "avance.son",
        steps = listOf(
            Step("poursuit", R.string.lever_advance_sound_carries_on),
            Step("attend", R.string.lever_advance_sound_waits),
        ),
        fallback = "poursuit",
        held = Held.App,
    )

    // ────────────────────────────────────────────────────────────────────────────────────

    /**
     * The five `fait-refaire`, which all have the same shape: whether failing this aptitude
     * puts the passage up to be done again.
     *
     * A different axis from the two `avance`, which say whether the conversation **waits**
     * while it is done again. Marking without blocking is the ordinary free conversation.
     */
    private fun sendsBack(aptitude: String, lets: Int, sends: Int) = Stepped(
        key = "$aptitude.fait-refaire",
        steps = listOf(Step("non", lets), Step("oui", sends)),
        fallback = "non",
        held = Held.App,
    )

    val all: List<Lever> = listOf(
        MODEL_LISTENS, PACE, PACE_VALUE, RETAKES, ELOCUTION_SENDS_BACK,
        AI_LENGTH, AI_COMPLEXITY, AI_DISPLAY, REPLAY, NOISE, FILTER, UNDERSTANDING_SENDS_BACK,
        ECHO, EXPLANATION, REWORDINGS, CORRECTNESS_SENDS_BACK,
        RELEVANCE_SENDS_BACK,
        CAPTURE, SILENCE_THRESHOLD, TURN_LENGTH, PREPARATION, FLUENCY_SENDS_BACK,
        LIVES, LIVES_LEFT, ADVANCE_WORDS, ADVANCE_SOUND,
    )

    private val byKey: Map<String, Lever> = all.associateBy { it.key }.also {
        require(it.size == all.size) { "two levers share a key" }
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
