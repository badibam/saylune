package app.saylune.activity

import app.saylune.scene.SceneFiles
import java.io.File

/**
 * The scenes the app ships, read off the folder it packs them from.
 *
 * A sitting is opened from a scene and there is no other way to make one, so a test that needs
 * a sitting needs a scene. Reading the real file rather than building one by hand is what makes
 * these tests break when the shipped file stops holding together.
 */
internal object Shipped {

    /** A version that is not a release's, these being read off the disk and not off a build. */
    private const val VERSION = "test"

    fun definition(id: String) = SceneFiles.parse(
        id, VERSION, File("src/main/assets/definitions/$id.json").readText(),
    )

    /** The free conversation's own file, which every sitting here comes from. */
    fun free() = definition(Definitions.FREE_CONVERSATION)

    /** A fresh free conversation, as the pipeline opens one. */
    fun freeConversation() = Activity.from(free())
}
