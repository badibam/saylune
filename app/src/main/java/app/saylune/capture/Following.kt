package app.saylune.capture

import java.io.File

/**
 * A take being taken in by someone else while it is still being said.
 *
 * The recorder does not know who, or why: it hands over the raw file as it opens a take, and
 * says when the take became a wav or was thrown away. What reads it -- a remote pass, today --
 * tails the file on its own clock (`docs/design/remote-analysis.md`).
 */
interface Following {

    /** The take was closed into [wav]: whatever is left goes now. */
    fun closed(wav: File)

    /** The take was thrown away, or ended holding nothing. */
    fun dropped()
}
