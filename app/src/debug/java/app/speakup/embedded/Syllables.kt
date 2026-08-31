package app.speakup.embedded

/**
 * The syllables of an utterance, cut on the sounds and never on the letters.
 *
 * English spelling lies about how many syllables were spoken -- `picked` is written in two
 * and said in one -- so the cut reads the grid the model actually produced. The price is
 * that a syllable exists only if its vowel was decoded; what to make of a word whose vowel
 * went missing belongs to the stress brick, not to this one.
 *
 * No dictionary decides anything here, and that is not thrift. A lexicon would judge by a
 * norm the design keeps outside, it would be American-only in the one free edition that
 * exists, and it would be wrong about the very thing it is asked: `market` said without the
 * `r` and `market` said with it are two grids and one cut, because the rule reads what was
 * said instead of citing the word.
 *
 * The port of `bench/syllables.py` and `bench/matrix.py`'s nucleus rule.
 */
object Syllables {

    /**
     * The vowels of the vocabulary in service. Beside the cut rather than beside any one
     * reading, since every brick that counts syllables needs the same list -- and a
     * candidate model with another alphabet needs its own.
     */
    private val VOWELS = setOf(
        "ɑ", "æ", "ə", "ɚ", "ɛ", "ɝ", "ɪ", "i", "ɔ", "ʊ", "u", "ʌ",
        "aɪ", "aʊ", "eɪ", "oʊ", "ɔɪ",
    )

    /** The vowels English glides through rather than holds. */
    private val DIPHTHONGS = setOf("aɪ", "aʊ", "eɪ", "oʊ", "ɔɪ")

    /** The r-coloured vowels, which the grid often writes as a second symbol. */
    private val RHOTIC = setOf("ɝ", "ɚ")

    /**
     * What can begin an English syllable. Every consonant but the velar nasal stands alone;
     * the clusters are the ones English actually allows, which is what stops `extra` being
     * cut before `xtr`. The flap is admitted: it never opens a word, but it opens the second
     * syllable of `butter`, which is the only place this rule is ever asked about.
     */
    private val CONSONANTS = listOf(
        "b", "ʧ", "d", "ð", "ɾ", "f", "g", "h", "ʤ", "k", "l", "m", "n", "p",
        "ɹ", "s", "ʃ", "t", "θ", "v", "w", "j", "z",
    )

    private val CLUSTERS = (
        "pl pɹ pj bl bɹ bj tɹ tw tj dɹ dw dj kl kɹ kw kj gl gɹ gw gj " +
            "fl fɹ fj θɹ θw ʃɹ hj mj nj lj vj " +
            "sp st sk sf sm sn sl sw sj " +
            "spl spɹ spj stɹ stj skɹ skw skj skl"
        ).split(" ")

    /**
     * Every onset the cut may hand to a syllable, the empty one included -- a syllable may
     * open on a vowel, and nothing is the answer whenever a cluster is entirely coda.
     */
    private val ONSETS: Set<List<String>> = buildSet {
        add(emptyList())
        CONSONANTS.forEach { add(listOf(it)) }
        for (cluster in CLUSTERS) {
            val group = mutableListOf<String>()
            var rest = cluster
            while (rest.isNotEmpty()) {
                // Symbols are not one character each, so a cluster is read against the
                // inventory rather than sliced.
                val symbol = CONSONANTS.filter { rest.startsWith(it) }.maxByOrNull { it.length }
                    ?: throw IllegalArgumentException("unreadable onset: $cluster")
                group.add(symbol)
                rest = rest.substring(symbol.length)
            }
            add(group.toList())
        }
    }

    /**
     * Where the syllables of a word begin, as ranks into [symbols].
     *
     * A vowel is a nucleus, except when it is the r-colouring of the vowel before it.
     * English writes `here`, `four` and `there` with one vowel each; the grid writes them
     * `ɪ ɝ`, `ɑ ɝ`, `ɛ ɝ`, and counting symbols gives every one of them two syllables.
     * Measured on 2500 model renders: those three pairs alone occur 146 times, and every
     * word they occur in has one syllable.
     *
     * **The merge stops at the diphthongs, and the same measurement is why.** A diphthong
     * before the same `ɝ` is a real second syllable -- `pow·er`, `play·er`, `hour·s` -- so
     * `eɪ ɝ`, `aʊ ɝ` and `aɪ ɝ` are left as two.
     */
    fun nuclei(symbols: List<String>): List<Int> {
        val out = mutableListOf<Int>()
        symbols.forEachIndexed { rank, symbol ->
            if (symbol !in VOWELS) return@forEachIndexed
            val colouring = out.isNotEmpty() && out.last() == rank - 1 &&
                symbol in RHOTIC && symbols[rank - 1] !in DIPHTHONGS
            if (!colouring) out.add(rank)
        }
        return out
    }

    /**
     * How many of the consonants between two nuclei open the second syllable.
     *
     * The maximal onset principle, bounded by what English allows: the longest tail of the
     * cluster that is a legal onset goes to the syllable ahead, and whatever is left falls
     * back as the coda of the one behind. `extra` cuts `ek·stɹə` rather than `e·kstɹə`,
     * because `kstɹ` opens nothing.
     */
    private fun opening(symbols: List<String>): Int {
        for (size in symbols.size downTo 0) {
            if (symbols.subList(symbols.size - size, symbols.size) in ONSETS) return size
        }
        return 0
    }

    /**
     * One entry per syllable: which sounds it holds, and which characters of the text.
     *
     * A syllable's letters are its sounds' letters, put together -- the join has already
     * posed them, and no clock enters here either. The syllable is **counted** on the sounds
     * and **painted** on the letters.
     */
    data class Cut(val sounds: IntRange, val spots: List<Int>, val word: String?)

    fun cut(sounds: List<Sound>): List<Cut> {
        val out = mutableListOf<Cut>()
        for ((start, stop) in runs(sounds)) {
            val symbols = sounds.subList(start, stop).map { it.symbol }
            val nuclei = nuclei(symbols)
            if (nuclei.isEmpty()) continue
            val edges = mutableListOf(0)
            for (i in 0 until nuclei.size - 1) {
                val here = nuclei[i]
                val then = nuclei[i + 1]
                edges.add(then - opening(symbols.subList(here + 1, then)))
            }
            edges.add(symbols.size)
            for (i in 0 until edges.size - 1) {
                val low = start + edges[i]
                val high = start + edges[i + 1]
                val spots = sounds.subList(low, high)
                    .flatMap { it.spots + it.borrowed }
                    .distinct()
                    .sorted()
                out.add(Cut(low until high, spots, sounds[low].word))
            }
        }
        return out
    }

    /**
     * The stretches of the sound list that belong to one word each.
     *
     * The join has already partitioned every sound between the words, so this only reads
     * the partition back rather than cutting anything: consecutive sounds sharing a word
     * are that word's run. A sound belonging to no word -- added matter -- ends the run it
     * follows, since nothing outside a word carries a syllable of that word.
     */
    private fun runs(sounds: List<Sound>): List<Pair<Int, Int>> {
        val out = mutableListOf<Pair<Int, Int>>()
        var start = 0
        while (start < sounds.size) {
            val here = sounds[start].wordAt
            if (here == null) {
                start++
                continue
            }
            var stop = start + 1
            while (stop < sounds.size && sounds[stop].wordAt == here) stop++
            out.add(start to stop)
            start = stop
        }
        return out
    }
}
