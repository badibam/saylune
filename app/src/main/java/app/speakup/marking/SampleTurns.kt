package app.speakup.marking

/**
 * Two turns of the same sentence, standing in for the engine until it is wired up: one
 * carrying faults at all three scales at once, one clean. The prototype exists to judge the
 * marking on a crowded turn, so the faults are deliberately piled onto one utterance.
 *
 * Pitches are hertz as the engine reports them, one value per syllable; deviations are its
 * points below the model. `should` has no learner pitch: that is the harmonic-lock filter
 * discarding a syllable, which it does on up to a fifth of a turn.
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
        PhonemeDeviation(35, 36, 14f),  // r of before
        PhonemeDeviation(38, 40, 16f),  // th of they
        PhonemeDeviation(44, 45, 11f),  // r of ar
        PhonemeDeviation(45, 46, 12f),  // r of rive
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
)
