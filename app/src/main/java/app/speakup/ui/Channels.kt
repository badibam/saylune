package app.speakup.ui

import androidx.annotation.StringRes
import app.speakup.R

/**
 * The eight marks the learner can turn off, one channel each.
 *
 * **This menu is on the learner's side, and it is never a lever** (`pixel-ui.md`). The invariant
 * demands it: a mark whose presence depended on the setting of the day would carry nothing, its
 * absence being indistinguishable from approval. It is the exact opposite of the scrambling of
 * the AI's turn, which *is* a lever -- there it is an aid on what the AI says, here it is the
 * output of a measure. An activity may take an aid away; none hides a mark.
 *
 * **Turning a channel off turns off its display only, never its measure**: the note does not
 * move, and opening the channel again shows what was there.
 *
 * The density measured it: most words carry something, so a learner working on his melody will
 * want the rest out of the way.
 */
enum class Channel(val key: String, @param:StringRes val says: Int) {
    /** The tint on the letters: the gap to the model, one notch of the ramp per sound. */
    Tint("tint", R.string.channel_tint),

    /** The rule under a syllable: where the stress landed, and where it belonged. */
    Stress("stress", R.string.channel_stress),

    /** The band above the line. */
    Melody("melody", R.string.channel_melody),

    /** The squiggle under a group: the correction. */
    Squiggle("squiggle", R.string.channel_squiggle),

    /** The brackets around a group: the relevance. */
    Brackets("brackets", R.string.channel_brackets),

    /** The column of points in the blank between two words. */
    Pauses("pauses", R.string.channel_pauses),

    /** The pastille at the end of the line that names the turn: the following. */
    Following("following", R.string.channel_following),

    /** The pace, in characters, beside it. */
    Pace("pace", R.string.channel_pace);

    companion object {
        /** What the store holds: the channels that are **off**, so nothing set means all on. */
        fun hidden(stored: String?): Set<Channel> =
            stored.orEmpty().split(SEPARATOR).mapNotNull { key ->
                entries.firstOrNull { it.key == key.trim() }
            }.toSet()

        fun store(hidden: Set<Channel>): String =
            entries.filter { it in hidden }.joinToString(SEPARATOR) { it.key }

        private const val SEPARATOR = ","
    }
}

/** Which marks are drawn right now. */
@JvmInline
value class Channels(private val hidden: Set<Channel>) {
    operator fun contains(channel: Channel): Boolean = channel !in hidden

    companion object {
        /** Every mark, which is what the app shows until the learner says otherwise. */
        val All = Channels(emptySet())
    }
}
