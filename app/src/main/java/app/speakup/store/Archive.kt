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
 * count is enough to produce (`docs/design/activity-model.md`). So there is no column here
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
    val matter: String,
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

/** How it went. Null throughout while nothing has judged it. */
data class OutcomeRow(
    val verdict: String?,
    val judge: String?,
    val at: Long?,
    val says: String?,
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
    /** Which clock closed the turn, or null when a hand sent it. */
    val ending: String?,
    /** The marks, as the screen shows them. Null when nothing read this. */
    val marking: String?,
    val sounds: String?,
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

    /** The most recent one, to reopen at launch. Null the very first time. */
    @Query("SELECT * FROM activities ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(): ActivityRow?

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun activity(id: String): ActivityRow?

    /** Every recording any utterance names, across every conversation. */
    @Query("SELECT said FROM utterances WHERE said IS NOT NULL")
    suspend fun recordings(): List<String>

    @Query("SELECT * FROM utterances WHERE activity = :activity ORDER BY rank ASC")
    suspend fun utterances(activity: String): List<UtteranceRow>
}

@Database(entities = [ActivityRow::class, UtteranceRow::class], version = 7)
abstract class Archive : RoomDatabase() {

    abstract fun dao(): ArchiveDao

    companion object {

        /**
         * Drops `format`, which named a kind of activity there was only ever one of.
         *
         * An activity is a conversation and nothing else: what looked like other formats --
         * reading a text aloud, repeating after a model -- are moments inside a conversation
         * rather than shapes beside it, so a column every row filled the same way described an
         * intention and not the model (`docs/design/activity-model.md`).
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

        @Volatile private var instance: Archive? = null

        fun of(context: Context): Archive = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, Archive::class.java, "archive",
            ).addMigrations(DROP_FORMAT, JUDGED_MARKING, CAPTURE_FACTS, ACTIVITY_IN_SHAPE,
                    SPEAKER_IDENTITY, ATTEMPT_AND_ANSWER)
                .build().also { instance = it }
        }
    }
}
