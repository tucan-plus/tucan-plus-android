package de.selfmade4u.tucanplus

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// https://developer.android.com/privacy-and-security/risks/sensitive-data-external-storage
// https://developer.android.com/privacy-and-security/keystore
// https://developer.android.com/privacy-and-security/cryptography#security-crypto-jetpack-deprecated
// https://developer.android.com/reference/javax/crypto/KeyGenerator

object CipherManager  {

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val TRANSFORMATION = "$AES_ALGORITHM/$BLOCK_MODE/$PADDING"
    private const val KEY_ALIAS = "tucan-credentials2"

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply {
        load(null) // With load function we initialize our keystore
    }

    @Throws(Exception::class)
    private fun createKey(): SecretKey {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .build()

        return KeyGenerator.getInstance(AES_ALGORITHM, ANDROID_KEY_STORE).apply {
            init(keyGenParameterSpec)
        }.generateKey()
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    // TODO FIXME add tests that check that this roundtrips
    fun encrypt(inputText: String): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encryptedBytes = cipher.doFinal(inputText.toByteArray())
        return Pair(Base64.encodeToString(cipher.iv, Base64.DEFAULT), Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
    }

    fun decrypt(data: Pair<String, String>): String {
        val iv = Base64.decode(data.first, Base64.DEFAULT)
        val encrypted = Base64.decode(data.second, Base64.DEFAULT)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun deleteAlias() {
        keyStore.deleteEntry(KEY_ALIAS)
    }
}