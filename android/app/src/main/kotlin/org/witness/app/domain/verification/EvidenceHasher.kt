package org.witness.app.domain.verification

import java.security.MessageDigest

private const val SHA_256 = "SHA-256"

class EvidenceHasher {
    fun hashChunk(chunk: ByteArray): String {
        return MessageDigest
            .getInstance(SHA_256)
            .digest(chunk)
            .toHexString()
    }

    fun merkleRoot(chunkHashes: List<String>): String {
        return when (chunkHashes.size) {
            0 -> ""
            1 -> chunkHashes.first()
            else -> {
                val nextLevel = chunkHashes.chunked(size = 2).map { pair ->
                    if (pair.size == 2) hashPair(pair.first(), pair.last()) else pair.first()
                }
                merkleRoot(nextLevel)
            }
        }
    }

    private fun hashPair(firstHash: String, secondHash: String): String {
        val digest = MessageDigest.getInstance(SHA_256)
        digest.update(firstHash.hexToByteArray())
        digest.update(secondHash.hexToByteArray())
        return digest.digest().toHexString()
    }
}

private fun ByteArray.toHexString(): String {
    return joinToString(separator = "") { byte -> "%02x".format(byte) }
}

private fun String.hexToByteArray(): ByteArray {
    require(length % 2 == 0) { "Hex string must contain an even number of characters." }

    return chunked(size = 2)
        .map { byte -> byte.toInt(radix = 16).toByte() }
        .toByteArray()
}
