package app.saylune.analysis

import android.content.Context
import android.net.Uri
import app.saylune.debug.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * The acoustic model, which the app does not carry and has to be given once.
 *
 * **Why it is not in the APK**: 358 MB against an app of 33, for a brick whose weights are
 * one free file among several candidates. The doc asks for an explicit opt-in at first use
 * -- never at first launch, never in silence (`docs/reference.md`) -- and that is what this
 * serves. Until it is here the pronunciation marks are off end to end, carrying their
 * reason, which is the behaviour "La panne" already describes.
 *
 * **Three pieces, and they only mean anything together.** The network is what hears sounds,
 * the vocabulary says what each of its columns stands for, and the probe is the linear read
 * that turns a hidden layer into word stress. A vocabulary from another model would not
 * fail: it would name every column wrong and measure something else in silence. So they are
 * fetched as one, verified as one, and a set that is not complete is not a set.
 *
 * **What is verified is the content, never the name.** Each piece carries the SHA-256 of the
 * bytes the bench measured against, so a truncated download, a mirror that serves something
 * else, or a file somebody brought by hand are all the same question with the same answer.
 * The wisdom's rule that a file's identity is its content, and here it is also the only
 * thing standing between the analysis and a model it was never calibrated on.
 *
 * **Where they land**: the app's own files directory. Not `/data/local/tmp`, which was the
 * arrangement while the shell had to push them -- SELinux lets an app read there and never
 * write, and an app cannot list a subdirectory of its own that the shell created. Once the
 * app fetches them itself, its own directory has none of those questions, and it is also
 * what gets removed when the app is uninstalled.
 */
object Weights {

    /**
     * Where the files are served from: a release of this project's own repository.
     *
     * **Beside the code that makes them, rather than at a model host.** These are not
     * somebody else's weights fetched from where they were published -- the network is an
     * export `bench/export.py` writes from a chosen candidate, so it is this project's
     * artefact and belongs with this project's tags.
     *
     * **A tag of its own, not the app's.** The model does not move at the app's pace: it
     * changes when the acoustic model is settled, which is a question of its own
     * (`../../../../../../../TODO.md`), so an app release would otherwise drag 358 MB behind
     * every version that did not touch it.
     *
     * The tag is stable rather than immutable -- a tag can be moved, an asset replaced. That
     * is not what makes this safe: **the digests are**. What arrives is checked against the
     * bytes the bench measured before it is put in place, so a swapped file is refused
     * rather than installed, which is the same answer a truncated download gets.
     */
    private const val REPOSITORY = "badibam/saylune"
    private const val TAG = "weights-timit-ipa-1"

    /**
     * One file the set needs, and what its bytes must be.
     *
     * [bytes] is not a second check on top of [sha256] -- it is what lets the offer say what
     * it will cost and a download say how far along it is, before a single byte has arrived.
     */
    data class Piece(val name: String, val bytes: Long, val sha256: String)

    /**
     * The set, measured on the very files the bench reads (`bench/out/onnx/`).
     *
     * The network is the 8-bit export: `bench/export.py` writes it beside the float one, and
     * it is the one a phone runs.
     */
    /**
     * The network, named rather than looked for.
     *
     * [EmbeddedAnalysis] used to take whatever `.onnx` it found in the directory, which is
     * one idea of the file, while this is another: seen on the phone, a network pushed there
     * by hand under a different name was loaded by the analysis while the screen said nothing
     * was there. Two seams disagreeing in silence about the same file, which is the shape of
     * defect this project has met before. One name, read from here by both.
     */
    val NETWORK = Piece(
        "timit-ipa-int8.onnx", 358_518_235,
        "ec2208f4fd224b04502ece9259fbcfc211657e721910cfba212162592c5128da",
    )

    val PIECES = listOf(
        NETWORK,
        Piece(
            "vocab.json", 486,
            "12b7de6be6a1bd3132ffdef8d8fe191c8e98f2d70639666e35d9cf6c9d2af77a",
        ),
        Piece(
            "probe.json", 62_613,
            "d904a7229f35fb0983a624f56df8a1adec25f4e16d7369f5c90b3b0ef0ba2707",
        ),
    )

