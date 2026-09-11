package app.saylune.activity

import android.content.Context
import app.saylune.BuildConfig
import app.saylune.scene.SceneFile
import app.saylune.scene.SceneFiles

/**
 * Where scenes are read from, which today is the files shipped with the app.
 *
 * **Importing the files into the store is ruled out**: the table would be the copy of a source
 * already on the disk, readable and versioned, so the second source that drifts which the
 * project refuses everywhere. A sitting keeps its own copy of the file it was opened from,
 * which is another thing: a template applied at creation, which nothing rewrites afterwards.
 */
object Definitions {

    /** The free conversation, which is a shipped scene like any other. */
    const val FREE_CONVERSATION = "free-conversation"

    private const val FOLDER = "definitions"
    private const val SUFFIX = ".json"

    /** Every scene the app ships, in no particular order. */
    fun all(context: Context, version: String = BuildConfig.VERSION_NAME): List<SceneFile> =
        context.assets.list(FOLDER).orEmpty()
            .filter { it.endsWith(SUFFIX) }
            .map { of(context, it.removeSuffix(SUFFIX), version) }

    /**
     * The scene called [id]. **An id nobody ships fails outright**, and so does a file that does
     * not hold together: it is checked at loading, and a problem is never left to a learner.
     */
    fun of(context: Context, id: String, version: String = BuildConfig.VERSION_NAME): SceneFile =
        SceneFiles.parse(
            id, version,
            context.assets.open("$FOLDER/$id$SUFFIX").use { it.readBytes().decodeToString() },
        )
}
