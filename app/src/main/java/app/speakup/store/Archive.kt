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
    val format: String,
    val matter: String,
    /** The levels, written out. Null while nothing sets them. */
    val settings: String?,
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
    val speaker: String,
    val text: String,
    /** Where the recording is. Null for the answers, which are not recorded. */
    val said: String?,
    /** The marks, as the screen shows them. Null when nothing read this. */
    val marking: String?,
    val sounds: String?,
    val model: String?,
    val faulty: Boolean,
    val take: String?,
    /** The identity of the utterance this says again. */
    val repeats: String?,
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
    @Query("SELECT * FROM activities WHERE format = 'Conversation' ORDER BY createdAt DESC")
    fun conversations(): Flow<List<ActivityRow>>

    /** The most recent one, to reopen at launch. Null the very first time. */
    @Query("SELECT * FROM activities WHERE format = 'Conversation' ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(): ActivityRow?

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun activity(id: String): ActivityRow?

    /** Every recording any utterance names, across every conversation. */
    @Query("SELECT said FROM utterances WHERE said IS NOT NULL")
    suspend fun recordings(): List<String>

    @Query("SELECT * FROM utterances WHERE activity = :activity ORDER BY rank ASC")
    suspend fun utterances(activity: String): List<UtteranceRow>
}

@Database(entities = [ActivityRow::class, UtteranceRow::class], version = 1)
abstract class Archive : RoomDatabase() {

    abstract fun dao(): ArchiveDao

    companion object {

        @Volatile private var instance: Archive? = null

        fun of(context: Context): Archive = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, Archive::class.java, "archive",
            ).build().also { instance = it }
        }
    }
}