    /** What the whole set weighs, which is what the offer has to say before it is taken. */
    val total: Long = PIECES.sumOf { it.bytes }

    fun home(context: Context): File = File(context.filesDir, "analysis").apply { mkdirs() }

    private fun target(context: Context, piece: Piece) = File(home(context), piece.name)

    /** Where a piece is put down while it arrives, so a half file is never a whole one. */
    private fun partial(context: Context, piece: Piece) =
        File(home(context), "${piece.name}.part")

    /**
     * What the app has, told from the files themselves rather than from a note kept beside.
     *
     * A flag saying "downloaded" would be a second source that drifts from the fact: the
     * fact is the bytes on disk, and reading them is what the wisdom asks for -- store the
     * fact, derive the status.
     *
     * The size is read first and the digest only when it matches, because hashing 358 MB
     * costs a second or two and a truncated file is told apart by its length alone.
     */
    sealed interface State {

        /** Nothing here, or not enough of it to be a set. */
        data object Absent : State

        /** Every piece present and every digest right. */
        data object Ready : State

        /**
         * The files are there and one of them is not what it should be.
         *
         * Kept apart from [Absent] because it is not the same event and does not deserve the
         * same offer: something was fetched and is wrong, which is worth saying before
         * spending 358 MB again.
         */
        data class Wrong(val piece: String) : State
    }

    suspend fun state(context: Context): State = withContext(Dispatchers.IO) {
        val present = PIECES.filter { target(context, it).isFile }
        if (present.size < PIECES.size) return@withContext State.Absent
        PIECES.forEach { piece ->
            val file = target(context, piece)
            if (file.length() != piece.bytes || digest(file) != piece.sha256) {
                return@withContext State.Wrong(piece.name)
            }
        }
        State.Ready
    }

    /**
     * How far a download has already got, in bytes, so the offer can say "resume" honestly.
     *
     * Only ever the part files: a piece already whole and verified is not counted, because
     * what this answers is how much is still owed.
     */
    suspend fun begun(context: Context): Long = withContext(Dispatchers.IO) {
        PIECES.sumOf { piece ->
            val whole = target(context, piece)
            if (whole.isFile) whole.length() else partial(context, piece).length()
        }
    }

    /**
     * How far the download has got, or null when none is running.
     *
     * **The download belongs to the app, not to the screen that starts it.** This app has
     * already paid for the other arrangement once: a turn launched from the conversation
     * screen's own scope died the moment the screen left the composition. A screen one
     * navigates away from would take 359 MB with it, and there is no reason for it to --
     * nobody has to sit and watch. So the work runs on a scope this object owns, the screen
     * reads the flow, and coming back to the screen finds it where it got to.
     */
    private val _progress = MutableStateFlow<Long?>(null)
    val progress: StateFlow<Long?> = _progress.asStateFlow()

    private val _failure = MutableStateFlow<String?>(null)
    val failure: StateFlow<String?> = _failure.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var running: Job? = null

    /**
     * Begin, or carry on. Asked twice, the second ask does nothing rather than fetching the
     * same bytes twice into the same file.
     */
    fun start(context: Context) {
        if (running?.isActive == true) return
        _failure.value = null
        _progress.value = 0L
        val app = context.applicationContext
        running = scope.launch {
            val outcome = fetch(app) { done, _ -> _progress.value = done }
            _progress.value = null
            outcome.exceptionOrNull()?.let {
                _failure.value = it.message ?: it::class.simpleName
                Trace.fail("weights: download stopped", "why" to it.message)
            }
        }
    }

