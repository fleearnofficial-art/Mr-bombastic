package com.example.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object MessageCrypt {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val SALT = "FleearnSecureE2EMessagingSalt2026"

    /**
     * Derives a unique symmetric key for two users based on their UIDs.
     * The UIDs are sorted alphabetically to guarantee both users derive the same key.
     */
    private fun deriveKey(uid1: String, uid2: String): SecretKeySpec {
        val sortedUids = listOf(uid1, uid2).sorted().joinToString(":")
        val rawInput = "$sortedUids:$SALT"
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(rawInput.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts a plaintext message for a given sender-receiver pair.
     */
    fun encrypt(plaintext: String, senderUid: String, receiverUid: String): String {
        if (plaintext.isBlank()) return plaintext
        return try {
            val key = deriveKey(senderUid, receiverUid)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivBytes = ByteArray(16)
            val keyBytes = key.encoded
            System.arraycopy(keyBytes, 0, ivBytes, 0, 16)
            val ivSpec = IvParameterSpec(ivBytes)
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plaintext // Fallback if encryption fails
        }
    }

    /**
     * Decrypts a ciphertext message for a given sender-receiver pair.
     */
    fun decrypt(ciphertext: String, senderUid: String, receiverUid: String): String {
        if (ciphertext.isBlank()) return ciphertext
        return try {
            val key = deriveKey(senderUid, receiverUid)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivBytes = ByteArray(16)
            val keyBytes = key.encoded
            System.arraycopy(keyBytes, 0, ivBytes, 0, 16)
            val ivSpec = IvParameterSpec(ivBytes)
            
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val encryptedBytes = Base64.decode(ciphertext, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // If decryption fails, maybe it is a legacy plaintext message
            ciphertext
        }
    }
}
