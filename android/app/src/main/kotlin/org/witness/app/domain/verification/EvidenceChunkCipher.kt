package org.witness.app.domain.verification

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_BITS = 128
private const val GCM_IV_BYTES = 12

class EvidenceChunkCipher(
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    fun encrypt(plaintext: ByteArray, secretKey: SecretKey): EncryptedPayload {
        val iv = ByteArray(GCM_IV_BYTES)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return EncryptedPayload(
            iv = iv,
            ciphertext = cipher.doFinal(plaintext),
        )
    }

    fun decrypt(payload: EncryptedPayload, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, payload.iv))
        return cipher.doFinal(payload.ciphertext)
    }
}

data class EncryptedPayload(
    val iv: ByteArray,
    val ciphertext: ByteArray,
) {
    fun encode(): ByteArray = iv + ciphertext

    companion object {
        fun decode(bytes: ByteArray): EncryptedPayload {
            require(bytes.size > GCM_IV_BYTES) { "encrypted payload is too short" }
            return EncryptedPayload(
                iv = bytes.copyOfRange(0, GCM_IV_BYTES),
                ciphertext = bytes.copyOfRange(GCM_IV_BYTES, bytes.size),
            )
        }
    }
}
