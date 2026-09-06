package app.speakup.activity

import java.io.File

/**
 * The definitions the app ships, read off the folder it packs them from.
 *
 * A sitting is opened from a definition and there is no other way to make one, so a test that
 * needs a sitting needs a definition. Reading the real file rather than building one by hand
 * is what makes these tests break when the shipped file stops holding together -- which is the
 * only reason to reach for the file at all.
 */
internal object Shipped {

    /** A version that is not a release's, these being read off the disk and not off a build. */
    private const val VERSION = "test"

    fun definition(id: String) = Definitions.parse(
        id, VERSION, File("src/main/assets/definitions/$id.json").readText(),
    )

    /** A fresh free conversation, as the pipeline opens one. */
    fun freeConversation() = Activity.from(definition(Definitions.FREE_CONVERSATION))
}
