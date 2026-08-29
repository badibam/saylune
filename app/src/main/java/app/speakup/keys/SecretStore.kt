package app.speakup.keys

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    /** Everything entered so far. Absent and blank are the same thing to a caller. */
    fun values(): Flow<Map<Secret, String>> = context.secrets.data.map { prefs ->
        Secret.entries.mapNotNull { secret ->
            prefs[stringPreferencesKey(secret.id)]?.let { secret to decrypt(it) }
        }.toMap()
    }

    suspend fun write(secret: Secret, value: String) {
        val key = stringPreferencesKey(secret.id)
        context.secrets.edit { prefs ->
            if (value.isBlank()) prefs.remove(key) else prefs[key] = encrypt(value.trim())
        }
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
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
            key(),
            GCMParameterSpec(TAG_BITS, raw, 0, IV_BYTES),
        )
        return String(cipher.doFinal(raw, IV_BYTES, raw.size - IV_BYTES), Charsets.UTF_8)
    }

    /** The Keystore key, made on first use. Not exportable, and not backed up with the app. */
    private fun key(): javax.crypto.SecretKey {
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
