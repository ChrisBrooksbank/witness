package org.witness.app.data.local.evidence

import android.content.Context
import java.io.File
import org.witness.app.domain.verification.EncryptedPayload
import org.witness.app.domain.verification.EvidenceChunkCipher
import org.witness.app.platform.security.AndroidEvidenceKeyManager

private const val EVIDENCE_DIRECTORY_NAME = ".evidence"
private const val NO_MEDIA_FILE_NAME = ".nomedia"

class EncryptedEvidenceFileStore(
    context: Context,
    private val keyManager: AndroidEvidenceKeyManager = AndroidEvidenceKeyManager(),
    private val cipher: EvidenceChunkCipher = EvidenceChunkCipher(),
) {
    private val evidenceDirectory = File(context.filesDir, EVIDENCE_DIRECTORY_NAME)

    fun writeChunk(evidenceId: String, chunkIndex: Int, plaintext: ByteArray): File {
        ensureEvidenceDirectory()
        val encryptedPayload = cipher.encrypt(plaintext, keyManager.getOrCreateKey())
        val destination = File(evidenceDirectory, "$evidenceId-$chunkIndex.chunk")
        destination.writeBytes(encryptedPayload.encode())
        return destination
    }

    fun readChunk(file: File): ByteArray {
        val encryptedPayload = EncryptedPayload.decode(file.readBytes())
        return cipher.decrypt(encryptedPayload, keyManager.getOrCreateKey())
    }

    private fun ensureEvidenceDirectory() {
        if (!evidenceDirectory.exists()) {
            evidenceDirectory.mkdirs()
        }
        File(evidenceDirectory, NO_MEDIA_FILE_NAME).createNewFile()
    }
}
