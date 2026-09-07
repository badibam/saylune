package app.saylune.capture

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * One recording per sound of the alphabet: what the symbol means, said on its own.
 *
 * **A legend, never a model.** The readout names sounds in IPA and most readers cannot read
 * IPA, so a symbol printed alone is a name for something they have never heard. This answers
 * *what `ʃ` is*; it never answers *how this word should have been said*, which is the
 * synthesised model at that place in the phrase and lives in a different gesture entirely.
 * The distinction matters because the doc rests on it: nothing outside the two recordings
 * ever judges, and this judges nothing -- it is the same class of outside file as the
 * affinity table, which says where to paint and not what is correct.
 *
 * Where it is the only possible answer is the spread: those rows name the runners-up of a
 * distribution, sounds nobody produced here, so there is no stretch of either recording to
 * point at.
 *
 * **A symbol with no recording is simply not offered.** Three of the thirty-eight have no
 * free recording anywhere -- `ɝ`, `eɪ`, `oʊ` -- and substituting the nearest cardinal vowel
 * would make the app assert one symbol while playing another, which is the one failure this
 * feature cannot afford. Missing is honest; approximate is not.
 *
 * Files are named by the symbol itself and carry their own licences, recorded beside them
 * (`bench/out/reference/provenance.json`, and the credits screen the attribution obliges).
 */
object Reference {

    private const val DIR = "reference"

    /**
     * The recording of [symbol], or null when none ships.
     *
     * Copied out of the assets on first use, because playback wants a path: an asset is a
     * stream inside the apk and neither `MediaPlayer` nor a slice reader takes one.
     */
    fun of(context: Context, symbol: String): File? {
        val name = "$symbol.ogg"
        val cached = File(File(context.cacheDir, DIR).apply { mkdirs() }, name)
        if (cached.isFile && cached.length() > 0) return cached
        return runCatching {
            context.assets.open("$DIR/$name").use { source ->
                cached.outputStream().use { source.copyTo(it) }
            }
            cached
        }.getOrNull()
    }

    /** Whether a symbol has a recording at all -- what decides if it is offered as a target. */
    fun has(context: Context, symbol: String): Boolean = of(context, symbol) != null

    /**
     * Who made each recording, under what licence, and where the original is.
     *
     * **Not optional, and not decoration.** Thirty-one of these are CC BY-SA, which grants
     * every freedom on one condition: that the people who recorded them are named. A credits
     * screen is how that condition is met, and an app that shipped the sounds without it
     * would be taking the work while declining the only thing asked in return.
     *
     * Written beside the sounds by `bench/reference.py --ship`, from the same pass that
     * copies them, so the list cannot fall behind the set it describes. Read here rather
     * than compiled in for the same reason.
     */
    fun credits(context: Context): List<Credit> = runCatching {
        val json = JSONObject(
            context.assets.open("$DIR/credits.json").bufferedReader().use { it.readText() }
        )
        json.keys().asSequence().map { symbol ->
            val row = json.getJSONObject(symbol)
            Credit(
                symbol = symbol,
                title = row.getString("title").removeSuffix(".ogg"),
                author = row.getString("author"),
                licence = row.getString("licence"),
                page = row.getString("page"),
            )
        }.sortedBy { it.symbol }.toList()
    }.getOrDefault(emptyList())
}

/** One recording and what its licence obliges to be said about it. */
data class Credit(
    val symbol: String,
    val title: String,
    val author: String,
    val licence: String,
    val page: String,
)
