package org.witness.app.domain.verification

import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EvidenceHasherTest {
    private val hasher = EvidenceHasher()

    @Test
    fun hashChunkReturnsStableSha256() {
        val hash = hasher.hashChunk("witness".encodeToByteArray())

        assertEquals(
            "ba1c566a4bad288c22a0b7511458c92ca5822cd41632e51806e9ea75ed12d13d",
            hash,
        )
    }

    @Test
    fun merkleRootReturnsEmptyStringForNoChunks() {
        assertEquals("", hasher.merkleRoot(emptyList()))
    }

    @Test
    fun merkleRootReturnsOnlyHashForSingleChunk() {
        val chunkHash = hasher.hashChunk("one".encodeToByteArray())

        assertEquals(chunkHash, hasher.merkleRoot(listOf(chunkHash)))
    }

    @Test
    fun merkleRootHashesEvenChunkPairs() {
        val chunkHashes = listOf("one", "two", "three", "four")
            .map { value -> hasher.hashChunk(value.encodeToByteArray()) }

        assertEquals(expectedMerkleRoot(chunkHashes), hasher.merkleRoot(chunkHashes))
    }

    @Test
    fun merkleRootCarriesOddChunkToNextLevel() {
        val chunkHashes = listOf("one", "two", "three")
            .map { value -> hasher.hashChunk(value.encodeToByteArray()) }

        assertEquals(expectedMerkleRoot(chunkHashes), hasher.merkleRoot(chunkHashes))
        assertNotEquals(hasher.merkleRoot(chunkHashes.dropLast(n = 1)), hasher.merkleRoot(chunkHashes))
    }

    private fun expectedMerkleRoot(chunkHashes: List<String>): String {
        return when (chunkHashes.size) {
            0 -> ""
            1 -> chunkHashes.first()
            else -> expectedMerkleRoot(
                chunkHashes.chunked(size = 2).map { pair ->
                    if (pair.size == 2) expectedHashPair(pair.first(), pair.last()) else pair.first()
                },
            )
        }
    }

    private fun expectedHashPair(firstHash: String, secondHash: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(firstHash.chunked(size = 2).map { byte -> byte.toInt(radix = 16).toByte() }.toByteArray())
        digest.update(secondHash.chunked(size = 2).map { byte -> byte.toInt(radix = 16).toByte() }.toByteArray())
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
