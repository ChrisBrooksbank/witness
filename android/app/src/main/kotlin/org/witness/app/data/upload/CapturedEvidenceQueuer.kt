package org.witness.app.data.upload

import android.content.Context
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.witness.app.data.local.evidence.EncryptedEvidenceFileStore
import org.witness.app.data.local.evidence.EvidenceCacheDatabase
import org.witness.app.data.local.evidence.EvidenceChunkEntity
import org.witness.app.data.local.evidence.EvidenceEntity
import org.witness.app.domain.model.CaptureMode
import org.witness.app.domain.model.EvidenceChunkHash
import org.witness.app.domain.model.MediaType
import org.witness.app.domain.verification.EvidenceHasher
import org.witness.app.platform.metadata.AndroidCaptureMetadataCollector
import org.witness.app.platform.metadata.InitialMetadataRequest

private const val MAX_PLAINTEXT_CHUNK_BYTES = 4 * 1024 * 1024

class CapturedEvidenceQueuer(
    private val context: Context,
    private val database: EvidenceCacheDatabase = EvidenceCacheDatabase.create(context),
    private val fileStore: EncryptedEvidenceFileStore = EncryptedEvidenceFileStore(context),
    private val hasher: EvidenceHasher = EvidenceHasher(),
    private val metadataCollector: AndroidCaptureMetadataCollector = AndroidCaptureMetadataCollector(context),
) {
    suspend fun queue(request: CapturedEvidenceQueueRequest) = withContext(Dispatchers.IO) {
        val encryptedChunks = encryptChunks(request)
        require(encryptedChunks.isNotEmpty()) { "captured media file was empty" }
        val chunkHashes = encryptedChunks.map { chunk -> chunk.hash }
        val merkleRoot = hasher.merkleRoot(chunkHashes.map { chunkHash -> chunkHash.sha256 })
        val metadata = metadataCollector.collectInitialMetadata(
            InitialMetadataRequest(
                evidenceId = request.evidenceId,
                merkleRoot = merkleRoot,
                chunkHashes = chunkHashes,
                mediaType = request.mediaType,
                captureMode = request.captureMode,
                capturedAt = request.startedAt,
            ),
        )

        database.evidenceDao().insertEvidence(metadata.toEntity())
        encryptedChunks.forEach { chunk ->
            database.evidenceChunkDao().upsertChunk(chunk.entity)
        }
        request.outputFile.delete()
        EvidenceUploadWorker.enqueue(context, request.evidenceId)
    }

    private fun encryptChunks(request: CapturedEvidenceQueueRequest): List<EncryptedChunk> {
        return request.outputFile.inputStream().buffered().use { input ->
            val chunks = mutableListOf<EncryptedChunk>()
            var chunkIndex = 0
            var plaintext = input.readNextChunk()
            while (plaintext.isNotEmpty()) {
                chunks += encryptChunk(request, chunkIndex, plaintext)
                chunkIndex += 1
                plaintext = input.readNextChunk()
            }
            chunks
        }
    }

    private fun encryptChunk(
        request: CapturedEvidenceQueueRequest,
        chunkIndex: Int,
        plaintext: ByteArray,
    ): EncryptedChunk {
        val encryptedFile = fileStore.writeChunk(request.evidenceId, chunkIndex, plaintext)
        val encryptedBytes = encryptedFile.readBytes()
        val sha256 = hasher.hashChunk(encryptedBytes)
        val capturedAt = request.startedAt.plusMillis(chunkIndex.toLong())
        return EncryptedChunk(
            hash = EvidenceChunkHash(
                evidenceId = request.evidenceId,
                chunkIndex = chunkIndex,
                sha256 = sha256,
                capturedAt = capturedAt,
                sizeBytes = encryptedBytes.size.toLong(),
            ),
            entity = EvidenceChunkEntity(
                id = "${request.evidenceId}-$chunkIndex",
                evidenceId = request.evidenceId,
                chunkIndex = chunkIndex,
                sha256 = sha256,
                encryptedFilePath = encryptedFile.absolutePath,
                sizeBytes = encryptedBytes.size.toLong(),
                capturedAtEpochMillis = capturedAt.toEpochMilli(),
            ),
        )
    }

    private fun org.witness.app.domain.model.EvidenceMetadata.toEntity(): EvidenceEntity {
        return EvidenceEntity(
            id = evidenceId,
            mediaType = mediaType.name,
            captureMode = captureMode.name,
            merkleRoot = merkleRoot,
            capturedAtEpochMillis = capturedAt.toEpochMilli(),
            deviceManufacturer = device.manufacturer,
            deviceModel = device.model,
            androidVersion = device.androidVersion,
            appVersion = appVersion,
            latitude = location?.latitude,
            longitude = location?.longitude,
            locationAccuracyMeters = location?.accuracyMeters,
            locationUnavailableReason = locationUnavailableReason,
        )
    }

    private fun java.io.InputStream.readNextChunk(): ByteArray {
        val buffer = ByteArray(MAX_PLAINTEXT_CHUNK_BYTES)
        val bytesRead = read(buffer)
        return when {
            bytesRead <= 0 -> ByteArray(0)
            bytesRead == buffer.size -> buffer
            else -> buffer.copyOf(bytesRead)
        }
    }

    private data class EncryptedChunk(
        val hash: EvidenceChunkHash,
        val entity: EvidenceChunkEntity,
    )
}

data class CapturedEvidenceQueueRequest(
    val evidenceId: String,
    val outputFile: File,
    val mediaType: MediaType,
    val captureMode: CaptureMode,
    val startedAt: Instant,
)
