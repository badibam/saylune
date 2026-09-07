package app.speakup.store

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/**
 * What was said, kept.
 *
 * **Stored is what depends on something that will not be found again** -- the audio of a
 * moment, the judgement of a judge, a version of a model. **Recomputed is everything that
 * depends only on the rows**: how many utterances, their order, the durations, anything a
 * count is enough to produce (`docs/activity.md`). So there is no column here
 * for the length of a run, none for the rank of a take, and none for the thread of a
 * conversation, which is the ordered run itself.
 *
 * The audio stays in files and only its path is a column. A recording is the one thing a
 * store is worst at holding and a filesystem is best at, and the design wants it kept rather
 * than inlined.
 */
@Entity(tableName = "activities")
data class ActivityRow(
    @PrimaryKey val id: String,
    /**
     * Where every lever of this sitting sits, written out.
     *
     * **Always on the line, a definition or no definition**: a definition is a template
     * applied at creation and not a dependency kept afterwards, so there is never a pointer
     * here that a reader would have to follow to know how the sitting was set.
     */
    val settings: String,
    /** What opens it. Null in a free conversation until the learner writes one. */
    val brief: String?,
    /** What it looks at. Null while nothing weighs anything. */
    val weights: String?,
    /** Who speaks in it. Null on a sitting made before there were definitions. */
    val cast: String?,
    /** The instructions in force at the start, by judged marking. */
    val instructions: String,
    /** What may change during it, and when. Empty until a definition declares any. */
    val rules: String,
    /** What a draw or the AI chose, in order. Without it the sitting stops recomputing. */
    val journal: String,
    /** Which definition it came from and at which version. Null for a free conversation. */
    val origin: String?,
    /** Which rules engine produced the journal. A change of it takes the resumption away. */
    val engine: Int,
    val status: String,
    val createdAt: Long,
    val startedAt: Long?,
    val endedAt: Long?,
    @Embedded(prefix = "outcome_") val outcome: OutcomeRow?,
    val prescriber: String,
)

/** One sitting and how many passages it holds. */
data class PassageCount(val activity: String, val n: Int)

/** How it went. Null throughout while nothing has judged it. */
data class OutcomeRow(
    val verdict: String?,
    val judge: String?,
    val at: Long?,
    /** A number where the sitting produces one -- a score. Null everywhere else. */
    val score: Int?,
)

/**
 * One thing said, tied to the single activity it belongs to.
 *
 * [rank] is its place in the run and is stored, which looks like the derived thing the design
 * says not to keep and is not: the order utterances were inserted in is not a fact SQL will
 * give back, only a habit of the engine. What is derived from the run -- the readings of a
 * turn, the model to imitate, the rank of the next take -- has no column.
 *
 * Deleting an activity takes its utterances with it, because an utterance with no activity is
 * an utterance with nothing to say where it came from.
 */
