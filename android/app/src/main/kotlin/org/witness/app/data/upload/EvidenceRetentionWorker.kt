package org.witness.app.data.upload

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import org.witness.app.data.local.evidence.EvidenceCacheDatabase

private const val RETENTION_WORK_NAME = "evidence_retention_cleanup"
private const val RETENTION_INTERVAL_HOURS = 6L

class EvidenceRetentionWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = EvidenceCacheDatabase.create(applicationContext)
        val evidenceDao = database.evidenceDao()
        val chunkDao = database.evidenceChunkDao()
        val fileDeleter = EvidenceFileDeleter()
        val now = System.currentTimeMillis()

        evidenceDao.getEvidenceReadyForDeletion(now).forEach { evidence ->
            val chunks = chunkDao.getChunks(evidence.id)
            val deletionResult = fileDeleter.delete(chunks.map { it.encryptedFilePath })
            if (!deletionResult.succeeded) return Result.retry()

            evidenceDao.deleteEvidence(evidence.id)
        }

        return Result.success()
    }

    companion object {
        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            val request = PeriodicWorkRequestBuilder<EvidenceRetentionWorker>(
                RETENTION_INTERVAL_HOURS,
                TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RETENTION_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
