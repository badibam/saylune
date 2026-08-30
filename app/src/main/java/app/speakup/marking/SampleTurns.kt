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
 */
private const val SENTENCE = "I think we should book a table before they arrive"

val MULTI_FAULT_TURN = TurnMarking(
    text = SENTENCE,
    syllables = listOf(
        Syllable(0, 1, 190f, 185f, false, false),
        Syllable(2, 7, 200f, 196f, false, false),
        Syllable(8, 10, 185f, 180f, false, false),
        Syllable(11, 17, 180f, null, false, false),
        Syllable(18, 22, 235f, 190f, false, false),
        Syllable(23, 24, 175f, 172f, false, false),
        Syllable(25, 27, 210f, 205f, modelStressed = true, learnerStressed = false),
        Syllable(27, 30, 165f, 168f, modelStressed = false, learnerStressed = true),
        Syllable(31, 33, 175f, 172f, false, false),
        Syllable(33, 37, 190f, 186f, modelStressed = true, learnerStressed = true),
        Syllable(38, 42, 180f, 178f, false, false),
        Syllable(43, 45, 165f, 200f, false, false),
        Syllable(45, 49, 145f, 250f, modelStressed = true, learnerStressed = true),
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
        AddedSound("k", at = 21, after = 20, afterSound = 5),
        AddedSound("ə", at = null, after = 23, afterSound = 6),
    ),
)

val CLEAN_TURN = TurnMarking(
    text = SENTENCE,
    syllables = listOf(
        Syllable(0, 1, 190f, 188f, false, false),
        Syllable(2, 7, 200f, 203f, false, false),
        Syllable(8, 10, 185f, 183f, false, false),
        Syllable(11, 17, 180f, 182f, false, false),
        Syllable(18, 22, 235f, 228f, false, false),
        Syllable(23, 24, 175f, 176f, false, false),
        Syllable(25, 27, 210f, 214f, modelStressed = true, learnerStressed = true),
        Syllable(27, 30, 165f, 163f, false, false),
        Syllable(31, 33, 175f, 178f, false, false),
        Syllable(33, 37, 190f, 188f, modelStressed = true, learnerStressed = true),
        Syllable(38, 42, 180f, 181f, false, false),
        Syllable(43, 45, 165f, 168f, false, false),
        Syllable(45, 49, 145f, 150f, modelStressed = true, learnerStressed = true),
    ),
    phonemes = emptyList(),
    words = emptyList(),
    added = emptyList(),
)
