package app.speakup.capture

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * One turn of the learner's voice, in one or more stretches.
 *
 * **Everything opens with a press**, at all three capture positions (`activity-model.md`).
 * The first one used to be held at the finger, which made it differ from the other two by
 * the *gesture* rather than by what it measures -- and forced any button serving both
 * regimes to have two behaviours that nothing on screen announced. What separates the
 * positions is now who opens the mic and whether the pause exists, and nothing else.
 *
 * [pause] and [send] are what follows, and they are the bottom of the screen: the same two
 * command a new turn and a repeat, whichever button started it. The stretches are unchanged
 * by the switch -- the first position already cut speech into stretches at the thumb -- so
 * what moved is who delimits them, not what they are.
 *
 * The 30-second ceiling is not ergonomics. A pass of the analysis grows as the **square** of
 * the turn's length -- 4316 MB on one minute of audio, enough to kill a 4 GB device -- and
 * no windowing is written yet. So the turn closes itself and says so, the way any unanalysed
 * turn carries its reason. The ceiling goes when the windowing lands
 * (`../../../../../../TODO.md`).
 */
class TurnRecorder(private val context: Context) {

    private val _state = MutableStateFlow(CaptureState())
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    /** Read once: what the device declares does not change under the app's feet. */
    val source: MicSource = MicSource.of(context)

    private var job: Job? = null
    private var pcm: File? = null

    /** Open the mic: begin the turn, or carry on the one already started. */
    fun open(scope: CoroutineScope) {
        if (job != null || _state.value.full) return
        val file = pcm ?: newFile().also { pcm = it }
        _state.value = _state.value.copy(recording = true)
        job = scope.launch(Dispatchers.IO) { read(file) }
    }

    /**
     * Stop recording and hold what has been said. Opening again appends to it.
     *
     * **The pause exists at the first capture position and nowhere else**, since it is what
     * makes the silences unreadable -- between two stretches, the blank measures the thumb.
     * It lives where they are not read, and the screen simply has no such button at the
     * other two positions.
     */
    fun pause() {
        job?.cancel()
        job = null
        _state.value = _state.value.copy(recording = false)
    }

    /** Throw the turn away and start over. */
    fun discard() {
        pause()
        pcm?.delete()
        pcm = null
        _state.value = CaptureState()
    }

    /**
     * Close the turn and hand back the wav. Null when nothing was said -- a caller must not
     * send an empty file to a provider and read the answer as a turn.
     */
    suspend fun send(): File? = withContext(Dispatchers.IO) {
        pause()
        val samples = pcm?.takeIf { it.length() > 0 } ?: return@withContext null
        val wav = File(samples.parentFile, samples.nameWithoutExtension + ".wav")
        WavFile.wrap(samples, wav)
        samples.delete()
        pcm = null
        _state.value = CaptureState()
        wav
    }

    @SuppressLint("MissingPermission") // The screen holds the button behind the grant.
    private suspend fun read(file: File) {
        val minimum = AudioRecord.getMinBufferSize(
            WavFile.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        check(minimum > 0) { "this device will not record 16 kHz mono PCM" }
        // Never MIC and never VOICE_COMMUNICATION: the measure compares the learner to the
        // model through the same machine, so gain or noise suppression applied to one side
        // alone is exactly the asymmetry the whole design exists to avoid. Which of the two
        // acceptable sources the device gives is reported on screen, not swallowed.
        val recorder = AudioRecord(
            source.id,
            WavFile.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minimum * 2,
        )
        check(recorder.state == AudioRecord.STATE_INITIALIZED) {
            "the microphone would not open"
        }
        val buffer = ByteArray(minimum)
        var written = file.length()
        try {
            recorder.startRecording()
            java.io.FileOutputStream(file, true).use { out ->
                while (currentCoroutineContext().isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read <= 0) continue
                    out.write(buffer, 0, read)
                    written += read
                    val elapsed = elapsedMs(written)
                    _state.value = _state.value.copy(elapsedMs = elapsed)
                    if (elapsed >= CEILING_MS) {
                        _state.value = _state.value.copy(recording = false, full = true)
                        break
                    }
                }
            }
        } finally {
            recorder.stop()
            recorder.release()
        }
    }

    private fun newFile(): File {
        val dir = File(context.filesDir, "turns").apply { mkdirs() }
        return File(dir, "${System.currentTimeMillis()}.pcm")
    }

    private fun elapsedMs(bytes: Long): Int =
        (bytes * 1000 / (WavFile.SAMPLE_RATE * WavFile.CHANNELS * WavFile.BITS / 8)).toInt()

    companion object {
        /** Provisional, and it goes when the analysis pass is windowed. */
        const val CEILING_MS = 30_000
    }
}

/**
 * What the screen draws. [full] is the ceiling having closed the turn by itself, which is
 * shown rather than suffered -- a turn that stops on its own must say why.
 */
data class CaptureState(
    val recording: Boolean = false,
    val elapsedMs: Int = 0,
    val full: Boolean = false,
) {
    val hasAudio: Boolean get() = elapsedMs > 0
}
