package app.speakup.capture

import android.content.Context
import android.media.AudioManager
import android.media.MediaRecorder
import androidx.annotation.StringRes
import app.speakup.R

/**
 * Which microphone source the device actually gave us.
 *
 * `UNPROCESSED` guarantees no gain, no noise suppression, no echo cancellation;
 * `VOICE_RECOGNITION` only makes them unlikely. The difference is not cosmetic here --
 * automatic gain would deform loudness, and loudness is a dimension the stress brick reads
 * (`../../../../../../TODO.md`, brick 7 reoriented).
 *
 * Falling back is a decision, taken with the user, not a convenience: a device that does not
 * declare `UNPROCESSED` support would otherwise record nothing at all. So the app says which
 * of the two it obtained rather than keeping the difference to itself -- the same reason an
 * unavailable option carries its reason instead of looking like a bug.
 */
enum class MicSource(val id: Int, @StringRes val label: Int) {
    Unprocessed(MediaRecorder.AudioSource.UNPROCESSED, R.string.mic_unprocessed),
    VoiceRecognition(MediaRecorder.AudioSource.VOICE_RECOGNITION, R.string.mic_voice_recognition),
    ;

    companion object {
        /** The best the device declares. The property is absent on devices that never had it. */
        fun of(context: Context): MicSource {
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val declared =
                audio.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)
            return if (declared == "true") Unprocessed else VoiceRecognition
        }
    }
}
