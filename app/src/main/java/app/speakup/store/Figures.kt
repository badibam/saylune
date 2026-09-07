package app.speakup.store

import org.json.JSONObject

/**
 * What an attempt made of each sheet, written out by path.
 *
 * **What is deduced from the audio is stored** (`docs/design/activity-model.md`): the purge
 * erases the recordings, so a measure that could no longer be recomputed afterwards has to
 * exist somewhere else. Two of the eleven sheets cannot be recomputed from anything the store
 * holds even before the purge -- the continuity, the longest silence and the pace are read off
 * timings the analysis pass produced and nothing keeps -- so the figures are the thing to keep,
 * not the material they came from.
 *
 * **Figures and never letters**, which is the project's rule everywhere: a letter is a note,
 * contextual by construction since the sensitivity moves its bounds, and the bounds have to be
 * free to move without spoiling old sittings.
 *
 * A sheet that did not measure is **absent from the map** and never a zero. That is what the
 * flat aggregation rests on, and it holds here for the same reason: a passage whose words' gate
 * closed has no sound at all, and writing zeros for it would say the sounds were bad.
 */
internal object Figures {

    fun write(byPath: Map<String, Float>): String =
        JSONObject().apply { byPath.forEach { (path, figure) -> put(path, figure.toDouble()) } }
            .toString()

    fun read(stored: String): Map<String, Float> {
        val json = JSONObject(stored)
        return json.keys().asSequence().associateWith { json.getDouble(it).toFloat() }
    }
}
