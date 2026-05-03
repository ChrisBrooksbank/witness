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
import java.util.concurrent.TimeUnit
import org.witness.app.data.local.evidence.EvidenceCacheDatabase
import org.witness.app.data.local.evidence.UploadStatus

private const val EVIDENCE_ID_KEY = "evidence_id"
private const val UPLOAD_WORK_PREFIX = "upload_"
private const val INITIAL_BACKOFF_SECONDS = 30L

class EvidenceUploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val evidenceId = inputData.getString(EVIDENCE_ID_KEY) ?: return Result.failure()
        val database = EvidenceCacheDatabase.create(applicationContext)
        val evidenceDao = database.evidenceDao()
        val chunkDao = database.evidenceChunkDao()
        val pendingChunks = chunkDao.getPendingChunks(evidenceId)

        if (pendingChunks.isEmpty()) return Result.success()

        evidenceDao.markEvidenceStatus(evidenceId, UploadStatus.FailedRetryable.name)
        pendingChunks.forEach { chunk ->
            chunkDao.markChunkStatus(
                evidenceId = evidenceId,
                chunkIndex = chunk.chunkIndex,
                status = UploadStatus.FailedRetryable.name,
            )
        }

        return Result.retry()
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
