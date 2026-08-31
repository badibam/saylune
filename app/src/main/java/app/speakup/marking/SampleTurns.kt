package app.speakup.marking

/**
 * Two turns of the same sentence, standing in for the engine until it is wired up: one
 * carrying faults at all three scales at once, one clean. The prototype exists to judge the
 * marking on a crowded turn, so the faults are deliberately piled onto one utterance.
 *
 * Pitches are hertz as the engine reports them, one value per syllable; deviations are its
 * points below the model. `should` has no learner pitch: that is the harmonic-lock filter
 * discarding a syllable, which it does on up to a fifth of a turn.
 *
 * `book` is the other kind of fault entirely: every one of its sounds is over the band, so
 * it carries a word mark and not three letter marks. That is the shape a swapped word takes,
 * and the fixture has to hold one or the channel is never looked at.
 *
 * And two added sounds, one of each kind: the `k` of `book` voiced where the model glides
 * straight on, which a silent letter can carry; and one between `a` and `table`, which no
 * letter of either word can, so it falls in the seam. Both are binary -- an insertion has no
 * model side, so there is no degree to give one.
 *
 * And one gutter, which is the third mark that lives in a seam and the one that tells the
 * other two apart on screen: the vowel of the syllabic `-le` in `table`, which English
 * writes with no letter at all. Unlike an added sound it has both recordings behind it, so
 * it carries a degree and is drawn hollow on the ramp rather than filled at the saturated
 * end. The clean turn has none, which is the point: a gutter under the band draws nothing.
 */
private const val SENTENCE = "I think we should book a table before they arrive"

val MULTI_FAULT_TURN = TurnMarking(
    text = SENTENCE,
    syllables = listOf(
        Syllable(0, 1, 0.9f, 0.5f, false, false),
        Syllable(2, 7, 1.8f, 1.5f, false, false),
        Syllable(8, 10, 0.5f, 0.0f, false, false),
        Syllable(11, 17, 0.0f, null, false, false),
        Syllable(18, 22, 4.6f, 0.9f, false, false),
        Syllable(23, 24, -0.5f, -0.8f, false, false),
        Syllable(25, 27, 2.7f, 2.3f, modelStressed = true, learnerStressed = false),
        Syllable(27, 30, -1.5f, -1.2f, modelStressed = false, learnerStressed = true),
        Syllable(31, 33, -0.5f, -0.8f, false, false),
        Syllable(33, 37, 0.9f, 0.6f, modelStressed = true, learnerStressed = true),
        Syllable(38, 42, 0.0f, -0.2f, false, false),
        Syllable(43, 45, -1.5f, 1.8f, false, false),
        Syllable(45, 49, -3.7f, 5.7f, modelStressed = true, learnerStressed = true),
    ),
    phonemes = listOf(
        PhonemeDeviation(2, 4, 23f),    // th of think
        PhonemeDeviation(13, 15, 9f),   // ou of should
        PhonemeDeviation(18, 19, 19f),  // b of book
        PhonemeDeviation(19, 21, 27f),  // oo of book
        PhonemeDeviation(21, 22, 22f),  // k of book
        PhonemeDeviation(35, 36, 14f),  // r of before
        PhonemeDeviation(38, 40, 16f),  // th of they
        PhonemeDeviation(44, 45, 11f),  // r of ar
        PhonemeDeviation(45, 46, 12f),  // r of rive
    ),
    words = listOf(WordFault(18, 22)),  // book: not one sound of it came through
    added = listOf(
        AddedSound("k", after = 20),
        AddedSound("ə", after = 23),
    ),
    gutters = listOf(Gutter("ə", after = 27, points = 21f)),
)

val CLEAN_TURN = TurnMarking(
    text = SENTENCE,
    syllables = listOf(
        Syllable(0, 1, 0.9f, 0.8f, false, false),
        Syllable(2, 7, 1.8f, 2.1f, false, false),
        Syllable(8, 10, 0.5f, 0.3f, false, false),
        Syllable(11, 17, 0.0f, 0.2f, false, false),
        Syllable(18, 22, 4.6f, 4.1f, false, false),
        Syllable(23, 24, -0.5f, -0.4f, false, false),
        Syllable(25, 27, 2.7f, 3.0f, modelStressed = true, learnerStressed = true),
        Syllable(27, 30, -1.5f, -1.7f, false, false),
        Syllable(31, 33, -0.5f, -0.2f, false, false),
        Syllable(33, 37, 0.9f, 0.8f, modelStressed = true, learnerStressed = true),
        Syllable(38, 42, 0.0f, 0.1f, false, false),
        Syllable(43, 45, -1.5f, -1.2f, false, false),
        Syllable(45, 49, -3.7f, -3.2f, modelStressed = true, learnerStressed = true),
    ),
    phonemes = emptyList(),
    words = emptyList(),
    added = emptyList(),
    gutters = emptyList(),
)
