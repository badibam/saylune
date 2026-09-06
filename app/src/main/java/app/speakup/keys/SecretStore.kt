package app.speakup.keys

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.speakup.debug.Trace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

private val Context.secrets by preferencesDataStore("secrets")

/**
 * What the user typed, encrypted at rest by a key the app cannot export.
 *
 * The app is a client with nothing of its own: the user brings their own keys, which go
 * only to the providers they belong to. That is not only an economy -- it is the one shape
 * compatible with publishing on F-Droid, where a key inside a release is a key published
 * with the sources.
 *
 * So: the cipher key lives in the Android Keystore and never leaves it, the ciphertext
 * lives in a DataStore, `allowBackup` is false so neither travels in a system backup, and
 * no value is ever logged. Getting this wrong is not the kind of mistake that is fixed
 * later -- by then a key has been through a log file.
 */
class SecretStore(private val context: Context) {

    /**
     * Everything entered so far. Absent and blank are the same thing to a caller.
     *
     * Every entry is decrypted on every read, and a turn reads this seven times -- twice for
     * recognition, twice for the language model, three times for the synthesis, each of the
     * three links once for the switching class and once for the provider it resolves to. What
     * that costs is traced rather than assumed: the gaps it sits in were measured at 262, 365
     * and 726 ms on the turn of 2026-09-06 13:17, and nothing said how much of them was this.
     */
    fun values(): Flow<Map<Secret, String>> = context.secrets.data.map { prefs ->
        val began = System.nanoTime()
        val out = Secret.entries.mapNotNull { secret ->
            prefs[stringPreferencesKey(secret.id)]?.let { secret to decrypt(it) }
        }.toMap()
        Trace.add(
            "secrets: read",
            "entries" to out.size.toString(),
            "ms" to ((System.nanoTime() - began) / 1_000_000).toString(),
        )
        out
    }

    suspend fun write(secret: Secret, value: String) {
        val key = stringPreferencesKey(secret.id)
        context.secrets.edit { prefs ->
            if (value.isBlank()) prefs.remove(key) else prefs[key] = encrypt(value.trim())
        }
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, cipherKey)
        val body = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + body, Base64.NO_WRAP)
    }

    /**
     * Throws rather than returning null when a stored entry will not decrypt.
     *
     * An entry that is absent means the user has not filled it in; an entry that is present
     * and unreadable means the Keystore key is gone under it. Reporting the second as the
     * first would put an empty field back in front of the user with no word of explanation,
     * and they would retype a key that was never wrong.
     */
    private fun decrypt(stored: String): String {
        val raw = Base64.decode(stored, Base64.NO_WRAP)
        require(raw.size > IV_BYTES) { "stored secret is too short to hold an IV" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            cipherKey,
            GCMParameterSpec(TAG_BITS, raw, 0, IV_BYTES),
        )
        return String(cipher.doFinal(raw, IV_BYTES, raw.size - IV_BYTES), Charsets.UTF_8)
    }

    /**
     * The Keystore key, made on first use and **held for the life of the process**.
     *
     * It was fetched afresh for every entry of every read, and a fetch is a round trip to the
     * Keystore -- hardware-backed on this phone. Around fourteen entries, seven reads a turn,
     * so something near a hundred round trips before the learner hears anything. The key
     * itself does not change while the app runs, so there was nothing to gain by asking again.
     *
     * Holding it does not hide a key that goes away underneath: a cipher built on it fails at
     * `doFinal`, and [decrypt] already says that an entry that will not decrypt is a lost key
     * and not an empty field.
     */
    private val cipherKey: javax.crypto.SecretKey by lazy { loadKey() }

    private fun loadKey(): javax.crypto.SecretKey {
        val store = KeyStore.getInstance(PROVIDER).apply { load(null) }
        (store.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val ALIAS = "speakup.secrets"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
