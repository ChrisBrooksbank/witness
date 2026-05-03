package org.witness.app.data.local.evidence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvidence(evidence: EvidenceEntity)

    @Query("SELECT * FROM evidence WHERE id = :evidenceId")
    suspend fun getEvidence(evidenceId: String): EvidenceEntity?

    @Query("SELECT * FROM evidence WHERE upload_status != :completeStatus ORDER BY captured_at_epoch_millis ASC")
    fun observePendingEvidence(completeStatus: String = UploadStatus.Complete.name): Flow<List<EvidenceEntity>>

    @Query(
        """
        UPDATE evidence
        SET upload_status = :status,
            confirmed_uploaded_at_epoch_millis = :confirmedAtEpochMillis,
            delete_after_epoch_millis = :deleteAfterEpochMillis
        WHERE id = :evidenceId
        """,
    )
    suspend fun markEvidenceUploadConfirmed(
        evidenceId: String,
        status: String,
        confirmedAtEpochMillis: Long,
        deleteAfterEpochMillis: Long,
    )

    @Query("UPDATE evidence SET upload_status = :status WHERE id = :evidenceId")
    suspend fun markEvidenceStatus(evidenceId: String, status: String)

    @Query("SELECT * FROM evidence WHERE delete_after_epoch_millis IS NOT NULL AND delete_after_epoch_millis <= :now")
    suspend fun getEvidenceReadyForDeletion(now: Long): List<EvidenceEntity>

    @Query("DELETE FROM evidence WHERE id = :evidenceId")
    suspend fun deleteEvidence(evidenceId: String)
}
