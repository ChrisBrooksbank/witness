package org.witness.app.domain.verification

import javax.crypto.KeyGenerator
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EvidenceChunkCipherTest {
    private val cipher = EvidenceChunkCipher()
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test
    fun encryptedPayloadRoundTrips() {
        val plaintext = "sensitive evidence bytes".encodeToByteArray()

        val encrypted = cipher.encrypt(plaintext, key)
        val decrypted = cipher.decrypt(encrypted, key)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun encodedPayloadDoesNotContainPlaintext() {
        val plaintext = "sensitive evidence bytes".encodeToByteArray()

        val encrypted = cipher.encrypt(plaintext, key).encode().decodeToString()

        assertNotEquals("sensitive evidence bytes", encrypted)
    }
}
