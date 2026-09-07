package app.saylune.sheets

/**
 * Every sheet the app measures, and where each sits in the tree.
 *
 * A challenge reduces to four things laid on this list: weights on the nodes, one sensitivity
 * position per sheet, instructions on the judged markings, and conditions. Nothing in it is a
 * new branch of code, and a definition written by a model becomes possible without handing it
 * the code -- one hands it this.
 *
 * **A node is addressed by its path**, `pronunciation/melody`, which is what lets an aptitude and
 * its single sheet keep the names the doc gives them without colliding. A condition names a
 * path; on a branch only the note is readable, on a sheet all three readings are.
 *
 * Three conditions for a sheet to exist at all, and they hold together: it is **always
 * computed**, whatever the activity, or a challenge's configuration would decide which fields
 * exist; it **means something with no challenge at all**, in an ordinary conversation, or it
 * is a special case dressed as a measure; and somebody could want to **score it alone**.
 *
 * **No sheet has two senses.** One whose direction would have to flip with the activity is the
 * wrong sheet.
 *
 * *All numbers here are a first pass.* The series and the columns are what the calibration
 * bench takes over, sheet by sheet, on takes recorded against criteria. They are material and
 * never a measurement.
 */
object Sheets {

    // ── The judged columns ──────────────────────────────────────────────────────────────
    //
    // Each is anchored by one sentence -- *a passage made entirely of this would be worth...*
    // -- which is what makes it writable and measurable, where a weighting laid in the void
    // would be neither.

    /**
     * Correctness: is this English?
     *
     * `ok` is worth the full mark and not 0,90, because **there is no notch above it**: a
     * difficult construction brought off right has no fixed norm -- difficult depends on who
     * is speaking, and the app has no learner level -- so an ambitious sentence that lands
     * gets marked `apt` on the relevance side, where the criterion is situational.
     *
     * `not-said` is close to nothing rather than nothing: a passage made of it is not
     * English at all. It needs no catastrophic note either, its real effect passing through no
     * note at all -- one word at that notch and the sound analysis does not run, for want of
     * anything to compare against.
     */
    private val CORRECTNESS = Reading.Column(
        listOf(
            Notch("not-said", 0.05f),
            Notch("malformed", 0.40f),
            Notch("ok", 1.00f),
        ),
        fallback = "ok",
    )

    /** Relevance: did you aim right? The one measure of the project with a good end. */
    private val RELEVANCE = Reading.Column(
        listOf(
            Notch("off-target", 0.10f),
            Notch("flat", 0.50f),
            Notch("apt", 1.00f),
            Notch("ok", 0.90f),
        ),
        fallback = "ok",
    )

    /**
     * Stumbling: a spoken word is kept, abandoned, or filler -- three slices of one cake.
     *
     * The judge marks only what is not kept, so **an unmarked word is `kept`** -- the same
     * shape as `ok` on the language scales, and far less output to ask of it than one notch
     * per spoken word. An abandoned word costs a little more than a filler, a restart
     * dragging more of the sentence with it than an *um* does.
     */
    private val STUMBLING = Reading.Column(
        listOf(
            Notch("abandoned", 0.30f),
            Notch("filler", 0.45f),
            Notch("kept", 1.00f),
        ),
        fallback = "kept",
    )

    /**
     * **Not a sheet**: what the model says of the turn it just wrote, which serves as the
     * weight of `uptake`.
     *
     * It says nothing about the learner, so it has no sheet, no sensitivity and no
     * instruction. Length alone was too coarse -- *"Fancy a cuppa?"* is harder than forty
     * simple words -- and the complexity lever does not replace it: that asks for a level, it
     * does not promise every sentence is hard. So it is one answer per sentence.
     *
     * Five notches and not the three of the complexity lever: this is a **weight**, and a
     * weight wants gradation where a request wants room to interpret. Its limits are written
     * with it: nobody checks it, it does not replay identically, and it is the model scoring
     * what it has just written.
     */
    val DIFFICULTY = Reading.Column(
        listOf(
            Notch("very-hard", 1.00f),
            Notch("hard", 0.80f),
            Notch("medium", 0.55f),
            Notch("easy", 0.30f),
            Notch("very-easy", 0.15f),
        ),
        fallback = null,
    )

