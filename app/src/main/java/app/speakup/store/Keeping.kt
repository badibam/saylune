package app.speakup.store

import app.speakup.activity.Activity
import app.speakup.activity.Outcome
import app.speakup.activity.Prescriber
import app.speakup.activity.Status
import app.speakup.capture.Ending
import app.speakup.conversation.Attempt
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
    settings = Sitting.write(settings),
    brief = brief?.let { Sitting.write(it) },
    weights = weights?.let { Sitting.write(it) },
    instructions = Sitting.writeInstructions(instructions),
    rules = Rules.write(rules),
    journal = Sitting.writeJournal(journal),
    origin = origin?.let { Sitting.write(it) },
    engine = engine,
    status = status.name,
    createdAt = createdAt,
    startedAt = startedAt,
    endedAt = endedAt,
    outcome = outcome?.let { OutcomeRow(it.verdict, it.judge, it.at, it.says, it.score) },
    prescriber = by.name,
)

internal fun ActivityRow.activity() = Activity(
    id = id,
    matter = matter,
    settings = Sitting.readPositions(settings),
    brief = brief?.let { Sitting.readBrief(it) },
    weights = weights?.let { Sitting.readWeights(it) },
    instructions = Sitting.readInstructions(instructions),
    rules = Rules.read(rules),
    journal = Sitting.readJournal(journal),
    origin = origin?.let { Sitting.readOrigin(it) },
    engine = engine,
    status = Status.valueOf(status),
    createdAt = createdAt,
    startedAt = startedAt,
    endedAt = endedAt,
    outcome = outcome?.takeIf { it.verdict != null }?.let {
        Outcome(it.verdict!!, it.judge.orEmpty(), it.at ?: createdAt, it.says.orEmpty(), it.score)
    },
    by = Prescriber.valueOf(prescriber),
)

internal fun Utterance.row(rank: Int) = UtteranceRow(
    id = id,
    activity = activity,
    rank = rank,
    speaker = speaker.key,
    text = text,
    said = said?.path,
    capture = capture,
    ending = ending?.name,
    marking = marking?.let { Marks.write(it) },
    sounds = marking?.let { Marks.writeSounds(sounds) },
    model = model?.path,
    judged = judged?.let { JudgedMarks.write(it) },
    take = take,
    repeats = repeats,
    attempt = attempt?.name,
    answers = answers,
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
    speaker = Speaker(speaker),
    activity = activity,
    text = text,
    said = said?.let { File(it) }?.takeIf { it.isFile },
    capture = capture,
    // By name and never by ordinal, like every other enum that travels through here.
    ending = ending?.let { Ending.valueOf(it) },
    marking = marking?.let { Marks.read(it) },
    sounds = sounds?.let { Marks.readSounds(it) }.orEmpty(),
    model = model?.let { File(it) }?.takeIf { it.isFile },
    judged = judged?.let { JudgedMarks.read(it) },
    take = take,
    repeats = repeats,
    attempt = attempt?.let { Attempt.valueOf(it) },
    answers = answers,
    id = id,
    at = at,
    engine = engine,
)
