package app.speakup.store

import app.speakup.activity.Activity
import app.speakup.activity.Aptitude
import app.speakup.activity.Outcome
import app.speakup.activity.Prescriber
import app.speakup.activity.Settings
import app.speakup.activity.Status
import app.speakup.conversation.Speaker
import app.speakup.conversation.Utterance
import org.json.JSONObject
import java.io.File

/**
 * Between what the app holds and what the tables hold.
 *
 * Kept here rather than on the types themselves: an utterance knows nothing about being
 * stored, and a row knows nothing about being spoken. Putting the translation in one place
 * is what lets either side change without dragging the other with it.
 *
 * The enums travel by name and not by ordinal. An ordinal is a promise never to reorder a
 * list, which nobody remembers making: inserting a status would silently reread every stored
 * row as the status after it.
 */

internal fun Activity.row() = ActivityRow(
    id = id,
    matter = matter,
    settings = settings?.let { levels ->
        JSONObject().apply {
            levels.levels.forEach { (aptitude, level) -> put(aptitude.name, level) }
        }.toString()
    },
    status = status.name,
    createdAt = createdAt,
    startedAt = startedAt,
    endedAt = endedAt,
    outcome = outcome?.let { OutcomeRow(it.verdict, it.judge, it.at, it.says) },
    prescriber = by.name,
)

internal fun ActivityRow.activity() = Activity(
    id = id,
    matter = matter,
    settings = settings?.let { stored ->
        val json = JSONObject(stored)
        Settings(json.keys().asSequence().associate {
            Aptitude.valueOf(it) to json.getDouble(it).toFloat()
        })
    },
    status = Status.valueOf(status),
    createdAt = createdAt,
    startedAt = startedAt,
    endedAt = endedAt,
    outcome = outcome?.takeIf { it.verdict != null }?.let {
        Outcome(it.verdict!!, it.judge.orEmpty(), it.at ?: createdAt, it.says.orEmpty())
    },
    by = Prescriber.valueOf(prescriber),
)

internal fun Utterance.row(rank: Int) = UtteranceRow(
    id = id,
    activity = activity,
    rank = rank,
    speaker = speaker.name,
    text = text,
    said = said?.path,
    marking = marking?.let { Marks.write(it) },
    sounds = marking?.let { Marks.writeSounds(sounds) },
    model = model?.path,
    faulty = faulty,
    take = take,
    repeats = repeats,
    engine = engine,
    at = at,
)

/**
 * The utterance back, with the recordings it names.
 *
 * **A path that no longer points at a file comes back null**, and that is the one place a
 * missing thing is not an error: the audio lives in files the store does not own, and a file
 * can be gone -- pulled off the device, cleared by the system. An utterance whose recording
 * has gone still has its text and its marks, which is most of what it was. Handing back a
 * path to nothing would instead fail at the moment someone pressed play, with nothing said
 * about why.
 */
internal fun UtteranceRow.utterance() = Utterance(
    speaker = Speaker.valueOf(speaker),
    activity = activity,
    text = text,
    said = said?.let { File(it) }?.takeIf { it.isFile },
    marking = marking?.let { Marks.read(it) },
    sounds = sounds?.let { Marks.readSounds(it) }.orEmpty(),
    model = model?.let { File(it) }?.takeIf { it.isFile },
    faulty = faulty,
    take = take,
    repeats = repeats,
    id = id,
    at = at,
    engine = engine,
)