    /**
     * Following: one notch per passage, on one axis -- what the answer proves it took in.
     *
     * Six notches where three are enough everywhere else, because this is the only sheet whose
     * fineness cannot come from counting: every other one averages dozens of elements, so a
     * coarse judgement repeated gives a fine figure. Here there is a single element, so the
     * precision **is** the notch's.
     *
     * `implied` sits **just above** `precise` and not far ahead: a turn with nothing
     * implied caps at `precise`, and that lack must cost almost nothing. `vague` is what holds
     * the sheet up -- without it, the plausible answer that commits to nothing had to be
     * counted right or wrong, and both were wrong.
     */
    private val FOLLOWING = Reading.Column(
        listOf(
            Notch("implied", 1.00f),
            Notch("precise", 0.95f),
            Notch("on-point", 0.80f),
            Notch("on-topic", 0.50f),
            Notch("vague", 0.25f),
            Notch("off-target", 0.00f),
        ),
        fallback = null,
    )

    // ── The tree ────────────────────────────────────────────────────────────────────────

    /**
     * Two sheets on the same elements, read by two ramps, and the inversion test separates
     * them. Two learners, fifty sounds each: **A** has a thick accent and stays perfectly
     * understandable, every sound some twenty points out and none past the gross-miss line;
     * **B** has a clean accent and two sounds completely wrong that change the word. On the
     * mean of the gaps A is worth 0,80 and B 0,93; on the step A is worth 1,00 and B 0,96.
     * The order flips, so no setting of a single sheet renders both verdicts.
     *
     * These are the two opposite challenges of the project: *make yourself understood* counts
     * only what changes the word, *lose your accent* counts everything.
     */
    private val ELOCUTION = Branch("pronunciation", listOf(
        Sheet(
            name = "intelligibility",
            elements = Elements.Sounds,
            // The line is where the screen's ramp saturates, and it is not measured: the
            // labelled set puts controls at 0,3 points and outright faults above 93, so
            // anywhere between 26 and 92 separates the two populations equally well.
            reading = Reading.Cliff(at = 30f),
            unit = Unit.Share,
            direction = null,
            from = null,
            // Packed at the top: a gross miss is rare and grave.
            series = listOf(0.70f, 0.80f, 0.88f, 0.93f, 0.96f, 0.98f, 0.99f, 0.995f),
        ),
        Sheet(
            name = "proximity",
            elements = Elements.Sounds,
            reading = Reading.Slope(from = 0f, to = 100f),
            unit = Unit.Share,
            direction = null,
            from = null,
            // Spread: the mean gap moves across the whole range from one learner to the next.
            series = listOf(0.55f, 0.65f, 0.72f, 0.79f, 0.85f, 0.90f, 0.94f, 0.97f),
        ),
        /**
         * The distance, in semitones: at each syllable the pitch gap between the two curves,
         * each side first brought back to its own median, and the **plain mean** of those.
         *
         * The strictest bound stays clear of the machine's noise band. A series that went
         * under one semitone would make A unreachable for a reason that has nothing to do
         * with the learner's melody.
         */
        Sheet(
            name = "melody",
            elements = Elements.Syllables,
            reading = Reading.Raw,
            unit = Unit.Semitones,
            direction = Direction.LowIsGood,
            from = null,
            series = listOf(6.5f, 5.2f, 4.2f, 3.5f, 2.9f, 2.4f, 2.0f, 1.7f),
        ),
        /**
         * The denominator is not "the words": a monosyllable has no choice of stress, and a
         * function word has no clear beat even in the model. So it is the words of more than
         * one syllable the model stresses clearly, at the probe margin of 0,90 an ear has
         * validated.
         */
        Sheet(
            name = "lexical-stress",
            elements = Elements.StressedWords,
            reading = Reading.Either,
            unit = Unit.Share,
            direction = null,
            from = null,
            series = listOf(0.55f, 0.68f, 0.78f, 0.85f, 0.90f, 0.94f, 0.97f, 0.99f),
        ),
    ))

    /**
     * One sheet under the aptitude, and the branch stays: a condition reads *"understanding is
     * in E"* by naming the aptitude, which a bare sheet would not let it do.
     */
    private val UNDERSTANDING = Branch("understanding", listOf(
        Sheet(
            name = "uptake",
            elements = Elements.Whole,
            reading = FOLLOWING,
            unit = Unit.NotchValue,
            direction = null,
            from = Marking.Following,
            series = listOf(0.10f, 0.30f, 0.40f, 0.55f, 0.70f, 0.85f, 0.90f, 0.97f),
        ),
    ))

