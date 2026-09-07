package app.saylune.store

import app.saylune.analysis.AnalysedSound
import app.saylune.analysis.Share
import app.saylune.marking.AddedSound
import app.saylune.marking.Gutter
import app.saylune.marking.PhonemeDeviation
import app.saylune.marking.Syllable
import app.saylune.marking.TurnMarking
import app.saylune.marking.WordFault
import org.json.JSONArray
import org.json.JSONObject

/**
 * The marks, written out and read back as they are.
 *
 * **Archived exactly as the screen shows them, with no model added on top** -- plain facts,
 * whose later uses derive themselves if they ever come (`docs/reference.md`). That is why
 * this is a text column and not five tables: five tables would be the added model the doc
 * refuses, and nothing asks a question of a single mark.
 *
 * Every offset here indexes the utterance's own text, so a marking read back is only ever
 * shown against the string it was measured on. Reading it against another would slide every
 * mark by however far the two differ.
 */
internal object Marks {

    fun write(marking: TurnMarking): String = JSONObject()
        .put("text", marking.text)
        .put("syllables", JSONArray().apply {
            marking.syllables.forEach {
                put(JSONObject()
                    .put("start", it.start).put("end", it.end)
                    .put("modelPitch", it.modelPitch)
                    // Null on purpose and never zero: nothing of the syllable was voiced,
                    // which is not the same as flat and must not read alike.
                    .put("learnerPitch", it.learnerPitch ?: JSONObject.NULL)
                    .put("modelStressed", it.modelStressed)
                    .put("learnerStressed", it.learnerStressed))
            }
        })
        .put("phonemes", JSONArray().apply {
            marking.phonemes.forEach {
                put(JSONObject().put("start", it.start).put("end", it.end)
                    .put("points", it.points))
            }
        })
        .put("words", JSONArray().apply {
            marking.words.forEach {
                put(JSONObject().put("start", it.start).put("end", it.end))
            }
        })
        .put("added", JSONArray().apply {
            marking.added.forEach {
                put(JSONObject().put("symbol", it.symbol).put("after", it.after)
                    .apply { it.saidMs?.let { at -> put("from", at.first).put("to", at.last) } })
            }
        })
        .put("gutters", JSONArray().apply {
            marking.gutters.forEach {
                put(JSONObject().put("symbol", it.symbol).put("after", it.after)
                    .put("points", it.points))
            }
        })
        .toString()

    fun read(stored: String): TurnMarking {
        val json = JSONObject(stored)
        return TurnMarking(
            text = json.getString("text"),
            syllables = json.getJSONArray("syllables").each {
                Syllable(
                    start = it.getInt("start"),
                    end = it.getInt("end"),
                    modelPitch = it.getDouble("modelPitch").toFloat(),
                    learnerPitch = if (it.isNull("learnerPitch")) null
                                   else it.getDouble("learnerPitch").toFloat(),
                    modelStressed = it.getBoolean("modelStressed"),
                    learnerStressed = it.getBoolean("learnerStressed"),
                )
            },
            phonemes = json.getJSONArray("phonemes").each {
                PhonemeDeviation(it.getInt("start"), it.getInt("end"),
                                 it.getDouble("points").toFloat())
            },
            words = json.getJSONArray("words").each {
                WordFault(it.getInt("start"), it.getInt("end"))
            },
            added = json.getJSONArray("added").each {
                AddedSound(
                    it.getString("symbol"), it.getInt("after"),
                    // Absent on a turn read before the place was carried: the row is then not
                    // playable, which is the truth about it rather than a guess at where it was.
                    saidMs = if (it.has("from")) it.getInt("from")..it.getInt("to") else null,
                )
            },
            gutters = json.getJSONArray("gutters").each {
                Gutter(it.getString("symbol"), it.getInt("after"),
                       it.getDouble("points").toFloat())
            },
        )
    }

    fun writeSounds(sounds: List<AnalysedSound>): String = JSONArray().apply {
        sounds.forEach { sound ->
            put(JSONObject()
                .put("symbol", sound.symbol)
                .put("points", sound.points)
                .put("letters", sound.letters)
                .put("borrowed", sound.borrowed)
                .put("at", span(sound.at))
                .put("modelMs", span(sound.modelMs))
                .put("saidMs", span(sound.saidMs))
                .put("model", shares(sound.model))
                .put("said", shares(sound.said)))
        }
    }.toString()

    fun readSounds(stored: String): List<AnalysedSound> =
        JSONArray(stored).each { sound ->
            AnalysedSound(
                symbol = sound.getString("symbol"),
                points = sound.getDouble("points").toFloat(),
                letters = sound.getString("letters"),
                borrowed = sound.getBoolean("borrowed"),
                at = range(sound.getJSONArray("at")),
                model = shares(sound.getJSONArray("model")),
                said = shares(sound.getJSONArray("said")),
                modelMs = range(sound.getJSONArray("modelMs")),
                saidMs = range(sound.getJSONArray("saidMs")),
            )
        }

    private fun span(range: IntRange) = JSONArray(listOf(range.first, range.last))

    /**
     * An empty range writes as `first > last` and reads back the same way.
     *
     * A sound can own no character at all, and `IntRange` says so by having its start past
     * its end. Normalising the two numbers on the way through would turn "owns nothing" into
     * "owns one character", and every mark on that sound would move onto a letter the sound
     * never claimed.
     */
    private fun range(array: JSONArray) = array.getInt(0)..array.getInt(1)

    private fun shares(shares: List<Share>) = JSONArray().apply {
        shares.forEach { put(JSONObject().put("symbol", it.symbol).put("part", it.part)) }
    }

    private fun shares(array: JSONArray) = array.each {
        Share(it.getString("symbol"), it.getDouble("part").toFloat())
    }

    private fun <T> JSONArray.each(read: (JSONObject) -> T): List<T> =
        (0 until length()).map { read(getJSONObject(it)) }
}