    /**
     * Fetch what is missing, reporting how far along the whole set is.
     *
     * **It resumes.** 358 MB over a phone's connection gets interrupted, and starting over
     * every time is how a feature becomes one nobody uses. A part file is kept and the next
     * attempt asks the server to carry on from its length; a server that will not is answered
     * by beginning again, which is correct and merely slower.
     *
     * The digest is checked **before** the part file becomes the real one, so what is in
     * place is either right or not there. Half a network is not a network.
     */
    suspend fun fetch(
        context: Context,
        onProgress: (done: Long, total: Long) -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            var done = PIECES.sumOf { if (target(context, it).isFile) it.bytes else 0L }
            PIECES.forEach { piece ->
                if (target(context, piece).isFile) {
                    // A piece can become whole by another road than this one -- brought by
                    // hand, or put there by the bench -- and its half-arrived copy would
                    // then sit there for good, owed to nobody. Measured on the phone: 45 MB
                    // of an abandoned download survived the file arriving over the cable.
                    partial(context, piece).delete()
                    return@forEach
                }
                val part = partial(context, piece)
                done -= part.length()
                pull(piece, part) { got -> onProgress(done + got, total) }
                done += piece.bytes

                val seen = digest(part)
                if (seen != piece.sha256) {
                    // Deleted rather than kept: a resume would carry on from bytes already
                    // known to be wrong, and the next attempt would fail the same way for
                    // ever. The user is told which piece, so a mirror that serves the wrong
                    // file is distinguishable from a connection that mangles one.
                    part.delete()
                    throw IOException("${piece.name} is not what it should be")
                }
                if (!part.renameTo(target(context, piece))) {
                    throw IOException("could not put ${piece.name} in place")
                }
                onProgress(done, total)
            }
            Trace.add("weights: fetched", "bytes" to total.toString())
        }
    }

    /**
     * One piece, from wherever it already got to.
     *
     * `Http` is not used here and that is deliberate: it reads a whole body into memory,
     * which is the right shape for a provider's answer and the wrong one for 358 MB.
     */
    private fun pull(piece: Piece, into: File, onProgress: (Long) -> Unit) {
        val from = into.length()
        val url = "https://github.com/$REPOSITORY/releases/download/$TAG/${piece.name}"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 60_000
            // A release asset is served from a signed URL on another host, so this is not
            // optional. The original URL is asked for afresh each time rather than the
            // redirect being kept: those signatures expire, and a resume may come hours later.
            instanceFollowRedirects = true
            if (from > 0) setRequestProperty("Range", "bytes=$from-")
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("$status fetching ${piece.name}")
            // 206 means the server took the Range and is continuing; 200 means it ignored
            // it and is sending the whole file, so what is already there is thrown away
            // rather than appended to.
            val resuming = status == HttpURLConnection.HTTP_PARTIAL && from > 0
            var written = if (resuming) from else 0L
            connection.inputStream.use { source ->
                FileOutputStream(into, resuming).use { sink ->
                    val buffer = ByteArray(1 shl 16)
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        sink.write(buffer, 0, read)
                        written += read
                        onProgress(written)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Take a file the user brought instead of fetching it.
     *
     * The published precedent offers this beside the download, and it earns its place twice
     * over here: whoever already has the file -- it is what the bench pushes -- spends
     * nothing, and whoever pays for their connection has a way in that does not cost 358 MB.
     *
     * **Which piece it is, is decided by its content and never by its name.** A file picked
     * from anywhere arrives with whatever name it was given; the digest says what it
     * actually is, and a file matching none of the three is refused rather than put
     * somewhere and found broken later.
     */
    suspend fun adopt(context: Context, uri: Uri): Result<Piece> = withContext(Dispatchers.IO) {
        runCatching {
            val scratch = File(home(context), "brought.part")
            try {
                context.contentResolver.openInputStream(uri)?.use { source ->
                    scratch.outputStream().use { source.copyTo(it) }
                } ?: throw IOException("nothing could be read from that file")

                val seen = digest(scratch)
                val piece = PIECES.firstOrNull { it.sha256 == seen }
                    ?: throw IOException("this is not one of the three files")
                if (!scratch.renameTo(target(context, piece))) {
                    throw IOException("could not put ${piece.name} in place")
                }
                piece
            } finally {
                scratch.delete()
            }
        }
    }

    /** Everything this put on the phone, gone. The part files too, or it is not gone. */
    suspend fun erase(context: Context) = withContext(Dispatchers.IO) {
        home(context).listFiles()?.forEach { it.delete() }
        Trace.add("weights: erased")
    }

    private fun digest(file: File): String =
        file.inputStream().use { digest(it) }

    private fun digest(stream: InputStream): String {
        val sha = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(1 shl 16)
        while (true) {
            val read = stream.read(buffer)
            if (read < 0) break
            sha.update(buffer, 0, read)
        }
        return sha.digest().joinToString("") { "%02x".format(it) }
    }
}
