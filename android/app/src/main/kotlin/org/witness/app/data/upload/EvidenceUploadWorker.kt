package org.witness.app.data.upload

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.time.Instant
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.witness.app.data.local.evidence.EvidenceCacheDatabase
import org.witness.app.data.local.evidence.EvidenceChunkEntity
import org.witness.app.data.local.evidence.EvidenceEntity
import org.witness.app.data.local.evidence.UploadStatus
import org.witness.app.data.remote.evidence.DevicePayload
import org.witness.app.data.remote.evidence.EvidenceMetadataPayload
import org.witness.app.data.remote.evidence.EvidenceUploadApi
import org.witness.app.data.remote.evidence.LocationPayload
import org.witness.app.data.remote.evidence.RegisterHashRequest
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val EVIDENCE_ID_KEY = "evidence_id"
private const val UPLOAD_WORK_PREFIX = "upload_"
private const val INITIAL_BACKOFF_SECONDS = 30L
private const val DEFAULT_NODE_BASE_URL = "http://10.0.2.2:8080/"
private const val OCTET_STREAM = "application/octet-stream"
private const val SHA256_PREFIX = "sha256:"

class EvidenceUploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val evidenceId = inputData.getString(EVIDENCE_ID_KEY) ?: return Result.failure()
        val database = EvidenceCacheDatabase.create(applicationContext)
        val evidenceDao = database.evidenceDao()
        val chunkDao = database.evidenceChunkDao()
        val evidence = evidenceDao.getEvidence(evidenceId) ?: return Result.failure()
        val pendingChunks = chunkDao.getPendingChunks(evidenceId)
        val api = uploadApi()

        if (pendingChunks.isEmpty()) return Result.success()

        return runCatching {
            evidenceDao.markEvidenceStatus(evidenceId, UploadStatus.Uploading.name)
            registerHash(api, evidence)
            uploadChunks(api, evidence, pendingChunks)
            val confirmedAt = System.currentTimeMillis()
            evidenceDao.markEvidenceUploadConfirmed(
                evidenceId = evidenceId,
                status = UploadStatus.Complete.name,
                confirmedAtEpochMillis = confirmedAt,
                deleteAfterEpochMillis = EvidenceEntity.deletionDeadline(confirmedAt),
            )
            Result.success()
        }.getOrElse {
            evidenceDao.markEvidenceStatus(evidenceId, UploadStatus.FailedRetryable.name)
            pendingChunks.forEach { chunk ->
                chunkDao.markChunkStatus(
                    evidenceId = evidenceId,
                    chunkIndex = chunk.chunkIndex,
                    status = UploadStatus.FailedRetryable.name,
                )
            }
            Result.retry()
        }
    }

    private suspend fun registerHash(api: EvidenceUploadApi, evidence: EvidenceEntity) {
        val hash = evidence.merkleRoot ?: return
        api.registerHash(
            evidenceId = evidence.id,
            request = RegisterHashRequest(
                evidenceId = evidence.id,
                hash = hash.withSha256Prefix(),
                timestamp = Instant.ofEpochMilli(evidence.capturedAtEpochMillis).toString(),
                metadata = evidence.toMetadataPayload(),
            ),
        )
    }

    private suspend fun uploadChunks(
        api: EvidenceUploadApi,
        evidence: EvidenceEntity,
        chunks: List<EvidenceChunkEntity>,
    ) {
        val chunkDao = EvidenceCacheDatabase.create(applicationContext).evidenceChunkDao()
        chunks.forEach { chunk ->
            val chunkFile = File(chunk.encryptedFilePath)
            require(chunkFile.exists()) { "encrypted chunk file missing" }
            api.uploadChunk(
                evidenceId = evidence.id,
                chunkIndex = chunk.chunkIndex,
                chunkHash = chunk.sha256.withSha256Prefix(),
                evidenceHash = evidence.merkleRoot.orEmpty().withSha256Prefix(),
                chunkBody = chunkFile.asRequestBody(OCTET_STREAM.toMediaType()),
            )
            chunkDao.markChunkUploaded(
                evidenceId = evidence.id,
                chunkIndex = chunk.chunkIndex,
                uploadedAtEpochMillis = System.currentTimeMillis(),
            )
        }
    }

    private fun uploadApi(): EvidenceUploadApi {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl(DEFAULT_NODE_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EvidenceUploadApi::class.java)
    }

    private fun EvidenceEntity.toMetadataPayload(): EvidenceMetadataPayload {
        val locationPayload = if (latitude == null || longitude == null || locationAccuracyMeters == null) {
            null
        } else {
            LocationPayload(
                latitude = latitude,
                longitude = longitude,
                altitude = null,
                accuracyMeters = locationAccuracyMeters,
                provider = locationUnavailableReason ?: "unknown",
            )
        }
        return EvidenceMetadataPayload(
            appVersion = appVersion,
            captureMode = captureMode,
            mediaType = mediaType,
            device = DevicePayload(
                manufacturer = deviceManufacturer,
                model = deviceModel,
                androidVersion = androidVersion,
                fingerprint = "",
            ),
            location = locationPayload,
            timeSource = "Device",
        )
    }

    private fun String.withSha256Prefix(): String {
        return if (startsWith(SHA256_PREFIX)) this else "$SHA256_PREFIX$this"
    }

    companion object {
        fun enqueue(context: Context, evidenceId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<EvidenceUploadWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(EVIDENCE_ID_KEY to evidenceId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    INITIAL_BACKOFF_SECONDS,
                    TimeUnit.SECONDS,
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$UPLOAD_WORK_PREFIX$evidenceId",
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }
}