    /**
     * Correctness asks one question -- is this English? -- and it is an absolute judgement, so
     * **it takes no instruction**. An instruction is always a demand of the situation, so it
     * belongs entirely to relevance.
     *
     * Absolute does not mean independent of any norm: nothing is correct in the abstract. What
     * is true, and what the project needs, is that **the norm is fixed by the app and the same
     * for every activity** -- and it is that invariance that makes correctness checkable at
     * the bench on isolated sentences.
     */
    private val CORRECTION = Branch("correctness", listOf(
        Sheet(
            name = "correctness",
            elements = Elements.KeptWords,
            reading = CORRECTNESS,
            unit = Unit.Share,
            direction = null,
            from = Marking.LanguageSpans,
            // Packed at the top: the words of a passage are nearly all `ok`.
            series = listOf(0.70f, 0.80f, 0.87f, 0.92f, 0.95f, 0.97f, 0.985f, 0.995f),
        ),
    ))

    private val RELEVANCE_BRANCH = Branch("relevance", listOf(
        Sheet(
            name = "relevance",
            elements = Elements.KeptWords,
            reading = RELEVANCE,
            unit = Unit.Share,
            direction = null,
            from = Marking.LanguageSpans,
            series = listOf(0.35f, 0.48f, 0.58f, 0.67f, 0.75f, 0.82f, 0.88f, 0.93f),
        ),
    ))

    /**
     * Four sheets, and **nothing is counted twice**: `continuite` owns all the silence
     * *between* words, `plus-long-silence` owns the block, `debit` reads only the time the
     * mouth is articulating, and `stumbling` owns the hesitations.
     */
    private val FLUENCY = Branch("fluency", listOf(
        /**
         * The share of the turn spent in silence, **minus the model's on the same sentence**,
         * in points of percentage, and it can be negative -- keeping quieter than the model is
         * no fluency fault, and nothing needs clipping for that: a negative gap falls above
         * the A bound, in the same band as zero.
         *
         * A difference and not a ratio, for the reason that already ruled out a ratio in
         * melody: the model's silent share can fall to one or two percent on a short sentence,
         * and a ratio turns hypersensitive there.
         */
        Sheet(
            name = "continuity",
            elements = Elements.TurnTime,
            reading = Reading.Raw,
            unit = Unit.PercentPoints,
            direction = Direction.LowIsGood,
            from = null,
            series = listOf(45f, 35f, 27f, 20f, 15f, 11f, 8f, 5f),
        ),
        /**
         * One element, its length in seconds, and the series reads in seconds: *"from three
         * seconds it is a C"*.
         *
         * Taking the silences as elements and averaging them would not work: a turn with no
         * silence at all would have no element, so no sheet, and the most fluent turn possible
         * would go unscored. A share of the turn spent in a blank would not work either, being
         * diluted by the turn's length. The worst silence depends on nothing but itself.
         */
        Sheet(
            name = "longest-silence",
            elements = Elements.Whole,
            reading = Reading.Raw,
            unit = Unit.Seconds,
            direction = Direction.LowIsGood,
            from = null,
            series = listOf(8.0f, 6.5f, 5.0f, 4.0f, 3.0f, 2.4f, 1.8f, 1.3f),
        ),
        /**
         * The speed gap to the model, in percent, on the kept words alone. Symmetric by
         * construction: twice as slow and twice as fast both give 100%.
         *
         * **A hypothesis to check**, and it is not measured: that the two sides are equally in
         * the way. Slowness is the French speaker's fault and excess speed is rare. If the
         * measure says they are not worth the same, the series becomes two lists instead of
         * one -- an extension, not a rework.
         */
        Sheet(
            name = "pace",
            elements = Elements.Whole,
            reading = Reading.Raw,
            unit = Unit.Percent,
            direction = Direction.LowIsGood,
            from = null,
            series = listOf(90f, 70f, 55f, 42f, 32f, 24f, 17f, 11f),
        ),
        /**
         * **Judged and never computed off a word list.** *euh* and *um* have no other use, but
         * *I mean*, *like*, *well*, *actually* are all real words -- *"I mean what I say"* and
         * *"it was, I mean, hard"* differ only in use, and only the judge sees the use.
         *
         * It survives only a verbatim recognition: an engine that cleans up the *ums* and the
         * stammers makes it mute without ever saying so, and fluency will look excellent.
         */
        Sheet(
            name = "stumbling",
            elements = Elements.SpokenWords,
            reading = STUMBLING,
            unit = Unit.Share,
            direction = null,
            from = Marking.Stumbling,
            series = listOf(0.60f, 0.72f, 0.80f, 0.86f, 0.91f, 0.94f, 0.97f, 0.99f),
        ),
    ))