@Entity(
    tableName = "utterances",
    foreignKeys = [ForeignKey(
        entity = ActivityRow::class,
        parentColumns = ["id"],
        childColumns = ["activity"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("activity")],
)
data class UtteranceRow(
    @PrimaryKey val id: String,
    val activity: String,
    val rank: Int,
    /** Who said it, by identity: the reserved key of the learner, or a character's. */
    val speaker: String,
    val text: String,
    /** Where the recording is. Null for the answers, which are not recorded. */
    val said: String?,
    /** Which capture position was in force. Null on anything that was not recorded. */
    val capture: String?,
    /**
     * How long the recording ran, in milliseconds. Null where nothing analysed it.
     *
     * It is what the **closing silence** is read against -- the stretch between the last sound
     * and the end of the recording -- and it could not be recomputed from the marks, which say
     * where each sound was and never where the tape stopped.
     */
    val recorded: Int?,
    /** Which clock closed the turn, or null when a hand sent it. */
    val ending: String?,
    /** The marks, as the screen shows them. Null when nothing read this. */
    val marking: String?,
    /**
     * What the model settled on this turn, by question key. Null on nearly every one.
     *
     * **On the turn that settled it and nowhere else**: the series of answers to a question is
     * the run in order, and the passage that dates each one is where its turn sits, so a table
     * beside the run would be a second source that could fall out of step with it.
     */
    val established: String?,
    val sounds: String?,
    /** What each sheet made of it, by path. Null where nothing measured it. */
    val measured: String?,
    /** Which side of the model the pace fell on -- true for slower. Null where unmeasured. */
    val slower: Boolean?,
    val model: String?,
    /** What the language model marked, as it came. Null when nothing judged this. */
    val judged: String?,
    val take: String?,
    /** The identity of the utterance this says again. */
    val repeats: String?,
    /** Which of the two repairs it is: a rewording, or the same words again. */
    val attempt: String?,
    /** For a turn of the AI, the learner's utterance it answers. */
    val answers: String?,
    val engine: String?,
    val at: Long,
)

@Dao
interface ArchiveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(activity: ActivityRow)

    @Update
    suspend fun update(activity: ActivityRow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(utterance: UtteranceRow)

    /**
     * Every conversation, most recent first.
     *
     * No filter on status. **Carrying on is continuing an activity that did not finish**, and
     * nothing has to be finished for a new one to begin -- so an unfinished conversation is
     * the ordinary state of every conversation but the one being had, and hiding them would
     * hide the whole list. Sorting and filtering it is for later.
     */
    @Query("SELECT * FROM activities ORDER BY createdAt DESC")
    fun conversations(): Flow<List<ActivityRow>>

    /**
     * The most recent one, to reopen at launch. Null the very first time.
     *
     * **Abandoned is out**: it is the status *start over* writes, and a sitting put out of
     * reach that the launch reopened would be reachable again by the one door nobody chose.
     */
    @Query("SELECT * FROM activities WHERE status != 'Abandoned' ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(): ActivityRow?

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun activity(id: String): ActivityRow?

    /** Every recording any utterance names, across every conversation. */
    @Query("SELECT said FROM utterances WHERE said IS NOT NULL")
    suspend fun recordings(): List<String>

    @Query("SELECT * FROM utterances WHERE activity = :activity ORDER BY rank ASC")
    suspend fun utterances(activity: String): List<UtteranceRow>

    /**
     * How many passages each sitting holds, which is what a tile shows.
     *
     * **A count of rows and nothing stored** (`docs/ui.md`): a passage is a turn of the
     * learner that opens one, so the attempts under it -- a rewording, a repeat -- are what
     * `attempt` names and are not counted again.
     */
    @Query(
        "SELECT activity, COUNT(*) AS n FROM utterances " +
            "WHERE speaker = :learner AND attempt IS NULL GROUP BY activity"
    )
    fun passages(learner: String = "learner"): Flow<List<PassageCount>>
}

@Database(entities = [ActivityRow::class, UtteranceRow::class], version = 13)
abstract class Archive : RoomDatabase() {

    abstract fun dao(): ArchiveDao

    companion object {

        /**
         * Drops `format`, which named a kind of activity there was only ever one of.
         *
         * An activity is a conversation and nothing else: what looked like other formats --
         * reading a text aloud, repeating after a model -- are moments inside a conversation
         * rather than shapes beside it, so a column every row filled the same way described an
         * intention and not the model (`docs/activity.md`).
         *
         * The table is rebuilt rather than altered: `ALTER TABLE ... DROP COLUMN` arrived in
         * SQLite 3.35, and `minSdk` 26 ships 3.18.
         */
        private val DROP_FORMAT = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `activities_new` (`id` TEXT NOT NULL, `matter` TEXT NOT NULL, " +
                        "`settings` TEXT, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "`startedAt` INTEGER, `endedAt` INTEGER, `prescriber` TEXT NOT NULL, " +
                        "`outcome_verdict` TEXT, `outcome_judge` TEXT, `outcome_at` INTEGER, " +
                        "`outcome_says` TEXT, PRIMARY KEY(`id`))",
                )
                db.execSQL(
                    "INSERT INTO `activities_new` SELECT `id`, `matter`, `settings`, `status`, " +
                        "`createdAt`, `startedAt`, `endedAt`, `prescriber`, `outcome_verdict`, " +
                        "`outcome_judge`, `outcome_at`, `outcome_says` FROM `activities`",
                )
                db.execSQL("DROP TABLE `activities`")
                db.execSQL("ALTER TABLE `activities_new` RENAME TO `activities`")
            }
        }

        /**
         * Replaces `faulty` with the marking that absorbed it.
         *
         * `faulty` was one boolean over the whole turn: it flattened the fact that a passage
         * can carry several faults, and left the portion concerned with nowhere to be marked.
         * What replaces it is one notch per group of words, which settles both.
         *
         * **The old verdicts are not carried over, and that is not a loss to repair.** A
         * boolean cannot be turned into spans -- nothing in it says which words -- and a
         * marking invented at migration time would be indistinguishable from one a judge
         * made. The turns keep their text, their audio and their sound marks; they carry no
         * judged marking, which is exactly true of them.
         *
         * The table is rebuilt rather than altered: `ALTER TABLE ... DROP COLUMN` arrived in
         * SQLite 3.35, and `minSdk` 26 ships 3.18.
         */
        private val JUDGED_MARKING = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `utterances_new` (`id` TEXT NOT NULL, `activity` TEXT NOT NULL, " +
                        "`rank` INTEGER NOT NULL, `speaker` TEXT NOT NULL, `text` TEXT NOT NULL, " +
                        "`said` TEXT, `marking` TEXT, `sounds` TEXT, `model` TEXT, `judged` TEXT, " +
                        "`take` TEXT, `repeats` TEXT, `engine` TEXT, `at` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), FOREIGN KEY(`activity`) REFERENCES `activities`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL(
                    "INSERT INTO `utterances_new` SELECT `id`, `activity`, `rank`, `speaker`, " +
                        "`text`, `said`, `marking`, `sounds`, `model`, NULL, `take`, `repeats`, " +
                        "`engine`, `at` FROM `utterances`",
                )
                db.execSQL("DROP TABLE `utterances`")
                db.execSQL("ALTER TABLE `utterances_new` RENAME TO `utterances`")
                db.execSQL("CREATE INDEX `index_utterances_activity` ON `utterances` (`activity`)")
            }
        }

        /**
         * Adds what a turn carries about its own recording: the capture position, and which
         * clock closed it.
         *
         * **Every turn carries its capture position**, without which nothing says whether its
         * silences mean anything, and nothing aggregates across positions. And it carries how
         * it ended -- sent, or interrupted and by which of the two clocks -- which is a fact
         * about the recording rather than a measure, read by the language model and by the
         * sheet of the interrupted turn.
         *
         * **Both stay null on the turns already stored, and that is what is true of them.**
         * They were recorded before either fact existed; writing in the position that happens
         * to be the default today would say they were caught under a regime nobody chose, and
         * nothing downstream could tell that apart from a turn that really was.
         */
        private val CAPTURE_FACTS = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `capture` TEXT")
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `ending` TEXT")
            }
        }

        /**
         * The activity in the shape the design asks for: seven fields it was missing, and an
         * eighth that changes type.
         *
         * **The settings stop being one level per aptitude and become a list of lever
         * positions**, which is exactly what the doc says to store: an aptitude is a preset
         * over levers and not a parameter of its own, so one level per aptitude could not say
         * what a hand-set sitting was and would have needed a second way of describing the
         * same sitting, with the two kept in step.
         *
         * **The old levels are not carried over, and that is not a loss to repair.** A level
         * per aptitude does not translate into lever positions -- nothing in it says which
         * levers, or where -- and positions invented at migration time would be
         * indistinguishable from ones somebody chose. The sittings keep their matter, their
         * status and their utterances; they carry no settings, which is exactly true of them:
         * nothing ever wrote one, the field having had no producer.
         *
         * The rest arrive empty, which is what they are: no brief, no weights, no
         * instructions, no rules, nothing chosen, no definition behind them. The engine is
         * stamped at the version this build interprets by, since a journal with nothing in it
         * replays under any semantics.
         *
         * The table is rebuilt rather than altered: `ALTER TABLE ... DROP COLUMN` arrived in
         * SQLite 3.35, and `minSdk` 26 ships 3.18.
         */
        private val ACTIVITY_IN_SHAPE = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `activities_new` (`id` TEXT NOT NULL, `matter` TEXT NOT NULL, " +
                        "`settings` TEXT NOT NULL, `brief` TEXT, `weights` TEXT, " +
                        "`instructions` TEXT NOT NULL, `rules` TEXT NOT NULL, " +
                        "`journal` TEXT NOT NULL, `origin` TEXT, `engine` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "`startedAt` INTEGER, `endedAt` INTEGER, `prescriber` TEXT NOT NULL, " +
                        "`outcome_verdict` TEXT, `outcome_judge` TEXT, `outcome_at` INTEGER, " +
                        "`outcome_says` TEXT, `outcome_score` INTEGER, PRIMARY KEY(`id`))",
                )
                db.execSQL(
                    "INSERT INTO `activities_new` SELECT `id`, `matter`, '{}', NULL, NULL, " +
                        "'[]', '[]', '[]', NULL, ${app.speakup.activity.Activity.ENGINE}, " +
                        "`status`, `createdAt`, `startedAt`, `endedAt`, `prescriber`, " +
                        "`outcome_verdict`, `outcome_judge`, `outcome_at`, `outcome_says`, " +
                        "NULL FROM `activities`",
                )
                db.execSQL("DROP TABLE `activities`")
                db.execSQL("ALTER TABLE `activities_new` RENAME TO `activities`")
            }
        }

        /**
         * The speaker becomes an identity rather than learner-or-AI.
         *
         * An activity points at a **cast** and not at one interlocutor, so an utterance has to
         * say which of them is speaking -- the synthesis picks a voice per utterance, and on
         * the model's side the AI returns the key of the character talking.
         *
         * The two names that existed map onto the two identities that exist: the learner is
         * the reserved key, and every AI turn stored so far was said by the one voice a free
         * conversation has. **Nothing is invented**, because there were only ever two, and any
         * other value is left as it stands rather than folded into one of them -- a row this
         * build cannot read is better than a row it reads as somebody else.
         */
        private val SPEAKER_IDENTITY = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "UPDATE `utterances` SET `speaker` = " +
                        "'${app.speakup.conversation.Speaker.LEARNER}' WHERE `speaker` = 'Learner'",
                )
                db.execSQL(
                    "UPDATE `utterances` SET `speaker` = " +
                        "'${app.speakup.conversation.Speaker.SPEAKUP}' WHERE `speaker` = 'Ai'",
                )
            }
        }

        /**
         * What an attempt is, and what a reply answers.
         *
         * `repeats` said **which** utterance was being attempted again; it now says **how** as
         * well, because a rewording remakes the exchange where a repeat leaves it alone. And a
         * reply of the AI points at the learner's utterance it answered, which is what lets a
         * reply be **superseded rather than deleted** when a rewording makes a fresh one.
         *
         * **The rows already there get neither, and that is true of them.** Every attempt
         * stored so far was a repeat -- rewording did not exist -- but writing that in would be
         * stamping a distinction on turns taken before there was one, and nothing downstream
         * could tell it from a distinction somebody made. The same for the replies: which
         * utterance each answered is recoverable from the order and is not the same thing as
         * having been recorded, and the one reader is a history that only ever looks at the
         * passage still open.
         */
        private val ATTEMPT_AND_ANSWER = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `attempt` TEXT")
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `answers` TEXT")
            }
        }


        /**
         * The stored keys go English, like the code that reads them.
         *
         * Every one of them lives inside JSON in a text column -- lever keys and their
         * positions in `settings`, node paths in `weights`, both again in `rules` and
         * `journal`, and the judge's notches in `judged`. So the rewrite is a token
         * substitution, and it is **quoted on both sides**: replacing a bare `non` would eat
         * the first French word that contains one, in a brief the learner wrote.
         *
         * The list is the exact inverse of the rename, longest first so that no key is eaten
         * by one of its own prefixes -- `redites` would otherwise swallow `redites-permises`.
         */
        private val KEYS_IN_ENGLISH = object : Migration(7, 8) {
            private val moved = listOf(
            "fluidite/remplissage-reprises" to "fluency/stumbling",
            "fluidite/plus-long-silence" to "fluency/longest-silence",
            "comprehension.fait-refaire" to "understanding.sends-back",
            "elocution/intelligibilite" to "pronunciation/intelligibility",
            "elocution/accent-lexical" to "pronunciation/lexical-stress",
            "correction.fait-refaire" to "correctness.sends-back",
            "reformulations-permises" to "rewordings-allowed",
            "pertinence.fait-refaire" to "relevance.sends-back",
            "elocution.fait-refaire" to "pronunciation.sends-back",
            "correction/correction" to "correctness/correctness",
            "pertinence/pertinence" to "relevance/relevance",
            "fluidite.fait-refaire" to "fluency.sends-back",
            "remplissage-reprises" to "stumbling",
            "elocution/proximite" to "pronunciation/proximity",
            "comprehension/suivi" to "understanding/uptake",
            "fluidite/continuite" to "fluency/continuity",
            "tour-ia.complexite" to "ai-turn.complexity",
            "elocution/melodie" to "pronunciation/melody",
            "tour-ia.affichage" to "ai-turn.display",
            "plus-long-silence" to "longest-silence",
            "tour-ia.longueur" to "ai-turn.length",
            "redites-permises" to "retakes-allowed",
            "armee-et-silence" to "armed-and-sending",
            "entre-les-lignes" to "implied",
            "intelligibilite" to "intelligibility",
            "tour-interrompu" to "interrupted-turn",
            "regle-et-phrase" to "rule-and-sentence",
            "fluidite/debit" to "fluency/pace",
            "ecoutes-modele" to "model-listens",
            "jeter-la-prise" to "discard-take",
            "vies.restantes" to "lives.left",
            "cadence.valeur" to "tempo.value",
            "accent-lexical" to "lexical-stress",
            "reformulations" to "rewordings",
            "tres-difficile" to "very-hard",
            "seuil-silence" to "silence-threshold",
            "comprehension" to "understanding",
            "ne-se-dit-pas" to "not-said",
            "sur-le-sujet" to "on-topic",
            "avance.mots" to "advance.words",
            "explication" to "explanation",
            "pas-de-vies" to "no-lives",
            "tres-facile" to "very-easy",
            "remplissage" to "filler",
            "avance.son" to "advance.sound",
            "duree-tour" to "turn-length",
            "continuite" to "continuity",
            "correction" to "correctness",
            "pertinence" to "relevance",
            "en-rapport" to "on-point",
            "elocution" to "pronunciation",
            "proximite" to "proximity",
            "qui-parle" to "speaker",
            "explicite" to "explicit",
            "mal-forme" to "malformed",
            "abandonne" to "abandoned",
            "difficile" to "hard",
            "reecoute" to "replays",
            "fluidite" to "fluency",
            "brouille" to "scrambled",
            "comptees" to "counted",
            "poursuit" to "carries-on",
            "interdit" to "forbidden",
            "cadence" to "tempo",
            "melodie" to "melody",
            "redites" to "retakes",
            "ecoutes" to "listens",
            "imposee" to "set",
            "moyenne" to "medium",
            "present" to "some",
            "filtre" to "filter",
            "permis" to "allowed",
            "attend" to "waits",
            "courte" to "short",
            "longue" to "long",
            "elevee" to "high",
            "aucune" to "none",
            "marque" to "heavy",
            "absent" to "none",
            "a-cote" to "off-target",
            "facile" to "easy",
            "retenu" to "kept",
            "precis" to "precise",
            "bruit" to "noise",
            "suivi" to "uptake",
            "debit" to "pace",
            "basse" to "low",
            "texte" to "text",
            "aucun" to "none",
            "leger" to "light",
            "regle" to "rule",
            "libre" to "free",
            "doigt" to "by-hand",
            "armee" to "armed",
            "moyen" to "medium",
            "juste" to "apt",
            "vies" to "lives",
            "fort" to "heavy",
            "rien" to "nothing",
            "plat" to "flat",
            "non" to "no",
            "oui" to "yes",
            )

            /**
             * One statement per pair, applied in list order: sqlite's parser stack overflows
             * long before a hundred nested `REPLACE`, so the whole list cannot be one
             * expression. Sequential updates apply the pairs in the same order the nesting
             * would have, which is what the longest-first ordering above needs.
             */
            private fun rewrite(db: SupportSQLiteDatabase, table: String, column: String) {
                moved.forEach { (from, to) ->
                    db.execSQL(
                        "UPDATE `$table` SET `$column` = " +
                            "REPLACE(`$column`, '\"$from\"', '\"$to\"')",
                    )
                }
            }

            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("settings", "weights", "instructions", "rules", "journal").forEach {
                    rewrite(db, "activities", it)
                }
                rewrite(db, "utterances", "judged")
            }
        }

        /**
         * The cast comes onto the line, the free conversation having become a definition.
         *
         * Added as a nullable column rather than one with a default: null says *made before
         * there were definitions*, which is a fact about those rows and not a stand-in for a
         * cast they never had.
         */
        private val CAST_ON_THE_LINE = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `activities` ADD COLUMN `cast` TEXT")
            }
        }

        /**
         * The subject leaves the line: nothing names a sitting but its definition.
         *
         * The column held a label the model wrote, which was the proof of concept's only
         * descriptive field and has no reader left -- a sitting is identified by the file it
         * was opened from. The labels go with it; what those conversations were is in their
         * turns, which nothing here touches.
         *
         * The table is rebuilt rather than altered: dropping a column arrived in SQLite 3.35
         * and `minSdk` 26 ships 3.18.
         */
        private val NAMED_BY_ITS_DEFINITION = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE `activities_new` (`id` TEXT NOT NULL, " +
                        "`settings` TEXT NOT NULL, `brief` TEXT, `weights` TEXT, `cast` TEXT, " +
                        "`instructions` TEXT NOT NULL, `rules` TEXT NOT NULL, " +
                        "`journal` TEXT NOT NULL, `origin` TEXT, `engine` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "`startedAt` INTEGER, `endedAt` INTEGER, `prescriber` TEXT NOT NULL, " +
                        "`outcome_verdict` TEXT, `outcome_judge` TEXT, `outcome_at` INTEGER, " +
                        "`outcome_says` TEXT, `outcome_score` INTEGER, PRIMARY KEY(`id`))",
                )
                db.execSQL(
                    "INSERT INTO `activities_new` SELECT `id`, `settings`, `brief`, `weights`, " +
                        "`cast`, `instructions`, `rules`, `journal`, `origin`, `engine`, " +
                        "`status`, `createdAt`, `startedAt`, `endedAt`, `prescriber`, " +
                        "`outcome_verdict`, `outcome_judge`, `outcome_at`, `outcome_says`, " +
                        "`outcome_score` FROM `activities`",
                )
                db.execSQL("DROP TABLE `activities`")
                db.execSQL("ALTER TABLE `activities_new` RENAME TO `activities`")
            }
        }

        /**
         * What each attempt measured, kept on its line.
         *
         * The figures were computed at the two gate moments and dropped: the gate kept its
         * verdict and nothing kept the numbers behind it. That made a passage unreadable after
         * the fact -- three of the eleven sheets are read off timings the analysis pass
         * produced and nothing else holds, so they could not be recomputed from the store even
         * with the audio still there.
         *
         * The pace's side comes with them, in its own column: its figure is a distance,
         * symmetric so that twice as slow and twice as fast weigh the same, so the side is a
         * fact beside it and never inside it.
         *
         * Null on every existing row, which is exactly right: those sittings measured before
         * anything kept the figures, and inventing them would be inventing measures.
         */
        private val FIGURES_ON_THE_LINE = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `measured` TEXT")
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `slower` INTEGER")
            }
        }

        /**
         * How long each turn was recorded for, on its line.
         *
         * The closing silence -- between the last sound and the end of the recording -- could
         * not be drawn without it, and nothing else holds it: the marks say where each sound
         * was, never where the tape stopped. Null on every existing row, which is right: those
         * turns were recorded before anything kept the figure, and their closing silence is
         * simply not known.
         */
        private val RECORDING_LENGTH = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `recorded` INTEGER")
            }
        }

        /**
         * What the model settled, on the turn that settled it -- and the free text of an
         * outcome, which held it before there was anywhere better, goes.
         *
         * The column was written for a design where the model answered once at the end. It
         * answers at moments an author wrote now, so the answers arrive while the sitting runs
         * and a sitting walked away from has them with no outcome at all: they are facts of
         * the sitting and not of how it went. Nothing ever wrote the column, so nothing is
         * lost by dropping it.
         *
         * The activities table is rebuilt rather than altered: dropping a column arrived in
         * SQLite 3.35 and `minSdk` 26 ships 3.18.
         */
        private val ESTABLISHED_ON_THE_TURN = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `utterances` ADD COLUMN `established` TEXT")
                db.execSQL(
                    "CREATE TABLE `activities_new` (`id` TEXT NOT NULL, " +
                        "`settings` TEXT NOT NULL, `brief` TEXT, `weights` TEXT, `cast` TEXT, " +
                        "`instructions` TEXT NOT NULL, `rules` TEXT NOT NULL, " +
                        "`journal` TEXT NOT NULL, `origin` TEXT, `engine` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "`startedAt` INTEGER, `endedAt` INTEGER, `prescriber` TEXT NOT NULL, " +
                        "`outcome_verdict` TEXT, `outcome_judge` TEXT, `outcome_at` INTEGER, " +
                        "`outcome_score` INTEGER, PRIMARY KEY(`id`))",
                )
                db.execSQL(
                    "INSERT INTO `activities_new` SELECT `id`, `settings`, `brief`, `weights`, " +
                        "`cast`, `instructions`, `rules`, `journal`, `origin`, `engine`, " +
                        "`status`, `createdAt`, `startedAt`, `endedAt`, `prescriber`, " +
                        "`outcome_verdict`, `outcome_judge`, `outcome_at`, " +
                        "`outcome_score` FROM `activities`",
                )
                db.execSQL("DROP TABLE `activities`")
                db.execSQL("ALTER TABLE `activities_new` RENAME TO `activities`")
            }
        }

        @Volatile private var instance: Archive? = null

        fun of(context: Context): Archive = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, Archive::class.java, "archive",
            ).addMigrations(DROP_FORMAT, JUDGED_MARKING, CAPTURE_FACTS, ACTIVITY_IN_SHAPE,
                    SPEAKER_IDENTITY, ATTEMPT_AND_ANSWER, KEYS_IN_ENGLISH, CAST_ON_THE_LINE,
                    NAMED_BY_ITS_DEFINITION, FIGURES_ON_THE_LINE, RECORDING_LENGTH,
                    ESTABLISHED_ON_THE_TURN)
                .build().also { instance = it }
        }
    }
}
