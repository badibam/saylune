package app.saylune.marking

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * A turn the bench measured, read from the device rather than compiled in.
 *
 * `bench/turn.py` writes it from real audio -- a learner's take against the synthesised
 * model -- and `adb push` drops it beside the app. Reading it at launch rather than baking
 * it into the APK is what makes the screen worth looking at: a new take is one push away,
 * where an asset would cost a rebuild for every sentence.
 *
 * **Two writers, one shape.** `bench/turn.py` writes it from the bench's own takes, and the
 * app writes the same fields into the file it keeps beside every turn it analysed
 * (`conversation/Takes.kt`) -- so a turn recorded on the phone is pushed back and read here
 * exactly like one of the bench's. What kept the two from drifting was nothing at all, which is
 * how the app came to require a channel the phone's own files did not carry;
 * `TurnFileTest` reads one file of each kind now, and a field that moves fails it.
 *
 * **The contract, and why the absent channels are not a silent fallback.** The phrase and its
 * phoneme marks are required: a file without them is not a turn, and it says so by failing. The
 * other four channels are **optional and absent means empty** -- a file written before a channel
 * existed is the ordinary case here, these files being kept for months and read back by a build
 * that has moved on, and a channel with nothing in it is exactly what it draws.
 */
object TurnFile {

    const val NAME = "turn.json"

    /** Where `adb push` can reach without root, and the app can read without a permission. */
    fun path(context: Context): File? =
        context.getExternalFilesDir(null)?.let { File(it, NAME) }

    /**
     * The turn, or null when no file is there. A file that is present but malformed throws:
     * a silently empty screen would read exactly like a turn with nothing to report, which
     * is the one thing this screen exists to tell apart.
     */
    fun read(context: Context): Turn? {
        val file = path(context) ?: return null
        if (!file.isFile) return null
        return parse(file.readText())
    }

    /** The same reading, off the text alone, so a test can hold two real files to it. */
    fun parse(text: String): Turn {
        val json = JSONObject(text)

        val phonemes = json.getJSONArray("phonemes").let { array ->
            (0 until array.length()).map { index ->
                val entry = array.getJSONObject(index)
                PhonemeDeviation(
                    start = entry.getInt("start"),
                    end = entry.getInt("end"),
                    points = entry.getDouble("points").toFloat(),
                )
            }
        }
        val syllables = json.optJSONArray("syllables").let { array ->
            (0 until (array?.length() ?: 0)).map { index ->
                val entry = array!!.getJSONObject(index)
                Syllable(
                    start = entry.getInt("start"),
                    end = entry.getInt("end"),
                    modelPitch = entry.getDouble("modelPitch").toFloat(),
                    learnerPitch = if (entry.isNull("learnerPitch")) null
                                else entry.getDouble("learnerPitch").toFloat(),
                    modelStressed = entry.getBoolean("modelStressed"),
                    learnerStressed = entry.getBoolean("learnerStressed"),
                )
            }
        }
        val words = json.optJSONArray("words").let { array ->
            (0 until (array?.length() ?: 0)).map { index ->
                val entry = array!!.getJSONObject(index)
                WordFault(start = entry.getInt("start"), end = entry.getInt("end"))
            }
        }
        val added = json.optJSONArray("added").let { array ->
            (0 until (array?.length() ?: 0)).map { index ->
                val entry = array!!.getJSONObject(index)
                AddedSound(
                    symbol = entry.getString("symbol"),
                    after = entry.getInt("after"),
                    saidMs = if (entry.has("from"))
                        entry.getInt("from")..entry.getInt("to") else null,
                )
            }
        }
        val gutters = json.optJSONArray("gutters").let { array ->
            (0 until (array?.length() ?: 0)).map { index ->
                val entry = array!!.getJSONObject(index)
                Gutter(
                    symbol = entry.getString("symbol"),
                    after = entry.getInt("after"),
                    points = entry.getDouble("points").toFloat(),
                )
            }
        }
        return Turn(
            marking = TurnMarking(json.getString("text"), syllables, phonemes, words,
                                  added, gutters),
            take = json.optString("take"),
            model = json.optString("model"),
        )
    }
}

/** The marking, and enough of its provenance to know which take is on screen. */
data class Turn(
    val marking: TurnMarking,
    val take: String,
    val model: String,
)