    /** The tree that gives notes. A definition lays its weights on these nodes. */
    val tree: Branch = Branch(
        "", listOf(ELOCUTION, UNDERSTANDING, CORRECTION, RELEVANCE_BRANCH, FLUENCY),
    )

    /**
     * Sheets that compute and enter no note. They exist for the **conditions**, which read
     * them like any other. Neither sensitivity nor weight: two fields with no object, rather
     * than declared inert -- and the custom screen has no slider to offer for them.
     *
     * They sit outside the tree because the tree is what carries weights, and these carry
     * none. A condition names them by their bare name.
     */
    val unscored: List<Sheet> = listOf(
        /**
         * Was this turn sent, yes or no. Two causes of interruption and one sheet: the silence
         * of more than x in the third capture position, and the turn's length ceiling, which
         * exists in all three.
         *
         * **Binary, so it gives no note**: every possible bound renders the same pair of
         * letters, and a two-valued figure makes a poor member of a mean anyway -- all or
         * nothing, no gradation, and its weight would tip a whole aptitude at once.
         */
        Sheet("interrupted-turn", Elements.Whole, Reading.Either, Unit.Share, null, null, null),
        /**
         * The three counts. They are whole numbers, they do not normalise, and above all they
         * **climb at every attempt**: weighed into an aptitude that sends the passage back,
         * the note can never cross the bar again and the learner burns attempts with no way
         * out. A challenge that wants success first time writes it as a **condition**.
         */
        Sheet("retakes", Elements.Whole, Reading.Raw, Unit.Times, Direction.LowIsGood, null, null),
        Sheet("rewordings", Elements.Whole, Reading.Raw, Unit.Times, Direction.LowIsGood, null, null),
        Sheet("listens", Elements.Whole, Reading.Raw, Unit.Times, Direction.LowIsGood, null, null),
    )

    /** Every node of the tree, by its path. */
    private val byPath: Map<String, Node> = buildMap {
        fun walk(node: Node, above: String) {
            val path = if (above.isEmpty()) node.name else "$above/${node.name}"
            if (node.name.isNotEmpty()) {
                require(put(path, node) == null) { "$path: two nodes share a path" }
            }
            if (node is Branch) node.children.forEach { walk(it, path) }
        }
        walk(tree, "")
        unscored.forEach { require(put(it.name, it) == null) { "${it.name}: name already taken" } }
    }

    /**
     * The node at [path].
     *
     * **Fails outright on a path nobody declared.** A challenge is written against this list,
     * so a name it does not hold is a writing mistake and never something to guess at.
     */
    fun of(path: String): Node =
        byPath[path] ?: error("$path: no such node. The catalogue is the list of them.")

    /** Every sheet, scored and unscored, in the order they are declared. */
    val all: List<Sheet> = byPath.values.filterIsInstance<Sheet>()

    /**
     * Where [sheet] sits in the **tree**, or null when it is one of the unscored.
     *
     * The unscored ones sit outside the tree because the tree is what carries weights and they
     * carry none, so they have a bare name and not a path -- and telling the two apart by
     * looking for a slash would be reading a shape rather than asking.
     */
    fun scoredPathOf(sheet: Sheet): String? =
        pathOf(sheet).takeIf { sheet !in unscored }

    /** Where [node] sits, as the path a condition names. */
    fun pathOf(node: Node): String =
        byPath.entries.first { it.value === node }.key

    /**
     * The column of the sheet at [path].
     *
     * The one place notch names live. Whoever reads a marking back from the model checks it
     * against this rather than against a list of its own, so a notch the catalogue does not
     * declare fails at the seam instead of travelling on as data nothing can read.
     */
    fun columnOf(path: String): Reading.Column =
        (of(path) as? Sheet)?.reading as? Reading.Column
            ?: error("$path: not a sheet read by a column")
}
