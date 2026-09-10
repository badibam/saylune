package app.saylune.embedded

import java.io.File

/**
 * One pass of the acoustic model over one audio, wherever it runs.
 *
 * The seam is here and not one level up, at [app.saylune.analysis.Analysis], and the
 * difference is the whole of what may be deported. `Analysis.examine` is handed the
 * reference text; a pass is handed a waveform and nothing else, so a machine that runs one
 * learns an audio and never what was said, what was expected, or what any of it was worth.
 * Grid, join, alignment, overlap, thresholds and marks stay where they are.
 *
 * **It also holds the symmetry the measurement rests on.** `EmbeddedAnalysis` reads the model
 * and the take through the same instance, side by side, so a montage where one side is
 * computed here and the other elsewhere is not something a caller can assemble by accident:
 * the machine's own bias cancels because it is the same bias twice (`docs/reference.md`).
 */
interface AcousticPass {

    /** What produced these readings, in one line, so nothing compares two eras in silence. */
    val version: String

    fun read(wav: File): PassReading

    fun close()
}

/**
 * The matrix of one audio, what the stress probe needs of it, and what the pass cost.
 *
 * [values] is [frames] × [symbols], row-major, each row summing to one: the spread of
 * resemblance over every sound the network knows, every 20 ms.
 */
class PassReading(
    val frames: Int,
    val symbols: Int,
    val values: FloatArray,
    val layer: Layer,
    val millis: Long,
    val seconds: Float,
)

/**
 * Layer 19, in whichever form the pass could afford to keep it.
 *
 * The stress probe is a linear read of it, and a local pass keeps the layer whole because it
 * costs nothing to. A remote one cannot: at 1024 columns a frame it is a thousand times the
 * matrix -- 1.22 MB against 0.05 for a six second turn -- so the projection is applied at the
 * far end and one scalar per frame comes back instead.
 *
 * **The two are the same arithmetic in a different order, not an approximation.** The probe
 * standardises the mean of the layer over a span and dots it with a frozen weight;
 * standardise-then-dot is linear, and averaging commutes with a linear map, so dotting each
 * frame first and averaging after lands on the same number. What differs is where the
 * rounding falls, which is why [Whole] is kept whole wherever it can be: it is the form every
 * figure in the docs was measured on.
 */
sealed interface Layer {

    /** The layer as the network renders it: [width] columns per frame, row-major. */
    class Whole(val hidden: FloatArray, val width: Int) : Layer

    /** The probe already applied, one scalar per frame, bias not yet added. */
    class Folded(val perFrame: FloatArray) : Layer
}
