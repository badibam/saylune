package app.speakup.store

import app.speakup.analysis.AnalysedSound
import app.speakup.analysis.Share
import app.speakup.marking.AddedSound
import app.speakup.marking.Gutter
import app.speakup.marking.PhonemeDeviation
import app.speakup.marking.Syllable
import app.speakup.marking.TurnMarking
import app.speakup.marking.WordFault
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The marks written out and read back are the marks that went in.
 *
 * A store that quietly flattens something is the worst kind: the screen still draws, so it
 * looks like the measure said that. The melody is the case with real teeth -- a learner pitch
 * that came back as nothing, or as its model's, would draw a flat line under a turn that was
 * anything but, and nothing on screen would say the number had been lost in a column.
 */
class MarksTest {

    private val marking = TurnMarking(
        text = "I am twenty five years old",
        syllables = listOf(
            Syllable(0, 1, modelPitch = 2.5f, learnerPitch = -4.25f,
                     modelStressed = true, learnerStressed = false),
            Syllable(2, 4, modelPitch = -1.75f, learnerPitch = 6.5f,
                     modelStressed = false, learnerStressed = true),
            // Nothing of it was voiced, which is not the same as flat and must survive as
            // null rather than come back as a number.
            Syllable(5, 11, modelPitch = 0.5f, learnerPitch = null,
                     modelStressed = false, learnerStressed = false),
        ),
        phonemes = listOf(PhonemeDeviation(0, 1, 31.5f), PhonemeDeviation(2, 4, 0.25f)),
        words = listOf(WordFault(12, 16)),
        added = listOf(AddedSound("s", 4), AddedSound("ɑ n", -1)),
        gutters = listOf(Gutter("ə", 11, 12.5f), Gutter("n", -1, 0.5f)),
    )

    private val sounds = listOf(
        AnalysedSound(
            symbol = "aɪ", points = 31.5f, letters = "I", borrowed = false,
            at = 0..0,
            model = listOf(Share("aɪ", 0.9f), Share("ɑ", 0.1f)),
            said = listOf(Share("ɑ", 0.55f)),
            modelMs = 40..180, saidMs = 60..210,
        ),
        AnalysedSound(
            // A sound owning no character at all: IntRange says so by starting past its end,
            // and normalising the two numbers would put its marks on a letter it never had.
            symbol = "ə", points = 2f, letters = "", borrowed = true,
            at = IntRange.EMPTY,
            model = emptyList(), said = emptyList(),
            modelMs = 180..190, saidMs = 210..225,
        ),
    )

    @Test
    fun `a marking survives the round trip`() {
        assertEquals(marking, Marks.read(Marks.write(marking)))
    }

    @Test
    fun `every learner pitch comes back as it went in`() {
        val back = Marks.read(Marks.write(marking)).syllables
        assertEquals(listOf(-4.25f, 6.5f, null), back.map { it.learnerPitch })
        assertEquals(listOf(2.5f, -1.75f, 0.5f), back.map { it.modelPitch })
    }

    @Test
    fun `the sounds survive the round trip`() {
        assertEquals(sounds, Marks.readSounds(Marks.writeSounds(sounds)))
    }

    @Test
    fun `a sound that owns no character still owns none`() {
        val back = Marks.readSounds(Marks.writeSounds(sounds))[1]
        assertEquals(true, back.at.isEmpty())
    }
}
