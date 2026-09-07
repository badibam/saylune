package app.speakup.capture

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import app.speakup.debug.Trace
import app.speakup.levers.At
import app.speakup.levers.Count
import app.speakup.levers.Levers
import app.speakup.levers.Positions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
 * **Two clocks run while it records, and both are visible**: how long the turn has lasted,
 * and how long the current silence has. Each can close the turn, and [CaptureState.ending]
 * says which did -- a fact about the recording rather than a measure, read by the language
 * model, which it forbids to complete an unfinished sentence, and by the sheet of the
 * interrupted turn.
 *
 * **A turn a clock closed is truncated and sent as it stands**, never cut into two turns:
 * what was left to say is never captured, the mic reopening only after the AI has answered.
 *
 * The ceiling on the turn's length is [Capture.ceilingMs], which the `duree-tour` lever sets
 * -- asking for an answer in five seconds and tolerating thirty at most are the same variable
 * set differently. Its top is technical: a pass of the analysis grows as the **square** of
 * the turn's length -- 4316 MB on one minute of audio, enough to kill a 4 GB device -- and no
 * windowing is written yet. The top goes when the windowing lands, and the lever stays
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
    fun open(scope: CoroutineScope, capture: Capture = Capture()) {
        if (job != null || _state.value.ending != null) return
        val file = pcm ?: newFile().also { pcm = it }
        _state.value = _state.value.copy(recording = true, silenceMs = 0)
        job = scope.launch(Dispatchers.IO) { read(file, capture) }
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
        _state.value = _state.value.copy(recording = false, silenceMs = 0)
    }

    /** Throw the turn away and start over. */
    fun discard() {
        pause()
        pcm?.delete()
        pcm = null
        _state.value = CaptureState()
    }

    /**
     * Close the turn and hand back both readings of it. Null when nothing was said -- a
     * caller must not send an empty file to a provider and read the answer as a turn.
     */
    suspend fun send(): Take? = withContext(Dispatchers.IO) {
        pause()
        val samples = pcm?.takeIf { it.length() > 0 } ?: return@withContext null
        // **The empty stretches come out here**, between the recording and everything that
        // reads it. What is kept on disk is the speech alone -- silence costs its length and
        // no samples -- and what every reader gets is the turn put back together, silence
        // included: the analysis rests on continuous frames, and stripping one of the two
        // recordings and not the other is the asymmetry the whole montage exists to avoid.
        val stretches = Segments.cut(samples)
        val speech = File(samples.parentFile, samples.nameWithoutExtension + ".speech")
        Segments.keep(samples, speech, stretches)
        val rebuilt = File(samples.parentFile, samples.nameWithoutExtension + ".whole")
        Segments.rebuild(speech, rebuilt, stretches)
        Trace.add(
            "capture: stretches",
            "speech / silence" to
                "${stretches.count { it.speech }} / ${stretches.count { !it.speech }}",
            "speech / recorded" to "${speech.length()} / ${samples.length()} bytes",
        )

        val name = samples.nameWithoutExtension
        val whole = File(samples.parentFile, "$name.wav")
        WavFile.wrap(rebuilt, whole)
        val spoken = File(samples.parentFile, "$name-speech.wav")
        WavFile.wrap(speech, spoken)
        rebuilt.delete()
        speech.delete()
        samples.delete()
        pcm = null
        _state.value = CaptureState()
        Take(whole, spoken)
    }

    @SuppressLint("MissingPermission") // The screen holds the button behind the grant.
    private suspend fun read(file: File, capture: Capture) {
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
        // Silence that has run without a break. It starts at zero on every open, so a mic
        // opened by hand starts its count at the press -- which is what the take actually
        // holds, the delay before the press never having been recorded.
        var silence = 0
        try {
            recorder.startRecording()
            java.io.FileOutputStream(file, true).use { out ->
                while (currentCoroutineContext().isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read <= 0) continue
                    out.write(buffer, 0, read)
                    written += read
                    val elapsed = elapsedMs(written)
                    silence =
                        if (Silence.level(buffer, read) < Silence.LEVEL) silence + msOf(read)
                        else 0
                    // **Only while the state still says recording**, and that guard is the
                    // whole reason this is an `update` and not an assignment. `AudioRecord.read`
                    // does not come back when the coroutine is cancelled, so a take that is
                    // being closed leaves this loop one last write, landing after `send` has
                    // already put the state back to empty. The capture then reads as a paused
                    // turn holding an elapsed time with no recording behind it -- and nothing
                    // but `discard` gets out of it, the mic refusing to reopen on a take that
                    // is still there. Everything that closes a take clears `recording` first,
                    // so that flag is what tells a live write from a late one.
                    _state.update {
                        if (it.recording) it.copy(elapsedMs = elapsed, silenceMs = silence)
                        else it
                    }
                    // The turn's length first: it holds at all three positions, and a turn
                    // that reaches it has been speaking, not waiting.
                    if (elapsed >= capture.ceilingMs) {
                        closed(silence, Ending.ByLength)
                        break
                    }
                    val sends = capture.sendsAfterMs
                    if (sends != null && silence >= sends) {
                        closed(silence, Ending.BySilence)
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

    /**
     * A clock closed the take, written under the same guard as the tick above.
     *
     * A hand that pressed `SEND` in the same moment has already cleared `recording`, and an
     * ending laid on top of the empty state would lock the mic shut: [open] refuses to reopen
     * on a take that ended, and nothing would clear it but `discard`.
     */
    private fun closed(silence: Int, ending: Ending) = _state.update {
        if (it.recording) it.copy(recording = false, silenceMs = silence, ending = ending)
        else it
    }

    private fun elapsedMs(bytes: Long): Int = msOf(bytes)

    private fun msOf(bytes: Number): Int =
        (bytes.toLong() * 1000 /
            (WavFile.SAMPLE_RATE * WavFile.CHANNELS * WavFile.BITS / 8)).toInt()

    companion object {
        /**
         * The highest a turn's length can be set to, which is the technical top and not the
         * setting: it is what a pass of the analysis can hold. Provisional, and it goes when
         * the analysis pass is windowed.
         */
        const val CEILING_MS = 30_000
    }
}

/**
 * One finished turn, in the two readings the app needs of it.
 *
 * [whole] is the turn as it was said, the empty stretches rebuilt as silence. **Everything
 * measured reads this one**: the analysis rests on continuous frames and on the rule that
 * nothing is done to one of the two recordings alone -- the model's render carries its own
 * silences and nobody strips those -- so a take with its blanks taken out would be an
 * asymmetry dressed as an economy.
 *
 * [spoken] is the speech alone, and it exists for one sentence of the doc: **no silence goes
 * to the network.** It is what the recognition is given, which has no use for the blanks and
 * charges for the seconds.
 */
data class Take(val whole: File, val spoken: File)

/**
 * What the two clocks are set to for this turn, read off the sitting's levers.
 *
 * [sendsAfterMs] is null wherever the silence does not send, which is the first two capture
 * positions: there, the learner is the only one who sends. It is not zero -- zero would send
 * the turn at once, which is why the threshold and the position are two levers and not one.
 */
data class Capture(
    val ceilingMs: Int = TurnRecorder.CEILING_MS,
    val sendsAfterMs: Int? = null,
) {
    companion object {

        /**
         * What this sitting's levers set the two clocks to.
         *
         * Read in one place and never at each site that wants a clock: the buttons, the status
         * line and the recorder all want the same two numbers, and three derivations of one
         * setting are three chances of disagreeing about when a turn ends.
         */
        fun of(positions: Positions): Capture = Capture(
            ceilingMs = seconds(positions, Levers.TURN_LENGTH.key, TurnRecorder.CEILING_MS),
            // Null wherever the silence does not send, which is the first two positions:
            // there the learner is the only one who sends. Not zero -- zero would send at once.
            sendsAfterMs =
                if ((positions.of(Levers.CAPTURE.key) as? At)?.name == Levers.ARMED_AND_SENDING)
                    seconds(positions, Levers.SILENCE_THRESHOLD.key, DEFAULT_SILENCE_MS)
                else null,
        )

        /**
         * A numeric lever read in milliseconds, or [fallback] where it carries no number.
         *
         * A lever with no number is one whose position is *no maximum* -- the shape the
         * catalogue gives a ceiling that does not exist. The clocks want a number either way,
         * so the caller says what standing for *no limit* means to it.
         */
        private fun seconds(positions: Positions, key: String, fallback: Int): Int =
            (positions.of(key) as? Count)?.n?.times(1000) ?: fallback

        private const val DEFAULT_SILENCE_MS = 5_000
    }
}

/**
 * How a turn stopped recording, when something other than a hand stopped it.
 *
 * **Two causes, one sheet.** Both are the interrupted turn, and both are facts about the
 * recording rather than measures: the language model reads them, which is what forbids it to
 * complete an unfinished sentence, and the sheet of the interrupted turn reads them too.
 */
enum class Ending {
    /** The turn reached the length it was allowed. It exists at all three positions. */
    ByLength,
    /** The silence ran past its threshold. Only the third capture position has this one. */
    BySilence,
}

/**
 * What the screen draws.
 *
 * [elapsedMs] and [silenceMs] are **the two countdowns, visible at all times** -- the time
 * the turn has run and the silence running now. Two times running out, shown the same way.
 * They are not a lever: they say what the levers have set.
 *
 * [ending] is a clock having closed the turn by itself, which is shown rather than suffered
 * -- a turn that stops on its own must say why.
 */
data class CaptureState(
    val recording: Boolean = false,
    val elapsedMs: Int = 0,
    val silenceMs: Int = 0,
    val ending: Ending? = null,
) {
    val hasAudio: Boolean get() = elapsedMs > 0
}
