package app.saylune.chain

import java.io.File

/**
 * The voice: the one the learner hears, and the one the analysis measures against.
 *
 * These are the same voice by default and by decision -- the accent setting governs both,
 * and there is no third thing to align, since the analysis consults no dialect reference and
 * the model is the only norm (`docs/reference.md`).
 *
 * Nothing is asked of the provider but a wav. Anchoring marks to the text is computed
 * entirely on the device, from order and spelling, so every synthesis engine is a candidate
 * -- the free ones included. None is chosen.
 *
 * One render serves three times, which is why the caller caches it and this seam does not:
 * a yardstick for the measure, a model to hear, and a model to hear again at every retry.
 */
interface Synthesis {

    /**
     * A wav of [text] in [voice]. Throws [ChainFailure] -- and a failure while someone is
     * saying a sentence again is said plainly rather than worked around, because redoing a
     * sentence with no model to hear is self-assessment by ear, which the architecture
     * refuses everywhere else.
     */
    suspend fun speak(text: String, voice: Voice): File
}

/**
 * A voice at a provider, which is the pair the settings screen ends its cascade on.
 *
 * A voice promoted to model passes a test, and a voice that fails it stays usable for
 * talking while being flagged unfit to measure against -- what that test checks is still to
 * be redefined, and no voice is qualified today.
 */
data class Voice(val provider: String, val id: String)
